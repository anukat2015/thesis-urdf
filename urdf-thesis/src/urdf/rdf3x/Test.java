package urdf.rdf3x;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Properties;




public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Driver 		drvr = new Driver();
		Properties 	info = new Properties();
		info.put("DIR", "/home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
		try {
			Connection conn = Driver.connect("src/rdf3x.properties");
			Statement stmt = (Statement) conn.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQueryCountRows("SELECT DISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?free . ?A <http://yago-knowledge.org/resource/wasBornOnDate> ?C . ?B <http://yago-knowledge.org/resource/wasCreatedOnDate> ?C }");
			rs.next();
			System.out.println(rs.getInt(1));	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
