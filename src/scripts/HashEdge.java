package scripts;

public class HashEdge {
	
	private HashNode one = null;
	private HashNode two = null;
	
	private long counter;
	
	public HashEdge (HashNode one, HashNode two) {
		this.one = one;
		this.two = two;
		counter = 1;
	}
	
	public boolean isEqual(HashEdge edge) {
		// equals one
		if (one.getName().equals(edge.one.getName()) ||
				two.getName().equals(edge.one.getName())) {
			// equals two
			if (one.getName().equals(edge.two.getName()) ||
					two.getName().equals(edge.two.getName())) {
				// Edge with both similar HashNodes
				return true;
			}
			counter = 1;
		}
		return false;
	}
	
	
	public void INC() {
		counter++;
	}
	
	
	public long getCounter() {
		return counter;
	}
	
	
	public HashNode getSource() {
		return one;
	}
	
	public HashNode getTarget() {
		return two;
	}
}