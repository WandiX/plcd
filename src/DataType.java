import org.w3c.dom.Node;

public class DataType extends XmiElement {
	
	/*
	 * Store the data type for variables in SysML diagram
	 */
	
	protected static final String tagName = "packagedElement";

	DataType(Node nd) {
		super(nd);
	}
	
	DataType(Node nd, String n, String i) {
		super(nd, n, i);
	}
	
}
