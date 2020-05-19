import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

public class SysMLDiag {
	
	/*
	 * The general class that store the info of a SysML diagram
	 * In PLCD, there are only Block Definition Diagram and Parametric Diagram
	 */
	
	private Element rootElement;
	private int id;	//PARAM_DIAG = 1; BLOCK_DEFINATION_DAIG = 0
	private String[] tags; //Tags that indicates blocks
	Map<String, NotationShape> shapeMap;	//Map from id in uml file to the elements in the notation file
	
	//Indices of lists corresponds to the indices of tags
	private List<Map<String, UmlClass>> classList;
	private List<Map<String, UmlClass>> classNameList;
	
	
	private Map<String, Property> propMap;
	private Map<String, Property> propNameMap;
	
	private UmlClass domainClass = null;
	
	

	SysMLDiag(int i, String[] t) {
		id = i;
		tags = t;
		classList = new ArrayList<>(tags.length);
		classNameList = new ArrayList<>(tags.length);
		shapeMap = new HashMap<>();
		propMap = new HashMap<>();
		propNameMap = new HashMap<>();
		
		for (int j=0; j<tags.length; j++) {
			classList.add(new HashMap<>());
			classNameList.add(new HashMap<>());
		}
		

	}
	
	
	public void addProperty(Property p) {
		if (p == null) return;
		
		propMap.put(p.getId(), p);
		propNameMap.put(p.getName(), p);
	}
	
	public Collection<Property> getAllProperties() {
		return propMap.values();
	}
	
	public Property getProperty(String pId) {
		return propMap.get(pId);
	}
	
	public Property getPropByName(String name) {
		return propNameMap.get(name);
	}
	
	public boolean containsProperty(String pId) {
		return propMap.containsKey(pId);
	}
	
	public boolean containsPropName(String name) {
		return propNameMap.containsKey(name);
	}
	
	public void removeProperty(String pId) {
		Property p = propMap.get(pId);
		
		propMap.remove(pId);
		propNameMap.remove(p.getName());
	}
	
	public void removePropByName(String name) {
		Property p = propNameMap.get(name);
		
		propNameMap.remove(name);
		propMap.remove(p.getId());
	}
	
	public void setDomainClass(UmlClass clz) {
		domainClass = clz;
	}
	
	public UmlClass getDomainClass() {
		return domainClass;
	}
	
	public Set<UmlClass> getAllClass() {
		Set<UmlClass> classSet = new HashSet<>();
		for (int i=0; i<classList.size(); i++) {
			Map<String, UmlClass> currMap = classList.get(i);
			classSet.addAll(currMap.values());
		}
		
		return classSet;
	}
	
	public int getDiagId() {
		return id;
	}
	
	public String[] getTags() {
		return tags;
	}
	
	public String getTag(int index) {
		return tags[index];
	}
	
	public void setRootElement(Element e) {
		rootElement = e;
	}
	
	public Element getRootElement() {
		return rootElement;
	}
	
	public void addClass(UmlClass c, int index) {
		classList.get(index).put(c.getId(), c);
		classNameList.get(index).put(c.getName(), c);
	}
	
	public Map<String, UmlClass> getClassMap(int index) {
		return classList.get(index);
	}
	
	public Map<String, UmlClass> getClassMap(String tag) {
		for (int i=0; i<tags.length; i++) {
			if (tag.equals(tags[i])) {
				return classList.get(i);
			}
		}
		return null;
	}
	
	
	public int getClassSize(int index) {
		return classList.get(index).size();
	}
	
	
	public UmlClass getClassByName(String name) {

		for (int i=0; i<tags.length; i++) {
			Map<String, UmlClass> nameMap = classNameList.get(i);
			if (nameMap.containsKey(name)) {
		
				
				return nameMap.get(name);
			}
		}
		
		
		return null;
	}
	
	public UmlClass getClassById(String id) {
		for (int i=0; i<tags.length; i++) {
			if (classList.get(i).containsKey(id)) {
				return classList.get(i).get(id);
			}
		}
		return null;
	}
	
	
	public int getClassListSize(int index) {
		return classList.size();
	}
	
	public String getName() {
		return rootElement.getAttribute("name");
	}
	
	public void addShape(NotationShape shape) {
		shapeMap.put(shape.getId(), shape);
	}
	
	public NotationShape getShape(String id) {
		return shapeMap.get(id);
	}
	
	public boolean containsShape(String id) {
		return shapeMap.containsKey(id);
	}
	
	public void removeUmlClass(String id) {
		for (int i=0; i<classList.size(); i++) {
			Map<String, UmlClass> classMap = classList.get(i);
			if (classMap.containsKey(id)) {
				UmlClass clazz = classMap.get(id);
				String name = clazz.getName();
				classMap.remove(id);
				classNameList.get(i).remove(name);
			}
		}
	}
	
	public void removeUmlClassByName(String name) {
		for (int i=0; i<classList.size(); i++) {
			Map<String, UmlClass> classNameMap = classNameList.get(i);
			if (classNameMap.containsKey(name)) {
				UmlClass clazz = classNameMap.get(name);
				String id = clazz.getId();
				classNameMap.remove(name);
				classList.get(i).remove(id);
			}
		}
	}
	
	
	public void removeShape(String id) {
		shapeMap.remove(id);
	}
	
}
