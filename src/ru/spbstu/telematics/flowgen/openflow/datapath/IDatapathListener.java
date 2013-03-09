package ru.spbstu.telematics.flowgen.openflow.datapath;

import org.json.JSONObject;

public interface IDatapathListener {

	public void onConnection(JSONObject[] commands);

	public void onDisconnection(JSONObject[] commands);

}
