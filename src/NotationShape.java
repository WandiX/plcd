import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class NotationShape {
	
	/*
	 * Store the notationShape in .notation file
	 */
	
	private Node notationShapeClass;
	private Node notatinoShapeEObject;
	private Node notationConnector;
	private String id;
	
	NotationShape(String i) {
		id = i;
	}
	
	//A notation shape of constraintBlock and normal block contains three parts:
	//1. A notation:Shape with a same id class element 
	//2. A notation:Shape with a same id eObjective element
	//3. A notation:Connector(source id is notation:Shape 1 and target id is notation:Shape 2)
	NotationShape(Node clazz, Node eObj, Node conn, String i) {

		String classId = getHrefId(clazz, "element");
		String eObjId = getHrefId(eObj, "eObjectValue");
		String connId = getHrefId(conn, "eObjectValue");
		
		
		if (classId.equals(i) && eObjId.equals(i) && connId.equals(i)) {
			notationShapeClass = clazz;
			notatinoShapeEObject = eObj;
			notationConnector = conn;
			id = i;
		}
		else {
			
			throw new IllegalArgumentException("Nodes in NotationShape should have the same href id");
		}
		
	}
	
	NotationShape(Node clazz, Node eObj, Node conn) {
		String classId = getHrefId(clazz, "element");
		String eObjId = getHrefId(eObj, "eObjectValue");
		String connId = getHrefId(conn, "eObjectValue");
		
		if (classId.equals(eObjId) && classId.equals(connId)) {
			id = classId;
			notationShapeClass = clazz;
			notatinoShapeEObject = eObj;
			notationConnector = conn;
		}
		else {
			throw new NullPointerException("Nodes in NotationShape should have the same href id");			
		}
	}
	
	//A notation shape of constraintBlock and normal block contains three parts:
	//1. A notation:Connector with a same id class element 
	//2. A notation:Connector with a same id eObjective element
	NotationShape(Node clazz, Node eObj) {
		String classId = getHrefId(clazz, "element");
		String eObjId = getHrefId(eObj, "eObjectValue");
		
		if (classId.equals(eObjId)) {
			id = classId;
			notationShapeClass = clazz;
			notatinoShapeEObject = eObj;
			notationConnector = null;
		}
		else {
			throw new NullPointerException("Nodes in NotationShape should have the same href id");			
		}
	}
	
	NotationShape(Node clazz, Node eObj, String id) {
		String classId = getHrefId(clazz, "element");
		String eObjId = getHrefId(eObj, "eObjectValue");
		
		if (eObjId.equals(id) && classId.equals(id)) {
			notationShapeClass = clazz;
			notatinoShapeEObject = eObj;
			notationConnector = null;
		}
		else {
			throw new NullPointerException("Nodes in NotationShape should have the same href id");
		}
	}
	
	public NotationShape clone() {
		//Node newShapeClass = this.notationShapeClass.cloneNode(true);
		
		return new NotationShape(this.notationShapeClass, this.notatinoShapeEObject, this.notationConnector, 
				this.id);
	}
	
	public boolean hasNotationConn() {
		return notationConnector != null;
	}
	
	public void updateId(String newId) {
		id = newId;
		
		updateHref(newId, notationShapeClass, "element");
		updateHref(newId, notatinoShapeEObject, "eObjectValue");
		updateHref(newId, notationConnector, "eObjectValue");
		
	}
	
	private void updateHref(String newId, Node node, String childTagName) {
		if (node == null) return;
		
		Element childElement = getHrefElement(node, childTagName);
		childElement.setAttribute("href", "model.uml#" + newId);
	}
	
	private String getHrefId(Node node, String childTagName) {
		if (node == null) {
			throw new IllegalArgumentException("Notation node is empty");
		}
		
		Element childElement = getHrefElement(node, childTagName);
		
		if (childElement == null) {
			throw new IllegalArgumentException("No uml:Class type element exists in notation " + 
					((Element)node).getAttribute("xmi:id"));
		}
		
		String href = childElement.getAttribute("href");
		String id = href.substring(10);
		return id;
	}
	
	private Element getHrefElement(Node node, String childTagName) {
		Element e = (Element)node;
		//System.out.println(id);
		//System.out.println(e.getAttribute("xmi:id") + "   " + childTagName);
		
		NodeList children = e.getElementsByTagName(childTagName);
		//System.out.println(children.getLength());
		
		if (children == null || children.getLength() == 0) {
			throw new NullPointerException("Notation node doesn't have child " + childTagName);
		}
		
		//boolean hasUmlClass = false;
		int nChildren = children.getLength();
		for (int i=0; i<nChildren; i++) {
			Element childEle = (Element)children.item(i);
			if (childEle.getAttribute("xmi:type").equals("uml:Class")) {
				//hasUmlClass = true;
				return childEle;
			}
		}
		
		//Return null if there is no uml:Class type element
		return null;
	}
	
	
	public String getId() {
		return id;
	}
	
	public void setShapeClass(Node clazz) {
		notationShapeClass = clazz;
	}
	
	public void setShapeEObject(Node eObj) {
		notatinoShapeEObject = eObj;
	}
	
	public void setConnector(Node conn) {
		notationConnector = conn;
	}
	
	public Node getShapeClass() {
		return notationShapeClass;
	}
	
	public Node getShapeEObject() {
		return notatinoShapeEObject;
	}
	
	public Node getConnector() {
		return notationConnector;
	}
}
