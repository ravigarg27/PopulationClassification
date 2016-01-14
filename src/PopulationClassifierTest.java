import java.io.File;
import java.io.IOException;

public class PopulationClassifierTest {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		//print labelings giving classifier model file, inputFile and outputFile
		File modelFolder = new File("Models/");
		File[] modelFiles = modelFolder.listFiles();
		String inputFile = "uptodateData.txt";
		
		for (int i = 0; i<modelFiles.length; i++) {
			
			File modelFile = modelFiles[i];
			HelperMethods.printLabelings(modelFile, inputFile);
			
		}
		

	}

}
