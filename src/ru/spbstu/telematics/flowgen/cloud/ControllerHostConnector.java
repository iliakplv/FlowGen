package ru.spbstu.telematics.flowgen.cloud;


import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.AttachmentPoint;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Host;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;


public class ControllerHostConnector implements Runnable {

	private static final String INFO = "[INFO] ";
	private static final String ERROR = "[ERROR] ";

	// Attempting every 5 seconds for 2 minutes
	private static final int ATTEMPTS = 25;
	private static final int INTERVAL_MILLIS = 5000;

	private ICloud cloud;
	private Action action;
	private String ip;


	public ControllerHostConnector(ICloud cloud, String ip, Action action) {
		if (cloud == null) {
			throw new NullPointerException("Cloud is null");
		}
		if (cloud.getFloodlightClient() == null) {
			throw new NullPointerException("No floodlight client attached to cloud");
		}
		if (StringUtils.isNullOrEmpty(ip)) {
			throw new IllegalArgumentException("Wrong IP (null or empty string)");
		}

		this.cloud = cloud;
		this.action = action;
		this.ip = ip;
	}


	@Override
	public void run() {

		boolean done = false;
		String dpid = "<not_found>";
		String mac = "<unknown>";
		int port = OpenflowUtils.DEFAULT_PORT;

		int attempt;
		for (attempt = 1; attempt <= ATTEMPTS; attempt++) {

			// Request controller for list of known hosts and parse it
			Hosts knownHosts = cloud.getFloodlightClient().getKnownHosts();

			// Search for host by IP
			for (Host host : knownHosts.getAllHosts()) {
				if (ip.equals(host.getIpv4())) {
					// Found!

					mac = host.getMac();
					AttachmentPoint ap = host.getAttachmentPoint();
					dpid = ap.getDpid();
					port = ap.getPort();

					if (action == Action.Connect) {
						cloud.launchHost(mac, dpid, port);
					} else {
						cloud.stopDevice(mac);
					}

					done = true;
					break;
				}
			}
			if (done) {
				break;
			}

			// Wait for time interval
			if (attempt < ATTEMPTS) {
				try {
					synchronized (this) {
						this.wait(INTERVAL_MILLIS);
					}
				} catch (InterruptedException e) {
					System.out.println(ERROR + "Interrupted exception caught:");
					e.printStackTrace();
					return;
				}
			}
		}

		if (done) {
			if (action == Action.Connect) {
				System.out.println(INFO + "[+] VM with IP (" + ip +
						") and MAC (" + mac +
						") connected by connector on port (" + port +
						") of DPID (" + dpid + ") in attempt #" + attempt);
			} else {
				System.out.println(INFO +"[-] VM with IP (" + ip +
						") and MAC (" + mac +
						") disconnected by connector from port (" + port +
						") of DPID (" + dpid + ") in attempt #" + attempt);
			}
		} else {
			System.out.println(ERROR + (action == Action.Connect ? "[+]" : "[-]") +
					" VM with IP (" + ip + ") not found by connector in list of known hosts");
		}

	}

	/***** Inner Classes *****/

	public static enum Action {
		Connect,
		Disconnect
	}
}
