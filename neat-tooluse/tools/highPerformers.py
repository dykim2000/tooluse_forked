################################
# This utility isolates the [n] top performing chromosomes
# Each top performing chromosome is copied into /db/max_chromosome
# The highest performer is then displayed visually using anji2dot.py
################################
import os
import shutil
import subprocess
import pandas as pd
import numpy as np
import xml.etree.ElementTree as ET
import csv

n = 10 # Number of top chromosomes to isolate

# Directories
generation_csv_dir = "../dashboard/csv/generation_fitness.csv"
csv_dir = "../dashboard/csv"
topology_csv = os.path.join(csv_dir, "topology.csv")
chromosome_dir = "../db/chromosome"
max_chromosome_dir = "../db/max_chromosome"
anji2dot_script = "anji2dot.py"

def main():
    # Read ../dashboard/csv/generation_fitness.csv to get max_chromosome_id and max_fitness
    df = pd.read_csv(generation_csv_dir)

    # Throw an error if the correct columns don't exist
    if not {"max_chromosome_id", "max_fitness"}.issubset(df.columns):
        raise ValueError("CSV must contain 'max_chromosome_id' and 'max_fitness' columns.")

    # Sort the dataframe by fitness
    df_sorted = df.sort_values(by="max_fitness", ascending=False)

    #Sometimes one chromosome can perform the best in multiple generations, so duplicates must be removed.
    df_unique = df_sorted.drop_duplicates(subset="max_chromosome_id",keep="first")
    
    # Sort by max_fitness to isolate the [n] top performing chromosomes
    top_n = df_unique.head(n)

    # Get the names of the chromosomes by converting the ID to an int and put in format "chromosome_____.xml"
    # top_files = [f"chromosome{int(cid)}.xml" for cid in top_n["max_chromosome_id"]]

    # Copy the [n] top chromosomes XML files from ../db/chromosome to ../db/max_chromosome
    os.makedirs(max_chromosome_dir, exist_ok=True) # Ensure that the max_chromosome directory exists

    # Clear out old files from max_chromosome_dir
    for file in os.listdir(max_chromosome_dir):
        file_path = os.path.join(max_chromosome_dir, file)
        if os.path.isfile(file_path):
            os.remove(file_path)

    for row in top_n.itertuples():
        file = f"chromosome{int(row.max_chromosome_id)}.xml"
        src = os.path.join(chromosome_dir, file)
        dst = os.path.join(max_chromosome_dir, file)
        if os.path.exists(src):
            shutil.copy(src, dst) # Copy the file from the chromosomes dir to max_chromosomes dir
            # Parse XML file and save information to a dashboard CSV
            # analyze_all_chromosomes(dst, row.max_fitness)

        else:
            print(f"Warning: {src} not found, skipping.")
    print(f"Successfully copied the {n} top performing chromosomes.")
 
    # Run anji2dot.py on the 1st top performing chromosome to visualize it
    # Call command: python3 anji2dot.py chromosome____.xml
    print(f"Best Chromosome - ID:{int(top_n['max_chromosome_id'].iloc[0])}, fitness: {top_n['max_fitness'].iloc[0]}")
    best_file = int(top_n['max_chromosome_id'].iloc[0])
    
    # Run visualization on the best performing model
    # subprocess.run(["python3", anji2dot_script, str(best_file)])



if __name__ == "__main__":
    main()