/* DynSystem.java */

package se.ga4;

import java.util.*;
import java.io.*;
import se.util.*;
import se.num.*;

public class DynSystem extends Chromosome implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
		
	/** Number of stochastic diff eqns in the simulation */
	public static int nsde=5;
	public static boolean fixedicond=false;
	public static int nnodeBreak=50;  /* size penalty kicks in */
	public static int nnodeMax=100;	  /* maximum size penalty */
	private RootNode sde;
		
	public DynSystem(int idx) {
		super(idx);
	}
	
	public void init() {
		sde=new RootNode(nsde,fixedicond);
		sde.init();
	}
	
	// Overrides Chromosome method
	public void setFitness(double fitness) {
		double modify=1.0;
		int n=recurseCount(sde);
		if(n<=nnodeBreak*nsde) modify=1.0;
		else if(n>=nnodeMax*nsde) modify=0.0;
		else {
			double x=(double)(n-nnodeBreak*nsde)/
				(double)(nsde*(nnodeMax-nnodeBreak));
			modify=1.0-x*x;
		}
		this.fitness=fitness*modify;
	}
	public Object clone() throws CloneNotSupportedException {
		DynSystem ds=new DynSystem(getIdx());
		ds.sde=(RootNode)this.sde.clone();
		return ds;
	} 

	public void mutate(double pmut) {
		recurseMutate(sde, pmut);
	}

	public void recurseMutate(TreeNode node, double pmut) {
		double p=Util.dran();
		node.mutate(pmut);
		if(node instanceof BranchingNode) {
			BranchingNode bn=(BranchingNode)node;
			for(int k=0; k<bn.nNode(); k++) {
				recurseMutate(bn.getNode(k),pmut);
			}
		}
	}

	/** return new dynamical system chromosome by combining 
		or optionally cloning 'this' and/or 'otherchromo'.
		This and other are symmetrically chosen, so it does
		not matter which one we use... */
	public Chromosome crossover(Chromosome otherchromo) {
		DynSystem chromo=null, other;
		try {
			chromo=(DynSystem)clone();
			other=(DynSystem)otherchromo;
			boolean cloneflg, clonechoice,sel;
			cloneflg=Util.dran()<0.1;	// ... just use the copy we already made
			if(!cloneflg) { 
				BranchingNode n1=(BranchingNode)pickrandom(chromo,false);
				TreeNode n2=pickrandom(other,true);
				if(n1!=null && n2!=null) {
					int ipos=Util.iran(n1.nNode());
					n1.setNode((TreeNode)n2.clone(),ipos);
				}
				else {
					Util.message(1,"Could not perform crossover");
				}
			}
		}
		catch (CloneNotSupportedException e) {
			Util.error("cloning: "+e);
		}
		return chromo;
	}
	
	/** Pick a random node from the specified dynamical system.
			If terminalok is false, any node which links down is ok.
			If terminalok is true, any node not at the top level is ok */
	public TreeNode pickrandom(DynSystem dsys, boolean terminalok) {
		Vector allnodes=new Vector();
		recurseNodes(allnodes,dsys.sde,terminalok);
		// now pick a random node from the list of all candidates
		int nn=allnodes.size();
		if(nn>0) 
			return (TreeNode)allnodes.elementAt(Util.iran(nn));
		else
			return null;
	}
	
	/** Aescend into the node tree. A node will be under consideration
			if it fulfills one of two conditions:
			1. TerminalNodes are not ok and is a BranchingNode
			2. TerminalNodes are ok and it is not a root level node. */
	public void recurseNodes(Vector nodelist, TreeNode node, 
				boolean terminalok) {
		if( (!terminalok && (node instanceof BranchingNode)) || 
				(terminalok && !(node instanceof RootNode)) ) {
			nodelist.addElement(node);
		}
		if(node instanceof BranchingNode) {
			BranchingNode bn=(BranchingNode)node;
			for(int k=0; k<bn.nNode(); k++) {
				recurseNodes(nodelist,bn.getNode(k),terminalok);
			}
		}
	}
	
	public void pruneNonDependent() {
		boolean[][] deptable=new boolean[nsde][nsde];
		boolean[] mustkeep=new boolean[nsde];
		int k;
		for(k=0;k<nsde;k++) {
			fillDependencies(sde.getNode(k),deptable[k]);
		}
		for(k=0;k<nsde;k++) {
			if(deptable[0][k]) {
				mustkeep[k]=true;
				for(int i=0;i<nsde;i++) {
					if(deptable[k][i]) mustkeep[i]=true;
				}
			}
		}
		for(k=0;k<nsde;k++) {
			if(!mustkeep[k]) {
				TreeNode tn=(TreeNode)sde;
				sde.setNode(TreeNode.randomNode(),k);
			}
		}
	}

	public void fillDependencies(TreeNode node, boolean[] deplist) {
		if(node instanceof BranchingNode) {
			BranchingNode bn=(BranchingNode)node;
			for(int k=0; k<bn.nNode(); k++) {
				fillDependencies(bn.getNode(k),deplist);
			}
		}
		else if(node instanceof VarNode) {
			VarNode vn=(VarNode)node;
			deplist[vn.getIdx()]=true;
		}
	}

	public int recurseCount(TreeNode node) {
		int count=1;
		if(node instanceof BranchingNode) {
			BranchingNode bn=(BranchingNode)node;
			for(int k=0; k<bn.nNode(); k++) {
				count+=recurseCount(bn.getNode(k));
			}
		}
		return count;
	}

	
	/** Write code for numerical integration of this system of SDEs */
	public void toCode(PrintWriter pw) {
		pw.println("int chinit"+idx+ "(double x[], int *nnodes) {");
		for(int k=0;k<nsde;k++) {
			if(fixedicond) {
				pw.println("  x["+k+"]="+sde.icond[k]+";");
			}
			else {
				pw.println("  x["+k+"]=ran1(&iseed)*2.0-1.0;");
			}
		}
		pw.println("}");
		pw.println("");
		pw.println("void chromo"+idx+
		"(double t, double signal, double x[], double dxdt[], double stoch[]) {");
		pw.print(sde.toCode());
		pw.println("}");
		pw.println("");
	}
		
	public String toString() {
		StringBuffer out=new StringBuffer("Chromosome ["+getIdx()+"]\n");
		out.append(sde.toString()+"\n");
		return out.toString();
	}
}
