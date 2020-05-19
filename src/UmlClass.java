import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

public class UmlClass extends XmiElement {
	
	/*
	 * Store the block in SysML diagram
	 */
	
	protected final static String xmi_type = "uml:Class";

	private Map<String, Property> propertyMap;
	private Map<String, List<Property>> propNameMap;
	private Node blockNodeInUml = null;
	private NotationShape shape = null;
	private Map<String, Property> varPropMap;
	
	UmlClass(Node nd, String i, String n) {
		super(nd, i, n);
		
		propertyMap = new HashMap<>();
		propNameMap = new HashMap<>();
		varPropMap = new HashMap<>();
		
	}
	
	UmlClass(Node nd) {
		super(nd);
		
		propertyMap = new HashMap<>();
		propNameMap = new HashMap<>();
		varPropMap = new HashMap<>();
	}
	

	
	public Property getBlockPropByName(String pName) {
		List<Property> propList = getPropertyByName(pName);
		
		if (propList == null) {
			return null;
		}
		if (propList.size() == 1) {
			return propList.get(0);
		}
		else if (propList.size() == 2) {
			return propList.get(0).isInBlockDefDiag() ? propList.get(0) : propList.get(1);
		}
		
		return null;
	}
	
	public Property getParamPropByName(String pName) {
		List<Property> propList = getPropertyByName(pName);
		
		if (propList == null) {
			return null;
		}
		else if (propList.size() == 1) {
			return propList.get(0).isInBlockDefDiag() ? null : propList.get(0);
		}
		if (propList.size() == 2) {
			return propList.get(0).isInBlockDefDiag() ? propList.get(1) : propList.get(0);
		}
		
		return null;
	}
	
	
	public void SetBlockNode(Node block) {
		blockNodeInUml = block;
	}
	
	public void SetNotationShape(NotationShape s) {
		shape = s;
	}
	
	public Node getBlockNode() {
		return blockNodeInUml;
	}
	
	public NotationShape getNotationShape() {
		return shape;
	}
	
	public void addProperty(Property p) {

		if (!p.getElement().hasAttribute("association") || p.getName().contains(".")) {
			varPropMap.put(p.getId(), p);
		}
		
		propertyMap.put(p.getId(), p);

		
		if (propNameMap.containsKey(p.getName())) {
			List<Property> list = propNameMap.get(p.getName());
			list.add(p);
		}
		else {
			List<Property> list = new LinkedList<>();
			list.add(p);
			propNameMap.put(p.getName(), list);
		}
		
		
	}
	
	public boolean containsVarProp(String id) {
		return varPropMap.containsKey(id);
	}
	
	public boolean containsVarProp(Property p) {
		return varPropMap.containsKey(p.getId());
	}
	
	public Property getVarProp(String id) {
		return varPropMap.get(id);
	}
	
	public Set<Property> getAllVariables() {
		return new HashSet<>(varPropMap.values());
	}
	
	public boolean containsProperty(Property p) {
		return propertyMap.containsKey(p.getId());
	}
	
	public boolean containsPropName(String name) {
		return propNameMap.containsKey(name);
	}
	
	public boolean containsProperty(String id) {
		return propertyMap.containsKey(id);
	}
	
	public Property getProperty(String id) {
		return propertyMap.get(id);
	}
	
	
	public List<Property> getPropertyByName(String name) {
		return propNameMap.get(name);
	}
	
	public Set<Property> getAllProperty() {
		return new HashSet<>(propertyMap.values());
	}
	
	public void removeProperty(String id) {
		try {
			Property p = propertyMap.get(id);
			
			if (p == null) {
				throw new NullPointerException("Property id " + id + " doesn't exist in " + this.name);
			}
			
			String name = p.getName();
			propertyMap.remove(id);
			
			List<Property> nameList = propNameMap.get(name);
			
			if (nameList.size() == 1) {
				propNameMap.remove(name);
			}
			else {
				for (Property nameProp: nameList) {
					if (nameProp.getId().equals(id)) {
						propNameMap.remove(name);
					}
				}
			}
			
			
			
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void removePropertyByName(String name) {
		try {

			propertyMap.remove(name);
			propNameMap.remove(name);
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}
	}
	

}
