package org.biosemantics.trec.opennlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;

import org.apache.commons.io.FileUtils;

public class SentenceSplitterOpennlpImpl {

	private static InputStream modelIn;
	private static SentenceModel model;
	private static SentenceDetectorME sentenceDetector;

	public SentenceSplitterOpennlpImpl(String modelFilePath) throws InvalidFormatException, IOException {
		modelIn = new FileInputStream(modelFilePath);
		model = new SentenceModel(modelIn);
		sentenceDetector = new SentenceDetectorME(model);
	}

	public String[] split(String text) {
		String[] sentences = sentenceDetector.sentDetect(text);
		return sentences;
	}

	public List<String> getFormattedSentences(String text) {
		String[] sentences = split(text);
		List<String> formattedSentences = new ArrayList<String>();
		for (String sentence : sentences) {
			if (sentence.contains("Safe-harbor compliant")) {
				// 2955530 sentence.txt
				continue;
			}
			if (sentence.contains("Electronically Signed by")) {
				// 2933893 sentence.txt
				continue;
			}
			if (sentence.contains("Dictated by")) {
				// 2933893 sentence.txt
				continue;
			}
			if (sentence.contains("Thank you")) {
				// 2928558 sentence.txt
				continue;
			}
			if (sentence.contains("____________________________")) {
				// 2895847 sentence.txt
				continue;
			}
			if (sentence.contains("CARBON-COPY")) {
				// 2892743 sentence.txt
				continue;
			}
			if (sentence.contains("END OF IMPRESSION")) {
				// 2848309 sentence.txt
				continue;
			}
			String formattedSentence = sentence.replaceAll("\\s+", " ");
			formattedSentences.add(formattedSentence);
		}
		return formattedSentences;
	}

	public void destroy() throws IOException {
		if (modelIn != null) {
			try {
				modelIn.close();
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) throws InvalidFormatException, IOException {
		SentenceSplitterOpennlpImpl splitter = new SentenceSplitterOpennlpImpl(
				"/Users/bhsingh/code/git/trec/src/main/resources/en-sent.bin");
		String text = FileUtils
				.readFileToString(new File("/Users/bhsingh/code/git/trec/src/main/resources/report2.txt"));
		String[] lines = splitter.split(text);
		for (String line : lines) {
			String formattedLine = line.replaceAll("\\s+", " ");
			System.out.println(formattedLine);

			System.out.println("---------------------------");
		}
		splitter.destroy();
	}
}
