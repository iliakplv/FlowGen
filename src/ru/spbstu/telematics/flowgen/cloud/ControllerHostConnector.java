package ru.spbstu.telematics.flowgen.cloud;


import org.json.JSONException;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.AttachmentPoint;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Host;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public class ControllerHostConnector implements Runnable {

	private static final int ATTEMPTS = 10;
	private static final int INTERVAL_MILLIS = 5000;

	private ICloud cloud;
	private String mac;


	public ControllerHostConnector(ICloud cloud, String mac) {
		if (cloud == null) {
			throw new NullPointerException("Cloud is null");
		}
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong MAC: " + mac);
		}

		this.cloud = cloud;
		this.mac = mac.toLowerCase();
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
				if (OpenflowUtils.macEquals(mac, host.getMac())) {
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
			System.out.println("[INFO] VM with MAC (" + mac +
					") launched by connector on port (" + port +
					") of DPID (" + dpid + ") in attempt #" + i);
		} else {
			System.out.println("[ERROR] VM with MAC (" + mac +
					") not launched by connector (not found in list of known hosts)");
		}

	}
}
