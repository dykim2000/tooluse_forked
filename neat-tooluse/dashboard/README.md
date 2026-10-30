# A dashboard utility that accompanies the Java tool use application. 
Authors: Dr. Yoonsuck Choe, Kiyrah Mowry
Texas A&M Computer Science Department

## buildDashboard
### Description
Builds and displays a "Dash" dashboard that provides a visual representation for various parameters involved in the most-recent training cycle of the network.

### Dependencies
pandas, plotly, dash

### Run
  python3 buildDashboard.py [dashboard_type]

  dashboard_type:
  1 - Fitness graphs
  2 - Topology graphs

  Example:
  python3 buildDashboard.py 1

* IMPORTANT: Dashboards are built upon the data collected during model training and analyses. Before running the dashboard program, be sure to train the new model using the Java Driver class to ensure that the dashboard data accurately reflects the current model being examined. 
* Before running dashboard_type 2, the highPerformers Python utility must be run. Instructions can be found for this in "/tools/README" under the highPerformers heading.

### View Topology Dashboard
Before the topology dashboard can be viewed, a few steps must be completed to ensure all relevant information has been generated for the current training cycle.
1. Run model training using "Driver.java". See "/neat-tooluse/README.md" for instructions. 
2. Copy the top-performing chromosomes using "/tools/highPerformers.py"
3. Rename the "/db/max_chromosomes" folder to "no_stick", "one_stick", or "two_sticks" based on the stick state chosen before training.
  * Note: The folder names must be exact in order for "/dashboard/buildDashboard.py" to search through them properly.
4. Inside the new folder, create a sub-folder called "logs".
5. Copy the CSV files from "/dashboard/csv" to "/db/folder_name"
  * chromosome_fitness.csv
  * generation_fitness.csv
  * model_parameters.csv
6. Repeat steps 1-5 to train for the different stick states.
7. In console/terminal, navigate to the "tools" folder. 
8. Run "python3 chromosomeTopology.py 3 0". This will search through the 3 folders, calculate the topology measures, and generate the "/dashboard/csv/topology.csv" file. 
9. In console/terminal, navigate to the "dashboard" folder. 
10. Run "python3 buildDashboard.py 2" to start the topology dashboard.

### Visualizations 
- List of relevant user-defined hyperparameters such as: population size, max timesteps, number of trials, number of generations, and number of sticks used (0, 1, 2)
- Maximum and average fitness of the population of chromosomes each generation. 
- Individual fitness of each chromosome per generation.
- The average percentage of successful target reaches from all chromosomes per generation. 
- The percentage of successful target reaches for the highest-performing chromosome of each generation.
- Comparison graphs between the 3 stick states (0 sticks, 1 stick, 2 sticks). It compares metrics such as network density, number of hidden neurons, number of connections, average percentage of hubs, average hub density, etc.

### Notes
- All dashboard components are built upon the custom CSVs that are recorded by both the Java and Python utilities. Any changes to the structure of the CSV including column names, csv names, etc, will result in the buildDashboard code needing to be updated to accommodate the changes.



  
