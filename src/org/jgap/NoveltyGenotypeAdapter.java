/*
 * Copyright 2001-2003 Neil Rotstan Copyright (C) 2004 Derek James and Philip Tucker
 * 
 * This file is part of JGAP.
 * 
 * JGAP is free software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser Public License as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * JGAP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with JGAP; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * Modified on Feb 3, 2003 by Philip Tucker
 * Modified on May 22, 2014 by Steven Loscalzo (altered to support feature selection notions)
 */
package org.jgap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jgap.event.GeneticEvent;

import mil.af.rl.novelty.MultiObj;
import mil.af.rl.predictive.PFSInfo;

/**
 * Genotypes are fixed-length populations of chromosomes. As an instance of a
 * <code>Genotype</code> is evolved, all of its <code>Chromosome</code> objects
 * are also evolved. A <code>Genotype</code> may be constructed normally,
 * whereby an array of <code>Chromosome</code> objects must be provided, or the
 * static <code>randomInitialGenotype()</code> method can be used to generate a
 * <code>Genotype</code> with a randomized <code>Chromosome</code> population.
 * Changes made by Tucker and James for <a
 * href="http://anji.sourceforge.net/">ANJI </a>:
 * <ul>
 * <li>added species</li>
 * <li>modified order of operations in <code>evolve()</code></li>
 * <li>added <code>addChromosome*()</code> methods</li>
 * </ul>
 * 
 * 
 */
public class NoveltyGenotypeAdapter extends Genotype implements Serializable {

	private static final long serialVersionUID = -767703554397573136L;

	/**
	 * The current active Configuration instance.
	 */
	protected Configuration activeConfiguration;

	/**
	 * Species that makeup this Genotype's population.
	 */
	protected List<Specie> species = new ArrayList<Specie>();

	/**
	 * Chromosomes that makeup this Genotype's population.
	 */
	protected List<Chromosome> chromosomes = new ArrayList<Chromosome>();
	private PFSInfo info;
	//_SL_ 20151130 added to support changing the fitness to relfect other objective funcitons.
	private MultiObj mo;

	/**
	 * This constructor is used for random initial Genotypes. Note that the
	 * Configuration object must be in a valid state when this method is
	 * invoked, or a InvalidconfigurationException will be thrown.
	 * 
	 * @param a_activeConfiguration
	 *            The current active Configuration object.
	 * @param a_initialChromosomes
	 *            <code>List</code> contains Chromosome objects: The Chromosome
	 *            population to be managed by this Genotype instance.
	 * @throws IllegalArgumentException
	 *             if either the given Configuration object or the array of
	 *             Chromosomes is null, or if any of the Genes in the array of
	 *             Chromosomes is null.
	 * @throws InvalidConfigurationException
	 *             if the given Configuration object is in an invalid state.
	 */
	public NoveltyGenotypeAdapter(Configuration a_activeConfiguration,
			List<Chromosome> a_initialChromosomes, MultiObj obj, PFSInfo info)
					throws InvalidConfigurationException {
		super(a_activeConfiguration, a_initialChromosomes); //This is basically ignored.
		// Sanity checks: Make sure neither the Configuration, the array
		// of Chromosomes, nor any of the Genes inside the array are null.
		// ---------------------------------------------------------------
		if (a_activeConfiguration == null)
			throw new IllegalArgumentException(
					"The Configuration instance may not be null.");

		if (a_initialChromosomes == null)
			throw new IllegalArgumentException(
					"The array of Chromosomes may not be null.");

		for (int i = 0; i < a_initialChromosomes.size(); i++) {
			if (a_initialChromosomes.get(i) == null)
				throw new IllegalArgumentException(
						"The Chromosome instance at e1 "
								+ i
								+ " of the array of "
								+ "Chromosomes is null. No instance in this array may be null.");
		}
		this.info = info;
		this.mo = obj;
		activeConfiguration = a_activeConfiguration;

		// First arg of adjust and add must be the same
		adjustChromosomeList(a_initialChromosomes, a_activeConfiguration.getPopulationSize());
		addChromosomes(a_initialChromosomes);
	}

	public NoveltyGenotypeAdapter(Genotype genotype, MultiObj mo, PFSInfo info) throws InvalidConfigurationException {
		super(genotype.m_activeConfiguration, genotype.m_chromosomes); //This is basically ignored.
		species = genotype.m_species;
		activeConfiguration = genotype.m_activeConfiguration;
		this.info = info;
		this.mo = mo;
		for(Object obj : genotype.getChromosomes()){
			addChromosome((Chromosome)obj);
		}
	}


	protected void adjustChromosomeMaterialList(List<ChromosomeMaterial> chroms, int targetSize) {
		while (chroms.size() < targetSize) {
			int idx = chroms.size() % chroms.size();
			ChromosomeMaterial orig = chroms.get(idx);
			ChromosomeMaterial clone = orig
					.clone(orig.getPrimaryParentId());
			chroms.add(clone);
		}
		while (chroms.size() > targetSize) {
			// remove from end of list
			chroms.remove(chroms.size() - 1);
		}
	}

	/**
	 * adjust chromosome list to fit population size; first, clone population
	 * (starting at beginning of list) until we reach or exceed pop. size or
	 * trim excess (from end of list)
	 * 
	 * @param chroms
	 *            <code>List</code> contains <code>Chromosome</code> objects
	 * @param targetSize
	 */
	protected void adjustChromosomeList(List<Chromosome> chroms, int targetSize) {
		while (chroms.size() < targetSize) {
			int idx = chroms.size() % chroms.size();
			Chromosome orig = chroms.get(idx);
			Chromosome clone = new Chromosome(orig.cloneMaterial(),
					activeConfiguration.nextChromosomeId());

			chroms.add(clone);
			//				info.setFeatureMap(clone, info.getFeatureMap(orig));
		}
		while (chroms.size() > targetSize) {
			// remove from end of list
			Chromosome extinct = chroms.remove(chroms.size() - 1);
			info.removeInfo(extinct);
		}
	}

	/**
	 * @param chromosomes
	 *            <code>Collection</code> contains Chromosome objects
	 * @see Genotype#addChromosome(Chromosome)
	 */
	@Override
	protected void addChromosomes(Collection chromosomes) {
		Iterator<Chromosome> iter = chromosomes.iterator();
		while (iter.hasNext()) {
			Chromosome c = iter.next();
			addChromosome(c);
		}
	}

	/**
	 * @param chromosomeMaterial
	 *            <code>Collection</code> contains ChromosomeMaterial objects
	 * @see Genotype#addChromosomeFromMaterial(ChromosomeMaterial)
	 */
	@Override
	protected void addChromosomesFromMaterial(
			Collection chromosomeMaterial) {
		Iterator<ChromosomeMaterial> iter = chromosomeMaterial.iterator();
		while (iter.hasNext()) {
			ChromosomeMaterial cMat = iter.next();
			addChromosomeFromMaterial(cMat);
		}
	}

	/**
	 * @param cMat
	 *            chromosome material from which to construct new chromosome
	 *            object
	 * @see Genotype#addChromosome(Chromosome)
	 */
	protected void addChromosomeFromMaterial(ChromosomeMaterial cMat) {
		Chromosome chrom = new Chromosome(cMat,
				activeConfiguration.nextChromosomeId());
		addChromosome(chrom);
	}

	/**
	 * add chromosome to population and to appropriate specie
	 * 
	 * @param chrom
	 */
	protected void addChromosome(Chromosome chrom) {
		//
		if(chromosomes == null)
			return;
		
		BehaviorChromosome bc = new BehaviorChromosome(chrom);
		chromosomes.add(bc);
		
		//		if(chromosomes.size() != 0)
		//			info.setFeatureMap(chrom, info.getFeatureMap(chromosomes.get(0)));
		// specie collection
		boolean added = false;
		Specie specie = null;
		Iterator<Specie> iter = species.iterator();
		while (iter.hasNext() && !added) {
			specie = iter.next();
			if (specie.match(bc)) {
				specie.add(bc);
				added = true;
			}
		}
		if (!added) {
			specie = new Specie(activeConfiguration.getSpeciationParms(),
					bc);
			species.add(specie);
		}
	}

	/**
	 * @return List contains Chromosome objects, the population of Chromosomes.
	 */
	public synchronized List<Chromosome> getChromosomes() {
		return chromosomes;
	}

	/**
	 * @return List contains Specie objects
	 */
	public synchronized List<Specie> getSpecies() {
		return species;
	}

	/**
	 * Retrieves the Chromosomes in the population with the highest fitness
	 * values.
	 * 
	 * @param k
	 *            Maximum number of Chromosomes to retrieve
	 * @return List of up to k Chromosome with the k highest fitness values
	 */
	public synchronized List<Chromosome> getKFittestChromosome(int k) {
		LinkedList<Chromosome> fittestChromosomes = new LinkedList<Chromosome>();

		ListIterator<Chromosome> iter = getChromosomes().listIterator();
		while (iter.hasNext()) {
			Chromosome chrom = iter.next();
			int i = 0;
			boolean found = false;
			for (i = 0; i < fittestChromosomes.size() && !found; i++) {
				if (chrom.getFitnessValue() >= fittestChromosomes.get(i).getFitnessValue()) {
					fittestChromosomes.add(i, chrom);
					found = true;

				}
			}

			if (fittestChromosomes.size() > k) {
				fittestChromosomes.removeLast();
			} else if (i < k && !found) {
				fittestChromosomes.add(chrom);
			}
		}

		return fittestChromosomes;
	}

	/**
	 * Retrieves the Chromosome in the population with the highest fitness
	 * value.
	 * 
	 * @return The Chromosome with the highest fitness value, or null if there
	 *         are no chromosomes in this Genotype.
	 */
	public synchronized Chromosome getFittestChromosome() {
		if (getChromosomes().isEmpty())
			return null;

		// Set the highest fitness value to that of the first chromosome.
		// Then loop over the rest of the chromosomes and see if any has
		// a higher fitness value.
		// --------------------------------------------------------------
		Iterator<Chromosome> iter = getChromosomes().iterator();
		Chromosome fittestChromosome = iter.next();
		int fittestValue = fittestChromosome.getFitnessValue();

		while (iter.hasNext()) {
			Chromosome chrom = iter.next();
			if (chrom.getFitnessValue() > fittestValue) {
				fittestChromosome = chrom;
				fittestValue = fittestChromosome.getFitnessValue();
			}
		}

		return fittestChromosome;
	}

	public synchronized double getAverageFitness() {
		double sum = 0;
		int count = 0;
		Iterator<Chromosome> iter = getChromosomes().iterator();
		iter.next(); // Get it fired up

		while (iter.hasNext()) {
			Chromosome chrom = iter.next();
			sum += chrom.getFitnessValue();
			count++;
		}

		return sum / (double) count;
	}

	/**
	 * Kind of a replay thing to get the best chromosome to gather samples in
	 * the environment.
	 */
	public void evaluateBest() {
		BulkFitnessFunction bulkFunction = activeConfiguration
				.getBulkFitnessFunction();
		List<Chromosome> chrom = new ArrayList<Chromosome>(1);
		chrom.add(getFittestChromosome());
		bulkFunction.evaluate(chrom);
	}

	/**
	 * Kind of a replay thing to get the specified chromosome to gather samples in
	 * the environment.
	 */
	public void evaluateChrom(Chromosome c) {
		BulkFitnessFunction bulkFunction = activeConfiguration
				.getBulkFitnessFunction();
		List<Chromosome> chrom = new ArrayList<Chromosome>(1);
		chrom.add(c);
		bulkFunction.evaluate(chrom);
	}

	/**
	 * Performs one generation cycle, evaluating fitness, selecting survivors,
	 * repopulting with offspring, and mutating new population. This is a
	 * modified version of original JGAP method which changes order of
	 * operations and splits <code>GeneticOperator</code> into
	 * <code>ReproductionOperator</code> and <code>MutationOperator</code>. New
	 * order of operations:
	 * <ol>
	 * <li>assign <b>fitness </b> to all members of population with
	 * <code>BulkFitnessFunction</code> or <code>FitnessFunction</code></li>
	 * <li><b>select </b> survivors and remove casualties from population</li>
	 * <li>re-fill population with offspring via <b>reproduction </b> operators</li>
	 * <li><b>mutate </b> offspring (note, survivors are passed on un-mutated)</li>
	 * </ol>
	 * Genetic event <code>GeneticEvent.GENOTYPE_EVALUATED_EVENT</code> is fired
	 * between steps 2 and 3. Genetic event
	 * <code>GeneticEvent.GENOTYPE_EVOLVED_EVENT</code> is fired after step 4.
	 */
	@SuppressWarnings("deprecation")
	public synchronized void evolve() {
		// m_activeConfiguration.lockSettings();

		// If a bulk fitness function has been provided, then convert the
		// working pool to an array and pass it to the bulk fitness
		// function so that it can evaluate and assign fitness values to
		// each of the Chromosomes.
		// --------------------------------------------------------------
		// System.out.println("Evaluating");
		BulkFitnessFunction bulkFunction = activeConfiguration
				.getBulkFitnessFunction();
		if (bulkFunction != null) {
			bulkFunction.evaluate(chromosomes);
			//			((ConcurrentLearner)bulkFunction).printOutputs();
		} else {
			// Refactored such that Chromosome does not need a reference to
			// Configuration. Left his
			// in for backward compatibility, but it makes more sense to use
			// BulkFitnessFunction
			// now.
			FitnessFunction function = activeConfiguration
					.getFitnessFunction();
			Iterator<Chromosome> it = chromosomes.iterator();
			while (it.hasNext()) {
				Chromosome c = it.next();
				c.setFitnessValue(function.getFitnessValue(c));
			}
		}
		//_SL 20151130
		//The fitness at this point reflects actual objective function fitness.
		//We want the evolutionary criterion to reflect novelty or some multi-objective combination.
		//Store the fitness only information
		Map<Long, Integer> fitnessOnly = new HashMap<>();
		for(Chromosome ch : chromosomes)
			fitnessOnly.put(ch.getId(), ch.getFitnessValue());
		//Update the "fitness" with the newly computed values
		Map<Long, Integer> multi = mo.measureObjective(chromosomes);
		for(Chromosome chrom : chromosomes)
			chrom.setFitnessValue(multi.get(chrom.getId()));

		// _SL_ 6/27/11 Refactored to disconnect evolution from fitness
		// evaluation
		onlyEvolve();

		//Now put the fitness back so that other Genotype operations are not affected:
		for(Chromosome chrom : chromosomes){
			//The mutations would result in the new chromosomes not having a fitness, use their primary parent instead
			// _DB_ 02/16/16 set fitness to default if no current fitness to avoid null pointer if no parent id
			//int fitness = fitnessOnly.containsKey(chrom.getId()) ? fitnessOnly.get(chrom.getId()) : fitnessOnly.get(chrom.getPrimaryParentId()); 
			int fitness = fitnessOnly.containsKey(chrom.getId()) ? fitnessOnly.get(chrom.getId()) : -1;
			chrom.setFitnessValue(fitness);
		}
		//_SL_ 20151202 pulled out of onlyEvolve so that proper fitness is logged.
		// Fire an event to indicate we've performed an evolution.
		// -------------------------------------------------------
		activeConfiguration.getEventManager()
		.fireGeneticEvent(
				new GeneticEvent(
						GeneticEvent.GENOTYPE_EVOLVED_EVENT, this));
	}

	/**
	 * Handles the evolution of the chromosomes that make up this genotype. The
	 * chromosomes are assumed to have already had their fitness values set by
	 * some prior step.
	 */
	public void onlyEvolve() {
		// System.out.println("Finished Evaluating");
		// Fire an event to indicate we've evaluated all chromosomes.
		// -------------------------------------------------------
		activeConfiguration.getEventManager().fireGeneticEvent(
				new GeneticEvent(GeneticEvent.GENOTYPE_EVALUATED_EVENT, this));
		// System.out.println("Natural Selection");
		// Select chromosomes to survive.
		// ------------------------------------------------------------
		NaturalSelector selector = activeConfiguration.getNaturalSelector();
		selector.add(activeConfiguration, chromosomes);
		chromosomes = selector.select(activeConfiguration);
		selector.empty();

		// Repopulate the population of species and chromosomes with those
		// selected
		// by the natural selector, and cull species down to contain only
		// remaining
		// chromosomes.
		Iterator<Specie> speciesIter = species.iterator();
		while (speciesIter.hasNext()) {
			Specie s = speciesIter.next();
			s.cull(chromosomes);
			if (s.isEmpty())
				speciesIter.remove();
		}

		// Fire an event to indicate we're starting genetic operators. Among
		// other things this allows for RAM conservation.
		// -------------------------------------------------------
		activeConfiguration.getEventManager().fireGeneticEvent(
				new GeneticEvent(
						GeneticEvent.GENOTYPE_START_GENETIC_OPERATORS_EVENT,
						this));

		// Execute Reproduction Operators.
		// -------------------------------------
		Iterator<ReproductionOperator> iterator = activeConfiguration
				.getReproductionOperators().iterator();
		/*
		 * The following code is dangerous. The list offspring is supposed to be
		 * ChromosomeMaterial, however, it might be used in the
		 * adjustChromosomeList method, and in that case it will be a list of
		 * Chromosome. The two classes are not related and should not be used in
		 * this manner. -SL _SL_ 8/11/10 FIXED (branched adjustChromosomeList to
		 * accept either case)
		 */
		// System.out.println("Reproducing");
		try {
			List<ChromosomeMaterial> offspring = new ArrayList<ChromosomeMaterial>();
			while (iterator.hasNext()) {
				ReproductionOperator operator = iterator.next();
				operator.reproduce(activeConfiguration, species, offspring);
			}

			// System.out.println("Mutating");
			// Execute Mutation Operators.
			// -------------------------------------
			Iterator<MutationOperator> mutOpIter = activeConfiguration
					.getMutationOperators().iterator();
			while (mutOpIter.hasNext()) {
				MutationOperator operator = mutOpIter.next();
				operator.mutate(activeConfiguration, offspring);
			}

			// in case we're off due to rounding errors
			Collections.shuffle(offspring,
					activeConfiguration.getRandomGenerator());
			adjustChromosomeMaterialList(
					offspring,
					activeConfiguration.getPopulationSize()
					- chromosomes.size());

			// add offspring
			// ------------------------------
			addChromosomesFromMaterial(offspring);

			// Fire an event to indicate we're starting genetic operators. Among
			// other things this allows for RAM conservation.
			// -------------------------------------------------------
			activeConfiguration
			.getEventManager()
			.fireGeneticEvent(
					new GeneticEvent(
							GeneticEvent.GENOTYPE_FINISH_GENETIC_OPERATORS_EVENT,
							this));
		} catch (InvalidConfigurationException ex) {
			throw new RuntimeException("bad config", ex);
		}
	}

	/**
	 * @return <code>String</code> representation of this <code>Genotype</code>
	 *         instance.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		Iterator<Chromosome> iter = chromosomes.iterator();
		while (iter.hasNext()) {
			Chromosome chrom = iter.next();
			buffer.append(chrom.toString());
			buffer.append(" [");
			buffer.append(chrom.getFitnessValue());
			buffer.append(']');
			buffer.append('\n');
		}

		return buffer.toString();
	}

	/**
	 * Convenience method that returns a newly constructed Genotype instance
	 * configured according to the given Configuration instance. The population
	 * of Chromosomes will created according to the setup of the sample
	 * Chromosome in the Configuration object, but the gene values (alleles)
	 * will be set to random legal values.
	 * <p>
	 * Note that the given Configuration instance must be in a valid state at
	 * the time this method is invoked, or an InvalidConfigurationException will
	 * be thrown.
	 * 
	 * @param a_activeConfiguration
	 * @return A newly constructed Genotype instance.
	 * @throws InvalidConfigurationException
	 *             if the given Configuration instance not in a valid state.
	 */
	public static NoveltyGenotypeAdapter randomInitialGenotype(
			Configuration a_activeConfiguration, MultiObj noveltyObj, PFSInfo info)
					throws InvalidConfigurationException {
		if (a_activeConfiguration == null) {
			throw new IllegalArgumentException(
					"The Configuration instance may not be null.");
		}
		a_activeConfiguration.lockSettings();

		// Create an array of chromosomes equal to the desired size in the
		// active Configuration and then populate that array with Chromosome
		// instances constructed according to the setup in the sample
		// Chromosome, but with random gene values (alleles). The Chromosome
		// class' randomInitialChromosome() method will take care of that for
		// us.
		// ------------------------------------------------------------------
		int populationSize = a_activeConfiguration.getPopulationSize();
		List<Chromosome> chroms = new ArrayList<Chromosome>(populationSize);

		for (int i = 0; i < populationSize; i++) {
			ChromosomeMaterial material = ChromosomeMaterial
					.randomInitialChromosomeMaterial(a_activeConfiguration);
			chroms.add(new Chromosome(material, a_activeConfiguration
					.nextChromosomeId()));
			//I think it should be null map since this is only used in the beginning
		}

		return new NoveltyGenotypeAdapter(a_activeConfiguration, chroms, noveltyObj, info);
	}

	/**
	 * Compares this Genotype against the specified object. The result is true
	 * if the argument is an instance of the Genotype class, has exactly the
	 * same number of chromosomes as the given Genotype, and, for each
	 * Chromosome in this Genotype, there is an equal chromosome in the given
	 * Genotype. The chromosomes do not need to appear in the same order within
	 * the populations.
	 * 
	 * @param other
	 *            The object to compare against.
	 * @return true if the objects are the same, false otherwise.
	 */
	public boolean equals(Object other) {
		try {
			// First, if the other Genotype is null, then they're not equal.
			// -------------------------------------------------------------
			if (other == null) {
				return false;
			}

			Genotype otherGenotype = (Genotype) other;

			// First, make sure the other Genotype has the same number of
			// chromosomes as this one.
			// ----------------------------------------------------------
			if (chromosomes.size() != otherGenotype.m_chromosomes.size()) {
				return false;
			}

			// Next, prepare to compare the chromosomes of the other Genotype
			// against the chromosomes of this Genotype. To make this a lot
			// simpler, we first sort the chromosomes in both this Genotype
			// and the one we're comparing against. This won't affect the
			// genetic algorithm (it doesn't care about the order), but makes
			// it much easier to perform the comparison here.
			// --------------------------------------------------------------
			Collections.sort(chromosomes);
			Collections.sort(otherGenotype.m_chromosomes);

			Iterator<Chromosome> iter = chromosomes.iterator();
			Iterator<Chromosome> otherIter = otherGenotype.m_chromosomes
					.iterator();
			while (iter.hasNext() && otherIter.hasNext()) {
				Chromosome chrom = iter.next();
				Chromosome otherChrom = otherIter.next();
				if (!(chrom.equals(otherChrom))) {
					return false;
				}
			}

			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns the active configuration
	 * 
	 * @return May be null
	 */
	// _SL_ 1/16/11 Needed access this to evaluate the policies out of band with
	// the actual evolution.
	public Configuration getActiveConfiguration() {
		return activeConfiguration;
	}

	/**
	 * Replace the genotypes current set of chromosomes with a new set.  This method is
	 * used since existing chromosomes cannot have their material changed which needs to
	 * happen as a result of feature selection.
	 * 
	 * @param newChroms
	 */
	public void replaceChromosomes(List<Chromosome> newChroms) {
		if(newChroms == null || newChroms.size() != chromosomes.size())
			throw new IllegalArgumentException("Cannot replace chromosomes with different length set");
		chromosomes = new ArrayList<Chromosome>();
		for(Chromosome nC : newChroms)
			addChromosome(nC);
		//		for(int i = 0; i < newChroms.size(); ++i){
		//			Specie specie = chromosomes.get(i).getSpecie();
		//			Chromosome newChrom = newChroms.get(i);
		//			newChrom.setSpecie(specie);
		//			if(!specie.match(newChrom))
		//				specie.add(newChrom);
		//		}

	}
}
