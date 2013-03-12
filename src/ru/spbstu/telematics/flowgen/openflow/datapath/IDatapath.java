package ru.spbstu.telematics.flowgen.openflow.datapath;


import ru.spbstu.telematics.flowgen.openflow.rules.IFirewallRule;

import java.util.List;


public interface IDatapath {

	// Virtual Machines
	public void connectVm(String mac, int port);
	public void disconnectVm(String mac);
	public void disconnectVm(int port);
	public IFirewallRule getVmRule(String mac);
	public IFirewallRule getVmRule(int port);
	public List<IFirewallRule> getAllVmRules();

	// Network
	public void connectToNetwork();
	public void disconnectFromNetwork();
	public List<IFirewallRule> getAllNetworkRules();

	// All rules
	public List<IFirewallRule> getAllRules();

	// Listeners
	public void registerListener(IDatapathListener listener);
	public void unregisterListener(IDatapathListener listener);

}
