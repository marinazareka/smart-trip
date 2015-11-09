package oss.fruct.org.smarttrip.transportkp;

public class Args {
	private String mapFile;
	private String graphhopperDir;

	public Args(String mapFile, String graphhopperDir) {
		this.mapFile = mapFile;
		this.graphhopperDir = graphhopperDir;
	}

	public String getMapFile() {
		return mapFile;
	}

	public String getGraphhopperDir() {
		return graphhopperDir;
	}
}
