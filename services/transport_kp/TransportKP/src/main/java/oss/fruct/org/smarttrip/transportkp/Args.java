package oss.fruct.org.smarttrip.transportkp;

public class Args {
	private String mapFile;
	private String graphhopperDir;
	private String sibAddress;
	private String sibName;
	private int sibPort;

	public Args(String mapFile, String graphhopperDir, String sibAddress, String sibName, int sibPort) {
		this.mapFile = mapFile;
		this.graphhopperDir = graphhopperDir;
		this.sibAddress = sibAddress;
		this.sibName = sibName;
		this.sibPort = sibPort;
	}
        
        public Args() {
            this.mapFile = null;
            this.graphhopperDir = System.getProperty("java.io.tmpdir") + "/graphhopper";
            this.sibAddress = "127.0.0.1";
            this.sibName = "X";
            this.sibPort = 10010;
        }

	public String getMapFile() {
		return mapFile;
	}

	public void setMapFile(String newMapFile) {
		this.mapFile = newMapFile;
	}

	public String getGraphhopperDir() {
		return graphhopperDir;
	}

	public void setGraphhopperDir(String newDir) {
		this.graphhopperDir = newDir;
	}

	public String getSibAddress() {
		return sibAddress;
	}

	public void setSibAddress(String newAddress) {
		this.sibAddress = newAddress;
	}

	public String getSibName() {
		return sibName;
	}

	public void setSibName(String newName) {
		this.sibName = newName;
	}

	public int getSibPort() {
		return sibPort;
	}

	public void setSibPort(int newPort) {
		this.sibPort = newPort;
	}

	public void setSibPort(String newPort) {
		this.sibPort = Integer.parseInt(newPort);
	}
}
