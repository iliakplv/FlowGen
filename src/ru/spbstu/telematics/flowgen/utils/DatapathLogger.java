package ru.spbstu.telematics.flowgen.utils;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatapathLogger implements IDatapathListener {

	private final static String SPACE = " ";
	private final static String LABEL_CONNECT =		"[+]";
	private final static String LABEL_DISCONNECT =	"[-]";
	private final static String LOG_DATE_FORMAT =	"dd.MM.yyyy HH:mm:ss";

	private DateFormat dateFormat;

	private String datapathLabel;


	public DatapathLogger(String dpid, String datapathName) {

		dateFormat = new SimpleDateFormat(LOG_DATE_FORMAT);

		StringBuilder sb = new StringBuilder();
		sb.append(dpid);
		sb.append(" (");
		sb.append(datapathName);
		sb.append(")");
		datapathLabel = sb.toString();
	}

	@Override
	public void onConnection(JSONObject[] commands) {
		Date date = new Date();
		for (JSONObject command : commands) {
			printLog(date, LABEL_CONNECT, command.toString());
		}
	}

	@Override
	public void onDisconnection(JSONObject[] commands) {
		Date date = new Date();
		for (JSONObject command : commands) {
			printLog(date, LABEL_DISCONNECT, command.toString());
		}
	}

	private void printLog(Date date, String tag, String message) {

		StringBuilder sb = new StringBuilder();

		sb.append(dateFormat.format(date));
		sb.append(SPACE);

		sb.append(tag);
		sb.append(SPACE);

		sb.append(datapathLabel);
		sb.append(SPACE);

		sb.append(message);

		System.out.println(sb.toString());
	}

}
