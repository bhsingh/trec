package org.biosemantics.trec.pipe.calculator;

import java.io.IOException;
import java.util.List;

import org.biosemantics.trec.kb.CachedKbClient;
import org.biosemantics.trec.report.TfIdfCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortestPathWithTfIdf implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ShortestPathScoreCalculator.class); // ,

	private String[] topicCuis;
	private List<String> reportCuis;
	private String result;
	private String visit;
	private TfIdfCalculator tfIdfCalculator;
	private CachedKbClient cachedKbClient;

	public ShortestPathWithTfIdf(String[] topicCuis, List<String> reportCuis, String result, String visit,
			TfIdfCalculator tfIdfCalculator, CachedKbClient cachedKbClient) {
		this.topicCuis = topicCuis;
		this.reportCuis = reportCuis;
		this.result = result;
		this.visit = visit;
		this.tfIdfCalculator = tfIdfCalculator;
		this.cachedKbClient = cachedKbClient;
	}

	public void run() {
		long start = System.currentTimeMillis();
		float finalScore;
		try {
			float[] data = calculatePathScore();
			float pathsScore = data[0];
			finalScore = pathsScore / reportCuis.size();
			String score = String.format("%.5f", finalScore);
			logger.info("{}\t{}\t{}\t{}:ms\t{}\t{}\t{}",
					new Object[] { visit, result, score, (System.currentTimeMillis() - start), reportCuis.size(),
							data[1], data[2] });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public float[] calculatePathScore() throws IOException {
		logger.debug(" visit:{} reportsCuis:{} ", new Object[] { visit, reportCuis.size() });
		float finalScore = 0F;
		float matches = 0F;
		float exactmatches = 0F;
		for (String topicCui : topicCuis) {
			for (String reportCui : reportCuis) {
				double score = 0F;
				int intPathLength = cachedKbClient.getPathLength(topicCui, reportCui, 2);
				if (intPathLength != 0) {
					matches++;
					if (intPathLength == 1) {
						exactmatches++;
					}
					Double tfIdf = tfIdfCalculator.getTfIdf(visit, reportCui);
					if (tfIdf == null) {
						logger.error("no tfidf found for {} {}", new Object[] { visit, reportCui });
					} else {
						float pathLength = intPathLength;
						score = ((1F / pathLength) * tfIdf);
					}
				}
				finalScore += score;
			}
		}
		return new float[] { finalScore, exactmatches, matches };
	}

}
