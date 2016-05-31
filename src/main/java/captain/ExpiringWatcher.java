package captain;

public class ExpiringWatcher extends Thread {
	
	private DiscoveryService discovery;
	private int interval;
	private boolean stop;
	
	public ExpiringWatcher(DiscoveryService discovery) {
		this.discovery = discovery;
	}
	
	public void run() {
		while(!stop) {
			this.discovery.trimAllExpired();
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				stop = true;
			}
		}
	}
	
	public ExpiringWatcher interval(int interval) {
		this.interval = interval;
		return this;
	}
	
	public void quit() {
		this.stop = true;
	}
	
}
