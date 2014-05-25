package mil.af.rl.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class InstanceUtils {

	public static Instance instanceFromList(double weight, List<Double> values){
		Double[] converted = new Double[1];
		converted = values.toArray(converted);
		double[] prim = new double[converted.length];
		for(int i = 0; i < converted.length; ++i)
			prim[i] = converted[i];
		
		return new DenseInstance(weight, prim);
	}
	
	
	/**
	 * This method reads in a .arff file.  The data is read into the weka Instances object.
	 * 
	 * @param fileName the .arff file to read
	 * @return the Instances of data that have been read in.
	 */
	public static Instances readArff(String fileName){
		Instances instances = null;
		try {
			//Read the arff file and set the class e1
			instances = new Instances(new FileReader(fileName));
			instances.setClassIndex(instances.numAttributes()-1);			
		} catch (FileNotFoundException e) {
			//Exit the program if there are any errors
			System.err.println("Cannot open arff file.");
			System.err.println(e.getMessage());
			System.err.println("Exiting...");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Cannot read arff file.");
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("Exiting...");
			System.exit(1);
		}
		return instances;
	}

	/**
	 * This method write out the given instances in the Plaid format with .row, .col and .dat parts for the
	 * files.  Missing values will be reported as 0 in the data file as the Plaid algorithm does not deal
	 * with missing values.  The class attribute(s) will not be written to the file.
	 * <br />
	 * It is further expected that the genes are encoded as the attributes of the instances, and samples are
	 * instances.  This will be transposed in the plaid files as it expects that the genes are row and
	 * samples are columns.
	 * 
	 * @param fileName  The base file name to use (no extension)
	 * @param instances The data to create the plaid data sets from
	 */
	public static void writeInPlaidFormat(String fileName, Instances instances){
		try {
			BufferedWriter dat = new BufferedWriter(new FileWriter(new File(fileName+".dat")));
			BufferedWriter row = new BufferedWriter(new FileWriter(new File(fileName+".row")));
			BufferedWriter col = new BufferedWriter(new FileWriter(new File(fileName+".col")));
			//Write the cols file
			for(int r = 0; r < instances.numInstances(); ++r)
				col.write("s"+r+"\n");
			col.close();
			//Write the rows file
			for(int c = 0; c < instances.numAttributes(); ++c)
				if(instances.classIndex() != c)
					row.write(instances.attribute(c).name()+"\n");
			row.close();
			//Write the data file
			for(int attr = 0; attr < instances.numAttributes(); ++attr){
				if(instances.classIndex() != attr){
					for(int inst = 0; inst < instances.numInstances(); ++inst)
						dat.write(instances.instance(inst).value(attr)+"\t");
					dat.write("\n");
				}
			}
			dat.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write out the instances in the input format needed for the xMotif algorithm.  This format is
	 * fully described here: <a href="https://bioinformatics.cs.vt.edu/~murali/xmotif/">
	 * https://bioinformatics.cs.vt.edu/~murali/xmotif/</a>.  The file with the gene expression data will
	 * be printed to <code>fileName.exp</code> and the file with the class information (if present) will
	 * be written to <code>fileName.cls</code>.
	 * 
	 * @param fileName the name of the file to be written to (avoid using an extension here)
	 * @param instances the source data that will be written out in the xMotif format.
	 */
	public static void writeInXMotifFormat(String fileName, Instances instances){
		try {
			BufferedWriter dat = new BufferedWriter(new FileWriter(new File(fileName+".exp")));
			BufferedWriter cls = new BufferedWriter(new FileWriter(new File(fileName+".cls")));
			//Write the expression file, one gene per row, the first row has label information:
			dat.write("Name\tID\t");
			for(int s = 0; s < instances.numInstances(); ++s)
				dat.write("s"+s+"\t");
			dat.write("\n");
			//Write out the genes
			for(int g = 0; g < instances.numAttributes(); ++g){
				if(instances.classIndex() != g){
					dat.write(instances.attribute(g).name()+"\t"+g+"\t");
					for(int s = 0; s < instances.numInstances(); ++s)
						dat.write(instances.instance(s).value(g)+"\t");
					dat.write("\n");
				}
			}
			//Write out the class label info:
			if(instances.classIndex() >= 0){
				for(int s = 0; s < instances.numInstances(); ++s){
					cls.write("s"+s+"\t"+instances.instance(s).classValue()+"\n");
				}
			}
			dat.close();
			cls.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print out the instances in .arff format to the file with the name
	 * fileName.  If the file cannot be written to, the method will print
	 * an error message and continue execution.
	 * @param fileName the file to write to
	 * @param instances the Instances to save in arff format
	 */
	public static void writeArff(String fileName, Instances instances){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(instances.toString());
			bw.close();
		} catch (IOException e) {
			System.err.println("Could not write file.  Is it still open?");
			e.printStackTrace();
		}
	}

	/**
	 * This method takes a data set and returns a truncated version of the data set where
	 * only the features indicated in the selectedFeatures array are present (as well as
	 * the class label). The number of features taken is limited by numFeatures.
	 * @param data the data set to truncate
	 * @param selectedFeatures the array of features to preserve
	 * @param numFeatures up to this many features will be taken from selectedFeatures
	 * @param keepOldIds true if the original attribute names should be carried over to the new set.
	 * @return the truncated data set
	 */
	public static Instances truncateInstances(Instances data, int[] selectedFeatures,
			int numFeatures, boolean keepOldIds) {
		FastVector attrInfo = new FastVector();
		//Make the names of the attributes
		if(!keepOldIds){
			for(int i = 0; i < selectedFeatures.length && i < numFeatures; ++i)
				attrInfo.addElement(
						//new Attribute(data.attribute(selectedFeatures[i]).name()));
						new Attribute(String.valueOf(i)));
		}
		else
			for(int i = 0; i < selectedFeatures.length && i < numFeatures; ++i)
				attrInfo.addElement(new Attribute(data.attribute(selectedFeatures[i]).name()));
		//Take care of the class label
		attrInfo.addElement(data.classAttribute());
		//Create the instances
		Instances trunc = new Instances("truncated"+data.relationName(), attrInfo, 0);
		trunc.setClassIndex(trunc.numAttributes()-1);
		for(int i = 0; i < data.numInstances(); ++i){
			//+1 is for the class label
			double[] vals = new double[numFeatures+1];
			//Get the instance's values for the selected features
			for(int feature = 0; feature < selectedFeatures.length &&
			feature < numFeatures; ++feature)
				vals[feature] = data.instance(i).value(selectedFeatures[feature]);
			//Add on the class label
			vals[vals.length-1] = data.instance(i).classValue();
			Instance inst = new DenseInstance(1.0, vals);
			inst.setDataset(trunc);
			trunc.add(inst);
		}

		return trunc;
	}

	/**
	 * This method transposes a weka Instances object.
	 * @param data the data to tranpose
	 * @param classLabel true if include the class label in the new data set
	 * @return the transposed Instances with or without the class attribute
	 */
	public static Instances transposeInstances(Instances data, boolean classLabel) {
		Instances transposed;
		//Create the attribute vector
		FastVector attrInfo = new FastVector();
		//Make the names of the attributes
		for(int i = 0; i < data.numInstances(); ++i)
			attrInfo.addElement(
					new Attribute("f"+i));
		//If we don't want the class label and there is one present in the data
		if(classLabel && data.classIndex() > -1)
			attrInfo.addElement(new Attribute("Class Label"));
		//Figure out the stopping bound
		int bound = data.numAttributes();
		if(!classLabel && data.classIndex() > -1)
			bound--;//Don't include the class label
		transposed = new Instances(data.relationName()+"transposed", attrInfo, 0);
		for(int attr = 0; attr < bound; ++attr){
			//skip past the class attribute if necessary
			if(!classLabel && data.classIndex() == attr)
				continue;				
			double[] vals = new double[data.numInstances()];
			//Get the instance's values for the selected features
			for(int inst = 0; inst < vals.length; ++inst)
				vals[inst] = data.instance(inst).value(attr);
			Instance inst = new DenseInstance(1.0, vals);
			inst.setDataset(transposed);
			transposed.add(inst);
		}
		return transposed;
	}

	/**
	 * This method breaks apart given Instances data into separate
	 * Instances objects that are homogenous with respect to the class
	 * label of data.
	 * The resulting features are also stable, meaning the order in which
	 * the original instances appeared in the original data will be the
	 * order in which they appear in their respective striation.
	 * @param data the Instances to separate according to class label
	 * @return an array of Instances objects, each homogenous with respect
	 * to a given class label
	 */
	public static Instances[] separate(Instances data){
		Instances[] pieces = new Instances[data.numClasses()];

		//Easiest way to do it is with repeated calls to truncate
		int[][] selectedFeatures = new int[pieces.length][];
		Map<Double, Integer> classCount =
			new HashMap<Double, Integer>(data.numClasses());
		Map<Double, Integer> classMap = 
			new HashMap<Double, Integer>(data.numClasses());
		//Get the counts for each class
		int spot = 0;
		for(int i = 0; i < data.numInstances(); ++i){
			double key = data.instance(i).classValue();
			if(classCount.containsKey(key))
				classCount.put(key, classCount.get(key)+1);
			else{
				classCount.put(key, 1);
				classMap.put(key, spot);
				spot++;
			}
		}
		//Make the memory for each of the features of the classes
		for(Double key : classCount.keySet())
			selectedFeatures[classMap.get(key)] = new int[classCount.get(key)];

		//Fill up the selected features for each class
		int[] lastFilled = new int[data.numClasses()];
		for(int i = 0; i < data.numInstances(); ++i){
			int index = classMap.get(data.instance(i).classValue());
			selectedFeatures[index][lastFilled[index]] = i;
			lastFilled[index]++;
		}
		//Finally, make the new Instances objects
		for(int i = 0; i < pieces.length; ++i){
			pieces[i] = truncateInstances(data, selectedFeatures[i], 
					selectedFeatures[i].length, true);
		}
		return pieces;
	}

	/**
	 * This method parses out the true features that are written in the
	 * synthetic test sets that I created.  Must have the explicit format:
	 * %True class function = 7.043350480427968*x0 + ...
	 * 
	 * @param fileName the synthetic data set to extract info from
	 * @return the list feature indices (0 based)
	 */
	public static int[] getTrueFeatures(String fileName) {
		List<Integer> feats = new ArrayList<Integer>();
		Scanner scan;
		try {
			scan = new Scanner(new FileReader(fileName));
			String line = scan.nextLine();
			while(!line.startsWith("%True class function = "))
				line = scan.nextLine();
			//Now we have the class label line, use a Regex to pick out the right parts
			Pattern p = Pattern.compile("(\\d+\\.\\d+)(\\*x)(\\d+)");
			//Skip over the preamble stuff
			Matcher m = p.matcher(line.substring(23));
			while(m.find()){
				feats.add(Integer.parseInt(m.group(3)));
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int[] fs = new int[feats.size()];
		for(int i = 0; i < fs.length; ++i)
			fs[i] = feats.get(i);
		return fs;
	}

	/**
	 * This method merges together two data sets and returns one single
	 * dataset.
	 * 
	 * Assumes that both data sets have the same size and order of features.
	 * @param set1
	 * @param set2
	 * @return Instances object which has numInstances == set1.numInstances() +
	 * set2.numInstances() and numAttributes() == set1.numAttributes()
	 */
	public static Instances merge(Instances set1, Instances set2) {

		if(set1 == null || set2 == null || set1.numAttributes() != set2.numAttributes())
			throw new IllegalArgumentException();
		//Copy over all instances from set1
		Instances res = new Instances(set1);
		//Add in the instances from set2
		for(int i = 0; i < set2.numInstances(); ++i)
			res.add(set2.instance(i));
		return res;
	}

	/**
	 * Create Instances with the given values in the data section, transposed if the transpose field is set to true.
	 * If hasClass is true another feature is added to the data.  WARNING: setting hasClass and transpose to true will put the
	 * class labels as a row, which is probably not desirable.
	 * The feature labels must be given for all features, excluding the class label if present.
	 * The title ends up in the relation section of the arff file.
	 * @param values the data values
	 * @param hasClass true if the data includes a class label column
	 * @param transpose true if the data is to be transposed prior to creation of the Instances
	 * @param title the relation name
	 * @param featureNames the names of all features in the Instances object
	 * @return Instances object made according to the given values.
	 */
	public static Instances createInstances(double[][] values, boolean hasClass, boolean transpose, String title, String[] featureNames, String[] categories){
		if(transpose){
			double[][] tValues = new double[values[0].length][values.length];
			for(int i = 0; i < values.length; ++i)
				for(int j = 0; j < values[i].length; ++j)
					tValues[j][i] = values[i][j];
			values = tValues; //Thanks to pass-by-value this won't hurt anything outside the method
		}
		FastVector attrInfo = new FastVector();
		//Make the names of the attributes
		for(int i = 0; i < values[0].length-1; ++i)
			attrInfo.addElement(
					new Attribute((featureNames == null || featureNames[i] == null)?"feature_"+i:featureNames[i]));
		//Add the class attribute
		if(hasClass)
			attrInfo.addElement(createClassAttribute(categories));
		else
			attrInfo.addElement(new Attribute((featureNames == null || 
					featureNames[featureNames.length-1] == null)?
							"feature_"+(featureNames.length-1):
								featureNames[featureNames.length-1]));
		Instances ret = new Instances(title, attrInfo, 0);
		if(hasClass)
			ret.setClassIndex(ret.numAttributes()-1);
		//Copy in all of the values.
		for(int i = 0; i < values.length; ++i){
			Instance inst = new DenseInstance(1.0, values[i]);
			inst.setDataset(ret);
			ret.add(inst);
		}
		return ret;
	}

	
	public static Instances createRegressionInstances(double[][] values, boolean transpose, String title){
		if(transpose){
			double[][] tValues = new double[values[0].length][values.length];
			for(int i = 0; i < values.length; ++i)
				for(int j = 0; j < values[i].length; ++j)
					tValues[j][i] = values[i][j];
			values = tValues; //Thanks to pass-by-value this won't hurt anything outside the method
		}
		FastVector attrInfo = new FastVector();
		//Make the names of the attributes
		for(int i = 0; i < values[0].length-1; ++i)
			attrInfo.addElement(
					new Attribute("feature_"+i));
		//Add the class attribute
		attrInfo.addElement(createClassAttribute(null));
		
		Instances ret = new Instances(title, attrInfo, 0);
		ret.setClassIndex(ret.numAttributes()-1);
		//Copy in all of the values.
		for(int i = 0; i < values.length; ++i){
			Instance inst = new DenseInstance(1.0, values[i]);
			inst.setDataset(ret);
			ret.add(inst);
		}
		return ret;
	}
	
	/**
	 * Creates an instances object from the given values matrix.  If hasClass is set to true,
	 * then the last column of the values matrix will be treated as the class label, otherwise it
	 * will be treated as a regular attribute.
	 * 
	 * @param values
	 * @param hasClass true if the last column of values are the class labels
	 * @return
	 */
	public static Instances createInstances(double[][] values, boolean hasClass, String[] categories) {
		String[] featureNames = new String[values[0].length];
		for(int i = 0; i < featureNames.length; ++i)
			featureNames[i] = "Attribute"+i;
		return createInstances(values, hasClass, false, "NewInstances"+values.toString(), featureNames, categories);
	}

	public static Instances createInstances(double[][] values, List<Integer> attrInds, int classInd, String title){
		FastVector attrInfo = new FastVector();
		//Make the names of the attributes
		for(int i:attrInds) {
			attrInfo.addElement(new Attribute("feature_"+i));
		}
		attrInfo.addElement(new Attribute("class_value"));
		Instances ret = new Instances(title, attrInfo, 0);
		ret.setClassIndex(attrInds.size());
		//Copy in all of the values.
		for(int i = 0; i < values.length; ++i){
			double[] sample = new double[attrInds.size()+1];
			for(int j=0; j<attrInds.size(); j++) {
				sample[j] = values[i][attrInds.get(j)];
			}
			sample[attrInds.size()] = values[i][classInd];
			Instance inst = new DenseInstance(1.0, sample);
			inst.setDataset(ret);
			ret.add(inst);
		}
		return ret;
	}
	/**
	 * Creates a new Instance which only contains the subset of attributes listed
	 * in attrInds.  These must be valid indices from the Instances object orig.
	 * 
	 * @param orig
	 * @param attrInds
	 * @return
	 */
	public static Instances createInstances(Instances orig, List<Integer> attrInds){
		FastVector attrInfo = new FastVector();
		for(int i : attrInds)
			attrInfo.addElement(orig.attribute(i).copy());
		attrInfo.addElement(orig.classAttribute().copy());
		Instances ret = new Instances(orig.relationName()+"_"+attrInds, attrInfo, 0);
		ret.setClassIndex(attrInds.size());
		for(int i = 0; i < orig.numInstances(); ++i){
			double[] inst = new double[attrInds.size()+1];
			for(int j = 0; j < attrInds.size(); ++j)
				inst[j] = orig.instance(i).value(attrInds.get(j));
			inst[inst.length-1] = orig.instance(i).classValue();
			Instance newInst = new DenseInstance(1.0, inst);
			newInst.setDataset(ret);
			ret.add(newInst);
		}
		return ret;
	}
	
//	public static Instances createInstances(double[][] values, List<Integer> attrInds, int classInd, String title){
//		FastVector attrInfo = new FastVector();
//		//Make the names of the attributes
//		for(int i:attrInds) {
//			attrInfo.addElement(new Attribute("feature_"+i));
//		}
//		attrInfo.addElement(new Attribute("class_value"));
//		Instances ret = new Instances(title, attrInfo, 0);
//		ret.setClassIndex(attrInds.size());
//		//Copy in all of the values.
//		for(int i = 0; i < values.length; ++i){
//			double[] sample = new double[attrInds.size()+1];
//			for(int j=0; j<attrInds.size(); j++) {
//				sample[j] = values[i][attrInds.get(j)];
//			}
//			sample[attrInds.size()] = values[i][classInd];
//			Instance inst = new Instance(1.0, sample);
//			inst.setDataset(ret);
//			ret.add(inst);
//		}
//		return ret;
//	}
	/**
	 * Creates an instances object from the given list of instances.  If hasClass is set to true,
	 * then the last entry of each instance will be treated as the class label, otherwise it
	 * will be treated as a regular attribute.
	 * 
	 * @param values
	 * @param hasClass true if the last entry of each instance is its class label
	 * @return
	 */
	public static Instances createInstances(List<double[]> values, boolean hasClass, String[] nominalValues){
		FastVector attrInfo = new FastVector();
		//Make the names of the attributes
		for(int i = 0; i < values.get(0).length-1; ++i)
			attrInfo.addElement(
					new Attribute("Attribute"+i));
		//Add the class attribute
		if(hasClass)
			attrInfo.addElement(createClassAttribute(nominalValues));
		else
			attrInfo.addElement(new Attribute("Attribute"+(values.get(0).length-1)));
		
		Instances ret = new Instances("NewInstances"+values.toString(), attrInfo, 0);
		if(hasClass)
			ret.setClassIndex(ret.numAttributes()-1);
		//Copy in all of the values.
		for(int i = 0; i < values.size(); ++i){
			Instance inst = new DenseInstance(1.0, values.get(i));
			inst.setDataset(ret);
			ret.add(inst);
		}
		return ret;
	}
	
	private static Attribute createClassAttribute(String[] nominalValues){
		Attribute retVal = null;
		if(nominalValues != null){
			FastVector classValues = new FastVector(nominalValues.length);
			for(String val : nominalValues)
				classValues.addElement(val);
			retVal = new Attribute("Class", classValues);
		}
		else
			retVal = new Attribute("Class");
		return retVal;
	}
	
	/**
	 * This code prints out the Instances data in the BicAT data format.
	 * 
	 * @param data the Instances that are to be converted
	 * @param fileName the file to save the converted data (should be a .txt file).
	 */
	public static void printInBicATform(Instances data, String fileName){
		//BicAT files are tab separated with the name of the conditions across the first row and the
		//name of each gene in the first column.  Genes are in the rows, conditions are across the columns.
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			//Print the header info
			pw.print("Genes\\Samples\t");
			for(int i = 0; i < data.numInstances()-1; ++i)
				pw.print(i+"\t");
			pw.println(data.numInstances()-1);
			//Print out the data
			for(int gene = 0; gene < data.numAttributes(); ++gene){
				pw.print(data.attribute(gene).name()+"\t");
				for(int samp = 0; samp < data.numInstances()-1; ++samp)
					pw.print(data.instance(samp).value(gene)+"\t");
				pw.println(data.instance(data.numInstances()-1).value(gene));
			}
			pw.close();
		} catch (IOException e) {
			System.err.println("Could not open the file for writing: "+fileName);
		}
	}

	/**
	 * This method converts all of the contents of the data object into a two dimensional array.
	 * @param data
	 * @param removeClassVariable true if the class variable should be removed, false otherwise
	 * @return
	 */
	public static double[][] convertToMatrix(Instances data, boolean removeClassVariable){
		double[][] retVal = new double[data.numInstances()]
		                               [(removeClassVariable && data.classIndex() >= 0) ? data.numAttributes()-1 : data.numAttributes()];
		boolean effect = retVal[0].length != data.numAttributes();
		int clsIndex = data.classIndex();
		for(int i = 0; i < retVal.length; ++i){
			int offset = 0;
			for(int j = 0; j < retVal[i].length; ++j)
				if(j != clsIndex)
					retVal[i][j-offset] = data.instance(i).value(j);
				else if(effect){
					offset = 1;
				}
				else //no effect, we still want the class label
					retVal[i][j] = data.instance(i).value(j);
		}
		return retVal;
	}

	/**
	 * This method prints out the sample mapping file corresponding to how the instance are currently arrange in data and where those
	 * indices came from in the original data (possibly the same data set).
	 * The output file format will be:
	 * 0	1244
	 * 1	1903
	 * ...
	 * Where the first number on a line is the e1 of the sample in this data set and the second number is the e1 in the orignal data set.
	 *
	 * ONLY WORKS FOR WEKA_SL2.jar
	 * @param data
	 * @param fileName
	 */
//	public static void printMapping(Instances data, String fileName) {
//		int[] map = data.getMapping();
//		PrintWriter out = null;
//		try {
//			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
//			for(int i = 0; i < map.length; ++i)
//				out.println(i+"\t"+map[i]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally{
//			if(out != null)
//				out.close();
//		}
//	}

	/**
	 * Determines the mapping from the output sample indices to the full data sample indices.  This
	 * file was created when the data was partitioned during the experimentation.
	 * <br />
	 * It has the format: <br />
	 * 0 123
	 * 1 17
	 * 2 43
	 * ...
	 * Where the first number is the value found in this set of results and the second value is the e1 found in the
	 * full data set.  There is an entry for every possible e1 in the subset.
	 * 
	 * @param fileName
	 * @return
	 */
	public static Map<Integer, Integer> readMapping(String fileName) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		try {
			Scanner s = new Scanner(new FileReader(fileName));
			while(s.hasNextLine()){
				map.put(s.nextInt(), s.nextInt());
				s.nextLine(); //clear out the newline character(s)
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * This method scales down a full data set according to the mapping that is provided.  Such a mapping can be
	 * created by reading it in from a file like those created by printMapping.  It should be a deep copy of the 
	 * instances if Weka is pulling its weight.
	 * 
	 * @see #printMapping(Instances, String)
	 * @param full
	 * @param map
	 * @return
	 */
	public static Instances scaleDown(Instances full,
			Map<Integer, Integer> map) {
		Instances small = new Instances(full, map.size());
		for(Integer key : map.keySet()){
			small.add(new DenseInstance(full.instance(map.get(key))));
		}
		return small;
	}

	/**
	 * Standardizes the instances to have zero mean and unit standard deviation.  If provided, the class
	 * label is not altered.  Missing values will be treated as zeros.
	 * 
	 * @param raw the raw instance data.
	 * @return new Instances object with standardized values from the raw input data
	 */
	public static Instances standarize(Instances raw){
		Instances stand = new Instances(raw);
		double[] helper = new double[(raw.classIndex() >= 0)? raw.numAttributes()-1 : raw.numAttributes()];
		for(int i = 0; i < stand.numInstances(); ++i){
			//Need to get the mean and std of each row
			int toFill = 0;
			for(int a = 0; a < stand.numAttributes(); ++a){
				if(a != raw.classIndex()){
					helper[toFill] = raw.instance(i).value(a);
					++toFill;
				}
			}
			double mean = ListUtils.computeMean(helper);
			double stdev = ListUtils.computeStdev(mean, helper);
			//Apply the transformation
			for(int a = 0; a < stand.numAttributes(); ++a)
				if(a != raw.classIndex())
					stand.instance(i).setValue(a, (stand.instance(i).value(a)-mean)/stdev);
		}
		return stand;
	}

	/**
	 * Returns an array containing the double values for the specified e1 in data.  If the e1 is out of legal
	 * bounds for the data null will be returned.
	 * @param e1 the instance e1 to retrieve from the data
	 * @param data the source of the data values
	 * @param withClass flag indicated whether or not to return the value of the class attribute.  If false the
	 * return value will have length data.numAttributes()-1 
	 * @return array containing the values of the instance in data, with or without the class variable.
	 */
	public static double[] getInstanceValues(int index, Instances data, boolean withClass){
		double[] retVal = null;
		if(index >= 0 && index < data.numInstances()){
			retVal = data.instance(index).toDoubleArray();
			//Need to cut out the class e1 if withClass is false
			if(data.classIndex() >= 0 && !withClass){
				double[] smaller = new double[retVal.length-1];
				System.arraycopy(retVal, 0, smaller, 0, data.classIndex());
				System.arraycopy(retVal, data.classIndex()+1, smaller, data.classIndex(), smaller.length-data.classIndex());
				retVal = smaller;
			}
		}
		return retVal;
	}
	
	/**
	 * Gets the values for attribute e1 in data. If the e1 is out of range this method will
	 * return null.
	 * This is here mostly to be symmetrical with getInstanceValues and only wraps a call to <code>data.attributeToDoubleArray(e1)</code> 
	 * 
	 * @param e1
	 * @param data
	 * @return the values of attribute e1 in data
	 */
	public static double[] getFeatureValues(int index, Instances data){
		if(index < 0 || index > data.numAttributes())
			return null;
		return data.attributeToDoubleArray(index);
	}

	/**
	 * Creates a new data set that is identical to src except that the class
	 * attribute is changed to classAttr.  The values for the class attribute are
	 * all set to zero.
	 * @param src
	 * @param classAttr
	 * @return
	 */
	public static Instances changeClassLabelType(Instances src,
			Attribute classAttr) {
		FastVector attrs = new FastVector();
		for(int i = 0; i < src.numAttributes(); ++i){
			if(src.classIndex() != i)
				attrs.addElement(src.attribute(i));
			else
				attrs.addElement(classAttr);
		}
		Instances alt = new Instances(src.relationName(), attrs, src.numInstances());
		alt.setClassIndex(src.classIndex());
		for(int i = 0 ; i < src.numInstances(); ++i){
			Instance next = new DenseInstance(src.instance(i));
			next.setValue(src.classIndex(), 0.0); //This will have to be set somewhere else.  TODO probably could be a parameter
			next.setDataset(alt);
			alt.add(next);
		}
		return alt;
	}
}