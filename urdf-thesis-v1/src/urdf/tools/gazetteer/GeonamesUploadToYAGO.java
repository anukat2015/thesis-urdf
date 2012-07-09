package urdf.tools.gazetteer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class GeonamesUploadToYAGO {

	static int fid = 10000000;

	public static void main(String[] args) throws Exception {

		// args = new String[2];
		// args[0] = "D:\\URDF\\trunk\\src\\gazetter\\sampledata\\geosmall.txt";
		// args[1] = "D:\\URDF\\trunk\\src\\gazetter\\sampledata\\countryregioncodes.txt";

		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		Driver driver = (Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		DriverManager.registerDriver(driver);

		Connection connection = DriverManager
		    .getConnection(
		        "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=infao5501)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=oracle)(SERVER=DEDICATED)))",
		        "yago", "yago");

		PreparedStatement pstmt1 = connection.prepareStatement("INSERT INTO FACTS2 (ID, RELATION, ARG1, ARG2, CONFIDENCE) VALUES (?, ?, ?, ?, ?)");

		PreparedStatement pstmt2 = connection.prepareStatement("INSERT INTO GAZ_DATA2 (UFI, LAT, LON, FULL_NAME) " + "VALUES (?, ?, ?, ?)");

		//PreparedStatement pstmt3 = connection
		//   .prepareStatement("UPDATE GAZ_DATA2 SET SDO_GEOMETRY = MDSYS.SDO_GEOMETRY(2001, NULL, MDSYS.SDO_POINT_TYPE(?, ?, NULL), NULL, NULL) WHERE UFI = ?");

		long c = 0;

		GeoFIPSCodes gfc = new GeoFIPSCodes(args[1]);
		gfc.init();

		String line = reader.readLine();
		line = reader.readLine();

		// while ((line = reader.readLine()) != null && ++c < 2070000)
		// ;

		while ((line = reader.readLine()) != null) {
			// System.out.println(line);

			String tokens[] = line.split("\t");
			if (tokens[9].compareTo("PCLI") == 0 || tokens[9].compareTo("AMD1") == 0)
				continue;

			extractRelations(pstmt1, gfc, tokens);
			insertGeoData(pstmt2, tokens);

			if (++c % 1000 == 0) {
				try {
					pstmt1.executeBatch();
					pstmt2.executeBatch();
					connection.commit();
					System.out.println("INSERT " + c);
				} catch (Exception e) {
					System.out.println("Exception for " + c + " > " + line);
					pstmt1.clearBatch();
					pstmt2.clearBatch();
				}
			}
		}

		pstmt1.executeBatch();
		pstmt2.executeBatch();
		connection.commit();

		pstmt1.close();
		pstmt2.close();
		/*
		 * reader.close();
		 * 
		 * reader = new BufferedReader(new FileReader(args[0]));
		 * 
		 * c = 0; line = reader.readLine(); while ((line = reader.readLine()) != null) { try { String tokens[] = line.split("\t"); try { double d1=
		 * Double.parseDouble(tokens[3]), d2 =Double.parseDouble(tokens[4]); int i1 = Integer.parseInt(tokens[1]); pstmt3.setDouble(1, d1); pstmt3.setDouble(2, d2);
		 * pstmt3.setInt(3, i1); } catch (NumberFormatException n) { n.printStackTrace(); } pstmt3.addBatch(); if (++c % 1000 == 0) { pstmt3.executeBatch();
		 * connection.commit(); System.out.println("UDPATE GEO " + c); } } catch (Exception e) { System.out.println("Exception for " + c + " > " + line);
		 * pstmt3.clearBatch(); } }
		 * 
		 * pstmt3.executeBatch(); connection.commit(); pstmt3.close();
		 */
		connection.close();
		reader.close();
	}

	private static void insertGeoData(PreparedStatement stmt, String[] tokens) throws Exception {
		String name = GeoFIPSCodes.normalize(tokens[24]);
		if (name.length() > 150)
			return;

		stmt.setInt(1, Integer.parseInt(tokens[1]));
		stmt.setDouble(2, Double.parseDouble(tokens[3]));
		stmt.setDouble(3, Double.parseDouble(tokens[4]));
		stmt.setString(4, name);
		// stmt.setString(5, null);
		stmt.addBatch();
	}

	private static void extractRelations(PreparedStatement stmt, GeoFIPSCodes gfc, String[] tokens) throws Exception {
		String id = tokens[1];
		String name = GeoFIPSCodes.normalize(tokens[24]);
		if (name.length() > 150)
			return;

		String lat1 = tokens[3];
		String lon1 = tokens[4];
		String country = gfc.getPlaceName(tokens[12]);
		String continent = gfc.getPlaceName(tokens[0]);
		String state = gfc.getPlaceName(tokens[12] + tokens[13]);

		setValues(stmt, "gaz_hasName", id, name);
		setValues(stmt, "gaz_hasLatitude", id, lat1);
		setValues(stmt, "gaz_hasLongitude", id, lon1);
		if (country != null)
			setValues(stmt, "gaz_isLocatedIn", id, country);
		if (continent != null)
			setValues(stmt, "gaz_isLocatedIn", id, continent);
		if (state != null)
			setValues(stmt, "gaz_isLocatedIn", id, state);
	}

	private static void setValues(PreparedStatement stmt, String relation, String arg1, String arg2) throws Exception {
		stmt.setLong(1, fid++);
		stmt.setString(2, relation);
		stmt.setString(3, arg1);
		stmt.setString(4, arg2);
		stmt.setFloat(5, 1);
		stmt.addBatch();
	}
}
