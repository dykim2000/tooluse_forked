package example;

import java.io.File;

import org.apache.log4j.Logger;
import org.jgap.Chromosome;
import org.jgap.Configuration;

import com.anji.integration.ActivatorTranscriber;
import com.anji.persistence.*;
import com.anji.util.*;
import org.apache.log4j.BasicConfigurator;

public class EvaluateIt {
	private static final String DEFAULT_CHROMOSOME_ID = "11678";

	public static void main( String[] args ) throws Exception {
		String chromosomeId = DEFAULT_CHROMOSOME_ID;
		if ( args.length > 0 ) {
			chromosomeId = args[0];
		}
		String propertiesFilename = RunConfig.getPropertiesFilename(args, 1, RunConfig.DEFAULT_PROPERTIES_FILE);
		String runPrefix = RunConfig.getPrefixFromPropertiesFilename(propertiesFilename);

		Properties props = new Properties();
		props.loadFromResource(propertiesFilename);
		RunConfig.applyOutputOverrides(props, runPrefix);
		File resultsRoot = RunConfig.ensureResultsRoot(runPrefix);
		
		BasicConfigurator.configure();
		logger.info("Using properties: " + propertiesFilename);
		logger.info("Loading chromosome id: " + chromosomeId);
		logger.info("Saving outputs under: " + resultsRoot.getPath());
		
		ExampleFitnessFunction ff = new ExampleFitnessFunction();
		ff.dashboardLog = false;//Make sure logging is turned off
		
		ff.setNeuronActLogFile(new File(resultsRoot, "neurons_actLog.txt").getPath());
		ff.setNeuronActLog(true);//record neuron activation log 
		ff.setEnableDisplay(true);//enable display : turn this off for fast computation
		ff.setActivatorTranscriber((ActivatorTranscriber) props.singletonObjectProperty(ActivatorTranscriber.class));
		ff.init( props );
		Persistence db = (Persistence) props.newObjectProperty( Persistence.PERSISTENCE_CLASS_KEY );
		Configuration config = new DummyConfiguration();

		Chromosome chrom = db.loadChromosome( chromosomeId, config );
		if ( chrom == null )
		    throw new IllegalArgumentException( "no chromosome found: " + chromosomeId );
		try {
		    ff.evaluate(chrom);
		} catch (RuntimeException e) { //Stop early if an error occurs or there's an early stop due to training stagnation.
		    System.out.println(e.getMessage());
		}
		logger.info( "Fitness = " + chrom.getFitnessValue() );
	}
	
	private static final Logger logger = Logger.getLogger( EvaluateIt.class );
}
