package mil.af.rl.problem.rars;

public class Misc {
	/**
	 * This routine analyses the parameters to determine if the car is in an 
	 * abnormal situation.  If so, it returns non-zero & sets the result
	 * vector so as to free the car.  If all is normal it returns zero. 
	 * This is a service for the robot drivers; it is only called by them. 
	 *
	 * @param backward      (in) backward
	 * @param v             (in) speed
	 * @param vn            (in) normal speed
	 * @param to_lft        (in) e2 to the left of the road
	 * @param to_rgt        (in) e2 to the right of the road
	 * @param alpha_ptr     (inout) alpha (direction)
	 * @param vc_ptr        (inout) vc (speed)
	 * @return              0 if OK 1 else.
	 */
	public static boolean stuck(boolean backward, double v, double vn, double to_lft, 
	          double to_rgt, ConVec vect) 
	{ 
	  if(to_lft < 0.0)                     // If over the left wall,  
	  {
	    if(vn > .5 * v)                    // test for more than 30 degrees off course 
	    {
	      reverse(v, vect); 
	      return true; 
	    } 
	    else if(vn > -.5 * v && backward)  // or going well backward
	    {
	      reverse(v, vect); 
	      return true; 
	    } 
	    else if(vn < -.5 * v)              // heading away from wall, 
	    {
	      vect.alpha = .03;                // turn to left 
	      vect.vc = (.66667 * v + 10.0);   // accelerate toward 30 fps 
	      return true; 
	    } 
	    else 
	    {
	      vect.alpha = -.03;               // turn to right 
	      vect.vc = (.66667 * v + 10.0);   // accelerate toward 30 fps 
	      return true; 
	    }
	  }
	  else if(to_rgt < 0.0)                // if over the right wall: 
	  {
	    if(vn < -.5 * v)                   // test for more than 30 degrees off course 
	    {
	      reverse(v, vect); 
	      return true; 
	    } 
	    else if(vn < .5 * v && backward)   // or going well backward 
	    {
	      reverse(v, vect); 
	      return true; 
	    }
	    else if(vn > .5 * v)               // heading away from wall, 
	    {
	      vect.alpha = -.03;               // turn to right 
	      vect.vc = .66667 * v + 10.0;     // accelerate toward 30 fps 
	      return true; 
	    } 
	    else 
	    {
	      vect.alpha = .03;                // turn to left 
	      vect.vc = .66667 * v + 10.0;     // accelerate toward 30 fps 
	      return true;
	    }
	  }
	  else if(backward)
	  {
	    if(vn > .866 * v)                  // you are going more-or-less sideways left 
	    {
	      vect.alpha = -.03; 
	      vect.vc = .66667 * v + 10; 
	      return true; 
	    } 
	    else if(vn < -.866 * v)            // you are going more-or-less sideways rt. 
	    {
	      vect.alpha = .03; 
	      vect.vc = .66667 * v + 10; 
	       return true; 
	    } 
	    else
	    {
	      reverse(v, vect); 
	      return true; 
	    }
	  }
	  else if(v < 15)                      // nothing wrong except you are going very slow: 
	  {
	    if(to_rgt > to_lft)                // you are on left side of track 
	    {
	      if(vn < -.7 * v)                 // and you are not heading very much to right 
	      {
	        vect.alpha = -.03; 
	      }
	      else 
	      {
	        vect.alpha = .03;
	      }
	    }
	    else                               // you are on the right side, 
	    {
	      if(vn > .7 * v)                  // and you are not heading very much to left 
	      {
	        vect.alpha = .03; 
	      }
	      else 
	      {
	        vect.alpha = -.03; 
	      }
	    }
	    vect.vc = .66667 * v + 10;         // acellerate moderately 
	    return true; 
	  }
	  return false;                            // We get here only if all is normal. 
	} 
	
	/**
	 * Call repeatedly to change direction by 180 degrees:
	 *
	 * @param v           
	 * @param alpha_ptr
	 * @param vc_ptr
	 */
	static void reverse(double v, ConVec vect) 
	{ 
	  if(v > 10.0)           // This is algorithm to reverse velocity vector: 
	  {
	    vect.vc = 0.0;       // if not going very slow, brake hard 
	  }
	  else
	  {
	    vect.vc = -15.0;     // when going slow enough, put 'er in reverse! 
	  }
	  vect.alpha = 0.0;      // don't turn. 
	} 
	
	/**
	 * Sqrt of sum of squares (used for vector magnitude)
	 *
	 * @param x             (in) x
	 * @param y             (in) y
	 * @return              magnitude
	 */
	static double vec_mag(double x, double y)
	{
	  return Math.sqrt(x * x + y * y); 
	} 
}
