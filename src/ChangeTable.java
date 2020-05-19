import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ChangeTable extends JTable {
	
	/*
	 * Store the info in the change table of the interface
	 */

	private static final long serialVersionUID = 1L;
	
	private GraphicPanel graphicPanel;
	
	private TreeSet<Integer> selectedNum;
	private int id = 0;
	final static int COLUMN_NUM = 10;
	private int invalidUnit = 0;	//Indicates if the issues of selected rows have "Invalid Unit" 
	
	
	ChangeTable() {
		selectedNum = new TreeSet<>();
	}
	
	ChangeTable(GraphicPanel panel, int i) {
		selectedNum = new TreeSet<>();
		graphicPanel = panel;
		id = i;
	}
	
	public boolean selectedInvalidUnit() {
		return invalidUnit > 0;
	}
	
	public void setId(int i) {
		id = i;
	}
	
	public Set<Integer> getSelectedRowsNum() {
		return selectedNum;
	}
	
	public List<String[]> getSelectedRowsContent() {
	
		List<String[]> selectedRows = new ArrayList<>();
		
		for (Integer curr: selectedNum) {

			selectedRows.add(getRowValues(curr));
		}
		
		return selectedRows;
	}
	
	public List<String[]> getAllRowsContent() {
		List<String[]> rows = new ArrayList<>();
		DefaultTableModel model = (DefaultTableModel) getModel(); 
		int rowNum = model.getRowCount();
		
		for (int i=0; i<rowNum; i++) {
			rows.add(getRowValues(i));
		}
		
		return rows;
	}
	
	public String[] getRowValues(int r) {
		DefaultTableModel model = (DefaultTableModel) getModel(); 

		String[] values = new String[COLUMN_NUM];
		for (int i=0; i<COLUMN_NUM; i++) {
			values[i] = model.getValueAt(r, i).toString();
		}
		
		return values;
	}
	
	@Override
    public Class<?> getColumnClass(int columnIndex) {
		Class<?> clazz = String.class;
		switch (columnIndex) {
			case 0:
				clazz = Integer.class;
				break;
      		case 9:	//Set the 10th col to be checkbox
      			clazz = Boolean.class;
      			break;
      		default:
      			clazz = String.class;
		}
		return clazz;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      return column == 9;
    }
	
    public void clearSelected() {
    	selectedNum.clear();
    }

	
	public void SetGraphicPanel(GraphicPanel panel) {
		graphicPanel = panel;
	}
	
	public int getSelectedNum() {
		return selectedNum.size();
	}
	
	public void removeSelectedRows() {
		DefaultTableModel model = (DefaultTableModel) getModel(); 
		//Iterator<Integer> itNum = selectedNum.iterator();
		
		while(selectedNum.size() > 0) {
			Integer currNum = selectedNum.pollLast();	//Start from the last element
			model.removeRow(currNum);
		}
		selectedNum.clear();
	}
	
	public void addTableData(List<String[]> results) {
		
		DefaultTableModel model = (DefaultTableModel) getModel(); 
		if (model.getColumnCount() != 0)
			return;

		setRowSelectionAllowed(false);
		String[] columnNames = {"ID", "Unit1", "Variable1", "Components1", "Unit2", "Variable2", "Components2", "Problem", "Criticality", ""};
		
		for (String name: columnNames) {
			model.addColumn(name);
		}
		
		for (String[] res: results) {
			Object[] row = new Object[10];
			System.arraycopy(res, 0, row, 0, res.length);
			row[9] = false;
			model.addRow(row);
		}
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(java.awt.event.MouseEvent evt) {
		    	int row = rowAtPoint(evt.getPoint());
		    	int col = columnAtPoint(evt.getPoint());
		    	if (row >= 0 && col >= 0) {
		    		String[] content = new String[XMIComparator.NUMBER_OF_COLUMNS];
		    		for (int i=0; i<XMIComparator.NUMBER_OF_COLUMNS; i++) {
		    			Object obj = getModel().getValueAt(row, i);
		    			
		    			content[i] = (obj == null ? "": obj.toString());
		    		}
		    		
		    		if (graphicPanel != null)
		    			graphicPanel.setText(content, id);

		    		if (col == 9) {
		    			
		    			//The boolean value before user clicks
		    			boolean preSelected = (boolean)getModel().getValueAt(row, 9);

		    			if (preSelected) {
		    				selectedNum.remove(new Integer(row));
		    				
		    				if (model.getValueAt(row, 7).equals(XMIComparator.issues[0])) {
		    					invalidUnit--;
		    				}
		    			}
		    			else  {
		    				selectedNum.add(row); 
		    				
		    				//If selected issue is "Invalid Unit"
		    				if (model.getValueAt(row, 7).equals(XMIComparator.issues[0])) {
		    					invalidUnit++;
		    				}
		    			}
		    		}
		    	}
			}
		});
		
	}
}
