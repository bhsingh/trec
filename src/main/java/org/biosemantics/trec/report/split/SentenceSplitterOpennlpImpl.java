package org.biosemantics.trec.report.split;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
