package org.biosemantics.trec.negation.negscope;

import java.util.ArrayList;

import org.biosemantics.trec.negation.service.NegationExtractorService;
import org.biosemantics.trec.negation.service.SentenceNegation;

public class NegScopeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sentence = "This is not dementia but it is malaria.";
		NegationExtractorService negex = new NegScopeServiceImpl();
		printNegResults(sentence, negex);
	}

	private static void printNegResults(String sentence, NegationExtractorService negex) {
		System.out.println("Sentence: " + sentence);
		ArrayList<SentenceNegation> results = negex.getNegations(sentence);
		if(results.size() != 0){
			for(SentenceNegation neg : results){
				System.out.println("Start: " + neg.getStart_pos() + "\tEnd: " + neg.getEnd_pos());
				System.out.println("Negation phrase: " + neg.getPhrase());
			}
		}		
	}

}
