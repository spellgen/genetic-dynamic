/* GeneticAlgorithm.java */

package se.ga4;

import java.util.*;
import java.io.*;
import se.util.*;

/** Superclass for ga:s. Provide fitness, general chromosome spec, 
	 and a vector to hold a population of 'Chromosomes'. */
public abstract class GeneticAlgorithm implements Serializable {
	static final long globalSerial=980504;
	static final long serialVersionUID = globalSerial;

	/** Simulation information */
	public Date startdate,finishdate;
	public String origin,basename;

	/** population of Chromosomes */
	public SortedVector pop=new SortedVector(); 

	/** Global mutation rate */
	public static double pmutate=0.005;
	public static long iseed=7;

	/** # of generations to run and save */
	public static int ngen=200;
	public static int nsave=50;
	/** Generation counter */
	public int currgen=0;
	/** default initial population size */
	public int npop=100;

	/** Signal doneness if uniformly fit population */
	private double relfitThreshold=0.01;
	
	/** Maximum fitness achieved so far */
	private double fitmaxGlobal=0.0;
	
	public abstract void calcFitness();

	/** Return a string with a summary of the top 'ntop' elements */
	public String topview(int ntop) {
		pop.sort();
		int n=pop.size();
		Chromosome chrom;
		StringBuffer out=new StringBuffer("Origin="+origin+
				", Generation="+currgen+
				", pop="+pop.size()+"\n"+
				"pmutate="+Util.dec.format(pmutate));
		for(int i=1;i<=Math.min(n,ntop);i++) {
			chrom=(Chromosome)pop.elementAt(n-i);
			out.append(i+": "+chrom+"\n");
		}
		return out.toString();
	}
	
	/** Join two populations */
	public void merge(GeneticAlgorithm ga4) {
		int n=ga4.pop.size();
		Chromosome chrom;
		for(int i=0;i<n;i++) {
			chrom=(Chromosome)ga4.pop.elementAt(i);
			pop.addElement(chrom);
		}
		currgen=0; // start over after merging
		Util.message("After adding ga4 to ga1: pop="+pop.size());
	}
	
	/** advance simulation one generation */
	public boolean evaluate() {
		double fitmax, relfit;
		// put most fit individuals at the end of the vector
		calcFitness();
		pop.sort();
		// Look for very fit individuals
		Chromosome mostfit=(Chromosome)pop.lastElement();
		Chromosome notfit=(Chromosome)pop.elementAt((int)(pop.size()*0.10));
		fitmax=mostfit.getFitness();
		if(fitmax==0.0) {
			Util.error("genstep: max fitness=0, now that's strange");
		}
		relfit=(fitmax-notfit.getFitness())/fitmax;
		garun.log.println("relative fitness="+relfit);
		Util.message(1,"genstep: Fitness achieved: max="+
			Util.dec.format(fitmax)+
			", relative="+Util.dec.format(relfit));
		if(relfit<relfitThreshold) return true;
		if(fitmax>fitmaxGlobal) {
			garun.log.println("A very fit chromosome:"+mostfit);
			fitmaxGlobal=fitmax;
			garun.log.println("Fitness after re-evaluate:"+
					mostfit.getFitness());
		}
		return false;
	}
	/** Build a new generation using crossover */
	public void crossover() {
		SortedVector newpop=new SortedVector();
		int n=pop.size();
		Chromosome chromo,c1,c2;
		for(int k=0;k<n;k++) {
			c1=(Chromosome)pop.elementAt(Util.biasediran(n));
			c2=(Chromosome)pop.elementAt(Util.biasediran(n));
			chromo=c1.crossover(c2);	// generate new chromosome
			chromo.setIdx(k);
			newpop.addElement(chromo);
		}
		pop=newpop;
	}
	public void mutate() {
		Chromosome ck;
		for(int k=0;k<pop.size();k++) {
			ck=(Chromosome)pop.elementAt(k);
			ck.mutate(pmutate);
		}
	}

	// define default printing
	private void readObject(ObjectInputStream in)
		throws IOException,ClassNotFoundException {
		Util.message(1,"Reading GeneticAlgorithm object");
		in.defaultReadObject();
	}
}
