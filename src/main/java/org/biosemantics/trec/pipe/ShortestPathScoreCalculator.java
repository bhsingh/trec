package org.biosemantics.trec.pipe;

import java.util.Collection;
import java.util.List;

import org.biosemantics.conceptstore.client.GraphDbInitializer;
import org.biosemantics.conceptstore.domain.Concept;
import org.biosemantics.conceptstore.domain.Notation;
import org.neo4j.graphdb.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortestPathScoreCalculator implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ShortestPathScoreCalculator.class); // ,

	private String[] topicCuis;
	private List<String> reportCuis;
	private String result;
	private String visit;
	private GraphDbInitializer db;

	public ShortestPathScoreCalculator(String[] topicCuis, List<String> reportCuis, String result, String visit,
			GraphDbInitializer db) {
		this.topicCuis = topicCuis;
		this.reportCuis = reportCuis;
		this.result = result;
		this.visit = visit;
		this.db = db;
	}

	public void run() {
		float finalScore = calculatePathScore();
		String score = String.format("%.2f", finalScore);
		logger.info("{} {} {}", new Object[] { visit, result, score });
	}

	public float calculatePathScore() {
		logger.debug("{} {} ", new Object[] { topicCuis, reportCuis });
		float finalScore = 0F;
		for (String topicCui : topicCuis) {
			Concept topic = getConceptForCui(topicCui);
			for (String reportCui : reportCuis) {
				Concept reportConcept = getConceptForCui(reportCui);
				Iterable<Path> paths = db.getTraversalRepository().findShortestPath(topic.getId(),
						reportConcept.getId(), 4);
				float pathCount = 0;
				float pathLength = 0;
				for (Path path : paths) {
					++pathCount;
					if (pathLength == 0) {
						if (path.length() == 0) {
							// same concept found notch up the score!
							// path length is 0 for same concept
							pathLength = 1000000F;// 1 million
							logger.info("same concept found for visit {}", visit);
						} else {
							pathLength = (path.length());
						}

					}
				}
				float score = 0;
				if (pathCount == 0 && pathLength == 0) {
					score = 0;
				} else {
					float inversePathLength = 1 / pathLength;
					score = pathCount * inversePathLength;
				}
				finalScore += score;
			}
		}
		return finalScore;
	}

	private Concept getConceptForCui(String cui) {
		Collection<Notation> notations = db.getNotationRepository().getByCode(cui);
		Concept foundConcept = null;
		if (notations.size() > 1) {
			throw new IllegalStateException(cui);
		}

		for (Notation notation : notations) {
			Collection<Concept> concepts = notation.getRelatedConcepts();
			if (concepts.size() > 1) {
				throw new IllegalStateException(cui);
			}
			for (Concept concept : concepts) {
				foundConcept = concept;
			}
		}
		return foundConcept;
	}

}
