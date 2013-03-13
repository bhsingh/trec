package org.biosemantics.trec.kb;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.biosemantics.conceptstore.client.GraphDbInitializer;
import org.biosemantics.conceptstore.domain.Concept;
import org.biosemantics.conceptstore.domain.Notation;
import org.neo4j.graphdb.Path;

public class CachedKbClient {

	private GraphDbInitializer db;
	private Map<String, Integer> pathLengthMap = new HashMap<String, Integer>();
	private Map<String, Boolean> pathExistsMap = new HashMap<String, Boolean>();
	private Map<String, Long> cuiMap = new HashMap<String, Long>();

	public CachedKbClient(String dbPath) {
		// Map<String, String> config =
		// MapUtil.stringMap("neostore.propertystore.db.index.keys.mapped_memory",
		// "500M",
		// "neostore.propertystore.db.index.mapped_memory", "500M",
		// "neostore.nodestore.db.mapped_memory",
		// "2000M", "neostore.relationshipstore.db.mapped_memory", "1000M",
		// "neostore.propertystore.db.mapped_memory", "1000M",
		// "neostore.propertystore.db.strings.mapped_memory",
		// "2000M");
		this.db = new GraphDbInitializer(dbPath);
	}

	public Integer getPathLength(String from, String to, int max) {
		Integer pathLength = 0;
		if (pathLengthMap.containsKey(from + to)) {
			pathLength = pathLengthMap.get(from + to);
		} else {
			Long fromId = getConceptForCui(from);
			Long toId = getConceptForCui(to);
			Iterable<Path> paths = db.getTraversalRepository().findShortestPath(fromId, toId, max);
			if (paths != null) {
				for (Path path : paths) {
					pathLength = (path.length() + 1);// 0 for same concept
					break;
				}
			}
			pathLengthMap.put(from + to, pathLength);
		}
		return pathLength;
	}

	public boolean pathExists(String from, String to, int max) {
		if (pathExistsMap.containsKey(from + to)) {
			return pathExistsMap.get(from + to);
		} else {
			Long fromId = getConceptForCui(from);
			Long toId = getConceptForCui(to);
			Iterable<Path> paths = db.getTraversalRepository().findShortestPath(fromId, toId, max);
			boolean exists = false;
			if (paths != null) {
				for (Path path : paths) {
					exists = true;
					break;
				}
			}
			pathExistsMap.put(from + to, exists);
			return exists;
		}
	}

	public Long getConceptForCui(String cui) {
		if (cuiMap.containsKey(cui)) {
			return cuiMap.get(cui);
		} else {
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

			cuiMap.put(cui, foundConcept.getId());
			return foundConcept.getId();
		}
	}

}
