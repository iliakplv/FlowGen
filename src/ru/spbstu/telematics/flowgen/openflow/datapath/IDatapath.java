package ru.spbstu.telematics.flowgen.openflow.datapath;


import ru.spbstu.telematics.flowgen.openflow.rules.IFirewallRule;

import java.util.List;
import java.util.Map;


public interface IDatapath {

	public String getDpid();
	public String getName();

	// Hosts
	public void connectHost(String mac, int port, int flowPriority);
	public void connectHost(String mac, int port);
	public void disconnectHost(String mac);
	public IFirewallRule getHostRule(String mac);
	public List<IFirewallRule> getAllHostsRules();
	public boolean containsHost(String mac);
	public int getHostPort(String mac);
	public Map<String, Integer> getMacPortMap();
	public int getHostPriority(String mac);
	public Map<String, Integer> getMacPriorityMap();

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
