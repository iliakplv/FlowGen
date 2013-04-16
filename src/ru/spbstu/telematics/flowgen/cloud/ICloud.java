package ru.spbstu.telematics.flowgen.cloud;


import ru.spbstu.telematics.flowgen.application.configuration.CloudConfig;
import ru.spbstu.telematics.flowgen.cloud.rabbitmq.NovaNetworkQueueListener;
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

	// Datapath listeners
	public void addDatapathListener(IDatapathListener listener);
	public void deleteDatapathListener(IDatapathListener listener);
	public Set<IDatapathListener> getDatapathListeners();
	public void clearDatapathListeners();

	// Devices
	public void launchGateway(String mac, String dpid, int port);
	public void launchHost(String mac, String dpid, int port);
	public void stopDevice(String mac);
	public void pauseDevice(String mac);
	public void wakeDevice(String mac);
	public void migrateDevice(String mac, String dstDpid, int dstPort);
	public DeviceState getDeviceState(String mac);
	public Set<String> getAllDevicesMacs();

	// Controller connectivity
	public void setFloodlightClient(IFloodlightClient client);
	public IFloodlightClient getFloodlightClient();
	public void findAndConnect(String ip);
	public void findAndDisconnect(String ip);

	// Nova listeners
	public void addNovaListener(NovaNetworkQueueListener listener);
	public void deleteNovaListener(NovaNetworkQueueListener listener);
	public Set<NovaNetworkQueueListener> getNovaListeners();
	public void clearNovaListeners();

	public CloudConfig getConfig();

}
