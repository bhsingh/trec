package org.biosemantics.trec.report.split;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;

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
				.readFileToString(new File("/Users/bhsingh/code/git/trec/src/main/resources/report1.txt"));
		String[] lines = splitter.split(text);
		for (String line : lines) {
			String formattedLine = line.replaceAll("\\s+", " ");
			System.out.println(formattedLine);

			System.out.println("---------------------------");
		}
		splitter.destroy();
	}
}
