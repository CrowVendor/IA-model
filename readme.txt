To run from command line:
javac wordModeling.java

There are three settings for running, to model average response times,
response times for sequences, and individual word activations:

java wordModeling sequence output_file
java wordModeling response language_int
java wordModeling word inputWord num_cycles num_results output_file

Currently the models are set up to run on the lexicons totalCombined.txt for
the IA model, and combined_possibles and combined_dutch_possibles for the BIA.
The test words are stored in the input file. Language 1 is English, and
language 2 is Dutch. 
