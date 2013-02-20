package org.biosemantics.trec.negation.negex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biosemantics.trec.negation.service.NegationExtractorService;
import org.biosemantics.trec.negation.service.SentenceNegation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NegExImpl implements NegationExtractorService {

	private static final Logger logger = LoggerFactory.getLogger(NegExImpl.class);
	private final int FAILSAFE_THRESHOLD = 1000;
	private int prev_pos;
	private GenNegEx negex;
	private Map<String, Object> map = new HashMap<String, Object>();

	/**
	 * find and return negation phrases from a sentence
	 */

	public NegExImpl() {
		negex = new GenNegEx(true);
		List<String> negPhrases = negex.getNegPhrases();
		for (String string : negPhrases) {
			map.put(string, null);
		}
	}

	public ArrayList<SentenceNegation> getNegations(String sentence) {
		ArrayList<SentenceNegation> results = new ArrayList<SentenceNegation>();
		int count = 0;

		String scope = "0";
		int prev_end_pos = 0;
		String newSentence = sentence;

		while (!scope.equals("-1")) {
			if (++count >= FAILSAFE_THRESHOLD) {
				logger.error("FAILSAFE REACHED!  sentence: --> " + newSentence);
				return results;
			}
			scope = negex.negScope(newSentence);

			if (scope.endsWith("-1")) {
				return results;
			}

			int scope_end = getScopePosition(scope, 1);

//			if (ExceptionsFound(scope, newSentence)) {
//				return results;
//			}

			if (scope_end <= 0) {
				return results;
			} else {
				SentenceNegation senNeg = new SentenceNegation();
				addPositionsToSentenceNegation(sentence, newSentence, scope, senNeg, prev_end_pos);
				if (senNeg.getEnd_pos() != -1) {
					newSentence = sentence.substring(prev_pos);
					prev_end_pos = prev_pos;
				}
				results.add(senNeg);
			}
		}
		return results;
	}

	/**
	 * Check for exceptions
	 * 
	 * @param scope
	 * @param newSentence
	 * @return true if found an exception
	 */
	private boolean ExceptionsFound(String scope, String newSentence) {
		boolean flag = false;
		if (negationFoundAtTheEnd(scope, newSentence)) {
			return true;
		}
		return flag;
	}

	/**
	 * If the only negation trigger found is at the end of the sentence, return false and do not do any processing this
	 * hack needs to be fixed at the sentence creation side so a sentence do not end with a negation word.
	 * 
	 * @param scope
	 * @param newSentence
	 * @return
	 */
	private boolean negationFoundAtTheEnd(String scope, String newSentence) {
		int scope_start = getScopePosition(scope, 0);
		int scope_end = getScopePosition(scope, 1);
		if (scope_start == 0) {
			if ((scope_end + 1) == (newSentence.split("\\s+").length - 1)) {
				logger.error("Negation trigger found at the end of the sentence! --> " + newSentence);
				return true;
			}
		}
		return false;
	}

	/**
	 * Get position from the negation scope variable
	 * 
	 * @param scope
	 * @param i
	 * @return scope position
	 */
	private int getScopePosition(String scope, int i) {
		if (scope.equals("-1") || scope.endsWith("-1")) {
			return -1;
		}
		String[] scopeRange = scope.split("-");
		scopeRange[0] = scopeRange[0].trim();
		scopeRange[1] = scopeRange[1].trim();
		int int_point = Integer.parseInt(scopeRange[0]);
		int end_point = Integer.parseInt(scopeRange[1]);

		if (i == 0) {
			return int_point;
		}

		return end_point;
	}

	/**
	 * Add start and end character position to the negation phrase
	 * 
	 * @param sentence
	 * @param newSentence
	 * @param scope
	 * @param senNeg
	 * @param prevEndPos
	 */
	private void addPositionsToSentenceNegation(String sentence, String newSentence, String scope,
			SentenceNegation senNeg, int prevEndPos) {
		if (!scope.endsWith("-1")) {
			String[] scopeRange = scope.split("-");
			scopeRange[0] = scopeRange[0].trim();
			scopeRange[1] = scopeRange[1].trim();
			int int_point = Integer.parseInt(scopeRange[0]);
			int end_point = Integer.parseInt(scopeRange[1]);
			String[] tokens = newSentence.split("\\s+");
			if (map.containsKey(tokens[tokens.length - 1])) {
				logger.error("last word found negative, sentence: {}", sentence);
			}
			String negationPhrase = "";
			for (int i = int_point; i <= end_point; i++) {
				negationPhrase = negationPhrase + " " + tokens[i];
			}
			negationPhrase = negationPhrase.substring(1);

			int_point = prevEndPos + newSentence.indexOf(negationPhrase);
			end_point = int_point + negationPhrase.length();

			senNeg.setStart_pos(int_point + 1);
			senNeg.setEnd_pos(end_point + 1);
			senNeg.setPhrase(negationPhrase);
			prev_pos = end_point;
		}
	}
}