package captain;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.ini4j.Wini;

public class Config {

	private String bindHost = "0.0.0.0";
	private int bindPort = 6789;
	private String redisHost = "localhost";
	private int redisPort = 6379;
	private int redisDb = 0;
	private int interval = 1000;
	private int threadNum = Runtime.getRuntime().availableProcessors();
	private boolean urgent = false;
	private String inifile = System.getProperty("user.home") + "/.captain/captain.ini";

	public String bindHost() {
		return bindHost;
	}

	public Config bindHost(String bindHost) {
		this.bindHost = bindHost;
		return this;
	}

	public int bindPort() {
		return bindPort;
	}

	public Config bindPort(int bindPort) {
		this.bindPort = bindPort;
		return this;
	}

	public URI redisUri() {
		return URI.create(String.format("redis://%s:%s/%s", redisHost, redisPort, redisDb));
	}

	public String redisHost() {
		return redisHost;
	}

	public Config redisHost(String redisHost) {
		this.redisHost = redisHost;
		return this;
	}

	public int redisPort() {
		return redisPort;
	}

	public Config redisPort(int redisPort) {
		this.redisPort = redisPort;
		return this;
	}

	public int interval() {
		return interval;
	}

	public Config interval(int interval) {
		this.interval = interval;
		return this;
	}

	public boolean readonly() {
		return interval == 0;
	}

	public String inifile() {
		return inifile;
	}

	public Config inifile(String inifile) {
		this.inifile = inifile;
		return this;
	}
	
	public Config threadNum(int threadNum) {
		this.threadNum = threadNum;
		return this;
	}
	
	public int threadNum() {
		return threadNum;
	}

	public int redisDb() {
		return redisDb;
	}

	public Config redisDb(int redisDb) {
		this.redisDb = redisDb;
		return this;
	}

	public Config urgent(boolean urgent) {
		this.urgent = urgent;
		return this;
	}

	public boolean urgent() {
		return this.urgent;
	}

	public void save() throws IOException {
		if (this.inifile == null || this.inifile.isEmpty()) {
			return;
		}
		File f = new File(this.inifile);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		Wini ini = new Wini(f);
		ini.put("server", "host", this.bindHost);
		ini.put("server", "port", this.bindPort);
		ini.put("server", "thread", this.threadNum);
		ini.put("redis", "host", this.redisHost);
		ini.put("redis", "port", this.redisPort);
		ini.put("redis", "db", this.redisDb);
		ini.put("watch", "interval", this.interval);
		ini.store();
	}

	public void load() throws IOException {
		if (this.inifile == null || this.inifile.isEmpty()) {
			return;
		}
		File f = new File(this.inifile);
		if (f.exists() && f.isFile()) {
			Wini ini = new Wini(f);
			String bindHost = ini.get("server", "host", String.class);
			Integer bindPort = ini.get("server", "port", Integer.class);
			Integer threadNum = ini.get("server", "thread", Integer.class);
			String redisHost = ini.get("redis", "host", String.class);
			Integer redisPort = ini.get("redis", "port", Integer.class);
			Integer redisDb = ini.get("redis", "db", Integer.class);
			Integer interval = ini.get("watch", "interval", Integer.class);
			if (bindHost != null) {
				this.bindHost = bindHost;
			}
			if (bindPort != null) {
				this.bindPort = bindPort;
			}
			if(threadNum != null) {
				this.threadNum = threadNum;
			}
			if (redisHost != null) {
				this.redisHost = redisHost;
			}
			if (redisPort != null) {
				this.redisPort = redisPort;
			}
			if (redisDb != null) {
				this.redisDb = redisDb;
			}
			if (interval != null) {
				this.interval = interval;
			}
		}
	}

}
