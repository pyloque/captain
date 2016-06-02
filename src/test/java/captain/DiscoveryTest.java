package captain;

import java.io.IOException;
import java.net.ServerSocket;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import junit.framework.Assert;

public class DiscoveryTest {

	private Bootstrap bootstrap;
	private String urlRoot;
	private String name = "sample_service";
	private String key = "sample_key";

	@Before
	public void setup() {
		bootstrap = new Bootstrap();
		Config config = new Config();
		config.bindHost("localhost").bindPort(randomPort()).redisHost("localhost").redisPort(6379).interval(1000)
				.inifile(null);
		bootstrap.initWithConfig(config);
		bootstrap.start();
		this.urlRoot = "http://localhost:" + bootstrap.config().bindPort();
		this.clean();
	}

	private int randomPort() {
		ServerSocket socket;
		try {
			socket = new ServerSocket(0);
			int port = socket.getLocalPort();
			socket.close();
			return port;
		} catch (IOException e) {
			throw new RuntimeException("impossible here");
		}
	}

	@Test
	public void testDiscovery() throws UnirestException {
		JSONObject js = Unirest.get(urlRoot + "/api/service/keep").queryString("name", name)
				.queryString("host", "localhost").queryString("port", 6000).queryString("ttl", 30).asJson().getBody()
				.getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		js = Unirest.get(urlRoot + "/api/service/version").queryString("name", name).asJson().getBody().getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		Assert.assertEquals(1, js.getJSONObject("versions").getInt(name));
		js = Unirest.get(urlRoot + "/api/service/set").queryString("name", name).asJson().getBody().getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		Assert.assertEquals(1, js.getInt("version"));
		Assert.assertEquals(1, js.getJSONArray("services").length());
		JSONObject item = js.getJSONArray("services").getJSONObject(0);
		Assert.assertEquals("localhost", item.getString("host"));
		Assert.assertEquals(6000, item.getInt("port"));
		
		js = Unirest.get(urlRoot + "/api/service/keep").queryString("name", name)
				.queryString("host", "localhost").queryString("port", 6001).queryString("ttl", 30).asJson().getBody()
				.getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		js = Unirest.get(urlRoot + "/api/service/version").queryString("name", name).asJson().getBody().getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		Assert.assertEquals(2, js.getJSONObject("versions").getInt(name));
		js = Unirest.get(urlRoot + "/api/service/set").queryString("name", name).asJson().getBody().getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		Assert.assertEquals(2, js.getInt("version"));
		Assert.assertEquals(2, js.getJSONArray("services").length());
		item = js.getJSONArray("services").getJSONObject(0);
		Assert.assertEquals("localhost", item.getString("host"));
		Assert.assertEquals(6000, item.getInt("port"));
		item = js.getJSONArray("services").getJSONObject(1);
		Assert.assertEquals("localhost", item.getString("host"));
		Assert.assertEquals(6001, item.getInt("port"));
	}

	@Test
	public void testKv() throws UnirestException {
		JSONObject value = new JSONObject("{\"a\": 5, \"b\": 3}");
		JSONObject js = Unirest.post(urlRoot + "/api/kv/set").field("key", key).field("value", value).asJson().getBody()
				.getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		js = Unirest.get(urlRoot + "/api/kv/get").queryString("key", key).asJson().getBody().getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		JSONObject kv = js.getJSONObject("kv").getJSONObject("value");
		long version = js.getJSONObject("kv").getLong("version");
		Assert.assertEquals(kv.getInt("a"), 5);
		Assert.assertEquals(kv.getInt("b"), 3);
		Assert.assertEquals(version, 1);
		
		value = new JSONObject("{\"a\": 4, \"b\": 2}");
		js = Unirest.post(urlRoot + "/api/kv/set").field("key", key).field("value", value).asJson().getBody()
				.getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		js = Unirest.get(urlRoot + "/api/kv/get").queryString("key", key).asJson().getBody().getObject();
		Assert.assertTrue(js.getBoolean("ok"));
		kv = js.getJSONObject("kv").getJSONObject("value");
		version = js.getJSONObject("kv").getLong("version");
		Assert.assertEquals(kv.getInt("a"), 4);
		Assert.assertEquals(kv.getInt("b"), 2);
		Assert.assertEquals(version, 2);
	}

	@After
	public void teardown() {
		clean();
		bootstrap.halt();
	}

	private void clean() {
		bootstrap.redis().execute(jedis -> {
			jedis.del("service_version_" + name);
			jedis.del("service_set_" + name);
			jedis.srem("service_names", name);
			jedis.del("kv_version_" + key);
			jedis.del("kv_item_" + key);
			jedis.srem("kv_g_keys", key);
		});
	}

}
