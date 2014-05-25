package mil.af.rl.predictive;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ArraySample implements Sample {
	private double[] allInfo;
	private SampleType type;
	private int batchId;
	private int stateSize;
	private int stateActionSize;
	
	public ArraySample(double[] state, double[] action, double reward, double[] nextState, SampleType type, int batchId){
		allInfo = new double[state.length*2+action.length+1];
		int pos = 0;
		for(double d : state)
			allInfo[pos++] = d;
		for(double d : action)
			allInfo[pos++] = d;
		for(double d : nextState)
			allInfo[pos++] = d;
		allInfo[pos] = reward;
		this.type = type;
		this.stateSize = state.length;
		this.stateActionSize = state.length + action.length;
		this.batchId = batchId;
	}
	
	public SampleType getType() {
		return type;
	}

	public Iterator<Double> getState() {
		return new BoundedIterator(0, stateSize);
	}

	public Iterator<Double> getAction() {
		return new BoundedIterator(stateSize, stateActionSize);
	}

	public Iterator<Double> getStateAction() {
		return new BoundedIterator(0, stateActionSize);
	}

	public Iterator<Double> getStateActionReward() {
		return new ClassLabelIterator(0, allInfo.length-1);
	}

	public Iterator<Double> getStateActionSingleStatePrime(int i){
		return new ClassLabelIterator(0, stateActionSize+i);
	}
	
	public Iterator<Double> getStateActionStatePrime() {
		return new BoundedIterator(0, allInfo.length-1);
	}

	public int size() {
		return allInfo.length;
	}

	public Iterator<Double> getStatePrime() {
		return new BoundedIterator(stateActionSize, allInfo.length-1);
	}

	@Override
	public double getAction(int index) {
		return allInfo[stateSize+index];
	}
	
	@Override
	public double getState(int index) {
		return allInfo[index];
	}
	
	@Override
	public double getStatePrime(int index) {
		return allInfo[stateActionSize+index];
	}
	
	@Override
	public Iterator<Double> getSelectedStateActionReward(List<Integer> selected) {
		return new CIIterator(new SelectiveIterator(selected, 0), new ClassLabelIterator(stateSize, allInfo.length-1));
	}

	@Override
	public Iterator<Double> getSelectedStateActionNextState(
			List<Integer> selected, int next) {
		return new CIIterator(new SelectiveIterator(selected, 0), new ClassLabelIterator(stateSize, stateActionSize+next));
	}
	
	public double getReward() {
		return allInfo[allInfo.length-1];
	}

	public double getBatchId() {
		return batchId;
	}

	public void setReward(double reward) {
		allInfo[allInfo.length-1] = reward; 
	}

	public int getStateLength(){
		return stateSize;
	}
	
	public int getActionLength(){
		return stateActionSize - stateSize;
	}
	
	@Override
	public ArraySample clone() throws CloneNotSupportedException {
		ArraySample clone = (ArraySample)super.clone();
		//All info was meant to be shared.
		//clone.allInfo = new ArrayList<Double>(allInfo);
		return clone;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(allInfo);
	}
	
	private class BoundedIterator implements Iterator<Double>{
		private int pos;
		private int end;
		
		private BoundedIterator(int start, int end){
			this.pos = start;
			this.end = end;
		}

		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public Double next() {
			return allInfo[pos++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
	private class ClassLabelIterator implements Iterator<Double>{
		private int pos = 0;
		private int last;
		
		private ClassLabelIterator(int start, int labelPos){
			last = labelPos;
			pos = start;
		}
		
		@Override
		public boolean hasNext() {
			return pos != last;
		}

		@Override
		public Double next() {
			Double retVal;
			if(pos < stateActionSize)
				retVal = allInfo[pos++];
			else{
				retVal = allInfo[last];
				pos = last;
			}
			return retVal;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}	
	}
	
	private class SelectiveIterator implements Iterator<Double>{
		private List<Integer> ids;
		private int offset;
		private int pos;
		
		private SelectiveIterator(List<Integer> ids, int offset){
			this.ids = ids;
			this.offset = offset;
		}
		
		@Override
		public boolean hasNext() {
			return pos < ids.size();
		}

		@Override
		public Double next() {
			return allInfo[offset+ids.get(pos++)];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}	
	}
	
	private class CIIterator implements Iterator<Double>{
		private Iterator<Double>[] iters;
		private int iterOn;
		
		@SafeVarargs
		private CIIterator(Iterator<Double>... iters){
			this.iters = iters;
		}

		@Override
		public boolean hasNext() {
			int len = iters.length-1;
			return iterOn < len || (iterOn == len && iters[iterOn].hasNext());
		}

		@Override
		public Double next() {
			Double retVal = iters[iterOn].next();
			if(!iters[iterOn].hasNext())
				iterOn++;
			return retVal;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
