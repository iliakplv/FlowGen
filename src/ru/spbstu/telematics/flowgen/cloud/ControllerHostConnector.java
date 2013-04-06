package ru.spbstu.telematics.flowgen.cloud;


import org.json.JSONException;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.AttachmentPoint;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Host;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public class ControllerHostConnector implements Runnable {

	// TODO attempts
	private static final int ATTEMPTS = 3;
	private static final int INTERVAL_MILLIS = 1000;

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
		if (!OpenflowUtils.validateIpv4(ip)) {
			throw new IllegalArgumentException("Wrong IP: " + ip);
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

		for (int i = 0; i < ATTEMPTS; i++) {

			Hosts knownHosts = null;
			try {
				knownHosts = Hosts.parse(cloud.getFloodlightClient().getAllKnownHosts());
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}

			for (Host host : knownHosts.getAllHosts()) {
				if (ip.equals(host.getIpv4()) && OpenflowUtils.macEquals(mac, host.getMac())) {
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
			System.out.println("[INFO] VM with MAC (" + mac + ") and IP (" + ip + ") launched by connector on port (" + port + ") of DPID (" + dpid + ") ");
		} else {
			System.out.println("[WARNING] VM with MAC (" + mac + ") and IP (" + ip + ") not launched by connector.");
		}

	}
}
