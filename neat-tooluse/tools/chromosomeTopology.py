import sys
import os
import shutil
import subprocess
import pandas as pd
import numpy as np
import xml.etree.ElementTree as ET
import plotly.express as px
import csv
import bct
from scipy.stats import norm
from scipy.stats import gaussian_kde
import matplotlib.pyplot as plt
import igraph as ig
import leidenalg as la
from collections import Counter

n = 10 # Number of top chromosomes to isolate

##### Directories and File Names #############
chromosome_dir = "../db/chromosome"
no_stick_dir = "../db/no_stick"
one_stick_dir = "../db/one_stick"
two_sticks_dir = "../db/two_sticks"
csv_dir = "../dashboard/csv"
topology_csv = os.path.join(csv_dir, "topology.csv")

def xmlToAdjMat(xml_path,weighted=False):
    '''
    Converts a chromosome XML file into an adjacency matrix.

    Params
    ---------
    xml_path : The path of the chromosome xml to be converted.
    weight : True for the adjacency matrix to contain original weights. False for connections to be only 0,1.
    Returns
    ---------
    A numpy adjacency matrix for the network
    '''

    tree = ET.parse(xml_path)
    root = tree.getroot()
    neurons = root.findall(".//neuron")
    neuron_ids = [int(n.attrib["id"]) for n in neurons]
    neuron_types = [n.attrib.get("type", "unknown") for n in neurons]

    # Map original IDs to sequential indices
    id_to_index = {nid: idx for idx, nid in enumerate(neuron_ids)}
    num_neurons = len(neuron_ids)

    # hid_neurons = [n for n in neurons if n.attrib.get("type") == "hid"]
    connections = root.findall(".//connection")

    adj_matrix = np.zeros((num_neurons, num_neurons))
    for c in connections:
        src = int(c.attrib["src-id"])
        dest = int(c.attrib["dest-id"])
        weight = float(c.attrib["weight"])
        val = 0
        if weighted:
            val = weight
        else: 
            val = 1

        adj_matrix[id_to_index[src], id_to_index[dest]] = val

    np.set_printoptions(threshold=np.inf, precision=0, linewidth=np.inf)
    # print(adj_matrix)
    return adj_matrix, neuron_types, neuron_ids

def adj_heatmap(xml_path, ordered=False, graphics=True, console=True):
    '''
    Displays a visual heatmap for the adjacency matrix. 
    Positive weights are in the warm spectrum, negative weights are cool, and black is weight 0.

    Params
    ---------
    xml_path : The path of the the XML file
    ordered : False = visualization is displayed without any reordering. True = Visualization is reordered according to louvain communities.
              * Note: For certain adjacency matrices, the louvain algorithm is unable to converge, so it throws an error. 
    graphics : False = do not display the heatmap graphic, True = display the heatmap graphic
    console : False = do not display any console print statements, True = display print statements

    Return
    --------
    hub_percent : the number of hub nodes as a percentage of the total nodes
    avg_density : the average density of all hub nodes
    '''
    
    if(ordered):
        '''
        adj_orig,neuron_types = xmlToAdjMat(xml_path, False)
        adj_weighted,neuron_types2 = xmlToAdjMat(xml_path, True)

        
        # Louvain Modularity Method #################
        # Notes: ways that successfully run: 
        # non-weighted, undirected
        # weighted, undirected, potentially removing negatives
        #       This makes big modules that seem to have a high number of positive weights
        
        print("Finding modularity....")
        adj = np.array(adj_weighted, dtype=float) # Ensure that adj is a proper numpy array and not a list of lists
        np.fill_diagonal(adj, 0)
        # adj_und = np.maximum(adj, adj.T)
        adj_und = (adj + adj.T) / 2
        ci,q = bct.community_louvain(adj_und, B="negative_asym")
        print("ci: ", ci)
        print("q: ", q)
        order = np.argsort(ci)
        adj_reordered = adj_weighted[order][:, order]
        types_reordered = [neuron_types[i] for i in order]
        membership_reordered = ci[order]

        
        # Participation coefficient
        # Pc = bct.participation_coef(adj, ci)
        # print("Participation Coefficient: ", Pc)

        # Within-module degree z-score
        # Z = bct.module_degree_zscore(adj, ci)
        # print("Within-module degree score: ", Z)
        

        fig_adj = adj_reordered
        fig_neuron_types = types_reordered
        '''

        
        # Leiden Method #############################
        adj_orig, neuron_types, neuron_ids = xmlToAdjMat(xml_path, True)
        g = ig.Graph.Weighted_Adjacency(adj_orig.tolist(), mode="DIRECTED", attr="weight")
        partition = la.find_partition(g, la.CPMVertexPartition, weights="weight", resolution_parameter=0.1)
        membership = np.array(partition.membership)
        counts = Counter(membership)
        small_modules = [m for m, c in counts.items() if c == 1]

        for m in small_modules:
            node = np.where(membership == m)[0][0]
            # assign to module of strongest neighbor
            strongest_neighbor = np.argmax(adj_orig[node])
            membership[node] = membership[strongest_neighbor]


        order = np.argsort(membership)
        adj_reordered = adj_orig[order][:, order]
        types_reordered = [neuron_types[i] for i in order]
        ids_reordered = [neuron_ids[i] for i in order]
        membership_reordered = membership[order]

        # membership is your array of community assignments
        modules, counts = np.unique(membership, return_counts=True)

        # if(console):
        #   print("Density: ", bct.density_dir(adj_orig))
        #   print("Constant Potts Model (CPM) score: ", partition.quality())

        # Positive Weights------------------------------------------------------------
        # Keep only the positive weights
        # W_pos = np.copy(adj_orig)
        # W_pos[W_pos < 0] = 0  

        # Keep all weights, but use absolute value to turn negative weights positive. 
        W_pos = np.abs(adj_orig)

        pos_reordered = W_pos[order][:,order] # Order the positive matrix to match the ordering of the modules. This is only for the heatmap graphic, not calculation.

        Pc_pos = bct.participation_coef(W_pos, membership)
        Z_pos  = bct.module_degree_zscore(W_pos, membership)

        if(console):
            print("Number of modules:", len(modules))
            print("Number of nodes in each module:")
            for m, c in zip(modules, counts):
                print(f"Module {m}: {c} nodes")

            np.set_printoptions(precision=3, suppress=False)
            for i, (nid, pc, z, m) in enumerate(zip(ids_reordered, Pc_pos, Z_pos, membership)):
                print(f"{nid:8}  Pc={pc:8.4f}  Z={z:8.4f}, Module={m:8f}")

        #print("Participation Coefficient (positive):", Pc_pos)
        #print("Within-module degree z-score (positive):", Z_pos)
        #print("Original membership: ", membership)

        #deg_pos = np.sum(W_pos > 0, axis=1)
        #print("Degree-positive: ", deg_pos)

        valid_modules = set(modules[counts > 1]) # Filter to only look for hubs in modules that have more than 1 node

        # Identify hubs
        hub_indices = np.where(Z_pos >= 1.0)[0]  # hubs based on z-score
        hub_ids = []
        hub_densities = []
        for idx in hub_indices:
            module_id = membership[idx]
            if module_id in valid_modules:
                neuron_id = neuron_ids[idx]
                node_strength = np.sum(W_pos[idx, :]) + np.sum(W_pos[:, idx])
                node_density = node_strength / (W_pos.shape[0] - 1)

                P = Pc_pos[idx]
                if P <= 0.30:
                    hub_type = "Provincial hub"
                elif P <= 0.75:
                    hub_type = "Connector hub"
                else:
                    hub_type = "Kinless hub"

        
                
                if(console):
                    print(f"Neuron {neuron_id}, Module={str(int(module_id))}, "
                        f"Z={Z_pos[idx]:.3f}, Pc={P:.3f}, "
                        f"Density={node_density:.3f}, Type={hub_type}")
                
                hub_ids.append(neuron_id)
                hub_densities.append(node_density)

        hub_percent = len(hub_ids)/len(neuron_ids)
        avg_density = np.mean(hub_densities)

        if(console):
            print("Filtered hub IDs:", hub_ids)
            print(f"Hubs: {len(hub_ids)}, Neurons: {len(neuron_ids)}, Percentage: {hub_percent:.3f}, Average hub density: {avg_density:.3f}")

        '''
        # Negative Weights------------------------------------------------------------
        # W_neg = np.copy(adj_orig)
        # W_neg[W_neg > 0] = 0   # keep only positive weights
        neg_reordered = W_neg[order][:,order]

        Pc_neg = bct.participation_coef(W_neg, membership)
        Z_neg  = bct.module_degree_zscore(W_neg, membership)

        hub_indices = np.where(Z_neg > 1.0)[0] # Get of indices of nodes that qualify as a hub
        hub_ids = [ids_reordered[i] for i in hub_indices]

        print("Participation Coefficient (negative):", Pc_neg)
        print("Within-module degree z-score (negative):", Z_neg)

        #deg_pos = np.sum(W_pos > 0, axis=1)
        #print("Degree-positive: ", deg_pos)

        valid_modules = set(modules[counts > 1]) # Filter to only look for hubs in modules that have more than 1 node

        # Identify hubs
        hub_indices = np.where(Z_neg > 1.0)[0]  # hubs based on z-score
        hub_ids = []
        for idx in hub_indices:
            module_id = membership[idx]
            if module_id in valid_modules:
                neuron_id = neuron_ids[idx]
                node_strength = np.sum(W_neg[idx, :]) + np.sum(W_neg[:, idx])
                node_density = node_strength / (W_neg.shape[0] - 1)
                
                print(f"Neuron {neuron_id}, Module={module_id}, "
                    f"Z={Z_pos[idx]:.3f}, Pc={Pc_pos[idx]:.3f}, "
                    f"Density={node_density:.3f}")
                
                hub_ids.append(neuron_id)

        print("Filtered hub IDs:", hub_ids)
        '''

        # Participation coefficient
        # Pc = bct.participation_coef(adj_orig, membership)
        # print("Participation Coefficient: ", Pc)

        # Within-module degree z-score
        # Z = bct.module_degree_zscore(adj_orig, membership)
        # print("Within-module degree score: ", Z)

        fig_adj = adj_reordered
        fig_neuron_types = ids_reordered#types_reordered
        
    else: # Unordered heatmap
        adj,neuron_types,neuron_ids = xmlToAdjMat(xml_path, True)
        # adj_und = (adj + adj.T) / 2
        fig_adj = adj
        fig_neuron_types = neuron_types

    if(graphics):
        colorscale = [
            [0.0, 'blue'],     # min value (negative)
            [0.49, 'cyan'],    # just below zero
            [0.5, 'black'],    # zero
            [0.51, 'yellow'],  # just above zero
            [1.0, 'red']       # max value (positive)
        ]
        max_abs = np.max(np.abs(fig_adj))
        fig = px.imshow(
            fig_adj,
            color_continuous_scale=colorscale,  # or "Blues", "Reds", "Hot"
            origin="upper",  # keeps (0,0) at top left
            zmin=-max_abs,
            zmax=max_abs
        )
        fig.update_layout(
            xaxis=dict(
                tickmode="array",
                tickvals=list(range(len(fig_neuron_types))),
                ticktext=fig_neuron_types
            ),
            yaxis=dict(
                tickmode="array",
                tickvals=list(range(len(fig_neuron_types))),
                ticktext=fig_neuron_types
            )
        )
        # fig.update_traces(showscale=True, colorscale="Viridis")
        fig.update_xaxes(showgrid=True)
        fig.update_yaxes(showgrid=True)

        if(ordered):
            # membership_reordered: 1D numpy array aligned to adj_reordered
            labels, counts = np.unique(membership_reordered, return_counts=True)

            start = 0
            for lbl, cnt in zip(labels, counts):
                s = start
                e = start + cnt - 1
                start += cnt

                # skip tiny singletons if you prefer
                if cnt <= 1:
                    continue

                fig.add_shape(
                    type="rect",
                    x0=s - 0.5, x1=e + 0.5,
                    y0=s - 0.5, y1=e + 0.5,
                    xref='x', yref='y',
                    line=dict(color="white", width=2),
                    fillcolor="rgba(0,0,0,0)",  # transparent fill
                    layer='above'
                )
            
            
        fig.show()

    if(ordered): return hub_percent, avg_density, hub_ids
    else: return -1, -1, -1 # Return values do not apply without ordering

def adj_hist(xml_path,degree_type="both"):
    '''
    Displays a histogram showing the frequency of degrees for an adjacency matrix.

    Params
    ---------
    adj : The weighted, directed adjacency matrix for one chromosome.
    degree_type: Options-"in", "out", "both". This determines whether the degrees viewed are in-degrees, out-degrees, or a sum of both in and out.
    '''
    adj, neuron_types = xmlToAdjMat(xml_path, True)
    degrees_all = bct.degrees_dir(adj)
    if(degree_type == "in"):
        degrees = degrees_all[0]
        title = "Distribution of Node in-degree"
    elif(degree_type == "out"):
        degrees = degrees_all[1]
        title = "Distribution of Node out-degree"
    else:
        degrees = degrees_all[2]
        title = "Distribution of Node degree (in + out)"
    

    # --- Histogram ---
    count, bins, ignored = plt.hist(degrees, bins='auto', density=False, alpha=0.6, color='g', edgecolor='black')

    # --- Fit a normal distribution (mean, std) ---
    mu, sigma = norm.fit(degrees)

    # Generate x values for the fitted curve
    x = np.linspace(min(degrees), max(degrees), 100)
    kde = gaussian_kde(degrees)


    bin_width = bins[1] - bins[0]
    pdf_scaled = kde(x) * len(degrees) * bin_width

    # --- Overlay the fitting curve ---
    plt.plot(x, pdf_scaled, 'r', linewidth=2)

    # Labels
    plt.title(title)
    plt.xlabel("Degree")
    plt.ylabel("Frequency")
    plt.show()

def main_single(analysis_type, chromosome_id):
    file = f"chromosome{int(chromosome_id)}.xml"
    xml_path = os.path.join(chromosome_dir, file)
    
    # Adjacency Matrix Heatmap ####################
    if(analysis_type == "1"):
        adj_heatmap(xml_path, False, True)
        adj_heatmap(xml_path, True, True, True)
    ###############################################

    if(analysis_type == "2"):
        adj_hist(xml_path)
        adj_hist(xml_path, "in")
        adj_hist(xml_path, "out")

def analyze_chromosomes(stick_type):
    if(stick_type == "NO_STICK"):
        folder_path = no_stick_dir
    elif(stick_type == "ONE_STICK"):
        folder_path = one_stick_dir
    elif(stick_type == "TWO_STICKS"):
        folder_path = two_sticks_dir

    generation_csv_dir = os.path.join(folder_path, "logs/generation_fitness.csv")
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

    # Iterate through each chromosome in the requested folder 
    for row in top_n.itertuples():
        file = f"chromosome{int(row.max_chromosome_id)}.xml"
        chromosome = os.path.join(folder_path, file)
        if os.path.exists(chromosome):
            # Parse XML file and save information to a dashboard CSV
            tree = ET.parse(chromosome)
            root = tree.getroot()
            neurons = root.findall("neuron")
            neuron_ids = [int(n.attrib["id"]) for n in neurons]

            # Map original IDs to sequential indices
            id_to_index = {nid: idx for idx, nid in enumerate(neuron_ids)}
            num_neurons = len(neuron_ids)

            hid_neurons = [n for n in neurons if n.attrib.get("type") == "hid"]
            connections = root.findall("connection")

            # print("Chromosome ID:", root.attrib.get("id"))
            # print("Hidden neurons: ", len(hid_neurons))
            # print("Number of connections: ", len(connections))

            adj,neuron_types, neuron_ids = xmlToAdjMat(chromosome, True)
            density = bct.density_dir(adj)

            hub_percent, hub_avg_density, hubs = adj_heatmap(chromosome, True, False, False)

            with open(topology_csv, mode="a", newline="") as f:
                writer = csv.writer(f)
                writer.writerow([stick_type, root.attrib.get("id"), row.max_fitness, row.percent_reaches_max_chromosome, len(hid_neurons), len(connections), density[0], hub_percent, hub_avg_density, hubs])
            
        else:
            print(f"Warning: {chromosome} not found, skipping.")

    # Parse the xml to an adjacency matrix
    # Calculate # hidden neurons
    # Save a line to the csv
    

def main_multiple():
    # Start the topology csv and write the header
    ##### Set up Topology CSV #####
    os.makedirs(csv_dir, exist_ok=True)
    
    # Clear the current file and rewrite the header row
    with open(topology_csv, mode="w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["stick_type","chromosome_id", "fitness", "percent_reaches", "hid_neurons","connections","density", "hub_percent", "hub_avg_density", "hubs"])

    analyze_chromosomes("NO_STICK")
    analyze_chromosomes("ONE_STICK")
    analyze_chromosomes("TWO_STICKS")

    print("All chromosomes have been successfully analyzed.")
    print("Results are updated in /dashboard/topology.csv")

# PROGRAM ENTRY POINT ----------------------------------------------
if len(sys.argv) != 3:
    print("usage: chromosomeTopology.py <analysis_type> <chromosome_id>")
    print("Analysis types: 1 = single chromosome (heatmap), " \
                        "2 = single chromosome (degree histogram)," \
                        "3 = multiple chromosomes")
    exit()
else:
    if(sys.argv[1] == "1" or sys.argv[1] == "2"): # Single chromosome analysis
        main_single(sys.argv[1], sys.argv[2])
    elif(sys.argv[1] == "3"): # Multiple chromosome analysis
        main_multiple()
    else: # Invalid analysis type
        print("Invalid <analysis_type> provided.")
        print("usage: chromosomeTopology.py <analysis_type> <chromosome_id>")
        print("Analysis types: 1 = single chromosome, 2 = multiple chromosomes")
        exit()