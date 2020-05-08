/* Chromosome.java */

package se.ga4;

import java.util.*;
import java.io.*;
import se.util.*;

/** Chromosome superclass. 
		Specify common methods: evaluate(), mutate(), crossover() */
public abstract class Chromosome implements Sortable, Serializable {
		static final long serialVersionUID = GeneticAlgorithm.globalSerial;
		protected double fitness;
		protected int idx;
		
		public int getIdx() {return idx;}
		public void setIdx(int idx) {this.idx=idx;}
		public void setFitness(double fitness) {
		    this.fitness=fitness;
		}
		public double getFitness() {return fitness;}
		
		public Chromosome(int idx) {this.idx=idx;}
		
		/** Return new chromosome by combining this+other */
		abstract public Chromosome crossover(Chromosome other);

		/** apply mutations to all relevant components of the chromosome */
		abstract public void mutate(double pmut);

		abstract public String toString();

		/** Chromosomes are compared based on their fitness */
		public int compareTo(Object other) {
				Chromosome cto=(Chromosome)other;
				if(fitness<cto.fitness) return -1;
				else if(fitness>cto.fitness) return 1;
				return 0;
		}
		
}
