package mil.af.rl.predictive;

import com.anji.util.Properties;

public abstract class StagnationDecorator implements Stagnation {
	protected Stagnation next;
	protected String stagOffset;
	/** For use when initializing from properties files, keep track of which element is being initialized */
	private int stagInitCount;
	
	public StagnationDecorator(Stagnation parent){
		this.next = parent;
	}
	
	//This is for use with the init method - it breaks the decorator pattern initialization,
	//but it does allow to use the same properties file initialization as does the rest.
	public StagnationDecorator(){}
	
	@Override
	public void updatePerformance(StagnationInfo info) {
		if(next != null)
			next.updatePerformance(info);
	}
	
	@Override
	public boolean isStagnant() {
		return next == null ? true : next.isStagnant();
	}
	
	@Override
	public void init(Properties props) throws Exception {
		++stagInitCount;
		stagOffset = "."+stagInitCount;
		if(props.containsKey(STAGNATION_BASE_CLASS+stagOffset)){
			next = (Stagnation)Class.forName(props.getProperty(STAGNATION_BASE_CLASS+stagOffset)).newInstance();
			next.setStagInitCount(stagInitCount);
			next.init(props);
		}
		--stagInitCount;
	}
	
	final public void setStagInitCount(int count){
		this.stagInitCount = count;
	}
}
