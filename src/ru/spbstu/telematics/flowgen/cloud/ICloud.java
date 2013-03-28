package ru.spbstu.telematics.flowgen.cloud;


import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;

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
	public void launchVm(String mac, String dpid, int port);
	public void pauseVm(String mac);
	public void wakeVm(String mac);
	public void stopVm(String mac);
	public void migrateVm(String mac, String dstDpid, int dstPort);
	public Set<String> getAllVmMacs();

}
