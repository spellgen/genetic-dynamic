/* garun.java */

package se.ga4;

import java.util.*;
import java.io.*;
import se.util.*;

/** main() for executing genetic algorithm */
public class garun {
	static final long serialVersionUID = GeneticAlgorithm.globalSerial;
	/** Common output log stream */
	static public LogStream log=null;
	static public TaggedOutputWriter tfout=null;

	public static void main(String[] args) {
		int nin=0;
		boolean viewflag=false;
		Util.setDebug(1);
		DynGA ga=new DynGA();
		String basename=null;
		for(CLArgs arg=new CLArgs(args,true);arg.loop();arg.next()) {
			if(arg.match("-ngen")) {ga.ngen=arg.ival();}
			else if(arg.match("-npop")) {ga.npop=arg.ival();}
			else if(arg.match("-seed")) {
				ga.iseed=Util.newseed(arg.ival());
				System.out.println("new seed="+ga.iseed);
			}
			else if(arg.match("-nsave")) {ga.nsave=arg.ival();}
			else if(arg.match("-navg")) {ga.navg=arg.ival();}
			else if(arg.match("-nsde")) {DynSystem.nsde=arg.ival();}
			else if(arg.match("-nprune")) {DynGA.nprune=arg.ival();}
			else if(arg.match("-pmut")) {ga.pmutate=arg.dval();}
			else if(arg.match("-view")) {viewflag=true;}
			else if(arg.match("-v")) {Util.setDebug(Util.getDebug()+1);}
			else if(arg.match("-tend")) {DynGA.tend=arg.dval();}
			else if(arg.match("-sigamp")) {DynGA.sigamp=arg.dval();}
			else if(arg.match("-dtper")) {DynGA.dtper=arg.ival();}
			else if(arg.match("-dtsav")) {DynGA.dtsav=arg.ival();}
			else if(arg.match("-fixflg")) {
				DynSystem.fixedicond=!DynSystem.fixedicond;
			}
			else if(arg.match("-o")) {
				basename=arg.sval();
				nin++;
				ga.basename=basename;
				ga.init();
				if(viewflag) viewit(ga);
				else runit(ga);
			}
			else if(arg.isflag()) {
				Util.error("Unknown flag: "+arg.sval());
			}
			else {	// We have an old simulation
				String iname=arg.sval();
				ga=DynGA.load(iname);
				if(basename==null) basename=Util.stripsuffix(iname);
				ga.basename=basename;
				nin++;
				if(viewflag) viewit(ga);
				else runit(ga);
			}
		}
		if(nin==0) {
			usage(ga);
		}
	} // end of main
		
	private static void runit(DynGA ga) {
		boolean done=false;
		ga.currgen=0;	// Reset the generation counter
		OutputStream s;
		try {
			s=new FileOutputStream(ga.basename+".log");
			log=new LogStream(s);
//			Util.setMessageStream(log);
			s=new FileOutputStream(ga.basename+".tf");
			tfout=new TaggedOutputWriter(s);
		}
		catch (IOException e) {
			Util.error("runit: IOException "+e);
		}
		ga.evaluate();
		for(int i=0; i<ga.ngen && !done; i++) {
			if(ga.currgen%ga.nsave==0) {
				String stepbase=ga.basename+"-"+ga.currgen;
				ga.save(stepbase);
			}
			ga.crossover();
			ga.mutate();
			ga.currgen++;
			if((ga.currgen)%ga.nprune==0) {
				ga.pruneNonDependent();
			}
			done=ga.evaluate();
		}
		if(ga.currgen%ga.nsave==0) {
			String stepbase=ga.basename+"-"+ga.currgen;
			ga.save(stepbase);
		}
		if(log!=null) log.close();
		if(tfout!=null) tfout.close();
	}
	
	private static void viewit(DynGA ga) {
		boolean done=false;
		int i;
		OutputStream s;
		try {
			s=new FileOutputStream(ga.basename+".log");
			log=new LogStream(s);
		}
		catch (IOException e) {
			Util.error("viewit: IOException "+e);
		}
		ga.evaluate();
		DynSystem dsys;
		for(int k=0;k<ga.pop.size();k++) {
			dsys=(DynSystem)ga.pop.elementAt(k);
			log.println(dsys);
		}
		if(log!=null) log.close();
	}
	
	public static void usage(DynGA ga) {
		Util.message(0,"usage: java ga [flags] <chromspec-file>");
		Util.message(0,"-npop .... population size ["+ga.npop+"]");
		Util.message(0,"-ngen .... maximun number of generations ["+ga.ngen+"]");
		Util.message(0,"-nsave ... save every n steps ["+ga.nsave+"]");
		Util.message(0,"-seed .... initial random seed ["+Util.initialseed()+"]");
		Util.message(0,"-fixflg .. keep initial conds fixed ["+DynSystem.fixedicond+"]");
		Util.message(0,"-navg .... # of evaluations for average ["+ga.navg+"]");
		Util.message(0,"-prune.... prune tree how often ["+ga.nprune+"]");
		Util.message(0,"-pmut .... mutation rate ["+ga.pmutate+"]");
		Util.message(0,"-tend .... integration time["+DynGA.tend+"]");
		Util.message(0,"-sigamp... signal amplitude ["+DynGA.sigamp+"]");
		Util.message(0,"-dtper ... timesteps per period ["+DynGA.dtper+"]");
		Util.message(0,"-dtsav ... record every dtsav timestep ["+DynGA.dtsav+"]");
		Util.message(0,"-o ....... specify output basename");
		Util.message(0,"-view .... just view the state of the system");
		Util.message(0,"-v ....... increase verbosity");
	}
}
