package mil.af.rl.anji;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mil.af.rl.predictive.PFSInfo;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.Specie;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anji.integration.Generation;
import com.anji.integration.XmlPersistableRun;
import com.anji.persistence.Persistence;
import com.anji.util.Properties;

/**
 * This code is primarily a clone of com.anji.persistence.FilePersistence, but due to visibility issues,
 * a simple extension was not possible.  Changing the base PERSISTENCE directory behavior is the motivation
 * for this clone.
 * 
 * @author sloscal1
 *
 */
public class FilePersistence extends com.anji.persistence.FilePersistence {
	/** This is a shadowed variable from the FilePersistence class, couldn't see it due to visibility */
	private final static Logger logger = Logger.getLogger( FilePersistence.class );
	/** Composite object that most of the persistence operations are delegated to */
//	private FilePersistence base;
	private File realBaseDir; //As opposed to the fake baseDir in base...
	private String realRunId; //As opposed to the runId in base (not accessible)
	
	/**
	 * See <a href=" {@docRoot}/params.htm" target="anji_params">Parameter Details </a> for
	 * specific property settings.
	 * @param props configuration parameters
	 */
	@Override
	public void init(Properties props) {
		super.init(props);
//		base = new FilePersistence();
//		base.init(props);
		//_SL_ 6/24/10 
		//Modified this file so that it actually uses the base directory property since I don't
		//always want the persistence files in ./PERSISTENCE 
		String baseDirStr = props.getProperty( BASE_DIR_KEY,".");
		baseDirStr += "/PERSISTENCE/" +props.getProperty( "run.name" ) + "_" + props.getProperty("random.seed");

		realBaseDir = new File( baseDirStr );
		realBaseDir.mkdirs();
		if ( !realBaseDir.exists() )
			throw new IllegalArgumentException( "base directory does not exist: " + baseDirStr );
		if ( !realBaseDir.isDirectory() )
			throw new IllegalArgumentException( "base directory is a file: " + baseDirStr );
		if ( !realBaseDir.canWrite() )
			throw new IllegalArgumentException( "base directory not writable: " + baseDirStr );
	}
	
	/**
	 * Construct full path of file based on <code>type</code> and <code>key</code>.
	 * 
	 * @param type
	 * @param key
	 * @return String resulting path
	 */
	public String fullPath( String type, String key ) {
		//Altered to use realBaseDir instead of baseDir
		StringBuffer result = new StringBuffer( realBaseDir.getAbsolutePath() );
		result.append( File.separatorChar ).append( type );

		File collectionDir = new File( result.toString() );
		if ( collectionDir.isDirectory() == false ) {
			if ( collectionDir.exists() )
				throw new IllegalArgumentException( result.toString() + " is a file" );
			collectionDir.mkdir();
		}

		result.append( File.separatorChar ).append( type ).append( key ).append( ".xml" );
		return result.toString();
	}

	@Override
	public Genotype loadGenotype(Configuration config) {
		try {
			InputStream in = loadStream( XmlPersistableRun.RUN_TAG, realRunId );
			return genotypeFromRunXml( in, config );
		}
		catch ( FileNotFoundException e ) {
			return null;
		}
		catch ( Exception e ) {
			String msg = "error loading run " + realRunId;
			logger.error( msg, e );
			throw new IllegalStateException( msg + ": " + e );
		}
	}
	
	/**
	 * @param type
	 * @param key
	 * @return streamed data of resource
	 * @throws IOException
	 */
	private InputStream loadStream( String type, String key ) throws IOException {
		return new FileInputStream( fullPath( type, key ) );
	}

	@Override
	public void reset() {
		File[] dirs = realBaseDir.listFiles();
		for ( int i = 0; i < dirs.length; ++i ) {
			File dir = dirs[ i ];
			if ( dir.isDirectory() ) {
				File[] files = dir.listFiles();
				for ( int j = 0; j < files.length; ++j ) {
					File file = files[ j ];
					String msg = file.delete() ? "file deleted: " : "error deleting file: ";
					msg += file.getAbsolutePath();
					logger.log( Priority.DEBUG , msg );
				}
			}
		}
	}

	@Override
	public void startRun(String arg0) {
		super.startRun(arg0);
		realRunId = arg0;
	}
	
	/**
	 * Construct new <code>Genotype</code> object from population in specified run XML data.
	 * 
	 * @param runXml XML data from which to construct initial population
	 * @return new <code>Genotype</code>
	 * @throws Exception
	 */
	private Genotype genotypeFromRunXml( InputStream runXml, Configuration config )
	throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse( runXml, realBaseDir.getAbsolutePath() );
		//_SL_ 3/2/11 turned the if statement into a while loop.  This form of parsing is
		//terrible and XPath should be used.  Anyway, the existing code did not allow the
		//standard xml header or comments at the top of the file.
		Node runNode = doc.getFirstChild();
		while(runNode != null && !runNode.getNodeName().equals(XmlPersistableRun.RUN_TAG))
			runNode = runNode.getNextSibling();
		if(runNode == null)
			throw new IllegalArgumentException( XmlPersistableRun.RUN_TAG +" not found at top level" );

		// loop through list to find last generation
		Node generationNode = null;
		for ( int i = 0; i < runNode.getChildNodes().getLength(); ++i ) {
			Node nextNode = runNode.getChildNodes().item( i );
			if ( Generation.GENERATION_TAG.equals( nextNode.getNodeName() ) )
				generationNode = nextNode;
		}

		return genotypeFromXml( generationNode, config, this );
	}
	
	/**
	 * Create a <code>Genotype</code> from XML.
	 * 
	 * @param generationNode XML
	 * @param config
	 * @param db persistence repository from which to read chromosomes
	 * @return new genotype
	 * @throws Exception
	 */
	private static Genotype genotypeFromXml( Node generationNode, Configuration config,
			Persistence db ) throws Exception {
		if ( Generation.GENERATION_TAG.equals( generationNode.getNodeName() ) == false )
			throw new IllegalArgumentException( "node name not " + Generation.GENERATION_TAG );

		// loop through list to find chromosomes
		ArrayList<Chromosome> chroms = new ArrayList<Chromosome>();
		for ( int generationChildIdx = 0; generationChildIdx < generationNode.getChildNodes()
		.getLength(); ++generationChildIdx ) {
			Node specieNode = generationNode.getChildNodes().item( generationChildIdx );
			if ( Specie.SPECIE_TAG.equals( specieNode.getNodeName() ) ) {
				// for each specie ...
				NamedNodeMap specieAttrs = specieNode.getAttributes();
				if ( specieAttrs == null )
					throw new IllegalArgumentException( "missing specie attributes" );

				// ... and loop through chromosomes
				for ( int specieChildIdx = 0; specieChildIdx < specieNode.getChildNodes().getLength(); ++specieChildIdx ) {
					Node chromNode = specieNode.getChildNodes().item( specieChildIdx );
					if ( Specie.CHROMOSOME_TAG.equals( chromNode.getNodeName() ) ) {
						NamedNodeMap chromAttrs = chromNode.getAttributes();
						if ( chromAttrs == null )
							throw new IllegalArgumentException( "missing chromosome attributes" );
						Node chromIdNode = chromAttrs.getNamedItem( Specie.ID_TAG );
						if ( chromIdNode == null )
							throw new IllegalArgumentException( "missing chromosome id" );

						// get id and load chromosome from persistence (skip if representative since its
						// already been added
						Long chromId = Long.valueOf( chromIdNode.getNodeValue() );
						Chromosome c = db.loadChromosome( chromId.toString(), config );
						if ( c != null )
							chroms.add( c );
						else
							logger.warn( "chromosome in run not found: " + chromId );
					}
				}
			}
		}

		// don't return empty genotype
		if ( chroms.size() <= 0 )
			return null;

		// sort in order of id so that they will be added in proper order (age)
		Collections.sort( chroms );
		return new Genotype( config, chroms );
	}
}
