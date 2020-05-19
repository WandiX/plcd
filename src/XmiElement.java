import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmiElement {
	
	/*
	 * The base class for all elements in XMI files
	 */
	
	protected Node node;
	protected String name;
	protected String id;
	
	XmiElement(Node nd, String n, String i) {
		node = nd;
		name = n;
		id = i;
	}
	
	XmiElement(Node nd) {
		node = nd;
		
		Element element = (Element)nd;
		name = element.getAttribute("name");
		id = element.getAttribute("xmi:id");
	}
	
	public Node getNode() {
		return node;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Element getElement() {
		return (Element)node;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof XmiElement))return false;
	    
	    XmiElement e = (XmiElement) other;
	    
	    return e.name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
