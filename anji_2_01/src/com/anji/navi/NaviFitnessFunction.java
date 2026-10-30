/*
 * Copyright (C) 2009 Yoonsuck Choe
 * 
 * This file is an add-on of ANJI (Another NEAT Java Implementation).
 * This add-on has the same terms and conditions as ANJI (see below).
 * This is a modified version of DoublePoleBalanceFitnessFunction.java
 * by Derek James.
 * 
 * Tue Nov  3 17:08:37 CST 2009
 *
 * --- original statement in DoublePoleBalanceFitnessFunction.java ---
 *
 * This file is a part of ANJI (Another NEAT Java Implementation).
 * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * created by Derek James on Jul 5, 2005
 */

package com.anji.navi;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.nio.DoubleBuffer;

import org.apache.log4j.Logger;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.imaging.IdentifyImageFitnessFunction;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Arrays;
import com.anji.util.Configurable;
import com.anji.util.Properties;
import com.anji.util.Randomizer;
import com.anji.navi.NaviDisplay;

/**
 * This is written from scratch by Yoonsuck Choe.
 * @author Yoonsuck Choe
 */
public class NaviFitnessFunction implements BulkFitnessFunction, Configurable {

private final static String ARENA_X_KEY = "navi.arena.x";
private final static String ARENA_Y_KEY = "navi.arena.y";
private final static String MAX_STEPS_KEY = "navi.maxsteps";
private final static String NUM_TRIALS_KEY = "navi.numtrials";

// Some useful physical model constants.
private static final double DUMMY = -9.8;

// private PoleBalanceDisplay display = null;

private static final double DEFAULT_ARENA_X = 40;
private double arena_x = DEFAULT_ARENA_X;

private static final double DEFAULT_ARENA_Y = 40;
private double arena_y = DEFAULT_ARENA_Y;

private final static int DEFAULT_TIMESTEPS = 1000;
private int maxTimesteps = DEFAULT_TIMESTEPS;

private final static int DEFAULT_NUM_TRIALS = 10;
private int numTrials = DEFAULT_NUM_TRIALS;
private int maxFitness = 1000;

private boolean displayFlag = false;

private final static Logger logger = Logger.getLogger( NaviFitnessFunction.class );

private ActivatorTranscriber factory;

private Random rand;

/**
 * @see com.anji.util.Configurable#init(com.anji.util.Properties)
 */
public void init( Properties props ) throws Exception {
	try {

		rand = new Random(1234);

		// 1. Init activator transcriber
		factory = (ActivatorTranscriber) props.singletonObjectProperty( ActivatorTranscriber.class );

		// 2. pull parameters from the properties file
		arena_x = props.getDoubleProperty(ARENA_X_KEY, DEFAULT_ARENA_X);
		arena_y = props.getDoubleProperty(ARENA_Y_KEY, DEFAULT_ARENA_Y);
		maxTimesteps = props.getIntProperty(MAX_STEPS_KEY, DEFAULT_TIMESTEPS );
		numTrials = props.getIntProperty(NUM_TRIALS_KEY, DEFAULT_NUM_TRIALS );
			// * Other types: getBooleanProperty( INPUT_VELOCITY_KEY, true );

		// 3. set up randomizer  
		Randomizer randomizer = (Randomizer) props.singletonObjectProperty( Randomizer.class );
		rand = randomizer.getRand();
	}
	catch ( Exception e ) {
		throw new IllegalArgumentException( "invalid properties: " + e.getClass().toString() + ": "
				+ e.getMessage() );
	}
}

/**
 * @see org.jgap.BulkFitnessFunction#evaluate(java.util.List)
 * @see IdentifyImageFitnessFunction#evaluate(Chromosome)
 */
public void evaluate( List genotypes ) {

	// evaluate each chromosome: this is generic, so no need to touch
	Iterator it = genotypes.iterator();
	while ( it.hasNext() ) {
		Chromosome c = (Chromosome) it.next();
		evaluate( c );
	}
}

public double dist(double x, double y, double x2, double y2) {

  	return Math.sqrt((x-x2)*(x-x2)+(y-y2)*(y-y2));

}

public double rangeCheck(double x) {
	

	if (x<0.0) {return 0.0;}
	else if (x>1.0) {return 1.0;}
	else {return x;}

}

/**
 * Evaluate chromosome and set fitness.
 * @param c
 */
public void evaluate( Chromosome c ) {
	try {
		Activator activator = factory.newActivator( c );

		// calculate fitness, sum of multiple trials
		double fitness = 0.0;

		double[] input = new double[5];

		double[] output;
	
		double finalDist = 0.0;

		double[][] result = new double[maxTimesteps][2];

		double start_x, start_y, goal_x, goal_y, cur_x, cur_y;

		double travelDist = 0.0;
		double travelDistSum = 0.0;

		if (displayFlag) {
			numTrials = 1;
		}

		for (int k=0; k<result.length; ++k) {
			result[k][0]=9999.9;
			result[k][1]=9999.9;
		}

		for (int k=0; k<numTrials; ++k) {

		  travelDist = 0.0;

		  start_x = rand.nextDouble();
		  start_y = rand.nextDouble();
		  goal_x = rand.nextDouble();
		  goal_y = rand.nextDouble();
		  cur_x = start_x;
		  cur_y = start_y;

		  while (dist(cur_x,cur_y,goal_x,goal_y)<0.3) {
			goal_x = rand.nextDouble();
			goal_y = rand.nextDouble();
	 	  }

		  input[0] = cur_x;
		  input[1] = cur_y;
		  input[2] = goal_x;
		  input[3] = goal_y;
		  input[4] = 1.0;

		  if (displayFlag) {
		  	System.out.println("RTVprob: "+ start_x+" "+start_y+" "+goal_x+" "+goal_y+" ");
		  	System.out.println("RTVtraj: "+ start_x+" "+start_y);
		  }
		  for ( int i = 0; i < maxTimesteps; i++ ) {
			result[i][0] = cur_x;
			result[i][1] = cur_y;
			output = activator.next( input );
			//System.out.println("RTVbefore: "+cur_x+" "+cur_y);
			//System.out.println("RTVout   : "+output[0]+" "+output[1]);
			cur_x = cur_x + output[0]/50.0;
			cur_y = cur_y + output[1]/50.0;
			travelDist += dist(0,0,output[0]/50.0,output[1]/50.0);
			//System.out.println("RTVafter: "+cur_x+" "+cur_y);
			cur_x = rangeCheck(cur_x);
			cur_y = rangeCheck(cur_y);
			//System.out.println("RTVrcheck:"+cur_x+" "+cur_y);
		  	input[0] = cur_x;
		  	input[1] = cur_y;
		  	input[2] = goal_x;
		  	input[3] = goal_y;
		  	input[4] = 1.0;

		  	if (displayFlag) {
		  		System.out.println("RTVtraj: "+ cur_x+" "+cur_y);
		  	}

			//System.out.println("RTVdist: "+dist(cur_x,cur_y,goal_x,goal_y));
			if (dist(cur_x,cur_y,goal_x,goal_y)<0.01){
				break;
			}
		  }

		  finalDist += dist(cur_x,cur_y,goal_x,goal_y);
		  travelDistSum += travelDist;

		  if (displayFlag) {
			NaviDisplay navi = new NaviDisplay(500,500,
				start_x,start_y,goal_x,goal_y,result);
			;
		  }
		
		}

		if (finalDist<1.0) {
		  System.out.println("RTVdist = "+finalDist);
		}

		//fitness = (double)numTrials-finalDist;

		double distFactor = (finalDist/(double)numTrials)/Math.sqrt(2.0);
		double travelFactor = (travelDistSum/numTrials)/
				      (maxTimesteps 
					* dist(0.0,0.0,1.0/50.0,1.0/50.0)); 
		/* 09dec01-v1
		fitness = ((1.0-distFactor)+(1.0-travelFactor)/3.0)/1.3;
		//fitness = (1.0-distFactor);
		*/

		/* 09dec01-v2 */
		fitness = (1.0-distFactor)*(1.0-travelFactor);

		//System.out.println("finaldist/numtrial = "+finalDist/numTrials+" ..."+"travelDistsum/numtrial = "+travelDistSum/numTrials+"/"+maxTimesteps*dist(0.0,0.0,1.0/50.0,1.0/50.0)+"... distfactor = "+distFactor+"....travelFactor="+travelFactor);
		// System.out.println("d = "+(1-distFactor)+"....t = "+(1-travelFactor)+"....f = "+fitness);
		System.out.print(".");

		int key = rand.nextInt();


		if (displayFlag || finalDist<0.5) {
			for (int k=0; k<maxTimesteps; ++k) {
			System.out.println("RTV"+key+": "+result[k][0]+" "+result[k][1]);
			}
		}
		c.setFitnessValue((int)(fitness*maxFitness));
	}
	catch ( Throwable e ) {
		logger.warn( "error evaluating chromosome " + c.toString(), e );
		c.setFitnessValue( 0 );
	}
}

/**
 * @see org.jgap.BulkFitnessFunction#getMaxFitnessValue()
 */
public int getMaxFitnessValue() {
	// YC: this function tells the main program what is the maximum
	// fitness attainable.
	return (maxFitness);
}

/**
 * Enables display (dummy, for now)
 */
public void enableDisplay() {
        // display = new PoleBalanceDisplay( trackLength, new double[] { poleLength1, poleLength2 },
          //              maxTimesteps );
        // display.setVisible( true );
	displayFlag = true;
}


} // end of class



////////////////////////////////////////////////////////////////////////////
// scrap code
////////////////////////////////////////////////////////////////////////////


/**
 * singleTrial
 * @param activator
 *
private int singleTrial( Activator activator ) {

	int fitness = 0;

	DoubleBuffer oscillBuffer = DoubleBuffer.allocate( 10000 );

	// logger.debug( "state = " + Arrays.toString( state ) );

	// Run the pole-balancing simulation.
	int currentTimestep = 0;
	for ( currentTimestep = 0; currentTimestep < maxTimesteps; currentTimestep++ ) {
		// Network activation values
		double[] networkInput;

		// Store the accumulated state variables for cart and pole 1 within the oscillation buffer.
		oscillBuffer.put(Math.abs(state[0]) + Math.abs(state[1]) + 
								Math.abs(state[2]) + Math.abs(state[3]));
		
		// Activate the network.
		double networkOutput = activator.next( networkInput )[ 0 ];
		energyUsed += networkOutput;
		// performAction( networkOutput, state );

		if ( currentTimestep%1000==0 )
		{
			if ( currentTimestep > 99 )
			{

				if(f2>0.0)
					f2 = 0.75 / f2;
			}
			fitness += 0.1 + 0.9*f2;
		}
	}

	logger.debug( "trial took " + currentTimestep + " steps" );
	return fitness;
}
*/

