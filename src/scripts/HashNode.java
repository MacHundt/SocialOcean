package scripts;

public class HashNode {

	private String name = "";
	private int counter = 0;
	private String id = "";

	public HashNode(String name) {
		this.name = name;
		counter = 1;
	}

	public String getName() {
		return name;
	}
	
	public void INC() {
		counter++;
	}
	
	public int getFreq() {
		return counter;
	}
	
	public boolean isEqual(HashNode one) {
		return (name.equals(one.getName()));
	}

	public void add(String nodeID) {
		this.id = nodeID;
	}
	
	public String getID() {
		return id;
	}
}
