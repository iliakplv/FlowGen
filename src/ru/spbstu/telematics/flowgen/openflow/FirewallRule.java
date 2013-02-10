package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;


public interface FirewallRule {

	public JSONObject ovsRuleAddCommand();

	public JSONObject ovsRuleRemoveCommand();

}
