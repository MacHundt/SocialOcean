package impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.mxgraph.model.mxCell;
import com.mxgraph.shape.mxIShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import utils.FilesUtil;

public class MyGraphComponent extends mxGraphComponent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 9050639895615440170L;


	public MyGraphComponent(mxGraph g) {
        super(g);
    }

    public mxInteractiveCanvas createCanvas()
    {
        return new MyInteractiveCanvas(this);           
    }
    
    
    public class MyInteractiveCanvas extends mxInteractiveCanvas {
    	
    	protected CellRendererPane rendererPane = new CellRendererPane();

    		protected JLabel vertexRenderer = new JLabel();

		protected mxGraphComponent graphComponent;
		
		private BufferedImage img = FilesUtil.readIconFile("icons/user.png");
		private BufferedImage male = FilesUtil.readIconFile("icons/user_male32.png");
		private BufferedImage female = FilesUtil.readIconFile("icons/user_female32.png");
		
    	
	    public MyInteractiveCanvas(MyGraphComponent myGraphComponent) {
	        super(myGraphComponent);
	        
	        this.graphComponent = myGraphComponent;
	        
	        vertexRenderer.setBorder(
					BorderFactory.createBevelBorder(BevelBorder.RAISED));
			vertexRenderer.setHorizontalAlignment(JLabel.CENTER);
			vertexRenderer
					.setBackground(graphComponent.getBackground().darker());
			vertexRenderer.setOpaque(true);
	    }
	    
	    
	    public void drawVertex(mxCellState state, String label)
		{
			vertexRenderer.setText(label);
			// TODO: Configure other properties...

			rendererPane.paintComponent(g, vertexRenderer, graphComponent,
					(int) (state.getX() + translate.getX()),
					(int) (state.getY() + translate.getY()),
					(int) state.getWidth(), (int) state.getHeight(), true);
		}

	    /*
	     * (non-Javadoc)
	     * @see com.mxgraph.canvas.mxICanvas#drawCell()
	     */
	    public Object drawCell(mxCellState state)
	    {
	        Map<String, Object> style = state.getStyle();
	        mxIShape shape = getShape(style);

	        if (g != null && shape != null)
	        {
	            // Creates a temporary graphics instance for drawing this shape
	            float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY,
	                    100);
	            Graphics2D previousGraphics = g;
	            g = createTemporaryGraphics(style, opacity, state);

	            // Paints the shape and restores the graphics object
	            shape.paintShape(this, state);

	            if(((mxCell)state.getCell()).isVertex()) { 
	            		int x = (int)(state.getCenterX() - state.getWidth() / 2);
	            		int y = (int)(state.getCenterY()- state.getHeight() / 2) - 15;
	            	
					mxCell o = (mxCell) state.getCell();
					if (o.getValue() instanceof MyUser) {
						MyUser user = (MyUser) o.getValue();
						if (user.getGender().equals("male") || user.getGender().equals("mostly_male")) {
							previousGraphics.drawImage(male, x, y, null);
						}
						else if (user.getGender().equals("female") || user.getGender().equals("mostly_female")) {
							previousGraphics.drawImage(female, x, y, null);
						}
						else
							previousGraphics.drawImage(img, x, y, null);
//	            	int x = (int)(state.getCenterX());
//		            int y = (int)(state.getCenterY());
//	                Image img = Toolkit.getDefaultToolkit().getImage("");
//	                BufferedImage img = FilesUtil.readIconFile("icons/pos_32.png");
//	                BufferedImage img = FilesUtil.readIconFile("icons/user.png");
						
					} else 
						previousGraphics.drawImage(img, x, y, null);

	            }

	            g.dispose();
	            g = previousGraphics;
	        }

	        return shape;
	    }
	}
}
