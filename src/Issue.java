
public class Issue {
	
	/*
	 * Store found issue (The row info in ChangeTable)
	 */
	
	private String unit1, unit2, var1, var2, comp1, comp2;
	//private String problem, priority;
	
	Issue(String u1, String v1, String c1, String u2, String v2, String c2) {
		unit1 = u1;
		unit2 = u2;
		var1 = v1;
		var2 = v2;
		comp1 = c1;
		comp2 = c2;
		
	}
	
	Issue(String u1, String v1, String c1, String u2, String v2, String c2, String pro, String pri) {
		this(u1, v1, c1, u2, v2, c2);
		//problem = pro;
		//priority = pri;
	}
	
	
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Issue))return false;
	    
	    Issue i = (Issue) other;
	    
	    return i.hashCode() == this.hashCode();
	}
	
	@Override
	public int hashCode() {
		return unit1.hashCode() + unit2.hashCode() + var1.hashCode() + var2.hashCode() + comp1.hashCode() +
				comp2.hashCode();
	}
}
