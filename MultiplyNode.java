/* MultiplyNode.java - Operator superclass */

package se.ga4;
import java.io.*;
import se.util.*;

public class MultiplyNode extends BranchingNode implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
	
	public Object clone() {
		MultiplyNode nn=new MultiplyNode();
		copy(nn);
		return nn;
	}
	
	public int nodemin() {return 2;}
	public int nodemax() {return 5;}
	
	public String string() {
		TreeNode nk=(TreeNode)nodeList.firstElement();
		StringBuffer out=new StringBuffer(nk.string());
		for(int k=1;k<nodeList.size();k++) {
			nk=(TreeNode)nodeList.elementAt(k);
			out.append("*"+nk.string());
		}
		return "("+out.toString()+")";
	}
}