package captain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import redis.clients.jedis.Response;

public class KvService {

	private RedisStore redis;

	public KvService(RedisStore redis) {
		this.redis = redis;
	}

	public void set(KvItem item) {
		redis.pipeline(pipe -> {
			pipe.set(keyForItem(item.getKey()), item.getValue().toString());
			pipe.sadd(globalAllKeys, item.getKey());
			pipe.incr(keyForVersion(item.getKey()));
			pipe.incr(globalVersionKey);
		});
	}

	public KvItem get(String key) {
		Holder<Response<String>> js = new Holder<Response<String>>();
		Holder<Response<Long>> version = new Holder<Response<Long>>();
		redis.pipeline(pipe -> {
			js.set(pipe.get(keyForItem(key)));
			version.set(pipe.incrBy(keyForVersion(key), 0));
		});
		KvItem item = new KvItem();
		item.setKey(key);
		item.setVersion(version.value().get());
		if (js.value().get() != null) {
			item.setValue(new JSONObject(js.value().get()));
		} else {
			item.setValue(new JSONObject());
		}
		return item;
	}

	public void delete(String key) {
		redis.pipeline(pipe -> {
			pipe.del(keyForItem(key));
			pipe.del(keyForVersion(key));
			pipe.srem(globalAllKeys, key);
			pipe.incr(globalVersionKey);
		});
	}

	private static String keyForVersion(String key) {
		return "kv_version_" + key;
	}

	private static String keyForItem(String key) {
		return "kv_item_" + key;
	}

	private final static String globalVersionKey = "kv_g_version";
	private final static String globalAllKeys = "kv_g_keys";

	public long globalVersion() {
		Holder<Long> version = new Holder<Long>();
		redis.execute(jedis -> {
			version.set(jedis.incrBy(globalVersionKey, 0));
		});
		return version.value();
	}

	public Set<String> allKeys() {
		Holder<Set<String>> holder = new Holder<Set<String>>();
		redis.execute(jedis -> {
			holder.set(jedis.smembers(globalAllKeys));
		});
		return holder.value();
	}

	public Map<String, Long> versions(String[] keys) {
		Map<String, Response<Long>> holders = new HashMap<String, Response<Long>>();
		redis.pipeline(pipe -> {
			for (String key : keys) {
				holders.put(key, pipe.incrBy(keyForVersion(key), 0));
			}
		});
		Map<String, Long> versions = new HashMap<String, Long>(keys.length);
		for (String key : keys) {
			versions.put(key, holders.get(key).get());
		}
		return versions;
	}

	public Map<String, KvItem> multiGet(String[] keys) {
		Map<String, Response<Long>> versions = new HashMap<String, Response<Long>>();
		Map<String, Response<String>> values = new HashMap<String, Response<String>>();
		redis.pipeline(pipe -> {
			for (String key : keys) {
				values.put(key, pipe.get(keyForItem(key)));
				versions.put(key, pipe.incrBy(keyForVersion(key), 0));
			}
		});
		Map<String, KvItem> items = new HashMap<String, KvItem>(keys.length);
		for (String key : keys) {
			KvItem item = new KvItem();
			item.setKey(key);
			String js = values.get(key).get();
			if (js != null) {
				item.setValue(new JSONObject(js));
			} else {
				item.setValue(new JSONObject());
			}
			long version = versions.get(key).get();
			item.setVersion(version);
			items.put(key, item);
		}
		return items;
	}
}
