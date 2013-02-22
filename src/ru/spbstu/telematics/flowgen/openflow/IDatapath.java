package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;
import java.util.List;


public interface IDatapath {

	// Virtual Machines
	public JSONObject[] connectVm(String mac, int port);
	public JSONObject[] disconnectVm(String mac);
	public JSONObject[] disconnectVm(int port);
	public IFirewallRule getVmRule(String mac);
	public IFirewallRule getVmRule(int port);
	public List<IFirewallRule> getAllVmRules();

	// Gateway
	public JSONObject[] connectGateway();
	public JSONObject[] disconnectGateway();
	public IFirewallRule getGatewayRule();

	// Subnet
	public JSONObject[] connectSubnet();
	public JSONObject[] disconnectSubnet();
	public IFirewallRule getSubnetRule();

	// All rules
	public List<IFirewallRule> getAllRules();

}
