package urdf.ilp;



import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
	private Connection conn;
	private Statement stmt;
	
	private int numOfPartitions;
	private ArrayList<String> dumpRelations=new ArrayList<String>();	// relations which won't participate in learning e.g. domain, range, etc
	
	
	
	public HeadSampler(String iniFile) throws Exception
	{
 		initializeConnection(iniFile);
 		initializeDumpRelations();
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
		
		//recreateIfExist = true;
		
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
  	private void initializeConnection(String iniFile) throws Exception
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
 	private void initializeDumpRelations()
 	{
 		dumpRelations.add("domain");
 		dumpRelations.add("range");
 		dumpRelations.add("isCalled");
 		dumpRelations.add("means");
 		dumpRelations.add("subPropertyOf");
 		dumpRelations.add("subClassOf");
 	}
 	public int getNumOfPartitions()
 	{
 		return this.numOfPartitions;
 	}
}
