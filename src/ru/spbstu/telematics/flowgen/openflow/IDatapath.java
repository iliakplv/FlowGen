package ru.spbstu.telematics.flowgen.openflow;


import java.util.List;


public interface IDatapath {

	// Virtual Machines
	public void connectVm(String mac, int port);
	public void disconnectVm(String mac);
	public void disconnectVm(int port);
	public IFirewallRule getVmRule(String mac);
	public IFirewallRule getVmRule(int port);
	public List<IFirewallRule> getAllVmRules();

	// Gateway
	public void connectGateway();
	public void disconnectGateway();
	public IFirewallRule getGatewayRule();

	// Broadcast
	public void connectBroadcast();
	public void disconnectBroadcast();
	public IFirewallRule getBroadcastRule();

	// Subnet
	public void connectSubnet();
	public void disconnectSubnet();
	public IFirewallRule getSubnetRule();

	// All rules
	public List<IFirewallRule> getAllRules();

	// Listeners
	public void registerListener(IDatapathListener listener);
	public void unregisterListener(IDatapathListener listener);

}
