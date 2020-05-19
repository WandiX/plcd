//import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument; 
import com.itextpdf.kernel.pdf.PdfWriter; 
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.kernel.color.Color; 


public class ReportTemplate {
	
	/*
	 * Store the information in the generated report
	 */
	
	private final static float [] pointColumnWidths = {20F, 70F, 70F, 70F, 70F, 70F, 70F, 70F, 70F};
	public final static int IDENTI_REPORT = 0;
	public final static int UPDATE_REPORT = 1;
	private final static String[] IDENTI_TITLES = {"Identification Report", "Identified Issues in P1", "Identified Issues in P2", "Identified issues between P1 and P2"};
	private final static String[] UPDATE_TITLES = {"Update Report", "Fixed Issues in P1", "Fixed Issues in P2", "Fixed issues between P1 and P2"};
	
	ReportTemplate(List<String[]> selectedP1, List<String[]> selectedP2, List<String[]> selectedBtw, int reportNum) {
		// Creating a PdfWriter
		String[] titles = (reportNum == IDENTI_REPORT) ? IDENTI_TITLES: UPDATE_TITLES;
		
		try {
			String dest = "report/" + titles[0] + ".pdf";       
		    PdfWriter writer = new PdfWriter(dest);
			
			// Creating a PdfDocument       
		    PdfDocument pdfDoc = new PdfDocument(writer);              
		   
		    // Adding a new page 
		   // pdfDoc.addNewPage();               
		   
		    // Creating a Document        
		    Document doc = new Document(pdfDoc);
		    
		    Paragraph title = new Paragraph(titles[0]);
		    title.setBold();
		    title.setTextAlignment(TextAlignment.CENTER);
		    
		    Paragraph p1 = new Paragraph(titles[1]);
		    Table tableP1 = addTableContent(selectedP1);
		    
		    Paragraph p2 = new Paragraph(titles[2]);
		    Table tableP2 = addTableContent(selectedP2);
		    
		    Paragraph btw = new Paragraph(titles[3]);
		    Table tableBtw = addTableContent(selectedBtw);
		    
		    Date date = new Date();
		    Paragraph time = new Paragraph(date.toString());
		    time.setTextAlignment(TextAlignment.RIGHT);
		         
		    // Adding Table to document
		    doc.add(title);
		    doc.add(p1);
		    doc.add(tableP1);
		    doc.add(p2);
		    doc.add(tableP2);
		    doc.add(btw);
		    doc.add(tableBtw);
		    doc.add(time);
		   
		    // Closing the document    
		    doc.close();              
		    //System.out.println("PDF Created");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	   
	    
	}
	
	
	private Table addTableContent(List<String[]> content) {
		Table table = new Table(pointColumnWidths);
		table.setFontSize(8);
		table.setWidthPercent(100);
		table.setBackgroundColor(Color.LIGHT_GRAY);
	    
	    // Adding cells to the table
	    table.addCell(""); 
	    table.addCell("Unit1");       
	    table.addCell("Variable1");       
	    table.addCell("Component1");       
	    table.addCell("Unit2");       
	    table.addCell("Variable2");       
	    table.addCell("Component2");
	    table.addCell("Problem");       
	    table.addCell("Criticality");
	    
	    int counter = 1;
	    for (String[] row: content) {
	    	table.addCell(Integer.toString(counter));
	    	for (int i=1; i<row.length - 1; i++) {
	    		table.addCell(row[i]);
	    	}
	    	counter++;
	    }
	    
	    
	    return table;
	}
}
