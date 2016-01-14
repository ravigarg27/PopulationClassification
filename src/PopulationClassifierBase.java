import java.io.IOException;
import java.util.ArrayList;

import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

@SuppressWarnings("rawtypes")
public class PopulationClassifierBase {

	public static void main(String[] args) throws IOException {
		
		//Create pipe for the processing
		InstanceList ilist = HelperMethods.getUnigramInstanceList();
		//InstanceList ilist = HelperMethods.getUniBigramInstanceList();
		
		//read input from the file
		String inputFile = "uptodateData.txt";
		ArrayList<Instance> input = HelperMethods.readInputFromTSVFile(inputFile);
		System.out.println("Input size()="+input.size());
		
		//put through the pipe
		ilist.addThruPipe(input.iterator());
		
		//num of folds for the cross validation
		int numOfFolds = 10;
		
		//Train on  the whole data and save the model
		//Also get the average cross validation accuracy
		for (ClassifierTrainer trainer : HelperMethods.getTrainerList()) {
			
			HelperMethods.trainAndSaveOnWholeData(ilist, trainer);
			
			
			ArrayList<Double> crossValidationResults = HelperMethods.getAverageCrossValidationScore(ilist,numOfFolds, trainer);
			System.out.println("Average "+ numOfFolds + " folds accuracy for model "+trainer.toString()+" is : "+crossValidationResults.get(0));
			System.out.println("Average "+ numOfFolds + " folds precision for model "+trainer.toString()+" is : "+crossValidationResults.get(1));
			System.out.println("Average "+ numOfFolds + " folds recall for model "+trainer.toString()+" is : "+crossValidationResults.get(2));
			System.out.println("Average "+ numOfFolds + " folds f1-score for model "+trainer.toString()+" is : "+crossValidationResults.get(3));

		}

		
		
		
				
	}



}
