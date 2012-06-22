package urdf.rdf3x;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;
import urdf.rdf3x.Driver;
import urdf.rdf3x.Connection;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;




public class CreateStatistics {
    private static final String sparqlMult1  = "select count ?arg1 where {?arg1 ?rel ?arg2}";
    private static final String sparqlMult2  = "select count ?arg2 where {?arg1 ?rel ?arg2}";
    private static final String sparqlN      = "select count ?x where {?arg1 ?rel ?arg2}";
    
    private static final String sparqlInsert = "insert data { ?rel ?stat ?val . }";
    
    private static final String hasMult1 = "<http://yago-knowledge.org/resource/hasArg1Mult>";
    private static final String hasMult2 = "<http://yago-knowledge.org/resource/hasArg2Mult>";
    private static final String hasN = "<http://yago-knowledge.org/resource/numberOfFacts>";
    /**
     * @param args
     */
    public static void LoadStatistics(String dbPropsPath, String sparqlPath) {
    	try {
 			Properties props = new Properties();
			props.load(new FileInputStream(new File(dbPropsPath)));
			Driver drvr = new urdf.rdf3x.Driver();
     		String db = (String) props.get("Database");
            Connection conn = (Connection) drvr.connect(db, props);
            LoadStatistics(conn, sparqlPath);
            conn.commit();
            conn.close();
    	}
    	catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void LoadStatistics(Connection conn, String sparqlPath) {
		try {
	    	Statement stmt;
			stmt = (Statement) conn.createStatement();
		   	File f = new File(sparqlPath);
	        if (f.exists()) {
	            BufferedReader br = new BufferedReader(new FileReader(f));
	            String line = null;
	            while ((line=br.readLine())!=null) {
	             	stmt.executeQuery(line);
	            }
	        }
	        conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
 
    }
    
    public static void LoadStatistics(Properties dbProps) {
    	try {
     		Driver drvr = new urdf.rdf3x.Driver();
     		String db = (String) dbProps.get("Database");
            Connection conn = (Connection) drvr.connect(db, dbProps);
            Statement stmt = (Statement) conn.createStatement();
            String query = "select distinct ?p where {?s ?p ?o}";
            float mult; int facts; int rows;

        	ResultSet relations = (ResultSet) stmt.executeQuery(query);
            while (relations.next()) {
                String relation = relations.getString(1);
                // Mult1
                String queryMult1 = sparqlMult1.replace("?rel", relation);
                ResultSet resultMult1 = (ResultSet) stmt.executeQuery(queryMult1);
                String mul; facts = 0; rows = 0;
                while (resultMult1.next()) {
                    rows++;
                    facts += Integer.parseInt(resultMult1.getString(2));
                }
                float x1 = ((float) facts) /((float) rows);
                mul = "\"" + Float.toString(x1) + "\"";
                String insertMult1 = sparqlInsert.replace("?rel", relation)
                                                 .replace("?stat", hasMult1)
                                                 .replace("?val", mul);
                stmt.executeQuery(insertMult1);
  
                
                // Mult2
                String queryMult2 = sparqlMult2.replace("?rel", relation);
                ResultSet resultMult2 = (ResultSet) stmt.executeQuery(queryMult2);
                mul = null; facts = 0; rows = 0;
                while (resultMult2.next()) {
                    rows++;
                    facts += Integer.parseInt(resultMult2.getString(2));
                }
                float x2 = ((float) facts) /((float) rows);
                mul = "\"" + Double.toString(x2) + "\"";
                String insertMult2 = sparqlInsert.replace("?rel", relation)
                                                 .replace("?stat", hasMult2)
                                                 .replace("?val", mul);
                stmt.executeQuery(insertMult2);
                
                // Number of Facts
                mul = "\"" + Integer.toString(facts) + "\"";
                String insertN = sparqlInsert.replace("?rel", relation)
	                                         .replace("?stat", hasN)
	                                         .replace("?val", mul);
                stmt.executeQuery(insertN);
            }
            conn.commit();
            conn.close();
                                
        } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }
    
    public static void LoadStatistics(String dbPropsPath) {
        // TODO Auto-generated method stub      	
 		try {
 			Properties props = new Properties();
			props.load(new FileInputStream(new File(dbPropsPath)));
			LoadStatistics(props);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    		
    }
    
    public static void main(String[] args) {
    	try {
 			Properties props = new Properties();
			props.load(new FileInputStream(new File("src/rdf3x.properties")));
			Driver drvr = new urdf.rdf3x.Driver();
     		String db = (String) props.get("Database");
            Connection conn = (Connection) drvr.connect(db, props);
            LoadStatistics(conn, "src/insert-stats-rdf3x");
            Statement stmt = (Statement) conn.createStatement();
    		String sparql = "SELECT ?r ?m1 ?m2 ?n ?domain ?range where {?r <http://yago-knowledge.org/resource/hasArg1Mult> ?m1 . " +
					   "?r <http://yago-knowledge.org/resource/hasArg2Mult> ?m2 . " +
					   "?r <http://yago-knowledge.org/resource/numberOfFacts> ?n . " +
					   "?r <http://www.w3.org/2000/01/rdf-schema#range> ?range . " +
					   "?r <http://www.w3.org/2000/01/rdf-schema#domain> ?domain}";
            ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
            while (rs.next()) {
            	System.out.println(rs.getString(1));
            	System.out.println(rs.getString(2));
            	System.out.println(rs.getString(3));
            	System.out.println(rs.getString(4));
            	System.out.println(rs.getString(5));
            	System.out.println(rs.getString(6));
            	System.out.println();
            }
            
            sparql = "select ?sub ?sup where {?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup}";
    		
    	    rs = (ResultSet) stmt.executeQuery(sparql);
            while (rs.next()) {
            	System.out.println(rs.getString(1));
            	System.out.println(rs.getString(2));
            	System.out.println();
            }
    	}
    	catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}