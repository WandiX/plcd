import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConnectorEnd {
	protected DataType unit;
	protected Property variable;
	protected UmlClass component;
	protected boolean isInDomain;	//If the connectorEnd has the same parent element with its role
	private Node nestedConnEnd;		//The Blocks:NestedConnectorEnd in uml file
	//private Node notationNode;
	private Node connEnd;
	private String id;
	
	ConnectorEnd(DataType un, Property var, UmlClass comp, Node end) {
		unit = un;
		variable = var;
		component = comp;
		isInDomain = false;
		connEnd = end;
		
		Element eEnd = (Element)end;
		id = eEnd.getAttribute("xmi:id");
		
	}
	
	ConnectorEnd(DataType un, Property var, UmlClass comp, boolean indicator, Node end) {
		unit = un;
		variable = var;
		component = comp;
		isInDomain = indicator;
		connEnd = end;
		
		Element eEnd = (Element)end;
		id = eEnd.getAttribute("xmi:id");
	}
	
	public String getId() {
		return id;
	}
	
	public Node getNode() {
		return connEnd;
	}
	
	public void setNestedConnEnd(Node node) {
		nestedConnEnd = node;
	}
	
//	public void setNotationNode(Node node) {
//		notationNode = node;
//	}
	
	public Node getNestedConnEnd() {
		return nestedConnEnd;
	}
	
//	public Node getNotationNode() {
//		return notationNode;
//	}
	
	public void setIsSameDomain(boolean indicator) {
		isInDomain = indicator;
	}
	
	public boolean getIsSameDomain() {
		return isInDomain;
	}
	
	public DataType getUnit() {
		return unit;
	}
	
	public String getUnitName() {
		return unit.getName();
	}
	
	public Property getVariable() {
		return variable;
	}
	
	public String getVariableName() {
		return variable.getName();
	}
	
	public UmlClass getComponent() {
		return component;
	}
	
	public String getComponentName() {
		return component.getName();
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof ConnectorEnd))return false;
	    
	    ConnectorEnd e = (ConnectorEnd) other;
	    
	    return e.hashCode() == this.hashCode();
	}
	
	@Override
	public int hashCode() {
		return unit.getId().hashCode() + variable.getId().hashCode() + component.getId().hashCode();
	}
}
