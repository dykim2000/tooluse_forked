# A Java application that trains and evaluates an adaptive network architecture using the NEAT algorithm. The goal of the program is to effectively train the model to reach a target using a two-segment arm and an optional one or two sticks. The sticks can be picked up by the arm to extend the total reach and enable successful reaching of farther targets. 

## Codex Refactor Note
This fork includes a Codex-based refactor focused on easier experiment management and reproducibility.
Key changes include:
* Command-line selection of chromosome ID and properties file for both training and evaluation.
* Run-specific output isolation under `db/<properties-file-prefix>/` (persistence files, chromosome XMLs, dashboard CSVs, and evaluation activation logs).
* Properties-driven stick configuration (`stick.state`) with a two-stick preset in `properties/example_twosticks.properties`.
* Detailed setup and usage guide: `README-codex.md`
* Session change log: `../log.md`

Authors: Dr. Yoonsuck Choe, Kiyrah Mowry
Texas A&M Computer Science Department

## Evaluate a Chromosome
This is the process of watching a single (pre-trained) chromosome attempt to reach the target.

1. Install eclipse IDE 

2. Import the entire neat-tooluse folder in Eclipse

3. Compile and run from command line:
    * `./run.sh` to use the default chromosome ID (`11678`)
    * `./run.sh <chromosome_id>` to evaluate a specific chromosome (for example `./run.sh 12001`)
    * `./run.sh <chromosome_id> <properties_file>` to select both chromosome and properties file (for example `./run.sh 12001 example_twosticks.properties`)

4. If you are using Eclipse instead of `run.sh`, pass program arguments to `EvaluateIt` in this order:
    * `<chromosome_id> <properties_file>`


## Train a new model
1. Update model training parameters (see instructions below)
2. Run training from command line:
    * `./evolve.sh` (uses `example.properties`)
    * `./evolve.sh <properties_file>` (for example `./evolve.sh example_twosticks.properties`)
3. Training/evaluation outputs are saved under:
    * `db/<properties-file-prefix>/`
    * Example: using `example_twosticks.properties` writes to `db/example_twosticks/`

### Update model training parameters
The primary user-defined parameters that are modified for each model training are primarily found in "ExampleFitnessFunction.java" and "/properties/example.properties".

#### ExampleFitnessFunction
- DEFAULT_TIMESTEPS 
  * The maximum number of timesteps completed for each trial. Change this to update how many steps the arm has available before either reaching the target or giving up. Default values: 500 or 1000. 
- DEFAULT_NUM_TRIALS 
  * The maximum number of trials. A trial is defined as a new event where the target and stick(s) are placed either randomly or according to a pre-defined test file, and the arm has a number of timesteps to try to reach the target. Default values: 10
- enableDisplay 
  * A boolean indicating whether the graphical animation window should be shown. Set to true to watch the training in action; set to false to speed up training time.
- testMode 
  * A boolean indicating whether the target should be placed randomly (false) or placed according to "test_set.txt" (true). This is not fully set up for the updated program and may need troubleshooting.
- l1,l2 
  * The length of the first and second segment of the arm, respectively.
- tool1_len, tool2_len 
  * The length of the first and second tool, respectively. Careful consideration should be made before changing this to ensure that the graphs and fitness criteria math still perform as expected. 
- ROLLING_WINDOW 
  * The number of chromosomes that are averaged to control the early stopping criteria. Default is 10.
- stickState 
  * The number of sticks used (0, 1, 2). The options are listed in the StickState enumeration.
- dashboardLog 
  * A boolean indicating whether dashboard CSV information should be recorded during training.
- calculateFitness() 
  * The function that determines what fitness criteria is used for each stick state. Options include D*S, D*S*T1, and D*S*T1*T2. Further options for fitness criteria are available in the previous research papers published by Dr. Choe and his students.

#### example.properties
- stick.state
  * Number of sticks to use in simulation. Supported values are `0`, `1`, `2` (or `NO_STICK`, `ONE_STICK`, `TWO_STICKS`).
- num.generations 
  * The number of generations completed during training. The program ends when all generations have been completed. Each generation consists of training the specified DEFAULT_NUM_TRIALS. Default value: 120
- popul.size 
  * The size of the chromosome population used in the NEAT algorithm. Default value: 100

#### example_twosticks.properties
- A ready-made two-stick configuration (`stick.state=2`) for two-stick simulation.

## View Dashboard of Training Results
See "/dashboard/README.md" for instructions. 
Please note: 
* Dashboards are directly dependent on the data collected during the training process. So it is important to train a new model before viewing its corresponding dashboard graphs. 

## View Dashboard of Topology Results
See "/dashboard/README.md" for instructions.

## View visualization of a Chromosome network
See ReadMe file in "/tools" and see Anji2dot heading for instructions. 


## Code Class Overview
- Arm Reaching
    * Records the current state of the arm, including whether a tool is being held or dropped, constraints on movement angles, constraints on tool pickup, and location of arm, target, and both sticks.

- Display
    * The graphics for the animation. Draws all appropriate graphical components based on the stickState. Handles repainting the scene during animation.

- Driver
    * Initializes ANJI properties, initializes loggers such as DashboardLogger, and starts the training process. This is the main class to run to begin training.

- EvaluateIt
    * Runs a set of trials on a single, pre-trained chromosome. Often used to either watch performance of a certain chromosome graphically or to record validation data on a frozen chromosome after training. 
  
- ExampleFitnessFunction
    * Controls most of the specific details about model training. This includes hyper-parameters, environment variables, fitness functions, and data logging information. The "evaluate(chromosome)" function trainings a single chromosome for a specified number of trials. The "calculateFitness" function is where the math is specified for the fitness function based on the number of sticks. The "evaluate(List genotypes)" function is the main training function that gets called from the Driver class. It iterates through all the chromosomes over all of the specified number of training generations. During each training step, relevant data gets saved by the dashboardLogger into CSV files. This function also controls the early-stopping criteria to terminate training if the model doesn't show improvement over recent generations.

- DashboardLogger 
    * Controls the CSV logging for saving parameters during model training. This does not influence the training process at all, but allows for easier visualization of training data after the chromosomes are trained. Saved files are written under `db/<properties-file-prefix>/dashboard/csv/`. 

- /dashboard/buildDashboard.py
    * Constructs a visual dashboard based on the CSV data recorded by the DashboardLogger. See "/dashboard/README.md" for further information.

- /tools/anji2dot.py
    * Displays a single chromosome as a graphical network. See "/tools/README.md" for further information.

- /tools/highPerformers.py
    * Isolates the top n highest-performing (aka highest-fitness) chromosomes after model training. This uses the saved DashboardLogger CSV data to find the Id's of the high-performing chromosomes and then copies the files from "/db/chromosome" to "/db/max_chromosome". Use this utility to save time in finding high performing chromosomes that you want to use to analyze topology of your trained chromosomes. See "/tools/README.md" for further information.

- /tools/bctPlayground.py
    * Not important to the main processes of the program, but can provide a sample area to experiment with new functions from the Brain Connectivity Toolkit Python (BCTPY) before integrating them into the full program.

- /tools/chromosomeTopology.py
    * After training a model and isolating the top performers, this utility helps to calculate, visualize, and save additional information about the structure and topology of chromosomes. It has options such as distribution graphs of node degree for a single chromosome, conversion from a chromosome XML file to its adjacency matrix, heatmap visualization (both with and without modular organization) for the adjacency matrix of a single chromosome, and automatic iteration through 3 stick-state saved folders to analyze all chromosomes for properties (such as hub ids, hub density, network density, number of hidden nodes, number of connections, etc) and saving this information into a new "/dashboard/csv/topology.csv". See "/tools/README.md" for further information.

## Model Architecture 
The model architecture always begins with 10 input parameters, representing the physical state of the task at any given point in the time, and 2 output parameters, representing the proximity to the goal target. As the model trains and evolves over generations, new connections, weights, and hidden neurons are developed according to the NEAT algorithm.

Input mapping: 
  - the input neurons in the chromosome have the following order:

    * input[0] = arm.theta1/90;		// arm angles
    * input[1] = arm.theta2/90; 
    * input[2] = r_d/260;			// distance to object
    * input[3] = r_degree_theta / 90;	// angle to object	
    * input[4] = r_d_pick/260;		// distance to tool 1 handle
    * input[5] = r_degree_pick/90;	// angle to tool 2 handle
    * input[6] = // distance to tool 2 handle
    * input[7] = // angle to tool 2 handle
    * input[8] = 0;			// arm limiter 1
    * input[9] = 0;			// arm limiter 2

  - output[0], output[1] : the two arm angles
