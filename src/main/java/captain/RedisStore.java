package captain;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

public class RedisStore {

	private JedisPool pool;

	public RedisStore(URI uri) {
		pool = new JedisPool(new JedisPoolConfig(), uri, 2000, 1000);
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

	public void pipeline(Consumer<Pipeline> func) {
		try (Jedis jedis = pool.getResource()) {
			try (Pipeline pipe = jedis.pipelined()) {
				pipe.multi();
				func.accept(pipe);
				pipe.exec();
			} catch (IOException e) {
				throw new RuntimeException("pipe close error", e);
			}
		}
	}

}
