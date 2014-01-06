package org.biosemantics.trec.negation.negex;

import java.util.ArrayList;

import org.biosemantics.trec.negation.NegationExtractorService;
import org.biosemantics.trec.negation.SentenceNegation;

public class NegationExtractorTest {

	public static void main(String[] args){
//		String sentence = "**INSTITUTION:  The patient was not transferred from the  TCU where she was noted to have worsening of her renal function.";
		String sentence = "The patient denied any chest pain but there was heartburn.";
		NegationExtractorService negex = new NegExImpl();
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
