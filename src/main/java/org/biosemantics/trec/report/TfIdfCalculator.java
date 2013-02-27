package org.biosemantics.trec.report;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import opennlp.tools.util.InvalidFormatException;
import org.biosemantics.utility.peregrine.PeregrineRmiClient;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TfIdfCalculator {

    private TrecDatabaseUtility trecDatabaseUtility;
    private ReportUtility reportUtility;
    private Peregrine peregrine;
    private static final Logger logger = LoggerFactory.getLogger(TfIdfCalculator.class);

    public TfIdfCalculator() throws SQLException, InvalidFormatException, IOException {
        trecDatabaseUtility = new TrecDatabaseUtility();
        reportUtility = new ReportUtility();
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{"/org/biosemantics/utility/peregrine/peregrine-utility-context.xml"});
        peregrine = ((PeregrineRmiClient) appContext.getBean("peregrineRmiClient")).getPeregrine();
    }

    public void calculateTfIdf() throws SQLException, IOException {

        Set<String> visits = trecDatabaseUtility.getAllUniqueVisits();
        Map<String, Integer> conceptCountMap = new HashMap<String, Integer>();
        Map<String, Map<String, Integer>> visitConceptFrequencyMap = new HashMap<String, Map<String, Integer>>();
         Set<String> allUniqueConcepts = new HashSet<String>();
        float totalVisits = visits.size();
        logger.debug("total visits {}", totalVisits);
        int ctr = 0;
        for (String visit : visits) {
            Set<String> reports = trecDatabaseUtility.getReportsForVisit(visit);
            Map<String, Integer> conceptFrequencyMap = new HashMap<String, Integer>();
            Set<String> allUniqueConceptsForVisit = new HashSet<String>();
            for (String report : reports) {
                String reportText = trecDatabaseUtility.getReportText(report);
                List<String> sentences = reportUtility.getFormattedSentences(reportText);
                for (String sentence : sentences) {
                    List<IndexingResult> indexingResults = peregrine.index(sentence, Language.EN);
                    for (IndexingResult indexingResult : indexingResults) {
                        String concept = String.format("C%07d", (Integer) indexingResult.getTermId().getConceptId());
                        allUniqueConceptsForVisit.add(concept);
                        allUniqueConcepts.add(concept);
                        int frequency = 0;
                        if (conceptFrequencyMap.containsKey(concept)) {
                            frequency = conceptFrequencyMap.get(concept);
                        }
                        conceptFrequencyMap.put(concept, ++frequency);
                    }
                }
            }
            for (String cui : allUniqueConceptsForVisit) {
                int count = 0;
                if (conceptCountMap.containsKey(cui)) {
                    count = conceptCountMap.get(cui);
                }
                conceptCountMap.put(cui, ++count);
            }
            visitConceptFrequencyMap.put(visit, conceptFrequencyMap);
            logger.debug("{}", ++ctr);
        }
        Map<String, Double> idfMap = new HashMap<String, Double>();
        for (String cui : allUniqueConcepts) {
            float count = Float.MIN_VALUE;
            if (conceptCountMap.containsKey(cui)){
                count = conceptCountMap.get(cui);
            }
            double idf = Math.log(totalVisits / count);
            idfMap.put(cui, idf);
        }
        CSVWriter cSVWriter = new CSVWriter(new FileWriter("/tfidf.txt"));
        for (Entry<String, Map<String, Integer>> entry : visitConceptFrequencyMap.entrySet()) {
            for (Entry<String, Integer> innerEntry : entry.getValue().entrySet()) {
               Double idf = idfMap.get(innerEntry.getKey());
               double tf = innerEntry.getValue();
               double tfidf = tf * idf;
               cSVWriter.writeNext(new String[]{entry.getKey(), innerEntry.getKey(), String.valueOf(tfidf)});
            }
        }
        cSVWriter.flush();
        cSVWriter.close();
    }
}
