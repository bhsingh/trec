/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.trec.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import opennlp.tools.util.InvalidFormatException;
import org.biosemantics.trec.opennlp.SentenceSplitterOpennlpImpl;

/**
 *
 * @author bhsingh
 */
public class ReportUtility {

    private SentenceSplitterOpennlpImpl splitter;

    public ReportUtility() throws InvalidFormatException, IOException {
        this.splitter = new SentenceSplitterOpennlpImpl("/Users/bhsingh/code/git/trec/src/main/resources/en-sent.bin");
    }

    public List<String> getFormattedSentences(String text) {
        String[] sentences = splitter.split(text);
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
}
