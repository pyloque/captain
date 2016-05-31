package captain;

import java.util.Set;

public class ServiceSet {

	private Set<ServiceItem> items;
	private long version;
	private String name;

	public ServiceSet(String name, Set<ServiceItem> items, long version) {
		this.name = name;
		this.items = items;
		this.version = version;
	}

	public Set<ServiceItem> items() {
		return items;
	}

	public long version() {
		return version;
	}
	
	public String name() {
		return this.name;
	}
	
	public boolean isEmpty() {
		return items.isEmpty();
	}

}
