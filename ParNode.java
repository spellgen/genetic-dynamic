/* ParNode.java - Operator superclass */

package se.ga4;
import java.io.*;
import java.lang.*;
import se.util.*;

public class ParNode extends TerminalNode
	implements Serializable, Cloneable {
	static final long serialVersionUID=GeneticAlgorithm.globalSerial;
	private double par;
	
	public ParNode(double par) {
		this.par=par;
	}
	public ParNode() {
		 par=randomPar(DynGA.paramin,DynGA.paramax);
	}
	
	public Object clone() {
		ParNode nn=new ParNode(par);
		copy(nn);
		return nn;
	}
	
	/** Parameter mutation is one of:
		1) Draw a new parameter altogether.
		2) Slightly modify (+/- 10%) */
	public void mutate(double pmut) {
		if(mutated=Util.dran()<pmut) {
			par=randomPar(DynGA.paramin,DynGA.paramax);
		}
		if(mutated|=Util.dran()<pmut) {
			par*=Util.dran(0.9,1.1);
		}
	}		
	
	public String string() {
		if(longform) return "("+par+")";
		else return Util.dec.format(par);
	}
}
