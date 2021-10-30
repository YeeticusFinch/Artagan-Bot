//import com.stanford_nlp.model.*;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
//import edu.stanford.nlp.trees.Tree;
//import edu.stanford.nlp.util.CoreMap;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class StringSimilarity {

	//static Properties props = null;
	//static StanfordCoreNLP pipeline;
	
	//Calculates the similarity (a number within 0 and 1) between two strings.
	public static double similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1 == null || s2 == null || s1.length() < 5 || s2.length() < 3)
			return -1;
		if (s1.length() < s2.length()) {
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0)
			return 1.0;
		
		LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
		return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength;
		// return (longerLength - editDistance(longer, shorter)) / (double)

	}

	// Lenvenshtein stuff I found online
	// See http://rosettacode.org/wiki/Levenshtein_distance#Java
	public static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	//Test it out
	public static void printSimilarity(String s, String t) {
		System.out.println(String.format("%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
	}
	
	
	// I clearly don't know what I'm doing
	/*
	//Get a sentiment score for a string
	public static double getSentiment(String s) {
		if (s.length() < 2)
			return;
		int words = 0;
		Annotation annotation = pipeline.process(text);
		double positiveWSum = 0;
	    double somewhatPositiveWSum = 0;
	    double neutralWSum = 0;
	    double somewhatNegativeWSum = 0;
	    double negativeWSum = 0;
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			// this is the parse tree of the current sentence
			Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
	        int sentenceWordCount = tree.getLeaves().size();
			int predictedClass = RNNCoreAnnotations.getPredictedClass(tree);
	        SimpleMatrix matrix = RNNCoreAnnotations.getPredictions(tree);

	        classes[predictedClass]++;

	        SentenceSentiment sentiment = new SentenceSentiment(matrix.get(0, 0), matrix.get(1, 0),
	                matrix.get(2, 0), matrix.get(3, 0), matrix.get(4, 0), predictedClass, sentenceWordCount);

	        positiveWSum += sentiment.getPositive() * sentenceWordCount;
	        somewhatPositiveWSum += sentiment.getSomewhatPositive() * sentenceWordCount;
	        neutralWSum += sentiment.getNeutral() * sentenceWordCount;
	        somewhatNegativeWSum += sentiment.getSomewhatNegative() * sentenceWordCount;
	        negativeWSum += sentiment.getNegative() * sentenceWordCount;

	        words += sentenceWordCount;
		}
		double positiveMean = 0;
	    double somewhatPositiveMean = 0;
	    double neutralMean = 0;
	    double somewhatNegativeMean = 0;
	    double negativeMean = 0;
		if (words > 0) {
			positiveMean = positiveWSum / words;
			somewhatPositiveMean = somewhatPositiveWSum / words;
	        neutralMean = neutralWSum / words;
	        somewhatNegativeMean = somewhatNegativeWSum / words;
	        negativeMean = negativeWSum / words;
	    }
		
		return (positiveMean + 0.5*somewhatPositiveMean - 0.5 * somewhatNegativeMean - negativeMean) / Math.max(0.1, positiveMean + somewhatPositiveMean + neutralMean + somewhatNegativeMean + negativeMean);
	}
	
	//Compare two strings by sentiment
	public static double sentimentSimilarity(String s1, String s2) {
		if (props == null) {
			props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
			pipeline = new StanfordCoreNLP(props);
		}
		double result = Math.abs(getSentiment(s1) - getSentiment(s2));
		
		return result;
	}
	*/
}