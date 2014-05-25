/**
 * 
 */
package mil.af.rl.problem.rars;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import mil.af.rl.predictive.SubspaceIdentification;
import mil.af.rl.problem.RLProblem;

import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * @author RARS developers, modified by Kevin Acunto.
 * 
 */
public class SimpleRars extends RLProblem implements Configurable {
	/*
	 * Many of these properties keys are no longer used, but were useful in previous experiments
	 * and so are left in the code to help facilitate future work.
	 */
	/** Properties key for the number of relevant sensors (NOT COUNTING velocity). */
	public final static String REL_FEATURES = "rars.features.relevant";
	private final static String RED_FEATURES = "rars.features.redundant";
	/** Properties key for the number of irrelevant features in the environment */
	public final static String IRR_FEATURES = "rars.features.irrelevant";

	private final static String START_SEG = "rars.config.start_seg";
	private final static String ANG_OFFSET = "rars.config.ang_off";
	private final static String DIST_OFFSET = "rars.config.dist_off";
	private final static String CEN_OFFSET = "rars.config.cen_off";
	private final static String INIT_SPEED = "rars.config.init_speed";

	private final static String PRINT_STEPS = "rars.logging.steps";
	private final static String PRINT_ACT = "rars.logging.actions";
	private final static String PRINT_TRANS = "rars.logging.transitions";

	/** Specify the feature class to be used to supply the irrelevant features*/
	public final static String FEATURE_TYPE = "rars.feature.type";
	
	private final static String REVERSE_REWARD = "rars.reverse_reward";
	

	private static final int NUM_ACTIONS = 2;


	/*
	 * The names of the track to supply to the RARS executable (separated by
	 * spaces)
	 */
	// private String[] trackNames;
	/** The number of relevant features used in this problem */
	private int relevantFeatures;
	/** The number of redundant features used in this problem */
	private int redundantFeatures;
	/** The number of irrelevant features used in this problem */
	private int irrelevantFeatures;
	/** The type of irrelevant features to use in this problem */
	private FeatureType featureType;
	/** This is the immediate reward observed this time step */
	private double reward;

	/**
	 * The maximum number of steps for the learner to take in the RARS
	 * environment
	 */
	private int maxSteps;

	// **** Internal State variables of the problem.
	/**
	 * The number of consecutive off track observations the learner can see
	 * before the run is considered a failure
	 */
	private static final int FAIL_COUNT = 1;
	/** The number of stationary actions that are considered a failure */
	private static final int FAIL_STAT = 6;

	/** Whether or not the RARS game is ready to send/receive info */
	/** The fitness of this learner */
	private double fitness;

	/**
	 * Keeps track of the number of consecutive updates that the car has been
	 * off the track. Too many will lead to a failure.
	 */
	private int offCount;
	/**
	 * It was noticed that the learner will send large sequences of zeros so if
	 * it sends too many in a row it will fail.
	 */
	private int stationary;
	/**
	 * A random variable used to decide which irrelevant features become
	 * negative
	 */
	// private Random rand = new Random(12345);

	/** track */
	public Track track;

	private Car car;

	// Features from NEATController
	/** Number of steps that have been taken by the car */
	private int steps = 0;
	/** the fitness function score */
	private double score;
	/** The X position of the car */
	// private double carPosX;
	// /** The Y position of the car */
	// private double carPosY;
	// /** The angle of the car relative to the track */
	// private double carAngle;
	/** The previous length of the sensors (used to draw over) */
	// private double[] lines;
	/** The current length of the sensors */
	private double[] features;

	// This is the most e2 that the car can cover in a single timestep (approx)

	private double carInitX, carInitY, carInitAng;

	/** the segment of the track that the car starts on */
	private int start_seg;

	/** how far in front or behind the segment boundary the car starts */
	private double dist_off;

	/** how much in degrees off straight the car starts (-90, 90) */
	private int ang_off;

	/** how far from the center of the track the car starts */
	private double cen_off;

	/** initial speed of car */
	private double init_speed;

	private double start_pos;
	private int init_seg;

	private boolean print_steps;
	private boolean print_trans;
	private boolean print_actions;

	private int curr_seg = 0;
	
	private int segSequence;

	// _SL_ 8/17/11 Added to test learning two policies: 1 for straight, 1 for
	// curved.
	// The segment from the segment pool
	private int segmentIndex = 0;
	// Straight segments on fiorano
	// private int[] tracksOfInterest = {0, 2, 4, 7, 10, 14, 15, 18, 20, 24,
	// 27};
	// Curved segments on fiorano
	private int[] tracksOfInterest = { 1, 3, 5, 6, 8, 9, 11, 12, 13, 15, 17,
			19, 21, 22, 23, 25, 26 };
	// These three variables are for the new fitness calculation which goes
	// along
	// with the segment jumping behavior
	private double initDistance;
	private boolean partitioned = false;

	/**
	 * An absorbing terminal state TODO this is not implemented yet, I need to
	 * adjust the goal/failure states to lead here
	 */
	private double[] terminalState;
	/** This allows FQI to minimize the aggregate value */
	private boolean reverseReward;

	@Override
	public void init(Properties props) throws Exception {
		resetRandom();
		maxSteps = props.getIntProperty(MAX_STEPS_KEY, 2000);
		maxSteps = props.getIntProperty("maxsteps", maxSteps);
		// _SL_ 11/1/10 there can be multiple tracks which define a problem set
		// trackNames = props.getProperty(TRACK_NAME).split("\\s+");
		// StringBuilder sb = new StringBuilder();
		// for(String t : allTracks)
		// sb.append(t+" ");
		// trackName = sb.toString();
		// trackName = props.getProperty(TRACK_NAME); //PRIOR to _SL_ 11/1/10
		relevantFeatures = props.getIntProperty(REL_FEATURES);
		redundantFeatures = props.getIntProperty(RED_FEATURES);
		irrelevantFeatures = props.getIntProperty(IRR_FEATURES);
		// boolean showLines = props.getBooleanProperty(SHOW_VECTORS, true);

		print_steps = props.getBooleanProperty(PRINT_STEPS, false);
		print_trans = props.getBooleanProperty(PRINT_TRANS, false);
		print_actions = props.getBooleanProperty(PRINT_ACT, false);

		track = new Track(props);
		car = new Car(track);

		// _KA_ 5/27/11 Added properties to set initial car position
		start_seg = props.getIntProperty(START_SEG, 0);
		dist_off = props.getDoubleProperty(DIST_OFFSET, track.length
				- track.seg_dist[track.NSEG - 1]);
		ang_off = props.getIntProperty(ANG_OFFSET, 0);
		cen_off = props.getDoubleProperty(CEN_OFFSET, 0);
		init_speed = props.getDoubleProperty(INIT_SPEED, Car.STARTING_SPEED);
		// _SL_ 6/30/11 Refactored to put all the x y coordinate calculations
		// separate from here so that a car can be moved mid-race.
		features = new double[relevantFeatures + irrelevantFeatures
		                      + redundantFeatures];
		state = new double[features.length + 1];
		
		featureType = (FeatureType)Class.forName(props.getProperty(FEATURE_TYPE)).newInstance();
		featureType.init(10, props);
		System.out.println(featureType);

		// Figure out the relevant feature set here:
		Set<Integer> totRelevant = new HashSet<Integer>();
		for (int i = 0; i < relevantFeatures + 1; ++i)
			totRelevant.add(i);

		System.out.println("TOTAL_RELEVANT:" + totRelevant);
		numPerceptions = 8;
		numActions = NUM_ACTIONS;

		setStartingState(start_seg, dist_off, ang_off, cen_off, init_speed);

		initialState = state.clone();
		terminalState = new double[initialState.length];
		Arrays.fill(terminalState, -50); // I think all have to be between 0 and
		// 1 normally, so negatives should
		// work.
		setstatelimits();
		// _SL_ 9/6/13 Added to try FQI on this problem:
		reverseReward = props.getBooleanProperty(REVERSE_REWARD, false);
	}

	/**
	 * 
	 * @param startSeg
	 *            The starting segment number (negatives will be moved to 0,
	 *            segment lengths that are too large will be modded down)
	 * @param distOff
	 *            The e2 offset from the start of the starting segment Negative
	 *            offsets move backwards, positive move forwards, use large
	 *            scales.
	 * @param angOff
	 *            The starting angle of the car [-89, 89] positive is left.
	 * @param cenOff
	 *            The center displacement of the car. 0 is center, and it ranges
	 *            from [-trackWidth/2, trackWidth/2]. Left is positive.
	 * @param initSpeed
	 *            The initial speed of the car
	 * @param numLaps
	 *            The number of laps that are expected
	 */
	private void setStartingState(int startSeg, double distOff, int angOff,
			double cenOff, double initSpeed) {
		// Preconditions: track is set
		// Postconditions:
		// start_pos is set
		// end_dist is set
		// cen_off is corrected
		// firstStep is true
		// carInitX is set
		// carInitY is set
		// carInitAng is set
		// car is put on the track

		// double d = 1.5 * 20; // e2 between cars
		double ang_toend;
		double R; // curve radius at cars position
		// , N = track.m_iStartRows;
		double W, x = 0, y = 0, car_ang = 0;

		double sl_pos = track.length - track.seg_dist[track.NSEG - 1];
		start_pos = track.length + distOff;
		startSeg = startSeg % track.NSEG;
		if (startSeg > 0)
			start_pos += track.seg_dist[startSeg - 1] + sl_pos;
		start_pos = start_pos % track.length;

		// System.out.println("Start pos: " + start_pos);
		if (cenOff > track.width / 2)
			cenOff = track.width / 2;
		if (cenOff < -track.width / 2)
			cenOff = -track.width / 2;
		W = track.width / 2 + cenOff;
		// find starting segment for given c

		int k = 0;

		// _KA_ 5/27/11 fixed car initial placement
		while (k < track.NSEG - 1 && start_pos >= track.seg_dist[k] + sl_pos) {
			k++;
		}

		init_seg = k;
		if (track.lftwall[k].radius > 0) // in left
			// curve
		{
			double offset = 0;
			if (k > 0)
				offset = track.seg_dist[k] + sl_pos - start_pos;
			else
				offset = track.seg_dist[k] - start_pos;
			ang_toend = 2 * offset
					/ (track.lftwall[k].radius + track.rgtwall[k].radius);
			R = track.rgtwall[k].radius - W;
			x = track.lftwall[k].cen_x - R
					* Math.sin(ang_toend - track.lftwall[k].end_ang);
			y = track.lftwall[k].cen_y - R
					* Math.cos(ang_toend - track.lftwall[k].end_ang);
			car_ang = track.lftwall[k].end_ang - ang_toend + (angOff % 90)
					* Math.PI / 180;
		} else if (track.lftwall[k].radius < 0) // in
			// right
			// curve
		{
			double offset = 0;
			if (k > 0)
				offset = track.seg_dist[k] + sl_pos - start_pos;
			else
				offset = track.seg_dist[k] - start_pos;
			ang_toend = -2 * offset
					/ (track.lftwall[k].radius + track.rgtwall[k].radius);
			R = Math.abs(track.rgtwall[k].radius) + W;
			x = track.lftwall[k].cen_x - R
					* Math.sin(ang_toend + track.lftwall[k].end_ang);
			y = track.lftwall[k].cen_y + R
					* Math.cos(ang_toend + track.lftwall[k].end_ang);
			car_ang = ang_toend + track.lftwall[k].end_ang + (angOff % 90)
					* Math.PI / 180;
		} else // on straight
		{
			double offset = 0;
			if (k > 0)
				offset = start_pos - track.seg_dist[k - 1] - sl_pos;
			else
				offset = start_pos;

			x = track.rgtwall[k].beg_x + offset
					* Math.cos(track.lftwall[k].beg_ang) - W
					* Math.sin(track.lftwall[k].beg_ang);
			y = track.rgtwall[k].beg_y + offset
					* Math.sin(track.lftwall[k].beg_ang) + W
					* Math.cos(track.lftwall[k].beg_ang);
			car_ang = track.rgtwall[k].beg_ang + (angOff % 90) * Math.PI / 180;
		}
		// Put car on computed starting position and initialize
		// its variables. Save starting position
		carInitX = x;
		carInitY = y;
		car_ang += Math.PI;
		if (car_ang < Math.PI)
			car_ang += Math.PI;
		else
			car_ang -= Math.PI;
		carInitAng = car_ang;
		// _SL_ 6/30/11 added this to keep post conditions consistent before and
		// after move.
		this.cen_off = cenOff;
		car.putCar(x, y, carInitAng, init_seg, initSpeed);
		determineState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#setRandomState()
	 */
	@Override
	public void setRandomState() {
		// reset();
		// TODO set any of the irrelevant features that are dependent on the
		// state of the car
		System.out.println("seg: " + segSequence);
		setStartingState(segSequence++, // Sequentially increment the
				// segments...
				0, // The beginning of that segment
				0,// random.nextInt(30)-15, //Look straightish relative to the
				// track normal
				0,/* random.nextDouble()*track.width/4.0, */// Any valid
				// left/right
				// position
				100/* random.nextGaussian()*50+100 */); // 65% chance of going
		// 50 to 150, 35
		// above and below
		// it.
		if(featureType != null)
			featureType.resetRandomState(random);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#setState(int)
	 */
	@Override
	public void setState(int state) {
		reset();
	}

	/**
	 * Resets the problem to the initial state and resets the current time step
	 * to 0
	 */
	@Override
	public void reset() {
		super.reset();

		steps = 0;
		currentTimeStep = 0;
		offCount = 0;
		score = 0;
		fitness = 0;
		segmentIndex = 0;

		initDistance = -1.0;
		if (partitioned)
			car.putCar(carInitX, carInitY, carInitAng,
					tracksOfInterest[segmentIndex], init_speed);
		else
			car.putCar(carInitX, carInitY, carInitAng, init_seg, init_speed);
		featureType.resetRandom();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#applyActions(double[])
	 */
	@Override
	public void applyActions(double[] actions) {
		doActions(actions);
		currentTimeStep = steps;
	}

	private double[] computeFeatures(Car c) {
		double[] newFeatures = new double[features.length];
		// Relevant features:
		double delta = Math.PI / (relevantFeatures - 1); // gap between relevant
		// sensors. -1 to
		// make sure the 0
		// and PI sensors
		// are there
		Situation s = c.getSituation();
		double angle = 0.0;
		double trackWidth = s.to_lft + s.to_rgt;
		// Pack in the next few segments of track so they can easily be iterated
		// through:
		SegmentInfo[] sections = new SegmentInfo[4];
		sections[0] = new SegmentInfo(s.cur_len, s.cur_rad);
		sections[1] = new SegmentInfo(s.nex_len, s.nex_rad);
		sections[2] = new SegmentInfo(s.after_len, s.after_rad);
		sections[3] = new SegmentInfo(s.aftaft_len, s.aftaft_rad);
		// for each angle that we're placing a sensor
		// cout <<"*************************************************"<<endl;
		// Compute the real angle that the car makes with the track
		for (int pos = 0; pos < relevantFeatures; pos++, angle += delta) {
			double length = 0;
			double rem = s.to_end;
			double aWT = Math.asin(s.vn / s.v);
			if (s.backward) {
				if (aWT < 0)
					aWT = -Math.PI - aWT;
				else
					aWT = Math.PI - aWT;
			}
			int sectionId = 0;
			// See how much the wheel angle changed, only the direction is
			// important
			double realAngle = angle + aWT;
			while (realAngle < 0.0)
				realAngle += 2.0 * Math.PI;
			while (realAngle > 2.0 * Math.PI)
				realAngle -= 2.0 * Math.PI;
			double realTheta = realAngle;
			// Put real theta in the first quadrant
			while (realTheta > Math.PI / 2.0) {
				realTheta -= Math.PI;
				realTheta = Math.abs(realTheta);
			}
			boolean started_backwards = false;
			if (realAngle > Math.PI)
				started_backwards = true;
			// determine if we're looking towards the left or right wall
			double near_wall = s.to_lft;
			double far_wall = s.to_rgt;
			if (realAngle < Math.PI / 2.0 || realAngle > 3.0 * Math.PI / 2.0) {
				near_wall = s.to_rgt;
				far_wall = s.to_lft;
			}
			newFeatures[pos] = 0; // Zero it out for now and skip the first even
			// position
			// Check if you are out of bounds - don't give a sensor reading in
			// that direction
			// if (s.to_lft > 0.0 && s.to_rgt > 0.0) {
			do {
				// cout <<sectionId<<" ";
				// calculate the vector from this point
				double segment = 0.0;
				// does this segment hit a wall?
				double curr_rad = sections[sectionId].radius;
				// double curr_len = sections[sectionId].length;
				if (curr_rad == 0.0) { // Straight, so if segment > to_end
					// yes.
					// cout <<"Straight ";
					// Back only makes sense for the first segment, we don't
					// have any other prior information
					double back = s.cur_len - rem;
					if (realAngle > Math.PI) {
						rem = back;
						back = s.to_end;
					}
					double upShift = 0.0;
					if (Math.PI / 2.0 - realTheta < 1e-7) // cos will be
						// zero so can't
						// use tan
						upShift = rem; // Will go straight to the end
					else {
						upShift = near_wall * Math.tan(realTheta);
					}
					if (upShift >= rem) {// too big for this section
						segment = rem / Math.sin(realTheta);
						near_wall = near_wall - Math.cos(realTheta) * segment;
					} else { // Completely fits, you hit wall, how far until
						// that happens
						segment = near_wall / Math.cos(realTheta);
						near_wall = 0;
					}
				}// We're dealing with a curved segment
				else {
					// figure out how much of the arc is left
					double gamma = realAngle > Math.PI ? Math.abs(curr_rad
							- rem) : Math.abs(rem);
					// double gammaTot = curr_len;
					double radMag = Math.abs(curr_rad); // Get the positive
					// value of radius,
					// used in many
					// calcs.
					boolean heading_right = true;
					if (realAngle > Math.PI / 2.0
							&& realAngle <= 3.0 * Math.PI / 2.0)
						heading_right = false;
					// Always pretend we are facing to the right for the
					// geometry.
					if ((curr_rad < 0.0 && heading_right)
							|| (curr_rad > 0.0 && !heading_right)) {
						/** RIGHT TURN */
						double[] ret = new double[2];
						calcIntersectPoint(-near_wall - radMag, radMag,
								realTheta, ret);
						if (ret[0] != -2.0 * radMag) {
							// INTERSECTION WITH INNER CIRCLE
							double closest = ret[0] < ret[1] ? ret[0] : ret[1];
							if (Math.PI / 2.0 - realTheta > 1e-7)
								segment = Math.abs(closest
										- (-near_wall - radMag))
										/ Math.cos(realTheta);
							else {
								segment = 0; // geometrically impossible, I
								// would think.
							}// DOES INTERSECTION HAPPEN AFTER ARC FINISHED?
							if (gamma < Math.PI / 2.0) {
								double lim = -radMag * Math.cos(gamma);
								if (closest - lim > 2) { // 1.5 is close
									// enough...
									// there are
									// some rounding
									// details.
									// YES.
									if (realTheta > 1e-7)
										segment = calcEscapePoint(-near_wall
												- radMag, realTheta, gamma,
												radMag, trackWidth, segment);
									else
										segment = near_wall;
									near_wall = -(near_wall - Math
											.cos(realTheta) * segment);
									realTheta = realTheta + gamma;
									realTheta = firstQuadrant(realTheta);
									realAngle = realTheta;
								}
							}
						} else {
							calcIntersectPoint(-near_wall - radMag, radMag
									+ trackWidth, realTheta, ret);
							// MUST HAVE INTERSECTION WITH THE OUTER CIRCLE,
							// BUT WHERE?
							// Will always be to the right in this
							// computation
							double correct = ret[0] > ret[1] ? ret[0] : ret[1];

							if (Math.PI / 2.0 - realTheta > 1e-7)
								segment = (correct - (-near_wall - radMag))
								/ Math.cos(realTheta);
							else
								// could be straight up
								segment = Math.sqrt((radMag + trackWidth)
										* (radMag + trackWidth)
										- (near_wall + radMag)
										* (near_wall + radMag));
							// DOES INTERSECTION HAPPEN AFTER ARC FINISHED?
							if (gamma < Math.PI) {
								double lim = Math.cos(gamma)
										* (-radMag - trackWidth);
								if (correct > lim) {
									// YES
									double effectiveGamma = gamma;
									if (gamma < Math.PI / 2.0)
										effectiveGamma = Math.PI - gamma;
									segment = calcEscapePoint(-near_wall
											- radMag, realTheta,
											effectiveGamma, radMag, trackWidth,
											segment);
									// The exit point flips this vector so
									// that it is facing the other side of
									// the track.
									segment = Math.abs(segment); // gamma
									// could
									// cause
									// a
									// negative
									// segment
									// length
									// Somehow, this can be wrong.

									double height = Math.sin(realTheta)
											* segment;
									double toCenter = height / Math.sin(gamma)
											- radMag;
									if (realTheta + gamma > Math.PI / 2.0) {
										realTheta = Math.PI - realTheta - gamma;
										firstQuadrant(realTheta);
										realAngle = Math.PI - realTheta;
										near_wall = -(trackWidth - toCenter);
									} else {
										realTheta = gamma + realTheta;
										firstQuadrant(realTheta);
										realAngle = realTheta;
										near_wall = -toCenter;
									}
								}
							}
						}
					} else {
						/** Left turn */
						double[] ret = new double[2];
						calcIntersectPoint(far_wall + radMag, radMag
								+ trackWidth, realTheta, ret);
						// Facing away from the direction of a turn requires
						// that you would hit the turn
						// or else escape it.
						double dx = ret[0] > ret[1] ? ret[0] : ret[1];
						if (Math.PI / 2.0 - realTheta > 1e-7)
							segment = (dx - far_wall - radMag)
							/ Math.cos(realTheta);
						else
							// could be straight up
							segment = Math
							.sqrt((radMag + trackWidth)
									* (radMag + trackWidth)
									- (far_wall + radMag)
									* (far_wall + radMag));
						// did this intersection occur after the end of the
						// arc?
						if (gamma < Math.PI / 2.0) {// otherwise it is fine
							// for sure
							double lim = Math.cos(gamma)
									* (radMag + trackWidth);
							if (dx < lim) { // Past the end of this segment
								if (realTheta > 1e-7)
									segment = calcEscapePoint(
											far_wall + radMag, realTheta,
											gamma, radMag, trackWidth, segment);
								else
									segment = near_wall;
								near_wall = -(trackWidth - ((segment * Math
										.sin(realTheta)) / Math.sin(gamma) - radMag));
								realTheta = realTheta - gamma;
								firstQuadrant(realTheta);
								realAngle = realTheta;

							}
						}
					}
					if (!heading_right)
						realAngle = Math.PI - realAngle;
					// If the driver made it through the whole turn on this
					// heading I set the toWall e2 to negative what it
					// really is. Otherwise it should be set to 0.
					if (near_wall > 0.0)// Hit
						near_wall = 0.0;
					else
						near_wall = Math.abs(near_wall);
				}// End dealing with turn
				if (near_wall < 1e-7)
					near_wall = 0;
				if (near_wall > 100)
					far_wall = near_wall;

				length += segment;
				far_wall = trackWidth - near_wall;
				// Make sure realAngle is in the correct region
				while (realAngle < 0.0)
					realAngle += 2.0 * Math.PI;
				while (realAngle > 2.0 * Math.PI)
					realAngle -= 2.0 * Math.PI;
				sectionId++;
				if (sectionId < 4)
					rem = sections[sectionId].length; // We haven't yet
				// touched the next
				// section.
				// cout
				// <<endl<<"Section ID: "<<sectionId<<" "<<near_wall<<" "<<far_wall<<" "<<trackWidth<<endl;
				// cout <<endl;
			} while (sectionId < 4 && !started_backwards && near_wall != 0.0); // While
			// you didn't get to the side
			// Figured out a feature, now I want to draw it.
			length = length < 0.0 ? 0.0 : length;
			newFeatures[pos] = length;

		}
		return newFeatures;
	}

	private double firstQuadrant(double angle) {
		// Make it positive:
		while (angle < 0.0)
			angle += Math.PI;
		if (angle > Math.PI / 2.0)
			angle = Math.PI - angle;

		return angle;
	}

	/**
	 * The biggest gamma that we're concerned with here would be < M_PI radians
	 * since you would not be able to avoid a wall on a sharper turn than that.
	 * toWall is set to the negative of its proper magnitude as a flag that the
	 * value has already been set.
	 */
	private double calcEscapePoint(double x1, double theta, double gamma,
			double r, double width, double segment) {
		double y2 = 2 * r;
		double hyp = y2 / Math.sin(theta); // 0 < theta < M_PI so it is always
		// positive
		double dx = Math.sqrt(hyp * hyp - y2 * y2); // shift it over to where I
		// started
		double x2 = dx + x1;
		// x3 will be 0, y3 will also be 0
		double x4 = r;
		double y4 = r * Math.tan(gamma);
		if (Math.abs(gamma - Math.PI / 2.0) < 1e-7
				|| Math.abs(gamma - 3 * Math.PI / 2.0) < 1e-7) // Divide by zero
			y4 = 1e10; // It's going to be really big.
		// Using the non-zero elements of the upper formula from:
		// http://mathworld.wolfram.com/Line-LineIntersection.html
		double D1212 = x1 * y2;
		double numX = D1212 * -x4;
		double denX = (x1 - x2) * (-y4) - (-y2) * (-x4);
		double numY = D1212 * -y4;
		double denY = denX;
		double x = numX / denX;
		double y = numY / denY;
		// Figure out how far the segment is connecting x1,y1 with the intercept
		dx = x - x1;
		segment = Math.sqrt(dx * dx + y * y);
		return segment;
	}

	private void calcIntersectPoint(double x1, double r, double theta,
			double[] retVal) {
		retVal[0] = -2.0 * r;
		retVal[1] = -2.0 * r; // Indicates no intersection
		// Adapted from:
		// http://mathworld.wolfram.com/Circle-LineIntersection.html
		if (Math.PI / 2.0 - theta < 1e-7 && Math.abs(x1) < r) { // just about
			// pi/2
			// Expecting the x value where we will hit the circle
			retVal[0] = x1;
			retVal[1] = x1;
		} else if (theta < 1e-7) { // Avoid trouble with sine
			// Going to hit one of the walls...
			if (x1 > 0) { // This means that we are checking against the outer
				// wall...
				retVal[0] = r;
			} else {// Checking against the inner wall
				retVal[0] = -r;
			}
			retVal[1] = retVal[0];
		} else { // More difficult calculation
			double dy = 2 * r; // - 0 omitted since y1 is 0.
			double dr = dy / Math.sin(theta); // 0 < theta < M_PI/2 so it is
			// always positive
			double dx = Math.sqrt(dr * dr - dy * dy); // shift it over to where
			// I started
			double D = x1 * dy; // dy == y2, x2*y1 == 0.
			if (r * r * dr * dr - D * D >= 0) { // We have intersection.
				// Figure out the closer of the x's, that is the point of
				// intersection
				double sq = Math.sqrt(r * r * dr * dr - D * D); // Sign of dy is
				// chosen to be
				// positive
				double xt1 = (D * dy + dx * sq) / (dr * dr);
				double xt2 = (D * dy - dx * sq) / (dr * dr);
				retVal[0] = xt1;
				retVal[1] = xt2;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#inGoalState()
	 */
	@Override
	public boolean inGoalState() {
		return currentTimeStep >= maxSteps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#inFailureState()
	 */
	@Override
	public boolean inFailureState() {
		return offCount >= FAIL_COUNT || stationary >= FAIL_STAT;
	}

	@Override
	public double getReward() {
		// Get the distance you traveled if you're inbounds
		// double reward = (fitness - lastFitness);
		// if(car.offroad)
		// reward = 0.0;
		double reward = 0.0;

		if(reverseReward){
			if (offCount >= FAIL_COUNT)
				reward = 5;
			else
				reward = - (fitness / currentTimeStep);

		}
		else{
			if (offCount >= FAIL_COUNT)
				reward = -5;
			else
				reward = fitness / currentTimeStep;
		}
		// System.out.println(reward);
		return reward;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#numberOfActions()
	 */
	@Override
	public int numberOfActions() {
		return NUM_ACTIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#numberOfPerceptions()
	 */
	@Override
	public int numberOfPerceptions() {
		return relevantFeatures + irrelevantFeatures + redundantFeatures + 1; // +1
		// for
		// velocity
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#getNetInput()
	 */
	@Override
	public double[] getNetInput() {
		double[] networkInput = null;

		if(featureType.totalFeatures != numPerceptions){
			networkInput = new double[featureType.totalFeatures];
		}
		else
			networkInput = new double[numPerceptions];

		// I'll assume that the car can't go more than 300 mph!
		networkInput[0] = state[0] / 300.0;

		// All of the distances need to be scaled somehow.
		// Currently done by 2*trackWidth (trackWidth being 100)
		for (int i = 1; i < Math.min(relevantFeatures + 1, networkInput.length); i++)
			// networkInput[i] = Math.min(200.0, state[i]) / 200.0;
			// networkInput[i] = 30.0 / (30.0 + state[i]);
			networkInput[i] = 50.0 / (50.0 + state[i]);

		if(featureType != null)
			featureType.fillFeatures(networkInput);
		return networkInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rl.problem.RLProblem#getScore()
	 */
	@Override
	public double getScore() {
		return fitness;
	}

	@Override
	protected void setstatelimits() {
		//_SL_ 8/30/13 Added to support the action translation to discretize this problem:
		statemin = new double[state.length];
		statemax = new double[state.length];
		for(int i = 0; i < state.length; ++i){
			statemin[i] = 0.0;
			statemax[i] = 1.0;
		}
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
		static int storedLength;
		int[] positions;

		@Override
		void init(int numRelevant, Properties props) {
			//Get the basics..
			super.init(numRelevant, props);
			//Only want to initialize this field 1x
			if(numRelevant >= totalFeatures){
				positions = new int[0];
				initials = new int[0];
			}
			else if(storedTransitions == null)
				synchronized(Stored.class){
					if(storedTransitions == null){
						//Read the transition lines from a file
						storedTransitions = new ArrayList<double[]>();
						File f = new File(props.getProperty(FEATURE_TYPE+".storedpath"));
						try(Scanner input = new Scanner(f)){
							while(input.hasNextLine()){
								//Get a line and break it up into the vector components
								String l = input.nextLine();
								Scanner line = new Scanner(l);
								line.useDelimiter("[\\[\\],\\s]+");
								if(storedLength == 0)
									storedLength = l.trim().split("[\\[\\],\\s]+").length;
								double[] parsed = new double[storedLength];
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
						int numPositions = (int)Math.ceil((totalFeatures-offset)/(double)storedLength);
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
				int end = Math.min(storedLength, totalFeatures-start);
				System.arraycopy(storedTransitions.get(positions[i]), 0, state, start, end);
				start += storedLength;
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
	}

	public static class AdditionalFeatures extends FeatureType{
		/** The additional features that will be added */
		private AdditionalTypes[] toAdd;
		private double[] factors = {1};
		private double[] summands = {0};
		private double[] computedState;
		private boolean recompute = true;

		@Override
		void init(int numRelevant, Properties props) {
			super.init(numRelevant, props);
			//Could make it so that only some of the additional types are added.
			//TODO make this more customizable.
			toAdd = new AdditionalTypes[10];
			//			for(int i = 1; i < 9; ++i)
			//				toAdd[i-1] = new DiffSensors(i, i+1);
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
						state[pos] = 50.0 / (50.0 + factors[iter] * base + summands[iter]); //Normalization like sensors
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

		static class DiffSensors implements AdditionalTypes{
			private int sensor1;
			private int sensor2;

			public DiffSensors(int sensor1, int sensor2){
				this.sensor1 = sensor1;
				this.sensor2 = sensor2;
			}

			@Override
			public double compute(double[] state) {
				return state[sensor1] - state[sensor2];
			}
		}

		static class AbsDiffSensors implements AdditionalTypes{
			private int sensor1;
			private int sensor2;

			public AbsDiffSensors(int sensor1, int sensor2){
				this.sensor1 = sensor1;
				this.sensor2 = sensor2;
			}

			@Override
			public double compute(double[] state) {
				return Math.abs(state[sensor1] - state[sensor2]);
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
	}

	public static class JAAMASSensors extends FeatureType{
		private RandomFeature randPart;
		private AdditionalFeatures redPart;
		private Stored storedPart;
		private double[] cached;
		private boolean changed = true;
		private int numRandom = 10;
		private int numRedund = 10;
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
		
	private void determineState() {
		car.Observe();
		features = computeFeatures(car);
		Situation s = car.getSituation();
		// NEAT want's the velocity and then all of the rangefinder things.
		state[0] = s.v;
		// Put all of the feature measurements:
		for (int i = 0; i < relevantFeatures + irrelevantFeatures
				+ redundantFeatures; i++) {
			state[i + 1] = features[i];
		}
	}

	public void doActions(double[] actions) {
		if(featureType != null) featureType.takeStep();

		// Figure out the current state:
		// _SL_ 3/18/11 I moved all of the state determination code into this
		// method so
		// it could be called to initialize the first state.
		steps++;
		currentTimeStep++;
		// Computing the action vector to update the state with
		ConVec result = new ConVec();
		// I don't want to be concerned with refueling right now.
		result.fuel_amount = 150; // fuel when starting
		result.alpha = (actions[0] - 0.5) * Math.PI;
		// The vc will always be in [0,1] from racer, should be something more
		// than that to allow for acceleration.
		// result.vc = actions[1] * (50 + Misc.vec_mag(car.xdot, car.ydot));
		// result.vc = actions[1] * 50 + Misc.vec_mag(car.xdot, car.ydot);
		result.vc = actions[1] * 300;
		result.request_pit = 0; // don't request a pit stop
		int currId = car.seg_id;

		double prevDistance = car.distance;
		car.MoveCar(result);
		determineState();
		if (initDistance < 0.0) {
			initDistance = car.distance;
			prevDistance = initDistance;
		}
		double newDistance = car.distance;
		double distance = newDistance - prevDistance;
		// Correct the e2 traveled if going across the finish line (prevDistance
		// would be large)
		if (prevDistance > newDistance + 100)
			distance += track.length;

		reward = 2 * distance;

		int nextId = car.seg_id;
		// System.out.println(currId+" "+nextId);
		// _SL_ 8/8/11 Added to force the learner to just address a single track
		// feature
		// Trying to determine if subtasks exist and exactly how much they help.
		// _SL_ Noticed a problem, possibly with setStartState or something, all
		// feature
		// values are 0.0 sometimes, causing the response to be NaN in here.
		// Just
		// skip to the next segment to avoid this problem.
		if (partitioned) {
			boolean isNaN = false;
			for (double d : actions)
				if (Double.isNaN(d))
					isNaN = true;
			if (isNaN) {
				nextId = currId + 1; // Trigger the next if statement
			}
			if (currId != nextId) {
				// finished = true; //When only one segment at a time was tested
				segmentIndex++;
				if (segmentIndex < tracksOfInterest.length){
					// setStartingState(tracksOfInterest[segmentIndex], 1.0, 0,
					// 0.0, init_speed);
					setStartingState(tracksOfInterest[segmentIndex], 1.0,
							random.nextInt() * 60 - 30, random.nextDouble()
							* track.width - track.width / 2.0,
							random.nextDouble() * 50 + init_speed);
					initDistance = car.distance;
				}
			}
		}
		// Compute the score after moving with these actions
		// Control calls drive -- this is from drive
		Situation s = car.getSituation();
		// Got the off and on course stuff figured here:
		if (Misc.stuck(s.backward, s.v, s.vn, s.to_lft, s.to_rgt, result)) {
			offCount++;
			reward = 0;
			if (partitioned && offCount >= FAIL_COUNT) {
				offCount = 0;
				segmentIndex++;
				if (segmentIndex < tracksOfInterest.length) {
					// setStartingState(tracksOfInterest[segmentIndex], 1.0, 0,
					// 0.0, init_speed);
					setStartingState(tracksOfInterest[segmentIndex], 1.0,
							random.nextInt() * 60 - 30, random.nextDouble()
							* track.width - track.width / 2.0,
							random.nextDouble() * 50 + init_speed);
					initDistance = car.distance;
				}
			}
		}

		if (curr_seg != s.seg_ID) {
			if (print_trans) {
				System.out.println("***Transitioning from segment " + curr_seg
						+ " to " + s.seg_ID + " ***");
				System.out.println("Rad = " + track.rgtwall[curr_seg].radius
						+ " -> " + track.rgtwall[s.seg_ID].radius);
			}
			curr_seg = s.seg_ID;
		}
		if (print_steps) {
			for (int i = 0; i < state.length; i++) {
				System.out.print(state[i] + "\t");
			}
		}
		if (print_actions) {
			System.out.print(actions[0] + "\t" + actions[1]);
		}
		if (print_steps || print_actions) {
			System.out.println();
		}
		score += reward;
		fitness = score;
	}

	public Car getCar() {
		return car;
	}

	public int getNumRel() {
		return relevantFeatures;
	}

	@Override
	public double[] getTerminalState() {
		return terminalState;
	}

	@Override
	public double doAction(int action) {
		throw new UnsupportedOperationException("Can't do this in RARS.");
	}

	@Override
	public void doAction(double action) {
		throw new UnsupportedOperationException("Can't do this in RARS.");
		
	}
}
