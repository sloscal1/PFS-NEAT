/**
 * This package describes the bulk of the Predictive Feature Selection framework and the PFS-NEAT
 * algorithm in:
 * <br />
 * Loscalzo, S., Wright, R., and Yu, L. Predictive Feature Selection for Genetic Policy Search, to
 * appear in the Journal of Autonomous Agents and Multi-Agent Systems (JAAMAS) 2014.
 * <br />
 * Much of this package is devoted to interface definition and several concrete implementations of those
 * interfaces. The idea is that there is a SubspaceIdentification class that controls how the feature
 * selection will take place. A feature selection class would make use of a SampleContainer to provide
 * data, filtered through a SampleSelector. The samples are obtained by a learner that implements the
 * PredictiveLearner interface. The SubspaceIdentification class can be called according to the
 * Stagnation classes used. Let us define the interfaces and their implementing classes here:
 * <br />
 * Sample - the data structure that stores and controls access to a state, action, next state, reward
 * observation.
 * <ul>
 *   <li>ArraySample</li>
 * </ul>
 * SampleContainer
 * <ul>
 *   <li>ArraySampleContainer</li>
 *   <li>EagerChromosomeSampleContainer</li>
 *   <li>FailureSeparatingSampleContainer</li>
 * </ul>
 * Stagnation - the base interface of the StagnationDectorator family of classes.
 * <ul>
 *   <li>StagnationDecorate - enable a decorate pattern so that multiple stagnation thresholds may be
 *   used.</li>
 *   <li>TrendStagnation - population is stagnant if the fitness of the champion does not improve over
 *   some value in some number of generations.</li>
 *   <li>SlidingWindowStagnation - force stagnation not to be triggered for at least a window number of
 *   generations, and the window can grow or shrink via parameter.</li>
 * </ul>
 * SubspaceIdentification - the interface for feature selection algorithm in th PFS framework.
 * <ul>
 *   <li>WekaIncSubspaceIdentification - Use Weka's data mining library to provide feature selection
 *   algorithms</li>
 * </ul>
 */

package mil.af.rl.predictive;