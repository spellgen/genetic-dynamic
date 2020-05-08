/* TerminalNode.java - superclass for nodes with subnodes */

package se.ga4;
import java.io.*;

public abstract class TerminalNode extends TreeNode 
	implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;

	public void init() {}
	public abstract Object clone();
	public abstract String string();
	public void copy(TreeNode tn) {}
} 