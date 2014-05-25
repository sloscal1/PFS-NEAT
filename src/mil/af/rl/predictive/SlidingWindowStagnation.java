package mil.af.rl.predictive;

import com.anji.util.Properties;

public class SlidingWindowStagnation extends StagnationDecorator {
	public static final String WINDOW = "stagnation.sliding.window";
	public static final String FACTOR = "stagnation.sliding.factor";
	
	private double currWindow;
	private double factor;
	private double stationary;
	
	/**
	 * 
	 * @param parent may be null if this is the only Stagnation operator
	 * @param baseWindow the basic window size to use (must be >= 0)
	 * @param factor multiplicative value to use when adjusting baseWindow (must be >= 0.0, and > 1.0 to expand).
	 */
	public SlidingWindowStagnation(Stagnation parent, int baseWindow, double factor) {
		super(parent);
		currWindow = baseWindow;
		this.factor = factor;
	}
	
	public SlidingWindowStagnation(){}
	
	@Override
	public void init(Properties props) throws Exception {
		super.init(props);
		this.currWindow = props.getIntProperty(WINDOW+stagOffset);
		this.factor = props.getDoubleProperty(FACTOR+stagOffset);
	}

	@Override
	public void updatePerformance(StagnationInfo info) {
		super.updatePerformance(info);
		if(info.didFeatureSelection){
			stationary = 0.0;
			currWindow *= factor;
		}
		else
			stationary = info.iterationOn - info.iterationOfSelection;
	}

	@Override
	public boolean isStagnant() {
		return super.isStagnant() && stationary >= currWindow;
	}

}
