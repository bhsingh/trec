package org.biosemantics.trec.report;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import opennlp.tools.util.InvalidFormatException;

import org.biosemantics.trec.negation.NegationExtractorService;
import org.biosemantics.trec.negation.SentenceNegation;
import org.biosemantics.trec.negation.negex.NegExImpl;
import org.biosemantics.trec.opennlp.SentenceSplitterOpennlpImpl;
import org.biosemantics.utility.peregrine.PeregrineRmiClient;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ReportParser {
	private TrecDatabaseUtility trecDatabaseUtility;
	private SentenceSplitterOpennlpImpl splitter;
	private NegationExtractorService negationExtractorService;
	private Peregrine peregrine;

	public ReportParser() throws SQLException, InvalidFormatException, IOException {
		trecDatabaseUtility = new TrecDatabaseUtility();
		splitter = new SentenceSplitterOpennlpImpl("/home/bhsingh/code/git/trec/src/main/resources/en-sent.bin");
		negationExtractorService = new NegExImpl();
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "/org/biosemantics/utility/peregrine/peregrine-utility-context.xml" });
		PeregrineRmiClient peregrineRmiClient = (PeregrineRmiClient) appContext.getBean("peregrineRmiClient");
		peregrine = peregrineRmiClient.getPeregrine();
	}

	public void reportsToSentences() throws SQLException, IOException {
		System.out.println("read all reports");
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(CONCEPTS_FILE)));
		Set<String> visits = trecDatabaseUtility.getAllUniqueVisits();
		int visitCtr = 0;
		for (String visit : visits) {
			Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
			for (String report : reports) {
				String text = trecDatabaseUtility.getReportText(report);
				List<String> sentences = splitter.getFormattedSentences(text);
				int sentenceCtr = 0;
				for (String sentence : sentences) {
					List<String> output = new ArrayList<String>();
					output.add(visit);
					output.add(report);
					output.add(String.valueOf(++sentenceCtr));
					output.add(sentence);
					List<SentenceNegation> sentenceNegations = negationExtractorService.getNegations(sentence);
					List<IndexingResult> indexingResults = peregrine.index(sentence, Language.EN);
					for (IndexingResult indexingResult : indexingResults) {
						String concept = String.format("C%07d", (Integer) indexingResult.getTermId().getConceptId());
						int conceptStartPos = indexingResult.getStartPos() + 1;
						int conceptEndPos = indexingResult.getEndPos() + 2;
						boolean negated = false;
						if (sentenceNegations != null) {
							for (SentenceNegation negation : sentenceNegations) {
								if (negation.getStart_pos() <= conceptStartPos
										&& negation.getEnd_pos() >= conceptEndPos) {
									negated = true;
									break;
								}
							}
						}
						if (negated) {
							output.add(concept + "|" + 1 + "|" + conceptStartPos + "|" + conceptEndPos);
						} else {
							output.add(concept + "|" + 0 + "|" + conceptStartPos + "|" + conceptEndPos);
						}
					}
					csvWriter.writeNext(output.toArray(new String[output.size()]));
				}
			}
			logger.debug("visit parsed: {}", ++visitCtr);
		}
		csvWriter.flush();
		csvWriter.close();
	}

	public void indexSentences() throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(SENTENCE_FILE));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(CONCEPTS_FILE)));
		Map<String, Integer> freqmap = new HashMap<String, Integer>();
		ValueComparator bvc = new ValueComparator(freqmap);
		TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "/org/biosemantics/utility/peregrine/peregrine-utility-context.xml" });
		PeregrineRmiClient peregrineRmiClient = (PeregrineRmiClient) appContext.getBean("peregrineRmiClient");
		NegationExtractorService negex = new NegExImpl();
		Peregrine peregrine = peregrineRmiClient.getPeregrine();
		List<String[]> lines = csvReader.readAll();
		List<String> output = new ArrayList<String>();
		int ctr = 0;
		try {
			for (String[] columns : lines) {
				String text = columns[2];
				output.add(columns[0]);
				output.add(columns[1]);
				output.add(text);
				ArrayList<SentenceNegation> sentenceNegations = negex.getNegations(text);
				List<IndexingResult> indexingResults = peregrine.index(text, Language.EN);
				for (IndexingResult indexingResult : indexingResults) {
					String concept = String.format("C%07d", (Integer) indexingResult.getTermId().getConceptId());
					int conceptStartPos = indexingResult.getStartPos() + 1;
					int conceptEndPos = indexingResult.getEndPos() + 2;
					boolean negated = false;
					if (sentenceNegations != null) {
						for (SentenceNegation negation : sentenceNegations) {
							if (negation.getStart_pos() <= conceptStartPos && negation.getEnd_pos() >= conceptEndPos) {
								negated = true;
								break;
							}
						}
					}
					if (negated) {
						output.add(concept + "|" + 1 + "|" + conceptStartPos + "|" + conceptEndPos);
					} else {
						output.add(concept + "|" + 0 + "|" + conceptStartPos + "|" + conceptEndPos);
					}
					int frequency = 0;
					if (freqmap.containsKey(concept)) {
						frequency = freqmap.get(concept);
					}
					freqmap.put(concept, ++frequency);
				}
				csvWriter.writeNext(output.toArray(new String[output.size()]));
				System.out.println(++ctr);
				output.clear();
			}
		} finally {
			csvWriter.flush();
			csvWriter.close();
			csvReader.close();
		}
		sorted_map.putAll(freqmap);
		CSVWriter freqWriter = new CSVWriter(new FileWriter(new File(CONCEPTS_FREQ_FILE)));
		for (Entry<String, Integer> entry : sorted_map.entrySet()) {
			freqWriter.writeNext(new String[] { entry.getKey(), String.valueOf(entry.getValue()) });
		}
		freqWriter.flush();
		freqWriter.close();

	}

	public static void main(String[] args) throws SQLException, IOException {
		ReportParser reportParser = new ReportParser();
		reportParser.reportsToSentences();
	}


	private static final String SENTENCE_FILE = "/home/bhsingh/Public/sentence.txt";
	private static final String CONCEPTS_FILE = "/home/bhsingh/Public/concept.txt";
	private static final String CONCEPTS_FREQ_FILE = "/home/bhsingh/Public/frequency.txt";
	private static final Logger logger = LoggerFactory.getLogger(ReportParser.class);
}

class ValueComparator implements Comparator<String> {

	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
