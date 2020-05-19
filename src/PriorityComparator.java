import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PriorityComparator implements Comparator<String[]> {
	final static Map<String, Integer> priorityNumMap = createMap();
	final static int PRIORITY_COL = 8;
	
	
	
    private static Map<String, Integer> createMap()
    {
    	//To make sure the most critical issues appear first in accending order, assign lowest score to the most critical level
        Map<String, Integer> map = new HashMap<>();
        map.put("Catastrophic", 1);
        map.put("Critical", 2);
        map.put("Marginal", 3);
        map.put("Negligible", 4);
        map.put("", 5);
        
        return map;
    }

	@Override
	public int compare(String[] row1, String[] row2) {
		int col = XMIComparator.NUMBER_OF_COLUMNS;
		if (row1.length != col) {
			throw new IllegalArgumentException();
		}
		
		//System.out.println(row1[7]);
		
		
		//System.out.println(row1[PRIORITY_COL] + " " + row2[PRIORITY_COL]);
		
		int prNum1 = priorityNumMap.get(row1[PRIORITY_COL]);
		int prNum2 = priorityNumMap.get(row2[PRIORITY_COL]);
		
		//System.out.println(prNum1 + " " + prNum2);
		
		return Integer.compare(prNum1, prNum2);
	}

}
