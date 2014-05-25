package mil.af.rl.problem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import mil.af.rl.predictive.SubspaceIdentification;

import com.anji.util.Configurable;
import com.anji.util.Properties;

public class DoublePoleBalance extends RLProblem implements Configurable {

	private final static String TRACK_LENGTH_KEY = "polebalance.track.length";

	private final static String ANGLE_THRESHOLD_KEY = "polebalance.angle.threshold";

	private final static String POLE_1_LENGTH_KEY = "pole.1.length";

	private final static String POLE_2_LENGTH_KEY = "pole.2.length";

	private final static String FEATURE_TYPE = "dpb.feature.type";

		// Some useful physical model constants.
	private static final double GRAVITY = -9.8;

	private static final double MASSCART = 1.0;

	private static final double FORCE_MAG = 10.0;

	private static final double TIME_DELTA = 0.01;
	private static final double MUP = 0.000002;

	public static final int cartPosition = 0;
	public static final int cartVelocity = 1;
	public static final int pole1Angle = 2;
	public static final int pole1AngularVelocity = 3;
	public static final int pole2Angle = 4;
	public static final int pole2AngularVelocity = 5;

	/**
	 * 0.0174532; 2pi/360
	 */
	private static final double ONE_DEGREE = Math.PI / 180.0;

	/**
	 * 0.628329;
	 */
	private static final double THIRTYSIX_DEGREES = Math.PI / 5.0;

	private final static double DEFAULT_TRACK_LENGTH = 4.8;

	private double trackLength = DEFAULT_TRACK_LENGTH;

	private double trackLengthHalfed;

	private double poleAngleThreshold = THIRTYSIX_DEGREES;

	private double poleLength1 = 0.5;

	private double poleMass1 = 0.1;

	private double poleLength2 = 0.05;

	private double poleMass2 = 0.01;

	/** The type of irrelevant features to use in this problem */
	private FeatureType featureType;

	@Override
	public double[] getNetInput() {
		double[] ret = null;

		if(featureType.totalFeatures != numPerceptions){
			ret = new double[featureType.totalFeatures];
			System.arraycopy(super.getNetInput(), 0, ret, 0, numPerceptions);
			featureType.fillFeatures(ret);
		}
		else
			ret = super.getNetInput();

		return ret;
	}

	@Override
	public void resetRandom() {
		super.resetRandom();
		if(featureType != null) featureType.resetRandom();
	}

	private void setTrackLength( double aTrackLength ) {
		trackLength = aTrackLength;
		trackLengthHalfed = trackLength / 2;
	}


	@Override
	public double getReward() {
		if(inFailureState())
			return -1;
		return 0;
	}

	@Override
	public double getScore() {
		return this.currentTimeStep;
	}

	@Override
	public boolean inFailureState() {
		return ( ( state[ cartPosition ] <= statemin[cartPosition] ) || ( state[ cartPosition ] >= statemax[cartPosition] )
				|| ( state[ pole1Angle ] <= statemin[pole1Angle] ) || ( state[ pole1Angle ] >= statemax[pole1Angle] )
				|| ( state[ pole2Angle ] <= statemin[pole2Angle] ) || ( state[ pole2Angle ] >= statemax[pole2Angle] ) );
	}

	@Override
	public boolean inGoalState() {
		if(currentTimeStep >= MAX_STEPS)
			return true;
		return false;
	}

	@Override
	public void setRandomState() {
		initialState = new double[ featureType.totalFeatures ];
		initialState[ cartPosition ] =  0;
		initialState[ cartVelocity ] = 0; //random.nextDouble() - 0.5; //-0.5 to 0.5 (half of range)
		initialState[ pole1AngularVelocity ] = random.nextDouble() - 0.5;
		initialState[ pole2AngularVelocity ] = random.nextDouble() - 0.5;
		initialState[ pole1Angle ] = ONE_DEGREE;
		initialState[ pole2Angle ] = 0;

		if(featureType != null)
			featureType.resetRandomState(random);
		reset();
	}

	@Override
	public void setState(int state) {
		setRandomState();
	}

	/**
	 * @see com.anji.util.Configurable#init(com.anji.util.Properties)
	 */
	@Override
	public void init( Properties props ) throws Exception {
		try {
			super.init(props);
			setTrackLength( props.getDoubleProperty( TRACK_LENGTH_KEY, DEFAULT_TRACK_LENGTH ) );
			poleAngleThreshold = props.getDoubleProperty( ANGLE_THRESHOLD_KEY, THIRTYSIX_DEGREES );
			poleLength1 = ( props.getDoubleProperty( POLE_1_LENGTH_KEY, 0.5 ) / 2 );
			poleMass1 = ( poleLength1 / 5 );
			poleLength2 = ( props.getDoubleProperty( POLE_2_LENGTH_KEY, 0.05 ) / 2 );
			poleMass2 = ( poleLength2 / 5 );
			//			startPoleAngle1 = props.getDoubleProperty( START_POLE_ANGLE_1_KEY, ONE_DEGREE );
			//			startPoleAngle2 = props.getDoubleProperty( START_POLE_ANGLE_2_KEY, 0 );
			random = new Random(randomSeed);
			numPerceptions = 6;
			featureType = (FeatureType)Class.forName(props.getProperty(FEATURE_TYPE)).newInstance();
			featureType.init(numPerceptions, props);
			setRandomState();
			state = initialState.clone();
			setstatelimits();			
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new IllegalArgumentException( "invalid properties: " + e.getClass().toString() + ": "
					+ e.getMessage() );
		}
	}

	@Override
	protected void setstatelimits()
	{
		statemin = new double[initialState.length];
		statemax = new double[initialState.length];

		statemin[cartPosition] = -trackLengthHalfed;
		statemax[cartPosition] = trackLengthHalfed;

		statemin[cartVelocity] = -1.0;
		statemax[cartVelocity] = 1.0;

		statemin[pole1Angle] = -poleAngleThreshold;
		statemax[pole1Angle] = poleAngleThreshold;

		statemin[pole1AngularVelocity] = -1.0;
		statemax[pole1AngularVelocity] = 1.0;

		statemin[pole2Angle] = -poleAngleThreshold;
		statemax[pole2Angle] = poleAngleThreshold;

		statemin[pole2AngularVelocity] = -1.0;
		statemax[pole2AngularVelocity] = 1.0;
		for(int i = 0; i < statemin.length; ++i){
			statemin[i] = -1.0;
			statemax[i] = 1.0;
		}
	}

	@Override
	public void applyActions(double[] actions) {
		//Find the max number out of these actions
		int max = 0;
		for(int i = 1; i < actions.length; ++i)
			if(actions[i] > actions[max])
				max = i;
		
		double actionC = 0.0;

		int action = (int)Math.round(max);
		switch(action)
		{
		case 0:
			actionC = 0.5;
			break;
		case 1:
			actionC = 1.0;
			break;
		case 2:
			actionC = 0.0;
			break;
		}
		doAction(actionC * 2.0 - 1.0);
		++currentTimeStep;
	}
	
	@Override
	public double doAction(int action)
	{
		double actionC = 0.0;

		switch(action)
		{
		case 0:
			actionC = 0.5;
			break;
		case 1:
			actionC = 1.0;
			break;
		case 2:
			actionC = 0.0;
			break;
		}

		return actionC;
	}

	@Override
	public void doAction(double action)
	{
		if(featureType != null) featureType.takeStep();
		int i;
		double[] dydx = new double[ 6 ];

		// Apply action to the simulated cart-pole 
		for ( i = 0; i < 2; ++i ) {
			dydx[ cartPosition ] = state[ cartVelocity ];
			dydx[ pole1Angle ] = state[ pole1AngularVelocity ];
			dydx[ pole2Angle ] = state[ pole2AngularVelocity ];
			stepC( action, state, dydx );
			rk4C( action, state, dydx, state );
		}

		boundstate(cartPosition);
		boundstate(cartVelocity);
		boundstate(pole1Angle);
		boundstate(pole1AngularVelocity);
		boundstate(pole2Angle);
		boundstate(pole2AngularVelocity);
	}

	private void stepC( double action, double [] st ,double[] derivs ) {
		double force, costheta_1, costheta_2, sintheta_1, sintheta_2, gsintheta_1, gsintheta_2, temp_1, temp_2, ml_1, ml_2, fi_1, fi_2, mi_1, mi_2;


		force = action * FORCE_MAG;

		costheta_1 = Math.cos( st[ pole1Angle ] );
		sintheta_1 = Math.sin( st[ pole1Angle ] );
		gsintheta_1 = GRAVITY * sintheta_1;
		costheta_2 = Math.cos( st[ pole2Angle ] );
		sintheta_2 = Math.sin( st[ pole2Angle ] );
		gsintheta_2 = GRAVITY * sintheta_2;

		ml_1 = poleLength1 * poleMass1;
		ml_2 = poleLength2 * poleMass2;
		temp_1 = MUP * st[ pole1AngularVelocity ] / ml_1;
		temp_2 = MUP * st[ pole2AngularVelocity ] / ml_2;

		fi_1 = ( ml_1 * st[ pole1AngularVelocity ] * st[ pole1AngularVelocity ] * sintheta_1 )
				+ ( 0.75 * poleMass1 * costheta_1 * ( temp_1 + gsintheta_1 ) );

		fi_2 = ( ml_2 * st[ pole2AngularVelocity ] * st[ pole2AngularVelocity ] * sintheta_2 )
				+ ( 0.75 * poleMass2 * costheta_2 * ( temp_2 + gsintheta_2 ) );

		mi_1 = poleMass1 * ( 1 - ( 0.75 * costheta_1 * costheta_1 ) );
		mi_2 = poleMass2 * ( 1 - ( 0.75 * costheta_2 * costheta_2 ) );

		derivs[ cartVelocity ] = ( force + fi_1 + fi_2 ) / ( mi_1 + mi_2 + MASSCART );
		derivs[ pole1AngularVelocity ] = -0.75 * ( derivs[ 1 ] * costheta_1 + gsintheta_1 + temp_1 ) / poleLength1;
		derivs[ pole2AngularVelocity ] = -0.75 * ( derivs[ 1 ] * costheta_2 + gsintheta_2 + temp_2 ) / poleLength2;
	}

	private void rk4C( double f, double[] y, double[] dydx, double[] yout ) {
		int i;

		double hh, h6;
		double[] dym = new double[ 6 ];
		double[] dyt = new double[ 6 ];
		double[] yt = new double[ 6 ];

		hh = TIME_DELTA * 0.5;
		h6 = TIME_DELTA / 6.0;
		for ( i = 0; i <= 5; i++ )
			yt[ i ] = y[ i ] + hh * dydx[ i ];
		stepC( f, yt, dyt );
		dyt[ 0 ] = yt[ 1 ];
		dyt[ 2 ] = yt[ 3 ];
		dyt[ 4 ] = yt[ 5 ];
		for ( i = 0; i <= 5; i++ )
			yt[ i ] = y[ i ] + hh * dyt[ i ];
		stepC( f, yt, dym );
		dym[ 0 ] = yt[ 1 ];
		dym[ 2 ] = yt[ 3 ];
		dym[ 4 ] = yt[ 5 ];
		for ( i = 0; i <= 5; i++ ) {
			yt[ i ] = y[ i ] + TIME_DELTA * dym[ i ];
			dym[ i ] += dyt[ i ];
		}
		stepC( f, yt, dyt );
		dyt[ 0 ] = yt[ 1 ];
		dyt[ 2 ] = yt[ 3 ];
		dyt[ 4 ] = yt[ 5 ];
		for ( i = 0; i <= 5; i++ )
			yout[ i ] = y[ i ] + h6 * ( dydx[ i ] + dyt[ i ] + 2.0 * dym[ i ] );
	}

	/**
	 * This class contains the different types of irrelevant features to
	 * fill the state variable
	 * @author sloscal1
	 *
	 */
	private static abstract class FeatureType{
		/** The number of features provided by the problem */
		protected int offset;
		/** Total number of features to fill in */
		protected int totalFeatures;
		/** Random number generator for irrelevant features */
		protected Random irrelevantRand;
		/** Seed for random number generator */
		protected long irrelRandSeed;

		/**
		 * Initialize this feature filler.  All parameters must be provided.
		 * @param numRelevant
		 * @param totalFeatures
		 * @param randSeed
		 */
		void init(int numRelevant, Properties props){
			this.offset = numRelevant;
			this.totalFeatures = props.getIntProperty(SubspaceIdentification.FS_FEATURES_KEY);
			irrelRandSeed = props.getLongProperty("random.seed");
			irrelevantRand = new Random(irrelRandSeed);
		}

		/**
		 * Reset random number generator to initial state.
		 */
		void resetRandom(){
			irrelevantRand = new Random(irrelRandSeed);
		}

		/**
		 * Fill in any features beyond the offset using whichever
		 * concrete implementation strategy that is selected.
		 * @param state must not be null and must be same length  as
		 * totalFeatures.
		 */
		abstract void fillFeatures(double[] state);
		
		/**
		 * Alters what will be returned from fillFeatures, otherwise successive
		 * calls to fillFeatures will return the same values.
		 */
		abstract void takeStep();
		
		abstract void resetRandomState(Random rand);
	}
	
	/** Available for those runs that do not include irrelevant features */
	static class Empty extends FeatureType{
		@Override
		void fillFeatures(double[] state) {
			//Does nothing.
		}

		@Override
		void takeStep() {
			//Doesn't need to do anything			
		}
		
		@Override
		void resetRandomState(Random rand) {
			//No action required
		}
	} 

	/** Simple implementation of randomly generated features */
	static class RandomFeature extends FeatureType{
		private double[] previous;
		private boolean changeState = true;
		
		@Override
		void init(int numRelevant, Properties props) {
			super.init(numRelevant, props);
			previous = new double[totalFeatures];
		}
		
		@Override
		void fillFeatures(double[] state) {
			if(changeState){
				for(int i = offset; i < state.length; ++i)
					state[i] = irrelevantRand.nextDouble();
				System.arraycopy(state, 0, previous, 0, state.length);
			}
			else
				System.arraycopy(previous, 0, state, 0, state.length);
			changeState = false;
		}

		@Override
		void takeStep() {
			changeState = true;
		}

		@Override
		void resetRandomState(Random rand) {
			irrelevantRand = new Random(rand.nextLong());
		}
	}

	/** Follows a stored policy to provide more realistic state changes */
	static class Stored extends FeatureType{
		static List<double[]> storedTransitions;
		static int[] initials;
		int[] positions;
		
		@Override
		void init(int numRelevant, Properties props) {
			//Get the basics..
			super.init(numRelevant, props);
			//Only want to initialize this field 1x
			if(storedTransitions == null)
				synchronized(Stored.class){
					if(storedTransitions == null){
						//Read the transition lines from a file
						storedTransitions = new ArrayList<double[]>();
						File f = new File(props.getProperty(FEATURE_TYPE+".storedpath"));
						try(Scanner input = new Scanner(f)){
							while(input.hasNextLine()){
								//Get a line and break it up into the vector components
								Scanner line = new Scanner(input.nextLine());
								line.useDelimiter("[\\[\\],\\s]+");
								double[] parsed = new double[offset];
								int pos = 0;
								while(line.hasNext()){
									if(line.hasNextDouble())
										parsed[pos++] = line.nextDouble();
									else
										line.next();
								}
								line.close();
								//Add it to our collection
								storedTransitions.add(parsed);
							}
						} catch (FileNotFoundException e) {
							throw new IllegalArgumentException("Could not initialize stored trajectories.", e);
						}
						//Now that we have transitions, fill in initial positions:
						int numPositions = (int)Math.ceil((totalFeatures-offset)/(double)offset);
						initials = new int[numPositions];
						for(int i = 0; i < numPositions; ++i)
							initials[i] = irrelevantRand.nextInt(storedTransitions.size());
					}
				}
			resetRandom();
		}

		@Override
		void fillFeatures(double[] state) {
			int start = offset;
			for(int i = 0; i < initials.length; ++i){
				int end = Math.min(offset, totalFeatures-start);
				System.arraycopy(storedTransitions.get(positions[i]), 0, state, start, end);
				start += offset;
			}
		}
		
		@Override
		void resetRandom() {
			super.resetRandom();
			positions = initials.clone();
		}

		@Override
		void takeStep(){
			for(int i = 0; i < positions.length; ++i)
				positions[i] = (positions[i] + 1) % storedTransitions.size();
		}

		@Override
		void resetRandomState(Random rand) {
			//Need to set the random state of position in a reproducicble manner
			for(int i = 0; i < positions.length; ++i)
				positions[i] = rand.nextInt(storedTransitions.size());			
		}
	};
	
	static class AdditionalFeatures extends FeatureType{
		/** The additional features that will be added */
		private AdditionalTypes[] toAdd = new AdditionalTypes[6];
		private double[] factors = {1};
		private double[] summands = {0};
		private double[] computedState;
		private boolean recompute = true;

		@Override
		void init(int numRelevant, Properties props) {
			super.init(numRelevant, props);
			//Could make it so that only some of the additional types are added.
			//TODO make this more customizable.
			for(int i = 0; i < toAdd.length; ++i)
				toAdd[i] = new LaggedSensors(5, i);
		}

		@Override
		void fillFeatures(double[] state) {
			if(recompute){
				for(int i = 0; i < toAdd.length; ++i){
					//What is the base value computed from the rest of this state for this add't feature?
					double base = toAdd[i].compute(state);
					//Determine the variations of this base for the rest of the state vector
					for(int pos = offset + i, iter = 0; pos < totalFeatures; pos += toAdd.length, iter = (iter+1)%factors.length){
						state[pos] = factors[iter] * base + summands[iter];
					}
				}
				computedState = state.clone();
				recompute = false;
			}
			else //Not sure how much this caching really saves... may remove in future
				System.arraycopy(computedState, 0, state, 0, computedState.length);
		}

		@Override
		void takeStep() {
			recompute = true;			
		}

		@Override
		void resetRandomState(Random rand) {
			//Nothing to be done.
		}

		static class ABSPOLE1V implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return Math.abs(state[pole1AngularVelocity]);
			}
		}

		static class	ABSPOLE2V implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return Math.abs(state[pole2AngularVelocity]);
			}
		}

		static class	ABSPOLE1A implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return Math.abs(state[pole1Angle]);
			}
		}

		static class ABSPOLE2A implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return Math.abs(state[pole2Angle]);
			}
		}

		static class DIFFANGLE implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return state[pole1Angle] - state[pole2Angle];
			}
		}

		static class DIFFVEL implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return state[pole1AngularVelocity] - state[pole2AngularVelocity];
			}
		}

		static class ABSSUMANGLE implements AdditionalTypes{
			@Override
			public double compute(double[] state) {
				return Math.abs(state[pole1Angle]) + Math.abs(state[pole2Angle]);
			}
		}

	}

	static class LaggedSensors implements AdditionalTypes{
		private double[] old;
		private int toCopy;
		private int pos;

		public LaggedSensors(int delay, int toCopy){
			old = new double[delay];
			this.toCopy = toCopy;
		}

		@Override
		public double compute(double[] state) {
			//This value was visited delay timesteps ago, return it
			double retVal = old[pos];
			//set it to the new value (old is now delay+1 steps old)
			old[pos] = state[toCopy];
			//Move to the next newest slot (will be delay steps old next call).
			pos = (pos + 1) % old.length;
			return retVal;
		}
	}
	
	public interface AdditionalTypes{
		double compute(double[] state);
	}

	
	public static class JAAMASSensors extends FeatureType{
		private RandomFeature randPart;
		private AdditionalFeatures redPart;
		private Stored storedPart;
		private double[] cached;
		private boolean changed = true;
		private int numRandom = 6;
		private int numRedund = 6;
		private int numRelevant;

		@Override
		void init(int numRelevant, Properties props) {
			super.init(numRelevant, props);
			this.numRelevant = numRelevant;
			randPart = new RandomFeature();
			//make it so that there are only numRandom irrelevant features
			synchronized(props){
				int oldVal = props.getIntProperty(SubspaceIdentification.FS_FEATURES_KEY);
				props.setProperty(SubspaceIdentification.FS_FEATURES_KEY, ""+(numRelevant+numRandom));
				randPart.init(numRelevant, props);
				props.setProperty(SubspaceIdentification.FS_FEATURES_KEY, ""+oldVal);
			}
			//Add in a few redudant ones
			redPart = new AdditionalFeatures();
			redPart.init(numRelevant+numRandom, props);
			//the rest.
			storedPart = new Stored();
			storedPart.init(numRelevant+numRandom+numRedund, props);
		}

		@Override
		void fillFeatures(double[] state) {
			if(totalFeatures != numRelevant){
				if(changed){
					double[] randState = new double[numRelevant+numRandom];
					randPart.fillFeatures(randState);
					System.arraycopy(randState, numRelevant, state, numRelevant, numRandom);
					redPart.fillFeatures(state);
					storedPart.fillFeatures(state);
					cached = state.clone();
					changed = false;
				}
				else
					System.arraycopy(cached, 0, state, 0, state.length);
			}
		}

		@Override
		void takeStep() {
			randPart.takeStep();
			redPart.takeStep();
			storedPart.takeStep();
			changed = true;
		}

		@Override
		void resetRandomState(Random rand) {
			randPart.resetRandomState(rand);
			redPart.resetRandomState(rand);
			storedPart.resetRandomState(rand);
			changed = true;
		}
	}
}