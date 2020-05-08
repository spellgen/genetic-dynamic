/* TreeNode.java - Superclass for all nodes */

package se.ga4;
import java.io.*;
import java.util.*;
import se.util.*;

public abstract class TreeNode implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
	protected static boolean longform;
	public static boolean mutated=false;
		
	/** Create a node and fill it up with random nodes below */
	public TreeNode() {
	}
	
	public abstract void init();
	public abstract Object clone();
	public abstract void mutate(double pmut);
	public abstract void copy(TreeNode tn);
	public abstract String string();
		
	/* Return a random node */
	static double poper=0.4; // must be less than 0.5 for binary operators
	public static TreeNode randomNode() {
		double psel=Util.dran(), pop=Util.dran();
		TreeNode out;
		if(pop<poper) {
			if(psel<0.3) out=new AddNode();
			else if(psel<0.6) out=new NegateNode();
			else out=new MultiplyNode();
		}	
		else {
			if(psel<0.5) out=new ParNode();
			else out=new VarNode();
		}
		out.init();
		return out;
	}
	
	public static double randomPar(double min, double max) {
		double x=Util.dran(Math.log(min),Math.log(max));
		return Math.exp(x);
	}
	
	public String toString() {
		longform=false;
		return string();
	}
	public String toCode() {
		longform=true;
		return string();
	}
}
