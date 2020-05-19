import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Element;

import com.opencsv.CSVWriter;

public class XMIComparator {
	
	/*
	 * Compare the info between XMI Parsers
	 */
	
	private XMIParser parser1;
	private XMIParser parser2;
	private List<String[]> resultsP1;
	private List<String[]> resultsP2;
	private List<String[]> resultsBtw;
	final static int NUMBER_OF_COLUMNS = 9;
	final static String inputPath = "xmi";
	final static String[] issues = {"Invalid Unit", "New Variable", "New Block", "Deleted Variable", 
			"Deleted Block", "Inconsistent Units", "Changed Unit", "New Connector", "Deleted Connector"};
	
	private Set<String> unitSet;
	//private Set<String> custUnitSet;
	private Map<String, String> varPriorityMap1;	//The hashmap from variable name to priority 
	private Map<String, String> varPriorityMap2;
	private Map<String, String> compPriorityMap1;	//The hashmap from component name to priority
	private Map<String, String> compPriorityMap2;
	
	XMIComparator() {
		parser1 = new XMIParser(inputPath + "/model1.uml", inputPath + "/model1.notation", 1);
		parser2 = new XMIParser(inputPath + "/model2.uml", inputPath + "/model2.notation", 2);
		
		resultsP1 = new ArrayList<>();
		resultsP2 = new ArrayList<>();
		resultsBtw = new LinkedList<>();
		//results2 = parser2.getResults();
		
		
		getUnitsFromFile("units/SIUnits.csv");	//Get SI units
		getUnitsFromFile("units/CustomUnits.csv");	//Get custom units
		getPriority(1);
		getPriority(2);
		
		compareResults(parser1, 1);
		compareResults(parser2, 2);
		compareXMI();
		
		//Filter out valid changes using change logs and change lists (Only checks the cross-product issues)
		filterResults();
	}
	
	
	private int filterResults() {
		BufferedReader reader;
		Map<Integer, Issue> changeMap = new HashMap<>();
		
		//Read the valid change list from file
		try {
			reader = new BufferedReader(new FileReader("change/ChangeList.csv"));
			String line = null;
			boolean isHeader = true;
			while ((line = reader.readLine()) != null) {
				
				if (isHeader) {
					isHeader = false;
					continue;
				}

				String[] tokens = line.split(",");
				Issue row = new Issue(tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7]);
				int rowHash = row.hashCode();
				
				changeMap.put(rowHash, row);
				
				
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Filter out the found cross-product issues
		List<String[]> tempRes = new LinkedList<>();
		
		for (String[] res: resultsBtw) {
			Issue resIssue = new Issue(res[1], res[2], res[3], res[4], res[5], res[6]);
			int resHash = resIssue.hashCode();
			
			if (changeMap.containsKey(resHash)) {
				tempRes.add(res);
			}
		}
		
		return tempRes.size();
	}
	
	
	private void getUnitsFromFile(String filename) {
		//Scanner scanner;
		BufferedReader reader;
		unitSet = new HashSet<>();

		
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = null;
			boolean isHeader = true;
			while ((line = reader.readLine()) != null) {
				
				if (isHeader) {
					isHeader = false;
					continue;
				}

				String[] tokens = line.split(",");
				unitSet.add(tokens[2]);
				
				//If the line is empty in last column
				if (tokens.length > 3) {
					unitSet.add(tokens[3]);
				} 
				
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
        //return rows;
	}
	
	
	private void getPriority(int product) {
		BufferedReader reader;
		Map<String, String> varPriorityMap = new HashMap<>();
		Map<String, String> compPriorityMap = new HashMap<>();
		
		if (product == 1) {
			varPriorityMap1 = varPriorityMap;
			compPriorityMap1 = compPriorityMap;
		}
		else if (product == 2) {
			varPriorityMap2 = varPriorityMap;
			compPriorityMap2 = compPriorityMap;
		}
		else {
			throw new IllegalArgumentException();
		}
		
		String filename = "priority/priority" + Integer.toString(product) + ".csv"; 
		
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = null;
			while ((line = reader.readLine()) != null) {
			    
				String[] tokens = line.split(",");
				
				varPriorityMap.put(tokens[1], tokens[4]);
				
				//Here we assume that the priority doesn't change in P2
				if (compPriorityMap.containsKey(tokens[2])) {
					String oldPr = compPriorityMap.get(tokens[2]);
					int oldPrNum = PriorityComparator.priorityNumMap.get(oldPr);
					int newPrNum = PriorityComparator.priorityNumMap.get(tokens[4]);
					
					if (newPrNum > oldPrNum) {
						compPriorityMap.put(tokens[2], tokens[4]);
					}
				}
				else {
					compPriorityMap.put(tokens[2], tokens[4]);
					
				}
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public List<String[]> getResults(int p) {
		//P represents the indices of products
		//1 means results in product 1, 2 means results in product 2, 3 means the difference between products.
		
		if (p == 1) {
			return resultsP1;
		}
		else if (p == 2) {
			return resultsP2;
		}
		else {
			return resultsBtw;
		}
		
	}
	
	public XMIParser getParser(int product) {
		if (product == 1) return parser1;
		else if (product == 2) return parser2;
		else return null;
	}
	
	private void checkUnitValidity(Collection<Property> varSet, XMIParser parser, List<String[]> results, Map<String, String> varPrMap) {
		//Check the validity of variable units
		
		Iterator<Property> varIter = varSet.iterator();
		SysMLModel model = parser.getSysMLModel();
		
		while(varIter.hasNext()) {
			Property var = varIter.next();
			String varName = var.getName();
			

			
			//Check if the variable is shown in the diagram
			UmlClass parent = var.getParentClass();
			
			//System.out.println(var.getName() + parent.getName());
			
			//If it is a block variable
			if (varName.contains(".") && parent.equals(model.getParamDiag().getDomainClass())) {
				int iDot = varName.indexOf('.');
				String domain = varName.substring(0, iDot);
				parent = model.getComposition(domain);
				
				if (parent == null) {
					continue;
				}		
			}
			
			//If it is a constraint variable
			if (model.getBlockDefDiag().getClassById(parent.getId()) == null) {
				continue;
			}
			
			
			String typeId = var.getTypeId();
			DataType datatype = parser.getSysMLModel().getDataType(typeId);
			
			
			if (datatype == null) {
				throw new NullPointerException("DataType id " + typeId + " cannot be found in model " + parser.version);
			}
			
			String datatypeName = datatype.getName();
			
			
			if (!unitSet.contains(datatypeName)) {
				String[] issue = new String[NUMBER_OF_COLUMNS];
				
				Arrays.fill(issue, "");
				issue[1] = datatypeName;
				issue[2] = var.getName();
				
				int dot = issue[2].indexOf('.');
				if (dot >= 0) {
					String domain = issue[2].substring(0, dot);
					issue[3] = model.getComposition(domain).getName();
				}
				else {
					issue[3] = var.getParentClass().getName();
				}
				
				
				issue[7] = issues[0]; //"Invalid Unit"
				
				if (varPrMap.containsKey(var.getName())) {
					issue[8] = varPrMap.get(var.getName());
				}
				else {
					issue[8] = "";
				}
				
				
				//addedVariables.add(hashcode);
				results.add(issue);
			}
		}
	}
	
	
	private void compareResults(XMIParser parser, int p) {
		//Compare the units in single products.
		//For each product, the results are stored in List<String[]>
		//Each element in the list represents one change
		//The sequence in String[] is {"ID", "Unit", "Variable", "Components", "Unit", "Variable", "Components", "Problem", "Priority"}
		 
		List<String[]> results = null;
		Map<String, String> varPrMap = null;
		Set<Connector> connSet = parser.getConnectors();
		Collection<Property> varSet = parser.getSysMLModel().getAllVariables();
		
		if (p == 1) {
			//model = parser1.getSysMLModel();
			results = resultsP1;
			varPrMap = varPriorityMap1;
		}
		else if (p == 2) {
			//model = parser2.getSysMLModel();
			results = resultsP2;
			varPrMap = varPriorityMap2;
		}
		
		checkUnitValidity(varSet, parser, results, varPrMap);
		
		//System.out.println(results.size());
		
		Iterator<Connector> connIter = connSet.iterator();
		
		while(connIter.hasNext()) {
			Connector conn = connIter.next();
			
			ConnectorEnd end1 = conn.getEnd(0);
			ConnectorEnd end2 = conn.getEnd(1);
			
			if (!end1.getUnitName().equals(end2.getUnitName())) {
				String[] row = new String[NUMBER_OF_COLUMNS];
				
				row[1] = end1.getUnitName();
				row[2] = end1.getVariableName();
				row[3] = end1.getComponentName();
				row[4] = end2.getUnitName();
				row[5] = end2.getVariableName();
				row[6] = end2.getComponentName();
				row[7] = issues[5]; //"Inconsistent Units"
				row[8] = getHigherPriority(row[2], row[5], p);				
				
				
				results.add(row);
			}
		}
		
		
		sortStringArrayList(results);
	}
	
	private void sortStringArrayList(List<String[]> results) {
		if (results == null || results.size() == 0) {
			return;
		}
		
		results.sort(new PriorityComparator());
		
		//Assign row number to each issue result
		for (int i=0; i<results.size(); i++) {
			String[] row = results.get(i);
			row[0] = Integer.toString(i+1);
		}
	}
	
	private String getHigherPriority(String var1, String var2, int p) {
		
		Map<String, String> varPrMap1 = null;
		Map<String, String> varPrMap2 = null;
		
		//System.out.println("getHigherPriority");
		
		
		if (p == 1) {
			varPrMap1 = varPriorityMap1;
			varPrMap2 = varPriorityMap1;
		}
		else if (p == 2) {
			varPrMap1 = varPriorityMap2;
			varPrMap2 = varPriorityMap2;
		}
		else if (p == 3) {
			varPrMap1 = varPriorityMap1;
			varPrMap2 = varPriorityMap2;
		}
		else {
			throw new IllegalArgumentException();
		}
		
		
		if (varPrMap1.containsKey(var1) || varPrMap2.containsKey(var2)) {
			
			//If both sides have priorities, choose the higher one
			if (varPrMap1.containsKey(var1) && varPrMap2.containsKey(var2)) {
				Map<String, Integer> prNumMap = PriorityComparator.priorityNumMap; 
				String pr0 = varPrMap1.get(var1);
				String pr1 = varPrMap2.get(var2);
				int res = Integer.compare(prNumMap.get(pr0), prNumMap.get(pr1));
				if (res <= 0) {
					return pr0;
				}
				else
					return pr1;
			}
			else if (varPrMap1.containsKey(var1)) {
				return varPrMap1.get(var1);
			}
			else if (varPrMap2.containsKey(var2)) {
				return varPrMap2.get(var2);
			}
		}
		return "";
	}
	
	
	public void printResults() {
		
    	//Format the output
    	try {
			CSVWriter writer = new CSVWriter(new FileWriter("Results.csv"));
			
			writer.writeNext(new String[]{"Product 1"});
			for (String[] change: resultsP1) {
				writer.writeNext(change);
			}
			
			writer.writeNext(new String[]{"Product 2"});
			for (String[] change: resultsP2) {
				writer.writeNext(change);
			}
			
			writer.writeNext(new String[]{"Compare Product 1 and Product 2"});
			for (String[] change:resultsBtw) {
				writer.writeNext(change);
			}
			
			
			writer.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
    }
	
	private Set<UmlClass> identifyInvisibleComp(Set<UmlClass> compSet) {
		//When papyrus delete a component, it deletes the component in notation file
		//but not in the uml file
		//So when a component doesn't have its corresponding notation shape, it it invisible
		
		Set<UmlClass> invisible = new HashSet<>();
		
		Iterator<UmlClass> it = compSet.iterator();
		
		while(it.hasNext()) {
			UmlClass curr = it.next();
			
			if (curr.getNotationShape() == null) {
				invisible.add(curr);
			}
		}
		
		return invisible;
	}
	
	//Add New Vars found In Block Def Diagram
	private void identifyNewVar(Set<Property> newVarSet, UmlClass compInP2) {
		
		Iterator<Property> itNewVar = newVarSet.iterator();
		while(itNewVar.hasNext()) {

			Property newVar = itNewVar.next();
			
			
			String[] diff = new String[NUMBER_OF_COLUMNS];
			Arrays.fill(diff, "");
			
			diff[5] = newVar.getName();
			diff[6] = compInP2.getName();
			diff[7] = issues[1]; //New variable
			
			if (varPriorityMap2.containsKey(diff[5])) {
				diff[8] = varPriorityMap2.get(diff[5]);
			}
			
			resultsBtw.add(diff);
		}
	}
	
	//Add Del Vars found In Block Def Diagram
	private void identifyDelVar(Set<Property> delVarSet, UmlClass compInP1) {
		Iterator<Property> itDelVar = delVarSet.iterator();
		while(itDelVar.hasNext()) {
			
			Property delVar = itDelVar.next();
			
			//If the element has association attribute, ignore the element;
			Element delVarEle = delVar.getElement();
			if (delVarEle.hasAttribute("association")) {
				continue;
			}
			
			String[] diff = new String[NUMBER_OF_COLUMNS];
			Arrays.fill(diff, "");
			
			diff[2] = delVar.getName();
			diff[3] = compInP1.getName();	
			diff[7] = issues[3]; //Deleted Variable
			
			if (varPriorityMap1.containsKey(diff[2])) {
				diff[8] = varPriorityMap1.get(diff[2]);
			}
			else {
				diff[8] = "";
			}
			
			resultsBtw.add(diff);
		}
	}
	
	
	
	private void compareXMI() {

		
		SysMLModel model1 = parser1.getSysMLModel();
		SysMLDiag blockDiag1 = model1.getBlockDefDiag();
		Set<UmlClass> compSet1 = blockDiag1.getAllClass();
		SysMLDiag paramDiag1 = model1.getParamDiag();
		UmlClass domainParamClass1 = paramDiag1.getDomainClass();
		
		SysMLModel model2 = parser2.getSysMLModel();
		SysMLDiag blockDiag2 = model2.getBlockDefDiag();
		Set<UmlClass> compSet2 = blockDiag2.getAllClass();
		SysMLDiag paramDiag2 = model1.getParamDiag();
		//UmlClass domainParamClass2 = paramDiag1.getDomainClass();
		
		//Components in P2 not in P1 (New block)
		Set<UmlClass> compNotIn1 = new HashSet<>(compSet2);
		compNotIn1.removeAll(compSet1);
		Set<UmlClass> invisibleIn1 = identifyInvisibleComp(compNotIn1);
		compNotIn1.removeAll(invisibleIn1);
		
		Iterator<UmlClass> itNewComp = compNotIn1.iterator();

		while(itNewComp.hasNext()) {

			UmlClass newComp = itNewComp.next();
			String[] diff = new String[NUMBER_OF_COLUMNS];
			Arrays.fill(diff, "");

			diff[6] = newComp.getName();
			diff[7] = issues[2]; //New block

			if (compPriorityMap2.containsKey(newComp.getName())) {
				diff[8] = compPriorityMap2.get(newComp.getName());
			}
			else {
				diff[8] = "";
			}
			
			resultsBtw.add(diff);
			
			Set<Property> newVarSet = newComp.getAllVariables();
			identifyNewVar(newVarSet, newComp);
		}	
 		
		//Components in P1 not in P2 (Deleted block)
		Set<UmlClass> compNotIn2 = new HashSet<>(compSet1);
		compNotIn2.removeAll(compSet2);
		Set<UmlClass> invisibleIn2 = identifyInvisibleComp(compNotIn2);
		compNotIn1.removeAll(invisibleIn2);
		
		Iterator<UmlClass> itDelComp = compNotIn2.iterator();
		while(itDelComp.hasNext()) {
			
			
			UmlClass delComp = itDelComp.next();
			
			String[] diff = new String[NUMBER_OF_COLUMNS];
			Arrays.fill(diff, "");
			
			diff[3] = delComp.getName();
			diff[7] = issues[4]; //"Deleted Block"
			
			
			if (compPriorityMap1.containsKey(delComp.getName())) {
				diff[8] = compPriorityMap1.get(delComp.getName());
			}
			else {
				diff[8] = "";
			}
			
			resultsBtw.add(diff);
			
			Set<Property> delVarSet = delComp.getAllVariables();
			identifyDelVar(delVarSet, delComp);
		}
		
		Set<UmlClass> compIntersect = new HashSet<>(compSet1);
		compIntersect.retainAll(compSet2);
		
		
		
		//For the same component, compare the variable
		//For the same variable, compare the unit in two products
		
		
		Iterator<UmlClass> iter = compIntersect.iterator();
		while(iter.hasNext()) {
			
			
			UmlClass compInP1 = iter.next();
			UmlClass compInP2 = blockDiag2.getClassByName(compInP1.getName());
			

			Set<Property> varSet1 = compInP1.getAllVariables();
			Set<Property> varSet2 = compInP2.getAllVariables();
			
			if (compInP1.getName().equals(domainParamClass1.getName())) {
				//If the current component is domain component, add parameters in parametric diagrams to the variable set
				
//				Set<Property> domainParamVarSet1 = domainParamClass1.getAllVariables();				
//				varSet1.addAll(domainParamVarSet1);
//				
//				Set<Property> domainParamVarSet2 = domainParamClass2.getAllVariables();				
//				varSet2.addAll(domainParamVarSet2);
				
				//This part has been covered in compareVarInParam()
				continue;
				
			}
			
			
			//varSet2 - varSet1
			Set<Property> notIn1 = new HashSet<>(varSet2);
			notIn1.removeAll(varSet1);
			identifyNewVar(notIn1, compInP2);
			
				
			//varSet1 - varSet2
			Set<Property> notIn2 = new HashSet<>(varSet1);
			notIn2.removeAll(varSet2);
			identifyNewVar(notIn2, compInP1);
			
				
			//Intersection of varSet1 and varSet2
			Set<Property> intersect = new HashSet<>(varSet1);
			intersect.retainAll(varSet2);
				
			//For variables in the intersection, compare the units
			Iterator<Property> itVar = intersect.iterator();
			
			//Compare the units for the same variable can be seperated into two parts
			//One is the variables with dots (The variables in the parametric diagram).
			//Another is the variables without dots (The variables in the block definition diagram)
			while(itVar.hasNext()) {
				
				Property varInP1 = itVar.next();
				Property varInP2 = blockDiag2.getPropByName(varInP1.getName());
				
				if (varInP2 == null) {
					varInP2 = paramDiag2.getPropByName(varInP1.getName());
				}
				
				if (varInP2 == null) {
					throw new NullPointerException("Variable " + varInP1.getName() + " doesn't exist in P2");
				}

				
				String typeId1 = varInP1.getTypeId();
				String typeId2 = varInP2.getTypeId();
				
				DataType datatype1 = model1.getDataType(typeId1);
				DataType datatype2 = model2.getDataType(typeId2);
				
				
				if (datatype1 == null || datatype2 == null) {
					continue;
				}
				
				String typeName1 = datatype1.getName();
				String typeName2 = datatype2.getName();
				
				//If the unit id or unit name changed in P2, record it as Changed Unit Issue
				if (!datatype1.equals(datatype2) || !typeName1.equals(typeName2)) {
					
					String[] diff = new String[NUMBER_OF_COLUMNS];
					diff[0] = "";
					diff[1] = datatype1.getName();
					diff[2] = varInP1.getName();
					diff[3] = compInP1.getName();
					diff[4] = datatype2.getName();
					diff[5] = varInP2.getName();
					diff[6] = compInP2.getName();
					diff[7] = issues[6]; //Changed unit
					
					diff[8] = getHigherPriority(diff[2], diff[5], 3);
					
					//getHigherPriority(diff[2], diff[]);
					
					resultsBtw.add(diff);
					
				}
			}
		}
		
		compareVarInParam();
		
		/*
		 * The identification of connectors not in use
		 */
		//Extract the connectors that exists in Product 1, but not in Product 2 (delConns)
		//And connectors that exists in Product 2, but not in Product 1 (newConn)
		Set<Connector> connectors1 = new HashSet<>(parser1.getSysMLModel().getAllConnectors());

		Set<Connector> connectors2 = new HashSet<>(parser2.getSysMLModel().getAllConnectors());
		
		Set<Connector> newConns = new HashSet<>(connectors2);
		Set<Connector> delConns = new HashSet<>(connectors1);
		
		newConns.removeAll(connectors1);
		delConns.removeAll(connectors2);
		
		addConnChanges(newConns, issues[7]);
		addConnChanges(delConns, issues[8]);

		sortStringArrayList(resultsBtw);
		
		
	}
	
	
	//Add the variable results (new var/ deleted var) found in Parametric Diagram
	private void addVarResults(Property prop, int issue) {
		SysMLModel model1 = parser1.getSysMLModel();
		SysMLModel model2 = parser2.getSysMLModel();
		
		int[] newVarIndices = new int[] {5, 6};
		int[] delVarIndices = new int[] {2, 3};
		int[] varIndices = null;
		SysMLModel model = null;
		
		if (issue == 1) {	//New Variable
			varIndices = newVarIndices;
			model = model2;
		}
		else if (issue == 3) {	//Deleted Variable
			varIndices = delVarIndices;
			model = model1;
		} 
		else {
			return;
		}
		
		
		String newVarName = prop.getName(); 
		
		String[] diff = new String[NUMBER_OF_COLUMNS];
		Arrays.fill(diff, "");
		
		diff[varIndices[0]] = newVarName;
		
		setParentName(prop, model, diff, varIndices[1]);
		
		diff[7] = issues[issue]; 
		
		if (varPriorityMap2.containsKey(diff[5])) {
			diff[8] = varPriorityMap2.get(diff[5]);
		}
		
		resultsBtw.add(diff);
	}
	
	private void compareVarInParam() {
		SysMLModel model1 = parser1.getSysMLModel(); 
		SysMLModel model2 = parser2.getSysMLModel();
		
		UmlClass domain1 = model1.getParamDiag().getDomainClass();
		UmlClass domain2 = model2.getParamDiag().getDomainClass();
		
		Set<Property> propSet1 = domain1.getAllVariables();
		Set<Property> propSet2 = domain2.getAllVariables();
		
		Set<Property> varSet1 = new HashSet<Property>();
		Set<Property> varSet2 = new HashSet<Property>();
		
		Iterator<Property> propIter1 = propSet1.iterator();
		while (propIter1.hasNext()) {
			Property domainProp = propIter1.next();
			if (domainProp.getName().contains(".")) {
				varSet1.add(domainProp);
			}

		}
		
		Iterator<Property> propIter2 = propSet2.iterator();
		while (propIter2.hasNext()) {
			Property domainProp = propIter2.next();
			if (domainProp.getName().contains(".")) {
				varSet2.add(domainProp);
			}

		}
		
		
		//New variable
		Set<Property> newVarSet = new HashSet<>(varSet2);
		newVarSet.removeAll(varSet1);
		Iterator<Property> newVarIter = newVarSet.iterator();
		
		while(newVarIter.hasNext()) {
			Property newVar = newVarIter.next();
			
			addVarResults(newVar, 1);	//Issue #1: New variable
		}
		
		//Deleted variable
		Set<Property> delVarSet = new HashSet<>(varSet1);
		delVarSet.removeAll(varSet2);		
		Iterator<Property> delVarIter = delVarSet.iterator();
		
		while(delVarIter.hasNext()) {
			Property delVar = delVarIter.next();
			
			addVarResults(delVar, 3);	//Issue #2: Deleted variable
		}
		
		//Changed unit
		Set<Property> interVar = new HashSet<>(varSet1);
		interVar.retainAll(varSet2);
		Iterator<Property> interVarIter = interVar.iterator();
		
		while (interVarIter.hasNext()) {
			Property var1 = interVarIter.next();
			Property var2 = model2.getParamDiag().getPropByName(var1.getName());
			DataType type1 = model1.getDataType(var1.getTypeId());
			DataType type2 = model2.getDataType(var2.getTypeId());
			
			
			if (!type1.getName().equals(type2.getName())) {
				String[] diff = new String[NUMBER_OF_COLUMNS];
				
				diff[0] = "";
				diff[1] = type1.getName();
				diff[2] = var1.getName();
				
				setParentName(var1, model1, diff, 3);
				
				diff[4] = type2.getName();
				diff[5] = var2.getName();
				
				setParentName(var2, model2, diff, 6);
				
				diff[7] = issues[6]; //Changed unit
				
				diff[8] = getHigherPriority(diff[2], diff[5], 3);

				
				resultsBtw.add(diff);
			}
			
		}
		
	}
	
	private void setParentName(Property prop, SysMLModel model, String[] diff, int diffInd) {
		
		
		
		String varName = prop.getName();
		int index = varName.indexOf('.');
		
		if (index >= 0) {
			String domain = varName.substring(0, index);
			UmlClass compClass = model.getComposition(domain);
			
			if (compClass == null) {
				throw new NullPointerException("Domain " + domain + " doesn't exist in P2" );
			}
			
			diff[diffInd] = compClass.getName();				
		}
		else {
			String adjId = prop.getElement().getAttribute("type");
			UmlClass adjClass = model.getUmlClass(adjId);
			
			diff[diffInd] = adjClass.getName();
		}
	}
	
	private int addConnChanges(Set<Connector> conns, String issue) {
		//Add connector changes to the results
		int counter = 0;
		Iterator<Connector> it = conns.iterator();
		
		while (it.hasNext()) {
			
			counter++;
			Connector conn = it.next();
			
			List<ConnectorEnd> ends = conn.getEnds();
			ConnectorEnd end1 = ends.get(0);
			ConnectorEnd end2 = ends.get(1);
			
			String[] diff = new String[NUMBER_OF_COLUMNS];
			diff[0] = Integer.toString(counter);
			diff[1] = "";
			diff[2] = end1.getVariableName();
			diff[3] = end1.getComponentName();
			diff[4] = "";
			diff[5] = end2.getVariableName();
			diff[6] = end2.getComponentName();
			diff[7] = issue;
			
			int p = 0;
			if (issue.equals(issues[7])) {	//New Connector
				p = 2;
			}
			else if (issue.equals(issues[8])) {	//Deleted Connector
				p = 1;
			}
			else {
				throw new IllegalArgumentException();
			}
			
			diff[8] = getHigherPriority(diff[2], diff[5], p);
			
			resultsBtw.add(diff);
		}
		
		return counter;
	}
	
}
