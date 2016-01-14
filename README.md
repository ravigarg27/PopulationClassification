# Population Classification 

# The project currently has three classes.
PopulationClassifierBase - To train the models, save the model files and see the cross validation score.
HelperMethods - All the methods required for the whole pipeline using Mallet 
PopulationClassifierTest - To test the models on test data.

# File Format
Input train file is uptodateData.txt. The file has tab seperated values with columns Id, Sentence, Target. First line should have these column names and will be ignored while reading. Test file must also be in same format.

The Naive Bayes and MaxEnt models are already provided in the Models folder for the current input data of 4489 sentences (Class 0 (No population) - 3045, Class 1(has population) - 1444 sentences).

The testoutput will have labels with their weights in decreasing order i.e. best first
# To run
Import the project in eclipse or any editor. Update the input training data filename and testdata filename. Run the PopulationClassifierBase to first get the models and then run PopulationClassificationTest to get labels for the testdata.
