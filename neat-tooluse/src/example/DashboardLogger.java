package example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.anji.util.Properties;
import example.ExampleFitnessFunction.StickState;

public class DashboardLogger {
	private File csvDir;
	private final Properties props;
	//Model Parameters - Stores the user-defined parameters that are set before running the program
	private String modelParameters_filename = "model_parameters.csv";
	private FileWriter modelParameters_writer;
	
	//Generations - Stores the max and avg fitness during each generation.
	private String generations_filename = "generation_fitness.csv";
	private FileWriter generations_writer;
	
	//Chromosomes - Stores the generation,chromosome_id,and fitness
	private String chromosomes_filename = "chromosome_fitness.csv";
	private FileWriter chromosomes_writer;
	
	public enum Log {GENERATIONS, CHROMOSOMES};
	
	public DashboardLogger(File resultsRoot, Properties props) {
		this.props = props;
		//Initialize the CSV directory
		csvDir = new File(resultsRoot, "dashboard/csv");
		if(!csvDir.exists()) {//If the folders don't exist, create them
			csvDir.mkdirs();
		}
		
		//Initialize and open the generations file
		initializeGenerations(csvDir);
		initializeChromosomes(csvDir);
	}
	
	private void initializeGenerations(File csvDir) {
		try {
			File path = new File(csvDir, generations_filename);
            generations_writer = new FileWriter(path); // overwrite existing file
            // Write header
            generations_writer.write("generation,avg_fitness,max_fitness,percent_reaches,max_chromosome_id,percent_reaches_max_chromosome\n");//Write the header line
            generations_writer.flush();
            generations_writer.close();//Close the initialization, so that the file can be reopened in append mode.
            generations_writer = new FileWriter(path, true);//Open in append mode
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void initializeChromosomes(File csvDir) {
		try {
			File path = new File(csvDir, chromosomes_filename);
            chromosomes_writer = new FileWriter(path); // overwrite existing file
            // Write header
            chromosomes_writer.write("generation,chromosome_id,fitness\n");//Write the header line
            chromosomes_writer.flush();
            chromosomes_writer.close();//Close the initialization, so that the file can be reopened in append mode.
            chromosomes_writer = new FileWriter(path, true);//Open in append mode
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void append(Log log, double[] data) {
		FileWriter writer = getWriter(log);
		if(writer==null){//If a valid log wasn't given
			System.out.println("Error:Can't append to log. Valid log name was not provided.");
			return;
		}
		try {
			String line = "";
			for(int i = 0; i < data.length-1; i++) {
				line += data[i] + ",";
			}
			line += data[data.length-1] + "\n";//Add the last piece of data
			writer.write(line);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes all available logs
	 */
	public void closeAll() {
		close(Log.GENERATIONS);
		close(Log.CHROMOSOMES);
	}
	
	public void close(Log log) {
		FileWriter writer = getWriter(log);
		if(writer==null) {
			System.out.println("Error:Could not close log. Valid log name was not provided.");
			return;
		}
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Error:Could not close log. Valid log name was not provided.");
		}
	}
	
	private FileWriter getWriter(Log log) {
		switch(log) {
		case GENERATIONS:
			return generations_writer;
		case CHROMOSOMES:
			return chromosomes_writer;
		default:
			return null;
		}
	}
	
	/**
	 * 
	 * @param maxTimesteps
	 * @param numTrials
	 * @param numGenerations
	 * @param populationSize
	 * @throws IOException
	 */
	public void logModelParameters(int maxTimesteps, int numTrials, StickState stickState) throws IOException {
		int numGenerations = -1;
		int populationSize = -1;
		String numGenerationsProperty = props.getProperty("num.generations");
		String populationSizeProperty = props.getProperty("popul.size");
		if (numGenerationsProperty != null) {
			numGenerations = Integer.parseInt(numGenerationsProperty);
		}
		if (populationSizeProperty != null) {
			populationSize = Integer.parseInt(populationSizeProperty);
		}
		
		String str_stickState = "";
		switch(stickState) {
		case NO_STICK:
			str_stickState = "No Stick";
			break;
		case ONE_STICK:
			str_stickState = "One Stick";
			break;
		case TWO_STICKS:
			str_stickState = "Two Sticks";
			break;
		}
        
		try {
			modelParameters_writer = new FileWriter(new File(csvDir, modelParameters_filename));
			modelParameters_writer.write("Max Timesteps,Number of Trials,Number of Generations,Population Size,Stick State\n");
			modelParameters_writer.write(maxTimesteps + ", " + 
									 	 numTrials + ", " + 
									 	 numGenerations + ", " + 
									 	 populationSize + ", " + 
									 	 str_stickState);
			modelParameters_writer.flush();
			modelParameters_writer.close();
		} catch(Exception e) {
			System.out.println("Error: Unable to log model parameters.");
		}
	}
}
