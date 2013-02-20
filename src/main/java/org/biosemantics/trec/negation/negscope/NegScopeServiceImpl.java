package org.biosemantics.trec.negation.negscope;

import java.util.ArrayList;

import org.biosemantics.trec.negation.service.NegationExtractorService;
import org.biosemantics.trec.negation.service.SentenceNegation;

import edu.uwm.bionlp.bins.NegationTagger;

public class NegScopeServiceImpl implements NegationExtractorService {
	static String featureSet = "words"; // The feature set to use. Can be
										// "words", "pos_words_crf",
	// "pos_words_regex", "pos_cue_crf" or "pos_cue_regex"
	/*
	 * Absolute path to the src/main/resources/negscope folder
	 */
	static String baseDir = null;
	private NegationTagger.NegationDetectionFeatureSet featureSetName;
	private String modelFileName;
	private String cueDetectionFile;
	private NegationTagger nt;

	public NegScopeServiceImpl(String baseDir) {
		this.baseDir = baseDir;
		featureSetName = edu.uwm.bionlp.bins.Driver.getAlgorithmName(featureSet);
		modelFileName = edu.uwm.bionlp.bins.Driver.getModelName(featureSet);
		cueDetectionFile = edu.uwm.bionlp.bins.Driver.getCueDetectionFile(featureSet);
		nt = new NegationTagger(this.baseDir + modelFileName);
	}

	@Override
	public ArrayList<SentenceNegation> getNegations(String sentence) {
		ArrayList<SentenceNegation> results = new ArrayList<SentenceNegation>();
		try {
			String sentenceWithtags = tagSentence(sentence);
			// System.out.println(sentenceWithtags);
			if (sentenceWithtags != null) {
				String[] tokens = sentenceWithtags.split("\\s+");
				for (int i = 0; i < tokens.length; i++) {
					String token = tokens[i];
					String[] str = token.split("\\|");
					if (str[1].equals("B-S")) {
						StringBuilder sb = new StringBuilder();
						sb.append(str[0]);

						for (int j = i + 1; j < tokens.length; j++) {
							String tkn = tokens[j];
							String[] str1 = tkn.split("\\|");
							if (str1[1].equals("I-S")) {
								if (str1[1].matches("\\p{Punct}|\\d")) {

								} else {
									sb.append(" ");
								}
								sb.append(str1[0]);
							} else {
								break;
							}

						}

						String phrase = sb.toString();
						int index = sentence.lastIndexOf(phrase);
						String negPhrase = sentence.substring(index, index + phrase.length());
						// System.out.println(negPhrase);

						SentenceNegation senNeg = new SentenceNegation();
						senNeg.setStart_pos(index);
						senNeg.setEnd_pos(index + phrase.length());
						senNeg.setPhrase(negPhrase);
						results.add(senNeg);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("sentence: " + sentence);
			e.printStackTrace();
		}

		return results;
	}

	/**
	 * Parse sentence and tag each word tag 0 = out of negation scope tag B-S =
	 * start of negation scope tag I-S = part of negation scope
	 * 
	 * @param sentence
	 * @return sentence with tags
	 */

	private String tagSentence(String sentence) {
		String result = null;
		try {
			// if (cueDetectionFile.isEmpty()) {
			// nt = new NegationTagger(baseDir + modelFileName);
			// } else {
			// nt = new NegationTagger(featureSetName, baseDir + modelFileName,
			// baseDir + cueDetectionFile, baseDir
			// + "left3words-wsj-0-18.tagger");
			// }

			String str = new String(sentence.getBytes("US-ASCII")); // Optional,
																	// but
																	// useful.
			result = nt.tagNegationScope(str).trim();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error in taging sentence!");
		}

		return result;
	}

}
