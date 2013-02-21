package ru.spbstu.telematics.flowgen.openflow;


import java.util.List;


public interface IDatapath {

	// Virtual Machines
	public IFirewallRule connectVm(String mac, int port);
	public IFirewallRule disconnectVm(String mac);
	public IFirewallRule disconnectVm(int port);
	public IFirewallRule getVmRule(String mac);
	public IFirewallRule getVmRule(int port);
	public List<IFirewallRule> getAllVmRules();

	// Gateway
	public IFirewallRule connectGateway();
	public IFirewallRule disconnectGateway();
	public IFirewallRule getGatewayRule();

	// Subnet
	public IFirewallRule connectSubnet();
	public IFirewallRule disconnectSubnet();
	public IFirewallRule getSubnetRule();

	// All rules
	public List<IFirewallRule> getAllRules();

}
