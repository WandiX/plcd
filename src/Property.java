import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Property extends XmiElement {
	
	/*
	 * Store the property for block
	 */
	
	private String typeId;
	private UmlClass parent;
	private Connector connector = null;
	static final String xmiType = "uml:Property";
	private boolean isInBlock;
	
	Property(Node nd, UmlClass p, boolean isBlockProp) {
		super(nd);
		Element e = (Element)nd;
		
		parent = p;
		typeId = e.getAttribute("type");
		isInBlock = isBlockProp;
		//datatype = d;
	}
	
	final static String getDomainName(String str) {
		int index1 = str.indexOf('.');
		int index2 = str.indexOf(':');
		
		if (index1 > 0) {
			String subString = str.substring(0, index1);
			return subString;
		}
		else if (index2 > 0) {
			String subString = str.substring(0, index2);
			return subString;
		}

		return str;
	}
	
	public boolean isInBlockDefDiag() {
		return isInBlock;
	}
	
	public void addConn(Connector conn) {
		
		connector = conn;
	}
	
	public boolean hasConn() {
		return connector == null;
	}
	
	public Connector getConn() {

		return connector;
	}
	
	
	public Element getParentElement() {
		return parent.getElement();
	}
	
	public UmlClass getParentClass() {
		return parent;
	}
	
	
	public String getTypeId() {
		return typeId;
	}
	
}
