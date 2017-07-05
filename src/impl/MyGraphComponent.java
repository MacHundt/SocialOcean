package impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

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
	    public MyInteractiveCanvas(MyGraphComponent myGraphComponent) {
	        super(myGraphComponent);
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
	                int y = (int)(state.getCenterY()- state.getHeight() / 2);
//	            	int x = (int)(state.getCenterX());
//		            int y = (int)(state.getCenterY());
//	                Image img = Toolkit.getDefaultToolkit().getImage("");
//	                BufferedImage img = FilesUtil.readIconFile("icons/pos_32.png");
	                BufferedImage img = FilesUtil.readIconFile("icons/user.png");
	                previousGraphics.drawImage(img, x, y, null);
	            }

	            g.dispose();
	            g = previousGraphics;
	        }

	        return shape;
	    }
	}
}
