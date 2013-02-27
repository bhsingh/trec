package org.biosemantics.trec.report;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		return DriverManager.getConnection("jdbc:mysql://localhost/trec2011?" + "user=root&password=");
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

	private static final String GET_CHECKSUM_SQL = "select checksum from report_mapping where visitid = ?";
	private static final String ALL_VISITS_SQL = "select distinct(visitid) from report_mapping_updated";

}
