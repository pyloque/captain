package captain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.exceptions.JedisConnectionException;
import spark.Spark;

public class Bootstrap {

	private final static Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

	public static void main(String[] args) {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.initialize(args);
		bootstrap.watch().start();
	}

	private Config config;

	private RedisStore redis;
	private DiscoveryService discovery;
	private KvService kv;
	private ExpiringWatcher watcher;
	private final static GsonTransformer jsonify = new GsonTransformer();
	private final static FreeMarkerEngine engine = new FreeMarkerEngine();
	private final static String jsonType = "application/json";

	public void initialize(String[] args) {
		Config config = new Config();
		if (args.length > 0) {
			config.inifile(args[0]);
		}
		try {
			config.load();
		} catch (IOException e) {
			LOG.error("load config file error", e);
			System.exit(-1);
		}
		this.initWithConfig(config);
	}

	public void initWithConfig(Config config) {
		this.config = config;
		this.redis = new RedisStore(config.redisUri());
		this.discovery = new DiscoveryService(this.redis);
		this.kv = new KvService(this.redis);
		this.watcher = new ExpiringWatcher(this.discovery);
		this.watcher.interval(config.interval()).setDaemon(true);
	}

	public void switchRedis() {
		if (!config.readonly()) {
			// shutdown watcher thread, entering maintenance mode
			this.watcher.quit();
			while (this.watcher.isAlive()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
			config.urgent(true);
		}
		RedisStore oldRedis = this.redis;
		this.initWithConfig(config);
		oldRedis.close();
	}

	public RedisStore redis() {
		return this.redis;
	}

	public Config config() {
		return config;
	}

	public Bootstrap watch() {
		if (!config.readonly()) {
			this.watcher.start();
		}
		return this;
	}

	public void start() {
		Spark.ipAddress(config.bindHost());
		Spark.port(config.bindPort());
		Spark.threadPool(config.threadNum());
		Spark.staticFileLocation("/static");

		if (!config.readonly()) {
			this.initWritableHandlers();
		}
		this.initReadonlyHandlers();
		this.initViewHandlers();
		this.initExceptionHandlers();

		Spark.awaitInitialization();
		LOG.warn("captain server started");
	}

	public void initExceptionHandlers() {
		Spark.exception(Exception.class, (exc, req, res) -> {
			LOG.error(String.format("error occured on path=%s", req.pathInfo()), exc);
			res.status(500);
		});
	}

	public void initWritableHandlers() {
		Spark.post("/api/kv/set", jsonType, (req, res) -> {
			String key = req.queryParams("key");
			String value = req.queryParams("value");
			KvItem item = new KvItem();
			item.setKey(key);
			Map<String, Object> result = new HashMap<String, Object>();
			try {
				item.setValue(new JSONObject(value));
				this.kv.set(item);
				result.put("ok", true);
			} catch (JSONException e) {
				result.put("ok", false);
				result.put("reason", value);
			}
			return result;
		}, jsonify);

		Spark.get("/api/kv/del", jsonType, (req, res) -> {
			String key = req.queryParams("key");
			this.kv.delete(key);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("ok", true);
			return result;
		}, jsonify);

		Spark.get("/api/service/keep", jsonType, (req, res) -> {
			String name = req.queryParams("name");
			String host = req.queryParams("host");
			String port = req.queryParams("port");
			String ttl = req.queryParams("ttl");
			String payload = req.queryParams("payload");
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("ok", false);
			if (Helpers.isEmpty(name) || Helpers.isEmpty(host) || Helpers.isEmpty(port) || !Helpers.isInteger(port)
					|| !Helpers.isInteger(ttl)) {
				res.status(400);
				result.put("reason", "params illegal");
			} else {
				if (payload == null) {
					payload = "";
				}
				this.discovery
						.keep(new ServiceItem(name, host, Integer.parseInt(port), Integer.parseInt(ttl), payload));
				result.put("ok", true);
			}
			return result;
		}, jsonify);

		Spark.get("/api/service/cancel", jsonType, (req, res) -> {
			String name = req.queryParams("name");
			String host = req.queryParams("host");
			String port = req.queryParams("port");
			Map<String, Object> result = new HashMap<String, Object>();
			if (Helpers.isEmpty(name) || Helpers.isEmpty(host) || Helpers.isEmpty(port) || !Helpers.isInteger(port)) {
				res.status(400);
				result.put("reason", "params illegal");
			} else {
				this.discovery.cancel(new ServiceItem(name, host, Integer.parseInt(port)));
				result.put("ok", true);
			}
			return result;
		}, jsonify);
	}

	public void initReadonlyHandlers() {
		Spark.get("/api/version", jsonType, (req, res) -> {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("ok", true);
			result.put("service.version", this.discovery.globalVersion());
			result.put("kv.version", this.kv.globalVersion());
			return result;
		}, jsonify);

		Spark.get("/api/kv/version", jsonType, (req, res) -> {
			Map<String, Object> result = new HashMap<String, Object>();
			String[] keys = req.queryMap("key").values();
			if (keys.length == 0) {
				result.put("ok", false);
				result.put("reason", "params illegal");
				return result;
			}
			Map<String, Long> versions = this.kv.versions(keys);
			result.put("ok", true);
			result.put("versions", versions);
			return result;
		}, jsonify);

		Spark.post("/api/kv/set", jsonType, (req, res) -> {
			String key = req.queryParams("key");
			String value = req.queryParams("value");
			Map<String, Object> result = new HashMap<String, Object>();
			try {
				JSONObject js = new JSONObject(value);
				KvItem item = new KvItem();
				item.setKey(key).setValue(js);
				this.kv.set(item);
				result.put("ok", true);
			} catch (JSONException e) {
				LOG.error("add key value error", e);
				result.put("ok", false);
				result.put("reason", "illegal json value");
			}
			return result;
		}, jsonify);

		Spark.get("/api/kv/get", jsonType, (req, res) -> {
			Map<String, Object> result = new HashMap<String, Object>();
			String key = req.queryParams("key");
			if (Helpers.isEmpty(key)) {
				result.put("ok", false);
				result.put("reason", "params illegal");
				return result;
			}
			KvItem item = this.kv.get(key);
			result.put("ok", true);
			result.put("kv", item);
			return result;
		}, jsonify);

		Spark.get("/api/kv/mget", jsonType, (req, res) -> {
			Map<String, Object> result = new HashMap<String, Object>();
			String[] names = req.queryMap("name").values();
			if (names.length == 0) {
				result.put("ok", false);
				result.put("reason", "params illegal");
				return result;
			}
			Map<String, KvItem> items = this.kv.multiGet(names);
			result.put("ok", true);
			result.put("kvs", items);
			return result;
		}, jsonify);

		Spark.get("/api/service/version", jsonType, (req, res) -> {
			String[] names = req.queryMap("name").values();
			Map<String, Object> result = new HashMap<String, Object>();
			Map<String, Long> versions = this.discovery.multiVersions(names);
			result.put("versions", versions);
			result.put("ok", true);
			return result;
		}, jsonify);

		Spark.get("/api/service/set", jsonType, (req, res) -> {
			String name = req.queryParams("name");
			Map<String, Object> result = new HashMap<String, Object>();
			if (Helpers.isEmpty(name)) {
				res.status(400);
				result.put("reason", "params illegal");
			} else {
				ServiceSet set = this.discovery.serviceSet(name);
				result.put("ok", true);
				result.put("version", set.version());
				result.put("services", set.items());
			}
			return result;
		}, jsonify);

	}

	public void initViewHandlers() {
		Spark.get("/ui/", (req, res) -> {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("config", config);
			try {
				Set<String> nameset = this.discovery.allNames();
				String[] names = new String[nameset.size()];
				nameset.toArray(names);
				Map<String, Integer> services = this.discovery.multiLens(names);
				long sversion = this.discovery.globalVersion();
				context.put("services", new TreeMap<String, Integer>(services));
				context.put("sversion", sversion);
				Set<String> keyset = this.kv.allKeys();
				long kversion = this.kv.globalVersion();
				context.put("kvs", new TreeSet<String>(keyset));
				context.put("kversion", kversion);
			} catch (JedisConnectionException e) {
				context.put("reason", e.toString());
				context.put("stacktraces", e.getStackTrace());
			}
			return Spark.modelAndView(context, "ui.ftl");
		}, engine);

		Spark.get("/ui/service/", (req, res) -> {
			String name = req.queryParams("name");
			ServiceSet set = this.discovery.serviceSet(name);
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("version", set.version());
			context.put("services", new TreeSet<ServiceItem>(set.items()));
			context.put("name", name);
			context.put("config", config);
			return Spark.modelAndView(context, "ui_service.ftl");
		}, engine);

		Spark.get("/ui/kv/", (req, res) -> {
			Map<String, Object> context = new HashMap<String, Object>();
			String key = req.queryParams("key");
			KvItem item = this.kv.get(key);
			context.put("kv", item);
			context.put("config", config);
			return Spark.modelAndView(context, "ui_kv.ftl");
		}, engine);

		Spark.get("/ui/kv/del", (req, res) -> {
			String key = req.queryParams("key");
			this.kv.delete(key);
			res.redirect("/ui/");
			return null;
		});

		Spark.get("/ui/kv/edit", (req, res) -> {
			Map<String, Object> context = new HashMap<String, Object>();
			String key = req.queryParams("key");
			if (key != null) {
				context.put("kv", this.kv.get(key));
			}
			context.put("config", config);
			return Spark.modelAndView(context, "ui_kv_edit.ftl");
		}, engine);

		Spark.post("/ui/kv/edit", (req, res) -> {
			String key = req.queryParams("key");
			String value = req.queryParams("value");
			try {
				JSONObject js = new JSONObject(value);
				KvItem item = new KvItem();
				item.setKey(key).setValue(js);
				this.kv.set(item);
			} catch (JSONException e) {
				LOG.error("add key value error", e);
			}
			res.redirect("/ui/kv/?key=" + key);
			return null;
		});

		Spark.get("/ui/config/", (req, res) -> {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("config", config);
			return Spark.modelAndView(context, "config_edit.ftl");
		}, engine);

		Spark.post("/ui/config/", (req, res) -> {
			String redisHost = req.queryParams("redisHost");
			int redisPort = Integer.parseInt(req.queryParams("redisPort"));
			int redisDb = Integer.parseInt(req.queryParams("redisDb"));
			config.redisHost(redisHost).redisPort(redisPort).redisDb(redisDb);
			try {
				config.save();
			} catch (IOException e) {
				LOG.error("save config error", e);
			}
			switchRedis();
			res.redirect("/ui/");
			return null;
		});

		Spark.get("/ui/watcher/start", (req, res) -> {
			this.watcher.start();
			config.urgent(false);
			res.redirect("/ui/");
			return null;
		});
	}

	public void halt() {
		if (this.watcher.isAlive()) {
			this.watcher.interrupt();
			this.watcher.quit();
		}
		this.redis.close();
		Spark.stop();
	}
}
