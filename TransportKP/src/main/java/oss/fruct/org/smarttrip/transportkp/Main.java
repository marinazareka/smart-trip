package oss.fruct.org.smarttrip.transportkp;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.smartspace.JnaSmartSpace;
import oss.fruct.org.smarttrip.transportkp.smartspace.MockSmartSpace;
import oss.fruct.org.smarttrip.transportkp.smartspace.SmartSpace;

import java.util.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static Random random = new Random();

	public static void log(Object printable) {
		//System.err.print("" + printable.toString());
	}

	public static void logln(Object printable) {
		//System.err.println("" + printable.toString());
	}

	public static void main(String[] args) {
		setupLog();

		GraphHopper graphHopper = createGraphhopper();
		SmartSpace smartSpace = createSmartspace();

		TransportKP transportKP = new TransportKP(smartSpace, graphHopper);
		transportKP.start();
	}

	private static void setupLog() {
		log.trace("Test trace");
		log.debug("Test debug");
		log.info("Test info");
		log.warn("Test warn");
		log.error("Test error");
	}

	private static SmartSpace createSmartspace() {
		return new JnaSmartSpace();
	}

	private static GraphHopper createGraphhopper() {
		GraphHopper graphHopper = new GraphHopper().forServer();
		graphHopper.setOSMFile("/tmp/osm/osm.osm.pbf");
		graphHopper.setGraphHopperLocation("/tmp/osm/graphhopper");
		graphHopper.setEncodingManager(new EncodingManager("foot"));
		graphHopper.setCHWeighting("shortest");
		graphHopper.importOrLoad();
		return graphHopper;
	}
}
