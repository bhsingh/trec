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

import org.apache.commons.io.FileUtils;
import org.biosemantics.conceptstore.client.GraphDbInitializer;
import org.biosemantics.trec.report.ReportSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortestPathImpl {

	private static final String GRAPH_DB = "/media/ssd/bhsingh/graph.db";
	private static GraphDbInitializer db;
	private static ReportSearch reportSearch;
	private static final String[] topicCuis = new String[] { "C0011053", "C0018772", "C1384666", "C2029884" };
	// private static final String[] visits = new String[] { "+SKG7tCxA/zR",
	// "+W4IQBZ/eY/Q", "u5Vz3dfoZa1d" };
	// rel not,partially
	private static final Logger logger = LoggerFactory.getLogger(ShortestPathImpl.class); // ,

	public void init() throws IOException {
		// Map<String, String> config =
		// MapUtil.stringMap("neostore.propertystore.db.index.keys.mapped_memory",
		// "500M",
		// "neostore.propertystore.db.index.mapped_memory", "500M",
		// "neostore.nodestore.db.mapped_memory",
		// "2000M", "neostore.relationshipstore.db.mapped_memory", "1000M",
		// "neostore.propertystore.db.mapped_memory", "1000M",
		// "neostore.propertystore.db.strings.mapped_memory",
		// "2000M");
		db = new GraphDbInitializer(GRAPH_DB);
		reportSearch = new ReportSearch();
	}

	public static void main(String[] args) throws IOException, SQLException {
		ShortestPathImpl impl = new ShortestPathImpl();
		impl.init();
		List<String> lines = FileUtils.readLines(new File(
				"/home/bhsingh/code/git/trec/src/main/resources/answers101.txt"));
		for (String line : lines) {
			String[] columns = line.split("\\|");
			String visit = columns[1];
			String answer = columns[2];
			List<String> reports = reportSearch.getReportsForVisit(visit);
			List<String> reportCuis = new ArrayList<String>();
			for (String report : reports) {
				reportCuis.addAll(reportSearch.getPositiveCuisForReport(report));
			}
			ShortestPathScoreCalculator shortestPathScoreCalculator = new ShortestPathScoreCalculator(topicCuis,
					reportCuis, answer, visit, db);
			new Thread(shortestPathScoreCalculator).start();
		}
	}
}
