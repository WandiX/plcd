
import java.io.File;
import java.security.SecureRandom;
import java.util.*;
 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
//import javax.xml.xpath.XPath;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
 
 
public class XMIParser {
	
	/*
	 * Parse the info in the XMI files for SysML diagrams (Block Definition Diagram and Parametric Diagram)
	 */
	
	private List<String[]> results = null;	//Save the output of single version
	private Set<Connector> resConnectors;	//Store the connectors that have mismatch units
	
	
	private Document docNotation;
	private Document docUml;
	private Node rtNodeUml;		//The root xmi node in uml file
	private Node rtModelUml;	//The root Node with tag uml:Model in uml file
	private Node rtNotationParam;
	private Node rtNotationBlock;
	
	
	private SysMLModel model;
	int version;
	
	final static int BLOCK_DEFINATION_DAIG = 0;
	final static int PARAM_DIAG = 1;
	final static String[] tagsInBlock = new String[] {"Class_Shape", "StereotypeComment", "StereotypeCommentLink",
			"Association_Edge"};
	final static String[] tagsInParam = new String[] {"Class_Shape", "StereotypeComment", "StereotypeCommentLink", 
			"Connector_Edge"};
	
	final static String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";	// Characters to generate element id
    
    XMIParser(String uml, String notation, int ver){
    	
    	version = ver;

        SysMLDiag paramDiagram = new SysMLDiag(PARAM_DIAG, tagsInParam);
        SysMLDiag blockDiagram = new SysMLDiag(BLOCK_DEFINATION_DAIG, tagsInBlock);
        
        model = new SysMLModel(paramDiagram, blockDiagram);      
        
        parseUML(uml);
        parseNotations(notation);
       
        resConnectors = new HashSet<>();
        results = new ArrayList<>();
        
        setDomainInDiags();
        analyzeConnectorInfo();      
        fillInCompositionInfo();

    }
    
    private void fillInCompositionInfo() {
		//Add the mapping from composition links to their corresponding components
    	SysMLDiag blockDiag = model.getBlockDefDiag();
		UmlClass blockDomain = blockDiag.getDomainClass();
		
		if (blockDomain == null) {
			throw new NullPointerException("Missing domain class in block definition diagram");
		}
		
		Set<Property> linkSet = blockDomain.getAllProperty();
		Iterator<Property> linkIter = linkSet.iterator();
		
		while(linkIter.hasNext()) {
			Property link = linkIter.next();
			
			String typeId = link.getElement().getAttribute("type");
			
			UmlClass linkedComp = model.getUmlClass(typeId);
						
			model.addComposition(link.getName(), linkedComp);

		}
	}
    
    public Node getUmlRootNode() {
    	return rtNodeUml;
    }

    public Node getUmlRootModelNode() {
    	return rtModelUml;
    }
    
    public Node getNotationRootParam() {
    	return rtNotationParam;
    }
    
    public Node getNotationRootBlock() {
    	return rtNotationBlock;
    }
    
    public List<String[]> getResults() {
    	
    	return results;
    }
    
    
    public SysMLModel getSysMLModel() {
    	return model;
    }
    
    public Document getNotationDoc() {
    	return docNotation;
    }
    
    public Document getUmlDoc() {
    	return docUml;
    }
    
    public Set<Connector> getConnectors() {
    	return resConnectors;
    }
    
//    private void removeWhiteSpace(Document doc) {
//    	XPath xp = XPathFactory.newInstance().newXPath();
//    	NodeList nl;
//		try {
//			nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
//			for (int i=0; i < nl.getLength(); ++i) {
//	    	    Node node = nl.item(i);
//	    	    node.getParentNode().removeChild(node);
//	    	}
//		} catch (XPathExpressionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
    public void writeXmiFile() {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		
		try {
			//removeWhiteSpace(docUml);
			//removeWhiteSpace(docNotation);
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //Add a new line after an element
			
			
			String filename = "res/model" + Integer.toString(version);
			//docUml.normalize();
			
			DOMSource sourceUml = new DOMSource(docUml);
			StreamResult resultUml = new StreamResult(new File(filename + ".uml"));
			transformer.transform(sourceUml, resultUml);
			
			
			DOMSource sourceNotation = new DOMSource(docNotation);
			StreamResult resultNotation = new StreamResult(new File(filename + ".notation"));
			transformer.transform(sourceNotation, resultNotation);
			
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    
    public String getRandomId() {
    	
    	int len = 23;
    	String newId = null;
    	SecureRandom rnd = new SecureRandom();
    	do {
    		StringBuilder sb = new StringBuilder(len);
        	sb.append('_');
        	for(int i = 1; i < len; i++) 
        		sb.append(chars.charAt(rnd.nextInt(chars.length())) );
        	
        	newId = sb.toString();
    	} while(model.containsId(newId));
    	return newId.toString();
    }
    
    public Node appendNodeToParam(Node node, String newId, boolean maintainChildren) {
    	Node importedNode = getImportedNode(node, newId, maintainChildren, docNotation);
    	rtNotationParam.appendChild(importedNode);
    	return importedNode;
    }
    
    public Node appendNodeToBlock(Node node, String newId, boolean maintainChildren) {    	
		Node importedNode = getImportedNode(node, newId, maintainChildren, docNotation);
    	rtNotationBlock.appendChild(importedNode);
    	return importedNode;
    }
    
    public Node appendNodeToUmlRoot(Node node, String newId, boolean maintainChildren) {    	
    	Node importedNode = getImportedNode(node, newId, maintainChildren, docUml);
    	rtNodeUml.appendChild(importedNode);
    	return importedNode;
    }
    
    public Node appendNodeToUmlModel(Node node, String newId, boolean maintainChildren) {
    	Node importedNode = getImportedNode(node, newId, maintainChildren, docUml);
    	
    	rtModelUml.appendChild(importedNode);
    	return importedNode;
    }
    
    public Node appendNodeToUmlClass(Node node, String newId, Node parent, boolean maintainChildren) {
    
    	Node importedNode = getImportedNode(node, newId, maintainChildren, docUml);
    	parent.appendChild(importedNode);
    	
    	return importedNode;
    }
    
    public Node appendNodeToNotationClass(Node node, String newId, Node parent, boolean maintainChildren) {
    	
    	Node importedNode = getImportedNode(node, newId, maintainChildren, docNotation);    	
    	parent.appendChild(importedNode);
    	
    	return importedNode;
    }
    
    public Node appendConnNodeToUmlClass(Node node, String newId, Node parent, Node newEndPropNode1, Node newEndPropNode2) {
    	//If node is uml connector, check the id of ends
    	Node importedNode = getImportedNode(node, newId, true, docUml);
    	parent.appendChild(importedNode);
    	Element importedEle = (Element)importedNode;
    	
    	
    	//Get the ids of ends in P2. 
    	Element newEndPropEle1 = (Element)newEndPropNode1;
    	Element newEndPropEle2 = (Element)newEndPropNode2;
    	String newVarId1 = newEndPropEle1.getAttribute("xmi:id");
    	String newVarId2 = newEndPropEle2.getAttribute("xmi:id");
    	
    	if (importedEle.getAttribute("xmi:type").equals("uml:Connector")) {
    		
    		
    		
			//Get the information from the old node
    		NodeList ends = importedEle.getElementsByTagName("end");
			Node endNode1 = ends.item(0);
			Node endNode2 = ends.item(1);
			Element endEle1 = (Element)endNode1;
			Element endEle2 = (Element)endNode2;
			
			
			String varId1 = endEle1.getAttribute("role");
			String varId2 = endEle2.getAttribute("role");
			String endId1 = endEle1.getAttribute("xmi:id");
			String endId2 = endEle2.getAttribute("xmi:id");
			
			if (model.containsId(endId1)) {
				endId1 = getRandomId();
			}
			
			if (model.containsId(endId2)) {
				endId2 = getRandomId();
			}
			
			//If end1 id in P1 matches end1 id in P2, end2 id in P1 matches end2 id in P2
			//Return imported node directly.
			if (newVarId1.equals(varId1) && newVarId2.equals(varId2)) {
				
				return importedNode;
			}
			
			if (newVarId1.equals(varId2) && newVarId2.equals(varId1)) {
				
				return importedNode;
			}
			
			//If only one of the ends match
			
			if (newVarId1.equals(varId1)) {
				endEle2.setAttribute("role", newVarId2);
				return importedNode;
			}
			
			if (newVarId1.equals(varId2)) {
				endEle2.setAttribute("role", newVarId1);
				return importedNode;
			}
			
			if (newVarId2.equals(varId1)) {
				endEle1.setAttribute("role", newVarId2);
				return importedNode;
			}
			
			if (newVarId2.equals(varId2)) {
				endEle1.setAttribute("role", newVarId1);
				return importedNode;
			}
			
			endEle1.setAttribute("role", newVarId1);
			endEle2.setAttribute("role", newVarId2);
			
			
		}
    	return importedNode;
    }
    
    private Node getImportedNode(Node node, String newId, boolean maintainChildren, Document doc) {
    	if (newId == null || model.containsId(newId)) {
    		newId = getRandomId();
    	}
    	
    	if (doc == null) {
    		System.out.println("doc is null");
    	}
    	
    	if (node == null) {
    		System.out.println("node is null");
    	}
		
    	Node importedNode = doc.importNode(node, true);
    	Element e = (Element)importedNode;
		e.setAttribute("xmi:id", newId);
		
		//maintainChildren indicates that if the imported node maintain previous children
		if (!maintainChildren) {	
			removeChilds(importedNode);
		}
		
		return importedNode;
    }
    
    private void removeChilds(Node node) {
        while (node.hasChildNodes())
            node.removeChild(node.getFirstChild());
    }
    
    public void modifyPropertyAttr(String className, String propName, String attrName, String newValue) {
    	SysMLDiag blockDiagram = model.getBlockDefDiag();
    	//SysMLDiag paramDiagram = model.getParamDiag();
    	
    	UmlClass classInBlock = blockDiagram.getClassByName(className);
    	//UmlClass classInParam = paramDiagram.getClassByName(className);
    	
		if (classInBlock == null) {
			throw new NullPointerException(className + " cannot be found in Block Definition Diagram of P" + Integer.toString(version));
		}
		
		Property propInBlock = blockDiagram.getPropByName(propName);
		Node propNdInBlock = propInBlock.getNode();
		Node attrInBlock = propNdInBlock.getAttributes().getNamedItem(attrName);
		attrInBlock.setNodeValue(newValue);
		
    }
    
    
    private void setDomainInDiags() {
    	//Find the domain Class (The only class in parametric diagram)
    	SysMLDiag paramDiag = model.getParamDiag();
    	SysMLDiag blockDiag = model.getBlockDefDiag();
    	Set<UmlClass> paramClassSet = paramDiag.getAllClass();
    	
    	if (paramClassSet.size() != 1) {
    		throw new NullPointerException("Find " + paramClassSet.size() + " possible domain class in parametric diagram");
    	}
    	
    	UmlClass paramDomainClass = paramClassSet.iterator().next();
    	String domainName = paramDomainClass.getName();
    	UmlClass blockDomainClass = blockDiag.getClassByName(domainName);
    	
    	paramDiag.setDomainClass(paramDomainClass);
    	blockDiag.setDomainClass(blockDomainClass);
    	
    	parseNotationDomain();
    }
    
    private void analyzeConnectorInfo() {
    	Collection<Connector> conns = model.getAllConnectors();
    	Iterator<Connector> itConn = conns.iterator();    	
    	
    	//Indicate if the domain elements in both diagrams have been set
    	while(itConn.hasNext()) {
    		
    		Connector conn = itConn.next();
    		Node node = conn.getNode();
			Element e = (Element)node;
			Element domainEle = (Element)e.getParentNode();
			String domainId = domainEle.getAttribute("xmi:id");
			
			//Get the both ends of connectors and compare units
			NodeList listConnectorEnds = e.getElementsByTagName("end");
			int nEnds = listConnectorEnds.getLength();
			String[] names = new String[3];	//Store the names of ends
			String[] variables = new String[2];	//Store the related variables
			String[] components = new String[2]; 	//Store the related components
			List<ConnectorEnd> ends = new ArrayList<>(2);
			List<Property> vars = new ArrayList<>(2);	//Store the corresponding properties of ends
			
			
			for (int j=0; j<nEnds; j++) {
				Node endNode = listConnectorEnds.item(j);
				Element endEle = (Element)endNode;
				String role = endEle.getAttribute("role");
				
				Property prop = model.getProperty(role);
				
				if (prop == null) {
					throw new NullPointerException("Property " + role + " can not be found");
				}
					
				
				UmlClass parentPropCls = prop.getParentClass();
				
				Element attr = prop.getElement();
				
				//If the end links to an attribute within the domain, extract the attribute
				//Otherwise, brute force all elements with their owned attributes
				if (parentPropCls.getId().equals(domainId)) {
					String name = attr.getAttribute("name");
					String type = attr.getAttribute("type");
					String var = name;
					
					variables[j] = var;
					DataType datatype = model.getDataType(type);
					
					names[j] = datatype.getName();
					
					//Search for the component name
					//Property property = model.getPropertyByName(var);
					UmlClass domainInBlock = model.getBlockDefDiag().getClassByName(domainEle.getAttribute("name"));
					
					if (domainInBlock == null) {
						throw new NullPointerException("No corresponding class " + domainEle.getAttribute("name") + " in Block Definition Diagram");
					}
					
					//Get the link name of var (The part before .)
					int index = var.indexOf('.');
					
					if (index > -1) {
						var = var.substring(0, index);
					}
					
					UmlClass domainInParam = model.getBlockDefDiag().getClassByName(domainEle.getAttribute("name"));
					
					Property property = domainInParam.getParamPropByName(var);
					
					
					if (property == null) {
						throw new NullPointerException("Property name " + var + " doesn't exist");
					}
					
					String propType = property.getTypeId();
					
					UmlClass compClass = model.getUmlClass(propType);
					
					if (compClass == null) {
						throw new NullPointerException("Component id " + compClass + " doesn't exist");
					}
					
					components[j] = compClass.getName();
					
					ConnectorEnd connEnd = new ConnectorEnd(datatype, prop, compClass, true, endNode);
					Node nestedConnEnd = model.getNestedConnEnd(connEnd.getId());
					connEnd.setNestedConnEnd(nestedConnEnd);
					ends.add(connEnd);
					vars.add(prop);
					
				}
				else {
					
					DataType datatype = model.getDataType(prop.getTypeId());
					if (datatype == null) {
						throw new NullPointerException("Property id " + prop.getId() + " doesn't have a datatype");
					}

					String name = datatype.getName();
					names[j] = name;
					variables[j] = prop.getName();
					components[j] = parentPropCls.getName();
					
					ConnectorEnd connEnd = new ConnectorEnd(datatype, prop, parentPropCls, false, endNode);
					Node nestedConnEnd = model.getNestedConnEnd(connEnd.getId());
					connEnd.setNestedConnEnd(nestedConnEnd);
					ends.add(connEnd);
					vars.add(prop);
				}
				
			}
			
			String connId = e.getAttribute("xmi:id");
			Node bindNode = model.getBindConn(connId);

			conn.setConnectorEnds(ends);
			conn.setBindNode(bindNode);
			
			vars.get(0).addConn(conn);
			vars.get(1).addConn(conn);
			
			
			if (!ends.get(0).getUnitName().equals(ends.get(1).getUnitName())) {
				resConnectors.add(conn);
				String[] comps = new String[] {ends.get(0).getComponentName(), ends.get(1).getComponentName()};
				results.add(comps);				
				
				String[] varbs = new String[] {ends.get(0).getVariableName(), ends.get(1).getVariableName()};
				results.add(varbs);
				
				String[] units = new String[] {ends.get(0).getUnitName(), ends.get(1).getUnitName()};
				results.add(units);
				
				
			}
    	}
    }
    

	private void parseNotations(String notation) {
		
		//Get the pairs in all diagrams
    	final String NOTATION = "notation:Diagram"; 	
    	
    	try {	 
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docNotation = docBuilder.parse(new File(notation));
 
            // normalize text representation
            docNotation.getDocumentElement().normalize();
 
            NodeList listDiagrams = docNotation.getElementsByTagName(NOTATION);
            int nDiagrams = listDiagrams.getLength();

            //Find the diagrams in model.notation
            for (int i=0; i<nDiagrams; i++) {
            	Node node = listDiagrams.item(i);
            	Element e = (Element)node;
            	
            	if (e.getAttribute("name").equals("SysML 1.4 Parametric Diagram")) {
            		rtNotationParam = node;
            		model.getParamDiag().setRootElement(e);
            	}
            	else if (e.getAttribute("name").equals("SysML 1.4 Block Definition Diagram")) {
            		rtNotationBlock = node;
            		model.getBlockDefDiag().setRootElement(e);
            	}
            }
            
            if (model.getParamDiag().getRootElement() == null || model.getBlockDefDiag().getRootElement() == null) {
            	throw new NullPointerException("Parametric Diagram or Block Definition Diagram doesn't exist");
            }
            
            //Element info in notation file
            getDiagClasses(model.getBlockDefDiag(), "children", "element");
            getDiagClasses(model.getBlockDefDiag(), "children", "eObjectValue");
            getDiagClasses(model.getBlockDefDiag(), "edges", "eObjectValue");
            
            //Association info in notation file
            getDiagClasses(model.getBlockDefDiag(), "edges", "element");
            
            //Element info in uml file
            getDiagClasses(model.getParamDiag(), "children", "element");
            
            //Connector info in notation file
            getDiagClasses(model.getParamDiag(), "edges", "element");
            getDiagClasses(model.getParamDiag(), "edges", "eObjectValue");
            getDiagClasses(model.getParamDiag(), "children", "eObjectValue");
            
            //parseNotationDomain();
            
 
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
 
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
 
        } catch (Throwable t) {
            t.printStackTrace();
        } 
    	
    	//return pairs;
    }
	
	private void parseNotationDomain() {
		//Since the properties in connections will not appear in .notation
		//They will not be added to SysMLDiag
		//This function aims to add the connection properties exist in the domain node in Block Definition Diagram
		SysMLDiag blockDiag = model.getBlockDefDiag();
		UmlClass domainClass = blockDiag.getDomainClass();
		
		if (domainClass == null) {
			throw new NullPointerException("Domain Class in Block Definition Diagram is empty");
		}
		
		Element domainEle = domainClass.getElement();
		
		NodeList listChildren = domainEle.getElementsByTagName("ownedAttribute");
		int nNodeList = listChildren.getLength();
		
		for (int i=0; i<nNodeList; i++) {
			Node linkNode = listChildren.item(i);
			
			Property linkProp = new Property(linkNode, domainClass, true);
			domainClass.addProperty(linkProp);
			blockDiag.addProperty(linkProp);
			
		}
	}
	
	private void getDiagClasses(SysMLDiag diag, String tagName, String childTagName) {
		
		//Put classes to their corresponding diagrams
		Element rtElement = diag.getRootElement();
		String[] tags = diag.getTags();
				
		//For each diagram, return the mapping between class and notation
		NodeList listChildren = rtElement.getElementsByTagName(tagName);
		int nChildren = listChildren.getLength();
		
        List<List<Node>> shapes = new ArrayList<>(tags.length);
        for (int i=0; i<tags.length; i++) {
        	shapes.add(new ArrayList<>());
        }

        for (int i=0; i<nChildren; i++) {
        	Node node = listChildren.item(i);
        	Element e = (Element)node;
        	
        	for (int j=0; j<tags.length; j++) {
        		if (e.getAttribute("type").equals(tags[j])) {
        			shapes.get(j).add(node);
        		}
        	}
        }
        
        for (int i=0; i<shapes.size(); i++) {
        	List<Node> shape = shapes.get(i);
        	
        	for (int j=0; j<shape.size(); j++) {
        		Node node = shape.get(j);
        		Element e = (Element)node;
        		NodeList listElements = e.getElementsByTagName(childTagName);
        		int nElements = listElements.getLength();
        		
        		for (int k=0; k<nElements; k++) {
        			Node childNode = listElements.item(k);
        			Element childEle = (Element)childNode;
        			
        			String xmiType = childEle.getAttribute("xmi:type");
        			
        			String id = getNotationHrefId(childEle);
        			
        			//If the element doesn't have attribute href, ignore the element
        			if (id == null) {
        				continue;
        			}
        			
        			
        			if (xmiType.equals("uml:Class")) {       				
        				
        				if (model.containsClass(id)) {
        					setClassNotation(id, diag, tagName, childTagName, node, i);
        				}
        			}
        			else if (xmiType.equals("uml:Association") & childTagName.equals("element") && tagName.equals("edges")) {

						model.addEdgeNotation(node, id);
					}
        			else if (xmiType.equals("uml:Connector")) {
        				
        				if (model.containsConnector(id)) {
        					
        					setConnNotation(id, tagName, childTagName, node, tags[i]);
        				}
        			}
        			else if (xmiType.equals("uml:Property")) {
        				Property p = model.getProperty(id);     				
        				
        				diag.addProperty(p);
        				
        			}
        			
        			
        		}
        	}
        }
        
	}
	
	private void setConnNotation(String id, String tagName, String childTagName, Node node, String type) {
		
		// Add the notation to its corresponding Connector instance
		
		Connector conn = model.getConnector(id);
		
		if (tagName.equals("edges")) {
			if (childTagName.equals("element")) {

				conn.setEdgeElement(node);
			}

		}

	}
	
	private void setClassNotation(String id, SysMLDiag diag, String tagName, String childTagName, Node node, int tagId) {

		UmlClass clazz = model.getUmlClass(id);
		diag.addClass(clazz, tagId);
		NotationShape notation = null;
		
		if (diag.containsShape(id)) {
			notation = diag.getShape(id);
		}
		else {
			notation = new NotationShape(id);
			diag.addShape(notation);
		} 
		
		if (tagName.equals("children")) {
			if (childTagName.equals("element")) {
				notation.setShapeClass(node);
			}
			else if (childTagName.equals("eObjectValue")) {
				notation.setShapeEObject(node);
			}
		}
		else if (tagName.equals("edges")) {
			
			if (childTagName.equals("eObjectValue")) {
				notation.setConnector(node);
			}	
		}
		
		clazz.SetNotationShape(notation);
		
		Node blockNode = model.getBlockInUml(id);
		
		if (blockNode == null) {
			blockNode = model.getConstBlockInUml(id);
		}
		
		if (blockNode == null) {
			throw new NullPointerException(clazz.getName() + " doesn't have Blocks:Block or "
					+ "Constraints:ConstraintBlock element in uml file");
		}
		
		
		clazz.SetBlockNode(blockNode);
	}
	
	
	private String getNotationHrefId(Element e) {
		try {
			if (e == null) {
				throw new NullPointerException("Element node is empty and cannot get href attribute");
			}
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}
		
		if (!e.hasAttribute("href")) {
			return null;
		}
		
		String href = e.getAttribute("href");
		
		int numId = href.indexOf('#');
		if (numId == -1) {
			return null;
		}
		
		String id = href.substring(numId + 1);
		
		return id;
	}
	
    
    private void parseUML(String uml) {
    	
    	//Parse UML file to get the mapping from class id to class element (classMap), 
    	//from property id to property element (propertyMap), from property id to its parent class(parentMap);
    	//from property name to property element (nameMap)
    	final String ELEMENT = "packagedElement";
        //final String FILE_ADDRESS = "model.uml";
        
    	
    	try {	 
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docUml = docBuilder.parse(new File(uml));
 
            // normalize text representation
            docUml.getDocumentElement().normalize();
            
            //Add Constraints:ConstraintBlock and Blocks:Block elements to model
            parseUmlBlocks();
            
            rtModelUml = docUml.getElementsByTagName("uml:Model").item(0);
 
            NodeList listElements = docUml.getElementsByTagName(ELEMENT);
            int nElements = listElements.getLength();
            
            for (int i=0; i<nElements; i++) {
            	Node node = listElements.item(i);
            	Element e = (Element)node;
            	if (e.getAttribute("xmi:type").equals("uml:PrimitiveType") || e.getAttribute("xmi:type").equals("uml:DataType")) {
            		
            		DataType type = new DataType(e);

            		model.addDataType(type);
            	}
            	else if (e.getAttribute("xmi:type").equals("uml:Class")) {
            		NodeList listProperties = e.getElementsByTagName("ownedAttribute");
            		int nProperties = listProperties.getLength();
            		
            		UmlClass umlClass = new UmlClass(e);
        			model.addClass(umlClass);
        			
            		for (int j=0; j<nProperties; j++) {
        				Element property = (Element)listProperties.item(j);
        				
        				//if (property.hasAttribute("aggregation")) {
        				//If the property has attribute "association", it indicates a link
        				//If the property has attribute "aggregation", it indicates a parameter 

        				String xmiType = property.getAttribute("xmi:type");
        				String propName = property.getAttribute("name");
        				
        				Property p = new Property(property, umlClass, xmiType.equals("uml:Property") && !propName.contains("."));
        				umlClass.addProperty(p);
        				model.addProperty(p);
        			
        			}
            		
            		//Store the information of connectors
            		NodeList listConnectors = e.getElementsByTagName("ownedConnector");
            		int nConnectors = listConnectors.getLength();
            		
            		
            		for (int j=0; j<nConnectors; j++) {
            			Node connNode = listConnectors.item(j);
            			Element connEle = (Element)connNode;
            			String connId = connEle.getAttribute("xmi:id");
            			
            			model.addConnector(connId, new Connector(connNode));
            			
            		}
            		
            	}
            	else if (e.getAttribute("xmi:type").equals("uml:Association")) {
            		
            		//Add associations to the model
        
            		model.addAssociation(node, e.getAttribute("xmi:id"));
            	}
            }

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
 
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
 
        } catch (Throwable t) {
            t.printStackTrace();
        }
    	
    }
    
    private void parseUmlBlocks() {
    	rtNodeUml = docUml.getFirstChild();
    	
    	
    	String[] tags = {"Blocks:Block", "ConstraintBlocks:ConstraintBlock", "Constraints:ConstraintProperty",
    			"Blocks:NestedConnectorEnd"};

    	
    	for (int i=0; i<tags.length; i++) {
    		String tag = tags[i];
    		NodeList listElements = docUml.getElementsByTagName(tag);
        	int nElements = listElements.getLength();
        	 
        	for (int j=0; j<nElements; j++) {
        		Node curr = listElements.item(j);
        		
        		if (i == 0) {
        			String baseClass = ((Element)curr).getAttribute("base_Class");
        			model.addBlockInUml(baseClass, curr);
        		}
        		else if (i == 1) {
        			String baseClass = ((Element)curr).getAttribute("base_Class");
        			model.addConstBlockInUml(baseClass, curr);
        		}
        		else if (i == 2) {
        			String baseProperty = ((Element)curr).getAttribute("base_Property");
        			model.addConstPropInUml(baseProperty, curr);
        		}

        		else if (i == 3) {
        			String baseConnEnd = ((Element)curr).getAttribute("base_ConnectorEnd");
        			model.addNestedConnEnd(baseConnEnd, curr);
        		}
        	}
    		
    	}
    }
}