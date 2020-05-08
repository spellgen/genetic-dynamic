/* RootNode.java - Operator superclass */

package se.ga4;
import java.io.*;
import se.util.*;

public class RootNode extends BranchingNode
	implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
	private int nsde;
	private boolean fixedicond;
	public double[] icond, stoch;
	
	public RootNode(int n, boolean fixed) {
		nsde=n;
		fixedicond=fixed;
		icond=new double[nsde];
		stoch=new double[nsde];
	}
	
	public void init() {
		super.init();
		for(int i=0;i<nsde;i++) {
			if(fixedicond) icond[i]=Util.dran(-1.0,1.0);
			stoch[i]=(i==0 ? 1.0 : 0.0);
		}
	}
	
	public Object clone() {
		RootNode nn=new RootNode(nsde,fixedicond);
		for(int i=0;i<nsde;i++) {
			if(fixedicond) nn.icond[i]=icond[i];
			nn.stoch[i]=stoch[i];
		}
		nn.fixedicond=fixedicond;
		copy(nn);
		return nn;
	}
	
	/** Must override BranchingNode.mutate() since it is not
		allowed to add more equations (for now).
		Furthermore we want to be able to tune the initial conditions */
	public void mutate(double pmut) {
		for(int i=0;i<nsde;i++) {
			if(fixedicond && (mutated=(Util.dran()<pmut))) {
				icond[i]=Util.dran(-1.0,1.0);
			}
			if(fixedicond && (mutated|=(Util.dran()<pmut))) {
				icond[i]*=(1.0+Util.dran(-0.1,0.1));
			}
		}
	}
			
	public int nodemin() {return DynSystem.nsde;}
	public int nodemax() {return DynSystem.nsde;}
	
	public String string() {
		TreeNode nk;
		StringBuffer out=new StringBuffer("");
		for(int k=0;k<nodeList.size();k++) {
			nk=(TreeNode)nodeList.elementAt(k);
			if(longform) out.append("  ");
			out.append("dxdt["+k+"]="+nk.string());
			if(longform) out.append(k==0 ? "+signal;" : ";");
			out.append("\n");
			if(longform) out.append(" stoch["+k+"]="+stoch[k]+";\n");
		}
		return out.toString();
	}
}
