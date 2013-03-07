package ru.spbstu.telematics.flowgen.utils;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.IDatapathListener;

public class DatapathLogger implements IDatapathListener {

	private final static String CONNECT = "+ ";
	private final static String DISCONNECT = "- ";

	@Override
	public void onConnection(JSONObject[] commands) {
		for (JSONObject command : commands) {
			System.out.println(CONNECT + command.toString());
		}
	}

	@Override
	public void onDisconnection(JSONObject[] commands) {
		for (JSONObject command : commands) {
			System.out.println(DISCONNECT + command.toString());
		}
	}

}
