package mil.af.rl.novelty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jgap.NoveltyGenotypeAdapter;
import org.jgap.event.GeneticEvent;
import org.jgap.event.GeneticEventListener;

import com.anji.persistence.Persistence;
import com.anji.util.Properties;

public class NoveltyArchivePersistenceEventListener implements GeneticEventListener{
	private Persistence db = null;
	
	public void init( Properties props ){
		db = (Persistence) props.singletonObjectProperty( Persistence.PERSISTENCE_CLASS_KEY );

	}
	
	@Override
	public void geneticEventFired( GeneticEvent event ){
		NoveltyGenotypeAdapter genotype = (NoveltyGenotypeAdapter) event.getSource();
		if(GeneticEvent.RUN_COMPLETED_EVENT.equals(event.getEventName())){
			NoveltyArchive arch = genotype.getArchive();
			try(PrintWriter out = new PrintWriter(new FileWriter(new File(((mil.af.rl.anji.FilePersistence)db).fullPath(arch.getXmlRootTag(), arch.getXmld())+File.separator+arch.getXmlRootTag())))){
					out.println(arch.toXml());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
