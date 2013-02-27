/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.trec.report;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scala.actors.threadpool.Arrays;
import scala.collection.parallel.ParIterableLike;

/**
 * 
 * @author bhsingh
 */
public class ReportSearch {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	private Map<String, List<String>> positiveConcepts = new HashMap<String, List<String>>();
	private Map<String, List<String>> negativeConcepts = new HashMap<String, List<String>>();

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://10.218.27.190/trec_med_rec_2011?"
				+ "user=root&password=21biosemantiek!?");
	}

	public ReportSearch() throws FileNotFoundException, IOException {
		CSVReader csvReader = new CSVReader(new FileReader(CONCEPTS_FILE));
		List<String[]> sentences = csvReader.readAll();
		for (String[] columns : sentences) {
			List<String> pos = new ArrayList<String>();
			List<String> neg = new ArrayList<String>();
			String checksum = columns[0];
			if (columns.length > 3) {
				String[] concepts = (String[]) Arrays.copyOfRange(columns, 3, columns.length);
				for (String strConcept : concepts) {
					String[] strColumns = strConcept.split("\\|");
					if (Integer.valueOf(strColumns[1]) == 0) {
						pos.add(strColumns[0]);
					} else {
						neg.add(strColumns[0]);
					}
				}

			}
			List<String> posConcepts;
			if (positiveConcepts.containsKey(checksum)) {
				posConcepts = positiveConcepts.get(checksum);
			} else {
				posConcepts = new ArrayList<String>();
			}
			posConcepts.addAll(pos);
			positiveConcepts.put(checksum, posConcepts);

			List<String> negConcepts;
			if (negativeConcepts.containsKey(checksum)) {
				negConcepts = negativeConcepts.get(checksum);
			} else {
				negConcepts = new ArrayList<String>();
			}
			negConcepts.addAll(neg);
			negativeConcepts.put(checksum, negConcepts);
		}
		csvReader.close();
	}

	public List<String> getReportsForVisit(String visit) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement ps = connection.prepareStatement(GET_CHECKSUM_SQL);
		ps.setString(1, visit);
		ResultSet rs = ps.executeQuery();
		List<String> reports = new ArrayList<String>();
		try {
			while (rs.next()) {
				reports.add(rs.getString("checksum"));
			}
		} finally {
			rs.close();
			ps.close();
			connection.close();
		}
		return reports;
	}
	
	public List<String> getPositiveCuisForReport(String checksum){
		return positiveConcepts.get(checksum);
	}

	private static final String GET_CHECKSUM_SQL = "select checksum from report_mapping where visitid = ?";
	private static final String CONCEPTS_FILE = "/home/bhsingh/Public/concept.txt";
}
