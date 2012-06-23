package urdf.ilp;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import basic.configuration.DBConfig;
import basic.configuration.DBConfig.DatabaseParameters;

/**
 * 	@author Christina Teflioudi
 *	The class creates sample data
 */
public class HeadSampler 
{
	private Statement stmt;
	
	private int numOfPartitions;
	private static HashSet<String> dumpRelations = new HashSet<String>();	// relations which won't participate in learning e.g. domain, range, etc
 	static {
 		//  @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
 		//  @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
 		//  @prefix y: <http://yago-knowledge.org/resource/> .
 		//  @prefix x: <http://www.w3.org/2001/XMLSchema#> .
 		//  @base <http://yago-knowledge.org/resource/> .
 
 		dumpRelations.add("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
 		dumpRelations.add("<http://www.w3.org/2000/01/rdf-schema#label>");
 		dumpRelations.add("<http://www.w3.org/2000/01/rdf-schema#domain>");
 		dumpRelations.add("<http://www.w3.org/2000/01/rdf-schema#range>");
 		dumpRelations.add("<http://www.w3.org/2000/01/rdf-schema#subPropertyOf>");
 		dumpRelations.add("<http://www.w3.org/2000/01/rdf-schema#subClassOf>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/isCalled>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/hasPreferredMeaning>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/hasPreferredName>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/hasArg1Mult>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/hasArg2Mult>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/numberOfFacts>");
 		dumpRelations.add("<http://yago-knowledge.org/resource/hasWikipediaUrl>");
 		
 		
 		dumpRelations.add("<type>");
 		dumpRelations.add("<label>");
 		dumpRelations.add("<domain>");
 		dumpRelations.add("<range>");
 		dumpRelations.add("<subPropertyOf>");
 		dumpRelations.add("<subClassOf>");
 		dumpRelations.add("<isCalled>");
 		dumpRelations.add("<hasPreferredMeaning>");
 		dumpRelations.add("<hasPreferredName>");
 		dumpRelations.add("<hasArg1Mult>");
 		dumpRelations.add("<hasArg2Mult>");
 		dumpRelations.add("<numberOfFacts>");
 		dumpRelations.add("<hasWikipediaUrl>");
 		
 		dumpRelations.add("rdf:type");
 		dumpRelations.add("rdfs:label");
 		dumpRelations.add("rdfs:domain");
 		dumpRelations.add("rdfs:range");
 		dumpRelations.add("rdfs:subPropertyOf");
 		dumpRelations.add("rdfs:subClassOf");
 		dumpRelations.add("y:isCalled");
 		dumpRelations.add("y:hasPreferredMeaning");
 		dumpRelations.add("y:hasPreferredName");
 		dumpRelations.add("y:hasArg1Mult");
 		dumpRelations.add("y:hasArg2Mult");
 		dumpRelations.add("y:numberOfFacts");
 		dumpRelations.add("y:hasWikipediaUrl");
 		
 			
 	}
	
	
	
	public static void Sample(String n3FilePath, int partitions) throws IOException {
		File inFile = new File(n3FilePath);
		String partitionPaths = n3FilePath.replaceFirst(".n3", "") + "-partition$n.n3";
		
        if (inFile.exists()) {
        	PrintWriter out[] = new PrintWriter[partitions];
        	
        	for (int i=0; i<partitions; i++) 
        		out[i] = new PrintWriter(new FileWriter(partitionPaths.replace("$n", Integer.toString(i+1))));
	
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            String line = null;
            
            int relPosInLine = 1;
            while ((line=br.readLine())!=null) {
            	line = line.trim();
            	while (!(line.endsWith(";") || line.endsWith("."))) {
            		String cont = br.readLine();
            		if (cont != null)
            			line += cont.trim();
            	}
            	if (line.startsWith("@")) 
            		for (int i=0; i<partitions; i++)
            			out[i].println(line);
            	else {
            		String relation = line.split(" ")[relPosInLine];
            		relPosInLine = (line.endsWith(".")) ? 1 : 0;	// If last line ended with ';' relation is in 1st column, if ended with "." id 2nd
            		if (!dumpRelations.contains(relation)) {
	            		int p = (int) Math.floor(Math.random() * partitions);
	            		out[p].println(line);
            		}
            	}
            }
            
            br.close();
            for (int i=0; i<partitions; i++) 
            	out[i].close();
            
        }
	}
	
	public HeadSampler(Connection conn) throws Exception {
 		this.stmt = conn.createStatement();
	}
	/**
	 * @param numOfPartitions
	 * @param recreateIfExist
	 * @param noise eg. 10 for 10% or 20 for 20%
	 * @throws SQLException
	 */
	public void sample(int numOfPartitions,boolean recreateIfExist, int noise, RelationsInfo info, int inputArg) throws SQLException
	{
		ResultSet rs;
		String existance;
		this.numOfPartitions=numOfPartitions;
		
		existance="SELECT 1 FROM user_objects WHERE object_type = 'TABLE' AND object_name='TRAIN"+inputArg+numOfPartitions+(noise==0?"'":"_"+noise+"'");
		System.out.println(existance);
		
		rs=stmt.executeQuery(existance);		
		
		
		if (!rs.next()||recreateIfExist) // if it exists
		{
			
			if (recreateIfExist)
			{
				// drop and recreate tables
				
				for (int i=1;i<=numOfPartitions;i++)
				{
					try {
						stmt.execute("DROP TABLE train"+inputArg+ i+(noise==0?"":"_"+noise));
					}
					catch(SQLException e) {
						
					}
				}
				
				
			}
			if (noise>0)	// create a new table facts_noise and use that one as base
			{
				addNoise(noise, info);
			}
			createTables(noise,inputArg);
			createIndexes(noise,inputArg);
			System.out.println("sampling done...");			
		}
		rs.close();
		System.out.println("keep previous sampling...");	
		
	}
	private void createTables(int noise, int inputArg) throws SQLException
	{
		String temp,modPart,train,baseTbl="facts"+(noise==0?"":"_"+noise);
		// create a temp table		
		temp="CREATE TABLE temp AS (SELECT rownum rnum, relation, arg"+inputArg+" FROM	(SELECT relation,arg"+inputArg+" FROM "+baseTbl+" GROUP BY relation,arg"+inputArg+" ORDER BY relation,arg"+inputArg+" ASC))";
		System.out.println(temp);
		
		String sparql = "select distinct ?rel ?arg1 where {?arg1 ?rel ?arg2} order by ?rel ?arg1 ";
		
		stmt.execute(temp);
		
		modPart="mod(rnum,"+this.numOfPartitions+")=";
		
		for (int i=1;i<=numOfPartitions;i++)
		{
			train="CREATE TABLE train"+inputArg+i+(noise==0?"":"_"+noise)+" AS (SELECT f0.relation, f0.arg1,f0.arg2 FROM facts f0,temp tbl WHERE  f0.relation=tbl.relation AND f0.arg"+inputArg+"=tbl.arg"+inputArg+" AND "+modPart+(i-1)+")";
			System.out.println(train);
			stmt.execute(train);			
		}
		
		stmt.execute("DROP TABLE temp");
	}
	private void createIndexes(int noise, int inputArg) throws SQLException
	{
		String name;
		for (int i=1;i<=numOfPartitions;i++)
		{
			name=inputArg+""+i+(noise==0?"":"_"+noise);
			
			// the training tables
			System.out.println("CREATE INDEX ind_train"+name+"_rel_arg1_arg2 ON train"+name+"(relation,arg1,arg2)");
 			stmt.execute("CREATE INDEX ind_train"+name+"_rel_arg1_arg2 ON train"+name+"(relation,arg1,arg2)");
 			
 			System.out.println("CREATE INDEX ind_train"+name+"_rel_arg2_arg1 ON train"+name+"(relation,arg2,arg1)");
 			stmt.execute("CREATE INDEX ind_train"+name+"_rel_arg2_arg1 ON train"+name+"(relation,arg2,arg1)");
 			
 			System.out.println("CREATE INDEX ind_train"+name+"_arg2_rel_arg1 ON train"+name+"(arg2,relation,arg1)");
			stmt.execute("CREATE INDEX ind_train"+name+"_arg2_rel_arg1 ON train"+name+"(arg2,relation,arg1)");
			
			System.out.println("CREATE INDEX ind_train"+name+"_arg1_rel_arg2 ON train"+name+"(arg1,relation,arg2)");
 			stmt.execute("CREATE INDEX ind_train"+name+"_arg1_rel_arg2 ON train"+name+"(arg1,relation,arg2)");
 			
 			System.out.println("CREATE INDEX ind_train"+name+"_arg1_arg2_rel ON train"+name+"(arg1,arg2,relation)");
 			stmt.execute("CREATE INDEX ind_train"+name+"_arg1_arg2_rel ON train"+name+"(arg1,arg2,relation)");		
		}
	}
 	
	/**
	 * 
	 * create new baseTbl from the initial but add noise
	 * @param noise
	 * @param info
	 * @throws SQLException
	 */
	private void addNoise(int noise, RelationsInfo info) throws SQLException
 	{
 		String relationName, dumps=dumpRelationsInString();
 		ArrayList<String> arg1=new ArrayList<String>();
 		ArrayList<String> arg2=new ArrayList<String>();
 		Iterator<String> it;
 		ResultSet rs;
 		int relSize,noiseSize,ind1,ind2,counter;
 		Set<String> relations=info.getAllRelations().keySet();
 		String baseTbl="facts"+"_"+noise;
 		int numOfDistinctEntities=100;
 		
 		// copy the basic stuff
 		stmt.execute("CREATE TABLE "+baseTbl+" AS (SELECT relation, arg1,arg2 FROM facts WHERE relation NOT IN "+dumps+" )");
 		
 		// create indexes on the new facts_noise table
		stmt.execute("CREATE INDEX ind_"+baseTbl+"_rel_arg1_arg2 ON "+baseTbl+"(relation,arg1,arg2)");
 		stmt.execute("CREATE INDEX ind_"+baseTbl+"_rel_arg2_arg1 ON "+baseTbl+"(relation,arg2,arg1)");
 		stmt.execute("CREATE INDEX ind_"+baseTbl+"_arg2_rel_arg1 ON "+baseTbl+"(arg2,relation,arg1)");
 		stmt.execute("CREATE INDEX ind_"+baseTbl+"_arg1_rel_arg2 ON "+baseTbl+"(arg1,relation,arg2)");
 		stmt.execute("CREATE INDEX ind_"+baseTbl+"_arg1_arg2_rel ON "+baseTbl+"(arg1,arg2,relation)");
 		
 		
 		
 		it=relations.iterator();
 		while (it.hasNext())
 		{
 			arg1.clear();
 			arg2.clear();

 			relationName=it.next();

 			rs=stmt.executeQuery("SELECT count(*) FROM facts WHERE relation='"+relationName+"'");
 			rs.next();
 			relSize=rs.getInt(1);
 			rs.close();

 			noiseSize=noise*relSize/100; // I need that many noisy facts

 			// get the entities 				
 			rs=stmt.executeQuery("SELECT arg1 FROM (SELECT arg1 FROM facts WHERE relation='"+relationName+"' GROUP BY arg1) WHERE rownum<"+numOfDistinctEntities);

 			while (rs.next())
 			{
 				arg1.add(rs.getString(1));
 			}
 			rs.close();

 			rs=stmt.executeQuery("SELECT arg2 FROM (SELECT arg2 FROM facts WHERE relation='"+relationName+"' GROUP BY arg2) WHERE rownum<"+numOfDistinctEntities);

 			while (rs.next())
 			{
 				arg2.add(rs.getString(1));
 			}
 			rs.close();

 			// create random facts
 			Random rand=new Random();
 			counter=0;

 			do
 			{
 				ind1=rand.nextInt(arg1.size());
 				ind2=rand.nextInt(arg2.size());
 			
 				rs=stmt.executeQuery("SELECT 1 FROM "+baseTbl+" WHERE relation='"+relationName+"' AND arg1='"+arg1.get(ind1)+"' AND arg2='"+arg2.get(ind2)+"'");

 				if (!rs.next())
 				{
 					stmt.execute("INSERT INTO "+baseTbl+" (relation, arg1,arg2) VALUES ("+relationName+", "+arg1.get(ind1)+", "+arg2.get(ind2)+")");
 					counter++;
 				}
 				rs.close();
 			}while (counter<noiseSize); 

 		}
 	}
	private String dumpRelationsInString() {
 		String out="(";
 		for (String dumpRelation: dumpRelations) {
 			out += "'" + dumpRelation + "', ";
 		}
 		out=out.substring(0,out.length()-2);
 		out+=")";
 		return out;
 	}

 	public int getNumOfPartitions()
 	{
 		return this.numOfPartitions;
 	}
 	
 	public static void main(String args[]) {
 		try {
			Sample("/home/adeoliv/Documents/Thesis/yago2core_20120109.n3", 3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
}
