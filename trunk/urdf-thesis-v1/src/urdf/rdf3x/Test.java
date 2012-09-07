package urdf.rdf3x;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;




public class Test {

	/**
	 * @param args
	 */
	static Connection conn;
	static Statement stmt;
	
	static void check(String q) throws SQLException {
		System.out.println(q);
		long t1 = System.currentTimeMillis();
		ResultSet rs = (ResultSet) stmt.executeQuery(q);
		long t2 = System.currentTimeMillis();
		//while (rs.next());
		rs.last();
		long t3 = System.currentTimeMillis();
		System.out.println((t3-t1) + "=" + (t3-t2) + "+" + (t2-t1));
		
	}
	
	public static void main(String[] args) {
//		Driver 		drvr = new Driver();
		Properties 	info = new Properties();
		info.put("DIR", "/home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
		try {
			conn = Driver.connect("src/rdf3x.properties");
			stmt = (Statement) conn.createStatement();
			check("SELECT COUNT ?D WHERE {{?A <http://yago-knowledge.org/resource/isMarriedTo> ?C . ?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D } match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }} ORDER BY ASC(?D) DESC(?match)");
			check("SELECT COUNT ?D WHERE {{?A <http://yago-knowledge.org/resource/isMarriedTo> ?C . ?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D } match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNT ?D WHERE {?A <http://yago-knowledge.org/resource/isMarriedTo> ?C . ?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D . ?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }");
			check("SELECT COUNT ?D WHERE {?A <http://yago-knowledge.org/resource/isMarriedTo> ?C . ?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D }");
			check("SELECT COUNT ?D WHERE {{?A <http://yago-knowledge.org/resource/isMarriedTo> ?C . ?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D } match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }} ORDER BY ASC(?D) DESC(?match)");
			
			System.out.println("=========================================================================");
			check("SELECT COUNT ?match WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNT ?C WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C } ORDER BY DESC(count) LIMIT 10");
			check("SELECT COUNT ?C WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }} ORDER BY ASC(?C) DESC(?match)");
			check("SELECT COUNT ?C WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C . ?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C}");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> <http://yago-knowledge.org/resource/Berlin> . ?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> <http://yago-knowledge.org/resource/Berlin>}");
			check("SELECT COUNT ?D ?E WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D . ?B <http://yago-knowledge.org/resource/hasEconomicGrowth> ?E} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }} ORDER BY ASC(?D) ASC(?E) DESC(?match)");
			System.out.println("-------");
			check("SELECT COUNT ?match WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNT ?C WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C } ORDER BY DESC(count) LIMIT 10");
			check("SELECT COUNT ?C WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }} ORDER BY ASC(?C) DESC(?match)");
			check("SELECT COUNT ?C WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }}");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C . ?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C}");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> <http://yago-knowledge.org/resource/Berlin> . ?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }");
			check("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/livesIn> ?B . ?A <http://yago-knowledge.org/resource/diedIn> <http://yago-knowledge.org/resource/Berlin>}");
			check("SELECT COUNT ?D ?E WHERE {{?A <http://yago-knowledge.org/resource/livesIn> ?B . ?B <http://yago-knowledge.org/resource/hasGDP> ?D . ?B <http://yago-knowledge.org/resource/hasEconomicGrowth> ?E} match {?A <http://yago-knowledge.org/resource/isCitizenOf> ?B . }} ORDER BY ASC(?D) ASC(?E) DESC(?match)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
