package example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import example.DashboardLogger;
import example.DashboardLogger.Log;

//import javax.management.timer.Timer;
import javax.swing.Timer;


import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TranscriberException;
import com.anji.util.Properties;

public class ExampleFitnessFunction implements BulkFitnessFunction{
	
	private final static int DEFAULT_TIMESTEPS = 1000;// = 500;  // The maximum steps each trial
	private int maxTimesteps = DEFAULT_TIMESTEPS;

	private final static int DEFAULT_NUM_TRIALS = 100;  // The maximum number of trials
	private int numTrials = DEFAULT_NUM_TRIALS;
	
	boolean enableDisplay = false;
	boolean testMode = false;
	
	boolean neuronActLog = false;
	FileWriter outFile = null;
	PrintWriter out = null;
	String filenameNActLog = "./neurons_actLog.txt";
	
	//private ActivatorTranscriber factory;
	private ActivatorTranscriber activatorTranscriber;
	private double maxFitnessValue;
	
	public enum StickState { NO_STICK, ONE_STICK, TWO_STICKS }
	private StickState stickState = StickState.ONE_STICK;
	
	private static final int l1 = 80;
	private static final int l2 = 100;
	private static final int tool1_len = 80;
	private static final int tool2_len = 80;
	
	public boolean dashboardLog = true;
	public DashboardLogger dashboardLogger;
	
	private int currentGeneration = 1;//Tracks which generation is currently being evolved.
	private double avgFitnessPerGeneration = 0;
	private int chromosomesPerGeneration = 0;
	private int reachesPerGeneration = 0;//The number of times that the target is successfully reached during each generation.
	private int reachesPerChromosome = 0;//The number of times that the target is successfully reached by each chromosome.
	
	private double maxReachDist = getMaxReach();//The maximum distance that the arm can reach. This depends on whether 0, 1, or 2 sticks are used.
	
	//Early termination
	private final double FITNESS_THRESHOLD = 1e-6;   // minimum required improvement
	private final int ROLLING_WINDOW = 10;       // number of generations in the window
	private final LinkedList<Double> fitnessHistory = new LinkedList<>();

	
	private int test_set[][];
	
	public void setEnableDisplay(boolean tf) {
		enableDisplay = tf;
	}
	//This is called when Evaluatedit is run to set neuronActLog true
	public void setNeuronActLog(boolean tf) {
		neuronActLog = tf;
	}

	public void setNeuronActLogFile(String path) {
		filenameNActLog = path;
	}
	
	public ExampleFitnessFunction() throws IOException {
		if( testMode )
		{
			try {
				BufferedReader reader = new BufferedReader(new FileReader("/Users/kiyrahkeith/Desktop/Choe_Lab_Research/tooluse/Everything_09112014/db/test/test_set2.txt"));
				int size, x, y;
				String input;
				input = reader.readLine();
				size = Integer.parseInt(input);
				test_set = new int[size][2];
				for( int i = 0; i < size; i++ ) {
					input = reader.readLine();
					x = Integer.parseInt(input);
					input = reader.readLine();
					y = Integer.parseInt(input);
					test_set[i][0] = x;
					test_set[i][1] = y;
					System.out.println(x);
					System.out.println(y);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
     * @see com.anji.util.Configurable#init(com.anji.util.Properties)
     */
	public void init( Properties props ) throws Exception {
		try {
			String stickStateProp = props.getProperty("stick.state");
			if (stickStateProp != null) {
				stickState = parseStickState(stickStateProp);
			}
			maxReachDist = getMaxReach();
			// 1. Init activator transcriber
			// This seems never called for our case
			//factory = (ActivatorTranscriber) props.singletonObjectProperty( ActivatorTranscriber.class );
		}
		catch ( Exception e ) {
			throw new IllegalArgumentException( "invalid properties: " + e.getClass().toString() + ": "
					+ e.getMessage() );
		}
	}

	private StickState parseStickState(String value) {
		String normalized = value.trim().toUpperCase();
		if ("0".equals(normalized) || "NO_STICK".equals(normalized) || "NOSTICK".equals(normalized)) {
			return StickState.NO_STICK;
		}
		if ("1".equals(normalized) || "ONE_STICK".equals(normalized) || "ONESTICK".equals(normalized)) {
			return StickState.ONE_STICK;
		}
		if ("2".equals(normalized) || "TWO_STICKS".equals(normalized) || "TWOSTICKS".equals(normalized)) {
			return StickState.TWO_STICKS;
		}
		throw new IllegalArgumentException("Invalid stick.state value: " + value + " (expected 0, 1, 2, NO_STICK, ONE_STICK, TWO_STICKS)");
	}
    
    public void evaluate( Chromosome c ) throws InterruptedException {
    	Activator activator;
    	
		try {
			activator = activatorTranscriber.newActivator(c);
			//activator = factory.newActivator( c );
		
			double input[] = new double[10];
			double output[];
			double output0, output1;
			double fitness = 0.0;
			
			int bx = 400;
			int by = 400;
			
			double distance = 0.0; //the distance to the target from the end effector
			double finalDistanceSum = 0.0;
			double finalStepSum = 0.0;
			double countReaches = 0.0;
			double countHold = 0.0;
			double countHold2 = 0.0;
			reachesPerChromosome = 0;
			
			if(dashboardLog) {
				try {
					dashboardLogger.logModelParameters(maxTimesteps, numTrials, stickState);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(neuronActLog) {
				try {
					outFile = new FileWriter(filenameNActLog);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				out = new PrintWriter(outFile);
			}
			
			ArmReaching arm = new ArmReaching( bx, by, l1, l2, tool1_len, tool2_len, stickState);
			Display canvas = new Display();
			if( enableDisplay )
				canvas.init( bx, by, 800, 600 );
			
			int j = 0;
			
			for( int i = 0; i < numTrials; i++ ) {
				if(neuronActLog) {
					out.println("numTrial: " + i);
				}
				
				//reset
				arm.resetArm();
				arm.resetTool();
				arm.resetTool2();
				
				if( testMode )
					arm.setTarget(test_set[i][0], test_set[i][1]);
				else
					arm.resetTarget(stickState);
//				
				
				for(j = 0; j < maxTimesteps; j++ ) {
					
					input[0] = arm.theta1/90;
					input[1] = arm.theta2/90; //should be divided by 170?

					//straight angle to target (degree_theta_bt)
					//straight distance to target (d_bt)
					double d_bt = Math.sqrt((arm.bx-arm.tx)*(arm.bx-arm.tx) + (arm.by-arm.ty)*(arm.by-arm.ty));
					double theta_bt = Math.asin((arm.bx-arm.tx) / -d_bt);
					double degree_theta_bt = theta_bt * 180 / Math.PI;
					
					//straight angle to the end effector (degree_theta_be)
					//straight distance to the end effector (d_be)
					double d_be = Math.sqrt((arm.bx-arm.ex)*(arm.bx-arm.ex) + (arm.by-arm.ey)*(arm.by-arm.ey));
					double theta_be = Math.asin((arm.bx-arm.ex) / -d_be);
					double degree_theta_be = theta_be * 180 / Math.PI;
					if( arm.ey > arm.by )
					{
						if( arm.ex > arm.bx )
							degree_theta_be = 180 - degree_theta_be;
						else
							degree_theta_be = -180 - degree_theta_be;
					}

					
					double r_d = d_bt - d_be; // relative distance
					double r_degree_theta = degree_theta_bt - degree_theta_be;  // relative angle
					
					
					double d_bto = Math.sqrt((arm.bx-arm.pick_x)*(arm.bx-arm.pick_x) + (arm.by-arm.pick_y)*(arm.by-arm.pick_y));
					double theta_bto = Math.asin((arm.bx-arm.pick_x) / -d_bto);
					double degree_theta_bto = theta_bto * 180 / Math.PI;
					if( arm.pick_y > arm.by ) {
						if( arm.pick_x > arm.bx )
							degree_theta_bto = 180 - degree_theta_bto;
						else
							degree_theta_bto = -180 - degree_theta_bto;
					}
					
					double r_d_pick = d_bto - d_be;
					//double r_d_drop = l1+l2;
					double r_degree_pick = degree_theta_bto - degree_theta_be;
					//double r_degree_drop = -90 - degree_theta_be;
					
					// Calculate relative distance and angle to second stick
					double d_bto2 = Math.sqrt((arm.bx - arm.pick2_x)*(arm.bx - arm.pick2_x) + 
	                           (arm.by - arm.pick2_y)*(arm.by - arm.pick2_y));
					double theta_bto2 = Math.asin((arm.bx - arm.pick2_x) / -d_bto2);
					double degree_theta_bto2 = theta_bto2 * 180 / Math.PI;
				
					// Quadrant correction
					if (arm.pick2_y > arm.by) {
					    if (arm.pick2_x > arm.bx)
					        degree_theta_bto2 = 180 - degree_theta_bto2;
					    else
					        degree_theta_bto2 = -180 - degree_theta_bto2;
					}
					double r_d_pick2 = d_bto2 - d_be;
					double r_degree_pick2 = degree_theta_bto2 - degree_theta_be;
					
					
					// The relative distance and angle from the end effector to the target
					input[2] = r_d/260;
					input[3] = r_degree_theta / 90;
					
					// STICK 1
					input[4] = 0;
					input[5] = 0;
					//Only use the parameters if stick 1 is in use and not already being held
					if(stickState != StickState.NO_STICK && !arm.holding) {

						// the relative distance and angle from the end effector to stick 1
						//Normalize dist by the total length of the arm+stick1
						input[4] = r_d_pick/ maxReachDist; 
						input[5] = r_degree_pick/90;
					}
					
					// STICK 2
					input[6] = 0;
					input[7] = 0;
					//Only use the parameters if stick 2 is in use, stick 1 is already held,  and stick 2 is not already being held
					if(stickState == StickState.TWO_STICKS && arm.holding && !arm.holding2) {

						// the relative distance and angle from the end effector to stick 1
						// Normalize dist by the total length of the arm+stick1+stick2
						input[6] = r_d_pick2 / maxReachDist;
						input[7] = r_degree_pick2 / 90;
					}
					
					// the relative location to drop
					//input[6] = r_d_drop;
					//input[7] = r_degree_drop/90;


					//These are to give some (sensory) feedback if the joints reach the limit
					//So hopefully learn to change the direction instead of pushing to the limit continuously  
					input[8] = 0;
					input[9] = 0;

					if ( Math.abs(arm.theta1 - 150.0 ) < 0.0001 ) {
						input[8] = 1.0;
					}
					else if( Math.abs(arm.theta1 + 150.0 ) < 0.0001 ) {
						input[8] = -1.0;
					}
					if ( Math.abs(arm.theta2 - 150.0 ) < 0.0001 ) {
						input[9] = 1.0;
					}
					else if( Math.abs(arm.theta2 + 150.0 ) < 0.0001 ) {
						input[9] = -1.0;
					}
					
					//input[8] = d_bt - l1 - l2; //This was in the original code, but was unused.

					if(neuronActLog) {
						out.println("numTrial: " + i + " timeStep: " + j);
						output = activator.next(input);
						out.printf("\n");
					} else {
						output = activator.next(input);
					}
					//activator.reset(); //reset here is correct?
					
					//				output[0] = 2*(output[0]-0.5);
					//				output[1] = 2*(output[1]-0.5);
					// activation function
					// sigmoid: 0 ~ 1
					// tanh: -1 ~ 1
					output0 = 3*(output[0]-0.5);
					output1 = 3*(output[1]-0.5);
					//System.out.println(output[0]);
					//System.out.println(output[1]);
					//fitness += arm.move(output0, output1);
					arm.move(output0, output1);
					//distance = arm.getDist2Target();

					if( enableDisplay ) {
						canvas.nextFrame( arm );
						try {
							Thread.sleep(16);
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}
					
					if(arm.hasReach()) {
						break;
					}
					
				} //end for( int j = 0; j < maxTimesteps; j++ )
				
				//add up distance and step values to calculate the fitness later
				if(arm.hasReach()) {
					//should we put some extra reward when it reaches to the target?
					//add up the number of reaches?
					countReaches += 1.0;
					reachesPerChromosome += 1;
					reachesPerGeneration += 1;
				}
				if( arm.holding ) {
					countHold += 1.0;
				}
				
				if( arm.holding2 ) {
					countHold2 += 1.0;
				}
				
				
				distance = arm.getDist2Target();
				finalDistanceSum += distance;
				finalStepSum += j;
								
			} //end for( int i = 0; i < numTrials; i++ )
			
			fitness = calculateFitness(finalDistanceSum, finalStepSum, reachesPerChromosome, countHold, countHold2);
			
			//System.out.println("num of reaches: " + countReaches);
			//System.out.println("finalDistanceSum: " + finalDistanceSum + " distFactor: " + distFactor);
			//System.out.println("finalStepSum: " + finalStepSum + " stepFactor: " + stepFactor);
			System.out.println("Finished one with fitness " + fitness);
			
			c.setFitnessValue((int)fitness);
			avgFitnessPerGeneration += fitness;
			chromosomesPerGeneration++;//Chromosomes per generation should always be the population size, but it is being recorded just in case.
		} catch (TranscriberException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(neuronActLog) {
			out.close();
		}
		
    }
    
    private double calculateFitness(double finalDistance, double finalSteps, double reaches, double hold1, double hold2) {
    	double fitness = 0;
    	
		double D = (finalDistance/((double)numTrials*2*maxReachDist));
		D = 1.0-D;
		D = 0.5 + 0.5*D;
		
		double S = ((finalSteps)/numTrials)/(maxTimesteps); 
		S = 1.0-S;
		S = 0.5 + 0.5*S;
		//double countFactor = (2.0*countReaches)/(double)numTrials;
		double T1 = hold1/(double)numTrials;
		T1 = 0.5 + 0.5*T1;
		
		double T2 = hold2/(double)numTrials;
		T2 = 0.5 + 0.5*T2;
		
		//System.out.println("\nD: " + D + ", S: " + S + ", T1: " + T1 + ", T2: " + T2);
		
		switch(stickState) {
			case NO_STICK:
				fitness = D*S;
				break;
			case ONE_STICK:
				fitness = D*S*T1;
				break;
			case TWO_STICKS:
				fitness = D*S*T1*T2;
				break;
		}
		
		fitness = fitness * 1000;
		return fitness;
    }

    private double getMaxReach() {
    	double maxReach = l1 + l2;
    	
    	switch(stickState) {
			case ONE_STICK:
				maxReach += tool1_len;
				break;
			case TWO_STICKS:
				maxReach += tool1_len + tool2_len;
				break;
    	}
    	return maxReach;
    }
    
	public void evaluate(List genotypes) {
		Iterator it = genotypes.iterator();
		maxFitnessValue = 0;
		reachesPerGeneration = 0;
		Long maxChromosomeIdPerGeneration = (long)-1.0;
		int reachesOfMaxChromosome = 0;//The number of times that the best performing chromosome reached the target.
		while( it.hasNext() )
		{
			Chromosome c = (Chromosome) it.next();
			try {
				evaluate(c);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( maxFitnessValue < c.getFitnessValue() ) {
				maxFitnessValue = c.getFitnessValue();
				maxChromosomeIdPerGeneration = c.getId();
				reachesOfMaxChromosome = reachesPerChromosome;
			}
			
			if(dashboardLog) dashboardLogger.append(Log.CHROMOSOMES, new double[] {currentGeneration, c.getId(), c.getFitnessValue()});
			
		}
		System.out.println("Finished generation: " + currentGeneration);
		avgFitnessPerGeneration /= chromosomesPerGeneration;
		
		//Calculates the success rate for the percentage of time that the target is reached. Range: (0,100)
		int percentReachesPerGeneration = (reachesPerGeneration*100)/(numTrials*chromosomesPerGeneration);
		int percentReachesOfMaxChromosome = (reachesOfMaxChromosome*100)/numTrials;
		
		if(dashboardLog) dashboardLogger.append(Log.GENERATIONS, new double[] {currentGeneration,avgFitnessPerGeneration, maxFitnessValue,percentReachesPerGeneration,maxChromosomeIdPerGeneration, percentReachesOfMaxChromosome});
		avgFitnessPerGeneration = 0;//Reset log parameters
		chromosomesPerGeneration = 0;
		currentGeneration++;
		
		// Early stopping for stagnation
		// Add current generation's best fitness to history
		fitnessHistory.add(maxFitnessValue);

		// Keep the list size within the rolling window
		if (fitnessHistory.size() > ROLLING_WINDOW) {
		    fitnessHistory.removeFirst();
		}

		// Only check stopping once we have a full window
		if (fitnessHistory.size() == ROLLING_WINDOW) {
		    double firstAvg = 0.0;
		    double lastAvg = 0.0;
		    int half = ROLLING_WINDOW / 2;

		    // Average over first half of window
		    for (int i = 0; i < half; i++) {
		        firstAvg += fitnessHistory.get(i);
		    }
		    firstAvg /= half;

		    // Average over last half of window
		    for (int i = half; i < ROLLING_WINDOW; i++) {
		        lastAvg += fitnessHistory.get(i);
		    }
		    lastAvg /= half;

		    // Compare averages
		    if ((lastAvg - firstAvg) < FITNESS_THRESHOLD) {
		    	System.out.println("##############################################");
		    	System.out.println("Rolling difference: " + (lastAvg-firstAvg));
		        throw new RuntimeException("Early stopping: fitness plateau detected at generation " + currentGeneration);
		    }
		}


	}


	public int getMaxFitnessValue() {
		return (int)maxFitnessValue;
	}
	
	
	public void setActivatorTranscriber(ActivatorTranscriber activatorTranscriber){
		this.activatorTranscriber = activatorTranscriber;
	}

}
