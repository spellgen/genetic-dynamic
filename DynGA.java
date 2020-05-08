/* DynGA.java */

package se.ga4;

import java.util.*;
import java.io.*;
import java.util.zip.*;
import se.util.*;

/** GA implementation for the dynamic system */
public class DynGA extends GeneticAlgorithm implements Serializable {
	static final long serialVersionUID = GeneticAlgorithm.globalSerial;
	/** # of instances of a particular simulation to try evaluate
		for an average fitness */
	static public int navg=5;
	/** Control how often we will prune irrelevant parts */
	static public int nprune=200;
	/** min/max for mutating parameters */
	static public double paramax=10.0;
	static public double paramin=1.0/paramax;
	/** relative threshold for convergence */
	static public double fthreshold=0.01;
	/** Integration parameters */
	static public double eps=1.0e-3;
	static public double sigamp=0.10;
	static public double tend=4.0;	// in periods
	static public int dtper=1000; // # steps per period
	static public int dtsav=0;			// save ever dtsav timestep

	public DynGA() {
		pop=new SortedVector();
		startdate=new Date();
	}

	public void init() {
		DynSystem dsys;
		Util.message(1,"Creating DynGA: npop="+npop);
		for(int k=0;k<npop;k++) {
			dsys=new DynSystem(k);
			dsys.init();
			pop.addElement(dsys);
		}
	}
		
	/** Evaluation is done on the population level for efficiency.
			Better to only compile and run external code once per generation */
	public void toCode(String base) {
		OutputStream os;
		PrintWriter pw=null;
		String filename=base+".c";
		try {
			os=new FileOutputStream(filename);
			pw=new PrintWriter(os);
		}
		catch (IOException e) {
			Util.error("viewit: IOException "+e);
		}
		pw.println("/* "+filename+" - sde drivers for step "+
			currgen+" */");
		pw.println("");
		pw.println("#include <stdio.h>");
		pw.println("#include <math.h>");
		pw.println("");
		pw.println("double ran1(long *);");
		pw.println("");
		pw.println("char *baseid=\""+base+"\";");
		pw.println("int genid="+currgen+";");
		pw.println("int npop="+pop.size()+", nsde="+DynSystem.nsde+";");
		pw.println("int navg="+navg+", dtper="+dtper+", dtsav="+dtsav+";");
		pw.println("double sigamp="+sigamp+", tend="+tend+";");
		pw.println("long iseed="+iseed+";");
		pw.println("");
		DynSystem dsys;
		for(int k=0; k<pop.size(); k++) {
			dsys=(DynSystem)pop.elementAt(k);
			dsys.toCode(pw);
		}
		pw.println("static void *sdelist["+pop.size()+"][2];");
		pw.println("");
		pw.println("void initsde() {");
		for(int k=0; k<pop.size(); k++) {
			pw.println("	sdelist["+k+"][0]=(*chinit"+k+");");
			pw.println("	sdelist["+k+"][1]=(*chromo"+k+");");
		}
		pw.println("}");
		pw.println("");
		pw.println("void *getsde(int k, int i) {");
		pw.println("	if(k<0 || k>="+pop.size()+") {");
		pw.println("		fprintf(stderr,\"getsde: k out of bounds\\n\");");
		pw.println("		exit(1);");
		pw.println("	}");
		pw.println("	if(i<0 && i>1) {");
		pw.println("		fprintf(stderr,\"getsde: i out of bounds\\n\");");
		pw.println("		exit(1);");
		pw.println("	}");
		pw.println("	return sdelist[k][i];");
		pw.println("}");
		pw.close();
	} 

	public void pruneNonDependent() {
		DynSystem dsys;
		for(int k=0; k<pop.size(); k++) {
			dsys=(DynSystem)pop.elementAt(k);
			dsys.pruneNonDependent();
		}
	}
	/* Compile, run and extract results form c-spec of genome.
			Assumes the c-source exists in the current directory */
	public double[] runCode(String base) {
		String filename= new String(base+".c");
		Properties sys=System.getProperties();
		String os=sys.getProperty("os.name");
		try {
			if(os.compareTo("Linux")==0) {
				String[] com1={"gcc","-O", "-o","runsim",
					filename,
					"-L$HOME/lib","-lgasim","-ltfd","-lm"};
				Util.exec(com1);
			}
			else if (os.compareTo("Windows NT")==0) {
				String[] com1={"gcc","-o","runsim",filename,
					"-L$HOME/lib","-lgasim","-ltfd","-lm"};
				Util.exec(com1);
			}
			else if (os.compareTo("Windows 95")==0) {
				String[] com1={"cl","-o","runsim",filename,
					"/link","/nodefaultlib:libcd.lib","gasim.lib","tfd.lib"};
				Util.exec(com1);
			}
			else {
				Util.error("DynGA.runcode: undefined os: "+os);
			}
		
		/* Run the code */
			String[] com2={"runsim",base};
			Util.exec(com2);
		}
		catch (IOException e) {
			Util.error("IOException while spawning external process: "+e);
		}
		catch (InterruptedException e) {
			Util.error("InterruptedException occured in external process: "+e); 
		}
		/* Pick up the output, look for the fitness values */
		String tfname=new String(base+".out");
		char tag;
		try {
			FileReader fr=new FileReader(tfname);
			TaggedInputReader tf=new TaggedInputReader(fr);
			while((tag=tf.rh())!=' ') {
				switch (tag) {
					case 'v':
						if(tf.getName().compareTo("fitness")==0) return tf.rv();
						else tf.rv();
						break;
					default:
						tf.skip();
						break;
				}
			}
		}
		catch (Exception e) {
			Util.error("Problem reading fitness file: "+e);
		}
		Util.error("no fitness data available in file "+tfname);
		return null;
	}
	
	/* Write code, run it, set fitness in population array */
	public void calcFitness() {
		toCode(basename);
		double[] fitness=runCode(basename);
		Chromosome ck;
		for(int k=0;k<pop.size();k++) {
			ck=(Chromosome)pop.elementAt(k);
			ck.setFitness(fitness[k]);
			fitness[k]=ck.getFitness();
		}
		if(garun.tfout!=null) garun.tfout.wv("fitness",fitness);
	}
			
	/** Use Java Serialization to save state of system */
	public void save(String base) {
		String outputname=base+".ga4";
		origin=outputname;
		finishdate=new Date();
		Util.message(1,"Saving GA to "+outputname);
		try {
			FileOutputStream fos=new FileOutputStream(outputname);
			GZIPOutputStream gzos=new GZIPOutputStream(fos);
			ObjectOutputStream oos=new ObjectOutputStream(gzos);
			oos.writeObject(this);
			oos.writeObject(Util.rangen);
			oos.flush();
			oos.close();
		}
		catch (IOException e) {
			Util.error("IOException: "+e.getMessage());
		}
	}
		
	/** Load previously saved state */
	public static DynGA load(String elemfile) {
		DynGA newga=null;
		try {
			FileInputStream fis=new FileInputStream(elemfile);
			GZIPInputStream gzis=new GZIPInputStream(fis);
			ObjectInputStream ois=new ObjectInputStream(gzis);
			newga=(DynGA)ois.readObject();			
			Util.rangen=(Random)ois.readObject();
			ois.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			Util.error("IOException: "+e.getMessage());
		}
		catch (ClassNotFoundException e) {
			Util.error("ClassNotFoundException: "+e.getMessage());
		}
		newga.startdate=new Date();
		return newga;
	}
}
