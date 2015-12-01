package mil.af.rl.novelty;

import java.util.Random;

import flann.FLANNParameters;
import flann.SWIGTYPE_p_double;
import flann.SWIGTYPE_p_float;
import flann.SWIGTYPE_p_void;
import flann.flann_algorithm_t;
import flann.flann_log_level_t;
import flann.java_flann;

public class Test {

	static{
		System.loadLibrary("flann");
		System.loadLibrary("flann_java");
	}

	public static void main(String[] args){
		Random rand = new Random(1351415411L);
		double[][] something = new double[100][10];
		for(int i = 0; i < something.length; ++i)
			for(int j = 0; j < something[i].length; ++j)
				something[i][j] = rand.nextDouble() * 100;
		
		FLANNParameters params = new FLANNParameters();
		params.setAlgorithm(flann_algorithm_t.FLANN_INDEX_AUTOTUNED);
		params.setTarget_precision(0.9F);
		params.setLog_level(flann_log_level_t.FLANN_LOG_INFO);

		SWIGTYPE_p_double data = java_flann.new_doublep();
		java_flann.doublep_assign(data, something[0][0]);
		SWIGTYPE_p_float speedup = java_flann.new_floatp();
		java_flann.floatp_assign(speedup, 0.0F);
		System.out.println("About to hit some index: "+data.toString());
		SWIGTYPE_p_void index = java_flann.flann_build_index_double(data, something.length, something[0].length, speedup, params);
		
	}

}
