package org.biosemantics.trec.negation.negex;

import java.util.ArrayList;

import org.biosemantics.trec.negation.service.NegationExtractorService;
import org.biosemantics.trec.negation.service.SentenceNegation;

public class NegationExtractorTest {

	public static void main(String[] args){
//		String sentence = "**INSTITUTION:  The patient was not transferred from the  TCU where she was noted to have worsening of her renal function.";
		String sentence = "The patient is being discharged on the following medications: 1. not Procrit 20,000 units subcutaneously every Tuesdays and Saturdays.";
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
