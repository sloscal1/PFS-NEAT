/*
 * Copyright (C) 2004 Derek James and Philip Tucker
 * 
 * This file is part of ANJI (Another NEAT Java Implementation).
 * 
 * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * created by Philip Tucker on Feb 16, 2003
 */
package mil.af.rl.novelty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import core.io.Arg;
import core.io.ProgramParameter;

/**
 * Adapter class to allow programs that expect properties files as input to be run with SearchParty.
 * 
 * @author Steven Loscalzo
 */
public class NoveltyEvolverSP {

	public static List<ProgramParameter> allParams = new ArrayList<>();
	public static ProgramParameter PROPERTY_TEMPLATE = new ProgramParameter("prop-template", Arg.REQUIRED, 't'){};
	public static ProgramParameter PROPERTY_RESOURCE_DIR = new ProgramParameter("prop-dir", Arg.REQUIRED, 'd'){};
	public static ProgramParameter MAIN_CLASS = new ProgramParameter("main-class", Arg.REQUIRED, 'm'){
		public boolean process(String arg0, java.util.Map<ProgramParameter,Object> arg1) {
			try {
				arg1.put(this, Class.forName(arg0));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return false;
		};
	};
	//TODO This one is program specific... need to factor this out somehow 
	public static ProgramParameter BVG_NUM_SAMPLES = new ProgramParameter("behavior_vector_generator.num_samples", Arg.REQUIRED, 'b'){
		public boolean process(String arg0, java.util.Map<ProgramParameter,Object> arg1) {
			arg1.put(this, Integer.parseInt(arg0));
			return false;
		};
	};
	public static ProgramParameter RANDSEED = new ProgramParameter("randseed", Arg.REQUIRED, 'r'){
			public boolean process(String arg0, java.util.Map<ProgramParameter,Object> arg1) {
				arg1.put(this, (int)Long.parseLong(arg0));
				return false;
			};
	};
	public static ProgramParameter TABLE_PREFIX = new ProgramParameter("result-table-prefix", Arg.REQUIRED, 'p'){};
	public static ProgramParameter SEARCHER_PORT = new ProgramParameter("searcher-port", Arg.REQUIRED, 's'){
		public boolean process(String arg0, java.util.Map<ProgramParameter,Object> arg1) {
			arg1.put(this, Integer.parseInt(arg0));
			return false;
		};
	};
	
	static{
		allParams.add(PROPERTY_TEMPLATE);
		allParams.add(BVG_NUM_SAMPLES);
		allParams.add(PROPERTY_RESOURCE_DIR);
		allParams.add(MAIN_CLASS);
		allParams.add(RANDSEED);
		allParams.add(TABLE_PREFIX);
		allParams.add(SEARCHER_PORT);
	}

	/**
	 * Main program used to perform an evolutionary run.
	 * 
	 * @param args command line arguments; args[0] used as properties file
	 * @throws Throwable
	 */
	public static void main( String[] args ) throws Throwable {
		//Get the arguments from the command line...
		List<ProgramParameter> requiredParams = new ArrayList<>(allParams);
		requiredParams.remove(BVG_NUM_SAMPLES);
		
		Map<ProgramParameter, Object> inputs = ProgramParameter.getValues(args, allParams, requiredParams);

		if(inputs != null){
			//Open the template properties file
			File input = new File((String)inputs.get(PROPERTY_RESOURCE_DIR)+File.separator+(String)inputs.get(PROPERTY_TEMPLATE));

			//Open the output file
			File outFile = new File(new File((String)inputs.get(PROPERTY_RESOURCE_DIR)).getAbsolutePath()+File.separator+input.getName()+inputs.get(RANDSEED));
			PrintWriter out = new PrintWriter(new BufferedWriter (new FileWriter(outFile)));

			//Modify the properties file
			try(Scanner read = new Scanner(input)){
				Set<ProgramParameter> used = new HashSet<>();
				while(read.hasNextLine()){
					String line = read.nextLine();
					boolean printed = false;
					for(ProgramParameter pp : inputs.keySet())
						if(line.startsWith(pp.getLongName())){
							out.println(pp.getLongName()+"="+inputs.get(pp).toString());
							used.add(pp);
							printed = true;
						} else if(pp == RANDSEED && line.startsWith("random.seed")){
							out.println("random.seed="+inputs.get(pp).toString());
							used.add(pp);
							printed = true;
						}
					if(!printed)
						out.println(line);
				}
				//Add in the other information that was passed and not represented in the template:
				System.out.println(used);
				for(ProgramParameter pp : inputs.keySet()){
					if(!used.contains(pp) && pp.getRequiredVal() == Arg.REQUIRED)
						out.println(pp.getLongName()+"="+inputs.get(pp).toString());
				}
			}
			out.close();

			String[] procArgs = {outFile.getName()};
			System.out.println("CLASSPATH="+System.getenv("CLASSPATH"));
			//Run the method with the modified properties file
			Method main = ((Class<?>)inputs.get(MAIN_CLASS)).getMethod("main", String[].class);
			main.invoke(null, new Object[]{procArgs});
		}
	}
}
