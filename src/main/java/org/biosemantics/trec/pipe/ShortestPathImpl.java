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
import org.biosemantics.trec.report.ReportSearch;
import org.biosemantics.trec.report.TfIdfCalculator;
import org.biosemantics.trec.report.TrecDatabaseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortestPathImpl {

	private static final String ANSWERS = "/Users/bhsingh/code/git/trec/src/main/resources/answers101.txt";
	private static final String GRAPH_DB = "/Users/bhsingh/code/neo4j-community-1.8/data/graph.db";
	private static final String[] topicCuis = new String[] { "C0011053", "C0018772", "C1384666", "C2029884" };
	// private static final String[] topicCuis = new String[] { "C0011053" };
	private static ReportSearch reportSearch;
	private static TrecDatabaseUtility trecDatabaseUtility;
	// private static final String[] visits = new String[] { "+SKG7tCxA/zR",
	// "+W4IQBZ/eY/Q", "u5Vz3dfoZa1d" };
	// rel not,partially
	private static TfIdfCalculator tfIdfCalculator;
	private static final Logger logger = LoggerFactory.getLogger(ShortestPathImpl.class); // ,
	private static CachedKbClient cachedKbClient;

	public void init() throws IOException, SQLException {
		reportSearch = new ReportSearch();
		trecDatabaseUtility = new TrecDatabaseUtility();
		tfIdfCalculator = new TfIdfCalculator();
		cachedKbClient = new CachedKbClient(GRAPH_DB);
	}

	public static void main(String[] args) throws IOException, SQLException {
		ShortestPathImpl impl = new ShortestPathImpl();
		impl.init();
		List<String> lines = FileUtils.readLines(new File(ANSWERS));
		for (String line : lines) {
			String[] columns = line.split("\\|");
			String visit = columns[1];
			String answer = columns[2];
			Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
			List<String> reportCuis = new ArrayList<String>();
			for (String report : reports) {
				List<String> concepts = reportSearch.getPositiveCuisForReport(report);
				if (concepts == null || concepts.isEmpty()) {
					logger.debug("no positive concepts found for report {}", report);
				} else {
					reportCuis.addAll(concepts);
				}
			}
			ShortestPathWithTfIdf tfIdfCal = new ShortestPathWithTfIdf(topicCuis, reportCuis, answer, visit,
					tfIdfCalculator, cachedKbClient);
			tfIdfCal.calculatePathScore();
		}
	}
}
