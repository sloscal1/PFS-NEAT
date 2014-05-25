package mil.af.rl.predictive;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.anji.util.Properties;

public class TrendStagnation extends StagnationDecorator {
	public static final String WINDOW = "stagnation.trend_window";
	public static final String THRESHOLD = "stagnation.trend_threshold";
	
	private int windowSize;
	private List<Double> performance;
	private double threshold;

	public TrendStagnation(Stagnation parent, double threshold, int windowSize) {
		super(parent);
		this.threshold = threshold;
		this.windowSize = windowSize;
		performance = new LinkedList<Double>();
	}
	
	public TrendStagnation(){}
	
	@Override
	public void init(Properties props) throws Exception {
		super.init(props);
		this.threshold = props.getDoubleProperty(THRESHOLD+stagOffset);
		this.windowSize = props.getIntProperty(WINDOW+stagOffset);
	}

	@Override
	public void updatePerformance(StagnationInfo info) {
		super.updatePerformance(info);
		if(info.didFeatureSelection)
			performance = new LinkedList<Double>();
		performance.add(info.lastPerformance);
		if(performance.size() > windowSize)
			performance.remove(0);
	}

	@Override
	public boolean isStagnant() {
		boolean retVal = false;
		if(performance.size() == windowSize){
			SimpleRegression reg = new SimpleRegression();
			double x = 0.0;
			for(Double d : performance)
				reg.addData(x++, d);
			double slope = reg.getSlope();
			System.out.println("SLOPE: "+slope+" "+performance);
			retVal = slope < threshold;
		}
		return retVal && super.isStagnant();
	}
}
