/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.trec.pipe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.biosemantics.conceptstore.client.GraphDbInitializer;
import org.biosemantics.conceptstore.domain.Concept;
import org.biosemantics.conceptstore.domain.Notation;
import org.biosemantics.trec.report.ReportSearch;
import org.neo4j.graphdb.Path;

public class ShortestPathImpl {

    private static final String GRAPH_DB = "/media/ssd/bhsingh/graph.db";
    private static GraphDbInitializer db;
    private static ReportSearch reportSearch;
    private static final String[] topicCuis = new String[]{"C0011053", "C0018772", "C1384666", "C2029884"};
    private static final String[] visits = new String[]{"+SKG7tCxA/zR", "+W4IQBZ/eY/Q", "u5Vz3dfoZa1d"};//rel , not, partially


    public void init() throws IOException {
        db = new GraphDbInitializer(GRAPH_DB);
        reportSearch = new ReportSearch();
    }

    public float calculatePathScore(String topicCui, List<String> reportCuis) {
        Concept topic = getConceptForCui(topicCui);
        float finalScore = 0F;
        for (String reportCui : reportCuis) {
            Concept reportConcept = getConceptForCui(reportCui);
            Iterable<Path> paths = db.getTraversalRepository().findShortestPath(topic.getId(), reportConcept.getId(), 4);
            int pathCount = 0;
            int pathLength = 0;
            for (Path path : paths) {
                ++pathCount;
                if (pathLength == 0) {
                    pathLength = path.length();
                }
            }
            float inversePathCount = 1 / pathCount;
            float score = pathLength * inversePathCount;
            finalScore += score;
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
    
    public static void main(String[] args) throws IOException, SQLException {
        ShortestPathImpl impl = new ShortestPathImpl();
        impl.init();
        for (String visit : visits) {
            List<String> reports = reportSearch.getReportsForVisit(visit);
            for (String cui : topicCuis) {
                float score = impl.calculatePathScore(cui, reports);
                System.out.println("visit=" + visit + " cui=" + cui + " score=" + score);
            }
        }
    }
}
