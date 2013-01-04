package urdf.ilp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Properties;

import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;




public class CreateStatistics {
    private static final String sparqlMult1  = "select count ?arg1 where {?arg1 ?rel ?arg2}";
    private static final String sparqlMult2  = "select count ?arg2 where {?arg1 ?rel ?arg2}";
    private static final String sparqlInsert = "insert data { ?rel ?stat ?val . }";
    
    private static final String hasMult1 = "<http://yago-knowledge.org/resource/hasArg1Mult>";
    private static final String hasMult2 = "<http://yago-knowledge.org/resource/hasArg2Mult>";
    private static final String hasVar1 = "<http://yago-knowledge.org/resource/hasArg1Var>";
    private static final String hasVar2 = "<http://yago-knowledge.org/resource/hasArg2Var>";
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
    
    public static void LoadStatistics(Connection conn, FileOutputStream fos) {
    	try {
    		PrintWriter out = new PrintWriter(fos);
            Statement stmt = (Statement) conn.createStatement();
            String query = "select distinct ?p where {?s ?p ?o}";
            int facts; int rows;
            
            String queryMult1, queryMult2, insertMult1, insertMult2, insertVar1, insertVar2, insertN;

        	ResultSet relations = (ResultSet) stmt.executeQuery(query);
            while (relations.next()) {
                String relation = relations.getString(1);
                // Mult1
                queryMult1 = sparqlMult1.replace("?rel", relation);
                ResultSet resultMult1 = (ResultSet) stmt.executeQuery(queryMult1);
                float avg = 0, var = 0;
                String avgMul, varMul; 
                facts = 0; rows = 0;
                
                while (resultMult1.next()) { // calculate Mult Arg1
                    rows++;
                    avg += resultMult1.getFloat(2);
                    facts += resultMult1.getInt(2);
                }
                avg /= (float) rows;
                
                resultMult1.beforeFirst();
                while (resultMult1.next())  {// calculate Var Mult Arg1
                    var += Math.pow((resultMult1.getFloat(2) - avg),2);
                }
                var /= (float) rows;
                
                
                avgMul = "\"" + Float.toString(avg) + "\"";
                varMul = "\"" + Float.toString(var) + "\"";
                
                insertMult1 = sparqlInsert.replace("?rel", relation)
                                          .replace("?stat", hasMult1)
                                          .replace("?val", avgMul);
                stmt.executeQuery(insertMult1);
                out.write(insertMult1+"\n");
                
                insertVar1 = sparqlInsert.replace("?rel", relation)
					                      .replace("?stat", hasVar1)
					                      .replace("?val", varMul);
                stmt.executeQuery(insertVar1);
                out.write(insertVar1+"\n");
  
                
                // Mult2
                queryMult2 = sparqlMult2.replace("?rel", relation);
                ResultSet resultMult2 = (ResultSet) stmt.executeQuery(queryMult2);
                avg = 0; rows = 0;
                
                while (resultMult2.next()) { // calculate Mult Arg2
                    rows++;
                    avg += resultMult2.getFloat(2);
                }
                avg /= (float) rows;
                
                resultMult2.beforeFirst();
                while (resultMult2.next()) { // calculate Var Mult Arg2
                    var += Math.pow((resultMult2.getFloat(2) - avg),2);
                }
                var /= (float) rows;
                
                avgMul = "\"" + Float.toString(avg) + "\"";
                varMul = "\"" + Float.toString(var) + "\"";
                
                insertMult2 = sparqlInsert.replace("?rel", relation)
                                          .replace("?stat", hasMult2)
                                          .replace("?val", avgMul);
                stmt.executeQuery(insertMult2);
                out.write(insertMult2+"\n");
                
                insertVar2 = sparqlInsert.replace("?rel", relation)
							              .replace("?stat", hasVar2)
							              .replace("?val", varMul);
                stmt.executeQuery(insertVar2);
                out.write(insertVar2+"\n");
                
                // Number of Facts
                String nFacts = "\"" + Integer.toString(facts) + "\"";
                insertN = sparqlInsert.replace("?rel", relation)
	                                         .replace("?stat", hasN)
	                                         .replace("?val", nFacts);
                stmt.executeQuery(insertN);
                out.write(insertN+"\n");
            }
            out.flush();
            out.close();
            fos.close();
            conn.commit();
            conn.close();
                                
        }
        catch (SQLException e) {
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
 			File f = new File("src/rdf3x.properties");
			props.load(new FileInputStream(f));
			Driver drvr = new urdf.rdf3x.Driver();
     		String db = (String) props.get("Database");
            Connection conn = (Connection) drvr.connect(db, props);
            LoadStatistics(conn, new FileOutputStream("src/insert-rdf3x-dbpedia-stat.sparql"));
            /*Statement stmt = (Statement) conn.createStatement();
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
            }*/
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