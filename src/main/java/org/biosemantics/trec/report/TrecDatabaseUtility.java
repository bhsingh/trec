package org.biosemantics.trec.report;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class TrecDatabaseUtility {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://10.218.27.190/trec_med_rec_2011?"
				+ "user=root&password=21biosemantiek!?");
	}

	private Connection textConnection;
	private PreparedStatement textPreparedStatement;
	private Connection reportConnection;
	private PreparedStatement reportPreparedStatement;

	public TrecDatabaseUtility() throws SQLException {
		textConnection = getConnection();
		textPreparedStatement = textConnection.prepareStatement(REPORT_TEXT_SQL);
		reportConnection = getConnection();
		reportPreparedStatement = textConnection.prepareStatement(GET_CHECKSUM_SQL);
	}

	public Set<String> getReportsForVisit(String visit) throws SQLException {
		reportPreparedStatement.setString(1, visit);
		ResultSet rs = reportPreparedStatement.executeQuery();
		Set<String> reports = new HashSet<String>();
		try {
			while (rs.next()) {
				reports.add(rs.getString("checksum"));
			}
		} finally {
			rs.close();
		}
		return reports;
	}

	public Set<String> getAllUniqueVisits() throws SQLException {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery(ALL_VISITS_SQL);
		Set<String> visits = new HashSet<String>();
		try {
			while (rs.next()) {
				visits.add(rs.getString("visitid"));
			}
		} finally {
			rs.close();
			statement.close();
			connection.close();
		}
		return visits;
	}

	public String getReportText(String checksum) throws SQLException {
		textPreparedStatement.setString(1, checksum);
		ResultSet rs = textPreparedStatement.executeQuery();
		String text = null;
		try {
			while (rs.next()) {
				text = rs.getString("report_text");
			}
		} finally {
			rs.close();
		}
		return text;
	}

	public void destroy() throws SQLException {
		textPreparedStatement.close();
		textConnection.close();
		reportPreparedStatement.close();
		reportConnection.close();
	}

	private static final String GET_CHECKSUM_SQL = "select checksum from report_mapping where visitid = ?";
	private static final String ALL_VISITS_SQL = "select distinct(visitid) from report_mapping_updated";
	private static final String REPORT_TEXT_SQL = "select report_text from report where checksum = ?";
}
