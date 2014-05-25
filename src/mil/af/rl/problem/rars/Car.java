package mil.af.rl.problem.rars;

public class Car {

	public static final double STARTING_SPEED = 20.0; // cars start at this
	// speed, ft/sec
	public static final double g = 32.2; // acceleration due to gravity,
	// ft./sec^2
	private static final long MAX_DAMAGE = 30000;
	private static final double MYU_MAX1 = 1.05;
	private static final double SLIPPING = 2.0;
	private static final double PM = 1e5;
	private static final double DRAG_CON = .0065;

	private Situation s;
	private Track currentTrack;
	int backward_count; // don't allow driving backwards
	double x, y, xdot, ydot, ang, adot; // current state of car (feet, seconds)
	double prex, prey, prang; // previous state drawn on screen
	double pre_xdot, pre_ydot; // previous velocity components
	boolean lap_flag; // changes from 0 to 1 on each crossing of finish line
	double to_end; // same as in s.to_end, above
	double to_rgt; // same as in s.to_rgt, above
	double vn; // same as in s.vn, above
	double cen_a, tan_a; // centripetal, tangential acceleration
	double pre_x_a, pre_y_a; // previous acceleration components
	double alpha, vc; // wheel angle of attack and wheel command velocity
	double prev_alpha; // previous value of alpha

	boolean offroad;
	boolean veryoffroad; // flags, set if off the track
	double power_req; // power requested by driver, divided by PwrMax
	double power; // power delivered, divided by PwrMax

	long damage; // accumulated damage units (out of race 30000)

	double start_time; // time instant of 1st crossing starting line
	double last_crossing; // time instant of most recent crossing

	int seg_id; // current track segment e1
	double distance; // how many feets travelled from SF lane
	long laps; // laps completed
	long laps_lead; // lead race at SF crossing

	double X, Y; // Car absolute coordinates!
	boolean out;

	// TODO: what is delta_time?
	double delta_time = .0549;

	public Car(Track trackIn) {
		s = new Situation();
		currentTrack = trackIn;
		x = y = 0; // This is for initializing of cars in qualifications
		X = Y = 0;
		prex = 0; // These 3 are initialized so that
		prey = 0; // draw_car() will work OK the
		prang = 0.0; // first time it is called.
		power = power_req = .9; // meaningless, but will be soon replaced by
		// move_car()

		tan_a = 0;
		last_crossing = 0;
		s.side_vision = 0;
		to_rgt = 0;
		offroad = false;
		out = false;
		distance = 0;
		damage = 0;
		
		seg_id = 0;
	}

	public Situation getSituation() {
		//Observe();
		return s;
	}

	/**
	 * Sets the situation to a previously stored one
	 * @param situation
	 */
	public void setSituation(Situation situation){
		this.s = situation;
	}
	
	public void putCar(double x_pos, double y_pos, double alf_ang) {
		putCar(x_pos, y_pos, alf_ang, 0, STARTING_SPEED);
	}
	
	public void putCar(Situation s) {
		x = s.x;
		y = s.y;
		ang = s.ang;
		to_end = s.to_end;
		prev_alpha = alpha = s.alpha;
		backward_count = 100;
		laps = s.laps_done;
		laps_lead = 0;
		lap_flag = s.lap_flag;
		distance = s.distance;
		vc = s.vc;
		xdot = vc * Math.cos(ang);
		ydot = vc * Math.sin(ang);
		pre_xdot = pre_ydot = 0.0;
		pre_x_a = pre_y_a = 0.0;
		tan_a = s.tan_a;
		cen_a = s.cen_a;
		
		prex = 0; // These 3 are initialized so that
		prey = 0; // draw_car() will work OK the
		prang = 0.0; // first time it is called.
		power = s.power;
		power_req = s.power_req;
		// move_car()
		damage = s.damage;
		seg_id = s.seg_ID;
		X = Y = 0.0;
		this.s = s;
	}
	
	public void putCar(double x_pos, double y_pos, double alf_ang, int segment, double speed) {
		x = x_pos;
		y = y_pos;
		ang = alf_ang;
		to_end = currentTrack.lftwall[segment].length; // not exact, but corrected by
		// observe()
		alpha = prev_alpha = 0.0;
		backward_count = 100;
		laps = -1; // to become 0 crossing the finish line at start of race
		// if(race_data.stage == QUALIFYING) seg_id = 0; //in race this is set
		// by arrange_cars
		laps_lead = 0;
		lap_flag = false;
		distance = 0.0;
		vc = speed;
		xdot = speed * Math.cos(ang);
		ydot = speed * Math.sin(ang);
		pre_xdot = pre_ydot = 0.0;
		pre_x_a = pre_y_a = 0.0;
		tan_a = cen_a = 0.0;
		
		
		prex = 0; // These 3 are initialized so that
		prey = 0; // draw_car() will work OK the
		prang = 0.0; // first time it is called.
		power = power_req = .9; // meaningless, but will be soon replaced by
		// move_car()

		tan_a = 0;
		last_crossing = 0;
		s.side_vision = 0;
		to_rgt = 0;
		offroad = false;
		out = false;
		distance = 0;
		damage = 0;
		
		seg_id = segment;

		// Also initialize car's public variables:
		X = Y = 0.0;
		s = new Situation();
		s.x=x;
		s.y=y;
		s.ang=ang;
		s.laps_done = -1;

	}

	/**
	 * This function uses the current state of the car, and the current track
	 * segment data, to compute the car's local situation as seen by the driver.
	 * (see struct situation)
	 */
	public void Observe() {
		double rad = 0; // current radius
		double dx, dy, xp, yp;
		double sine, cosine;
		double temp;
		int nex_seg; // segment ID of the next segment
		boolean flag = true; // controls possible repetition of calculations due
		// to
		// completion of a lap.
		if (out) // This gets set if the car is stuck and off the track
		{
			return;
		}

		s.v = Misc.vec_mag(xdot, ydot); // the actual speed
		// s.dead_ahead = dead_ahead; // copy the value set by move_car()
		s.power_req = power_req; // copy the value set by move_car()
		s.power = power; // copy the value set by move_car()
		// s.fuel = fuel; // copy the value set by move_car()
		s.damage = damage; // copy the value set by move_car()
		s.cen_a = cen_a; // copy the value set by move_car()
		s.tan_a = tan_a; // copy the value set by move_car()
		s.alpha = alpha; // copy the value set by move_car()
		s.vc = vc;
		s.start_time = start_time;
		s.lap_flag = false;
		// s.time_count = race_data.m_fElapsedTime;// copy the global value
		// s.stage = race_data.stage; // copy the global value
		// s.position = race_data.m_aPosOfCar[which]; // current position
		// s.started = started; // starting position
		// s.my_ID = which;
		// s.fuel_mileage = fuel_mileage; // miles per lb.
		// s.behind_leader = Behind_leader;

		// Copy private variables to public area:
		s.x = X = x;
		s.y = Y = y;

		// Computations below are based on the data in lftwall[] and
		// rgtwall[]. Radii are based on the inside rail in each case.
		int count = 0;
		while (flag && count < 2) // This loop repeats only when a segment boundary is
		// crossed,
		{ // in which case it repeats once:
			// compute sine and cosine of track direction:
			flag = false;
			sine = Math.sin(temp = currentTrack.rgtwall[seg_id].beg_ang);
			cosine = Math.cos(temp);
			nex_seg = seg_id + 1;
			if (nex_seg == currentTrack.NSEG) // which segment is
			// next
			{
				nex_seg = 0;
			}
			s.nex_len = currentTrack.rgtwall[nex_seg].length; // length and
			// radius
			s.nex_rad = currentTrack.lftwall[nex_seg].radius; // of next segment
			if (s.nex_rad < 0.0) // always use smaller radius
			{
				s.nex_rad = currentTrack.rgtwall[nex_seg].radius;
			}
			++nex_seg;
			if (nex_seg == currentTrack.NSEG) {
				nex_seg = 0;
			}
			s.after_len = currentTrack.rgtwall[nex_seg].length; // length and
			// radius
			s.after_rad = currentTrack.lftwall[nex_seg].radius; // and the one
			// after that
			if (s.after_rad < 0.0) // always use smaller radius
			{
				s.after_rad = currentTrack.rgtwall[nex_seg].radius;
			}
			++nex_seg;
			if (nex_seg == currentTrack.NSEG) {
				nex_seg = 0;
			}
			s.aftaft_len = currentTrack.rgtwall[nex_seg].length; // length and
			// radius
			s.aftaft_rad = currentTrack.lftwall[nex_seg].radius; // and the one
			// after
			// that
			if (s.aftaft_rad < 0.0) // always use smaller radius
			{
				s.aftaft_rad = currentTrack.rgtwall[nex_seg].radius;
			}

			s.cur_len = currentTrack.rgtwall[seg_id].length; // copy these two
			// fields:
			s.cur_rad = rad = currentTrack.lftwall[seg_id].radius; // rt. turn
			// will use
			// rgtwall
			if (seg_id != 0) // This is used to tell when the finish line
			{
				s.lap_flag = lap_flag = false; // is crossed, thus completing each lap
			}
			if (rad == 0) // if current segment is straight,
			{
				// xp and yp locate the car with respect to the beginning of the
				// right
				// hand wall of the straight segment. calculate them:
				dx = x - currentTrack.rgtwall[seg_id].beg_x;
				dy = y - currentTrack.rgtwall[seg_id].beg_y;
				xp = dx * cosine + dy * sine;
				yp = dy * cosine - dx * sine;
				s.to_rgt = yp; // fill in to_rgt and to_end:
				s.to_end = s.cur_len - xp;

				if (seg_id == 0) {
					if (xp > currentTrack.m_fFinish * s.cur_len && !lap_flag) {
						/*
						 * ################# This means the line crossing was
						 * noted: #############
						 */
						
						s.lap_flag = lap_flag = true;

						// Add 1 lap to lap count:
						++laps;
						s.laps_done++;
					}
				}

				// here we make sure we are still in the same segment:
				if (s.to_end <= 0.0) // see if a lap has been completed,
				{
					seg_id++;
					if (seg_id == currentTrack.NSEG) {
						seg_id = 0;
					}
					flag = true; // repeat the loop in context of next segment
					count++;
				}
				else
				{
					s.to_lft = currentTrack.width - yp; // fill in to_lft & cur_rad:
					s.cur_rad = 0.0;
					s.vn = ydot * cosine - xdot * sine; // compute cross-track speed
					s.backward = (xdot * cosine + ydot * sine < 0.0);
				}
			} else if (rad > 0.0) // when current segment is a left turn:
			{
				dx = x - currentTrack.rgtwall[seg_id].cen_x;// compute position
				// relative to
				// center
				dy = y - currentTrack.rgtwall[seg_id].cen_y;
				temp = Math.atan2(dy, dx); // this is the current angular
				// position
				s.to_end = currentTrack.rgtwall[seg_id].end_ang - temp
						- Math.PI / 2.0;// this is angle
				if (s.to_end > 1.5 * Math.PI) {
					s.to_end -= 2.0 * Math.PI;
				} else if (s.to_end < -.5 * Math.PI) {
					s.to_end += 2.0 * Math.PI;
				}
				if (s.to_end <= 0.0) // Handle segment crossing:
				{
					seg_id++;
					if (seg_id == currentTrack.NSEG) {
						seg_id = 0; // going from last segment to 1st
					}
					flag = true;
					count++;
				}
				else{
					s.to_lft = Misc.vec_mag(dx, dy) - rad;
					s.to_rgt = currentTrack.width - s.to_lft;
					s.vn = (-xdot * dx - ydot * dy) / Misc.vec_mag(dx, dy); // a
				// trig
				// thing
					s.backward = (ydot * dx - xdot * dy < 0.0);
				}
			} else {
				s.cur_rad = rad = currentTrack.rgtwall[seg_id].radius; // rt.
				// turn
				// needs
				// rgtwall
				dx = x - currentTrack.rgtwall[seg_id].cen_x;// compute position
				// relative to
				// center
				dy = y - currentTrack.rgtwall[seg_id].cen_y;
				temp = Math.atan2(dy, dx); // this is the current angular
				// position
				s.to_end = -currentTrack.rgtwall[seg_id].end_ang + temp
						- Math.PI / 2.0;// this is angle
				//make angle in 1st or 4th quadrant
				if (s.to_end < -.5 * Math.PI) {
					s.to_end += 2.0 * Math.PI;
				} else if (s.to_end >= 1.5 * Math.PI) {
					s.to_end -= 2.0 * Math.PI;
				}
				if (s.to_end <= 0.0) // Handle segment transistion:
				{
					seg_id++;
					if (seg_id == currentTrack.NSEG) {
						seg_id = 0;
					}
					flag = true;
					count++;
				}
				else
				{
					s.to_rgt = Misc.vec_mag(dx, dy) + rad;
					s.to_lft = currentTrack.width - s.to_rgt;
					s.vn = (xdot * dx + ydot * dy) / Misc.vec_mag(dx, dy); // a trig
				// thing
					s.backward = (xdot * dy - ydot * dx < 0.0);
				}
			}
		} // end while(flag) loop
		
		s.seg_ID = seg_id;
		// s.lap_time = lap_time;
		// s.lastlap_speed = lastlap_speed;
		// s.bestlap_speed = bestlap_speed;
		// s.laps_done = laps;
		// s.laps_to_go = args.m_iNumLap - laps;
		offroad = (s.to_lft < 0.0 || s.to_rgt < 0.0); // maybe set offroad flag
		veryoffroad = (s.to_lft < -currentTrack.width || s.to_rgt < -currentTrack.width); // maybe
		// veryoffroad
		to_end = s.to_end; // fill in these fields in Car object;
		to_rgt = s.to_rgt;
		vn = s.vn;

		// calculate e2 travelled from SF lane
		if (rad != 0) {
			distance = currentTrack.seg_dist[seg_id]
					- Math
							.abs((currentTrack.lftwall[seg_id].radius + currentTrack.rgtwall[seg_id].radius)
									* to_end / 2);
		} else {
			distance = currentTrack.seg_dist[seg_id] - to_end;
		}

		if (distance < 0) {
			distance += currentTrack.length;
		}

//		if(e2 < s.distance)
//		{
//			System.out.println("Went backwards");
//		}

		s.distance = distance;

		if (s.backward) {
			if (--backward_count == 0) {
				damage = MAX_DAMAGE;
			}
			return; // If going backwards, skip looking for nearby cars.
		} else {
			backward_count = 100; // Backwards driving allowed for max. 5
			// seconds
		}
	}

	/**
	 * combination of Control and MoveCar
	 * 
	 * @param output
	 */
	public void MoveCar(ConVec output) {
		vc = output.vc;
		alpha = output.alpha; // set private vars in car object
		// limit how fast alpha can change, and its maximum value:
		alpha = AlphaLimit(prev_alpha, alpha);
		prev_alpha = alpha;

		double D; // force on car from air, lb.
		double Fn, Ft; // normal & tangential components of track force vector
		double P; // power delivered to track, ft. lb. per sec.
		double v; // car's speed
		double Ln, Lt; // normal and tangential components of slip vector
		double l; // magnitude of slip vector, ft. per sec.
		double F; // force on car from track, lb.
		double x_a, y_a; // accelleration components in x & y directions
		double sine, cosine, temp;
		double mass = 75; // current mass of car

		v = Misc.vec_mag(xdot, ydot); // the car's speed, feet/sec

		sine = Math.sin(alpha);
		cosine = Math.cos(alpha); // alpha is angle of attack

		// air drag force (add up to 200% air drag for damage!)
		D = DRAG_CON * v * v * (2 * damage + MAX_DAMAGE) / MAX_DAMAGE;
		if (offroad) // if the car is off the track,
		{
			D += (0.6 + .008 * v) * mass * g; // add a lot more resistance
			if (veryoffroad) {
				D += 1.7 * mass * g;
			}
		}

		// //
		// // CALC POWER AND MOVE CAR
		// //

		int it = 0; // This is a loop counter.
		boolean isLoop = false;
		do {
			isLoop = false;
			Ln = -vc * sine;
			Lt = v - vc * cosine; // vector sum to compute slip vector
			l = Misc.vec_mag(Lt, Ln); // compute slip speed
			F = mass * g * friction(l); // compute friction force from track

			if (l < .0001) // to prevent possible division by zero
			{
				Fn = Ft = 0.0;
			} else {
				Fn = -F * Ln / l; // compute components of force vector
				Ft = -F * Lt / l;
			}
			// compute power delivered:
			P = (vc < 0.0 ? -vc : vc) * (Ft * cosine + Fn * sine);

			if (it == 0) // If this is the first time through here, then:
			{
				power_req = P / PM; // Tell the driver how much power it
				// requested.
				if (P > PM) // If the request was too high, reduce it to 100%
				// pwr.
				{
					++it;
					vc = zbrent(sine, cosine, v, v * cosine, vc, mass, .006);
					isLoop = true;
				}
			}
		} while (isLoop && it < 2);
		power = P / PM; // store this value in the car object

		temp = 1.0;

		// compute centripetal and tangential acceleration components:
		cen_a = Fn * temp / mass;
		tan_a = (Ft * temp - D) / mass;

		if (offroad) {
			// If off the track the car accumulates damage.
			// damage is proportional to square of acceleration:
			// damage from grass reduced 5x in version 0.70
			damage += ((tan_a * tan_a + cen_a * cen_a) / 50);
		}

		if (v < .0001) // prevent division by zero
		{
			adot = sine = cosine = 0.0;
		} else {
			adot = cen_a / v; // angular velocity
			sine = ydot / v;
			cosine = xdot / v; // direction of motion
		}
		x_a = tan_a * cosine - cen_a * sine; // x & y components of acceleration
		y_a = cen_a * cosine + tan_a * sine;

		// Advance the state using the Adam's predictor formula:
		x += (1.5 * xdot - .5 * pre_xdot) * delta_time;
		y += (1.5 * ydot - .5 * pre_ydot) * delta_time;
		pre_xdot = xdot;
		pre_ydot = ydot;
		xdot += (1.5 * x_a - .5 * pre_x_a) * delta_time;
		ydot += (1.5 * y_a - .5 * pre_y_a) * delta_time;
		pre_x_a = x_a;
		pre_y_a = y_a;
		if (v >= .0001) {
			s.ang = ang = Math.atan2(ydot, xdot); // new orientation angle
		}
	}

	/**
	 * This is the model of the track friction force on the tire. This force
	 * provides propulsion, cornering, and braking. Force is assumed to depend
	 * only on the slip speed, rising very rapidly with small slip speed, and
	 * then asymtotically approaching an upper limit. (This is similar to tires
	 * on unpaved surfaces.) There are two models here, the global variable
	 * "surface" chooses between them.
	 * 
	 * @param (in) given the slip speed [ft.per sec]
	 * @return returns the coef. of friction,
	 */
	private static double friction(double slip) {

		return MYU_MAX1 * (1.0 - Math.exp(-slip / SLIPPING));

	}

	/**
	 * This routine was adapted from the book "Numerical Recipes in C" by Press,
	 * et. al. It searches for a value of vc which causes the power to be very
	 * close to the maximum available. (This is Brent's method of root finding.)
	 * x1 and x2 are values of vc which bracket the root. b represents the
	 * variable vc. (See power_excess(), above.)
	 * 
	 * @param sine
	 *            (in)
	 * @param cosine
	 *            (in)
	 * @param v
	 *            (in) the current speed of the car [feet/sec]
	 * @param x1
	 *            (in) vc coodinate 1 (v*cosine) [feet/sec]
	 * @param x2
	 *            (in) vc coodinate 2 (vc) [feet/sec]
	 * @param mass
	 *            (in) the mass of the car [unit]
	 * @param tol
	 *            (in)
	 * @return the max vc depending of the powerpi
	 */
	static double zbrent(double sine, double cosine, double v, double x1,
			double x2, double mass, double tol) {
		final int ITMAX = 20;
		final double EPS = 1.0e-8;
		int iter;
		double a = x1, b = x2, c = x2, d = 0, e = 0, min1, min2;
		double fa = power_excess(a, sine, cosine, v, mass);
		double fb = power_excess(b, sine, cosine, v, mass);
		double fc, p, q, r, s, tol1, xm;
		double Ln, Lt; // normal and tangential components of slip vector
		double l; // magnitude of slip vector, ft. per sec.
		double F; // force on car from track, lb.
		double Fn, Ft; // tangential and normal (to car's path) force components

		if ((fa > 0.0 && fb > 0.0) || (fa < 0.0 && fb < 0.0)) {
			return b; // This should never happen.
		}
		fc = fb;
		for (iter = 1; iter <= ITMAX; iter++) {
			if ((fb > 0.0 && fc > 0.0) || (fb < 0.0 && fc < 0.0)) {
				c = a;
				fc = fa;
				e = d = b - a;
			}
			if (Math.abs(fc) < Math.abs(fb)) {
				a = b;
				b = c;
				c = a;
				fa = fb;
				fb = fc;
				fc = fa;
			}
			tol1 = 2.0 * EPS * Math.abs(b) + 0.5 * tol;
			xm = 0.5 * (c - b);
			if (Math.abs(xm) <= tol1 || fb == 0.0) {
				return b;
			}
			if (Math.abs(e) >= tol1 && Math.abs(fa) > Math.abs(fb)) {
				s = fb / fa;
				if (a == c) {
					p = 2.0 * xm * s;
					q = 1.0 - s;
				} else {
					q = fa / fc;
					r = fb / fc;
					p = s * (2.0 * xm * q * (q - r) - (b - a) * (r - 1.0));
					q = (q - 1.0) * (r - 1.0) * (s - 1.0);
				}
				if (p > 0.0) {
					q = -q;
				}
				p = Math.abs(p);
				min1 = 3.0 * xm * q - Math.abs(tol1 * q);
				min2 = Math.abs(e * q);
				if (2.0 * p < (min1 < min2 ? min1 : min2)) {
					e = d;
					d = p / q;
				} else {
					d = xm;
					e = d;
				}
			} else {
				d = xm;
				e = d;
			}
			a = b;
			fa = fb;
			if (Math.abs(d) > tol1) {
				b += d;
			} else {
				b += xm >= 0.0 ? Math.abs(tol1) : -Math.abs(tol1);
			}
			Ln = -b * sine;
			Lt = v - b * cosine; // vector sum to compute slip vector
			l = Misc.vec_mag(Lt, Ln); // compute slip speed
			F = mass * g * friction(l); // compute friction force from track
			if (l < .0001) // to prevent possible division by zero
			{
				Fn = Ft = 0.0;
			} else {
				Fn = -F * Ln / l; // compute components of force vector
				Ft = -F * Lt / l;
			}
			// compute power delivered:
			fb = (b < 0.0 ? -b : b) * (Ft * cosine + Fn * sine) - .9975 * PM;
		}
		return b;
	}

	/**
	 * This function is used only by zbrent(). It calculates power delivered to
	 * the tires using the same formulas as in move_car(). The value returned to
	 * the caller is that power minus 99.75% of maximum power. The target power
	 * used by zbrent() is therefore .9975 * PM. We use this instead of full
	 * power because zbrent() can err slightly on either side of the target, and
	 * we should not exceed PM.
	 * 
	 * @param vc
	 *            (in) the desired speed of the car [feet/sec]
	 * @param sine
	 *            (in) sin(alpha)
	 * @param cosine
	 *            (in) sin(alpha)
	 * @param v
	 *            (in) the current speed of the car [feet/sec]
	 * @param mass
	 *            (in) the mass of the car [unit]
	 * @return power excess
	 */
	static double power_excess(double vc, double sine, double cosine, double v,
			double mass) {
		double Ln, Lt; // normal and tangential components of slip vector
		double l; // magnitude of slip vector, ft. per sec.
		double F; // force on car from track, lb.
		double Fn, Ft; // tangential and normal (to car's path) force components

		Ln = -vc * sine;
		Lt = v - vc * cosine; // vector sum to compute slip vector
		l = Misc.vec_mag(Lt, Ln); // compute slip speed
		F = mass * g * friction(l); // compute friction force from track
		if (l < .0001) // to prevent possible division by zero
		{
			Fn = Ft = 0.0;
		} else {
			Fn = -F * Ln / l; // compute components of force vector
			Ft = -F * Lt / l;
		}
		// compute power excess over target value of .9975*PM:
		return (vc < 0.0 ? -vc : vc) * (Ft * cosine + Fn * sine) - .9975 * PM;
	}

	/**
	 * limits the rate of change and maximum value of the angle of attack
	 * 
	 * @param was
	 *            This is what alpha was
	 * @param request
	 *            The robot wants this alpha
	 */
	private double AlphaLimit(double was, double request) {
		final double MAX_RATE = 3.2; // maximum radians per second possible
		final double MAX_ALPHA = 1.0; // maximum radians possible

		double alpha; // the result to return

		if (request - was > MAX_RATE * delta_time) // want more positive alpha
		{
			alpha = was + MAX_RATE * delta_time;
		} else if (was - request > MAX_RATE * delta_time) // want more negative
		// alpha
		{
			alpha = was - MAX_RATE * delta_time;
		} else {
			alpha = request;
		}

		if (alpha > MAX_ALPHA) {
			alpha = MAX_ALPHA;
		} else if (alpha < -MAX_ALPHA) {
			alpha = -MAX_ALPHA;
		}

		return alpha;
	}
}
