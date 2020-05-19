import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SysMLModel {
	
	/*
	 * Store the info of SysML models
	 * Including Block Definition Diagram and Parametric Diagram
	 */
	
	private SysMLDiag paramDiag;
	private SysMLDiag blockDefDiag;
	private Map<String, DataType> dataTypeMap;
	private Map<String, DataType> dataTypeNameMap;
	private Map<String, UmlClass> classMap;	//Classes in all diagrams
	private Map<String, List<UmlClass>> classNameMap; //The key is the name of classes. One name can be used in both Block definition diagram and Parametric Diagram
	private Map<String, Property> propertyMap; //Properties in all diagrams
	private Map<String, Property> propertyNameMap; //The key is the name of properties
	
	//Store the elements of Blocks:Block and Constraints:ConstraintBlock in uml file
	//key is the value of base_Class attribute
	private Map<String, Node> constBlockUmlMap;
	private Map<String, Node> blockUmlMap;
	private Map<String, Node> constPropertyMap;
	
	private Map<String, Node> constConnMap;		//Store the information of Constraints:ConstraintProperty nodes in the uml file
	
	private Map<String, Node> associationMap;	//The list of elements with attribute "association" in uml file
	
	//The edges elments in the notation file (They has corresponding element in uml file)
	//The key of the map is the association id in uml file
	private Map<String, Node> edgeNotationMap;
	
	private Map<String, Connector> connectorMap;	//Store connectors in parametric diagram
	private Map<String, Node> nestedConnEndMap;		//Store Blocks:NestedConnectorEnd nodes in uml file
	
	
	private Map<String, UmlClass> compositionMap;	//Map from the composition link variable to the component
	
	
	SysMLModel(SysMLDiag param, SysMLDiag block) {
		paramDiag = param;
		blockDefDiag = block;
		dataTypeMap = new HashMap<>();
		dataTypeNameMap = new HashMap<>();
		classMap = new HashMap<>();
		classNameMap = new HashMap<>();
		propertyMap = new HashMap<>();
		propertyNameMap = new HashMap<>();
		//notationMap = new HashMap<>();
		
		constBlockUmlMap = new HashMap<>();
		blockUmlMap = new HashMap<>();
		constPropertyMap = new HashMap<>();
		
		constConnMap = new HashMap<>();
		
		associationMap = new HashMap<>();
		edgeNotationMap = new HashMap<>();
		
		connectorMap = new HashMap<>();
		nestedConnEndMap = new HashMap<>();
		//varDomainMap = new HashMap<>();
		
		compositionMap = new HashMap<>();
	}
	
	public void addComposition(String compName, UmlClass clz) {
		compositionMap.put(compName, clz);
	}
 	
	public UmlClass getComposition(String compName) {
		return compositionMap.get(compName);
	}
	
	public boolean containsComposition(String compName) {
		return compositionMap.containsKey(compName);
	}
	
	public Collection<DataType> getAllDataTypes() {
		return dataTypeMap.values();
	}
	
	public Set<String> getAllDataTypeNames() {
		return dataTypeNameMap.keySet();
	}
	
	public void addNestedConnEnd(String id, Node node) {
		nestedConnEndMap.put(id, node);
	}
	
	public Node getNestedConnEnd(String id) {
		return nestedConnEndMap.get(id);
	}
	
	
	public boolean containsConnector(String id) {
		return connectorMap.containsKey(id);
	}
	
	public void addConnector(String id, Connector conn) {
		connectorMap.put(id, conn);
	}
	
	public void removeConnector(String id) {
		connectorMap.remove(id);
	}
	
	public Connector getConnector(String id) {
		return connectorMap.get(id);
	}
	
	public Collection<Connector> getAllConnectors() {
		return connectorMap.values();
	}
	
	public boolean containsUmlBindConn(String id) {
		return constConnMap.containsKey(id);
	}
	
	public void addBindConnInUml(String id, Node node) {
		constConnMap.put(id, node);
	}
	
	public void removeBindConnInUml(String id) {
		constConnMap.remove(id);
	}
	
	public Node getBindConn(String id) {
		return constConnMap.get(id);
	}
	
	public boolean containsId(String id) {
		return dataTypeMap.containsKey(id) || classMap.containsKey(id) || propertyMap.containsKey(id);
	}
	
	public void addDataType(DataType type) {
		dataTypeMap.put(type.getId(), type);
		dataTypeNameMap.put(type.getName(), type);
	}
	
	public void addAssociation(Node node, String id) {
		associationMap.put(id, node);
	}
	
	public void addAssociation(Node node) {
		String id = ((Element)node).getAttribute("xmi:id");
		associationMap.put(id, node);
	}
	
	public Node getAssociation(String id) {
		return associationMap.get(id);
	}
	
	public boolean containsAssociation(String id) {
		return associationMap.containsKey(id);
	}
	
	public void removeAssociation(String id) {
		associationMap.remove(id);
	}
	
	public void addEdgeNotation(Node node, String id) {
		edgeNotationMap.put(id, node);
	}
	
	public Node getEdgeNotation(String id) {
		return edgeNotationMap.get(id);
	}
	
	public boolean containsEdgeNotation(String id) {
		return edgeNotationMap.containsKey(id);
	}
	
	public void removeEdgeNotation(String id) {
		edgeNotationMap.remove(id);
	}
	
	
	public void addConstBlockInUml(String id, Node node) {
		constBlockUmlMap.put(id, node);
	}
	
	public void addBlockInUml(String id, Node node) {
		blockUmlMap.put(id, node);
	}
	
	public Node getConstBlockInUml(String id) {
		return constBlockUmlMap.get(id);
	}
	
	public void removeConstBlockInUml(String id) {
		constBlockUmlMap.remove(id);
	}
	
	
	public Node getBlockInUml(String id) {
		return blockUmlMap.get(id);
	}
	
	public boolean containsUmlConstBlock(String id) {
		return constBlockUmlMap.containsKey(id);
	}
	
	public boolean containsUmlBlock(String id) {
		return blockUmlMap.containsKey(id);
	}
	
	public void removeUmlBlock(String id) {
		blockUmlMap.remove(id);
	}
	
	public void addConstPropInUml(String id, Node node) {
		//System.out.println(id);
		constPropertyMap.put(id, node);
	}
	
	public Node getConstPropInUml(String id) {
		return constPropertyMap.get(id);
	}
	
	public boolean containsUmlConstProp(String id) {
		return constPropertyMap.containsKey(id);
	}
	
	public void removeConstPropInUml(String id) {
		constPropertyMap.remove(id);
	}
	
	public SysMLDiag getParamDiag() {
		return paramDiag;
	}
	
	public SysMLDiag getBlockDefDiag() {
		return blockDefDiag;
	}
	
	public void addClass(UmlClass cls) {
		classMap.put(cls.getId(), cls);
		
		String name = cls.getName();
		if (classNameMap.containsKey(name)) {
			classNameMap.get(name).add(cls);
		}
		else {
			List<UmlClass> nameList = new ArrayList<>();
			nameList.add(cls);
			classNameMap.put(name, nameList);
		}
		
	}
	
	public void removeClass(String id) {
		try {
			UmlClass clazz = classMap.get(id);
			
			if (clazz == null) {
				throw new NullPointerException("UmlClass id " + id + " doesn't exists in model");
			}
			
			String name = clazz.getName();
			
			classNameMap.remove(name);
			classMap.remove(id);
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}	
	}
	
	
	public void addProperty(Property prop) {
		propertyMap.put(prop.getId(), prop);
		propertyNameMap.put(prop.getName(), prop);
	}
	
	public void setPropertyAttrByName(String className, String propName, String attrName, String value) {
		
		//If the property is in the domain class
		Property p = null;
		
		if (propName.contains(".")) {
			UmlClass paramDomain = paramDiag.getDomainClass();
			
			p = paramDomain.getParamPropByName(propName);
			
			if (p == null) {
				throw new NullPointerException("Property " + propName + " doesn't exist in Domain");
			}
		}
		else {
			//If className has corresponding class in parametric diagram, update it
			UmlClass classInParam = getUmlClassInParamByName(className);
			
			//If classInParam != null, it must be domain class
			if (classInParam != null) {

				p = paramDiag.getPropByName(propName);
				
				if (p == null) {
					throw new NullPointerException(propName + "doesn't exist in " + className);
				}
				
			}
			else {
				//If the property is in its own block
				UmlClass classInBlock = getUmlClassInBlockByName(className);
				
				if (classInBlock == null) {
					throw new NullPointerException(className + "doesn't exist in both diagrams");
				}
				
				p = classInBlock.getParamPropByName(propName);
				
				if (p == null) {
					throw new NullPointerException(propName + "doesn't exist in Parametric Diagram");
				}
				
				Property pBlock = classInBlock.getBlockPropByName(propName);
				
				if (pBlock == null) {
					throw new NullPointerException(propName + "doesn't exist in Block Definition diagram");
				}
				
				Element eleBlock = pBlock.getElement();
				eleBlock.setAttribute(attrName, value);
			}
		}

		
		
		Element ele = p.getElement();
		ele.setAttribute(attrName, value);
		
	}
	
	
	public boolean containsClass(String id) {
		return classMap.containsKey(id);
	}
	
	public boolean containsProperty(String id) {
		return propertyMap.containsKey(id);
	}
	
	public boolean containsPropName(String name) {
		return propertyNameMap.containsKey(name);
	}
	
	public UmlClass getUmlClass(String id) {
		return classMap.get(id);
	}
	
	public List<UmlClass> getUmlClassByName(String name) {
		return classNameMap.get(name);
	}
	
	public UmlClass getUmlClassInBlockByName(String name) {
		return blockDefDiag.getClassByName(name);
	}
	
	public UmlClass getUmlClassInParamByName(String name) {
		//System.out.println(name);
		
		if (name == null || name.equals("") || !classNameMap.containsKey(name)) {
			return null;
		}
		
		if (isConstBlock(name)) {
			//System.out.println("isConstBlock");
			List<UmlClass> classList = classNameMap.get(name);
			UmlClass blockClass = blockDefDiag.getClassByName(name);
			return (blockClass.equals(classList.get(0))? classList.get(1):classList.get(0));
		}
		else 
			return null;
	}
	
	public boolean isConstBlock(String name) {
		//Constraint block has two xmi nodes with the same name
		//One of them is in block definition diagram, another is in parametric diagram
		//System.out.println(name);
		return classNameMap.get(name).size() == 2;
	}
	
	public Property getProperty(String id) {
		return propertyMap.get(id);
	}
	
	public void removeProperty(String id) {
		propertyMap.remove(id);
	}
	

	public DataType getDataType(String id) {	
		return dataTypeMap.get(id);
	}
	
	public boolean containsDataType(String id) {
		return dataTypeMap.containsKey(id);
	}
	
	public boolean containsDataTypeName(String name) {
		return dataTypeNameMap.containsKey(name);
	}
	
	public DataType getDataTypeByName(String name) {		
		return dataTypeNameMap.get(name);
	}
	
	public Collection<Property> getAllProperties() {
		return propertyMap.values();
	}
	
	public Set<Property> getAllVariables() {
		//Rule out the connection properties
		
		Collection<Property> properties = getAllProperties();
		Set<Property> variables = new HashSet<Property>();
		Iterator<Property> propIter = properties.iterator();
		
		while (propIter.hasNext()) {
			Property prop = propIter.next();
			Element eleProp = prop.getElement();
			String typeId = eleProp.getAttribute("type");
			String name = eleProp.getAttribute("name");
			
			
			if ((!eleProp.hasAttribute("association") && !containsClass(typeId)) || (name.contains("."))) {
				variables.add(prop);
			}
			
		}
		
		return variables;
	}
	
}
