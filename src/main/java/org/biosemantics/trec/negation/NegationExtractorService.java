package org.biosemantics.trec.negation;

import java.util.ArrayList;

public interface NegationExtractorService {
	/**
	 * find and return negation phrases from a sentence
	 * @param sentence
	 * @return list of SentenceNegation object
	 */
	ArrayList<SentenceNegation> getNegations(String sentence);
}
