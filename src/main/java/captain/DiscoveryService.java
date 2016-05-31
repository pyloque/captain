package captain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

public class DiscoveryService {

	private RedisStore redis;

	public DiscoveryService(RedisStore redis) {
		this.redis = redis;
	}

	/**
	 * Service name's Version key
	 * 
	 * @param name
	 * @return
	 */
	private static String keyForVersion(String name) {
		return "service_version_" + name;
	}

	/**
	 * Service set's key
	 * 
	 * @param name
	 * @return
	 */
	private static String keyForSet(String name) {
		return "service_set_" + name;
	}

	/* All Service names */
	private final static String nameKeys = "service_g_names";
	/* Global Service Version */
	private final static String globalVersionKey = "service_g_version";

	public Set<String> allNames() {
		Holder<Set<String>> names = new Holder<Set<String>>();
		this.redis.execute(jedis -> {
			names.set(jedis.smembers(nameKeys));
		});
		return names.value();
	}

	public void keep(ServiceItem item) {
		long now = System.currentTimeMillis() / 1000;
		Holder<Long> count = new Holder<Long>();
		this.redis.execute(jedis -> {
			jedis.sadd(nameKeys, item.getName());
			count.set(jedis.zadd(keyForSet(item.getName()), now + item.getTtl(), item.getKey()));
		});
		if (count.value() > 0) {
			this.redis.transaction(pipe -> {
				pipe.incr(keyForVersion(item.getName()));
				pipe.incr(globalVersionKey);
			});
		}
	}

	public void cancel(ServiceItem item) {
		List<String> watchedKeys = new ArrayList<String>(1);
		watchedKeys.add(keyForVersion(item.getName()));
		Holder<Double> score = new Holder<Double>();
		this.redis.execute(jedis -> {
			score.set(jedis.zscore(keyForSet(item.getName()), item.getKey()));
		});
		if (score.value() == null) {
			return;
		}
		this.redis.transaction(pipe -> {
			pipe.zrem(keyForSet(item.getName()), item.getKey());
			pipe.incr(keyForVersion(item.getName()));
			pipe.incr(globalVersionKey);
		});
	}

	public long version(String name) {
		Holder<Long> version = new Holder<Long>();
		this.redis.execute(jedis -> {
			version.set(jedis.incrBy(keyForVersion(name), 0));
		});
		return version.value();
	}

	public long globalVersion() {
		Holder<Long> version = new Holder<Long>();
		this.redis.execute(jedis -> {
			version.set(jedis.incrBy(globalVersionKey, 0));
		});
		return version.value();
	}

	public Map<String, Long> multiVersions(String[] names) {
		Holder<List<String>> versions = new Holder<List<String>>();
		String[] keys = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			keys[i] = keyForVersion(names[i]);
		}
		if (keys.length > 0) {
			this.redis.execute(jedis -> {
				versions.set(jedis.mget(keys));
			});
		}
		Map<String, Long> result = new HashMap<String, Long>();
		for (int i = 0; i < names.length; i++) {
			String version = versions.value().get(i);
			if (version == null) {
				version = "0";
			}
			result.put(names[i], Long.parseLong(version));
		}
		return result;
	}

	public Map<String, Integer> multiLens(String[] names) {
		Map<String, Response<Long>> holder = new HashMap<String, Response<Long>>();
		this.redis.transaction(pipe -> {
			for (String name : names) {
				holder.put(name, pipe.zcard(keyForSet(name)));
			}
		});
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (String name : names) {
			result.put(name, holder.get(name).get().intValue());
		}
		return result;
	}

	public ServiceSet serviceSet(String name) {
		Holder<Response<Long>> version = new Holder<Response<Long>>();
		Holder<Response<Set<Tuple>>> services = new Holder<Response<Set<Tuple>>>();
		this.redis.transaction(pipe -> {
			version.set(pipe.incrBy(keyForVersion(name), 0));
			services.set(pipe.zrangeWithScores(keyForSet(name), 0, -1));
		});
		Set<ServiceItem> items = new HashSet<ServiceItem>();
		long now = System.currentTimeMillis() / 1000;
		for (Tuple tuple : services.value().get()) {
			String[] pair = tuple.getElement().split(":");
			items.add(new ServiceItem(name, pair[0], Integer.parseInt(pair[1]), (int) (tuple.getScore() - now)));
		}
		ServiceSet set = new ServiceSet(name, items, version.value().get());
		if (set.isEmpty()) {
			this.redis.execute(jedis -> {
				jedis.srem(nameKeys, name);
			});
		}
		return set;
	}
	
	public void trimAllExpired() {
		Set<String> names = this.allNames();
		for(String name: names) {
			this.trimExpired(name);
		}
	}

	private void trimExpired(String name) {
		long now = System.currentTimeMillis() / 1000;
		Holder<Long> count = new Holder<Long>();
		this.redis.execute(jedis -> {
			count.set(jedis.zremrangeByScore(keyForSet(name), 0, now));
		});
		if (count.value() > 0) {
			this.redis.transaction(pipe -> {
				pipe.incr(keyForVersion(name));
				pipe.incr(globalVersionKey);
			});
		}
	}

}
