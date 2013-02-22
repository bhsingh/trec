package org.biosemantics.trec.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biosemantics.trec.opennlp.SentenceSplitterOpennlpImpl;
import org.biosemantics.utility.peregrine.PeregrineRmiClient;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ReportParser {
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://localhost/trec2011?" + "user=root&password=");
	}

	public void reportsToSentences() throws SQLException, IOException {
		SentenceSplitterOpennlpImpl splitter = new SentenceSplitterOpennlpImpl(
				"/Users/bhsingh/code/git/trec/src/main/resources/en-sent.bin");
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery(REPORT_SQL);
		System.out.println("read all reports");
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(SENTENCE_FILE)));
		CSVWriter headingsWriter = new CSVWriter(new FileWriter(new File(HEADINGS_FILE)));
		int ctr = 0;
		try {
			while (rs.next()) {
				int senCtr = 1;
				String checksum = rs.getString("checksum");
				String reportText = rs.getString("report_text");
				String[] sentences = splitter.split(reportText);
				for (String sentence : sentences) {
					if (sentence.contains("Safe-harbor compliant")) {
						// 2955530 sentence.txt
						continue;
					}
					if (sentence.contains("Electronically Signed by")) {
						// 2933893 sentence.txt
						continue;
					}
					if (sentence.contains("Dictated by")) {
						// 2933893 sentence.txt
						continue;
					}
					if (sentence.contains("Thank you")) {
						// 2928558 sentence.txt
						continue;
					}
					if (sentence.contains("____________________________")) {
						// 2895847 sentence.txt
						continue;
					}
					if (sentence.contains("CARBON-COPY")) {
						// 2892743 sentence.txt
						continue;
					}
					if (sentence.contains("END OF IMPRESSION")) {
						// 2848309 sentence.txt
						continue;
					}

					String formattedSentence = sentence.replaceAll("\\s+", " ");
					Matcher matcher = pattern.matcher(formattedSentence);
					while (matcher.find()) {
						System.out.println(matcher.group());
						headingsWriter.writeNext(new String[] { checksum, String.valueOf(senCtr), matcher.group() });
					}
					csvWriter.writeNext(new String[] { checksum, String.valueOf(senCtr), formattedSentence });
					senCtr++;
				}

				System.out.println(ctr++);
			}
		} finally {
			rs.close();
			statement.close();
			connection.close();
			splitter.destroy();
			csvWriter.flush();
			csvWriter.close();
			headingsWriter.flush();
			headingsWriter.close();
		}
	}

	public void indexSentences() throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(SENTENCE_FILE));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(CONCEPTS_FILE)));
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "/org/biosemantics/utility/peregrine/peregrine-utility-context.xml" });
		PeregrineRmiClient peregrineRmiClient = (PeregrineRmiClient) appContext.getBean("peregrineRmiClient");
		Peregrine peregrine = peregrineRmiClient.getPeregrine();
		List<String[]> lines = csvReader.readAll();
		List<String> output = new ArrayList<String>();
		int ctr = 0;
		for (String[] columns : lines) {
			String text = columns[2];
			output.add(columns[0]);
			output.add(columns[1]);
			List<IndexingResult> indexingResults = peregrine.index(text, Language.EN);
			for (IndexingResult indexingResult : indexingResults) {
				output.add(String.format("C%07d", (Integer) indexingResult.getTermId().getConceptId()));
			}
			csvWriter.writeNext(output.toArray(new String[output.size()]));
			System.out.println(++ctr);
		}
		csvWriter.flush();
		csvWriter.close();
		csvReader.close();

	}

	public static void main(String[] args) throws SQLException, IOException {
		ReportParser reportParser = new ReportParser();
		reportParser.indexSentences();
	}

	private static final String REPORT_SQL = "select checksum, report_text from report";
	private static final String SENTENCE_FILE = "/Users/bhsingh/code/data/trec2013/sentence.txt";
	private static final String HEADINGS_FILE = "/Users/bhsingh/code/data/trec2013/heading.txt";
	private static final String CONCEPTS_FILE = "/Users/bhsingh/code/data/trec2013/concept.txt";
	Pattern pattern = Pattern.compile("([A-Z]+\\s*?)+:");
}
