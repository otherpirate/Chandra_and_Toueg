package projects.Chandra_and_Toueg.nodes.nodeImplementations;


import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.Method;
import java.util.TreeSet;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.EPSOutputPrintStream;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;


/**
 * The class to simulate the sample2-project.
 */
public class CTNode extends Node implements Comparable<CTNode> {

	private static int maxNeighbors = 0; // global field containing the max number of neighbors any node ever had
	
	private boolean isMaxNode = false; // flag set to true when this node has most neighbors
	private boolean drawAsNeighbor = false; // flag set by a neighbor to color specially
	
	// The set of nodes this node has already seen
	private TreeSet<CTNode> neighbors = new TreeSet<CTNode>();
	
	/**
	 * Reset the list of neighbors of this node.
	 */
	public void reset() {
		neighbors.clear();
	}
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
	}

	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
		for(Edge e : this.outgoingConnections){
			neighbors.add((CTNode) e.endNode); // only adds really new neighbors
		}
	}

	@Override
	public void preStep() {
		// color this node specially when it has most neighbors
		if(this.neighbors.size() >= CTNode.maxNeighbors) {
			CTNode.maxNeighbors = this.neighbors.size();
			this.isMaxNode = true;
		} else {
			this.isMaxNode = false;
		}
	}

	@Override
	public void postStep() {
	}
	
	private static boolean isColored = false;
	
	/**
	 * Colors all the nodes that this node has seen once.
	 */
	@NodePopupMethod(menuText="Color Neighbors")
	public void ColorNeighbors(){
		for(CTNode n : neighbors) {
			n.drawAsNeighbor = true;
		}
		isColored = true;
		// redraw the GUI to show the neighborhood immediately
		if(Tools.isSimulationInGuiMode()) {
			Tools.repaintGUI();
		}
	}
	
	/**
	 * Resets the color of all previously colored nodes.
	 */
	@NodePopupMethod(menuText="Undo Coloring")
	public void UndoColoring() { // NOTE: Do not change method name!
		// undo the coloring for all nodes
		for(Node n : Runtime.nodes){
			((CTNode) n).drawAsNeighbor = false;
		}
		isColored = false;
		// redraw the GUI to show the neighborhood immediately
		if(Tools.isSimulationInGuiMode()) {
			Tools.repaintGUI();
		}
	}

	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#includeMethodInPopupMenu(java.lang.reflect.Method, java.lang.String)
	 */
	public String includeMethodInPopupMenu(Method m, String defaultText) {
		if(!isColored && m.getName().equals("UndoColoring")) {
			return null; // there's nothing to be undone
		}
		return defaultText;
	}
	

	@Override
	public String toString() {
		return "This node has seen "+neighbors.size()+" neighbors during its life.";
	}
	
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#draw(java.awt.Graphics, sinalgo.gui.transformation.PositionTransformation, boolean)
	 */
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// Set the color of this node depending on its state
		if(isMaxNode) {
			this.setColor(Color.RED);
		} else if(drawAsNeighbor) {
			this.setColor(Color.BLUE);
		} else {
			this.setColor(Color.BLACK);
		}
		double fraction = Math.max(0.1, ((double) neighbors.size()) / Tools.getNodeList().size());
		this.drawingSizeInPixels = (int) (fraction * pt.getZoomFactor() * this.defaultDrawingSizeInPixels);
		drawAsDisk(g, pt, highlight, this.drawingSizeInPixels);
	}
	
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#drawToPostScript(sinalgo.io.eps.EPSOutputPrintStream, sinalgo.gui.transformation.PositionTransformation)
	 */
	public void drawToPostScript(EPSOutputPrintStream pw, PositionTransformation pt) {
		// the size and color should still be set from the GUI draw method
		drawToPostScriptAsDisk(pw, pt, drawingSizeInPixels/2, getColor());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CTNode tmp) {
		if(this.ID < tmp.ID) {
			return -1;
		} else {
			if(this.ID == tmp.ID) {
				return 0;
			} else {
				return 1;
			}
		}
	}
	
}
