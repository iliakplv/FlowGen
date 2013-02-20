package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;


public interface IFirewallRule {

	/**
	 * JSON commands to add flows to Open vSwitch
	 */

	public JSONObject ovsInFlowAddCommand();
	public JSONObject ovsOutFlowAddCommand();


	/**
	 * JSON commands to remove flows from Open vSwitch
	 */

	public JSONObject ovsInFlowRemoveCommand();
	public JSONObject ovsOutFlowRemoveCommand();

}
