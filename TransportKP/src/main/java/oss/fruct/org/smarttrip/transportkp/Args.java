package oss.fruct.org.smarttrip.transportkp;

public class Args {
	private String mapFile;
	private String graphhopperDir;
	private final String sibAddress;
	private final String sibName;
	private final int sibPort;

	public Args(String mapFile, String graphhopperDir, String sibAddress, String sibName, int sibPort) {
		this.mapFile = mapFile;
		this.graphhopperDir = graphhopperDir;
		this.sibAddress = sibAddress;
		this.sibName = sibName;
		this.sibPort = sibPort;
	}

	public String getMapFile() {
		return mapFile;
	}

	public String getGraphhopperDir() {
		return graphhopperDir;
	}

	public String getSibAddress() {
		return sibAddress;
	}

	public String getSibName() {
		return sibName;
	}

	public int getSibPort() {
		return sibPort;
	}
}
