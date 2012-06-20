package urdf.rdf3x;

import java.sql.SQLException;
import java.util.Properties;




public class CreateStatistics {
	private static final String sparqlMult1  = "select count ?arg1 where {?arg1 ?rel ?arg2}";
	private static final String sparqlMult2  = "select count ?arg2 where {?arg1 ?rel ?arg2}";
	private static final String sparqlN 	 = "select count ?x where {?arg1 ?rel ?arg2}";
	
	private static final String sparqlInsert = "insert data {?rel ?mul ?val .}";
	
	private static final String hasMult1 = "<http://yago-knowledge.org/resource/hasArg1Mult>";
	private static final String hasMult2 = "<http://yago-knowledge.org/resource/hasArg2Mult>";
	private static final String hasN = "<http://yago-knowledge.org/resource/numberOfFacts>";
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
			String query = "select distinct ?p where {?s ?p ?o}";
			float mult; int facts; int rows;
			ResultSet relations = (ResultSet) stmt.executeQuery(query);
			while (relations.next()) {
				String relation = relations.getString(1);
				
				// Mult1
				String queryMult1 = sparqlMult1.replace("?rel", relation);
				ResultSet resultMult1 = (ResultSet) stmt.executeQuery(queryMult1);
				mult = 0; facts = 0; rows = 0;
				while (resultMult1.next()) {
					rows++;
					facts += resultMult1.getInt(2);
				}
				mult = facts/rows;
				String insertMult1 = sparqlInsert.replace("?rel", relation)
												 .replace("?mul", hasN)
												 .replace("?val", Integer.toString(facts));
				stmt.executeQuery(insertMult1);
				
				// Mult2
				String queryMult2 = sparqlMult2.replace("?rel", relation);
				ResultSet resultMult2 = (ResultSet) stmt.executeQuery(queryMult2);
				mult = 0; facts = 0; rows = 0;
				while (resultMult2.next()) {
					rows++;
					facts += resultMult2.getInt(2);
				}
				mult = facts/rows;
				String insertMult2 = sparqlInsert.replace("?rel", relation)
												 .replace("?mul", hasMult2)
												 .replace("?val", Double.toString(mult));
				stmt.executeQuery(insertMult2);
				
				// Number of Facts
				String insertN = sparqlInsert.replace("?rel", relation)
											 .replace("?mul", hasMult2)
											 .replace("?val", Integer.toString(facts));
				stmt.executeQuery(insertMult2);
			}
					
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
