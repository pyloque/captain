package captain;

import org.json.JSONObject;

public class KvItem implements Comparable<KvItem>{

	private String key;

	private JSONObject value;

	private long version;

	public String getKey() {
		return key;
	}

	public KvItem setKey(String key) {
		this.key = key;
		return this;
	}

	public JSONObject getValue() {
		return value;
	}
	
	public KvItem setValue(JSONObject value) {
		this.value = value;
		return this;
	}

	public long getVersion() {
		return version;
	}

	public KvItem setVersion(long version) {
		this.version = version;
		return this;
	}
	
	public String getBeatifiedValue() {
		return this.value.toString(4);
	}

	@Override
	public int compareTo(KvItem o) {
		return this.key.compareTo(o.key);
	}

}
