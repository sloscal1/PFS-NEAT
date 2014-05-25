package mil.af.rl.problem.rars;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class reads a particular persistence xml file and prints out a report of the
 * features that are connected in this particular neural network.
 * <br />
 * The file name is given as an input to the program, and a chromosome Id is also
 * given.  If that id does not exist in the given file the program will crash.
 * 
 * @author Steven Loscalzo <Steven.Loscalzo@rl.af.mil>
 *
 */
public class FeatureSetReader {

	/**
	 * This method returns a sorted list of all connected neurons.  The integers in this
	 * list correspond to the position each feature is in the input vector, so 0 is the
	 * first feature, 1 is the second feature, and so on.
	 * 
	 * @param fileName the name of the persistence xml file
	 * @param chromId the id of the chromosome to analyze
	 * @return the positions of the connected features in the neural network described in the xml file.
	 * 
	 * @throws Exception there are a variety of exceptions possible, mostly from the XML queries
	 * being used.  It probably means that there is a malformed or missing fileName or chromId.
	 */
	public static List<Integer> getConnectedInputs(String fileName, int chromId) throws Exception{
		//Need to go through the PERSISTENCE folder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(fileName);
		XPathFactory xFact = XPathFactory.newInstance();
		XPath xpath = xFact.newXPath();
		//Get all of the input nodes for this network
		XPathExpression expr = xpath.compile("//chromosome[@id='"+chromId+"']/neuron[@type='in']/@id");
		NodeList inNeurons = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		//The minId thing is not going to work in the event that new Id's are added.
		//Instead, we must use the feature-map element to figure this out
		XPathExpression exp3 = xpath.compile("//feature-id/text()");
		NodeList mapping = (NodeList)exp3.evaluate(doc, XPathConstants.NODESET);
		Map<Integer, Integer> inputNodes = new HashMap<Integer, Integer>();

		int minId = Integer.MAX_VALUE;
		//Dump all of the ids in a set for faster lookup later
		//Also get the min id so I can normalize the input ids to be from 0 - num inputs
		for(int i = 0; i < inNeurons.getLength(); ++i){
			int id = Integer.parseInt(inNeurons.item(i).getNodeValue());
			int feature = id;
			if(mapping.getLength() != 0){
				feature = Integer.parseInt(mapping.item(i).getNodeValue());
			}
			else{
				if(id < minId)
					minId = id;
			}
			inputNodes.put(id, feature);
		}


		//Get all of the connections
		XPathExpression exp2 = xpath.compile("//chromosome[@id='"+chromId+"']/connection");
		NodeList conns = (NodeList)exp2.evaluate(doc, XPathConstants.NODESET);
		//See which connections go to input nodes
		Set<Integer> connectedIn = new HashSet<Integer>();
		for(int i = 0;i < conns.getLength(); ++i){
			int srcId = Integer.parseInt(conns.item(i).getAttributes().getNamedItem("src-id").getNodeValue());
			if(inputNodes.containsKey(srcId)){
				connectedIn.add(inputNodes.get(srcId));
			}
		}
		if(mapping.getLength() == 0){
			Set<Integer> sorted = new HashSet<Integer>();
			for(Integer id : connectedIn)
				sorted.add(id-minId); //Make the connected id's start from 0
			connectedIn = sorted;
		}
		List<Integer> result = new ArrayList<Integer>(connectedIn);
		Collections.sort(result); //Now the name is correct!
		return result;
	}

	/**
	 * This is just a demo program to show how the getConnectedInputs method can be used.
	 * I also have it around for debugging purposes to make sure my xml queries were correct.
	 * 
	 * If we assume the xml file is:
	 * ./PERSISTENCE/folder1/folder2/file.xml
	 * args[0] should be folder1/folder2/file.xml
	 * @param args[0] is the xml file to read to get the feature set
	 *        args[1] is the chromosome id
	 *        args[2] is the number of relevant features (rel if id &lt; args[2])
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public static void main(String[] args) throws Exception {
		List<Integer> sorted = getConnectedInputs(args[0], Integer.parseInt(args[1]));
		//Print out a description of the features:
		int rel = 0;

		//Assume that the first args[2] features are relevant
		int numRel = Integer.parseInt(args[2]);
		for(Integer id : sorted)
			if(id < numRel)
				rel++;
		System.out.println("Num relevant features: "+rel);
		System.out.println("Num irrelevant features: "+(sorted.size()-rel));
		System.out.println("Features: "+sorted);
	}

}
