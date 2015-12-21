package oss.fruct.org.smarttrip.transportkp;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.smartspace.JnaSmartSpace;
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

	public static int getRandomNumber() {
		return 4; // chosen by fair dice roll.
				  // guaranteed to be random.
	}

	public static void canGoWrong() {
		throw new RuntimeException();
	}

	public static void main(String[] args) {
		Args parsedArgs = parseOptions(args);
		if (parsedArgs == null) {
			return;
		}

		setupLog();

		GraphHopper graphHopper = createGraphhopper(parsedArgs.getMapFile(), parsedArgs.getGraphhopperDir());
		SmartSpace smartSpace = createSmartspace(parsedArgs);

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
		options.addOption(Option.builder("a")
				.required()
				.longOpt("sib-address")
				.desc("SIB address")
				.hasArg()
				.build());
		options.addOption(Option.builder("p")
				.required()
				.longOpt("sib-port")
				.desc("SIB port")
				.hasArg()
				.build());
		options.addOption(Option.builder("n")
				.required()
				.longOpt("sib-name")
				.desc("SIB name")
				.hasArg()
				.build());


		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			String mapFile = commandLine.getOptionValue("f");
			String graphhopperDir = commandLine.getOptionValue("t");
			String sibAddress = commandLine.getOptionValue("a");
			String sibName = commandLine.getOptionValue("n");
			int sibPort = Integer.parseInt(commandLine.getOptionValue("p"));

			return new Args(mapFile, graphhopperDir, sibAddress, sibName, sibPort);
		} catch (ParseException | NumberFormatException e) {

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

	private static SmartSpace createSmartspace(Args args) {
		return new JnaSmartSpace("TransportKP", args.getSibName(), args.getSibAddress(), args.getSibPort());
	}

	private static GraphHopper createGraphhopper(String mapFile, String graphhopperDir) {
		GraphHopper graphHopper = new GraphHopper().forServer();
		graphHopper.setOSMFile(mapFile);
		graphHopper.setGraphHopperLocation(graphhopperDir);
		graphHopper.setEncodingManager(new EncodingManager("foot"));
		graphHopper.setCHEnable(false);
		graphHopper.importOrLoad();
		return graphHopper;
	}
}
