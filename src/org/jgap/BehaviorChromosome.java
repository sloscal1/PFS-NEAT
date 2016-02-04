package org.jgap;

import org.jgap.Chromosome;
import org.jgap.ChromosomeMaterial;

public class BehaviorChromosome extends Chromosome {
	/** Gen ID */
	private static final long serialVersionUID = 8116683538299856209L;
	
	private double[] behaviorVector;
	
	public BehaviorChromosome(ChromosomeMaterial arg0, Long arg1) {
		super(arg0, arg1);
	}

	public void setBehaviorVector(double[] behaviorVector){
		this.behaviorVector = behaviorVector.clone(); //Defensive copy
	}
	public double[] getBehaviorVector(){
		return behaviorVector.clone(); //Defensive copy...
	}

}
