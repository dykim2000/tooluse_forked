# A toolbox of Python scripts developed to assist in the analysis and visualization of the training conducted in the main Java tool use program.
Authors: Dr. Yoonsuck Choe, Kiyrah Mowry
Texas A&M Computer Science Department

## Anji2dot
### Description
Simple script to plot the network topology.

### Dependencies
graphviz 

### Run
  pip install graphviz 

  python3 anji2dot.py [chromosome_id]

  Example:
  python3 anji2dot.py 11687

### Notes
- The program contains a hardcoded path to the location of the chromosome XML files in the Java application. The naming notation of "chromosome12345.xml" is automatically appended to reduce required typing per run.


## highPerformers

### Description
A utility to isolate and analyze the highest-performing chromosomes from the current training run.
The program moves the [n] user-defined highest-fitness chromosomes from the primary chromosome folder into "/db/max_chromosomes". The id and fitness of the first top-performing chromosome is output to the terminal. Optional code is available commented-out at the bottom of the program that will automatically run the anji2dot visualization of the top-performer. Once the chromosomes have been copied to the max_chromosomes folder, the program analyzes basic topology of each chromosome and saves the data into "/dashboard/topology.csv".

### Dependencies
Pandas, Numpy

### Run
  python3 highPerformers.py

  * Note: this program can only be run after training is completed with dashboardLogger turned on. This populates the chromosomes files and dashboard CSV files that are necessary for this utility to run properly.

## chromosomeTopology

### Description
A utility to analyze, view, and record various topology measures for chromosomes in the network. 


### Run 
  python3 chromosomeTopology.py analysis-type chromosome-id

  Example run command: "python3 chromosomeTopology.py 2 11687" or "python3 chromosomeTopology.py 3 0"

  - analysis-type
    * 1 : Single chromosome adjacency-matrix heatmap visualization (with and without Leiden module reorganization).
    * 2 : Single chromosome degree distribution histogram (defaults to showing 3 histograms: in-degree, out-degree, and both)
    * 3 : Multiple chromosome topology saving. This moves through 3 stick-state saved folders in the "/db/" directory, calculates topology measures, and saves the results in "/dashboard/csv/topology.csv".
      * Note: if option 3 is chosen, then the chromosome-id parameter doesn't matter, so it can be set to 0.

  - chromosome-id
    * The unique identifier for a single chromosome that is in "/db/chromosome/chromosome_____.xml"
    * Example: 11687
  
