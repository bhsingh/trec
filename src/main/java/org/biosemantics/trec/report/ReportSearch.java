/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.trec.report;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.actors.threadpool.Arrays;
import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * @author bhsingh
 */
public class ReportSearch {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	private Map<String, List<String>> positiveConcepts = new HashMap<String, List<String>>();
	private Map<String, List<String>> negativeConcepts = new HashMap<String, List<String>>();

	public ReportSearch() throws FileNotFoundException, IOException {
		logger.debug("reading concepts file");
		CSVReader csvReader = new CSVReader(new FileReader(CONCEPTS_FILE));
		List<String[]> sentences = csvReader.readAll();
		for (String[] columns : sentences) {
			List<String> pos = new ArrayList<String>();
			List<String> neg = new ArrayList<String>();
			String checksum = columns[0];
			if (columns.length > 3) {
				String[] concepts = (String[]) Arrays.copyOfRange(columns, 3, columns.length);
				for (String strConcept : concepts) {
					String[] strColumns = strConcept.split("\\|");
					if (Integer.valueOf(strColumns[1]) == 0) {
						pos.add(strColumns[0]);
					} else {
						neg.add(strColumns[0]);
					}
				}

			}
			List<String> posConcepts;
			if (positiveConcepts.containsKey(checksum)) {
				posConcepts = positiveConcepts.get(checksum);
			} else {
				posConcepts = new ArrayList<String>();
			}
			posConcepts.addAll(pos);
			positiveConcepts.put(checksum, posConcepts);

			List<String> negConcepts;
			if (negativeConcepts.containsKey(checksum)) {
				negConcepts = negativeConcepts.get(checksum);
			} else {
				negConcepts = new ArrayList<String>();
			}
			negConcepts.addAll(neg);
			negativeConcepts.put(checksum, negConcepts);
		}
		csvReader.close();
		logger.debug("concepts file read");
	}
	
	public List<String> getPositiveCuisForReport(String checksum) {
		return positiveConcepts.get(checksum);
	}

	private static final String CONCEPTS_FILE = "/Users/bhsingh/code/data/trec2013/concept.txt";
	private static final Logger logger = LoggerFactory.getLogger(ReportSearch.class);
}
