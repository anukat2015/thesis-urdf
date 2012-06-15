package urdf.rdf3x;

import java.sql.SQLException;
import java.util.Properties;




public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Driver 		drvr = new Driver();
		Properties 	info = new Properties();
		info.put("DIR", "/home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
		try {
			Connection conn = (Connection) drvr.connect("rdf3x:///home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin/db", info);
			Statement stmt = (Statement) conn.createStatement();
			String query = "select distinct ?x where {{?x <http://www.w3.org/2000/01/rdf-schema#domain> ?y} UNION {?y <http://www.w3.org/2000/01/rdf-schema#domain> ?x}}";
			ResultSet rs = (ResultSet) stmt.executeQuery(query);
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
					
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
