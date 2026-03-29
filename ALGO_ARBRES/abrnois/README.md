# ABRnois Project

## Overview
The ABRnois project implements a data structure that combines the properties of binary search trees (BST) and tournament trees. This structure allows for efficient searching and prioritization of elements, specifically designed to manage a list of frequently used French words.

## Structure
The project consists of the following files:
- **abrnois.c**: Contains the implementation of the ABRnois data structure and its associated functions.
- **README.md**: Provides documentation for the project.
- **rapport.pdf**: A report detailing the implementation process and challenges faced.

## Functions Implemented
1. **Noeud * alloue_noeud(char * mot)**: Allocates a new node with an initial occurrence of 1.
2. **int exporte_arbre(char * nom_pdf, ABRnois A)**: Creates a graphical representation of the ABRnois tree in a PDF file.
3. **void rotation_gauche(ABRnois * A)**: Performs a left rotation on the tree.
4. **void rotation_droite(ABRnois * A)**: Performs a right rotation on the tree.
5. **int insert_ABRnois(ABRnois * A, char * mot)**: Inserts a new node or updates the occurrence of an existing word.
6. **int extrait_priorite_max(ABRnois * A, Liste * lst)**: Extracts nodes with the maximum occurrence and returns them in a sorted list.

## Compilation and Execution
To compile the project, use the following command:
```
gcc -o abrnois abrnois.c
```

To run the program, execute:
```
./abrnois frequents.txt corpus_1.txt corpus_2.txt ...
```

## Usage
The program reads words from the specified corpus files, inserts them into the ABRnois structure, and extracts the most frequent words into the specified output file. Options for generating PDF representations of the tree at various stages are also available.

## Conclusion
This project demonstrates the implementation of a hybrid data structure that efficiently manages and prioritizes word occurrences, providing a valuable tool for linguistic analysis.