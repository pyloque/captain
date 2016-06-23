package captain;

public class ServiceItem implements Comparable<ServiceItem> {

	private String name;
	private String host;
	private int port;
	private String payload;
	private int ttl;

	public ServiceItem(String name, String host, int port) {
		this(name, host, port, 0);
	}

	public ServiceItem(String name, String host, int port, int ttl) {
		this(name, host, port, ttl, "");
	}
	
	public ServiceItem(String name, String host, int port, int ttl, String payload) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.ttl = ttl;
		this.payload = payload;
	}
	
	public ServiceItem setPayload(String payload) {
		this.payload = payload;
		return this;
	}
	
	public String getPayload() {
		return this.payload;
	}

	public String getKey() {
		return String.format("%s:%s", host, port);
	}

	public String getName() {
		return name;
	}

	public int getTtl() {
		return ttl;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() & this.host.hashCode() & Integer.hashCode(this.port);
	}

	@Override
	public boolean equals(Object obj) {
		ServiceItem other = (ServiceItem) obj;
		return this.name.equals(other.name) && this.host.equals(other.host) && this.port == other.port;
	}

	@Override
	public int compareTo(ServiceItem o) {
		int delta = this.name.compareTo(o.name);
		if (delta != 0) {
			return delta;
		}
		return this.getKey().compareTo(o.getKey());
	}

}
