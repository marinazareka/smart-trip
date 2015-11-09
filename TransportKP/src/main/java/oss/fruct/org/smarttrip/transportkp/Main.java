package oss.fruct.org.smarttrip.transportkp;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import org.apache.commons.cli.*;
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
		Args parsedArgs = parseOptions(args);
		if (parsedArgs == null) {
			return;
		}

		setupLog();

		GraphHopper graphHopper = createGraphhopper(parsedArgs.getMapFile(), parsedArgs.getGraphhopperDir());
		SmartSpace smartSpace = createSmartspace();

		TransportKP transportKP = new TransportKP(smartSpace, graphHopper);
		transportKP.start();
	}

	private static Args parseOptions(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("f")
				.required()
				.desc("OSM file")
				.hasArg()
				.build());
		options.addOption(Option.builder("t")
				.required()
				.desc("Graphhopper directory")
				.hasArg()
				.build());

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			String mapFile = commandLine.getOptionValue("f");
			String graphhopperDir = commandLine.getOptionValue("t");

			return new Args(mapFile, graphhopperDir);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("TransportKP", options);
			return null;
		}
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

	private static GraphHopper createGraphhopper(String mapFile, String graphhopperDir) {
		GraphHopper graphHopper = new GraphHopper().forServer();
		graphHopper.setOSMFile(mapFile);
		graphHopper.setGraphHopperLocation(graphhopperDir);
		graphHopper.setEncodingManager(new EncodingManager("foot"));
		graphHopper.setCHWeighting("shortest");
		graphHopper.importOrLoad();
		return graphHopper;
	}
}
