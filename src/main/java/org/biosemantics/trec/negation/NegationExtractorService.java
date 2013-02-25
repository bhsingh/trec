package org.biosemantics.trec.negation;

import java.util.ArrayList;

public interface NegationExtractorService {
	/**
	 * find and return negation phrases from a sentence. NOTE: Negex only works
	 * for lower case negations, hence in the impl we convert the sentence first
	 * to lower case
	 * 
	 * @param sentence
	 * @return list of SentenceNegation object
	 */
	ArrayList<SentenceNegation> getNegations(String sentence);
}
