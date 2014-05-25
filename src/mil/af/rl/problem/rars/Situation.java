package mil.af.rl.problem.rars;

public class Situation implements Cloneable{
	double cur_rad; // radius of inner track wall, 0 = straight, minus = right
	double cur_len; // length of current track segment (angle if curve)
	double to_lft; // e2 to left wall
	double to_rgt; // e2 to right wall
	double to_end; // how far to end of current track seg. (angle or feet)
	double v; // the speed of the car, feet per second
	double vn; // component of v perpendicular to track direction
	double nex_len; // length of the next track segment (angle if curve)
	double nex_rad; // radius of inner wall of next segment (or 0)
	double after_rad; // radius of the segment after that one. (or 0)
	double after_len; // length of the segment after that one.
	double aftaft_rad; // radius of segment after that one. (or 0)
	double aftaft_len; // length of segment after that one.
	double cen_a, tan_a; // centripetal, tangential acceleration
	double alpha, vc; // wheel angle of attack and wheel command velocity
	double power_req; // ratio: power requested by driver to maximum power
	double power; // power delivered, divided by PwrMax
	double fuel; // lbs. of fuel remaining
	double fuel_mileage; // miles per lb.
	double time_count; // elapsed time since start of race
	double start_time; // value of time_count when first lap begins
	double bestlap_speed; // speed of cars best lap in that race fps
	double lastlap_speed; // speed of last lap, fps
	double lap_time; // time in seconds to complete most recent lap
	double distance; // e2 traveled from start of first segment
	double behind_leader; // seconds behind leader on last SF crossing
	int dead_ahead; // set when there is a car dead ahead, else 0
	long damage; // accumulated damage units (out of race 30000)
	double x, y, ang;
	// Stage stage; // What's happening, what stage of the competition?
	int my_ID; // to tell which car object you are driving
	int seg_ID; // current segment number, 0 to NSEG-1
	int laps_to_go; // laps remaining to be completed
	int laps_done; // laps completed
	int out_pits; // 1 if coming out from pits after stop - adjust your speed
	int go_pits; // becomes 1 when main program takes over for pitting
	int position; // 0 means leading, 1 means 2nd place, etc.
	int started; // where were we on the starting grid?
	boolean lap_flag; // changes from 0 to 1 on each crossing of finish line
	boolean backward; // set if cars motion is opposed to track direction
	int starting; // if not zero, robot knows to initialize data
	int side_vision; // allow cars alongside in s.nearby data
	// rel_state* nearby; // relative states of three cars in front of you
	// void* data_ptr; // pointer to driver's scratchpad RAM area

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
