package captain;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisStore {

	private JedisPool pool;

	public RedisStore(URI uri) {
		pool = new JedisPool(new JedisPoolConfig(), uri, 2000,
				1000);
	}

	public RedisStore(JedisPool pool) {
		this.pool = pool;
	}

	public void close() {
		this.pool.close();
	}

	public void execute(Consumer<Jedis> func) {
		try (Jedis jedis = pool.getResource()) {
			func.accept(jedis);
		}
	}

	public boolean transaction(Consumer<Pipeline> func) {
		return this.transaction(Collections.emptyList(), func);
	}

	public boolean transaction(List<String> watchedKeys, Consumer<Pipeline> func) {
		Response<List<Object>> res;
		try (Jedis jedis = pool.getResource()) {
			try (Pipeline pipe = jedis.pipelined()) {
				pipe.multi();
				for (String key : watchedKeys) {
					pipe.watch(key);
				}
				func.accept(pipe);
				res = pipe.exec();
			} catch (IOException e) {
				throw new RuntimeException("pipe close error", e);
			}
		}
		return !res.get().isEmpty();
	}

}
