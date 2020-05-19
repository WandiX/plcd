import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class ModelModifier extends JFrame {

	/*
	 *  Combine the SysML model comparator and interface
	 *  Receive user inputs and modify the SysML models 
	 */
	
	// Get screen size using the Toolkit class
	final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	final static int screenHeight = screenSize.height;
	final static int screenWidth = screenSize.width;
	
	private static final long serialVersionUID = 1L;
	
	// Define the size of interface elements
	final static int WIDTH_OF_TABLE_PANEL = (int)(0.5 * screenWidth);
	final static int HEIGHT_OF_SCROLLPANE = (int)(0.2 * screenHeight);
	final static int WIDTH_OF_WINDOW = screenWidth;
	final static int HEIGHT_OF_WINDOW = (int)(0.9 * screenHeight);
	final static int WIDTH_OF_BUTTON = (int)(0.2 * WIDTH_OF_TABLE_PANEL);
	final static int HEIGHT_OF_BUTTON = (int)(0.05 * screenHeight);
	final static int HORIZONTAL_POS_OF_BUTTON = (int)(0.8 * screenHeight);
	final static int VERTICAL_POS_OF_BUTTON = (int)(0.05 * screenWidth);
	final static int GAP_BTW_SCROLLPANE = (int)(0.05 * screenHeight);
	
	final static int P1 = 1;
	final static int P2 = 2;
	final static int BTW = 3;
	final static int INIT = 0;
	final static String[] issues = XMIComparator.issues;
	
	static ChangeTable tableP1; 
	static ChangeTable tableP2;
	static ChangeTable tableBtw;
	static GraphicPanel graphicPanel;
	static int selectedNum = 0;
	static XMIComparator comparator;
	
	//The updated issues 
	static List<String[]> updatedP1;
	static List<String[]> updatedP2;
	static List<String[]> updatedBtw;
	

	ModelModifier(){        

	    //Set the layout to be borderlayout, the gap is 5 pixels
		setLayout(new BorderLayout(5,5)); 

	    setFont(new Font("Helvetica", Font.PLAIN, 14));

	    graphicPanel = new GraphicPanel(); 
	    comparator = new XMIComparator();  //Load comparison results between P1 and P2
	    updatedP1 = new ArrayList<>();
	    updatedP2 = new ArrayList<>();
	    updatedBtw = new ArrayList<>();

	    getContentPane().add("West",  initTablePanel());
	    getContentPane().add("Center", graphicPanel);

	}
	
	private static JPanel initTablePanel() {
		
		
		
		JPanel tablePanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
			   return new Dimension(WIDTH_OF_TABLE_PANEL, HEIGHT_OF_WINDOW);
			};
		};
		tablePanel.setLayout(null);
		
		initTables();
		
		for (int i=1; i<4; i++) {			
			
			String descrip = "The change list ";
			
			if (i == 3) {
				descrip += "Between P1 and P2";
			}
			else {
				descrip += "of P" + Integer.toString(i);
			}
			
			JLabel label = new JLabel(descrip);
			
			//Add description labels
			int hPosLabel = (i - 1) * (HEIGHT_OF_SCROLLPANE + GAP_BTW_SCROLLPANE) - 70;
			label.setBounds(10, hPosLabel, WIDTH_OF_TABLE_PANEL, HEIGHT_OF_SCROLLPANE);
			tablePanel.add(label);
			
			//Fill in table data and put the table to scrollPanel
			int hPosScroll = i * (HEIGHT_OF_SCROLLPANE + GAP_BTW_SCROLLPANE) - HEIGHT_OF_SCROLLPANE;
			JScrollPane scrollPane = new JScrollPane(getTable(i));
			scrollPane.setBounds(0, hPosScroll, WIDTH_OF_TABLE_PANEL, HEIGHT_OF_SCROLLPANE);

			
			tablePanel.add(scrollPane);
		}
		
        
        //Click to Update XMI
        JButton updateBtn = new JButton("Update");
        updateBtn.setBounds(VERTICAL_POS_OF_BUTTON, HORIZONTAL_POS_OF_BUTTON, WIDTH_OF_BUTTON, HEIGHT_OF_BUTTON);
        
        updateBtn.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		if (tableP1.selectedInvalidUnit() || tableP2.selectedInvalidUnit() || tableBtw.selectedInvalidUnit()) {
        			String input = JOptionPane.showInputDialog("Please enter the new units to replace invalid units");
        			//System.out.println(input);
        			
        			if (input != null && !input.equals("")) {
        				JOptionPane.showMessageDialog(null, "Please rerun the tool after update", "Alert", JOptionPane.ERROR_MESSAGE);
        				
        				updateXMI(input);	
        			}
        			else if (input == null){	//When the user clicks "Cancel"
        				return;
        			}
        			else {	//When the user clicks "OK" with the empty input (I.e., input is "")
        				JOptionPane.showMessageDialog(null, "The new units can not be empty", "Alert", JOptionPane.ERROR_MESSAGE);
        			}
            		
        		}
        		else {
        			Object selected = JOptionPane.showConfirmDialog(null,
        					"Do you want to update selected issues?", "Confirmation", JOptionPane.YES_NO_OPTION);
        			
        			if (selected != null && (int)selected == 0) {
        				JOptionPane.showMessageDialog(null, "Please rerun the tool after update", "Alert", JOptionPane.INFORMATION_MESSAGE);
        				
        				updateXMI(null);
        			}
        		}
        		
        		
        	}
        });
        
        //Click to ignore selected rows
        JButton ignoreBtn = new JButton("Ignore Issues");
        ignoreBtn.setBounds(VERTICAL_POS_OF_BUTTON * 2 + WIDTH_OF_BUTTON, HORIZONTAL_POS_OF_BUTTON, WIDTH_OF_BUTTON, HEIGHT_OF_BUTTON);
        ignoreBtn.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		//Let the user choose the correct part
        		int selected = Integer.valueOf(JOptionPane.showConfirmDialog(null, "Are you sure you want to ignore selected issues?", 
        				"Confirmation", JOptionPane.YES_NO_OPTION));
        		
	
        		if (selected == 0){  //0 if user clicks "yes" 
        			tableP1.clearSelected();
        			tableP2.clearSelected();
        			tableBtw.clearSelected();
        		} 
        	}
        });
        
        
        
        //Click to generate report
        JButton reportBtn = new JButton("Generate Report");
        reportBtn.setBounds(VERTICAL_POS_OF_BUTTON * 3 + WIDTH_OF_BUTTON * 2, HORIZONTAL_POS_OF_BUTTON, WIDTH_OF_BUTTON, HEIGHT_OF_BUTTON);
        
        
        
        reportBtn.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		//ReportTemplate template = new ReportTemplate(updatedP1, updatedP2, updatedBtw);
        		
        		//JOptionPane.showMessageDialog(null, "Report Generated!", "Reminder", JOptionPane.INFORMATION_MESSAGE);
        		
        		Object[] options = { "Identification", "Update" };
        		
        		/*
        		int selected = Integer.valueOf(JOptionPane.showOptionDialog(null, "Choose the type of report", "Options",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]));
        		
        		if (selected == 0) {
        			JOptionPane.showMessageDialog(null, "Identification Report Generated!", "Reminder", JOptionPane.INFORMATION_MESSAGE);
        		}
        		else {
        			JOptionPane.showMessageDialog(null, "Update Report Generated!", "Reminder", JOptionPane.INFORMATION_MESSAGE);
        		}
        		*/
        		
        		Object selectedValue = JOptionPane.showInputDialog(null,
        				"Choose the type of report", "Input",
        				JOptionPane.INFORMATION_MESSAGE, null,
        				options, options[0]);
        		
        		String selected = String.valueOf(selectedValue);
        
        		if (selected.equals(options[0])) {
        			
        			ReportTemplate template = new ReportTemplate(tableP1.getAllRowsContent(), tableP2.getAllRowsContent(), 
        					tableBtw.getAllRowsContent(), ReportTemplate.IDENTI_REPORT);
        			
        			JOptionPane.showMessageDialog(null, "Identification Report Generated!", "Reminder", JOptionPane.INFORMATION_MESSAGE);
        		}
        		else if (selected.equals(options[1])) {
        			ReportTemplate template = new ReportTemplate(updatedP1, updatedP2, updatedBtw, ReportTemplate.UPDATE_REPORT);
        			
        			JOptionPane.showMessageDialog(null, "Update Report Generated!", "Reminder", JOptionPane.INFORMATION_MESSAGE);
        		}
        	}
        });
       
        
        
        
       
        tablePanel.add(updateBtn);
        tablePanel.add(ignoreBtn);
        tablePanel.add(reportBtn);
        
        
        
        return tablePanel;
    }
	
	private static void updateXMI(String input) {
		/*
		 *  The issue table with possible solutions
		 *  
			Label & Category & Explanation & \bfseries Solution (If wrong)
			IU & Invalid Unit & Unit not in SI & Update the unit
			NV & New Variable & Variable not in P1 exists in P2 & Delete new variable
			NB & New Block & Block not in P1 exists in P2 & Delete new block
			DV & Deleted Variable & Variable not in P2 exists in P1 & Recover the variable with units
			DB & Deleted Block & Block not in P2 exists in P1 & Recover the Component		
			CU & Inconsistent Units & Two connected variables have different units & Make two units the same
			CU & Changed Unit & Same variables has different units in P1 and P2 & Make two units the same 
		 */
		
		List<String[]> selectedP1 = tableP1.getSelectedRowsContent();
		//List<Integer> selectedListP1 = tableP1.getSelectedRowsNum();
		List<String[]> selectedP2 = tableP2.getSelectedRowsContent();
		//List<Integer> selectedListP2 = tableP2.getSelectedRowsNum();
		List<String[]> selectedBtw = tableBtw.getSelectedRowsContent();
		//List<Integer> selectedListBtw = tableBtw.getSelectedRowsNum();
		
		//System.out.println(selectedBtw.size());
		issueIdentifier(selectedP1, 1, input);
		issueIdentifier(selectedP2, 2, input);
		issueIdentifier(selectedBtw, 3, input);
		
		updatedP1.addAll(selectedP1);
		updatedP2.addAll(selectedP2);
		updatedBtw.addAll(selectedBtw);
		
		tableP1.removeSelectedRows();
		tableP2.removeSelectedRows();
		tableBtw.removeSelectedRows();
	}
	
	private static void issueIdentifier(List<String[]> selected, int p, String input) {
		//System.out.println(p + "" + selected.size());
		
		for (String[] row: selected) {
			//System.out.println(row[7]);
			
			
			//int rowNum = Integer.parseInt(row[0]);
			String unit1 = row[1];
			String var1 = row[2];
			String comp1 = row[3];
			String unit2 = row[4];
			String var2 = row[5];
			String comp2 = row[6];
			
			//System.out.println(comp1 + "********************");
			
			XMIParser parser1 = comparator.getParser(1);
			XMIParser parser2 = comparator.getParser(2);
			
			if (parser1 == null) {
				throw new NullPointerException("Parser1 is empty");
			}
			
			if (parser2 == null) {
				throw new NullPointerException("Parser2 is empty");
			}
			
//			SysMLModel model1 = parser1.getSysMLModel();
//			SysMLModel model2 = parser2.getSysMLModel();
			
//			SysMLDiag param1 = model1.getParamDiag();
//			SysMLDiag param2 = model2.getParamDiag();
//			
//			SysMLDiag block1 = model1.getBlockDefDiag();
//			SysMLDiag block2 = model2.getBlockDefDiag();
			
//			UmlClass classInBlock1 = block1.getClassByName(comp1);
//			UmlClass classInBlock2 = block2.getClassByName(comp2);
			
//			UmlClass classInParam1 = param1.getClassByName(comp1);
//			UmlClass classInParam2 = param2.getClassByName(comp2);
			
			//Element rtElement1 = 
			
			
			
//			if (classInParam1 == null) {
//				System.out.println(comp1 + " cannot be found in Parametric Diagram of P1");
//				System.exit(-1);
//			}
//			
//			if (classInParam2 == null) {
//				System.out.println(comp2 + " cannot be found in Parametric Diagram of P2");
//				System.exit(-1);
//			}
			
			if (row[7].equals(issues[0])) {		//Invalid Unit
				
				updateInvalidUnit(input, p, var1, comp1);
			}
			else if (row[7].equals(issues[1])) {	//New Variable
				//Note that the current ChangeTables don't contain connections.
				updateNewVar(comp2, var2);				
			}
			else if (row[7].equals(issues[2])) {	//New Block
				
				updateNewBlock(comp2);
				
			}
			else if (row[7].equals(issues[3])) {	//Deleted Variable
				//Get the UmlClass of comp1 in block definition diagram
				updateDeletedVar(comp1, var1);
				
			}
			else if (row[7].equals(issues[4])) {	//Deleted Block
				//Note that the current function doesn't add previous associations
				updateDeletedBlock(comp1);
			}
			else if (row[7].equals(issues[5])) {	//Inconsistent Units
				
//				if (classInBlock1 == null) {
//					System.out.println(comp1 + " cannot be found in Block Definition Diagram of P1");
//					System.exit(-1);
//				}
//				
//				if (classInBlock2 == null) {
//					System.out.println(comp2 + " cannot be found in Block Definition Diagram of P2");
//					System.exit(-1);
//				}
				
				//System.out.println("x1x");
				updateInconsistentUnits(p, unit1, unit2, var1, var2, comp1, comp2);
			}
			else if (row[7].equals(issues[6])) {	//Changed Unit
				updateChangedUnit(comp1, comp2, var2, unit1);
			}
			else if (row[7].equals(issues[7])) {	//New Connector
				
				updateNewConn(var1, var2);
			}
			else if (row[7].equals(issues[8])) {	//Deleted Connector				
				updateDeletedConn(var1, var2, comp1, comp2);
			}
			else {
				throw new IllegalArgumentException("Issue " + row[7] + " cannot be recognized.");
			}
			
			parser1.writeXmiFile();
			parser2.writeXmiFile();
		}
	}
	
	private static void updateInvalidUnit(String input, int p, String var1, String comp1) {
		if (input == null || input.equals("")) 
			return;
		
		XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		
		
		XMIParser parser = null;
		SysMLModel model = null;
		
		if (p == 1) {
			parser = parser1;
			//model = model1;
		}
		else if (p == 2) {
			parser = parser2;
			//model = model2;
		}
		else {
			return;
		}
		
		model = parser.getSysMLModel();
		String typeId = null;
		
		//If the input unit exists, get the unit id
		//If the input unit doesn't exist, create a new datatype node and get a random id
		if (model.containsDataTypeName(input)) {
			DataType type = model.getDataTypeByName(input);
			typeId = type.getId();
		}
		else {
			
			//System.out.println("update invalid unit");
			
			Node newType = parser.getUmlDoc().createElement("packagedElement");
			Element newTypeEle = (Element)newType;
			typeId = parser.getRandomId();
			newTypeEle.setAttribute("xmi:type", "uml:DataType");
			newTypeEle.setAttribute("xmi:id", typeId);
			newTypeEle.setAttribute("name", input);
			parser.appendNodeToUmlModel(newType, typeId, true);
		}
		
		//SysMLDiag paramDiag = model.getParamDiag();
		//SysMLDiag blockDefDiag = model.getBlockDefDiag();
		
		UmlClass compInBlock = model.getUmlClassInBlockByName(comp1);
		//UmlClass compInParam = model.getUmlClassInParamByName(comp1);
		
//		if (compInParam == null) {
//			System.out.println("compInParam == null");
//		}
		
//		if (compInBlock == null) {
//			System.out.println("compInBlock == null");
//		}
		
		List<Property> varsInBlock = compInBlock.getPropertyByName(var1);
		String domainName = model.getParamDiag().getDomainClass().getName();
		boolean isDomainClass = compInBlock.getName().equals(domainName);
		
		if (varsInBlock == null) {
			throw new NullPointerException("Variable " + var1 + " doesn't exist in component " + compInBlock);
		}
		
		for (Property var: varsInBlock) {
			if (var.isInBlockDefDiag() && isDomainClass) {
				//If the var is in domain class (Connection variable)
				throw new NullPointerException("Domain connection variable doesn't have type");
			}
			else {
				Element propNodeInBlock = var.getElement();
				propNodeInBlock.setAttribute("type", typeId);
			}
		}
		
//		System.out.println(varInBlock.getId());
//		
//		//List<Property> varInBlock = compInBlock.getPropertyByName(var1);
//		
//		if (varInBlock == null) {
//			//If the property is in the domain block
//			Set<UmlClass> domainSet = model.getParamDiag().getAllClass();
//			
//			
//			if (domainSet.size() != 1) {
//				throw new IllegalArgumentException();
//			}
//			
//			Iterator<UmlClass> itDomain = domainSet.iterator();
//			UmlClass domain = itDomain.next();
//			varInBlock = domain.getPropertyByName(var1);
			//List<Property> propList = domain.getPropertyByName(var1);
			
//			if (varInBlock != null) {
//				//Property propInBlock = propList.get(0);
//				Element propNodeInBlock = varInBlock.getElement();
//				propNodeInBlock.setAttribute("type", typeId);
//			}
//			else {	
//				throw new NullPointerException();
//			}
//		}
		
		
//		Element varNodeInBlock = varInBlock.getElement();
//		varNodeInBlock.setAttribute("type", typeId);
		
		
		//If the parameter appears in parametric diagram
//		if (compInParam != null) {
//			
//			Property var2InParam = compInParam.getPropertyByName(var1);
//			
//			if (var2InParam == null) {
//				System.out.println("empty");
//			}
//			
//			Element var2NodeInParam = var2InParam.getElement();
//			var2NodeInParam.setAttribute("type", typeId);
//		}
	}
	
	
	private static void updateNewConn(String var1, String var2) {
		//XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		
		//SysMLModel model1 = parser1.getSysMLModel();
		SysMLModel model2 = parser2.getSysMLModel();
		
		
		Collection<Connector> conns = model2.getAllConnectors();
		Iterator<Connector> itConn = conns.iterator();
		Connector connP2 = null;
		
		//Go through all connectors to find 
		while(itConn.hasNext()) {
			Connector currConn = itConn.next();
			if (currConn.hasEnds(var1, var2)) {
				connP2 = currConn;
			}
		}
		
		if (connP2 == null) {
			throw new NullPointerException("Cannot find Connector with ends " + var1 + " and " + var2 + " in P2");
		}
		
		Node connNodeP2 = connP2.getNode();
		//Element connEleP2 = (Element)connNodeP2;
		//String connId = connP2.getId();
		List<ConnectorEnd> listEnds = connP2.getEnds();
		Node umlRootNode = parser2.getUmlRootNode();
		
		//Remove all related Blocks:NestedConnectorEnd in the uml file
		for (int i=0; i<listEnds.size(); i++) {
			ConnectorEnd endP2 = connP2.getEnd(i);
			
			Node nestedConnEndNodeP2 = endP2.getNestedConnEnd();
			if (nestedConnEndNodeP2 != null)
				umlRootNode.removeChild(nestedConnEndNodeP2);
		}
		
		//Remove the Blocks:BindingConnector node in the uml file
		Node bindNodeP2 = connP2.getBindNode();
		
		//System.out.println(connP2.getId());
		
		if (bindNodeP2 != null) {
			umlRootNode.removeChild(bindNodeP2);
		}
		
		
		
		
		//Remove the related notation nodes in the notation file
		Node edgeElementNode = connP2.getEdgeElement();
		Node edgeElementParent = edgeElementNode.getParentNode();
		edgeElementParent.removeChild(edgeElementNode);
		
//		Node edgeEObjectNode = connP2.getEdgeEObject();
//		if (edgeEObjectNode != null) {
//			Node edgeEObjectParent = edgeEObjectNode.getParentNode();
//			edgeEObjectParent.removeChild(edgeEObjectNode);
//		}
//		
//		
//		
//		Node childEObjectNode = connP2.getChildEObject();
//		
//		if (childEObjectNode != null) {
//			Node childEObjectParent = childEObjectNode.getParentNode();
//			childEObjectParent.removeChild(childEObjectNode);
//		}
		
		
		
		Node connParent = connNodeP2.getParentNode();
		connParent.removeChild(connNodeP2);
	}
	
	private static void updateDeletedConn(String var1, String var2, String comp1, String comp2) {
		//Get the infomation from ends
		//Note that one of the block is constraint block. It has two uml:Class nodes in uml file
		//Another block only has the node in block definition diagram
		
		XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		
		SysMLModel model1 = parser1.getSysMLModel();
		SysMLModel model2 = parser2.getSysMLModel();
		
		//SysMLDiag block1 = model1.getBlockDefDiag();
		//SysMLDiag block2 = model2.getBlockDefDiag();
		
		SysMLDiag param1 = model1.getParamDiag();
		SysMLDiag param2 = model2.getParamDiag();
		
		
		
		UmlClass compBlock1 = model1.getUmlClassInBlockByName(comp1);
		//UmlClass compParam1 = model1.getUmlClassInParamByName(comp1);
		
		UmlClass compBlock2 = model1.getUmlClassInBlockByName(comp2);
		//UmlClass compParam2 = model1.getUmlClassInParamByName(comp2);
		
		UmlClass domainParamClass1 = param1.getDomainClass();
		UmlClass domainParamClass2 = param2.getDomainClass();
		
		//Get the corresponding variables and components in P1
		//In P1, the connection between the two variables is missing
		UmlClass newCompBlock1 = model2.getUmlClassInBlockByName(comp1);
		//UmlClass newCompParam1 = model2.getUmlClassInParamByName(comp1);
		
		UmlClass newCompBlock2 = model2.getUmlClassInBlockByName(comp2);
		//UmlClass newCompParam2 = model2.getUmlClassInParamByName(comp2);
		
		Connector connP1 = null;
		Property end1P1 = null;
		Property end1P2 = null;
		Property end2P1 = null;
		Property end2P2 = null;
		
		Node endNode1P2 = null;	//The end nodes in P2
		Node endNode2P2 = null;
		//String blockVar = null;	//The variable name of block variable
		//boolean isVar1InDomain = false;
		
		Property domainEnd = null;
		Property compEnd = null;
				
		//If the property in comp1 of P1 isn't located in domain class
		if (var2.contains(".")) {
			
			end1P1 = compBlock1.getParamPropByName(var1);
			end2P1 = domainParamClass1.getParamPropByName(var2);
			end1P2 = newCompBlock1.getParamPropByName(var1);
			endNode1P2 = end1P2.getNode();
			end2P2 = domainParamClass2.getParamPropByName(var2);
			endNode2P2 = end2P2.getNode();
			
			domainEnd = end2P2;
			compEnd = end1P2;
			
			connP1 = end2P1.getConn();
		}
		//If end2 isn't in the domain class
		else if (var1.contains(".")) {
			
			end1P1 = domainParamClass1.getParamPropByName(var1);
			end2P1 = compBlock2.getBlockPropByName(var2);
			end1P2 = domainParamClass2.getBlockPropByName(var1);
			endNode1P2 = end1P2.getNode();
			end2P2 = newCompBlock2.getBlockPropByName(var2);
			endNode2P2 = end2P2.getNode();
			
			//endNode1P2 = domainParamClass2.getParamPropByName(var1).getNode();
			domainEnd = end1P2;
			compEnd = end2P2;
			
			connP1 = end1P1.getConn();
		}
		
		//Element connParentEleP1 = domainParamClass1.getElement();
		Node connNodeP1 = connP1.getNode();
		
		String connId = connP1.getId();
		Node connParentNodeP2 = domainParamClass2.getNode();
		
		Node importedConnNodeP2 = parser2.appendConnNodeToUmlClass(connNodeP1, connId, connParentNodeP2, endNode1P2, endNode2P2);
		Node portNodeP2 = getPortNode(importedConnNodeP2);
		
		//Add connector's notations in notation file
		Node importedNode = importConnNotations(connP1, domainParamClass2, parser2);
		updateTargetAndSource(importedNode, domainEnd, compEnd, domainParamClass2);
		
		//Note that one of the ends may doesn't have Blocks:NestedConnectorEnd node
		importNestedConnEndNode(connP1, portNodeP2, parser2, domainParamClass1, domainParamClass2);
	}
	
	private static Node getPortNode(Node connNode) {
		Element connEle = (Element)connNode;
		NodeList endList= connEle.getElementsByTagName("end");
		int nEnds = endList.getLength();
		
		if (nEnds != 2) {
			String connId = connEle.getAttribute("xmi:id");
			throw new IllegalArgumentException("The Connector " + connId + " has invalid ends in P2");
		}
		
		Element end1 = (Element)endList.item(0);
		//Element end2 = (Element)endList.item(1);
		
		return end1.hasAttribute("partWithPort")?endList.item(0):endList.item(1);
	}
	
	private static Node importConnNotations(Connector connP1, UmlClass parentClassP2, XMIParser parser2) {
		//Append the notation nodes of connector to the new notation file
		Node edgeEleNodeP1 = connP1.getEdgeElement();
		Element edgeEleP1 = (Element)edgeEleNodeP1;
				
		//System.out.println(edgeEleP1.getAttribute("xmi:id"));
		
//		Node edgeEObjNodeP1 = connP1.getEdgeEObject();
//		Element eEdgeEObjP1 = (Element)edgeEObjNodeP1;
//				
//				
//		//System.out.println(connId);
//		Node childEObjNodeP1 = connP1.getChildEObject();
//		Element eChildEObjP1 = (Element)childEObjNodeP1;

				
		//Get the shape of parent in notation file
//		Node parentShapeClassP2 = parentClassP2.getNotationShape().getShapeClass();
//		Element eParentShapeClassP2 = (Element)parentShapeClassP2;
//				
//		NodeList notationChildren = eParentShapeClassP2.getElementsByTagName("edges");
//		int nChildren = notationChildren.getLength();
//		Node parentChildEObjP2 = null;
//		//System.out.println(eParentShapeClassP2.getAttribute("xmi:id"));
//				
//		for (int i=0; i<nChildren; i++) {
//			Node child = notationChildren.item(i);
//			Element eChild = (Element)child;
//					
//			if (eChild.getAttribute("type").equals("Connector_Edge")) {
//				parentChildEObjP2 = child;
//			}
//		}
//				
//		if (parentChildEObjP2 == null) {
//			throw new NullPointerException();
//		}
		
//		if (edgeEleNodeP1 == null) {
//			System.out.println("edgeEleNodeP1 is null");
//		}
//		System.out.println("here");
				
		Node importedNode = parser2.appendNodeToParam(edgeEleNodeP1, edgeEleP1.getAttribute("xmi:id"), true);
		return importedNode;
		
		
		
		//parser2.appendNodeToParam(edgeEObjNodeP1, eEdgeEObjP1.getAttribute("xmi:id"), true);
		//parser2.appendNodeToNotationClass(childEObjNodeP1, eChildEObjP1.getAttribute("xmi:id"), parentChildEObjP2, true);		
	}
	
	private static void updateTargetAndSource(Node importedNode, Property domainEnd, Property compEnd, UmlClass paramDomain) {
		//Update target id and source id for the connection in the notation file
		
		//Get the notation id for domainEnd
		UmlClass domainClass = domainEnd.getParentClass();
		Node domainNode = domainClass.getNotationShape().getShapeClass();
		Node parentDomainNode = getPropNotation(domainEnd.getId(), domainNode, "uml:Property");
		String domainEndParentId = ((Element)parentDomainNode).getAttribute("xmi:id");
		
		//Get the notation id for compEnd
		Node compNode = paramDomain.getNotationShape().getShapeClass();
		Node parentCompNode = getPropNotation(compEnd.getId(), compNode, "uml:Port");
		String compEndParentId = ((Element)parentCompNode).getAttribute("xmi:id");
		
		Element importedEle = (Element) importedNode;
		importedEle.setAttribute("source", compEndParentId);
		importedEle.setAttribute("target", domainEndParentId);
	}
	
	private static Node getPropNotation(String propId, Node classNotation, String xmiType) {
		Element classEle = (Element)classNotation;
		//String classEleId = classEle.getId();
		//String parentId = null;
		
		NodeList eleList = classEle.getElementsByTagName("element");
		int nEleList = eleList.getLength();
		
		for (int i=0; i<nEleList; i++) {
			Node currNode = eleList.item(i);
			Element currEle = (Element)currNode;
			
			String type = currEle.getAttribute("xmi:type");
			
			if (type.equals(xmiType)) {
				String href = currEle.getAttribute("href");
				String hrefPropId = href.substring(10);
				
				if (hrefPropId.equals(propId)) {
					Node parentNode = currEle.getParentNode();
					return parentNode;
					//parentId = ((Element)childrenNode).getAttribute("xmi:id");
					//System.out.println(parentId);		
				}
			}
		}
		
		return null;
	}
	
	private static void importNestedConnEndNode(Connector connP1, Node importedPortEndP2, XMIParser parser2, UmlClass paramDomainClass1,
			UmlClass paramDomainClass2) {
		
		//Import the Blocks:NestedConnectorEnd in P1 to P2
		Node nestedConnEnd1P1 = connP1.getEnd(0).getNestedConnEnd();
		if (nestedConnEnd1P1 != null) {
			String nestedEndId1P1 = ((Element)nestedConnEnd1P1).getAttribute("xmi:id");		
			Node importedNestedEnd = parser2.appendNodeToUmlRoot(nestedConnEnd1P1, nestedEndId1P1, true);
			updateNestedConnEnd(connP1.getEnd(0), importedPortEndP2, importedNestedEnd, paramDomainClass1, paramDomainClass2);
		}
				
		Node nestedConnEnd2P1 = connP1.getEnd(1).getNestedConnEnd();
		if (nestedConnEnd2P1 != null) {
			String nestedEndId2P1 = ((Element)nestedConnEnd2P1).getAttribute("xmi:id");
			Node importedNestedEnd = parser2.appendNodeToUmlRoot(nestedConnEnd2P1, nestedEndId2P1, true);
			updateNestedConnEnd(connP1.getEnd(1), importedPortEndP2, importedNestedEnd, paramDomainClass1, paramDomainClass2);
		}
	}
	
	private static void updateNestedConnEnd(ConnectorEnd connEndP1, Node importedPortEndP2, Node impotedNestedConnEnd,
			UmlClass paramDomainClass1, UmlClass paramDomainClass2) {
		Element importedNestedEle = (Element) impotedNestedConnEnd;
		Element importedPortEleP2 = (Element) importedPortEndP2;
		String portIdP2 = importedPortEleP2.getAttribute("xmi:id");
		
		importedNestedEle.setAttribute("base_ConnectorEnd", portIdP2);
		importedNestedEle.setAttribute("base_Element", portIdP2);
		
		Node connEndP1Node = connEndP1.getNode();
		Element connEndP1Ele = (Element)connEndP1Node;
		String partWithPort = connEndP1Ele.getAttribute("partWithPort");
		
		Property partWithPortProp1 = paramDomainClass1.getProperty(partWithPort);
		
		if (partWithPortProp1 == null) {
			throw new NullPointerException("Property id " + partWithPort + " doesn't exist in Param Diag in P1");
		}
		
		String partWithPortName = partWithPortProp1.getName();
		Property partWithPortProp2 = paramDomainClass2.getBlockPropByName(partWithPortName);
		
//		Set<Property> propertySet = paramDomainClass2.getAllProperty();
//		System.out.println(propertySet.size());
//		for (Property p: propertySet) {
//			System.out.println(p.getName() + " " + p.isInBlockDefDiag());
//		}
		
		
		if (partWithPortProp2 == null) {
			throw new NullPointerException("Property name " + partWithPortName + " doesn't exist in Param Diag in P2");
		}
		
		importedNestedEle.setAttribute("propertyPath", partWithPortProp2.getId());
	}
	
	private static void updateDeletedVar(String comp1, String var1) {
		try {
			XMIParser parser1 = comparator.getParser(1);
			XMIParser parser2 = comparator.getParser(2);
			
			SysMLModel model1 = parser1.getSysMLModel();
			SysMLModel model2 = parser2.getSysMLModel();
			
			SysMLDiag block1 = model1.getBlockDefDiag();
			SysMLDiag block2 = model2.getBlockDefDiag();
			
			//String newBlockId = null;
			
			List<UmlClass> compList = model2.getUmlClassByName(comp1);
			
			if (compList == null) {
				throw new NullPointerException(comp1 + " doesn't exist in P1");
			}
			
			if (compList.size() == 1) {
				//The property only exists in block definition diagram
				
				//System.out.println("compList.size() == 1"); 
				addPropInBlockClass(block1, block2, var1, comp1, parser2, null);
			}
			else if (compList.size() == 2) {
				//If the property exists in both parametric diagram and block definition diagram
				//In parametric diagram, t he SysMLModel will only save the domain UmlClass
				//In block definition diagram, the SysMLModel will save all blocks
				
				//UmlClass classInBlock1 = block1.getClassByName(comp1);
				
				//Add selected property to block definition diagram in uml file
				addPropInBlockClass(block1, block2, var1, comp1, parser2, null);
				
				//Add selected property to parametric diagram in uml file
				//Note that the class in parametric diagram and block definition diagram has different ids
				addPropInParamClass(block1, block2, var1, comp1, parser2, compList, null);				
			}
			
			//Add selected property to notation file
			//The shape id is the same as class id in block definition diagram
			UmlClass compClass1 = block1.getClassByName(comp1);
			Node shapeClass1 = compClass1.getNotationShape().getShapeClass();
			String propId1 = compClass1.getBlockPropByName(var1).getId();
			Node propNotation1 = getPropNotation(propId1, shapeClass1, "uml:Property");
			
			
			UmlClass compClass2 = block2.getClassByName(comp1);
			Node shapeClass2 = compClass2.getNotationShape().getShapeClass();
			//String propId2 = compClass2.getPropertyByName(var1).getId();
			Element shapeClassEle = (Element)shapeClass2;
			Node paramParent = null;
			
			NodeList paramList = shapeClassEle.getElementsByTagName("children");
			int nParamList = paramList.getLength();
			
			for (int i=0; i<nParamList; i++) {
				Node paramNode = paramList.item(i);
				Element paramEle = (Element)paramNode;
				
				if (paramEle.getAttribute("type").equals("Parameters")) {
					paramParent = paramNode;
					break;
				}
			}
			
			//System.out.println("Before appendNodeToNotation");
			
			if (paramParent != null) {
				//System.out.println("appendNodeToNotation");
				parser2.appendNodeToNotationClass(propNotation1, null, paramParent, true);
			}
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static String addPropInParamClass(SysMLDiag block1, SysMLDiag block2, String var1, String comp1, XMIParser parser2, List<UmlClass> compList, String newId) {
		UmlClass classInBlock1 = block1.getClassByName(comp1);
		//Property propInBlock1 = classInBlock1.getPropertyByName(var1);
		//String propId1 = propInBlock1.getId();
		
		UmlClass classInParam1 = null;
		
		if (classInBlock1.equals(compList.get(0))) {
			classInParam1 = compList.get(1);
		}
		else {
			classInParam1 = compList.get(0);
		}

		UmlClass classInBlock2 = block2.getClassByName(comp1);
		UmlClass classInParam2 = null;
		
		if (classInBlock2.equals(compList.get(0))) {
			classInParam2 = compList.get(1);
		}
		else {
			classInParam2 = compList.get(0);
		}
		
		//System.out.println(classInParam2.getName());
		
		//Property paramProp1 = classInParam1.getPropertyByName(var1);
		Element classEleInParam1 = classInParam1.getElement();
		NodeList attrNodes = classEleInParam1.getElementsByTagName("ownedAttribute");
		int nAttrNodes = attrNodes.getLength();
		Node newNode = null;
		
		
		
		for (int i=0; i<nAttrNodes; i++) {
			Node attrNode = attrNodes.item(i);
			Element attrEle = (Element)attrNode;
			
			//If no newId is given, the newId is set to the id in P1
			if (newId == null) {
				newId = attrEle.getAttribute("xmi:id");
			}
			
			//System.out.println(newId);
			if (attrEle.getAttribute("name").equals(var1)) {
				newNode = attrNode;
				
				parser2.appendNodeToUmlClass(attrNode, newId, classInParam2.getNode(), true);
				break;
			}
		}
		
		//If the node doesn't exist in P1, return null
		return newNode == null ? null:((Element)newNode).getAttribute("xmi:id");
	}
	
	private static String addPropInBlockClass(SysMLDiag block1, SysMLDiag block2, String var1, String comp1, XMIParser parser2, String newId) {
		try {
			UmlClass compClass1 = block1.getClassByName(comp1);
			if (!compClass1.containsPropName(var1)) {
				throw new NullPointerException(var1 + " doesn't exist in " + comp1 + " of P1");
			}
		
			Property varProp1 = compClass1.getBlockPropByName(var1);
			Node varNode1 = varProp1.getNode();
			String varNodeId = varProp1.getId();
			//System.out.println(varNodeId);
			
			UmlClass compClass2 = block2.getClassByName(comp1); 
			if (compClass2 == null) {
				throw new NullPointerException(comp1 + " doesn't exist in uml file of P2");
			}
			
			if (newId == null) {
				newId = varNodeId;
			}
			
			Node compNode = compClass2.getNode();
			Node newNode = parser2.appendNodeToUmlClass(varNode1, newId, compNode, true);
			
			return ((Element)newNode).getAttribute("xmi:id");
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	

	private static void updateNewVar(String comp2, String var2) {		
			
		XMIParser parser2 = comparator.getParser(2);
		
		SysMLModel model2 = parser2.getSysMLModel();
		SysMLDiag block2 = model2.getBlockDefDiag();
		
		
		UmlClass compClass = block2.getClassByName(comp2);
		Property newVar = compClass.getBlockPropByName(var2);
		Node newVarNode = newVar.getNode();
		Element newVarEle = (Element)newVarNode;
		String newVarId = newVar.getId();
		
		
		if (!newVarEle.hasAttribute("association")) {
			NotationShape compShape = compClass.getNotationShape();
			Node compShapeClass = compShape.getShapeClass();
			
			Node propShapeNode = getPropNotation(newVarId, compShapeClass, "uml:Property");
			Node propShapeParent = propShapeNode.getParentNode();
			propShapeParent.removeChild(propShapeNode);
			
			compClass.removeProperty(newVarId);
			model2.removeProperty(newVarId);
		}
		else {
			// If connection attributes are considered as variables
			//Remove Constraints:ConstraintProperty in uml file
			if (model2.containsUmlConstProp(newVarId)) {
				Node endConstProp =  model2.getConstPropInUml(newVarId);
				Node endConstPropParent = endConstProp.getParentNode();
				endConstPropParent.removeChild(endConstProp);
			}
			
			//System.out.println();
			
			if (newVarEle.hasAttribute("association")) {
				try {
					
					//Remove the other end of association in uml file
					//String newVarId = newVarEle.getAttribute("xmi:id");
					String associatId = newVarEle.getAttribute("association");
					Node associatNode = model2.getAssociation(associatId);
					
					if (associatNode == null) {
						throw new NullPointerException("Association id " + associatId + " doesn't exist in P2");
					}
					
					String[] memberEnds = ((Element)associatNode).getAttribute("memberEnd").split(" ");
					String anotherEndId = (memberEnds[0].equals(newVarId) ? memberEnds[1]:memberEnds[0]);
					//System.out.println(anotherEndId);
					
					Property anotherEndProp = model2.getProperty(anotherEndId);
					//System.out.println(anotherEndId);
					
					if (anotherEndProp == null) {
						throw new NullPointerException("Property id " + anotherEndId + " in Association " + associatId + " doesn't exist in P2");
					}
					
					Node anotherEndNode = anotherEndProp.getNode();
					Node anotherEndParent = anotherEndNode.getParentNode();
					anotherEndParent.removeChild(anotherEndNode);
					model2.removeProperty(anotherEndId);
					
					//Remove Constraints:ConstraintProperty of another end in the uml:Association
					if (model2.containsUmlConstProp(anotherEndId)) {
						Node endConstProp =  model2.getConstPropInUml(anotherEndId);
						Node endConstPropParent = endConstProp.getParentNode();
						endConstPropParent.removeChild(endConstProp);
					}
					
					
					
					//Remove association node in uml file
					Node associatNodeParent = associatNode.getParentNode();
					associatNodeParent.removeChild(associatNode);
					model2.removeAssociation(associatId);
					
					//Remove Association in notation file
					Node edgeNotation = model2.getEdgeNotation(associatId);
					
					if (edgeNotation == null) {
						throw new NullPointerException("EdgeNotation id " + associatId + " doesn't exist in P2");
					}
					
					
					Node edgeNotationParent = edgeNotation.getParentNode();
					edgeNotationParent.removeChild(edgeNotation);
					model2.removeEdgeNotation(associatId);
				}
				catch (NullPointerException npe) {
					npe.printStackTrace();
					System.exit(-1);
				}					
			}
			
			//Remove the variable node in uml file
			Node newVarNodeParent = newVarNode.getParentNode();
			newVarNodeParent.removeChild(newVarNode);
			compClass.removePropertyByName(var2);
			model2.removeProperty(newVar.getId());
			
		}		
	}
	
//	private static Node getPropNotation(String propId, Node classNotation) {
//		NodeList propShapes = ((Element)classNotation).getElementsByTagName("children");
//		int nPropShapes = propShapes.getLength();
//		
//		for (int i=0; i<nPropShapes; i++) {
//			Node propShapeNode = propShapes.item(i);
//			Element propShapeEle = (Element)propShapeNode;
//			
//			//System.out.println(propShapeEle.getAttribute("xmi:id"));
//			
//			if (propShapeEle.getAttribute("type").equals("SysML::ConstraintBlock::Parameter_label")) {
//				
//				Node propNode = propShapeEle.getElementsByTagName("element").item(0);
//				String href = ((Element)propNode).getAttribute("href");
//				String id = href.substring(10);
//				if (id.equals(propId)) {
//					return propShapeNode;
//				}
//				
//				break;
//			}
//		}
//		return null;
//	}
	
	
	private static void updateDeletedBlock(String oldComp) {
		XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		
		SysMLModel model1 = parser1.getSysMLModel();
		SysMLModel model2 = parser2.getSysMLModel();
		
		SysMLDiag block1 = model1.getBlockDefDiag();
		SysMLDiag block2 = model2.getBlockDefDiag();
		
		UmlClass compClassInBlock1 = block1.getClassByName(oldComp);
		//UmlClass compClassInParam1 = param1.getClassByName(comp1);
		
		if (compClassInBlock1 == null) {
			throw new NullPointerException(oldComp + " cannot be found in Block Definition Diagram in P1");
		}
		
		//Node notation = model1.getNotation(compClassInBlock1.getId());
		//String compId = compClassInBlock1.getId();
		
		
		String oldCompId = compClassInBlock1.getId();
		String newCompId = oldCompId;
		
		//Add packageElement nodes to uml file
		Node blockNodeInUml = compClassInBlock1.getNode();
		parser2.appendNodeToUmlModel(blockNodeInUml, newCompId, false);	//Ignore the children of blocks
		
		Node blockNode = null;
		if (model1.containsUmlConstBlock(oldCompId)) {
			blockNode = parser2.getUmlDoc().createElement("Constraints:ConstraintBlock");
		}
		else if (model1.containsUmlBlock(oldCompId)) {
			blockNode = parser2.getUmlDoc().createElement("Blocks:Block");
		}
		
		
		//Add a Constraints:ConstraintBlock to indicates the type of previous node
		//Node constraintNode = parser2.getUmlDoc().createElement("Constraints:ConstraintBlock");
		Element blockElement = (Element) blockNode;
		String constraintId = parser2.getRandomId();
		blockElement.setAttribute("xmi:id", constraintId);
		blockElement.setAttribute("base_Class", newCompId);
		parser2.appendNodeToUmlRoot(blockNode, constraintId, true);
		
		//Add nodes to notation file (Component id is the same in both files)
		NotationShape oldShapeInBlock = block1.getShape(oldCompId);
		//System.out.println(oldCompId);
		
		if (oldShapeInBlock == null) {
			throw new NullPointerException("Block " + oldComp + " doesn't exist in the Block Definition Diagram in P1");
		}
		
//		NotationShape newShapeInBlock = oldShapeInBlock.clone();
//		if (!newCompId.equals(oldCompId)) {
//			newShapeInBlock.updateId(newCompId);
//		}
		
		Node oldShapeClass = oldShapeInBlock.getShapeClass();
		Node oldShapeEObject = oldShapeInBlock.getShapeEObject();
		Node oldNotationConn = oldShapeInBlock.getConnector();
		
//		Element oldShapeClassEle = (Element)oldShapeClass;
//		System.out.println(oldShapeClassEle.getAttribute("xmi:id"));
//		Element oldShapeEObjectEle = (Element)oldShapeEObject;
//		System.out.println(oldShapeEObjectEle.getAttribute("xmi:id"));
//		Element oldNotationConnEle = (Element)oldNotationConn;
//		System.out.println(oldNotationConnEle.getAttribute("xmi:id"));
		
		Node newShapeClass = parser2.appendNodeToBlock(oldShapeClass, ((Element)oldShapeClass).getAttribute("xmi:id"), true);
		Node newShapeEObject = parser2.appendNodeToBlock(oldShapeEObject, ((Element)oldShapeEObject).getAttribute("xmi:id"), true);
		Node newNotationConn = parser2.appendNodeToBlock(oldNotationConn, ((Element)oldNotationConn).getAttribute("xmi:id"), true);
		
		//System.out.println(((Element)newShapeClass).getAttribute("xmi:id"));
		
		NotationShape newShapeInBlock = new NotationShape(newShapeClass, newShapeEObject, newNotationConn, newCompId);
		block2.addShape(newShapeInBlock);
	}
	
	private static void updateNewBlock(String newComp) {
		//In uml file
		//1. find all elements and its properties and associations
		//2. For properties, delete them, if it has corresponding Constraints:ConstraintProperty element, also delete
		//3. For associations, find the association id and its corresponding element
		//4. Find another end of the association and delete it, then delete the association element
		//In notation file
		//1. Delete all elements in the NotationShape
		//2. delete the corresponding association element
		
		//XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		
		//SysMLModel model1 = parser1.getSysMLModel();
		SysMLModel model2 = parser2.getSysMLModel();
		
		//SysMLDiag block1 = model1.getBlockDefDiag();
		SysMLDiag block2 = model2.getBlockDefDiag();
		
		//UmlClass classInBlock1 = block1.getClassByName(comp1);
		UmlClass classInBlock2 = block2.getClassByName(newComp);
		
		
		
		Node node = classInBlock2.getNode();
		Element e = (Element)node;
		
		NodeList attrs = e.getElementsByTagName("ownedAttribute");
		int nAttrs = attrs.getLength();
		
		//Identify attributes and associations
		//Remove attributes
		//Find association elements and ends in associations, remove them
		for (int i=0; i<nAttrs; i++) {
			Node attr = attrs.item(i);
			Element eAttr = (Element)attr;
			if (eAttr.hasAttribute("association")) {
				try {
					//Remove association-related elements in uml file
					String associatId = eAttr.getAttribute("association");
					Node associat = model2.getAssociation(associatId);
					if (associat == null) {
						throw new NullPointerException("Association id " + associatId + " doesn't exist in P2");
					}
					
					
					Element eAssociat = (Element)associat;
					Node associatParent = associat.getParentNode();
					String memberEnds = eAssociat.getAttribute("memberEnd");
					
					
					//Get the other end of the association and remove it
					String xmiId = eAttr.getAttribute("xmi:id");
					String[] ends = memberEnds.split(" ");
					String anotherEnd = ends[0].equals(xmiId) ? ends[1] : ends[0];
					
					Property anotherEndProp = model2.getProperty(anotherEnd);
					Node anotherEndNode = anotherEndProp.getNode();
					Node anotherEndParent = anotherEndNode.getParentNode();
					
					if (model2.containsUmlConstProp(anotherEnd)) {
						//If the another end of the association contains Constraints:ConstraintProperty
						//System.out.println(anotherEnd);
						Node anotherEndConstProp = model2.getConstPropInUml(anotherEnd);
						//System.out.println(((Element)anotherEndConstProp).getAttribute("xmi:id"));
						Node anotherEndConstPropParent = anotherEndConstProp.getParentNode();
						anotherEndConstPropParent.removeChild(anotherEndConstProp);
					}
					
					anotherEndParent.removeChild(anotherEndNode);
					
					associatParent.removeChild(associat);
					node.removeChild(attr);
					
					//Remove association related elements(edges) in notation file
					//System.out.println(associatId);
					Node edgeNotation = model2.getEdgeNotation(associatId);
					if (edgeNotation == null) {
						throw new NullPointerException("EdgeNotation id " + associatId + " doesn't exist in P2");
					}
					//System.out.println(associatId);
					
					Node edgeNotationParent = edgeNotation.getParentNode();
					edgeNotationParent.removeChild(edgeNotation);
				}
				catch (NullPointerException npe) {
					npe.printStackTrace();
					System.exit(-1);
				}
			}
			else if (eAttr.hasAttribute("aggregation")) {
				String xmiId = eAttr.getAttribute("xmi:id");
				Node baseClass = model2.getConstPropInUml(xmiId);
				if (baseClass != null) {
					
					Node baseClassParent = baseClass.getParentNode();
					baseClassParent.removeChild(baseClass);
				}
				
				node.removeChild(eAttr);		
			}
		}
		
		
		
		//Remove notation nodes of blocks
		Node rtNotation = parser2.getNotationRootBlock();
		NotationShape shape = classInBlock2.getNotationShape();
		rtNotation.removeChild(shape.getShapeClass());
		rtNotation.removeChild(shape.getShapeEObject());
		rtNotation.removeChild(shape.getConnector());
		
		
		//Remove the Blocks:Block node or Constraints:ConstraintProperty node
		Node rtUml = parser2.getUmlRootNode();
		try {
			Node blockNode = classInBlock2.getBlockNode();
			if (blockNode != null) {
				rtUml.removeChild(blockNode);
			}
			else {
				throw new NullPointerException("Node " + classInBlock2.getName() + " doesn't have corresponding block node in uml file");
			}
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			System.exit(-1);
		}
		
		
		//Remove the element itself in uml file
		Node compParent = node.getParentNode();
		compParent.removeChild(node);
	}
	
	private static void updateInconsistentUnits(int p, String unit1, String unit2, 
			String var1, String var2, String comp1, String comp2) {
		XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		//SysMLModel model1 = parser1.getSysMLModel();
		//SysMLModel model2 = parser2.getSysMLModel();
		
		SysMLModel model = null;
		SysMLDiag param = null;		
		//Set<Connector> resConnectors = null;
		if (p == P1) {					
			//resConnectors = parser1.getConnectors();
			model = parser1.getSysMLModel();
			param = model.getParamDiag();
		}
		else if (p == P2) {
			//resConnectors = parser2.getConnectors();
			model = parser2.getSysMLModel();
			param = model.getParamDiag();
		}
		else {
			throw new IllegalArgumentException("Inconsistent Unit issue only exist in single product, the index should be 1 or 2");
		}
		
		//Properties both in parametric diagram and block definition diagram are saved in the block umlclass
		//UmlClass blockClass1 = model.getUmlClassInBlockByName(comp1);
		UmlClass blockClass2 = model.getUmlClassInBlockByName(comp2);
		
		//Find the location of variables
		//Property prop1 = blockClass1.getParamPropByName(var1);
		Property prop2 = blockClass2.getParamPropByName(var2);
		
		DataType dataType1 = model.getDataTypeByName(unit1);
		//DataType dataType2 = model.getDataTypeByName(unit2);
		
		//If p2 variable isn't in the domain; p1 is in the domain
		if(prop2 != null) {
			Property blockProp2 = blockClass2.getBlockPropByName(var2);
			
			Element propEle2 = prop2.getElement();
			propEle2.setAttribute("type", dataType1.getId());
			
			Element blockPropEle2 = blockProp2.getElement();
			blockPropEle2.setAttribute("type", dataType1.getId());
		}
		
		//If p2 variable is in the domain; p1 isn't
		if (prop2 == null) {
			UmlClass paramDomain = param.getDomainClass();
			
			Property paramProp2 = paramDomain.getParamPropByName(var2);
			
			Element paramPropEle2 = paramProp2.getElement();
			paramPropEle2.setAttribute("type", dataType1.getId());
		}		
		
	}
	
	private static void updateChangedUnit(String comp1, String comp2, String targetVar, String targetUnit) {
		//targetVar: the variable name that needs to update unit in P2
		//targetUnit: the unit name that needs to be put on targetVar
		
		XMIParser parser1 = comparator.getParser(1);
		XMIParser parser2 = comparator.getParser(2);
		
		SysMLModel model1 = parser1.getSysMLModel();
		SysMLModel model2 = parser2.getSysMLModel();
		
		SysMLDiag block1 = model1.getBlockDefDiag();
		SysMLDiag block2 = model2.getBlockDefDiag();
		
		
		
		UmlClass classInBlock1 = block1.getClassByName(comp1);
		UmlClass classInBlock2 = block2.getClassByName(comp2);
		
		
		if (classInBlock1 == null) {
			throw new NullPointerException(comp1 + " cannot be found in Block Definition Diagram of P1");
		}
		
		if (classInBlock2 == null) {
			throw new NullPointerException(comp2 + " cannot be found in Block Definition Diagram of P2");
		}
		
		
		
		
		DataType typeInModel1 = model1.getDataTypeByName(targetUnit);
		DataType typeInModel2 = model2.getDataTypeByName(targetUnit);
		Node dataNode1 = typeInModel1.getNode();
		String newId = "";
		Node newNode = null;
		if (typeInModel2 == null) {
			
			//If unit1 doesn't exists in P2, create a new DataType with new Id
			newNode = parser2.appendNodeToUmlModel(dataNode1, null, true);
			newId = ((Element)newNode).getAttribute("xmi:id");
		}
		else {
			//If unit1 exists in P2, use the existing id
			newId = typeInModel2.getId();
		}
		
		//Change the type of all properties with name var1
		model2.setPropertyAttrByName(comp2, targetVar, "type", newId);
		
		
	}
	
	
	private static ChangeTable getTable(int p) {
		if (p == P1) return tableP1;
		else if (p == P2) return tableP2;
		else return tableBtw;
	}
	
	private static void initTables() {

		Color lightBlue = new Color(218, 227, 243);
		Color lightYellow = new Color(255, 242, 204);
		
		tableP1 = new ChangeTable(graphicPanel, P1);
		tableP1.addTableData(comparator.getResults(1));
		//System.out.println(comparator.getResults(1).size());
		tableP1.setBackground(lightBlue);
		
	    tableP2 = new ChangeTable(graphicPanel, P2);
	    tableP2.addTableData(comparator.getResults(2));
		tableP2.setBackground(lightYellow);
		
		tableBtw = new ChangeTable(graphicPanel, BTW) {

			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component comp = super.prepareRenderer(renderer, row, column);

            	if(column == 1 || column == 2 || column == 3) {
                	comp.setBackground(lightBlue);
            	}
            	else if(column == 4 || column == 5 || column == 6) {
                	comp.setBackground(lightYellow);
            	}
            	else {
            		comp.setBackground(Color.white);
            	}

            	return comp;
			}
			
		};
		
		tableBtw.addTableData(comparator.getResults(3));
	}
	
	
	
	
	public static void main(String args[]) {

		ModelModifier f = new ModelModifier();

		f.setTitle("XMI Parser");
	    f.setSize(WIDTH_OF_WINDOW, HEIGHT_OF_WINDOW);
	    f.setVisible(true);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    f.setLocationRelativeTo(null);             //Center the window
		
	    //System.out.println( "\u2103");

	}
	
	
}


