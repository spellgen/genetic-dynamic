/* gamerge.java */

package se.ga4;

import java.util.*;
import java.io.*;
import se.util.*;

/** main() for executing genetic algorithm */
public class gamerge {
	static final long serialVersionUID = GeneticAlgorithm.globalSerial;
	public static LogStream log=null;
	private static String oname=null,iname1=null,iname2=null;
	private static int nin=0;
	private static void process() {
		Util.message("processing "+iname1+" and "+iname2);
		DynGA ga1=new DynGA();
		DynGA ga4=new DynGA();
		ga1=ga1.load(iname1);
		ga4=ga4.load(iname2);
		ga1.merge(ga4);
		if(oname==null) oname=Util.stripsuffix(iname1)+"+"+
			Util.stripsuffix(iname2);
		ga1.save(oname+".ga-0");
		nin++;
	}
	
	public static void main(String[] args) {
		Util.setDebug(1);
		Util.dec.setMaximumFractionDigits(4);
		for(CLArgs arg=new CLArgs(args,true);arg.loop();arg.next()) {
			if(arg.match("-o")) {oname=arg.sval();}
			else if(arg.isflag()) {
				Util.error("Unknown flag: "+arg.sval());
			}
			else {	
				Util.message("processing "+arg.sval());
				if(iname1==null) {
					iname1=arg.sval();
				}
				else if(iname2==null) {
						iname2=arg.sval();
						process();
						iname1=iname2=oname=null;
				}
			}
		} // argument loop
		if(nin<0) usage();
		if(log!=null) log.close();
	}
	public static void usage() {
		Util.message(0,"usage: java gamerge [flags] <ga-files> ");
		Util.message(0,"-o			specify output filename");
	}
}