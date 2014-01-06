package org.biosemantics.trec.report;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import opennlp.tools.util.InvalidFormatException;

import org.apache.commons.lang.StringUtils;

public class ReportToSentence {

	public static void main(String[] args) throws SQLException, InvalidFormatException, IOException {
		TrecDatabaseUtility trecDatabaseUtility = new TrecDatabaseUtility();
		ReportUtility reportUtility = new ReportUtility();
		try {
			int ctr = 0;
			Set<String> visits = trecDatabaseUtility.getAllUniqueVisits();
			for (String visit : visits) {
				Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
				for (String report : reports) {
					String text = trecDatabaseUtility.getReportText(report);
					List<String> sentences = reportUtility.getFormattedSentences(text);
					for (String sentence : sentences) {
						if (!StringUtils.isBlank(sentence)) {
							trecDatabaseUtility.insertSentence(visit, report, sentence);
						} else {
							System.err.println(sentence);
						}
					}
				}
				System.out.println(++ctr);
			}

		} finally {
			trecDatabaseUtility.destroy();
			
		}
	}
}
