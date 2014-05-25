package mil.af.rl.anji;

import org.jgap.InvalidConfigurationException;

import com.anji.neat.NeatConfiguration;
import com.anji.util.Properties;

/**
 * Allows for the configuration file to report whether or not the network is
 * fully connected, which in turn is a simple way to control FS-NEAT versus
 * regular NEAT functionality.
 * 
 * @author sloscal1
 *
 */
public class NeatConfigurationAdapter extends NeatConfiguration{
	/** Gen UID */
	private static final long serialVersionUID = 4856320435059350692L;
	private boolean fullyConnected;
	
	public NeatConfigurationAdapter(Properties props)
			throws InvalidConfigurationException {
		super(props);
		fullyConnected = props.getBooleanProperty( INITIAL_TOPOLOGY_FULLY_CONNECTED_KEY, true );
	}
	
	public boolean isFullyConnected(){
		return fullyConnected;
	}
}
