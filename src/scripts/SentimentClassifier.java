package scripts;

import java.io.File;
import java.io.IOException;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;

/**
 * Class that handles the sentiment classification
 * 
 */

public class SentimentClassifier {

	String[] categories;
	LMClassifier lmc;
	
	private String path_to_classifier = "./classifiers/classifier.txt";

	public SentimentClassifier() {

		try {
			// train();
			lmc = (LMClassifier) AbstractExternalizable.readObject(new File(
				path_to_classifier));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Classifies the text to sentiment
	 * @param text  to classify
	 * @return best fitting sentiment categaory
	 */

	public String classify(String text) {
		ConditionalClassification classification = lmc.classify(text);
		return classification.bestCategory();
	}
	
	
	/**
	* Trains the classifier
	*
	*/
	
	public void train() throws IOException, ClassNotFoundException {
		File trainDir;
		String[] categories;
		LMClassifier clm;
		trainDir = new File("I://trainDirectory");
		categories = trainDir.list();
		int nGram = 7;

		clm = DynamicLMClassifier.createNGramProcess(categories, nGram);
		for (int i = 0; i < categories.length; ++i) {
			String category = categories[i];
			Classification classification = new Classification(category);
			File file = new File(trainDir, categories[i]);
			File[] trainFiles = file.listFiles();
			for (int j = 0; j < trainFiles.length; ++j) {
				File trainFile = trainFiles[j];
				String review = Files.readFromFile(trainFile, "ISO-8859-1");
				Classified classified = new Classified(review, classification);
				((ObjectHandler) clm).handle(classified);
			}
		}
		AbstractExternalizable.compileTo((com.aliasi.util.Compilable) (clm),
				new File("I://classifier.txt"));
	}

}