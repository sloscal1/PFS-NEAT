package mil.af.rl.anji;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgap.Chromosome;

import com.anji.integration.AnjiActivator;
import com.anji.neat.ConnectionAllele;
import com.anji.neat.NeatChromosomeUtility;
import com.anji.neat.NeuronAllele;
import com.anji.neat.NeuronType;
import com.anji.nn.ActivationFunctionFactory;
import com.anji.nn.AnjiNet;
import com.anji.nn.CacheNeuronConnection;
import com.anji.nn.Neuron;
import com.anji.nn.NeuronConnection;

/**
 * This network simply allows for serialization of a neat chromosome for use in the RemoteLearner.
 * @author sloscal1
 *
 */
public class SerializableNetwork implements Serializable {
	/** Gen ID */
	private static final long serialVersionUID = 3243855105353212064L;
	private List<NeuronInfo> inputNeuronActivations;
	private List<NeuronInfo> outputNeuronActivations;
	private List<NeuronInfo> hiddenNeuronActivations;
	private List<ConnectionInfo> connectionInfos;
	private String chromIdName;
	
	private class NeuronInfo implements Serializable{
		/** Gen ID */
		private static final long serialVersionUID = -8285456243542641707L;
		private String activationFunctionName;
		private long id;
		
		public NeuronInfo(String activationFunctionName, long id){
			this.id = id;
			this.activationFunctionName = activationFunctionName;
		}
	}
	
	private class ConnectionInfo implements Serializable{
		/** Gen ID */
		private static final long serialVersionUID = 1952460584114991525L;
		private long id;
		private long srcId;
		private long destId;
		private double weight;
		
		public ConnectionInfo(long id, long srcId, long destId, double weight){
			this.id = id;
			this.srcId = srcId;
			this.destId = destId;
			this.weight = weight;
		}
	}
	
	@SuppressWarnings("unchecked")
	public SerializableNetwork(Chromosome chrom){		
		inputNeuronActivations = new ArrayList<>();
		outputNeuronActivations = new ArrayList<>();
		hiddenNeuronActivations = new ArrayList<>();
		List<NeuronAllele> allAlleles = NeatChromosomeUtility.getNeuronList(chrom.getAlleles());
		for(NeuronAllele na : allAlleles){
			List<NeuronInfo> cat = null;
			if(na.isType(NeuronType.INPUT))
				cat = inputNeuronActivations;
			else if(na.isType(NeuronType.OUTPUT))
				cat = outputNeuronActivations;
			else
				cat = hiddenNeuronActivations;
			NeuronInfo curr = new NeuronInfo(na.getActivationType().toString(),
														  na.getInnovationId());
			cat.add(curr);
		}
		
		connectionInfos = new ArrayList<>();
		List<ConnectionAllele> allConns = NeatChromosomeUtility.getConnectionList(chrom.getAlleles());
		for(ConnectionAllele ca : allConns){
			connectionInfos.add(new ConnectionInfo(ca.getInnovationId(), 
					ca.getSrcNeuronId(), ca.getDestNeuronId(), ca.getWeight()));
		}
		
		chromIdName = chrom.getId().toString();
	}
	
	public AnjiActivator getActivator(){
		ActivationFunctionFactory aff = ActivationFunctionFactory.getInstance();
		List<Neuron> inNeurons = new ArrayList<>();
		Map<Long, Neuron> neuronMap = new HashMap<>();
		for(NeuronInfo n : inputNeuronActivations){
			Neuron na = new Neuron(aff.get(n.activationFunctionName));
			na.setId(n.id);			
			inNeurons.add(na);
			neuronMap.put(n.id, na);
		}
		//Get the output neurons constructed again		
		List<Neuron> outNeurons = new ArrayList<>();
		for(NeuronInfo n : outputNeuronActivations){
			Neuron na = new Neuron(aff.get(n.activationFunctionName));
			na.setId(n.id);
			neuronMap.put(n.id, na);
			outNeurons.add(na);
		}
		//Get the hidden neurons constructed again
		for(NeuronInfo n : hiddenNeuronActivations){
			Neuron na = new Neuron(aff.get(n.activationFunctionName));
			na.setId(n.id);
			neuronMap.put(n.id, na);
		}
		//Get the connection alleles
		List<NeuronConnection> conns = new ArrayList<>();
		for(ConnectionInfo c : connectionInfos){
			NeuronConnection conn = new CacheNeuronConnection(neuronMap.get(c.srcId));
			conn.setId(c.id);
			conn.setWeight(c.weight);
			neuronMap.get(c.destId).addIncomingConnection(conn);
			conns.add(conn);
		}
		
		//TODO the recurrent cycles number is currently set to 1, but it should be something different.
		return new AnjiActivator(new AnjiNet(neuronMap.values(), inNeurons, outNeurons, conns, chromIdName), 1);
	}
}
