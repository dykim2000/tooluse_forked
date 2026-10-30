package example;

import java.io.IOException;
import java.io.File;

import org.apache.log4j.BasicConfigurator;


import com.anji.integration.ActivatorTranscriber;
import com.anji.neat.Evolver;
import com.anji.util.Properties;

public class Driver {
	public static void main(String[] args){
		Properties properties = new Properties();
		final String propertiesFilename = RunConfig.getPropertiesFilename(args, 0, RunConfig.DEFAULT_PROPERTIES_FILE);
		final String runPrefix = RunConfig.getPrefixFromPropertiesFilename(propertiesFilename);
		try{
			properties.loadFromResource(propertiesFilename);
			RunConfig.applyOutputOverrides(properties, runPrefix);
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		File resultsRoot = RunConfig.ensureResultsRoot(runPrefix);
		System.out.println("Using properties: " + propertiesFilename);
		System.out.println("Saving outputs under: " + resultsRoot.getPath());
		BasicConfigurator.configure();
		ExampleFitnessFunction fitnessFunction = (ExampleFitnessFunction) properties.singletonObjectProperty(Evolver.FITNESS_FUNCTION_CLASS_KEY);
		fitnessFunction.dashboardLogger = new DashboardLogger(resultsRoot, properties);
		Evolver evolver = new Evolver();
		try{
			evolver.init(properties);
		}catch(Exception e){
			e.printStackTrace();
		}
		fitnessFunction.setActivatorTranscriber((ActivatorTranscriber) properties.singletonObjectProperty(ActivatorTranscriber.class));
		try {
			evolver.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Close the dashboard logger
		fitnessFunction.dashboardLogger.closeAll();
		
	}
	
}
