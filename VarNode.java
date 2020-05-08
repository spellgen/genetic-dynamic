/* VarNode.java - Operator superclass */

package se.ga4;
import java.io.*;
import se.util.*;

public class VarNode extends TerminalNode
	implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
	private int idx;
	
	public VarNode(int idx) {
		 if(idx>DynSystem.nsde) Util.error("VarNode("+idx+") is out of range");
		 this.idx=idx;
	}
	public VarNode() {
		this.idx=Util.iran(DynSystem.nsde);
	}
	
	public int getIdx() {return idx;}
	
	public Object clone() {
		VarNode nn=new VarNode(idx);
		copy(nn);
		return nn;
	}
	
	public void mutate(double pmut) {
		if(mutated=Util.dran()<pmut) {
			idx=Util.iran(DynSystem.nsde);
		}	
	}
	
	public String string() {
		return "x["+idx+"]";
	}
}