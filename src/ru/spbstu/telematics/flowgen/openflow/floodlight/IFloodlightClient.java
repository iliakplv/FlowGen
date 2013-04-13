package ru.spbstu.telematics.flowgen.openflow.floodlight;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.application.configuration.FloodlightConfig;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.ControllerData;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;


public interface IFloodlightClient {

	public void addFlow(JSONObject command);
	public void addFlows(JSONObject[] commands);

	public void removeFlow(JSONObject command);
	public void removeFlows(JSONObject[] commands);

	public ControllerData getControllerData();
	public Hosts getKnownHosts();


	public FloodlightConfig getConfig();

}
