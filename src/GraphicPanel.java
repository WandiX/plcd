import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GraphicPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String[] text;
	String[] Dimensions;
	int product = 0;
	final static int P1 = 1;
	final static int P2 = 2;
	final static int BTW = 3;
	final static int INIT = 0;

    GraphicPanel() {

        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.white);
        

//        addMouseListener(new MouseAdapter() {
//            public void mousePressed(MouseEvent e) {            	
                //moveSquare(e.getX(),e.getY());
//            }
//        });
//
//        addMouseMotionListener(new MouseAdapter() {
//            public void mouseDragged(MouseEvent e) {
//                moveSquare(e.getX(),e.getY());
//            }
//        });
        
    }
    
    //Set the current row content
    public void setText(String[] content, int p) {
    	//text = Arrays.copyOf(content, content.length);
    	text = content;
    	product = p;	//Set the current product
    	
    	repaint();
    }
    

    
    protected void paintComponent(Graphics g) {             
        
        super.paintComponent(g);
        g.setColor(Color.black);
	    g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        
        
        if (text == null) {
        	//Initialize the graphicPanel with descriptions
        	g.drawString("Click table cells to preview", 10, 30);
        	return;
        }
        
        
        
        String imgPath = findBackgroundPath();
        
	    if (imgPath == null) {
	    	throw new NullPointerException();
	    }
         
        
        File file = new File(imgPath);  
        Image image;
  		try {
  			image = ImageIO.read(file);
  			int height = image.getHeight(null);  
  	        int width = image.getWidth(null);  	        
  	         
        	g.drawImage(image, 0, 0, width, height, null);
        	
        	if (product == P1) {
        		g.drawString("P1", 100, 150);
        	}
        	else if (product == P2) {
        		g.drawString("P2", 100, 150);
        	}
        	else {
        		g.drawString("P1", 100, 150);
            	g.drawString("P2", 100, 440);
        	}
        	
    		//Add text to the original image
        	if (text[7].equals(XMIComparator.issues[0])) {	

        		//If the selected issue is "Invalid Unit"        		
        		g.drawString(text[2] + ":" + text[1], 330, 260);  	        	
  	        }
        	else if (text[7].equals(XMIComparator.issues[1])) {
        		//If the selected issue is "New Variable"
        		g.drawString(text[6], 250, 115);
        		g.drawString(text[6], 250, 440);
        		g.drawString(text[5], 450, 515);
        	}
        	else if (text[7].equals(XMIComparator.issues[2])) {
        		//If the selected issue is "New Block"
        		g.drawString(text[6], 250, 600);
        	}
        	else if (text[7].equals(XMIComparator.issues[3])) {
        		//"Deleted Variable"
        		g.drawString(text[3], 250, 115);
        		g.drawString(text[3], 250, 440);
        		g.drawString(text[2], 450, 190);
        	}
        	else if (text[7].equals(XMIComparator.issues[4])) {
        		//"Deleted Block"
        		g.drawString(text[3], 250, 110);
        	}
        	else if (text[7].equals(XMIComparator.issues[5])) {
        		//"Inconsistent Units"
        		g.drawString(text[3], 270, 105);
        		g.drawString(text[2] + ":" + text[1], 360, 260);
        		g.drawString(text[6], 270, 580);
        		g.drawString(text[5] + ":" + text[4], 360, 430);
        	}
        	else if (text[7].equals(XMIComparator.issues[6])) {
        		//"Changed Unit"
        		g.drawString(text[3], 250, 110);
        		g.drawString(text[2] + ":" + text[1], 450, 200);
        		g.drawString(text[6], 250, 600);
        		g.drawString(text[5] + ":" + text[4], 450, 530);
        	}
        	else if (text[7].equals(XMIComparator.issues[7])) {
        		//"New Connection"
        		g.drawString(text[3], 100, 170);
        		g.drawString(text[2], 330, 240);
        		g.drawString(text[6], 420, 320);
        		g.drawString(text[5], 400, 260);
        		
        		g.drawString(text[3], 100, 490);
        		g.drawString(text[2], 330, 560);
        		g.drawString(text[6], 420, 650);
        		g.drawString(text[5], 400, 580);
        	}
        	else if (text[7].equals(XMIComparator.issues[8])) {
        		//"Deleted Connection"
        		g.drawString(text[3], 100, 170);
        		g.drawString(text[2], 330, 240);
        		g.drawString(text[6], 420, 320);
        		g.drawString(text[5], 400, 260);
        		
        		g.drawString(text[3], 100, 490);
        		g.drawString(text[2], 330, 560);
        		g.drawString(text[6], 420, 650);
        		g.drawString(text[5], 400, 580);
        	}
  		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
  		}	
        
    }
    
    private String findBackgroundPath() {
    	String imgPath = null;
    	
    	//Set the path of background picture according to the selected issue
    	if (text[7].equals(XMIComparator.issues[0])) {
			//If the selected issue is "Invalid Unit"
			if (product == P1) {
				imgPath = "img/InvalidUnitP1.png";
			}
			else if (product == P2) {
				imgPath = "img/InvalidUnitP2.png";
			}
		}
        else if (text[7].equals(XMIComparator.issues[1])) {
        	//"New Variable"
        	imgPath = "img/NewVariable.png";
        }
        else if (text[7].equals(XMIComparator.issues[2])) {
        	//"New Block"
			imgPath = "img/NewBlock.png";
        }
        else if (text[7].equals(XMIComparator.issues[3])) {
        	//"Deleted Variable"
        	imgPath = "img/DeletedVariable.png";
        }
        else if (text[7].equals(XMIComparator.issues[4])) {
        	//"Deleted Block"
        	imgPath = "img/DeletedBlock.png";
        }
        else if (text[7].equals(XMIComparator.issues[5])) {
        	//"Inconsistent Units"
        	if (product == P1) {
        		imgPath = "img/InconsistentUnitsP1.png";
        	}
        	else if (product == P2) {
        		imgPath = "img/InconsistentUnitsP2.png";
        	}
        }
        else if (text[7].equals(XMIComparator.issues[6])) {
        	//"Changed Unit"
        	imgPath = "img/ChangedUnits.png";
        }
		else if (text[7].equals(XMIComparator.issues[7])) {
			//If the selected issue is "New Connection"
			imgPath = "img/NewConnection.png";
		}
		else if (text[7].equals(XMIComparator.issues[8])) {
			//If the selected issue is "Deleted Connection"
			imgPath = "img/DeletedConnection.png";
		}
    	
    	return imgPath;
    }
    
}
