package urdf.ilp;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.sql.Connection;
//import java.sql.Driver;
//import java.sql.ResultSet;
//import java.sql.Statement;

import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;

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
	private String baseTbl;
	private String headTbl; 
	private RelationsInfo relationsInfo;
	
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
	
	 
	public QueryHandler(Connection conn, String baseTbl, String headTbl) throws Exception
	{
		this.baseTbl = baseTbl;
		this.headTbl = headTbl;
		this.conn = conn; 	
		this.stmt = (Statement) conn.createStatement();
	}
	
	public QueryHandler(Connection conn, RelationsInfo relationsInfo) throws Exception
	{
		this.relationsInfo = relationsInfo;
		this.conn = conn; 	
		this.stmt = (Statement) conn.createStatement();
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
		
		switch(forWhat) {
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
	 * @param forWhat 0: for PositivesCovered, 1: for examplesCovered 2: for body 4: for mult1 5: for mult2 6: for possiblePosToBeCovered
	 * @param rule
	 * @param inputArg 0: both, 1: first, 2: second
	 * @return
	 * @throws SQLException 
	 * @throws Exception
	 */
	public float calcRuleProperties(Rule rule, int forWhat, int inputArg) throws IllegalArgumentException, SQLException {
		String sparql;
		ResultSet rs;
		switch(forWhat)
		{
			case 0:	// positivesCovered
				sparql = rule.positivesCoveredQuery();
				System.out.println(sparql);
				rs = (ResultSet) stmt.executeQuery(sparql);
				if (rs.next()) return rs.getFloat(2);
				break;
				
			case 1: // examplesCovered
				sparql = rule.examplesCoveredQuery(inputArg);
				System.out.println(sparql);
				rs = (ResultSet) stmt.executeQuery(sparql);
				if (rs.last()) return (float) rs.getRow();
				break;
				
			case 2: // body
				sparql = rule.bodySupportQuery();
				System.out.println(sparql);
				rs = (ResultSet) stmt.executeQuery(sparql);
				if (rs.next()) return rs.getFloat(2);
				break;
				
			case 4: // mult1
				sparql = rule.mult1Query();
				// TODO
				break;
				
			case 5: // mult2
				sparql = rule.mult2Query();
				//TODO
				break;
				
			case 6: // possiblePositivesToBeCovered
				sparql = rule.possiblePositivesToBeCoveredQuery(inputArg);
				System.out.println(sparql);
				rs = (ResultSet) stmt.executeQuery(sparql);
				if (rs.last()) return (float) rs.getRow();
				break;
				
			default: throw new IllegalArgumentException("forWhat argument should be:  0=PositivesCovered, 1=examplesCovered 2=body 4=mult1 5=mult2 6=possiblePosToBeCovered");

		}
		return -1;
	}
	

	
	public float getBodyAvgMult(Rule rule,int inputArg) throws SQLException{
		int selectArg = (inputArg==1)? rule.getHead().getFirstArgument() : rule.getHead().getSecondArgument();
		
		String sparql = "SELECT ?count ?"+selectArg+" WHERE {"+rule.getBodyPatterns()+"}";
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		
		float sum = 0;
		while (rs.next()) {
			sum += rs.getInt(2);
		}
		return sum/rs.getRow();
	}
	

	
	public float getBodyMultVar(Rule rule,int inputArg, float avg) throws SQLException {
		int selectArg = (inputArg==1)? rule.getHead().getFirstArgument() : rule.getHead().getSecondArgument();
		String sparql = "SELECT ?count ?"+selectArg+" WHERE {"+rule.getBodyPatterns()+"}";
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		
		float var = 0;
		while (rs.next()) {
			var += Math.pow(rs.getDouble(2) - avg, 2);
		}
		return var/rs.getRow();
	}

	
	public ArrayList<String> findConstants(Rule rule, int possiblePosToBeCoveredThershold, int positivesCoveredThreshold, float supportThreshold, int factsForHead, int inputArg,int depthOfRule) throws SQLException
	{
		System.out.println("/TODO findConstants()");
		return null;
	}
 	/*public ArrayList<String> findConstants(Rule rule, String[] clauses, int possiblePosToBeCoveredThershold, int positivesCoveredThreshold, float supportThreshold, int factsForHead, int inputArg,int depthOfRule) throws SQLException
	{ 
		String arg,put, finalClause, key;
		ArrayList<String> outConstants=new ArrayList<String>();
		
		arg="arg"+rule.getHead().getRelation().getConstantInArg();
		put=(rule.getHead().getRelation().getConstantInArg()==1?"input":"output");
		finalClause="SELECT "+put+" FROM ("+clauses[0]+clauses[4]+clauses[5]+clauses[3]+") GROUP BY "+put+" HAVING count(*)>"+positivesCoveredThreshold;
		finalClause+=" INTERSECT ";
		finalClause+="SELECT "+arg+" FROM ("+clauses[7]+") GROUP BY "+arg+" HAVING count(*)>"+possiblePosToBeCoveredThershold+" OR count(*)>"+supportThreshold*factsForHead;
		key=finalClause;
		
		System.out.println(finalClause);
		
		PreparedStatement ps=checkForQueryExistance(finalClause,key, 7, rule.getBodyLiterals().size());
		ps=fillInPreparedStatement(ps, rule, 1, 0, inputArg);
		ps=fillInPreparedStatement(ps, rule, depthOfRule+2, 6, inputArg);
		ResultSet  rs = (ResultSet) ps.executeQuery();
		//rsCounter++;
		//System.out.println(rsCounter);
		while (rs.next())
		{
			outConstants.add(rs.getString(1));
		}		
		return outConstants;		
	}*/
	
	
	public int getAllCombinationsOfPairs(String target, int arg) throws SQLException
	{
		int possibleOtherEntities;
		String finalClause;
		ResultSet rs;
		finalClause = "SELECT count(*) FROM (SELECT "+(arg==1?"input":"output")+" FROM (SELECT arg1 input, arg2 output FROM "+baseTbl+" WHERE relation='"+target+"') GROUP BY "+(arg==1?"input":"output")+")";
		
		
		System.out.println(finalClause);
		rs = (ResultSet) stmt.executeQuery(finalClause);
		//rsCounter++;
		//System.out.println(rsCounter);
		rs.next();
		possibleOtherEntities=rs.getInt(1);
		rs.close();
		return possibleOtherEntities;		
	}
	
	public int calcOverlap(Rule rule1, Rule rule2) throws SQLException {
		String sparql = "SELECT count ?count WHERE {" + rule1.getBodyPatterns() + rule2.getBodyPatterns() + "}";
		
		int depthOfRule1=0;
		for (int i=0,len=rule1.getBodyLiterals().size();i<len;i++){
			if (rule1.getBodyLiterals().get(i).getRelation().isAuxiliary())
				continue;
			depthOfRule1++;
		}
		
		System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		if (rs.next())
			return rs.getInt(2);
		
		return -1;
	}

	//***************************** FIRE QUERIES ***************************************
	public int fireSampleQuery(Relation relation) throws SQLException
	{
		String sparql = "SELECT count ?count WHERE {?s "+relation.getName()+" ?o}";
		
		System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		if (rs.next())
			return rs.getInt(2);

		return -1;		
	}
	
	public String[] getTypeConstants(String target, int numOfTries, int arg) throws SQLException {
		String sparql = "SELECT count ?type WHERE { ?arg1"+target+"?arg2 . ?arg"+arg+" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type } order by desc(count) limit "+numOfTries;	
		
		System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		
		String[] ans = new String[numOfTries];
		int i=0;
		while (rs.next()) {
			ans[i] = rs.getString(1);
			i++;
		}
		rs.close();
		return ans;		
	}

	// TODO Substitute for RelationsInfo.getVar(arg)
	public float getVarMult(String relationName, int arg) throws SQLException
	{	
		if (arg!=1 && arg!=2) 
			throw new SQLException("Invalid Argument, it should be 1 or 2");

		String sparql = "SELECT count ?arg"+arg+" WHERE {?arg1 "+relationName+" ?arg2 }";

		System.out.println(sparql);
		ResultSet rs=(ResultSet) stmt.executeQuery(sparql);
		
		float avg = 0; 
		int count = 0;
		while (rs.next()) {
			avg  += rs.getFloat(2);
			count++;
		}
		
		rs.first();		
		float var = (float) Math.pow(rs.getFloat(2)-avg, 2); 
		while (rs.next()) {
			var += (float) Math.pow(rs.getFloat(2)-avg, 2);
		}
		
		return var/((float) count);
	}
	
	public float getParamForGeneralityRatio(String target, int inputArg) throws SQLException {		
		if (inputArg==1 || inputArg==2) {
			float nominator = 0;
			float denominator = relationsInfo.getRelationFromRelations(target).getDistinctEntities(inputArg);
			
			String sparql = "SELECT DISTINCT ?arg"+inputArg+" WHERE { ?arg1 "+target+" ?arg2 }";
			ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
			if (rs.last()) nominator = (float) rs.getRow();
			return nominator/denominator;
		}
		else {
			float generalityRatio1 = getAllCombinationsOfPairs(target, 1);
			float generalityRatio2 = getAllCombinationsOfPairs(target, 2);
			return Math.max(generalityRatio1, generalityRatio2);
		}
	}
	/*
	 * 
	 * 
	 *  	
	 private void initializeConnection(String iniFile) throws Exception { 
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
	
	public static Connection getConnection(String iniFile) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		DatabaseParameters p = DBConfig.databaseParameters(iniFile);
		Connection conn = null;
	    if (p.system.toLowerCase().indexOf("postgres") >= 0) 
	    {
	    
	      DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());

	      conn = DriverManager.getConnection("jdbc:postgresql://" + p.host + ":" + p.port + (p.database == null ? "" : "/" + p.database), p.user,
	          p.password);

	    } 
	    else if (p.system.toLowerCase().indexOf("oracle") >= 0) 
	    {
	    	
	      DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());

	      conn = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + p.host
	          + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + p.inst + ")(server = dedicated)))", p.user, p.password);

	    } 
	    return conn;
	}
	
	
	public float getVarMult(String target,int arg) throws SQLException
	{
		String sql="SELECT VARIANCE(mult) FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation='"+target+"' GROUP BY f0.arg"+arg+")";
		
		System.out.println(sql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sql);
		//rsCounter++;
		//System.out.println(rsCounter);
		rs.next();
		float ans = rs.getFloat(1);
		rs.close();
		return ans;
	}
	
		public String[] getTypeConstants(String target, int numOfTries, int arg) throws SQLException {
		String sparql = "SELECT count ?type WHERE { ?arg1 $rel ?arg2 . ?arg$n <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type } order by desc(cont) limit $tries";
		//String sparql = "SELECT count ?type WHERE { ?arg1 <http://yago-knowledge.org/resource/livesIn> ?arg2 . ?arg1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type } order by desc(count) limit 100";
			
		System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		//rsCounter++;
		//System.out.println(rsCounter);
		String[] ans = new String[numOfTries];
		int i=0;
		while (rs.next()){
			ans[i] = rs.getString(1);
			i++;
		}
		rs.close();
		return ans;		
	}
	 * 
	 */

	
	
	
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
	/*public String[] parseRule(Rule rule, int inputArg)
	{
		String parallelHintBaseTbl=" PARALLEL(facts,8)";
		String parallelHintSampleTbl=" PARALLEL(train11,8)";
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
		
		rule.printRule(false);
		for (int i=0; i<sqlClauses.length; i++) System.out.println("Clause["+i+"] = "+sqlClauses[i]);
		
		return sqlClauses;
	}*/


	/*public int distinctEntitiesInExamplesCovered(Rule rule,String[] clauses, int arg, int inputArg) throws SQLException
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
		
	}*/
	
	/*private float fire(PreparedStatement ps, int forWhat,Rule rule)throws SQLException
	{
		ResultSet rs = (ResultSet) ps.executeQuery();
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
	}*/
	
	

	/*public float calcRulePropertiesOld(Rule rule, String[] clauses, int forWhat, int inputArg) throws Exception
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
		
		
		rule.printRule(true);
		String s = "Query For: ";
		switch(forWhat){case 0: s="PositivesCovered"; case 1: s="examplesCovered"; case 2: s="body"; case 4: s="mult1"; case 5: s="mult2"; case 6: s="possiblePosToBeCovered";}
		System.out.println(s);
		System.out.println(finalClause);
		PreparedStatement ps=fillInPreparedStatement(checkForQueryExistance(finalClause,key,forWhat, rule.getBodyLiterals().size()),rule,1, forWhat, inputArg);
		return fire(ps,forWhat,rule);
	}*/
	
	/*public float getBodyAvgMult(Rule rule,int inputArg, String clauses[]) throws SQLException{
	String groupBy=(inputArg==1?"input":"output");
	String basic=clauses[0]+clauses[1]+clauses[2]+clauses[3];
	String finalClause="SELECT avg(mult) average FROM (SELECT count(*) mult FROM("+basic+") GROUP BY "+groupBy+")";
	String key=clauses[1]+clauses[2];
	
	rule.printRule(true);
	System.out.println(finalClause);
	
	PreparedStatement ps=fillInPreparedStatement(checkForQueryExistance(finalClause,key,12, rule.getBodyLiterals().size()),rule,1, 2, inputArg);
	float avg=fire(ps,2,rule);		
	
	return avg;	
}*/
	
	/*public float getBodyMultVar(Rule rule,int inputArg, float avg, String clauses[]) throws SQLException {
	String groupBy=(inputArg==1?"input":"output");
	String basic=clauses[0]+clauses[1]+clauses[2]+clauses[3];
	String finalClause="SELECT sum((mult-"+avg+")*(mult-"+avg+")*numOfObservations)/sum(numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM("+basic+") GROUP BY "+groupBy+") GROUP BY mult)";
	//String finalClause="SELECT Variance(mult) FROM (SELECT count(*) mult FROM("+basic+") GROUP BY "+groupBy+")";
	
	
	String key=clauses[1]+clauses[2];
	
	rule.printRule(true);
	System.out.println(finalClause);
	
	PreparedStatement ps=fillInPreparedStatement(checkForQueryExistance(finalClause,key,13, rule.getBodyLiterals().size()),rule,1, 2, inputArg);
	float var=fire(ps,2,rule);	
	
	
	return var;		
}*/
	
}

