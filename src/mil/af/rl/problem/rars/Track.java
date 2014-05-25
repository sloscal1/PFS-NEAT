/**
 * 
 */
package mil.af.rl.problem.rars;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * @author owner
 * 
 */
public class Track implements Configurable {
	private final static int MAX_SECTION = 10;
	private final static int MAX_OBJECT3D = 100;
	private static final double DEGPRAD = 180/Math.PI;
	// Profile with height
	// The end of a profile is detected by a 0 or 1
	private static ProfileHeight g_aProfileHeight[];
	
	static{
		double[] arr1 = { 0, 1 };
		double[] arr2 = { 0, 0.04, 0.2, 0.5, 0.8, 0.96, 0.999, 0.96, 0.8, 0.5, 0.2, 0.04, 0 };
		double[] arr3 = { 0, 0.04, 0.2, 0.5, 0.8, 0.96, 1 };
		ProfileHeight p1 = new ProfileHeight("flat", arr1);
		ProfileHeight p2 = new ProfileHeight("hill", arr2);
		ProfileHeight p3 = new ProfileHeight("s", arr3);
		g_aProfileHeight = new ProfileHeight[]{p1, p2, p3, null};
	}
	
	private final static String TRACK_NAME = "rars.args.track";
	private static final String TRACK_PATH = "rars.args.trackPath";
		
	private Track_desc m_oTrackDesc;               // Precalculated track description
	 public String m_sFileName;                  // The filename this object was read from
	 public String m_sShortName;                 // The filename this object was read from (without .trk)
	 public double width;                          // width of track, feet   
	 public double length;                         // length of track, feet (5280 ft/mile, 3281 ft/km)   
	 public int NSEG;                              // number of segments of the track (<>m_iNumSegment)
	 public int m_iNumSegment;                     // number of segments of the track + the segments of the additional sections like pitstop (<>NSEG)
	 public int m_iNumObject3D;                    // number of Object3D (decorations) of the track
	 public int m_iNumSection;                     // number of sections
	  
	  // segments
	 public Segment[] rgtwall;                   // replaces trackout   
	 public Segment[] lftwall;                   // replaces trackin   
	 public Segment3D[] m_aSeg;                    // Segment of the track(with rgtwall), mostly for 3D info of the track
	 public Segment3D   m_oDefault;                // default segment

	  // I use static size here (not very clean) but it allows to remove a lot of code about allocation/desallocation of memory
	  public Object3D[] m_aObject3D = new Object3D[MAX_OBJECT3D]; // list of the Object3D (decorations) of the track
	  public Section[] m_aSection = new Section[MAX_SECTION];   // list of the sections track
	  public Object3D    m_oSky;                    // Name of the sky Object3D 

	  public double[] seg_dist;                  // e2 from SF lane to end of each segment
	  public int         pit_side;                  // car needs to know pit location
	  public double      pit_entry;                 // to avoid entering and exiting cars   
	  public double      pit_exit;                  // and to calculate estimated pit times   
	  public double      pit_speed;                 //    
	  public Fastest_lap m_oRecord;                 // fastest ever lap on this race

	  ////
	  //// Data read from the track file
	  ////
	  // Description of the track
	  public String m_sName;            // Name of the track (<>filename)
	  public String m_sAuthor;          // Author of the track
	  public String m_sDescription;    // Description of the track
	  public int  m_iVersion;             // Version of the track

	  // Unit data
	  public String m_sUnitDistanceName;// Name of the e2 unit
	  public double m_fUnitDistanceScale; // Number of (Rars) feets in a e2 unit

	  // Header data
	  public double m_fFinish;            // Fraction of segment 0 prior to finish line (0->1)   
	  public double m_fRgtStartX;         // Coordinates of where to start drawing track (feet)  
	  public double m_fRgtStartY;         //    
	  public double m_fStartAng;          // Angle of first segment (radians)   
	  public int    m_iStartRows;         // How many rows of starting cars

	  // XWindow data
	  public double m_fXMax;              // Max coordinate in XWindow (feet) + see CalcMinMax
	  public double m_fYMax;              // 
	  public double m_fScoreBoardX;       // These are in feet, track coordinates   
	  public double m_fScoreBoardY;       //   (where the scoreboard is located)   
	  public double m_fLeaderBoardX;      // Upper left corner of leader board, feet   
	  public double m_fLeaderBoardY;      //
	  public double m_fInstPanelX;        // Instrument Panel   
	  public double m_fInstPanelY;        //
	  public double m_fMessageX;          // Where "Length of track... " starts
	  public double m_fMessageY;          // 

	  // Pitstop Data
	  public int m_iPitSide;              // 1 is right side; left side is -1
	  public double m_fPitEntry;          // where car starts steering to pits   
	  public double m_fPitExit;           // back on the track   
	  public double m_fPitLaneStart;      // pit start-end denote stopping area    
	  public double m_fPitLaneEnd;        // those four are compared to s.distance    
	  public double m_fPitLaneSpeed;      // feet per second, divide by 1.8 to get mph    

	  ////
	  //// Others
	  ////
	  public double SCALE;                // feet per pixel, mapping track to screen   
	  public double from_start_to_seg1;   // e2 from start/finish line to seg end   
	  public double finish_rx, finish_ry; // These four variables are used to
	  public double finish_lx, finish_ly; // locate the finish line on the screen.
	  public double m_fLftStartX;         // Coordinates of where to start drawing track (feet)  
	  public double m_fLftStartY;         //    
	  public double m_fXMin;              // Min coordinate of the track - see CalcMinMax
	  public double m_fYMin;              //
	  public boolean m_bCopy;              // Allow to copy the track for the edition (TrackEditor) 
	private String m_sTrackPath;
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.anji.util.Configurable#init(com.anji.util.Properties)
	 */
	@Override
	public void init(Properties props) throws Exception {
		m_sName = "";
		  m_sAuthor = "";
		  m_sDescription = "";
		  m_iVersion = 0;

		  m_sUnitDistanceName = "feet";
		  m_fUnitDistanceScale = 1;
		  m_fFinish = 0;
		  m_iStartRows = 2;
		  m_fRgtStartX = m_fRgtStartY = m_fStartAng = 0;

		  m_iPitSide = 0;
		  m_fPitEntry = m_fPitExit = 0.0;
		  m_fPitLaneStart = m_fPitLaneEnd = 0.0;
		  m_fPitLaneSpeed = 0.0;

		  m_fXMax = m_fYMax = 0;
		  m_fScoreBoardX = m_fScoreBoardY = 0;
		  m_fInstPanelX = m_fInstPanelY = 0;
		  m_fLeaderBoardX = m_fLeaderBoardY = 0;
		  m_fMessageX = m_fMessageY = 0;

		  rgtwall = null;
		  lftwall = null;
		  NSEG = 0;
		  m_iNumSegment = 0;
		  m_iNumObject3D = 0;
		  m_iNumSection = 0;
		  m_bCopy = false;

		  //m_oSky.m_sModel = "model/sky.3ds";

		m_sFileName = props.getProperty(TRACK_NAME);
		m_sTrackPath = props.getProperty(TRACK_PATH);

		// TRK = Format version 0.8x and before
		if (!m_sFileName.toLowerCase().endsWith(".trk")) {
			m_sFileName += ".trk";
		}

		try {
			// open the track file for input
			Scanner inf = new Scanner(new FileReader(m_sTrackPath + "/" + m_sFileName));
			int degrees; // will become 1 for degrees, 0 for radians
			degrees = -1;
			int i;

			NSEG = inf.nextInt();
			inf.nextLine();
			m_fXMax = inf.nextDouble();
			m_fYMax = inf.nextDouble();
			inf.nextLine();
			width = inf.nextDouble();
			inf.nextLine();

			m_fRgtStartX = inf.nextDouble();
			m_fRgtStartY = inf.nextDouble();
			m_fStartAng = inf.nextDouble();
			inf.nextLine();

			m_fScoreBoardX = inf.nextDouble();
			m_fScoreBoardY = inf.nextDouble();
			inf.nextLine();

			m_fLeaderBoardX = inf.nextDouble();
			m_fLeaderBoardY = inf.nextDouble();
			inf.nextLine();

			m_fInstPanelX = inf.nextDouble();
			m_fInstPanelY = inf.nextDouble();
			inf.nextLine();

			m_fMessageX = inf.nextDouble();
			m_fMessageY = inf.nextDouble();
			inf.nextLine();

			m_fFinish = inf.nextDouble();
			m_iStartRows = inf.nextInt();
			inf.nextLine();

			m_iPitSide = inf.nextInt();
			;
			inf.nextLine();

			m_fPitEntry = inf.nextDouble();
			m_fPitLaneStart = inf.nextDouble();
			inf.nextLine();

			m_fPitLaneEnd = inf.nextDouble();
			m_fPitExit = inf.nextDouble();
			inf.nextLine();

			m_fPitLaneSpeed = inf.nextDouble();
			inf.nextLine();
		    
			// Create table rgtwall/leftwall/seg3d
			m_iNumSegment = NSEG;
			AllocSegment();
			
			for( i=0; i<m_iNumSegment; i++ )           // read the segment data:
		    {
		      m_aSeg[i].m_fRadius = inf.nextDouble();
		      if( m_aSeg[i].m_fRadius==0.0 )
		      {
		        m_aSeg[i].m_fLength = inf.nextDouble();
		      }
		      else
		      {
		        m_aSeg[i].m_fArc = inf.nextDouble();
		      }
		      inf.nextLine();
		      // this section of the loop handles (degree vs. radians):
		      if(degrees == -1 && m_aSeg[i].m_fRadius != 0.0)
		      {
		        if(m_aSeg[i].m_fArc < 5.0)
		        {
		          degrees = 0;
		        }
		        else
		        {
		          degrees = 1;
		        }
		      }
		      if(degrees == 1 && m_aSeg[i].m_fRadius != 0.0)
		      {
		        m_aSeg[i].m_fArc /= DEGPRAD;
		      }
		    }
			// Section
		    m_iNumSection = 1;
		    m_aSection[0] = new Section();
		    m_aSection[0].m_sName = "track";
		    m_aSection[0].m_iFirstSeg = 0;
		    m_aSection[0].m_iLastSeg = NSEG-1;
		    inf.close();
		} catch (FileNotFoundException ex) {
			System.out.println("No track file found!");
			System.exit(0);
		}
	}

	private void AllocSegment() {
		rgtwall = new Segment[m_iNumSegment];
		lftwall = new Segment[m_iNumSegment];
		m_aSeg   = new Segment3D[m_iNumSegment];
		seg_dist = new double[m_iNumSegment];
		for(int i=0; i<m_iNumSegment; i++)
		{
			rgtwall[i] = new Segment();
			lftwall[i] = new Segment();
			m_aSeg[i]   = new Segment3D();
		}
	}
	
	/**
	 * Rebuild the lftwall, rgtwall from m_aSeg
	 */
	public void Rebuild()
	{
		int i;

		  // ShortName
		  m_sShortName = m_sFileName;
		 // char * p = strchr( m_sShortName, '.' );
		  //*p = 0;

		  NSEG = m_aSection[0].m_iLastSeg+1;


		  lftwall = new Segment[m_iNumSegment];
		  seg_dist = new double[m_iNumSegment];

		  // Reset the remumbering (needed for AddSegment/DeleteSegment)
		  for( i=0; i<m_iNumSegment; i++ )
		  {
		    m_aSeg[i].m_iPrvSeg = -1;
		    m_aSeg[i].m_iNxtSeg = -1;
		    m_aSeg[i].m_iOtherYSeg = -1;
		  }
		      
		  for( i=0; i<m_iNumSegment; i++ )
		  {
		    // Fill in lftwall[] from rgtwall[]
		    rgtwall[i].radius = m_aSeg[i].m_fRadius;
		    lftwall[i] = new Segment();
		    if(rgtwall[i].radius == 0.0)
		    {
		      lftwall[i].radius = 0.0;
		      rgtwall[i].length = lftwall[i].length = m_aSeg[i].m_fLength;
		    }
		    else
		    {
		      lftwall[i].radius = rgtwall[i].radius - width;
		      rgtwall[i].length = lftwall[i].length = m_aSeg[i].m_fArc;
		    }

		    // Rebuild seg3D
		    m_aSeg[i].Rebuild();

		    // Previous segment
		    int prv_seg = i-1;
		    int nxt_seg = i+1;
		    if( m_aSeg[i].m_iPrvSeg<0 )
		    {
		      if( i==0 )
		      {
		        prv_seg = NSEG-1;
		        m_aSeg[NSEG-1].m_iNxtSeg = 0;
		      }
		      m_aSeg[i].m_iPrvSeg = prv_seg ;
		    }
		    if( m_aSeg[i].m_iNxtSeg<0 )
		    {
		      int iSection = GetSectionId( i );
		      if( i!=m_aSection[iSection].m_iLastSeg )
		      {
		        m_aSeg[i].m_iNxtSeg = nxt_seg ;
		      }
		    }
		    if( m_aSeg[i].m_sType.equals( "y" ) )
		    {
		      for( int j=0; j<m_iNumSection; j++ )
		      {
		        if( m_aSeg[i].m_sSection.equals( m_aSection[j].m_sName ) )
		        {
		          m_aSeg[i].m_iOtherYSeg = m_aSection[j].m_iFirstSeg;
		          m_aSeg[m_aSection[j].m_iFirstSeg].m_iOtherYSeg = i;
		          m_aSeg[m_aSection[j].m_iFirstSeg].m_iPrvSeg = prv_seg ;
		        }
		      }
		    }
		    else if( m_aSeg[i].m_sType.equals( "yinv" ) )
		    {
		      for( int j=0; j<m_iNumSection; j++ )
		      {
		        if( m_aSeg[i].m_sSection.equals( m_aSection[j].m_sName ) )
		        {
		          m_aSeg[i].m_iOtherYSeg = m_aSection[j].m_iLastSeg;
		          m_aSeg[m_aSection[j].m_iLastSeg].m_iOtherYSeg = i;
		          m_aSeg[m_aSection[j].m_iLastSeg].m_iNxtSeg = nxt_seg;
		        }
		      }
		    }
		  }

		  length = 0;
		  for( i=0; i<NSEG; i++ )
		  {
		    // Track length determined here
		    if( lftwall[i].radius !=0 )
		    {
		      length += Math.abs(lftwall[i].length*(lftwall[i].radius + rgtwall[i].radius)/2);
		    }
		    else
		    {
		      length += lftwall[i].length;
		    }
		  }

//TODO: what is this?
//		  if(args.m_iRaceLength) // if lap count is not explicitly declared use race length 
//		  {
//		    // This is an exception in args. (an "arg" is normally known before the start of the runtime)
//		    // But here, the length is not known before the track is read...
//		    args.m_iNumLap = long(args.m_iRaceLength*5280/length) + 1; // to find lap_count
//		  }

		  // initialize these global variables:
		  from_start_to_seg1 = (1.0 - m_fFinish) * rgtwall[0].length;
		  m_fLftStartX = m_fRgtStartX - width*Math.sin(m_fStartAng); //lftwall
		  m_fLftStartY = m_fRgtStartY + width*Math.cos(m_fStartAng);
		  finish_rx = m_fRgtStartX + m_fFinish * rgtwall[0].length * Math.cos(m_fStartAng);// rgt
		  finish_ry = m_fRgtStartY + m_fFinish * rgtwall[0].length * Math.sin(m_fStartAng);
		  finish_lx = finish_rx - width*Math.sin(m_fStartAng); //lftwall
		  finish_ly = finish_ry + width*Math.cos(m_fStartAng);

		  // Find additional members of segment structure from known data:
		  track_setup( m_fRgtStartX, m_fRgtStartY, m_fStartAng, rgtwall );
		  track_setup( m_fLftStartX, m_fLftStartY, m_fStartAng, lftwall );
		  CalcMinMax();

		  // Calculate e2 to end of each track segment.
		  // Distance and seg_dist counting starts from SF lane in ver.0.70!
		  // This is required for calculating "e2" that is part of car
		  // object and is used for nearby_cars in check_nearby
		  // and for locating pit entry, pitlane and pit exit positions
		  // and for arranging cars on their starting positions
		  for( i=0; i<NSEG; i++ ) 
		  {
		    if( i!=0 ) // not first segment
		    {
		       seg_dist[i] = seg_dist[i-1]; // take old value...
		    }
		    else  // first segment
		    {
		      seg_dist[i] = -m_fFinish*lftwall[0].length;
		    }
		     
		    if( lftwall[i].radius!=0 )   // and add length of this segment in feets!
		    {
		       seg_dist[i] += Math.abs(lftwall[i].length *
		               (lftwall[i].radius + rgtwall[i].radius)/2);
		    }
		    else                     // and add length of this segment in feets!
		    {
		      seg_dist[i] += lftwall[i].length;
		    }
		  }

		  m_oTrackDesc = new Track_desc();
		  // precalculate the track description
		  m_oTrackDesc.sName = m_sFileName; // track filename
		  m_oTrackDesc.NSEG = NSEG;
		  m_oTrackDesc.width = width;
		  m_oTrackDesc.length = length;           // drivers can get length from here
		  m_oTrackDesc.rgtwall = rgtwall;         // replaces trackout
		  m_oTrackDesc.lftwall = lftwall;         // replaces trackin
		  m_oTrackDesc.trackout = rgtwall;
		  m_oTrackDesc.trackin = lftwall;
		  m_oTrackDesc.seg_dist = seg_dist;       // e2 from SF to the end of each segment
		  m_oTrackDesc.pit_side = m_iPitSide;     // car needs to know pit location
		  m_oTrackDesc.pit_entry = m_fPitEntry;   // to avoid entering and exiting cars
		  m_oTrackDesc.pit_exit = m_fPitExit;     // and to calculate estimated pit times
		  m_oTrackDesc.pit_speed = m_fPitLaneSpeed; // mph
	}

	/**
	 * Find the min and the max of the track 
	 */
	private void CalcMinMax() {
		int i, j;

		  // look for the min and max points of the track
		  m_fXMin = (int) lftwall[0].beg_x;
		  m_fYMin = (int) lftwall[0].beg_y;
		  m_fXMax = (int) lftwall[0].beg_x;
		  m_fYMax = (int) lftwall[0].beg_y;

		  for( i=0; i<m_iNumSegment; i++ ) 
		  {  
		    // for each segment:
		    if( lftwall[i].radius!=0.0 )
		    {
		      int step = (int)( lftwall[i].length*5.0 );
		      double ang = lftwall[i].beg_ang;
		       
		      for( j=0; j<step; j++ )
		      {
		        m_fXMax = Math.max( m_fXMax, lftwall[i].cen_x + lftwall[i].radius*Math.sin(ang) );
		        m_fXMax = (int) Math.max( m_fXMax, lftwall[i].cen_x + rgtwall[i].radius*Math.sin(ang) );
		        m_fYMax = (int) Math.max( m_fYMax, lftwall[i].cen_y - lftwall[i].radius*Math.cos(ang) );
		        m_fYMax = (int) Math.max( m_fYMax, lftwall[i].cen_y - rgtwall[i].radius*Math.cos(ang) );
		        m_fXMin = (int) Math.min( m_fXMin, lftwall[i].cen_x + lftwall[i].radius*Math.sin(ang) );
		        m_fXMin = (int) Math.min( m_fXMin, lftwall[i].cen_x + rgtwall[i].radius*Math.sin(ang) );
		        m_fYMin = (int) Math.min( m_fYMin, lftwall[i].cen_y - lftwall[i].radius*Math.cos(ang) );
		        m_fYMin = (int) Math.min( m_fYMin, lftwall[i].cen_y - rgtwall[i].radius*Math.cos(ang) );

		        if( lftwall[i].radius>0.0 )
		        {
		          ang+=0.2;
		          if(ang > 2.0 * Math.PI) ang -= 2.0 * Math.PI;
		        }
		        else 
		        {
		          ang-=0.2;
		          if(ang < 2.0 * Math.PI) ang += 2.0 * Math.PI;
		        }
		      }
		    } 
		    else
		    {
		      m_fXMax = (int) Math.max( m_fXMax, lftwall[i].beg_x );
		      m_fXMax = (int) Math.max( m_fXMax, rgtwall[i].beg_x );
		      m_fYMax = (int) Math.max( m_fYMax, lftwall[i].beg_y );
		      m_fYMax = (int) Math.max( m_fYMax, rgtwall[i].beg_y );
		      m_fXMin = (int) Math.min( m_fXMin, lftwall[i].beg_x );
		      m_fXMin = (int) Math.min( m_fXMin, rgtwall[i].beg_x );
		      m_fYMin = (int) Math.min( m_fYMin, lftwall[i].beg_y );
		      m_fYMin = (int) Math.min( m_fYMin, rgtwall[i].beg_y );
		    }
		  }
	}

	/**
	 * Fills in the un-initialized portions of the segment array.
	 *
	 * @param xstart    (in) coordinates of starting point
	 * @param ystart    (in) coordinates of starting point
	 * @param angstart  (in) starting tangent angle 
	 * @param seg       (in) pointer to structure that defines path
	 */
	private void track_setup(double xstart, double ystart,
			double angstart, Segment[] seg) {
		 double cenx, ceny;          // center of circle arc
		  double radius;              // radius of circle arc (negative == rt. turn)
		  double x, y, ang;           // position and direction of start of segment
		  double newx, newy, newang;  // and the one after that  (alf in radians)
		  int i;

		  seg[NSEG-1].end_x   = xstart;  
		  seg[NSEG-1].end_y   = ystart;  
		  seg[NSEG-1].end_ang = angstart;  // store starting point & direction

		  for( i=0; i<m_iNumSegment; i++ )                 // for each segment:
		  {    
		    int prv = m_aSeg[i].m_iPrvSeg;
		    x   = seg[prv].end_x;
		    y   = seg[prv].end_y;
		    ang = seg[prv].end_ang;
		    radius = seg[i].radius;
		    if(radius == 0.0)         // is this a straightaway?
		    {                   
		      newx = x + seg[i].length * Math.cos(ang);         // find end coordinates
		      newy = y + seg[i].length * Math.sin(ang);
		      newang = ang;                                // direction won't change
		    }
		    else 
		    {
		      if(radius > 0.0) 
		      {
		        cenx = x - radius * Math.sin(ang);              // compute center location:
		        ceny = y + radius * Math.cos(ang);
		        newang = ang + seg[i].length;              // compute new direction
		        if(newang > 2.0 * Math.PI)
		        {
		          newang -= 2.0 * Math.PI;
		        }
		      }
		      else 
		      {
		        cenx = x - radius * Math.sin(ang);              // compute center location:
		        ceny = y + radius * Math.cos(ang);
		        newang = ang - seg[i].length;              // compute new direction
		        if(newang < -2.0 * Math.PI)
		        {
		          newang += 2.0 * Math.PI;
		        }
		      }
		      seg[i].cen_x = cenx;   seg[i].cen_y = ceny;
		      newx = cenx + radius * Math.sin(newang);          
		      newy = ceny - radius * Math.cos(newang);
		    }
		    seg[i].beg_ang = ang;
		    seg[i].beg_x = x;      seg[i].beg_y = y;       
		    seg[i].end_ang = newang;                       
		    seg[i].end_x = newx;   seg[i].end_y = newy;    
		  }
	}

	/**
	 * Find a section id of a segment if not found
	 */
	private int GetSectionId(int iSeg) {
		for( int i=0; i<m_iNumSection; i++ )
		  {
		    if( iSeg>=m_aSection[i].m_iFirstSeg && iSeg<=m_aSection[i].m_iLastSeg )
		    {
		      return i;
		    }
		  }
		  // It should never come here.
		  return -1;
	}

	public Track(Properties props) {
		try {
			init(props);
			Rebuild();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class Segment {
		public double radius = 0; // 0.0 means straight line, < 0.0 means right
									// turn
		public double length = 0; // radians if an arc, feet if straight line
		public double beg_x, beg_y; // coordinates of begining of segment
		public double end_x, end_y; // coordinates of end
		public double cen_x, cen_y; // coordinates of arc center (if arc)
		public double beg_ang, end_ang; // path angles, radians, 0.0 is in x
										// direction
	}
	
	/**
	 * Data of one side (right, left) for 3D 
	 */
	class Segment3D
	{
	    SegmentSide3D lft;
	    SegmentSide3D rgt;

	    // Data read from file
	    double m_fLength;                  // (Straight) Length of a segment for a straight
	    double m_fRadius;                  // (Curve) Radius of a segment 
	    double m_fArc;                     // (Curve) Angle of the curve 
	    double m_fEndZ;                    // Height at the end of the segment
	    double m_fEndBanking;              // Banking at the end of the segment
	    double m_fHillHeight;              // Height of a hill (profile_height=hill)
	    String m_sName;                  // Name of the segment
	    String m_sProfileHeight;         // Type of height profile
	    String m_sType;                  // Type of the segment
	    String m_sSection;               // Section name for y a yinv type of segment
	    String m_sModel;                 // 3DS Model for special segments where we do not want to use the 3D model generated automatically by Rars

	    // Calculated data
	    int m_iPrvSeg;                     // This id allows to build the track easily. It is used mostly by the sections.
	    int m_iNxtSeg;                     // This id allows to build the track easily. It is used mostly by the sections.
	    int m_iOtherYSeg;                  // For a y segment, this is the id of the other associated segment.
	    ProfileHeight m_pProfileHeight; 

	    public Segment3D(){
	    	m_fLength = 0; 
	    	  m_fRadius = 0; 
	    	  m_fArc = 0; 
	    	  m_fEndZ = 0; 
	    	  m_fEndBanking = 0; 
	    	  m_fHillHeight = 0;
	    	  m_iPrvSeg = -1; 
	    	  m_iOtherYSeg = -1; 

	    	  m_pProfileHeight = g_aProfileHeight[0]; // "flat"
	    	  m_sName = "";
	    	  m_sProfileHeight = "flat";
	    	  m_sType = "default";
	    	  m_sSection = "";
	    	  m_sModel = "";
	    }
	    
	    /**
	     * Calculate the data (m_pProfileHeight, ... )
	     */
	    void Rebuild(){
	    	int i = 0;
	    	  while( !g_aProfileHeight[i].sName.equals(m_sProfileHeight) )
	    	  {
	    	    i++;
	    	    if( g_aProfileHeight[i].sName==null )
	    	    {
	    	      //warning("Unknown profile height '%s'", m_sProfileHeight);
	    	      i = 0;
	    	      break;
	    	    }
	    	  }
	    	  m_pProfileHeight = g_aProfileHeight[i];
	    }
	    SegmentSide3D side(boolean bLft)
	    {
	      if( bLft ) return lft;
	             else return rgt;
	    }
	};
	
	/**
	 * Data of one side (right, left) for 3D 
	 */
	public class SegmentSide3D
	{
	    // Data read from file
	  public  boolean   m_bShowTree;
	  public  boolean   m_bShowBorderTree;
	  public  double m_fBorderSize;
	  public  double m_fBarrierHeight;
	  public  double m_fBorderTreeSize;
	  public  double m_fBorderTreeHeight;
	  public  String   m_sTextureBorder;
	  public  String   m_sTextureBarrier;
	  public  String   m_sTextureBorderTree;
	  public  String   m_sTextureTree;

	   public SegmentSide3D()
	    {
	      // default values
	      m_bShowTree = true;
	      m_bShowBorderTree = true;
	      m_fBorderSize = 90;
	      m_fBarrierHeight = 4.2;
	      m_fBorderTreeSize = 90;
	      m_fBorderTreeHeight = 75;
	      m_sTextureBorder = "grass_in";
	      m_sTextureBarrier =  "barrier";
	      m_sTextureBorderTree = "grass_out";
	      m_sTextureTree = "tree";
	    }

	    public int equals( SegmentSide3D side)
	    {
	      if( m_bShowTree==side.m_bShowTree
	       && m_bShowBorderTree==side.m_bShowBorderTree
	       && m_fBorderSize==side.m_fBorderSize 
	       && m_fBarrierHeight==side.m_fBarrierHeight 
	       && m_fBorderTreeSize==side.m_fBorderTreeSize 
	       && m_fBorderTreeHeight==side.m_fBorderTreeHeight
	       && m_sTextureBorder.equals(side.m_sTextureBorder) 
	       && m_sTextureBarrier.equals(side.m_sTextureBarrier)
	       && m_sTextureBorderTree.equals(side.m_sTextureBorderTree) 
	       && m_sTextureTree.equals(side.m_sTextureTree) )
	      {
	        return 1;
	      }
	      else
	      {
	        return 0;
	      }
	    }
	};
	
	/**
	 * Section: a section is a group of segment that are not in the main track
	 * like a pitstop, and so on
	 *
	 * The segments of the section are stored in rgtwall and seg3D (like normal segments)
	 * but are define after NSEG (so the drivers do not see them)
	 */
	public class Section
	{
	    public String m_sName;               // Name of the section (ex: "pitstop")
	    public int    m_iFirstSeg;               // First segment
	    public int    m_iLastSeg;                // Last segment

	    public Section()
	    {
	      // default values
	      m_sName = "";
	      m_iFirstSeg = -1;
	      m_iLastSeg = -1;
	    }
	};
	
	/**
	 * Object3D (3D object on the track)
	 */
	public class Object3D
	{
	    public String m_sModel;                 // Name of the model (file.3ds)
	    public double x, y, z;                    // Position of the object
	    public double rot_x, rot_y, rot_z;        // Rotation of the object

	    public Object3D()
	    {
	      m_sModel = "";
	      x = y = z = 0;
	      rot_x = rot_y = rot_z = 0;
	    }
	};
	
	public class Fastest_lap 
	{
	  public String track;
	  public double speed;
	  public String rob_name;
	};
	
	public class Track_desc 
	{
		public String sName;     // name of the track
		public int NSEG;           // number of track segments (see drawpath() in DRAW.CPP)
		public  double width;       // width of track, feet   
		public double length;      // length of track, feet (5280 ft/mile, 3281 ft/km)   

		public Segment[] rgtwall;   // replaces trackout   
		public Segment[] lftwall;   // replaces trackin   
		public Segment[] trackout;  // old track defining arrays   
		public Segment[] trackin;   

		public double[] seg_dist;   // e2 from SF lane to end of each segment
		public int pit_side;       // car needs to know pit location
		public double pit_entry;   // to avoid entering and exiting cars   
		public double pit_exit;    // and to calculate estimated pit times   
		public double pit_speed;   //    
		public Fastest_lap record; // fastest ever lap on this race
	};
	
	/**
	 * Profile of the height 
	 */
	public static class ProfileHeight
	{ 
	    public String sName;
	    public double[] aHeight = new double[20];

	    public ProfileHeight(String sNameIn, double[] aHeightIn)
	    {
	    	sName = sNameIn;
	    	aHeight = aHeightIn;
	    }
	    int GetNbSubSeg(){
	    	int i=1;
	    	  while( aHeight[i]!=0 && aHeight[i]!=1 )
	    	  {
	    	    i++;
	    	  }
	    	  return i;
	    }
	};

}
