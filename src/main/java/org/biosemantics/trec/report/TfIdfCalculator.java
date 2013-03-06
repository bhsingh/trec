package org.biosemantics.trec.report;

import java.io.FileReader;
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

import org.biosemantics.utility.peregrine.PeregrineRmiClient;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class TfIdfCalculator {

	private static final String TFIDF_FILE = "/home/bhsingh/Public/tfidf.txt";
	private TrecDatabaseUtility trecDatabaseUtility;
	private ReportUtility reportUtility;
	private Peregrine peregrine;
	private static final Logger logger = LoggerFactory.getLogger(TfIdfCalculator.class);
	private Map<String, Float> tfIdfMap = new HashMap<String, Float>();

	public TfIdfCalculator() throws SQLException, InvalidFormatException, IOException {
		trecDatabaseUtility = new TrecDatabaseUtility();
		reportUtility = new ReportUtility();
		if (tfIdfMap.size() == 0) {
			// add to map
			logger.debug("reading tfidf file");
			CSVReader csvReader = new CSVReader(new FileReader(TFIDF_FILE));
			List<String[]> lines = csvReader.readAll();
			for (String[] columns : lines) {
				String key = columns[0] + columns[1];
				tfIdfMap.put(key, Float.valueOf(columns[2]));
			}
			csvReader.close();
			logger.debug("read tfidf file");
		}
	}

	public void calculateTfIdf() throws SQLException, IOException {

		Set<String> visits = trecDatabaseUtility.getAllUniqueVisits();
		Map<String, Integer> conceptCountMap = new HashMap<String, Integer>();
		Map<String, Map<String, Integer>> visitConceptFrequencyMap = new HashMap<String, Map<String, Integer>>();
		Set<String> allUniqueConcepts = new HashSet<String>();
		float totalVisits = visits.size();
		logger.debug("total visits {}", totalVisits);
		int ctr = 0;
		for (String visit : visits) {
			Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
			Map<String, Integer> conceptFrequencyMap = new HashMap<String, Integer>();
			Set<String> allUniqueConceptsForVisit = new HashSet<String>();
			for (String report : reports) {
				String reportText = trecDatabaseUtility.getReportText(report);
				if (reportText == null) {
					logger.error("reporttext not found for visit {} checksum {} ", new Object[] { visit, report });
				} else {
					List<String> sentences = reportUtility.getFormattedSentences(reportText);
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
			visitConceptFrequencyMap.put(visit, conceptFrequencyMap);
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
		for (Entry<String, Map<String, Integer>> entry : visitConceptFrequencyMap.entrySet()) {
			for (Entry<String, Integer> innerEntry : entry.getValue().entrySet()) {
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

	public Float getTfIdf(String visit, String reportCui) throws IOException {
		return tfIdfMap.get(visit + reportCui);

	}
}
