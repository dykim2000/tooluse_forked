###### bctPlayground ###################
# A experimental place to test bctpy operations before integrating into the full project structure. 
# Author: Kiyrah Mowry
########################################

import numpy as np
import bct

# --- 1. Create a toy weighted adjacency matrix (symmetric) ---
A = np.array([
    [0, 1, 1, 0, 0, 0],
    [1, 0, 1, 0, 0, 0],
    [1, 1, 0, 1, 0, 0],
    [0, 0, 1, 0, 1, 1],
    [0, 0, 0, 1, 0, 1],
    [0, 0, 0, 1, 1, 0]
], dtype=float)

degree = bct.degrees_und(A)
print("Degree: ", degree)

# --- 2. Detect communities (using Louvain) ---
Ci, Q = bct.community_louvain(A)
print("Community assignments:", Ci)
print("Modularity (Q):", Q)

# --- 3. Compute node-level measures ---
# Participation coefficient (how distributed each node’s links are across modules)
P = bct.participation_coef(A, Ci)
print("Participation coefficient:", P)

# Within-module degree z-score (how well connected a node is within its module)
Z = bct.module_degree_zscore(A, Ci)
print("Within-module degree z-score:", Z)
