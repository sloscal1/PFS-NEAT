package mil.af.rl.anji;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import mil.af.rl.util.DoubleIndexPair;

import org.jgap.Allele;
import org.jgap.ChromosomeMaterial;
import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.jgap.MutationOperator;

import com.anji.integration.AnjiRequiredException;
import com.anji.neat.AddConnectionMutationOperator;
import com.anji.neat.ConnectionAllele;
import com.anji.neat.NeatConfiguration;
import com.anji.neat.NeuronAllele;
import com.anji.neat.NeuronType;
import com.anji.nn.ActivationFunctionType;
import com.anji.nn.RecurrencyPolicy;
import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * This class provides a functionally identical mutation operator as AddConnectionMutationOperator
 * with the added benefit of a polynomial complexity bound, as opposed to the
 * probable bound given by the former code.  I didn't do a thorough complexity
 * analysis, though I believe it to be O(N^2E) in the worst case.  I think there
 * is a much tighter bound in general.  
 * 
 * @author Steven Loscalzo
 *
 */
public class AddConnectionMutationOperatorNoInput extends MutationOperator
implements Configurable {

	/** properties key, add connection mutation rate */
	public static final String ADD_CONN_MUTATE_RATE_KEY = "add.connection.mutation.rate";
	/** default mutation rate */
	public static final float DEFAULT_MUTATE_RATE = 0.01f;

	private RecurrencyPolicy policy;

	@Override
	public void init(Properties props) throws Exception {
		setMutationRate( props.getFloatProperty( ADD_CONN_MUTATE_RATE_KEY,
				DEFAULT_MUTATE_RATE ) );
		policy = RecurrencyPolicy.load( props );

	}

	/**
	 * @see AddConnectionMutationOperator#AddConnectionMutationOperator(float)
	 */
	public AddConnectionMutationOperatorNoInput() {
		this( DEFAULT_MUTATE_RATE, RecurrencyPolicy.BEST_GUESS );
	}

	public AddConnectionMutationOperatorNoInput(float aMutationRate) {
		this( aMutationRate, RecurrencyPolicy.BEST_GUESS );
	}

	/**
	 * Creates new operator with specified recurrency policy.
	 * 
	 * @param aPolicy
	 * @see RecurrencyPolicy
	 */
	public AddConnectionMutationOperatorNoInput( RecurrencyPolicy aPolicy ) {
		this( DEFAULT_MUTATE_RATE, aPolicy );
	}

	/**
	 * Creates new operator with specified mutation rate and recurrency policy.
	 * 
	 * @param aMutationRate
	 * @param aPolicy
	 * @see RecurrencyPolicy
	 */
	public AddConnectionMutationOperatorNoInput( float aMutationRate, RecurrencyPolicy aPolicy ) {
		super( aMutationRate );
		policy = aPolicy;
	}

	private Configuration currentConfig;
	private boolean multiThread = false;
	private ExecutorService exec;
	private Long seedInit = 1351462414L;
	
	private class Operator implements Callable<Void>{
		private ChromosomeMaterial chrom;
		public Operator(ChromosomeMaterial chrom){
			this.chrom = chrom;
		}

		@Override
		public Void call() {
			Set<Allele> allelesToAdd = new HashSet<Allele>();
			Set<Allele> allelesToRemove = new HashSet<Allele>();
			try {
				mutate( currentConfig, chrom, allelesToAdd, allelesToRemove );
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			updateMaterial( chrom, allelesToAdd, allelesToRemove );
			return null;
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void mutate(Configuration config, List offspring)
			throws InvalidConfigurationException {
		List<ChromosomeMaterial> cmOffspring = (List<ChromosomeMaterial>)offspring;
		synchronized(seedInit){
			seedInit = 1351462414L;
		}
		
		if(!multiThread){
			ListIterator<ChromosomeMaterial> iter = cmOffspring.listIterator();
			long time = System.currentTimeMillis();
			while ( iter.hasNext() ) {
				ChromosomeMaterial material = iter.next();
				Set<Allele> allelesToAdd = new HashSet<Allele>();
				Set<Allele> allelesToRemove = new HashSet<Allele>();
				mutate( config, material, allelesToAdd, allelesToRemove);
				updateMaterial( material, allelesToAdd, allelesToRemove );
			}
			time = System.currentTimeMillis() - time;
		}
		if(multiThread){
			//Submit all the tasks
			List<Future<Void>> pending = new ArrayList<>();
			for(ChromosomeMaterial chrom : cmOffspring)
				pending.add(exec.submit(new Operator(chrom)));

			//need to make sure that all submitted tasks have completed.
			for(Future<Void> f : pending)
				try {
					if(f != null) f.get();
				} catch (InterruptedException | ExecutionException e) {
					//Oh well... just move on without these results.
				}
		}
	}
	
	/**
	 * This method adds random legal connections to the network according to the
	 * current recurrency policy.
	 *  
	 * @param jgapConfig configuration parameters
	 * @param neurons the neurons in the network
	 * @param currentConns the connections in the network
	 * @param allelesToAdd the Set of ConnectionAlleles which will be added to the network.
	 * @param toAdd the number of connections to make, maximum.  Negative values will
	 * be treated as 0.
	 * @param randSeed 
	 */
	private void addConnections(Configuration jgapConfig, List<NeuronAllele> neurons, SortedMap<Long, ConnectionAllele> currentConns,
			Set<Allele> allelesToAdd, int toAdd, int randSeed){
		if ( ( jgapConfig instanceof NeatConfiguration ) == false )
			throw new AnjiRequiredException( "com.anji.neat.NeatConfiguration" );
		NeatConfiguration config = (NeatConfiguration) jgapConfig;

		Map<Long, Node> network = new HashMap<Long, Node>();
		//O(N)
		for(NeuronAllele allele : neurons)
			network.put(allele.getInnovationId(), new Node(allele));
		

		//If there is no mutation possible, there is no work to be done
		if(getMutationRate() > 0){
			//make the adjacency graph
			//O(NE) to create graph

			for(ConnectionAllele conn : currentConns.values()){
				Node src = network.get(conn.getSrcNeuronId());
				Node dest = network.get(conn.getDestNeuronId());
				src.connectTo(dest);		//O(N)			
			}

			//Get a random order to assess the possible connections - they are
			//order dependent!
			List<DoubleIndexPair<Node, Node>> order = new ArrayList<DoubleIndexPair<Node, Node>>();
	
			Random rand = new Random(randSeed);
			
			float mutationProb = getMutationRate();
			int added = 0;
			//O(N^2E)

			for(Node src : network.values()){
				for(Node dest : network.values())
					order.add(new DoubleIndexPair<Node, Node>(src, dest));
			}
			Collections.shuffle(order, rand);

			//Actually do the edge adding
			for(int i = 0; added < toAdd && i < order.size(); ++i){
				Node src = order.get(i).getElement1();
				Node dest = order.get(i).getElement2();
				if(!src.isForbiddenTo(dest, policy) && rand.nextDouble() < mutationProb){  //A prior connection may render a possible one impossible
					ConnectionAllele newConn = config.newConnectionAllele(
							src.getId(),	dest.getId());
					newConn.setToRandomValue(rand);
					allelesToAdd.add(newConn);
					added++;
					//The network graph gets updated to reflect the new structure
					src.connectTo(dest);

				}
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected void mutate(Configuration jgapConfig, ChromosomeMaterial target,
			Set allelesToAdd, Set allelesToRemove)
					throws InvalidConfigurationException {
		List<NeuronAllele> neurons = NeatChromosomeUtility.getNeuronList(target.getAlleles());
		SortedMap<Long, ConnectionAllele> currentConns = NeatChromosomeUtility.getConnectionMap(target.getAlleles());

		//Add as many connections as dictated by the configuration parameters
		addConnections(jgapConfig, neurons, currentConns, allelesToAdd, Integer.MAX_VALUE, target.hashCode());
	}

	public void addSingleConnection( NeatConfiguration config, List<NeuronAllele> neuronList, SortedMap<Long, ConnectionAllele> conns,
			Set<Allele> allelesToAdd ) {
		//Only want to add the first possible connection
		int seed = 0;
		synchronized(seedInit){
			seed = seedInit.hashCode();
			seedInit *= 31;
		}
		addConnections(config, neuronList, conns, allelesToAdd, 1, seed);
	}

	/**
	 * This class is how the add connection class became more efficient.  A set of
	 * alleles that is about to be mutated is converted into a graph structure.
	 * This graph is composed of Nodes which know of their immediate parents and
	 * children to facilitate cycle detection.  This is used to figure out which
	 * connections are possible, and deterministically enables the mutations to 
	 * occur in polynomial time.
	 * 
	 * @author sloscal1
	 *
	 */
	private class Node implements Comparable<Node>{
		/** All parents of this node (empty if input node) */
		private Set<Node> parents = new HashSet<Node>();
		/** All children of this node (empty if output node) */
		private Set<Node> children = new HashSet<Node>();
		/** The childern that are 1 connection away from this node */
		private Set<Node> directChildren = new HashSet<Node>();
		/** The NeuronAllele this node models*/
		private NeuronAllele allele;
		/** Create a new Node object to represent the NeuronAllele with the given
		 * <code>allele</code>.
		 * @param allele must not be null
		 */
		public Node(NeuronAllele allele){
			this.allele = allele;
		}

		/**
		 * Get the id of the wrapped NeuronAllele - useful for printing and other
		 * unique identification purposes.
		 * @return
		 */
		public Long getId(){
			return allele.getInnovationId();
		}

		/**
		 * Connect <code>this</code> Node to the given <code>dest</code> Node.
		 * Updates the forbidden connections, and parents and children sets of the
		 * affected nodes.
		 * @param dest must not be null
		 */
		public void connectTo(Node dest){
			//Keep track of the true connections
			directChildren.add(dest);
			//Go up the line and add dest to the children set of all parents
			children.add(dest);
			children.addAll(dest.children);
			for(Node p : parents)
				p.children.addAll(children);

			//Go down the line and add this to the parent set of all children
			dest.parents.add(this);
			dest.parents.addAll(parents);
			for(Node c : dest.children)
				c.parents.addAll(dest.parents);
		}

		/**
		 * Checks if a connection between <code>this</code> Node and <code>dest</code>
		 * is a forbidden connection.
		 * @param dest must not be null.
		 * @param policy what is forbidden depends on the recurrency policy
		 * @return true if a connection between these nodes would result in a cycle.
		 */
		public boolean isForbiddenTo(Node dest, RecurrencyPolicy policy){
			long time = System.currentTimeMillis();
			boolean retVal = false;
			if(RecurrencyPolicy.DISALLOWED.equals(policy))
				retVal = equals(dest) || //No self
				directChildren.contains(dest) || //No duplicates
				allele.isType(NeuronType.OUTPUT) || //Would create loops
				dest.allele.isType(NeuronType.INPUT) || //would create loops
				this.allele.isType(NeuronType.INPUT) || //No unauthorized feature set expansions
				doesCauseLoop(dest);
			else
				retVal = directChildren.contains(dest) || 
				ActivationFunctionType.LINEAR.equals(dest.allele.getActivationType());
			time = System.currentTimeMillis() - time;
			return retVal;
		}

		/**
		 * Check if connecting <code>this</code> Node to the <code>potential</code> Node would
		 * result in a loop.
		 * 
		 * @param potential
		 * @return
		 */
		private boolean doesCauseLoop(Node potential){
			//Is potential one of your parents?  It will cause a loop
			boolean retVal = parents.contains(potential);
			//If any of the children are in your parent set, you will have a loop:
			for(Iterator<Node> iter = potential.children.iterator(); !retVal && iter.hasNext();){
				Node c = iter.next();
				//If one if pot's children is your parent, or you are already a child of potential
				//there will be a loop
				retVal = parents.contains(c) | equals(c);
			}
			return retVal;
		}

		@Override
		public int hashCode() {
			return getId().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Node && ((Node)obj).getId().equals(getId());
		}
		@Override
		public String toString() {
			StringBuilder ps = new StringBuilder();
			for(Node p : parents)
				ps.append(p.getId()+",");
			if(ps.length() > 0)
				ps.delete(ps.length()-1, ps.length()); //leave off the ,

			StringBuilder cs = new StringBuilder();
			for(Node c : children)
				cs.append(c.getId()+",");
			if(cs.length() > 0)
				cs.delete(cs.length()-1, cs.length());

			return "Node-"+getId()+"[parents="+ps.toString()+", children="+cs.toString()+"]";
		}

		@Override
		public int compareTo(Node o) {
			//There is no real natural ordering, just needed it to use Pair
			return 0;
		}
	}
}
