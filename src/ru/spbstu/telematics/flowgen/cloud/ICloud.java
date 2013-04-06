package ru.spbstu.telematics.flowgen.cloud;


import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;
import ru.spbstu.telematics.flowgen.openflow.floodlight.IFloodlightClient;

import java.util.Set;


public interface ICloud {

	// Datapath
	public void addDatapath(IDatapath datapath);
	public void deleteDatapath(String dpid);
	public IDatapath getDatapath(String dpid);
	public Set<String> getAllDpids();

	// Listeners
	public void addDatapathListener(IDatapathListener listener);
	public void deleteDatapathListener(IDatapathListener listener);
	public Set<IDatapathListener> getAllListeners();
	public void clearListeners();

	// Virtual Machines
	public void launchHost(String mac, String dpid, int port);
	public void pauseHost(String mac);
	public void wakeHost(String mac);
	public void stopHost(String mac);
	public void migrateHost(String mac, String dstDpid, int dstPort);
	public Set<String> getAllHostsMacs();

	// Controller connectivity
	public void setFloodlightClient(IFloodlightClient client);
	public IFloodlightClient getFloodlightClient();
	public void removeFloodlightClient();
	public boolean launchVmByMac(String mac);

}
