package scripts;

import java.io.File;
import java.io.IOException;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;

/**
 * Class that handles the topic classification
 * 
 */
public class TopicClassification {

	private JointClassifier<CharSequence> compiledClassifier;

//	private File TESTING_DIR = new File("../../data/fourNewsGroups/4news-test");

	private DynamicLMClassifier<NGramProcessLM> classifier;
	private Classification[] classiList;
	private JointClassification jc;

	// List of categories
	private String[] CATEGORIES = { "Computers & Technology", "Education", "Family", "Food", "Health", "Marketing",
			"Music", "News & Media", "Other", "Pets", "Politics", "Recreation & Sports" };

	private static int NGRAM_SIZE = 6;

	@SuppressWarnings("unchecked")
	/**
	 * trains the topic classifier
	 * 
	 */
	public void trainclassifier() throws ClassNotFoundException, IOException {

		for (int s = 1; s <= 1; s++) {

			File TRAINING_DIR = new File("/Users/michaelhundt/Desktop/Applikation/TrainedData/DataSet" + s);
			classifier = DynamicLMClassifier.createNGramProcess(CATEGORIES, NGRAM_SIZE);

			for (int i = 0; i < CATEGORIES.length; ++i) {
				File classDir = new File(TRAINING_DIR, CATEGORIES[i]);
				if (!classDir.isDirectory()) {
					String msg = "Could not find training directory=" + classDir + "\nHave you unpacked 4 newsgroups?";
					System.out.println(msg); // in case exception gets lost in shell
					throw new IllegalArgumentException(msg);
				}

				String[] trainingFiles = classDir.list();
				for (int j = 0; j < trainingFiles.length; ++j) {
					File file = new File(classDir, trainingFiles[j]);
					String text = Files.readFromFile(file, "ISO-8859-1");
					System.out.println("Training on " + CATEGORIES[i] + "/" + trainingFiles[j]);
					Classification classification = new Classification(CATEGORIES[i]);
					Classified<CharSequence> classified = new Classified<CharSequence>(text, classification);
					classifier.handle(classified);
				}
			}
			// compiling
			System.out.println("Compiling");
			// we created object so know it's safe
			compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable.compile(classifier);

			classiList = new Classification[CATEGORIES.length];
			for (int i=0 ; i< CATEGORIES.length; i++) {
				classiList[i] = new Classification(CATEGORIES[i]);
			}
		}
	}

	/**
	 * 
	 * @param content
	 * @return the best matching category
	 */
	public String getCategory(String content) {
		String bestcat = "";
		boolean storeCategories = true;
		JointClassifierEvaluator<CharSequence> evaluator = new JointClassifierEvaluator<CharSequence>(compiledClassifier, CATEGORIES, storeCategories);
		for (int i = 0; i < CATEGORIES.length; ++i) {

			// for (int j=0; j<testingFiles.length; ++j) {
			String text = content;
			Classified<CharSequence> classified = new Classified<CharSequence>(text, classiList[i]);
			evaluator.handle(classified);
			jc = compiledClassifier.classify(text);
			String bestCategory = jc.bestCategory();
//			String details = jc.toString();
			bestcat = bestCategory;
			classified = null;
			// System.out.println("Got best category of: " + bestCategory);
			// System.out.println(jc.toString());
			// System.out.println("---------------");
			// }
		}
		// ConfusionMatrix confMatrix = evaluator.confusionMatrix();
		// System.out.println("Total Accuracy: " + confMatrix.totalAccuracy());

		// System.out.println("\nFULL EVAL");
		// System.out.println(evaluator);
		return bestcat;
	}
	
}
