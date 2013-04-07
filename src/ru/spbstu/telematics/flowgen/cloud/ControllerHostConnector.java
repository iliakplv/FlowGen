package ru.spbstu.telematics.flowgen.cloud;


import org.json.JSONException;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.AttachmentPoint;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Host;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;


public class ControllerHostConnector implements Runnable {

	private static final int ATTEMPTS = 10;
	private static final int INTERVAL_MILLIS = 5000;

	private ICloud cloud;
	private String mac;
	private String ip;


	public ControllerHostConnector(ICloud cloud, String mac, String ip) {
		if (cloud == null) {
			throw new NullPointerException("Cloud is null");
		}
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong MAC: " + mac);
		}
		if (StringUtils.isNullOrEmpty(ip)) {
			throw new IllegalArgumentException("Wrong IP (null or empty string)");
		}

		this.cloud = cloud;
		this.mac = mac.toLowerCase();
		this.ip = ip;
	}


	@Override
	public void run() {

		boolean launched = false;
		String dpid = "not_found";
		int port = -1;

		int i;
		for (i = 1; i <= ATTEMPTS; i++) {

			// Request controller for list of known hosts and parse it
			Hosts knownHosts = null;
			try {
				knownHosts = Hosts.parse(cloud.getFloodlightClient().getAllKnownHosts());
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}

			// Search for launched host by MAC
			for (Host host : knownHosts.getAllHosts()) {
				if (ip.equals(host.getIpv4()) && OpenflowUtils.macEquals(mac, host.getMac())) {
					// Found!
					AttachmentPoint ap = host.getAttachmentPoint();
					dpid = ap.getDpid();
					port = ap.getPort();
					cloud.launchHost(mac, dpid, port);
					launched = true;
					break;
				}
			}
			if (launched) {
				break;
			}

			// Wait for time interval
			try {
				synchronized (this) {
					this.wait(INTERVAL_MILLIS);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}

		if (launched) {
			System.out.println("[INFO] VM with MAC (" + mac + ") and IP (" + ip +
					") launched by connector on port (" + port +
					") of DPID (" + dpid + ") in attempt #" + i);
		} else {
			System.out.println("[ERROR] VM with MAC (" + mac + ") and IP (" + ip +
					") not launched by connector (not found in list of known hosts)");
		}

	}
}
