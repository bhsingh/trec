/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.trec.pipe;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.biosemantics.trec.kb.CachedKbClient;
import org.biosemantics.trec.pipe.calculator.AssociationScoreCalculator;
import org.biosemantics.trec.report.ReportFileReader;
import org.biosemantics.trec.report.TfIdfCalculator;
import org.biosemantics.trec.report.TrecDatabaseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadedScoreCalculator {

	private static final String ANSWERS = "/home/bhsingh/code/git/trec/src/main/resources/answers101.txt";
	private static final String GRAPH_DB = "/ssd/bhsingh/graph.db";
	private static final String[] topicCuis = new String[] { "C0011053" };
	private static TrecDatabaseUtility trecDatabaseUtility;
	private static TfIdfCalculator tfIdfCalculator;
	private static CachedKbClient cachedKbClient;
	private static ReportFileReader reportFileReader;

	private static final Logger logger = LoggerFactory.getLogger(MultiThreadedScoreCalculator.class);

	public MultiThreadedScoreCalculator() throws IOException, SQLException {
		trecDatabaseUtility = new TrecDatabaseUtility();
		// tfIdfCalculator = new TfIdfCalculator();
		cachedKbClient = new CachedKbClient(GRAPH_DB);
		reportFileReader = new ReportFileReader();
	}

	public static void main(String[] args) throws IOException, SQLException {
		MultiThreadedScoreCalculator impl = new MultiThreadedScoreCalculator();
		List<String> lines = FileUtils.readLines(new File(ANSWERS));
		for (String line : lines) {
			String[] columns = line.split("\\|");
			String visit = columns[1];
			String answer = columns[2];
			Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
			List<String> reportCuis = new ArrayList<String>();
			for (String report : reports) {
				List<String> positiveConcepts = reportFileReader.getPositiveConcepts(report);
				if (positiveConcepts == null || positiveConcepts.isEmpty()) {
					// logger.error("missing positive concepts for report: {}",
					// report);
				} else {
					reportCuis.addAll(positiveConcepts);
				}
			}
			AssociationScoreCalculator calculator = new AssociationScoreCalculator(topicCuis, reportCuis, answer,
					visit, tfIdfCalculator, cachedKbClient);
			new Thread(calculator).start();
		}
		logger.debug("ALL THREADS STARTED!");
	}
}
