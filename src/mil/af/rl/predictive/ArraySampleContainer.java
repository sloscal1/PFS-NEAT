package mil.af.rl.predictive;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * WARNING:  This class uses references to all saved samples, making it extremely
 * volatile.  Defensive copies should be made everywhere in here, but for now,
 * I'm playing it risky.
 * 
 * @author sloscal1
 *
 */
public class ArraySampleContainer implements SampleContainer {
	/** This is the simple storage mechanism */
	private List<Sample> samples = new ArrayList<Sample>();
	private int maxSamples = -1;
	private Random rand = new Random(151416141L);
	
	public ArraySampleContainer() {
		this.maxSamples = -1;
	}
	public ArraySampleContainer(int maxSamples) {
		this.maxSamples = maxSamples;
	}

	@Override
	public void addSample(Sample sample) {
		
			samples.add(sample);
			if(maxSamples >0)
			{
				while(this.samples.size()>maxSamples)
					samples.remove(rand.nextInt(this.samples.size()));
			}
	}

	@Override
	public void addSamples(Collection<Sample> samples) {
		
		this.samples.addAll(samples);
		if(maxSamples >0)
		{
			while(this.samples.size()>maxSamples)
				this.samples.remove(rand.nextInt(this.samples.size()));
		}
	}
	
	
	@Override
	public void addSamples(Sample[] samples){
		for(Sample s : samples)
			this.samples.add(s);
		
		if(maxSamples >0)
		{
			while(this.samples.size()>maxSamples)
				this.samples.remove(rand.nextInt(this.samples.size()));
		}
	}

	@Override
	public void clearSamples() {
		samples.clear();
	}

	@Override
	public Collection<Sample> getSamples() {
		return Collections.unmodifiableCollection(samples);
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
	public void cullSamples() {
		int toRemove = maxSamples - samples.size();
		for(int i = 0; i < toRemove; ++i){
			while(this.samples.size()>maxSamples)
				this.samples.remove(rand.nextInt(this.samples.size()));
		}
	}
}
