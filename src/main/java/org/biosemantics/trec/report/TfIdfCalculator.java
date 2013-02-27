package org.biosemantics.trec.report;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TfIdfCalculator {

	private TrecDatabaseUtility trecDatabaseUtility;

	public TfIdfCalculator() {
		trecDatabaseUtility = new TrecDatabaseUtility();
	}

	public void calculateTfIdf() throws SQLException {
     Set<> trecDatabaseUtility.getAllUniqueVisits();
	}

}
