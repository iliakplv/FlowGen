package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;


public interface IStaticFlowPusherClient {

	public void addFlow(JSONObject command);
	public void addFlows(JSONObject[] commands);

	public void removeFlow(JSONObject command);
	public void removeFlows(JSONObject[] commands);


	/***** Inner classes *****/

	public enum CommandType {
		FLOW_ADD,
		FLOW_REMOVE
	}
}
