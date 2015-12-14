/*
 * Copyright (C) 2004 Derek James and Philip Tucker
 * 
 * This file is part of ANJI (Another NEAT Java Implementation).
 * 
 * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * created by Philip Tucker on Feb 16, 2003
 */
package mil.af.rl.novelty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import mil.af.rl.anji.NeatChromosomeUtility;
import mil.af.rl.anji.NeatConfigurationAdapter;
import mil.af.rl.anji.learner.ConcurrentFitnessFunction;
import mil.af.rl.anji.learner.RL_Learner;
import mil.af.rl.predictive.EagerChromosomeSampleContainer;
import mil.af.rl.predictive.PFSInfo;
import mil.af.rl.predictive.PredictiveLearner;
import mil.af.rl.predictive.SampleContainer;
import mil.af.rl.predictive.SubspaceIdentification;

import org.apache.log4j.Logger;
import org.jgap.Allele;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;
import org.jgap.ChromosomeMaterial;
import org.jgap.Genotype;
import org.jgap.NoveltyGenotypeAdapter;
import org.jgap.event.GeneticEvent;

import com.anji.Copyright;
import com.anji.integration.LogEventListener;
import com.anji.integration.PersistenceEventListener;
import com.anji.neat.ConnectionAllele;
import com.anji.neat.NeatConfiguration;
import com.anji.neat.NeuronAllele;
import com.anji.neat.NeuronType;
import com.anji.persistence.Persistence;
import com.anji.run.Run;
import com.anji.util.Configurable;
import com.anji.util.Properties;
import com.anji.util.Reset;

/**
 * Configures and performs an ANJI evolutionary run.
 * 
 * @author Philip Tucker
 * @author Steven Loscalzo modified for compatibility with PFS.
 */
public class NoveltyEvolver implements Configurable, PredictiveLearner {

	/**
	 * Main program used to perform an evolutionary run.
	 * 
	 * @param args command line arguments; args[0] used as properties file
	 * @throws Throwable
	 */
	public static void main( String[] args ) throws Throwable {
		System.out.println("Length: "+args.length);
		System.out.println("Filename: "+args[0]);
		try {
			System.out.println( Copyright.STRING );

			Properties props = new Properties( args[ 0 ] );
			NoveltyEvolver evolver = new NoveltyEvolver();
			evolver.init( props );
			evolver.run();
			System.exit( 0 );
		}
		catch ( Throwable th ) {
			logger.error( "", th );
			throw th;
		}
	}

	private static Logger logger = Logger.getLogger(NoveltyEvolver.class);

	//These are all keys for properties file info
	/** properties key, # generations in run */
	public static final String NUM_GENERATIONS_KEY = "num.generations";
	/** properties key, fitness function class */
	public static final String FITNESS_FUNCTION_CLASS_KEY = "fitness_function";
	/** whether or not the genotype should be read from an existing source */
	public static final String RESET_KEY = "run.reset";
	/** properties key, target fitness value - after reaching this run will halt */
	public static final String FITNESS_TARGET_KEY = "fitness.target";
	/** the size of the number of inputs to the networks (at maximum) */
	public static final String NUM_FEATURES = "stimulus.size";

	//State variables for this class
	/** The configuration file */
	private NeatConfiguration config = null;
	/** Champion chromosome */
	private Chromosome champ = null;
	/** The Genotype that is being evolved */
	private NoveltyGenotypeAdapter genotype = null;
	/** The maximum number of evolutions to allow */
	private int numEvolutions = 0;
	private double targetFitness = 0.0;
	private double thresholdFitness = 0.0;
	private int maxFitness = 0;
	private Persistence db = null;
	private Date runStartDate;
	private DateFormat fmt;
	private int generationOfFirstSolution;
	private double adjustedFitness;
	private int generation = 0;

	//PFS specific information:
	/** The storage container used to keep track of samples */
	private SampleContainer container;
	/** Whether or not to collect samples (optimization) */
	private boolean collect = false;
	/** The current set of selected features, mapped to the number when it was
	 * selected, or -1 (SubspaceIdentification.UNUSED) if it is not currently
	 * selected. */
	private Map<Integer, Integer> selectedFeatures;
	/** The previous set of selected features, used to roll-back if necessary */
	private Map<Integer, Integer> reversionSelectedFeatures;
	/** An example chromosome in the roll-back generation */
	private Chromosome reversionBest;
	/** The current run */
	private Run run;
	/** The shared PFS info object - could be a singleton pattern. */
	private PFSInfo info = new PFSInfo();
	/** Fitness function to use during evaluation */
	private BulkFitnessFunction fitnessFunc;
	/** The number of chromosomes to sample with lazy sampling (better on mem, worse on time) 
	 * This is only used if EagerChromosomeSampleContainer is not used. */
	private int lazyK = 5;
	/**
	 * ctor; must call <code>init()</code> before using this object
	 */
	public NoveltyEvolver() {
		super();
	}

	/**
	 * Construct new evolver with given properties. See <a href=" {@docRoot}
	 * /params.htm" target="anji_params">Parameter Details </a> for specific
	 * property settings.
	 * 
	 * @see com.anji.util.Configurable#init(com.anji.util.Properties)
	 */
	public void init(Properties props) throws Exception {
		boolean doReset = props.getBooleanProperty(RESET_KEY, false);
		if (doReset) {
			logger.warn("Resetting previous run !!!");
			Reset resetter = new Reset(props);
			resetter.setUserInteraction(false);
			resetter.reset();
		}

		config = new NeatConfigurationAdapter(props);

		// peristence
		db = (Persistence) props
				.singletonObjectProperty(Persistence.PERSISTENCE_CLASS_KEY);
		//Ending criteria
		numEvolutions = props.getIntProperty(NUM_GENERATIONS_KEY);
		targetFitness = props.getDoubleProperty(FITNESS_TARGET_KEY, 1.0d);
		thresholdFitness = props.getDoubleProperty(ConcurrentFitnessFunction.THRESHOLD_KEY,
				targetFitness);

		run = (Run) props.singletonObjectProperty(Run.class);
		db.startRun(run.getName());
		// fitness function
		fitnessFunc = (BulkFitnessFunction) props
				.newObjectProperty(FITNESS_FUNCTION_CLASS_KEY);
		//Make sure the same object is used throughout
		((ConcurrentFitnessFunction)fitnessFunc).setPFSInfo(info);

		config.getEventManager().addEventListener(
				GeneticEvent.GENOTYPE_EVALUATED_EVENT, run);

		// logging
		LogEventListener logListener = new LogEventListener(config);
		config.getEventManager().addEventListener(
				GeneticEvent.GENOTYPE_EVOLVED_EVENT, logListener);
		config.getEventManager().addEventListener(
				GeneticEvent.GENOTYPE_EVALUATED_EVENT, logListener);

		// persistence
		if(props.getBooleanProperty("use.searchparty", false)){
			SearchPartyEventListener spListener = new SearchPartyEventListener();
			spListener.init(props);
			config.getEventManager().addEventListener(GeneticEvent.GENOTYPE_EVOLVED_EVENT,
					spListener);
		}

		PersistenceEventListener dbListener = new PersistenceEventListener(
				config, run);
		dbListener.init(props);
		config.getEventManager()
		.addEventListener(
				GeneticEvent.GENOTYPE_START_GENETIC_OPERATORS_EVENT,
				dbListener);
		config.getEventManager().addEventListener(
				GeneticEvent.GENOTYPE_FINISH_GENETIC_OPERATORS_EVENT,
				dbListener);
		config.getEventManager().addEventListener(
				GeneticEvent.GENOTYPE_EVALUATED_EVENT, dbListener);

		config.setBulkFitnessFunction(fitnessFunc);

		maxFitness = config.getBulkFitnessFunction().getMaxFitnessValue();

		// load population, either from previous run or random
		Genotype geno = db.loadGenotype(config);
		int numFeatures = props.getIntProperty(NUM_FEATURES);
		// At first we have the identity mapping
		selectedFeatures = new LinkedHashMap<Integer, Integer>();
		for (int i = 0; i < numFeatures; ++i)
			selectedFeatures.put(i, i);

		MultiObj multiObj = (MultiObj)props.newObjectProperty(MultiObj.NOVELTY_OBJ_KEY);

		if (geno != null) {
			genotype = new NoveltyGenotypeAdapter(geno, multiObj, info);
			logger.info("genotype from previous run");
			// See if we need to assign a selected feature subset
			Chromosome c = genotype.getFittestChromosome();
			// Get the champion's selected set
			//Determine all the input nodes and if they're connected:
			@SuppressWarnings("unchecked")
			SortedSet<Allele> allAlleles = c.getAlleles();
			List<NeuronAllele> inputs = NeatChromosomeUtility.getNeuronList(allAlleles, NeuronType.INPUT);
			//Align the order with how the problem expects them
			Collections.sort(inputs, new InputNeuronComparator());
			//Now figure out which connections link up to these nodes
			List<ConnectionAllele> conns = NeatChromosomeUtility.getConnectionList(allAlleles);
			Set<Long> connectedInputs = new HashSet<>();
			for(ConnectionAllele conn : conns)
				connectedInputs.add(conn.getSrcNeuronId());
			//If the input is the src to some connection, then it's connected
			for(int i = 0, pos = 0; i < inputs.size(); ++i){
				if(connectedInputs.contains(inputs.get(i).getInnovationId()))
					selectedFeatures.put(i, pos++);
				else
					selectedFeatures.put(i, SubspaceIdentification.UNUSED);
			}
		} else {
			genotype = NoveltyGenotypeAdapter.randomInitialGenotype(config, multiObj, info);
			logger.info("random genotype");
		}

		// run start time
		runStartDate = Calendar.getInstance().getTime();
		logger.info("Run: start");
		fmt = new SimpleDateFormat("HH:mm:ss");

		// initialize result data
		generationOfFirstSolution = -1;
		champ = genotype.getFittestChromosome();
		// System.out.println("MAX FIT = " + maxFitness);
		adjustedFitness = (maxFitness > 0 ? champ.getFitnessValue()
				/ maxFitness : champ.getFitnessValue());
	}

	/**
	 * Log summary data of run including generation in which the first solution
	 * occurred, and the champion of the final generation.
	 * 
	 * @param generationOfFirstSolution
	 * @param champ
	 */
	@SuppressWarnings("unchecked")
	private static void logConclusion(int generationOfFirstSolution,
			Chromosome champ) {
		logger.info("generation of first solution == "
				+ generationOfFirstSolution);
		logger.info("champ # connections == "
				+ NeatChromosomeUtility.getConnectionList(champ.getAlleles())
				.size());
		logger.info("champ # hidden nodes == "
				+ NeatChromosomeUtility.getNeuronList(champ.getAlleles(),
						NeuronType.HIDDEN).size());
	}


	@Override
	public double getNumSamples() {
		return Double.valueOf(RL_Learner.getUpdates().toString());
	}

	@Override
	public double learn() {
		if (generation < numEvolutions && adjustedFitness < targetFitness){
			// generation start time
			Date generationStartDate = Calendar.getInstance().getTime();
			logger.info("Generation " + generation + ": start");

			if(collect && container instanceof EagerChromosomeSampleContainer) {
				for(Chromosome chrom : genotype.getChromosomes()) {
					info.setCollecting(chrom, true);
					info.setSampleContainer(chrom, container);
				}
				((EagerChromosomeSampleContainer)container).clearBuffers();
			}
			// next generation
			genotype.evolve();

			// result data
			champ = genotype.getFittestChromosome();
			if(collect){
				if(!(container instanceof EagerChromosomeSampleContainer)) {
					//LAZY sample collection
					List<Chromosome> topChroms = genotype.getKFittestChromosome(lazyK);
					for(int i = lazyK - 1; i >= 0; i--) {
						Chromosome chrom = topChroms.get(i);
						info.setCollecting(chrom, true);
						info.setSampleContainer(chrom, container);
						genotype.evaluateChrom(chrom);
						info.setCollecting(chrom, false);
					}
				}
				else
					((EagerChromosomeSampleContainer)container).aggregateSamples();
			}

			adjustedFitness = (maxFitness > 0 ? (double) champ
					.getFitnessValue() / maxFitness : champ.getFitnessValue());
			if (adjustedFitness >= thresholdFitness
					&& generationOfFirstSolution == -1)
				generationOfFirstSolution = generation;

			// generation finish
			Date generationEndDate = Calendar.getInstance().getTime();

			long durationMillis = generationEndDate.getTime()
					- generationStartDate.getTime();
			logger.info("Generation " + generation + ": end ["
					+ fmt.format(generationStartDate) + " - "
					+ fmt.format(generationEndDate) + "] [" + durationMillis
					+ "]");

			generation++;
		}

		//See if evolution is done
		if (generation >= numEvolutions || adjustedFitness >= targetFitness){
			config.getEventManager()
			.fireGeneticEvent(
					new GeneticEvent(GeneticEvent.RUN_COMPLETED_EVENT,
							genotype));
			logConclusion(generationOfFirstSolution, champ);
			Date runEndDate = Calendar.getInstance().getTime();
			long durationMillis = runEndDate.getTime() - runStartDate.getTime();
			logger.info("Run: end [" + fmt.format(runStartDate) + " - "
					+ fmt.format(runEndDate) + "] [" + durationMillis + "]");
			if(container instanceof EagerChromosomeSampleContainer)
				((EagerChromosomeSampleContainer)container).shutdown();
			if(fitnessFunc instanceof ConcurrentFitnessFunction)
				((ConcurrentFitnessFunction)fitnessFunc).shutdown();
		}

		return adjustedFitness;
	}

	/**
	 * The mapping is from the index of the feature to it's selected rank (or -1
	 * if it is not selected). The feature index is determined by sorting the
	 * innovation id's of the input neurons in a network in ascending order. For
	 * example the input neuron with the smallest innovation id in a network will
	 * be index 0 in our scheme (corresponding to key 0 in the resulting map).
	 * 
	 * @return the mapping currently in use by the chromosomes in this genotype.
	 */
	public Map<Integer, Integer> getActiveSubspace() {
		return selectedFeatures;
	}

	@Override
	public void setActiveSubspace(Map<Integer, Integer> mapping){
		//Figure out what changes need to happen:
		Set<Integer> additions = new LinkedHashSet<Integer>();
		Set<Integer> deletions = new LinkedHashSet<Integer>();

		for (Integer key : mapping.keySet()) {
			if (mapping.get(key) != SubspaceIdentification.UNUSED) {
				if (selectedFeatures.get(key) == SubspaceIdentification.UNUSED)
					additions.add(key);
			} else if (selectedFeatures.get(key) != SubspaceIdentification.UNUSED) {
				deletions.add(key);
			}
		}

		//Make any needed changes
		if(additions.size() != 0 || deletions.size() != 0){
			List<Chromosome> newChroms = new ArrayList<>();
			InputNeuronComparator comp = new InputNeuronComparator();
			List<Chromosome> chroms = genotype.getChromosomes();
			for(Chromosome chrom : chroms){
				//Get the input and output neurons
				@SuppressWarnings("unchecked")
				SortedSet<Allele> allAlleles = new TreeSet<Allele>(chrom.getAlleles());
				List<NeuronAllele> inputs = NeatChromosomeUtility.getNeuronList(allAlleles, NeuronType.INPUT);
				List<NeuronAllele> outputs = NeatChromosomeUtility.getNeuronList(allAlleles, NeuronType.OUTPUT);
				List<ConnectionAllele> allConns = NeatChromosomeUtility.getConnectionList(allAlleles);
				//Order the inputs in ascending order by id
				Collections.sort(inputs, comp);

				//Remove any connections from un-selected input nodes
				List<Long> toDelete = new ArrayList<>();
				for(Integer del : deletions)
					toDelete.add(inputs.get(del).getInnovationId()); //get the id of the corresponding feature
				allAlleles.removeAll(NeatChromosomeUtility.extractConnectionAllelesForSrcNeurons(allConns, toDelete));

				//Add connections (all pairwise connections between new sources and dests
				for(Integer add : additions)
					for(NeuronAllele out : outputs)
						allAlleles.add(config.newConnectionAllele(inputs.get(add).getInnovationId(), out.getInnovationId()));

				newChroms.add(new Chromosome(new ChromosomeMaterial(allAlleles, chrom.getPrimaryParentId(), chrom.getSecondaryParentId()), config.nextChromosomeId()));
			}
			genotype.replaceChromosomes(newChroms);
		}
		//Copy over the current set of selected features
		for(Integer key : mapping.keySet())
			selectedFeatures.put(key, mapping.get(key));
	}

	@Override
	public void setSampleCollection(boolean collect) {
		this.collect = collect;
	}

	@Override
	public void setSampleContainer(SampleContainer container) {
		this.container = container;
	}

	@Override
	public boolean canRevert() {
		return reversionSelectedFeatures != null && !reversionSelectedFeatures.equals(selectedFeatures);
	}

	@Override
	public Map<Integer, Integer> revert() {
		if(reversionSelectedFeatures != null){
			List<Chromosome> chroms = genotype.getChromosomes();
			Chromosome best = genotype.getFittestChromosome();
			//Replace any of the less promising chromosomes with the previous best
			int i = chroms.size()-1;
			while(i >= 0 && chroms.get(i).equals(best))
				--i;
			if(i == -1) //They were all the best somehow, just replace one
				i = 0;
			chroms.set(i, reversionBest);
			setActiveSubspace(reversionSelectedFeatures);
			reversionSelectedFeatures = null;
		}
		return reversionSelectedFeatures;
	}

	@Override
	public void setReversionPoint() {
		reversionSelectedFeatures = new HashMap<Integer, Integer>(selectedFeatures);
		reversionBest = genotype.getFittestChromosome();
	}

	/**
	 * This class is used to order NeuronAllele objects by their innovation ids.
	 * NeuronAllele a is smaller than NeuronAllele b if a's innovation id is
	 * less than b's.
	 * @author sloscal1
	 *
	 */
	private class InputNeuronComparator implements Comparator<NeuronAllele>{
		@Override
		public int compare(NeuronAllele o1, NeuronAllele o2) {
			return (int)(o1.getInnovationId() - o2.getInnovationId());
		}
	}

	/**
	 * Perform a single run.
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		// run start time
		Date runStartDate = Calendar.getInstance().getTime();
		logger.info( "Run: start" );
		DateFormat fmt = new SimpleDateFormat( "HH:mm:ss" );

		// initialize result data
		int generationOfFirstSolution = -1;
		champ = genotype.getFittestChromosome();
		//System.out.println("MAX FIT = " + maxFitness);
		double adjustedFitness = ( maxFitness > 0 ? champ.getFitnessValue() / maxFitness : champ
				.getFitnessValue() );

		for ( int generation = 0; ( generation < numEvolutions && adjustedFitness < targetFitness ); ++generation ) {
			// generation start time
			Date generationStartDate = Calendar.getInstance().getTime();
			//startTime = System.currentTimeMillis();
			logger.info( "Generation " + generation + ": start" );

			// next generation
			genotype.evolve();

			// result data
			champ = genotype.getFittestChromosome();
			adjustedFitness = ( maxFitness > 0 ? (double) champ.getFitnessValue() / maxFitness : champ
					.getFitnessValue() );
			if ( adjustedFitness >= thresholdFitness && generationOfFirstSolution == -1 )
				generationOfFirstSolution = generation;

			// generation finish
			Date generationEndDate = Calendar.getInstance().getTime();

			long durationMillis = generationEndDate.getTime() - generationStartDate.getTime();
			logger.info( "Generation " + generation + ": end [" + fmt.format( generationStartDate )
			+ " - " + fmt.format( generationEndDate ) + "] [" + durationMillis + "]" );
		}

		// run finish
		config.getEventManager().fireGeneticEvent(
				new GeneticEvent( GeneticEvent.RUN_COMPLETED_EVENT, genotype ) );
		logConclusion( generationOfFirstSolution, champ );
		Date runEndDate = Calendar.getInstance().getTime();
		long durationMillis = runEndDate.getTime() - runStartDate.getTime();
		logger.info( "Run: end [" + fmt.format( runStartDate ) + " - " + fmt.format( runEndDate )
		+ "] [" + durationMillis + "]" );
	}
}
