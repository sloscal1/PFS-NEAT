package mil.af.rl.predictive;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import mil.af.rl.predictive.Sample;
import mil.af.rl.predictive.SampleContainer;
import mil.af.rl.problem.RLProblem;

import com.anji.util.Configurable;
import com.anji.util.Properties;

public class FailureSeparatingSampleContainer implements SampleContainer, Configurable {
	private final static double EPSILON = 1e-6;
	private List<Sample> bin1 = new ArrayList<Sample>();
	private List<Sample> bin1Padding = new ArrayList<Sample>();
	private List<Sample> bin2 = new ArrayList<Sample>();
	private List<Sample> bin2Padding = new ArrayList<Sample>();
	private int maxSamples;
	private Random rand;

	@Override
	public void clearSamples() {
		bin1.clear();
		bin2.clear();
		bin1Padding.clear();
		bin2Padding.clear();
	}

	@Override
	public void cullSamples() {
		//Determine amount of samples to remove from bin1
		removeSamples(bin1, bin1Padding);
		//Repeat the process for bin2
		removeSamples(bin2, bin2Padding);
	}

	private void removeSamples(List<Sample> bin, List<Sample> padding){
		int toRemove = Math.max((bin.size() + padding.size()) - maxSamples/2, 0);
		//Try and take it all from the padding:
		if(toRemove <= padding.size()){
			int end = padding.size() - toRemove;
			for(int i = padding.size() - 1; i >= end; --i)
				padding.remove(end);
		}else{
			toRemove -= padding.size();
			padding.clear();
			//Remove randomly from the bin1 samples:
			for(int i = 0; i < toRemove; ++i)
				bin.remove(rand.nextInt(bin.size()));
		}
	}

	@Override
	public void addSample(Sample sample) {
		if(sample != null){
			double r = sample.getReward();
			double min = Double.MAX_VALUE;
			while(bin1.size() != 0 && bin1.get(0) == null)
				bin1.remove(0);
			if(bin1.size() != 0)
				min = bin1.get(0).getReward();
			if(r < min + EPSILON){
				//Is it a new distinct min?
				if(r < min - EPSILON){
					//Shift bin1 to bin2:
					bin2.addAll(bin1);
					bin2Padding.clear();
					//Make this sample the only one in bin1
					bin1.clear();
					bin1Padding.clear();
				}
				bin1.add(sample);
			}
			else
				//Belongs in the larger bin
				bin2.add(sample);
			//Prevent the sample set from growing too large.
			if(bin1.size() + bin1Padding.size() + bin2.size() + bin2Padding.size() > maxSamples*2)
				cullSamples();	
		}
	}

	@Override
	public void addSamples(Collection<Sample> samples) {
		for(Sample s : samples)
			addSample(s);
	}

	@Override
	public void addSamples(Sample[] samples) {
		addSamples(Arrays.asList(samples));
	}

	@Override
	public Collection<Sample> getSamples() {
		//Balance the sample sets:
		cullSamples();
		//See which is the smaller bin:
		int bin1Size = bin1.size() + bin1Padding.size();
		int bin2Size = bin2.size() + bin2Padding.size();
		List<Sample> small = bin1;
		List<Sample> smallPadding = bin1Padding;
		//Correct the order if necessary
		if(bin1Size > bin2Size){
			small = bin2;
			smallPadding = bin2Padding;
		}
		//Balance them if necessary
		if(bin1Size != bin2Size)
			rebalance(small, smallPadding, Math.abs(bin1Size - bin2Size));
		//Return the concatenation of them:
		List<Sample> ret = new ArrayList<Sample>(bin1);
		ret.addAll(bin1Padding);
		ret.addAll(bin2);
		ret.addAll(bin2Padding);
		return ret;
	}

	private void rebalance(List<Sample> small, List<Sample> smallPadding, int toAdd) {
		int bound = small.size();
		for(int i = 0; i < toAdd; ++i)
			smallPadding.add(small.get(rand.nextInt(bound)));
	}

	@Override
	public void printSamples(String fileName) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		Collection<Sample> usableSamples = getSamples();
		//Get a sample to template the output format:
		Sample format = usableSamples.iterator().next();
		Iterator<Double> state = format.getState();
		for(int i = 0; state.hasNext(); ++i, state.next())
			out.print("s"+i+"\t");

		Iterator<Double> action = format.getAction();
		for(int i = 0; action.hasNext(); ++i, action.next())
			out.print("a"+i+"\t");

		state = format.getState();
		for(int i = 0; state.hasNext(); ++i, state.next())
			out.print("s'"+i+"\t");

		out.println("r");

		//Now print out all the data:
		for(Sample samp : usableSamples){
			Iterator<Double> s = samp.getState();
			while(s.hasNext())
				out.print(s.next()+"\t");
			Iterator<Double> a = samp.getAction();
			while(a.hasNext())
				out.print(a.next()+"\t");
			Iterator<Double> sP = samp.getStatePrime();
			while(sP.hasNext())
				out.print(sP.next()+"\t");
			out.println(samp.getReward());
		}
		out.close();
	}

	@Override
	public void init(Properties props) throws Exception {
		maxSamples = props.getIntProperty(MAX_SAMPLES);
		rand = new Random(props.getLongProperty(RLProblem.RANDOM_SEED_KEY));
	}

}
