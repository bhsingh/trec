package org.biosemantics.trec.pipe.calculator;

import java.util.List;

import org.biosemantics.trec.kb.CachedKbClient;
import org.biosemantics.trec.report.TfIdfCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssociationScoreCalculator implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ShortestPathScoreCalculator.class); // ,

	private String[] topicCuis;
	private List<String> reportCuis;
	private String result;
	private String visit;
	private TfIdfCalculator tfIdfCalculator;
	private CachedKbClient cachedKbClient;

	public AssociationScoreCalculator(String[] topicCuis, List<String> reportCuis, String result, String visit,
			TfIdfCalculator tfIdfCalculator, CachedKbClient cachedKbClient) {
		this.topicCuis = topicCuis;
		this.reportCuis = reportCuis;
		this.result = result;
		this.visit = visit;
		this.tfIdfCalculator = tfIdfCalculator;
		this.cachedKbClient = cachedKbClient;
	}

	public void run() {
		int otherMatches = 0;
		int exactmatches = 0;
		for (String topicCui : topicCuis) {
			for (String reportCui : reportCuis) {
				if (reportCui.equals(topicCui)) {
					exactmatches++;
				} else {
					boolean exists = cachedKbClient.pathExists(topicCui, reportCui, 1);
					if (exists) {
						otherMatches++;
					}
				}
			}
		}
		logger.info("{}\t{}\t{}\t{}\t{}", new Object[] { visit, result, (reportCuis.size() * topicCuis.length),
				otherMatches, exactmatches });
	}

}
