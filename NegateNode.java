/* NegateNode.java - Operator superclass */

package se.ga4;
import java.io.*;

public class NegateNode extends BranchingNode
	implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
	
	public Object clone() {
		NegateNode nn=new NegateNode();
		copy(nn);
		return nn;
	}
	
	public int nodemin() {return 1;}
	public int nodemax() {return 1;}
		
	public String string() {
		TreeNode nk=(TreeNode)nodeList.firstElement();
		return "(-"+nk.string()+")";
	}
}