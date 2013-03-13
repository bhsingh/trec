package org.biosemantics.trec.report;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class ReportFileReader {
	private static final String CONCEPTS_FILE = "/home/bhsingh/Public/concept.txt";
	private static final Logger logger = LoggerFactory.getLogger(ReportFileReader.class);
	private Map<String, List<String>> positiveConcepts = new HashMap<String, List<String>>();
	private Map<String, List<String>> negativeConcepts = new HashMap<String, List<String>>();

	public ReportFileReader() throws IOException {
		logger.debug("creating positive and negative concept maps");
		CSVReader csvReader = new CSVReader(new FileReader(CONCEPTS_FILE));
		List<String[]> lines = csvReader.readAll();
		for (String[] columns : lines) {
			String report = columns[1];
			String[] conceptDetails = Arrays.copyOfRange(columns, 4, columns.length);
			for (String conceptDetail : conceptDetails) {
				String[] conceptDetailCols = conceptDetail.split("\\|");
				if (Integer.parseInt(conceptDetailCols[1]) == 1) {
					// negated
					List<String> values = null;
					if (negativeConcepts.containsKey(report)) {
						values = negativeConcepts.get(report);
					} else {
						values = new ArrayList<String>();
					}
					values.add(conceptDetailCols[0]);
					negativeConcepts.put(report, values);
				} else {
					// positive
					List<String> values = null;
					if (positiveConcepts.containsKey(report)) {
						values = positiveConcepts.get(report);
					} else {
						values = new ArrayList<String>();
					}
					values.add(conceptDetailCols[0]);
					positiveConcepts.put(report, values);
				}
			}
		}
		csvReader.close();
		logger.debug("done! positive and negative concept maps created");
	}

	public List<String> getPositiveConcepts(String report) {
		return positiveConcepts.get(report);
	}

	public List<String> getNegativeConcepts(String report) {
		return negativeConcepts.get(report);
	}
	
	public static void main(String[] args) throws IOException {
		ReportFileReader obj = new ReportFileReader();
	}

}
