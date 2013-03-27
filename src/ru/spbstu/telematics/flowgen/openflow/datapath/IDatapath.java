package ru.spbstu.telematics.flowgen.openflow.datapath;


import ru.spbstu.telematics.flowgen.openflow.rules.IFirewallRule;

import java.util.List;
import java.util.Map;


public interface IDatapath {

	// VM
	public void connectVm(String mac, int port);
	public void disconnectVm(String mac);
	public IFirewallRule getVmRule(String mac);
	public List<IFirewallRule> getAllVmRules();
	public boolean containsVm(String mac);
	public int getVmPort(String mac);
	public Map<String, Integer> getVmTopology();

	// Network
	public void connectToNetwork();
	public void disconnectFromNetwork();
	public List<IFirewallRule> getAllNetworkRules();
	public boolean isConnectedToNetwork();
	public int getTrunkPort();
	public String getGatewayMac();
	public boolean isGatewayMac(String mac);

	// Firewall
	public int getFirewallPort();

	// All rules
	public List<IFirewallRule> getAllRules();

	// VM Migration
	public void migrateVm(String vmMac, IDatapath dstDatapath, int dstPort);

	// Listeners
	public void registerListener(IDatapathListener listener);
	public void unregisterListener(IDatapathListener listener);

}
