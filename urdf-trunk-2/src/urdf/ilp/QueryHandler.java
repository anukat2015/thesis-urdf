package urdf.ilp;



import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import basic.configuration.DBConfig;
import basic.configuration.DBConfig.DatabaseParameters;



/**
 * 	@author Christina Teflioudi
 * 	The class QueryHandler generates and fires all queries to the DB. All communication with the DB is through this class.
 */
public class QueryHandler 
{
	private Connection conn;
	private Statement stmt;
	private int rsCounter=0;
	private String baseTbl;
	private String headTbl; 
	
	// for train tables
 	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForPositivesCovered=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating confidence	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForExamplesCovered=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating confidence	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForBody=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating specialityRatio	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForMult1=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating beta
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForMult2=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating beta
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForPossiblePosToBeCovered=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating support	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForConstants=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for finding nice constants
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForEntities1=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForEntities2=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForBodyAvgMult=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForBodyMultVar=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();
	
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForOverlapEntities=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForUnionEntities=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();
	
	// for overlap and subsumption
	private HashMap<Integer, HashMap<String,PreparedStatement>> queriesForOverlap=
		new HashMap<Integer, HashMap<String,PreparedStatement>>();	// used by RuleLearner for calculating overlap between bodies
	

	
	 
	public QueryHandler(String iniFile, String baseTbl, String headTbl) throws Exception
	{
		try 
		{			
			this.baseTbl=baseTbl;
			this.headTbl=headTbl;
			initializeConnection(iniFile); //initialize the connection						
		} 
		catch (ClassNotFoundException e) {e.printStackTrace();}	
		catch (SQLException e) {e.printStackTrace();}
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

 	
 	/**
 	 * @param whichStatements 0: for learning 2: for post-processing
 	 * @throws SQLException
 	 */
 	public void clearAllStatements(int whichStatements) throws SQLException
 	{
 		ArrayList<HashMap<Integer, HashMap<String,PreparedStatement>>> array=new ArrayList<HashMap<Integer, HashMap<String,PreparedStatement>>>();
 		
 		switch(whichStatements)
 		{
 			case 0:
 		 		array.add(queriesForExamplesCovered);
 		 		array.add(queriesForBody);
 		 		array.add(queriesForMult1);
 		 		array.add(queriesForMult2);
 		 		array.add(queriesForPossiblePosToBeCovered);
 		 		array.add(queriesForPositivesCovered); 		 		
 		 		array.add(queriesForConstants);
 		 		array.add(queriesForOverlapEntities);
 		 		array.add(queriesForUnionEntities);
 		 		array.add(queriesForEntities1);
 		 		array.add(queriesForEntities2);
 		 		array.add(queriesForBodyAvgMult);
 		 		array.add(queriesForBodyMultVar);
 				break;
 			default:
 				array.add(queriesForOverlap);
 		}
 		for (int i=0,len=array.size();i<len;i++)
 		{
 	 		clearStatements(array.get(i));
 		}
 	}
 	public void clearStatements(HashMap<Integer, HashMap<String,PreparedStatement>> queryMap) throws SQLException
 	{
 		Set<String> set2;
 		Set<Integer> set1;
 		Iterator<String> it2;
 		Iterator<Integer> it1;

 		set1=queryMap.keySet();
 		it1=set1.iterator();
 		int d, counter=0;
 		HashMap<String,PreparedStatement> map;

 		while (it1.hasNext())
 		{
 			
 			d=it1.next();
 			map=queryMap.get(d);
 			set2=map.keySet();
 			it2=set2.iterator();
 			while (it2.hasNext())
 			{
 				map.get(it2.next()).close();
 				counter++;
 			}
 		}
 		queryMap.clear();
 	}

	//***************************** FOR RULELEARNER ********************************************
	
	/**
	* @param finalClause: the actual query to be fired
	 * @param whereClause: key in the hash maps
	 * @param forWhat 0: for Positives, 1: for examples 2: for body 3: for overlap between bodies of 2 rules
	 * @param size
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement checkForQueryExistance(String finalClause, String key, int forWhat, int size) throws SQLException
	{
		PreparedStatement ps;
		HashMap<Integer, HashMap<String,PreparedStatement>>  queries;
		String name="";
		int count=0;
		switch(forWhat)
		{
			case 0:
				queries=queriesForPositivesCovered;
				name="queriesForPositives";
				break;
			case 1:
				queries=queriesForExamplesCovered;
				name="queriesForExamples";
				break;
			case 2:
				queries=queriesForBody;
				name="queriesForBody";
				break;
			case 3: // overlap
				queries=queriesForOverlap;
				name="queriesForOverlap";
				break;
			case 4:
				queries=queriesForMult1;
				name="queriesForMult1";
				break;
			case 5: 
				queries=queriesForMult2;
				name="queriesForMult2";
				break;
			case 6:
				queries=queriesForPossiblePosToBeCovered;
				name="queriesForExamplesForSupport";
				break;
			case 7:
				queries=queriesForConstants;
				name="queriesForConstants";
				break;
			case 8:
				queries=queriesForOverlapEntities;
				name="queriesForOverlapEntities";
				break;
			case 9: 				
				queries=queriesForUnionEntities;
				name="queriesForUnionEntities";
				break;
			case 10: // entities in examplesCovered arg1
				queries=queriesForEntities1;
				name="queriesForEntities1";
				break;
			case 11:// entities in examplesCovered arg2
				queries=queriesForEntities2;
				name="queriesForEntities2";
				break;
			case 12:	
				queries=queriesForBodyAvgMult;
				name="queriesForBodyAvgMult";
				break;
			default:
				queries=queriesForBodyMultVar;
				name="queriesForBodyMultVar";
		}
		
		if (!queries.containsKey(size))
		{

			
			queries.put(size, new HashMap<String,PreparedStatement>());
			ps = conn.prepareStatement(finalClause);
			queries.get(size).put(key, ps);
			
			Set<Integer> set=queries.keySet();
			Iterator<Integer> it=set.iterator();
			while(it.hasNext())
			{
				count+=queries.get(it.next()).size();
			}
			
			//System.out.println(name+": "+count);
		}
		else
		{
			if (!queries.get(size).containsKey(key))
			{
				ps = conn.prepareStatement(finalClause);
				queries.get(size).put(key, ps);
				
				Set<Integer> set=queries.keySet();
				Iterator<Integer> it=set.iterator();
				while(it.hasNext())
				{
					count+=queries.get(it.next()).size();
				}
				
				//System.out.println(name+": "+count);
			}
			else
			{
				ps=queries.get(size).get(key);
			}
		}

		return ps;
	}
	
	/**
	 * @param clauses
	 * 		0: select clause 
	 * 		1: from clause only for body -> body and examplesCovered 
	 * 		2: where clause only for body -> body
	 * 		3: group by clause
	 * 		4: from clause for body and head -> positivesCovered
	 * 		5: where clause for body and head without IN statement -> positivesCovered 
	 * 		6: where clause for body with IN statement -> examplesCovered
	 * 		
	 * 
	 * @param forWhat 0: for PositivesCovered, 1: for examplesCovered 2: for body 4: for mult1 5: for mult2 6: for possiblePosToBeCovered
	 * @param rule
	 * @param inputArg 0: both, 1: first, 2: second
	 * @return
	 * @throws Exception
	 */
	public float calcRuleProperties(Rule rule, String[] clauses, int forWhat, int inputArg) throws Exception
	{
		String finalClause;
		String key;
		switch(forWhat)
		{
			case 0:	// positivesCovered
				finalClause=clauses[0]+clauses[4]+clauses[5]+clauses[3];			
				key=clauses[4]+clauses[5];
				finalClause="SELECT count(*) FROM ("+finalClause+")";
				break;
			case 1: // examplesCovered
				finalClause=clauses[0]+clauses[1]+clauses[6]+clauses[3];
				key=clauses[1]+clauses[6];
				finalClause="SELECT count(*) FROM ("+finalClause+")";
				break;
			case 2: // body
				finalClause=clauses[0]+clauses[1]+clauses[2]+clauses[3];
				key=clauses[1]+clauses[2];
				finalClause="SELECT count(*) FROM ("+finalClause+")";
				break;
			case 4:
				finalClause=clauses[0]+clauses[1]+clauses[6]+clauses[3];
				key=clauses[1]+clauses[6];
				finalClause="SELECT avg(n1), sum(n1) FROM (SELECT count(*) n1 FROM ("+finalClause+") GROUP BY input)";
				break;
			case 5: // case 5
				finalClause=clauses[0]+clauses[1]+clauses[6]+clauses[3];
				key=clauses[1]+clauses[6];
				finalClause="SELECT avg(n1), sum(n1) FROM (SELECT count(*) n1 FROM ("+finalClause+") GROUP BY output)";
				break;
			default:
				finalClause=clauses[7];
				finalClause="SELECT count(*) FROM ("+finalClause+")";
				//key=clauses[1]+clauses[2];
				key=finalClause;
		}
		//rule.printRule(true);
		//System.out.println(finalClause);
		PreparedStatement ps=fillInPreparedStatement(checkForQueryExistance(finalClause,key,forWhat, rule.getBodyLiterals().size()),rule,1, forWhat, inputArg);
		return fire(ps,forWhat,rule);
	}
	public float getBodyAvgMult(Rule rule, String[] clauses,int inputArg) throws SQLException
	{
		String groupBy=(inputArg==1?"input":"output");
		String basic=clauses[0]+clauses[1]+clauses[2]+clauses[3];
		String finalClause="SELECT avg(mult) average FROM (SELECT count(*) mult FROM("+basic+") GROUP BY "+groupBy+")";
		String key=clauses[1]+clauses[2];
		//rule.printRule(true);
		//System.out.println(finalClause);
		PreparedStatement ps=fillInPreparedStatement(checkForQueryExistance(finalClause,key,12, rule.getBodyLiterals().size()),rule,1, 2, inputArg);
		
		float avg=fire(ps,2,rule);		
		//System.out.println("BodyMult: "+avg);
		return avg;
		
	}
	public float getBodyMultVar(Rule rule, String[] clauses,int inputArg, float avg) throws SQLException
	{
		String groupBy=(inputArg==1?"input":"output");
		String basic=clauses[0]+clauses[1]+clauses[2]+clauses[3];
		String finalClause="SELECT sum((mult-"+avg+")*(mult-"+avg+")*numOfObservations)/sum(numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM("+basic+") GROUP BY "+groupBy+") GROUP BY mult)";
		//String finalClause="SELECT Variance(mult) FROM (SELECT count(*) mult FROM("+basic+") GROUP BY "+groupBy+")";
		
		
		String key=clauses[1]+clauses[2];
		//rule.printRule(true);
		//System.out.println(finalClause);
		PreparedStatement ps=fillInPreparedStatement(checkForQueryExistance(finalClause,key,13, rule.getBodyLiterals().size()),rule,1, 2, inputArg);
		float var=fire(ps,2,rule);	
		//System.out.println("BodyVar: "+var);
		
		return var;		
	}
 	
	public ArrayList<String> findConstants(Rule rule, String[] clauses, int possiblePosToBeCoveredThershold, int positivesCoveredThreshold, float supportThreshold, int factsForHead, int inputArg,int depthOfRule) throws SQLException
	{
		String arg,put, finalClause, key;
		ArrayList<String> outConstants=new ArrayList<String>();
		
		arg="arg"+rule.getHead().getRelation().getConstantInArg();
		put=(rule.getHead().getRelation().getConstantInArg()==1?"input":"output");
		finalClause="SELECT "+put+" FROM ("+clauses[0]+clauses[4]+clauses[5]+clauses[3]+") GROUP BY "+put+" HAVING count(*)>"+positivesCoveredThreshold;
		finalClause+=" INTERSECT ";
		finalClause+="SELECT "+arg+" FROM ("+clauses[7]+") GROUP BY "+arg+" HAVING count(*)>"+possiblePosToBeCoveredThershold+" OR count(*)>"+supportThreshold*factsForHead;
		key=finalClause;
		
		//System.out.println(finalClause);
		
		PreparedStatement ps=checkForQueryExistance(finalClause,key, 7, rule.getBodyLiterals().size());
		ps=fillInPreparedStatement(ps, rule, 1, 0, inputArg);
		ps=fillInPreparedStatement(ps, rule, depthOfRule+2, 6, inputArg);
		ResultSet  rs = ps.executeQuery();
		//rsCounter++;
		//System.out.println(rsCounter);
		while (rs.next())
		{
			outConstants.add(rs.getString(1));
		}		
		return outConstants;		
	}
	/**
	 * @param rule
	 * @param inputArg 0: both, 1: first, 2: second
	 * @return 
	 * String []: 
	 * 		0: select clause 
	 * 		1: from clause only for body -> body and examples 
	 * 		2: where clause only for body -> body
	 * 		3: group by clause
	 * 		4: from clause for body and head -> positives
	 * 		5: where clause for body and head without IN statement -> positives 
	 *		6: where clause for body with IN statement -> examplesConf
	 *		7: basic clause for head with IN statement -> examplesSupp
	 *		8: from clause for body for evaluation with recursion
	 *		9: where clause body but not preparedStatement
	 */
	public String[] parseRule(Rule rule, int inputArg)
	{
		String parallelHintBaseTbl=" /*+ PARALLEL(facts,8) */ ";
		String parallelHintSampleTbl=" /*+ PARALLEL(train11,8) */ ";
		String[] sqlClauses=new String[10];
		ArrayList<Literal> bodyLiterals=rule.getBodyLiterals();
		Literal head=rule.getHead();
		boolean arg1Conn=false,arg2Conn=false, inHead=false;
		String input="", output="", inputForSupp="",outputForSupp="";
		String leftPart,rightPart;
		sqlClauses[0]= " SELECT ";//+parallelHintBaseTbl;
		sqlClauses[1]=" FROM ";
		
		sqlClauses[2]=" WHERE ";
		sqlClauses[3]=" GROUP BY ";
		sqlClauses[4]=" FROM "+headTbl+" f0, ";
		sqlClauses[5]=" WHERE f0.relation=? AND ";
		sqlClauses[6]=" WHERE ";
		sqlClauses[7]=" SELECT f0.arg1,f0.arg2 FROM "+headTbl+" f0 WHERE f0.relation=? AND ";
		sqlClauses[8]=" FROM ";
		sqlClauses[9]=" WHERE ";
		
		for (int i=0,len=bodyLiterals.size();i<len;i++)
		{
			if (!bodyLiterals.get(i).getRelation().isAuxiliary())
			{
				sqlClauses[1]+=baseTbl+" f"+(i+1)+", ";
				
				if (bodyLiterals.get(i).getRelationName().equals(rule.getHead().getRelationName()))
				{
					sqlClauses[8]+="insertTable"+" f"+(i+1)+", ";
				}
				else
				{
					sqlClauses[8]+=baseTbl+" f"+(i+1)+", ";
				}				
				
				sqlClauses[4]+=baseTbl+" f"+(i+1)+", ";
				
				sqlClauses[2]+="f"+(i+1)+".relation=? AND ";
				sqlClauses[9]+="f"+(i+1)+".relation='"+bodyLiterals.get(i).getRelationName()+"' AND ";
				sqlClauses[5]+="f"+(i+1)+".relation=? AND ";
				sqlClauses[6]+="f"+(i+1)+".relation=? AND ";
				
				if (!arg1Conn && head.getFirstArgument()==bodyLiterals.get(i).getFirstArgument()) // I got the 1st arg
				{
					sqlClauses[0]+=" f"+(i+1)+".arg1 input,";
					sqlClauses[3]+=" f"+(i+1)+".arg1,";
					sqlClauses[5]+=" f0.arg1=f"+(i+1)+".arg1 AND ";
					input="f"+(i+1)+".arg1 IN (SELECT f0.arg1 FROM "+headTbl+" f0 WHERE f0.relation=? ";
					inputForSupp="f"+(i+1)+".arg1";
					arg1Conn=true;
				}
				else if(!arg1Conn && head.getFirstArgument()==bodyLiterals.get(i).getSecondArgument())// I got the 1st arg
				{
					sqlClauses[0]+=" f"+(i+1)+".arg2 input,";
					sqlClauses[3]+=" f"+(i+1)+".arg2,";
					sqlClauses[5]+=" f0.arg1=f"+(i+1)+".arg2 AND ";
					input="f"+(i+1)+".arg2 IN (SELECT f0.arg1 FROM "+headTbl+" f0 WHERE f0.relation=? ";
					inputForSupp="f"+(i+1)+".arg2";
					arg1Conn=true;
				}
				if (!arg2Conn && head.getSecondArgument()==bodyLiterals.get(i).getFirstArgument())// I got the 2nd arg
				{
					sqlClauses[0]+=" f"+(i+1)+".arg1 output,";
					sqlClauses[3]+=" f"+(i+1)+".arg1,";
					sqlClauses[5]+=" f0.arg2=f"+(i+1)+".arg1 AND ";
					output="f"+(i+1)+".arg1 IN (SELECT f0.arg2 FROM "+headTbl+" f0 WHERE f0.relation=? ";
					outputForSupp="f"+(i+1)+".arg1";
					arg2Conn=true;
				}
				else if(!arg2Conn && head.getSecondArgument()==bodyLiterals.get(i).getSecondArgument())// I got the 2nd arg
				{
					sqlClauses[0]+=" f"+(i+1)+".arg2 output,";
					sqlClauses[3]+=" f"+(i+1)+".arg2,";
					sqlClauses[5]+=" f0.arg2=f"+(i+1)+".arg2 AND ";
					output="f"+(i+1)+".arg2 IN (SELECT f0.arg2 FROM "+headTbl+" f0 WHERE f0.relation=? ";
					outputForSupp="f"+(i+1)+".arg2";
					arg2Conn=true;
				}
				// Bind between relations
				for (int j=0;j<i;j++)
				{
					if (bodyLiterals.get(j).getRelation().isAuxiliary())
					{
						continue;
					}					
					if(bodyLiterals.get(i).getFirstArgument()==bodyLiterals.get(j).getFirstArgument())
					{
						sqlClauses[2]+="f"+(i+1)+".arg1=f"+(j+1)+".arg1 AND ";	
						sqlClauses[9]+="f"+(i+1)+".arg1=f"+(j+1)+".arg1 AND ";	
						sqlClauses[5]+="f"+(i+1)+".arg1=f"+(j+1)+".arg1 AND ";	
						sqlClauses[6]+="f"+(i+1)+".arg1=f"+(j+1)+".arg1 AND ";	
					}
					else if (bodyLiterals.get(i).getFirstArgument()==bodyLiterals.get(j).getSecondArgument())
					{
						sqlClauses[2]+="f"+(i+1)+".arg1=f"+(j+1)+".arg2 AND ";		
						sqlClauses[9]+="f"+(i+1)+".arg1=f"+(j+1)+".arg2 AND ";
						sqlClauses[5]+="f"+(i+1)+".arg1=f"+(j+1)+".arg2 AND ";	
						sqlClauses[6]+="f"+(i+1)+".arg1=f"+(j+1)+".arg2 AND ";
					}
					if(bodyLiterals.get(i).getSecondArgument()==bodyLiterals.get(j).getFirstArgument())
					{
						sqlClauses[2]+="f"+(i+1)+".arg2=f"+(j+1)+".arg1 AND ";	
						sqlClauses[9]+="f"+(i+1)+".arg2=f"+(j+1)+".arg1 AND ";	
						sqlClauses[5]+="f"+(i+1)+".arg2=f"+(j+1)+".arg1 AND ";
						sqlClauses[6]+="f"+(i+1)+".arg2=f"+(j+1)+".arg1 AND ";
					}
					else if (bodyLiterals.get(i).getSecondArgument()==bodyLiterals.get(j).getSecondArgument())
					{
						sqlClauses[2]+="f"+(i+1)+".arg2=f"+(j+1)+".arg2 AND ";	
						sqlClauses[9]+="f"+(i+1)+".arg2=f"+(j+1)+".arg2 AND ";	
						sqlClauses[5]+="f"+(i+1)+".arg2=f"+(j+1)+".arg2 AND ";		
						sqlClauses[6]+="f"+(i+1)+".arg2=f"+(j+1)+".arg2 AND ";	
					}					
				}
				
			}
			else // auxiliary relation
			{
				leftPart=null;
				rightPart=null;
				inHead=false;

				if (bodyLiterals.get(i).getSecondArgument()<0) // I have a constant
				{
					rightPart="'"+bodyLiterals.get(i).getConstant()+"' ";
				}

				for (int j=0;j<len;j++)
				{
					if (bodyLiterals.get(j).getRelation().isAuxiliary())
					{
						continue;
					}
					if (leftPart==null)
					{
						if (bodyLiterals.get(i).getFirstArgument()==bodyLiterals.get(j).getFirstArgument())
						{
							leftPart="f"+(j+1)+".arg1";
						}
						else if(bodyLiterals.get(i).getFirstArgument()==bodyLiterals.get(j).getSecondArgument())
						{
							leftPart="f"+(j+1)+".arg2";
						}
					}
					if (rightPart==null)
					{
						if (bodyLiterals.get(i).getSecondArgument()==bodyLiterals.get(j).getFirstArgument())
						{
							rightPart="f"+(j+1)+".arg1";
						}
						else if(bodyLiterals.get(i).getSecondArgument()==bodyLiterals.get(j).getSecondArgument())
						{
							rightPart="f"+(j+1)+".arg2";
						}
					}
					if (leftPart!=null && rightPart!=null)
					{
						break;
					}

				}
				if (leftPart==null)
				{
					if (bodyLiterals.get(i).getFirstArgument()==head.getFirstArgument())
					{
						leftPart="f0.arg1";
						inHead=true;
					}
					else if (bodyLiterals.get(i).getFirstArgument()==head.getSecondArgument())
					{
						leftPart="f0.arg2";
						inHead=true;
					}
				}
				if(rightPart==null)
				{
					if (bodyLiterals.get(i).getSecondArgument()==head.getFirstArgument())
					{
						rightPart="f0.arg1";
						inHead=true;
					}
					else if (bodyLiterals.get(i).getSecondArgument()==head.getSecondArgument())
					{
						rightPart="f0.arg2";
						inHead=true;
					}
				}
				
				sqlClauses[5]+=leftPart+bodyLiterals.get(i).getRelationName()+rightPart+" AND ";
				if (!inHead)
				{
					sqlClauses[2]+=leftPart+bodyLiterals.get(i).getRelationName()+rightPart+" AND ";
					sqlClauses[9]+=leftPart+bodyLiterals.get(i).getRelationName()+rightPart+" AND ";
					sqlClauses[6]+=leftPart+bodyLiterals.get(i).getRelationName()+rightPart+" AND ";
				}
				else // inHead
				{
					if (!input.equals(""))
					{
						input+=" AND "+leftPart+bodyLiterals.get(i).getRelationName()+rightPart;
					}
					if (!output.equals(""))
					{
						output+=" AND "+leftPart+bodyLiterals.get(i).getRelationName()+rightPart;
					}
				}
			}			
		}
		
		switch(inputArg)
		{
			case 1:
				sqlClauses[6]+=input+")";
				///////////////////////////////////////
				sqlClauses[7]+= "f0.arg1 IN (SELECT "+inputForSupp+sqlClauses[1].substring(0, sqlClauses[1].length()-2)+sqlClauses[2].substring(0, sqlClauses[2].length()-4)+")";
				break;
			case 2:
				sqlClauses[6]+=output+")";
				//////////////////////////////////////
				sqlClauses[7]+= "f0.arg2 IN (SELECT "+outputForSupp+sqlClauses[1].substring(0, sqlClauses[1].length()-2)+sqlClauses[2].substring(0, sqlClauses[2].length()-4)+")";
				break;
			default:
				if (input.equals(""))
				{
					sqlClauses[6]+=output+")";
				}
				else if (output.equals(""))
				{
					sqlClauses[6]+=input+")";
				}
				else
				{
					sqlClauses[6]+=" ("+input+")"+" AND "+output+")"+")";
				}
				
				if (inputForSupp.equals(""))
				{
//////////////////////////////////////
					sqlClauses[7]+= "f0.arg2 IN (SELECT "+outputForSupp+sqlClauses[1].substring(0, sqlClauses[1].length()-2)+sqlClauses[2].substring(0, sqlClauses[2].length()-4)+")";					
				}
				else if (outputForSupp.equals(""))
				{
//////////////////////////////////////
					sqlClauses[7]+= "f0.arg1 IN (SELECT "+inputForSupp+sqlClauses[1].substring(0, sqlClauses[1].length()-2)+sqlClauses[2].substring(0, sqlClauses[2].length()-4)+")";					
				}
				else
				{
//////////////////////////////////////
					sqlClauses[7]+="(f0.arg1 IN (SELECT "+inputForSupp+sqlClauses[1].substring(0, sqlClauses[1].length()-2)+sqlClauses[2].substring(0, sqlClauses[2].length()-4)+") AND f0.arg2 IN (SELECT "+outputForSupp+sqlClauses[1].substring(0, sqlClauses[1].length()-2)+sqlClauses[2].substring(0, sqlClauses[2].length()-4)+"))";
				}
				
		}
		
		sqlClauses[0]=sqlClauses[0].substring(0, sqlClauses[0].length()-1);
		sqlClauses[1]=sqlClauses[1].substring(0, sqlClauses[1].length()-2);
		sqlClauses[2]=sqlClauses[2].substring(0, sqlClauses[2].length()-4);
		sqlClauses[9]=sqlClauses[9].substring(0, sqlClauses[9].length()-4);
		sqlClauses[3]=sqlClauses[3].substring(0, sqlClauses[3].length()-1);
		sqlClauses[4]=sqlClauses[4].substring(0, sqlClauses[4].length()-2);
		sqlClauses[8]=sqlClauses[8].substring(0, sqlClauses[8].length()-2);
		sqlClauses[5]=sqlClauses[5].substring(0, sqlClauses[5].length()-4);
		sqlClauses[7]+=" GROUP BY f0.arg1,f0.arg2";
		
		return sqlClauses;
	}
	public int distinctEntitiesInExamplesCovered(Rule rule,String[] clauses, int arg, int inputArg) throws SQLException
	{
		String finalClause,key;
		PreparedStatement ps;
		int forWhat;
		
		if (arg==1)
		{
			finalClause="SELECT count(*) FROM ( SELECT input FROM ("+clauses[0]+clauses[1]+clauses[6]+clauses[3]+") GROUP BY input)";	
			forWhat=10;
		}
		else
		{
			finalClause="SELECT count(*) FROM ( SELECT output FROM ("+clauses[0]+clauses[1]+clauses[6]+clauses[3]+") GROUP BY output)";	
			forWhat=11;
		}
		key=clauses[1]+clauses[6];
		ps=checkForQueryExistance(finalClause,key, forWhat, rule.getBodyLiterals().size());
		ps=fillInPreparedStatement(ps, rule, 1, 1, inputArg);
		return (int)fire(ps, 1,rule);
		
	}
	
	public int getAllCombinationsOfPairs(String target, int arg) throws SQLException
	{
		int possibleOtherEntities;
		String finalClause;
		ResultSet rs;
		finalClause="SELECT count(*) FROM (SELECT "+(arg==1?"input":"output")+" FROM (SELECT arg1 input, arg2 output FROM "+baseTbl+" WHERE relation='"+target+"') GROUP BY "+(arg==1?"input":"output")+")";
		//System.out.println(finalClause);
		rs=stmt.executeQuery(finalClause);
		//rsCounter++;
		//System.out.println(rsCounter);
		rs.next();
		possibleOtherEntities=rs.getInt(1);
		rs.close();
		return possibleOtherEntities;		
	}
	
 	public int calcOverlap(Rule rule1, String[] clauses1, Rule rule2, String[] clauses2) throws SQLException
	{
		String finalClause="(("+clauses1[0]+clauses1[1]+clauses1[2]+clauses1[3]+") INTERSECT ("+clauses2[0]+clauses2[1]+clauses2[2]+clauses2[3]+"))";
		finalClause="SELECT count(*) FROM "+finalClause;
		PreparedStatement ps=checkForQueryExistance(finalClause, finalClause, 3, rule1.getBodyLiterals().size()+rule2.getBodyLiterals().size());
		
		int depthOfRule1=0;
		for (int i=0,len=rule1.getBodyLiterals().size();i<len;i++)
		{
			if (rule1.getBodyLiterals().get(i).getRelation().isAuxiliary())
			{
				continue;
			}
			depthOfRule1++;
		}
		
		ps=fillInPreparedStatement(ps, rule1,1, 2, 1);
		ps=fillInPreparedStatement(ps, rule2,depthOfRule1+1, 2, 1);
		return (int)fire(ps,-1,null);
	}

	//***************************** FIRE QUERIES ***************************************
	public int fireSampleQuery(Relation relation) throws SQLException
	{
		ResultSet rs=stmt.executeQuery("SELECT count(*) FROM "+headTbl+" WHERE relation ='"+relation.getName()+"'");
		//rsCounter++;
		//System.out.println(rsCounter);
		rs.next();	
		int ans=rs.getInt(1);
		rs.close();
		return  ans;		
	}

	private float fire(PreparedStatement ps, int forWhat,Rule rule)throws SQLException
	{
		ResultSet  rs = ps.executeQuery();
		//rsCounter++;
		//System.out.println(rsCounter);
		rs.next();
		float ans=rs.getFloat(1);
		if (forWhat==4||forWhat==5)
		{
			rule.setExamplesCovered(rs.getInt(2));
		}
		rs.close();
		return ans;
	}
	
	/**
	 * @param ps
	 * @param rule
	 * @param forWhat 0: for Positives, 1: for examples 2: for body 4: for mult1 5: for mult2
	 * @param inputArg 0: both, 1: first, 2: second
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement fillInPreparedStatement(PreparedStatement ps, Rule rule, int col, int forWhat, int inputArg) throws SQLException
	{
		int column=(col<1?1:col);
		
		if (forWhat==0 || forWhat==6)
		{
			ps.setString(column, rule.getHead().getRelationName());
			column++;
		}		
		
		for (int i=0,len=rule.getBodyLiterals().size();i<len;i++)
		{
			if (!rule.getBodyLiterals().get(i).getRelation().isAuxiliary())
			{
				ps.setString(column, rule.getBodyLiterals().get(i).getRelationName());
				column++;
			}
		}
		
		if (forWhat==6 && rule.bindsHeadVariables() && inputArg==0)
		{
			for (int i=0,len=rule.getBodyLiterals().size();i<len;i++)
			{
				if (!rule.getBodyLiterals().get(i).getRelation().isAuxiliary())
				{
					ps.setString(column, rule.getBodyLiterals().get(i).getRelationName());
					column++;
				}
			}
			
		}
		
		if (forWhat==1 ||forWhat==4 ||forWhat==5)
		{
			ps.setString(column, rule.getHead().getRelationName());
			if (inputArg==0 && rule.bindsHeadVariables())
			{
				column++;
				ps.setString(column, rule.getHead().getRelationName());
				
			}
		}
		return ps;

	}
	

	public String[] getTypeConstants(String target, int numOfTries, int arg) throws SQLException
	{
		String sql="SELECT arg2 FROM (SELECT f1.arg2, count(*) c FROM "+headTbl+" f0, "+baseTbl+" f1 WHERE f0.relation='"+target+
						"' AND f1.relation='type' AND f0.arg"+arg+"=f1.arg1 GROUP BY f1.arg2 ORDER BY c DESC) WHERE rownum<="+numOfTries;
			
		ResultSet rs=stmt.executeQuery(sql);
		//rsCounter++;
		//System.out.println(rsCounter);
		String[] ans=new String[numOfTries];
		int i=0;
		while (rs.next())
		{
			ans[i]=rs.getString(1);
			i++;
		}
		rs.close();
		
		return ans;		
	}

	public float getVarMult(String target,int arg) throws SQLException
	{
		String sql="SELECT VARIANCE(mult) FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation='"+target+"' GROUP BY f0.arg"+arg+")";
		
		ResultSet rs=stmt.executeQuery(sql);
		//rsCounter++;
		//System.out.println(rsCounter);
		rs.next();
		float ans=rs.getFloat(1);
		rs.close();
		return ans;
	}
	public float getParamForGeneralityRatio(String target, int inputArg) throws SQLException
	{
		String tbl1, tbl2, sql;
		float param1=0,param2=0;
		ResultSet rs;
		//rsCounter++;
		//System.out.println(rsCounter);
		if (inputArg!=2)
		{
			tbl1="(SELECT count(*) denom FROM (SELECT arg1 FROM "+headTbl+" WHERE relation='"+target+"' GROUP BY arg1))";
			tbl2="(SELECT count(*) nom FROM (SELECT arg1 FROM "+baseTbl+" WHERE relation='"+target+"' GROUP BY arg1))";			
			sql="SELECT nom/denom FROM"+tbl1+", "+tbl2;
			rs=stmt.executeQuery(sql);
			rs.next();
			param1=rs.getFloat(1);
			rs.close();
		}
		if (inputArg!=1)
		{
			tbl1="(SELECT count(*) denom FROM (SELECT arg2 FROM "+headTbl+" WHERE relation='"+target+"' GROUP BY arg2))";
			tbl2="(SELECT count(*) nom FROM (SELECT arg2 FROM "+baseTbl+" WHERE relation='"+target+"' GROUP BY arg2))";			
			sql="SELECT nom/denom FROM"+tbl1+", "+tbl2;
			rs=stmt.executeQuery(sql);
			rs.next();
			param2=rs.getFloat(1);
			rs.close();
		}
		switch(inputArg)
		{
		case 1:
			return param1;
		case 2:
			return param2;
		default://0
			return (param1>param2?param1:param2);
		}
		

	}
	

}

