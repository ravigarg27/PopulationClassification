import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntL1Trainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequenceWithBigrams;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.InstanceList.CrossValidationIterator;
import cc.mallet.types.Labeling;

@SuppressWarnings("rawtypes")
public class HelperMethods {




	public static InstanceList getUnigramInstanceList(){

		Pipe instancePipe = new SerialPipes (new Pipe[] {

				new Target2Label (),							  // Target String -> class label
				new CharSequence2TokenSequence (),  // Data String -> TokenSequence
				new TokenSequenceLowercase (),		  // TokenSequence words lowercased 
				new TokenSequenceRemoveStopwords (),// Remove stopwords from sequence //negative effect!
				new TokenSequence2FeatureSequence(),
				new FeatureSequence2FeatureVector(),

				//new PrintInputAndTarget()

		});

		//Create an empty list of training instances
		InstanceList ilist = new InstanceList (instancePipe);


		return ilist;

	}

	public static InstanceList getUniBigramInstanceList(){

		Pipe instancePipe = new SerialPipes (new Pipe[] {

				new Target2Label (),							  // Target String -> class label
				new CharSequence2TokenSequence (),  // Data String -> TokenSequence
				new TokenSequenceLowercase (),		  // TokenSequence words lowercased 
				new TokenSequenceRemoveStopwords (),// Remove stopwords from sequence //negative effect!
				new TokenSequence2FeatureSequenceWithBigrams(),
				new FeatureSequence2FeatureVector(),
				//new PrintInputAndTarget()

		});

		//Create an empty list of training instances
		InstanceList ilist = new InstanceList (instancePipe);


		return ilist;

	}



	public static ArrayList<ClassifierTrainer> getTrainerList() {

		ArrayList<ClassifierTrainer> trainersList = new ArrayList<ClassifierTrainer>();

		ClassifierTrainer<?> maxentTrainer = new MaxEntL1Trainer();
		ClassifierTrainer<?> naiveBayesTrainer = new NaiveBayesTrainer();

		trainersList.add(naiveBayesTrainer);
		trainersList.add(maxentTrainer);

		return trainersList;
	} 


	public static ArrayList<Instance> readInputFromTSVFile(String inputFile) throws IOException {

		ArrayList<Instance> input =	new ArrayList<Instance>();

		BufferedReader br = new BufferedReader(new FileReader(inputFile)); 
		//skip the header
		String line = br.readLine();
		line = br.readLine();

		while(line!=null) {

			String[] splits = line.split("\t");

			String id = splits[0];
			String data = splits[1];
			String label = splits[2];

			input.add(new Instance(data, label, id, data));
			line=br.readLine();
		}

		br.close();

		return input;
	}

	public static ArrayList<Double> getAverageCrossValidationScore(InstanceList ilist, int i, ClassifierTrainer trainer) 
	{

		double crossValidAccSum=0;
		double crossValidPrcSum=0;
		double crossValidRecSum=0;
		double crossValidF1Sum=0;

		int count=0;

		//get gross validation folds
		CrossValidationIterator cvIlists = ilist.crossValidationIterator(i);

		while(cvIlists.hasNext()){

			System.out.println("#############Performing "+count+" iteration###########");

			InstanceList[] ilists = cvIlists.next();	

			System.out.println("The train set size is "+ilists[0].size());
			System.out.println("The test set size is "+ilists[1].size());
			Classifier classifier = trainer.train (ilists[0]);
			System.out.println ("The training accuracy is "+ classifier.getAccuracy (ilists[0]));
			System.out.println ("The testing accuracy is "+ classifier.getAccuracy (ilists[1]));					
			System.out.println("The testing precision is "+classifier.getPrecision(ilists[1], 1));
			System.out.println("The testing recall is "+classifier.getRecall(ilists[1], 1));
			System.out.println("The testing f1score is "+classifier.getF1(ilists[1], 1));

			crossValidAccSum+=classifier.getAccuracy(ilists[1]);
			crossValidPrcSum+=classifier.getPrecision(ilists[1], 1);
			crossValidRecSum+=classifier.getRecall(ilists[1], 1);
			crossValidF1Sum+=classifier.getF1(ilists[1], 1);
			count++;


			//additional calculations
			ArrayList<Classification> outClassifications = classifier.classify(ilists[1]);
			int p1l1=0;
			int p1l0=0;
			int p0l1=0;
			int p0l0=0;
			int countCorrect=0;
			int countIncorrect=0;

			System.out.println("Outclassification size "+outClassifications.size());
			for(int k=0; k<outClassifications.size(); k++){

				//System.out.println("Data "+outClassifications.get(k).getInstance().getName());
				//System.out.println("Labeling "+outClassifications.get(k).getLabeling()); uncomment to get score
				double predictedLabel = outClassifications.get(k).getLabeling().getBestIndex();
				//System.out.println("Predicted label "+ predictedLabel);
				double targetLabel = Double.valueOf(outClassifications.get(k).getInstance().getTarget().toString());
				//System.out.println("Target "+ targetLabel);
				boolean bestlabelIsCorrect  = outClassifications.get(k).bestLabelIsCorrect();
				//System.out.println("Prediction "+bestlabelIsCorrect);


				if(bestlabelIsCorrect)
					countCorrect++;
				else
					countIncorrect++;

				if ((predictedLabel==1.0) && (targetLabel==1.0))
					p1l1++;
				else if ((predictedLabel==1.0) && (targetLabel==0.0))
					p1l0++;
				else if ((predictedLabel==0.0) && (targetLabel==1.0))
					p0l1++;
				else if ((predictedLabel==0.0) && (targetLabel==0.0))
					p0l0++;

			}

			System.out.println("Count Correct "+countCorrect);
			System.out.println("Count Incorrect "+countIncorrect);
			System.out.println("p1l1 "+p1l1);
			System.out.println("p1l0 "+p1l0);
			System.out.println("p0l1 "+p0l1);
			System.out.println("p0l0 "+p0l0);


		}

		ArrayList<Double> results = new ArrayList<Double>();
		double crossValidAccAvg = crossValidAccSum/count;
		double crossValidPrcAvg = crossValidPrcSum/count;
		double crossValidRecAvg = crossValidRecSum/count;
		double crossValidF1Avg = crossValidF1Sum/count;

		results.add(crossValidAccAvg);
		results.add(crossValidPrcAvg);
		results.add(crossValidRecAvg);
		results.add(crossValidF1Avg);

		return results;
	}


	public static Classifier loadClassifier(File serializedFile)
			throws FileNotFoundException, IOException, ClassNotFoundException {                                           

		Classifier classifier;

		ObjectInputStream ois =
				new ObjectInputStream (new FileInputStream (serializedFile));
		classifier = (Classifier) ois.readObject();
		ois.close();

		return classifier;
	}


	public static void saveClassifier(Classifier classifier, File serializedFile)
			throws IOException {

		ObjectOutputStream oos =
				new ObjectOutputStream(new FileOutputStream (serializedFile));
		oos.writeObject (classifier);
		oos.close();
	}

	public static void printLabelings(File modelFile, String inputFile) throws IOException, ClassNotFoundException {

		//load the classifier from model file, form the output file
		Classifier classifier = loadClassifier(modelFile);
		String modelFilename = modelFile.getName();
		String outputFile = "Outputs/"+modelFilename.substring(19, modelFilename.length()-9)+"Output";
		
		System.out.println("######################Getting outputs for model "+outputFile+" classifier###############");
		
		
		FileWriter wr = new FileWriter(outputFile);
		//read the input from input file and create instance variable
		ArrayList<Instance> instances = readInputFromTSVFile(inputFile);
		Iterator<?> instancesItr =
				classifier.getInstancePipe().newIteratorFrom(instances.iterator());

		//label and write to the output file
		while (instancesItr.hasNext()) {
			Labeling labeling = classifier.classify(instancesItr.next()).getLabeling();

			// print the labels with their weights in descending order (ie best first)                     
			for (int rank = 0; rank < labeling.numLocations(); rank++){
				System.out.print(labeling.getLabelAtRank(rank) + ":" +
						labeling.getValueAtRank(rank) + " ");
				wr.write(labeling.getLabelAtRank(rank) + ":" +
						labeling.getValueAtRank(rank) + " ");
			}
			System.out.println();
			wr.write("\n");
		}
		wr.close();
	}

	public static void trainAndSaveOnWholeData(InstanceList ilist, ClassifierTrainer trainer) throws IOException {

		Classifier classifier = trainer.train(ilist);
		String outputFilename = "Models/"+ classifier.toString();
		saveClassifier(classifier, new File(outputFilename));

	}
}
