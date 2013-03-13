package org.biosemantics.trec.report;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import opennlp.tools.util.InvalidFormatException;

import org.biosemantics.trec.opennlp.SentenceSplitterOpennlpImpl;
import org.biosemantics.utility.peregrine.PeregrineRmiClient;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVWriter;

public class TfIdfCalculator {

	private static final String TFIDF_FILE = "/home/bhsingh/Public/norm-tfidf.txt";
	private TrecDatabaseUtility trecDatabaseUtility;
	private SentenceSplitterOpennlpImpl splitter;
	private Peregrine peregrine;
	private static final Logger logger = LoggerFactory.getLogger(TfIdfCalculator.class);
	private Map<String, Double> tfIdfMap = new HashMap<String, Double>();

	public TfIdfCalculator() throws SQLException, InvalidFormatException, IOException {
		trecDatabaseUtility = new TrecDatabaseUtility();
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "/org/biosemantics/utility/peregrine/peregrine-utility-context.xml" });
		PeregrineRmiClient peregrineRmiClient = (PeregrineRmiClient) appContext.getBean("peregrineRmiClient");
		this.peregrine = peregrineRmiClient.getPeregrine();
		splitter = new SentenceSplitterOpennlpImpl("/home/bhsingh/code/git/trec/src/main/resources/en-sent.bin");
		// add to map
		// logger.debug("reading tfidf file");
		// CSVReader csvReader = new CSVReader(new FileReader(TFIDF_FILE));
		// List<String[]> lines = csvReader.readAll();
		// for (String[] columns : lines) {
		// String key = columns[0] + columns[1];
		// tfIdfMap.put(key, Double.valueOf(columns[2]));
		// }
		// csvReader.close();
		// logger.debug("read tfidf file");
	}

	public void calculateTfIdf() throws SQLException, IOException {

		Set<String> visits = trecDatabaseUtility.getAllUniqueVisits();
		Map<String, Integer> conceptCountMap = new HashMap<String, Integer>();
		Map<String, Map<String, Float>> visitConceptFrequencyMap = new HashMap<String, Map<String, Float>>();
		Set<String> allUniqueConcepts = new HashSet<String>();
		float totalVisits = visits.size();
		logger.debug("total visits {}", totalVisits);
		int ctr = 0;
		for (String visit : visits) {
			Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
			Map<String, Integer> conceptFrequencyMap = new HashMap<String, Integer>();
			Map<String, Float> updatedConceptFrequencyMap = new HashMap<String, Float>();
			Set<String> allUniqueConceptsForVisit = new HashSet<String>();
			for (String report : reports) {
				String reportText = trecDatabaseUtility.getReportText(report);
				if (reportText == null) {
					logger.error("reporttext not found for visit {} checksum {} ", new Object[] { visit, report });
				} else {
					List<String> sentences = splitter.getFormattedSentences(reportText);
					for (String sentence : sentences) {
						List<IndexingResult> indexingResults = peregrine.index(sentence, Language.EN);
						for (IndexingResult indexingResult : indexingResults) {
							String concept = String
									.format("C%07d", (Integer) indexingResult.getTermId().getConceptId());
							allUniqueConceptsForVisit.add(concept);
							allUniqueConcepts.add(concept);
							int frequency = 0;
							if (conceptFrequencyMap.containsKey(concept)) {
								frequency = conceptFrequencyMap.get(concept);
							}
							conceptFrequencyMap.put(concept, ++frequency);
						}
					}
				}
			}
			for (String cui : allUniqueConceptsForVisit) {
				int count = 0;
				if (conceptCountMap.containsKey(cui)) {
					count = conceptCountMap.get(cui);
				}
				conceptCountMap.put(cui, ++count);
			}
			// normalize
			float max = 0;
			for (Entry<String, Integer> entry : conceptFrequencyMap.entrySet()) {
				max = max > entry.getValue() ? max : entry.getValue();
			}

			for (Entry<String, Integer> entry : conceptFrequencyMap.entrySet()) {
				float originalValue = entry.getValue();
				updatedConceptFrequencyMap.put(entry.getKey(), (originalValue / max));
			}
			visitConceptFrequencyMap.put(visit, updatedConceptFrequencyMap);
			conceptFrequencyMap = null;
			logger.debug("{}", ++ctr);
		}
		Map<String, Double> idfMap = new HashMap<String, Double>();
		for (String cui : allUniqueConcepts) {
			float count = Float.MIN_VALUE;
			if (conceptCountMap.containsKey(cui)) {
				count = conceptCountMap.get(cui);
			}
			double idf = Math.log(totalVisits / count);
			idfMap.put(cui, idf);
		}
		CSVWriter cSVWriter = new CSVWriter(new FileWriter(TFIDF_FILE));
		for (Entry<String, Map<String, Float>> entry : visitConceptFrequencyMap.entrySet()) {
			for (Entry<String, Float> innerEntry : entry.getValue().entrySet()) {
				Double idf = idfMap.get(innerEntry.getKey());
				double tf = innerEntry.getValue();
				double tfidf = tf * idf;
				cSVWriter.writeNext(new String[] { entry.getKey(), innerEntry.getKey(), String.valueOf(tfidf) });
			}
		}
		cSVWriter.flush();
		cSVWriter.close();
		trecDatabaseUtility.destroy();
	}

	public static void main(String[] args) throws SQLException, IOException {
		TfIdfCalculator calculator = new TfIdfCalculator();
		calculator.calculateTfIdf();
	}

	public Double getTfIdf(String visit, String reportCui) throws IOException {
		return tfIdfMap.get(visit + reportCui);
	}
}
