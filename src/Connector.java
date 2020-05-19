import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class Connector {
	
	/*
	 * Store the information of a connector in SysML Parametric Diagram
	 */

	private String id;
	private List<ConnectorEnd> ends;
	private Node node;
	private Node bindConnNode;	//Store the Constraints:ConstraintProperty node in the uml file
	private Node edgeElement;	//Store the edges notation with element href="model.uml#id"
	
	Connector(Node nd) {
		node = nd;
		
		Element e = (Element)nd;
		id = e.getAttribute("xmi:id");
	}
	
	Connector(ConnectorEnd e1, ConnectorEnd e2, Node nd, Node bind) {
		
		ends = new ArrayList<>(2);
		ends.add(e1);
		ends.add(e2);
		node = nd;
		
		Element e = (Element)node; 
		id = e.getAttribute("xmi:id");
		
		bindConnNode = bind;
	}
	
	Connector(List<ConnectorEnd> endList, Node nd, Node bind) {
		if (endList.size() != 2) {
			throw new IllegalArgumentException("The size of ends list should be 2");
		}
		
		node = nd;
		ends = endList;
		bindConnNode = bind;
	}
	
	public void setConnectorEnds(List<ConnectorEnd> endList) {
		ends = endList;
	}
	
	public Node getBindNode() {
		return bindConnNode;
	}
	
	public String getId() {
		return id;
	}
	
	
	public Node getEdgeElement() {
		return edgeElement;
	}

	public void setBindNode(Node bind) {
		bindConnNode = bind;
	}
	
	public void setEdgeElement(Node edge) {
		edgeElement = edge;
	}
	

	public boolean equals(Connector conn) {

		return (ends.get(0).equals(conn.ends.get(0)) && ends.get(1).equals(conn.ends.get(1))) ||
				(ends.get(0).equals(conn.ends.get(1)) && ends.get(1).equals(conn.ends.get(0)));
	}
	
	public boolean containsEnd(ConnectorEnd end) {
		return ends.contains(end); 
	}
	
	public List<ConnectorEnd> getEnds() {
		return ends;
	}
	
	public ConnectorEnd getEnd(int index) {
		if (index > 1) return null;
		return ends.get(index);
	}
	
	public Node getNode() {
		return node;
	}
	
	
	public boolean hasEnds(String var1, String var2) {
		return (var1.hashCode() + var2.hashCode()) == this.hashCode();
	}
	
	@Override
	public boolean equals(Object other){

	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Connector))return false;
	    
	    Connector e = (Connector) other;
	    return e.hashCode() == this.hashCode();
	}
	
	@Override
	public int hashCode() {
		
		return ends.get(0).getVariableName().hashCode() + ends.get(1).getVariableName().hashCode();
	}
}
