package urdf.ilp;



import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import basic.configuration.DBConfig;
import basic.configuration.DBConfig.DatabaseParameters;

/**
 * 	@author Christina Teflioudi
 *	This class samples the database and creates a sample table
 */
public class Sampler 
{
	private Connection conn;
	private Statement stmt;
	
//	private int sampleRatio=4; // parts out of 10
	private int SEEDS=1000;
	
	private int DEPTH=4;
	
	private ArrayList<String> relations=new ArrayList<String>();
	
	private ArrayList<String> dumpRelations=new ArrayList<String>();	// relations which won't participate in learning e.g. domain, range, etc
	
	private PreparedStatement tableExistance;
	
	
	
  	public Sampler(String iniFile,ArrayList<String> relations) throws Exception
	{
 		initializeConnection(iniFile);
 		this.relations=relations;
 		
 		initializeDumpRelations();
 		
 		// prepare the preparedStatements
 		tableExistance=conn.prepareStatement("SELECT 1 FROM user_objects WHERE object_type = 'TABLE' AND object_name=?");
 		
 		createTables();
 		
	}

 	public String[] sample(Relation relation,int depth,boolean recreateIfExist) throws SQLException 
 	{
 		String[] tableNames=new String[2];
 		ResultSet  rs ;
 		String trainTbl="train_"+relation.getName()+"_"+depth;
 		tableNames[0]=trainTbl;
 		
 		// check if the table for this relation already exists
 		tableExistance.setString(1,trainTbl.toUpperCase());
		
		rs = tableExistance.executeQuery();
		
		if (rs.next()) // if it exists
		{
			if (!recreateIfExist)
			{
				return tableNames;
			}
			else
			{
				// drop and recreate tables
				stmt.execute("DROP TABLE "+ trainTbl);
			}
		}

		//createTables(relation,trainTbl,depth);
		//createIndexes(tableNames);
		
		return tableNames;
 		
 	}
 	private void createTmpTblsForRelation(String relation) throws SQLException
 	{
 		String dump=dumpRelationsInString();
 		// create table
 		String sqlCreateRootTbl="CREATE TABLE "+relation+0+" AS (SELECT * FROM ( SELECT * FROM facts WHERE relation='"+relation+"' ORDER BY dbms_random.value )WHERE rownum <= "+SEEDS+")";
 		System.out.println("sqlCreateRootTbl: "+sqlCreateRootTbl);
 		
 		stmt.execute(sqlCreateRootTbl);
 		String sqlCreateTbl;
 		
 		for (int i=1;i<DEPTH+1;i++)
 		{
 			sqlCreateTbl="CREATE TABLE "+relation+i+" AS (SELECT f.id, f.relation, f.arg1, f.arg2, f.confidence FROM "+relation+(i-1)+" s, facts f WHERE (s.arg1=f.arg1 OR s.arg1=f.arg2 OR s.arg2=f.arg1 OR s.arg2=f.arg2) "+
 			" AND f.relation NOT IN "+dump+")";
 			System.out.println("sqlCreateTbl: "+sqlCreateTbl);
 			stmt.execute(sqlCreateTbl);
 		}
 		
 	}
 	private void createTables() throws SQLException
 	{
 		
 		String sqlCreateSampleTbl="";
 		
 		//create tmp tables
 		for (int i=0;i<relations.size();i++)
 		{
 			createTmpTblsForRelation(relations.get(i));
 			
 			for (int d=0;d<=DEPTH;d++)
 			{
 				sqlCreateSampleTbl+="(SELECT * FROM "+relations.get(i)+d+") UNION ";
 			}
 		}
 		// create sample table
 		sqlCreateSampleTbl=sqlCreateSampleTbl.substring(0,sqlCreateSampleTbl.length()-6); // get rid of last UNION
 		sqlCreateSampleTbl="CREATE TABLE sample"+SEEDS+" AS (SELECT distinct(id,relation,arg1,arg2,confidence) FROM ("+sqlCreateSampleTbl+"))";
 		System.out.println("sqlCreateSampleTbl: "+sqlCreateSampleTbl);
 		
 		stmt.execute(sqlCreateSampleTbl);
 		
 		//create indexes on sample table
// 		System.out.println("creating index 1");
// 		stmt.execute("CREATE UNIQUE INDEX ind_sample_id ON sample(id)");
// 		System.out.println("creating index 2");
// 		stmt.execute("CREATE INDEX ind_sample_arg1 ON sample(arg1)");
// 		System.out.println("creating index 3");
// 		stmt.execute("CREATE INDEX ind_sample_arg2 ON sample(arg2)");
// 		System.out.println("creating index 4");
// 		stmt.execute("CREATE INDEX ind_sample_rel ON sample(relation)");
// 		System.out.println("creating index 5");
// 		stmt.execute("CREATE INDEX ind_sample_rel_arg1 ON sample(relation,arg1)");
// 		System.out.println("creating index 6");
// 		stmt.execute("CREATE INDEX ind_sample_rel_arg2 ON sample(relation,arg2)");
 		
 		//drop tmp tables
// 		for (int i=0;i<relations.size();i++)
// 		{
// 			for (int d=0;d<=DEPTH;d++)
// 			{
// 				stmt.execute("DROP TABLE "+relations.get(i)+d);
// 			}
// 		}
 		
 		
 	}
// 	private void createTables(Relation relation,String trainTbl,int depth) throws SQLException
// 	{
// 		String relationFacts,createTrainFinal,str,iterateTrainStr,createTrain;
// 		String where="";
// 		String dump=dumpRelationsInString();
// 		
// 		
// 		// get all the facts for the target relation
// 		relationFacts=" CREATE TABLE temp AS (SELECT rownum rnum, id, relation, arg1,arg2 FROM facts WHERE relation='"+relation.getName()+"')";
// 		stmt.execute(relationFacts);
// 		
// 		// find where clauses for train 		
// 		for (int i=0;i<sampleRatio;i++)
// 		{
// 			where+="mod(rnum,10)="+i+" OR ";
// 		}
// 		where=where.substring(0, where.length()-3);
// 		
// 		
// 		
// 		// get the seed entities
// 		createTrain="CREATE TABLE trainTmp AS ((SELECT relation as relation, arg1 as entity FROM temp WHERE "+where+")";
// 		createTrain+=" UNION (SELECT relation as relation, arg2 as entity FROM temp WHERE "+where+"))"; 		
//
//	 	stmt.execute(createTrain);
// 		
// 		// iterate according to depth to enrich tables
// 		str=" (SELECT relation FROM trainTmp tmp GROUP BY relation) "; 		
// 		
// 		iterateTrainStr="INSERT /*+ append */ INTO trainTmp (SELECT f0.relation as relation, f0.arg2 as entity FROM facts f0, trainTmp tmp  WHERE f0.arg1=tmp.entity AND f0.relation NOT IN"+dump+" AND f0.relation NOT IN "+str+")";
// 		iterateTrainStr+=" UNION (SELECT f0.relation as relation, f0.arg1 as entity FROM facts f0, trainTmp tmp  WHERE f0.arg2=tmp.entity AND f0.relation NOT IN"+dump+" AND f0.relation NOT IN "+str+")";
// 		 		
// 		for (int i=0;i<depth;i++)
// 		{
// 	 		stmt.execute(iterateTrainStr); 	 		
// 		}
// 		
// 		 // create the tables finally
// 		createTrainFinal="CREATE TABLE "+trainTbl+" AS (SELECT f.id as id, f.relation as relation, f.arg1 as arg1, f.arg2 as arg2 FROM facts f, ";       
// 		createTrainFinal+="(SELECT relation, entity FROM trainTmp GROUP BY relation, entity)  tmp ";       
// 		createTrainFinal+="WHERE f.relation=tmp.relation AND (tmp.entity=f.arg1 OR tmp.entity=f.arg2))";   		
// 		
// 		stmt.execute(createTrainFinal); 		          
//
// 		// drop temporary tables
// 		stmt.execute("DROP TABLE temp");
// 		stmt.execute("DROP TABLE trainTmp");
// 	}
// 	private void createIndexes(String[] tableNames) throws SQLException
// 	{
// 		for (int i=0,len=tableNames.length;i<len;i++)
// 		{
// 			stmt.execute("CREATE UNIQUE INDEX ind_"+tableNames[i]+"_id ON "+tableNames[i]+"(id)");
// 			stmt.execute("CREATE INDEX ind_"+tableNames[i]+"_arg1 ON "+tableNames[i]+"(arg1)");
// 			stmt.execute("CREATE INDEX ind_"+tableNames[i]+"_arg2 ON "+tableNames[i]+"(arg2)");
// 			stmt.execute("CREATE INDEX ind_"+tableNames[i]+"_rel ON "+tableNames[i]+"(relation)");
// 			stmt.execute("CREATE INDEX ind_"+tableNames[i]+"_rel_arg1 ON "+tableNames[i]+"(relation,arg1)");
// 			stmt.execute("CREATE INDEX ind_"+tableNames[i]+"_rel_arg2 ON "+tableNames[i]+"(relation,arg2)");
// 		}
// 		
// 	}
 	private String dumpRelationsInString()
 	{
 		String out="(";
 		for (int i=0,len=dumpRelations.size();i<len;i++)
 		{
 			out+="'"+dumpRelations.get(i)+"', ";
 		}
 		out=out.substring(0,out.length()-2);
 		out+=")";
 		return out;
 	}
 	// ***************** SETTERS ********************
  	public void setDumpRelations(ArrayList<String> relations)
 	{
 		this.dumpRelations=relations;
 	}

 	

 	// **************** INITIALIZERS ****************
 	private void initializeDumpRelations()
 	{
 		// yago specific
 		dumpRelations.add("domain");
 		dumpRelations.add("range");
 		dumpRelations.add("isCalled");
 		dumpRelations.add("means");
 		dumpRelations.add("subPropertyOf");
 		dumpRelations.add("subClassOf");
 		dumpRelations.add("foundIn");
 		
 		// time specific
 		dumpRelations.add("during");
 		dumpRelations.add("establishedOnDate");
 		dumpRelations.add("bornOnDate");
 		dumpRelations.add("createdOnDate");
 		dumpRelations.add("diedOnDate");
 		dumpRelations.add("happenedIn");
 		dumpRelations.add("publishedOnDate");
 		dumpRelations.add("since");
 		dumpRelations.add("until");
 		
 		// gazetteer
 		dumpRelations.add("gaz_hasLatitude");
 		dumpRelations.add("gaz_hasLongitude");
 		dumpRelations.add("gaz_hasName");
 		dumpRelations.add("gaz_isLocatedIn");
 		
 	}
 	private void initializeConnection(String iniFile) throws Exception
	{ 
 		try 
		{			
 		    DatabaseParameters p = DBConfig.databaseParameters(iniFile);

 		    if (p.system.toLowerCase().indexOf("postgres") >= 0) 
 		    {
 		    
 		      DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());

 		      this.conn = DriverManager.getConnection("jdbc:postgresql://" + p.host + ":" + p.port + (p.database == null ? "" : "/" + p.database), p.user,
 		          p.password);

 		    } 
 		    else if (p.system.toLowerCase().indexOf("oracle") >= 0) 
 		    {
 		    	
 		      DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());

 		      this.conn = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + p.host
 		          + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + p.inst + ")(server = dedicated)))", p.user, p.password);

 		    } 
 		    else
 		      throw new Exception("UNKNOWN DB DRIVER!");	
			this.stmt = conn.createStatement();				
		} 
		catch (ClassNotFoundException e) {e.printStackTrace();}	
		catch (SQLException e) {e.printStackTrace();}
		 
	}
 	
	public static void main(String[] args) 
	{
		try
		{
			ArrayList<String> relations=new ArrayList<String>();
			relations.add("politicianOf");
			//relations.add("actedIn");
			Sampler sampler=new Sampler(args[0],relations);
			
			
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}

}
