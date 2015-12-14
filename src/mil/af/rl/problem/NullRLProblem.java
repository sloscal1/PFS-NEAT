package mil.af.rl.problem;

import mil.af.rl.problem.RLProblem;

/**
 * This RLProblem exists solely to avoid having NPTs be thrown when initializing
 * the fitness function class when using the Kilobot Sim. The Kilobot sim changes the
 * control loop, embedding the NN into the problem instead of creating a communications
 * loop to provide control.
 * 
 * Use this class with {@link neat.iface.KiloLearner}
 * @author sloscal1
 *
 */
public class NullRLProblem extends RLProblem {

	@Override
	protected void setstatelimits() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRandomState() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(int stateindex) {
		// TODO Auto-generated method stub

	}

	@Override
	public double doAction(int action) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void doAction(double action) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean inGoalState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inFailureState() {
		return false;
	}

	@Override
	public double getReward() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getScore() {
		return 0;
	}
	
	@Override
	public void reset() {
	}

	@Override
	public void applyActions(double[] actions) {

	}

}
