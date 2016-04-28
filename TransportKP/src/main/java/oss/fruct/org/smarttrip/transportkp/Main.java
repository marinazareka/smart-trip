package oss.fruct.org.smarttrip.transportkp;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.smartspace.JnaSmartSpace;
import oss.fruct.org.smarttrip.transportkp.smartspace.SmartSpace;
import org.ini4j.Ini;

import java.util.*;

public class Main {
    
        // название конфиг файла
        private static final String configFile = String.valueOf("config.ini");
        private static final String[] configPath = {"./", "/etc/smart-trip/"}; 
    
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
        
        private static TransportKP transportKP;
        
	public static void main(String[] args) {
                // регистрируем прерывание по SIGTERM (Ctrl+C)
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override public void run() {
                        transportKP.stop();
                    }
                });
            
            
		Args config = parseConfig();

		Args parsedArgs = parseOptions(args, config);
		if (parsedArgs == null) {
			return;
		}

		setupLog();

		GraphHopper graphHopper = createGraphhopper(parsedArgs.getMapFile(), parsedArgs.getGraphhopperDir());
		SmartSpace smartSpace = createSmartspace(parsedArgs);

		transportKP = new TransportKP(smartSpace, graphHopper);
		transportKP.start();
	}

    private static Args parseConfig() {
        Args ret = new Args();
        
        Ini ini = new Ini();

        int i = 0;
        
        while (i < configPath.length) {
            try {
                ini.load(new FileReader(configPath[i] + configFile));
            } catch (IOException ex) {
                i++;
                continue;
            }
            
            Ini.Section sibConfig = ini.get("SIB");
            
            if (sibConfig != null) {
                ret.setSibName(sibConfig.getOrDefault("Name", ret.getSibName()));
                ret.setSibAddress(sibConfig.getOrDefault("Address", ret.getSibAddress()));
                ret.setSibPort(sibConfig.getOrDefault("Port", String.valueOf(ret.getSibPort())));
            }
            
            Ini.Section transportConfig = ini.get("Transport");
            
            if (transportConfig != null) {
                ret.setMapFile(transportConfig.getOrDefault("MapFile", ret.getMapFile()));
                ret.setGraphhopperDir(transportConfig.getOrDefault("GraphhopperDir",ret.getGraphhopperDir()));
            }
            break;
        }
        
        return ret;
    }

	private static Args parseOptions(String[] args, Args config) {
		Options options = new Options();
		options.addOption(Option.builder("f")
				.required(false)
				.desc("OSM file")
				.hasArg()
				.build());
		options.addOption(Option.builder("t")
                .required(false)
                .desc("Graphhopper directory")
                .hasArg()
                .build());
		options.addOption(Option.builder("a")
				.required(false)
				.longOpt("sib-address")
				.desc("SIB address")
				.hasArg()
				.build());
		options.addOption(Option.builder("p")
				.required(false)
				.longOpt("sib-port")
				.desc("SIB port")
				.hasArg()
				.build());
		options.addOption(Option.builder("n")
				.required(false)
				.longOpt("sib-name")
				.desc("SIB name")
				.hasArg()
				.build());


		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			String mapFile = commandLine.getOptionValue("f", config.getMapFile());
			String graphhopperDir = commandLine.getOptionValue("t", config.getGraphhopperDir());
			String sibAddress = commandLine.getOptionValue("a", config.getSibAddress());
			String sibName = commandLine.getOptionValue("n", config.getSibName());
			int sibPort = Integer.parseInt(commandLine.getOptionValue("p", String.valueOf(config.getSibPort())));

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
		graphHopper.setEncodingManager(new EncodingManager("car, foot"));
		graphHopper.setCHEnable(false);
		graphHopper.importOrLoad();
		return graphHopper;
	}
}
