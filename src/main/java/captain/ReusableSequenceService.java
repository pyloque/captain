package captain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Transaction;

public class ReusableSequenceService {

	private RedisStore redis;

	public ReusableSequenceService(RedisStore redis) {
		this.redis = redis;
	}

	private static String keyFor(String name) {
		return String.format("reusable-seq:%s", name);
	}

	private static final String ALL_SEQS = "reusable-seqs";

	public int nextId(String name) {
		String key = keyFor(name);
		Holder<Integer> holder = new Holder<>();
		redis.execute(jedis -> {
			jedis.sadd(ALL_SEQS, name);
			long pos = 0;
			do {
				jedis.watch(key);
				pos = jedis.bitpos(key, false);
				if (pos < 0) {
					pos = 0;
				}
				Transaction tx = jedis.multi();
				tx.setbit(key, pos, true);
				if (tx.exec() != null) {
					break;
				}
			} while (true);
			holder.set((int) pos);
		});
		return holder.value();
	}

	public void deleteSeq(String name) {
		String key = keyFor(name);
		redis.execute(jedis -> {
			jedis.srem(ALL_SEQS, name);
			jedis.del(key);
		});
	}

	public Map<String, Integer> allSeqs() {
		Map<String, Integer> seqs = new HashMap<>();
		redis.execute(jedis -> {
			for (String name : jedis.smembers(ALL_SEQS)) {
				seqs.put(name, jedis.bitcount(keyFor(name)).intValue());
			}
		});
		return seqs;
	}
	
	public int getSlots(String name) {
		Holder<Long> holder = new Holder<>();
		redis.execute(jedis -> {
			holder.set(jedis.bitcount(keyFor(name)));
		});
		return holder.value().intValue();
	}

	public void releaseId(String name, int id) {
		String key = keyFor(name);
		redis.execute(jedis -> {
			jedis.setbit(key, id, false);
		});
	}

	public List<Boolean> listIds(String name) {
		String key = keyFor(name);
		List<Boolean> result = new ArrayList<>();
		redis.execute(jedis -> {
			byte[] values = jedis.get(key.getBytes());
			if (values != null) {
				for (byte value : values) {
					for (int i = 7; i >= 0; i--) {
						result.add((value & (1 << i)) != 0);
					}
				}
			}
		});
		return result;
	}

}
