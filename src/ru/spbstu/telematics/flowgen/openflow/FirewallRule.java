package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;


public interface FirewallRule {

	public JSONObject ovsFlowAddCommand();

	public JSONObject ovsFlowRemoveCommand();

}
