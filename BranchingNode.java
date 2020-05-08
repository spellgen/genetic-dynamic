/* BranchingNode.java - superclass for nodes with subnodes */

package se.ga4;
import java.io.*;
import java.util.*;
import se.util.*;

public abstract class BranchingNode extends TreeNode 
	implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;

	protected Vector nodeList;
	
	public void init() {
		int n=nodemin();
		nodeList=new Vector(n,1);
		for(int i=0;i<n;i++) addNode(randomNode());
	}
	
	public abstract Object clone();
	public abstract String string();
	public abstract int nodemin();
	public abstract int nodemax();

	// The actual object is created in the subclass
	public void copy(TreeNode tn) {
		BranchingNode bn=(BranchingNode)tn;
		int n=this.nNode();
		bn.nodeList=new Vector(n,1);
		TreeNode nk;
		for(int i=0; i<n; i++) {
			nk=this.getNode(i);
			bn.addNode((TreeNode)nk.clone());
		}
	}
	
	public void mutate(double pmut) {
		if(this instanceof RootNode)
			Util.error("BranchingNode.mutate called for a RootNode, tsk tsk");
		if(nNode()<nodemax() && Util.dran()<pmut) {
			addNode(randomNode());
			mutated = true;
		}
		if(nNode()>nodemin() && Util.dran()<pmut) {
			delNode(Util.iran(nNode())); 
			mutated |= true;
		}
	}
	
	public int nNode() {return nodeList.size();}
	public TreeNode getNode(int k) {
		return (TreeNode)nodeList.elementAt(k);
	}
	public void addNode(TreeNode node) {
		if(nodeList.size()<nodemax()) {
			nodeList.addElement(node);
		}
	}
	public void setNode(TreeNode node, int k) {
		nodeList.setElementAt(node,k);
	}
	public void delNode(int k) {
		if(nodeList.size()>nodemin()) {
			nodeList.removeElementAt(k);
		}
	}
} 
