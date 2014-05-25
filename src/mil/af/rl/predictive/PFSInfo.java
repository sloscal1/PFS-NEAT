package mil.af.rl.predictive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgap.Chromosome;

/**
 * 
 * @author sloscal1
 *
 */
public class PFSInfo {
	private Set<Long> collectingChroms = new HashSet<>();
	private Map<Long,SampleContainer> containerMap = new HashMap<>();

	public void setSampleContainer(Chromosome chrom, SampleContainer container){
		containerMap.put(chrom.getId(), container);
	}

	public SampleContainer getContainer(Chromosome chrom) {
		return containerMap.get(chrom.getId());
	}
	
	public void setCollecting(Chromosome chrom, boolean collecting){
		if(collecting)
			collectingChroms.add(chrom.getId());
		else
			collectingChroms.remove(chrom.getId());
	}
	
	public boolean isCollecting(Chromosome chrom) {
		return collectingChroms.contains(chrom.getId());
	}

	public void removeInfo(Chromosome extinct) {
		Long id = extinct.getId();
		containerMap.remove(id);
		collectingChroms.remove(id);
	}
}
