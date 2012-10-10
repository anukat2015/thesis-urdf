package urdf.ilp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import urdf.rdf3x.Connection;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;



/**
 * 	@author Christina Teflioudi
 * 	The class QueryHandler generates and fires all queries to the DB. All communication with the DB is through this class.
 */
@SuppressWarnings("unused")
public class QueryHandler 
{
	private static Logger logger = Logger.getLogger(LearningManager.queriesLoggerName);
	
	private Statement stmt;
	
	public QueryHandler(Connection conn, String baseTbl, String headTbl) throws Exception {
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		this.stmt = (Statement) conn.createStatement();
	}
	
	public QueryHandler(Connection conn) throws Exception {
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		this.stmt = (Statement) conn.createStatement();
	}

	
	
	//***************************** FOR RULELEARNER ********************************************

	public int calculatePositivesCovered(Rule rule, int inputArg) {		
		int result = -1;	
		String sparql = rule.positivesCoveredQuery();
		try {
			ResultSet rs = (ResultSet) stmt.executeQuery(sparql);		
			if (rs.next()) 
				result = rs.getInt(1);		
		
			logger.log(Level.DEBUG,  "PositvesCovered="+result+" from:"+rule.getRuleString());
			rs.close();
			return result;
		} catch (SQLException e) {
			logger.log(Level.ERROR, sparql + "\n" + e.getMessage());
			//e.printStackTrace();
			return 0;
		}
	}
	
	public int calculateExamplesCovered(Rule rule, int inputArg) {		
		int result = -1;	
		String sparql = rule.examplesCoveredQuery(inputArg);
		try {
			ResultSet rs = (ResultSet) stmt.executeQuery(sparql);		
			if (rs.next()) 
				result = rs.getInt(1);		
	
			logger.log(Level.DEBUG,  "ExamplesCovered="+result+" from:"+rule.getRuleString());
			rs.close();
			return result;
		} catch (SQLException e) {
			logger.log(Level.ERROR, sparql + "\n" + e.getMessage());
			//e.printStackTrace();
			return 0;
		}
	}
	
	public int calculatePossiblePositivesToBeCovered(Rule rule, int inputArg) {		
		int result = -1;	
		String sparql = rule.possiblePositivesToBeCoveredQuery(inputArg);
		try {
			ResultSet rs = (ResultSet) stmt.executeQuery(sparql);		
			if (rs.next()) 
				result = rs.getInt(1);		
	
			logger.log(Level.DEBUG,  "PossiblePositivesToBeCovered="+result+" from:"+rule.getRuleString());
			rs.close();
			return result;
		} catch (SQLException e) {
			logger.log(Level.ERROR, sparql + "\n" + e.getMessage());
			//e.printStackTrace();
			return 0;
		}
	}
	
	public int calculateBodySize(Rule rule, int inputArg) {		
		int result = -1;	
		String sparql = rule.bodySizeQuery();
		if (sparql.equalsIgnoreCase("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/diedIn> ?C . ?D <http://yago-knowledge.org/resource/livesIn> ?C . ?D <http://yago-knowledge.org/resource/wasBornOnDate> ?E . ?B <http://yago-knowledge.org/resource/wasCreatedOnDate> ?E }") ||
			sparql.equalsIgnoreCase("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/diedIn> ?C . ?D <http://yago-knowledge.org/resource/wasBornIn> ?C . ?D <http://yago-knowledge.org/resource/diedOnDate> ?E . ?B <http://yago-knowledge.org/resource/wasCreatedOnDate> ?E }") ||
			sparql.equalsIgnoreCase("SELECT COUNTDISTINCT ?A ?B WHERE {?A <http://yago-knowledge.org/resource/directed> ?B . ?A <http://yago-knowledge.org/resource/diedIn> ?C . ?D <http://yago-knowledge.org/resource/wasBornIn> ?C . ?D <http://yago-knowledge.org/resource/diedOnDate> ?E . ?B <http://yago-knowledge.org/resource/startedOnDate> ?E }"))
			return 0;
		try {
			ResultSet rs = (ResultSet) stmt.executeQuery(sparql);		
			if (rs.next()) 
				result = rs.getInt(1);		
	
			logger.log(Level.DEBUG,  "BodySize="+result+" from:"+rule.getRuleString());
			rs.close();
			return result;
		} catch (SQLException e) {
			logger.log(Level.ERROR, sparql + "\n" + e.getMessage());
			//e.printStackTrace();
			return 0;
		}
	}

	public float[] calculateBodyAvgMultAndVar(Rule rule, int inputArg) throws SQLException {
		
		logger.log(Level.INFO, "Get Body Avg(Mult) and Var(Mult) from arg"+ inputArg + " for  " + rule.getRuleString());
		
		String selectArg;
		switch (inputArg) {
			case 1: selectArg = rule.getHead().getFirstArgumentVariable(); break;
			case 2: selectArg = rule.getHead().getSecondArgumentVariable(); break;
			default: throw new SQLException("Invalid inputArg="+inputArg+", it should be 1 or 2");			
		}
		String patterns = rule.getBodyPatterns();
		patterns = patterns.substring(0,patterns.length()-2);

		String sparql = "SELECT COUNT "+selectArg+" WHERE {"+patterns+"}";

		ResultSet rs=(ResultSet) stmt.executeQuery(sparql);
		
		float avg = 0; 
		int count = 0;
		while (rs.next()) {
			avg  += rs.getFloat(2);
			count++;
		}
		avg /= (float) count;
		
		rs.beforeFirst();		
		float var = 0;
		while (rs.next()) {
			var += (float) Math.pow(rs.getFloat(2)-avg, 2);
		}
		var /= ((float) count);
		
		rs.close();
		
		float result[] = new float[2];
		result[0] = avg;
		result[1] = var;
		
		logger.log(Level.DEBUG, "Avg="+avg+" Var="+var);

		return result;
	}
	
	public ArrayList<String> findConstants(Rule rule, int possiblePosToBeCoveredThershold, int positivesCoveredThreshold, float supportThreshold, int factsForHead, int inputArg,int numOfTries) throws SQLException {
		int variable;
		switch (inputArg) {
			case 1:  variable = rule.getHead().getSecondArgument(); break;
			case 2:  variable = rule.getHead().getFirstArgument(); break;
			default: throw new IllegalArgumentException("Illegal inputArg="+inputArg+", it should be 1 or 2");
		}
		
		return findConstantsForVariable(rule, possiblePosToBeCoveredThershold, positivesCoveredThreshold, supportThreshold, factsForHead, variable, numOfTries);	
	}
	
	public ArrayList<String> findConstantsForVariable(Rule rule, int possiblePosToBeCoveredThershold, int positivesCoveredThreshold, float supportThreshold, int factsForHead, int variable,int numOfTries) throws SQLException {
		logger.log(Level.INFO, "Finding constants for variable ?"+(char)variable+" from rule "+rule.getRuleString());
		
		String sparql = rule.findConstantsQuery(variable) + " LIMIT " + numOfTries;	
		
		ArrayList<String> results = new ArrayList<String>();
		float threshold = Math.min(possiblePosToBeCoveredThershold, supportThreshold*factsForHead);
		ResultSet  rs = (ResultSet) stmt.executeQuery(sparql);
		while (rs.next() && rs.getInt(2) >= threshold) { 
			results.add(rs.getString(1));
			System.out.println(rs.getString(1));
		}

		return results;	
	}
	
	public int calculateOverlap(Rule rule1, Rule rule2) throws SQLException {

		logger.log(Level.INFO, "Calculate Overlap of  "+ rule1.getRuleString() + " and " + rule2.getRuleString());
		
		String sparql = "SELECT count ?count WHERE {" + rule1.getBodyPatterns() + rule2.getBodyPatterns() + "}";

		int depthOfRule1=0;
		for (int i=0,len=rule1.getBodyLiterals().size();i<len;i++){
			if (rule1.getBodyLiterals().get(i).getRelation().isAuxiliary())
				continue;
			depthOfRule1++;
		}
		
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		if (rs.next()) {
			int result = rs.getInt(2);
			rs.close();
			return result;
		}
		rs.close();
		return -1;
	}
	
	public int calculateHeadSize(Rule rule) throws SQLException {
		return calculateRelationSize(rule.getHead().getRelation(), rule.getConstantInArg(), rule.getConstant());
	}
	
	public int calculateRelationSize(Relation relation) throws SQLException {
		return calculateRelationSize(relation, 0, null);
	}


	public int calculateRelationSize(Relation relation, int inputArg, String constant) throws SQLException {
		
		logger.log(Level.INFO, "Calculating Head Predicate size "+relation.getName()+" with arg"+inputArg+"="+constant);
		
		String sparql = "SELECT count ?count WHERE {?s "+relation.getName()+" ?o}";
		
		if (constant!=null && inputArg>=0) {
			if (inputArg==1) sparql = sparql.replace("?s", constant);
			if (inputArg==2) sparql = sparql.replace("?o", constant);		
		}
			
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		if (rs.next()) {
			int result = rs.getInt(2);
			rs.close();
			return result;
		}
		rs.close();
		return -1;		
	}
	
	public String[] retrieveTypeConstants(String target, int numOfTries, int arg) throws SQLException {
		
		logger.log(Level.DEBUG, "Retrieving top-"+numOfTries+" types for arg"+arg+" from relation "+target);
		
		String sparql = "SELECT count ?type WHERE { ?arg1 "+target+" ?arg2 . ?arg"+arg+" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type } order by desc(count) limit "+numOfTries;	
		String info = "Get Top-"+numOfTries+" Types for arg"+arg+" in "+target;
	
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		
		String[] ans = new String[numOfTries];
		int i=0;
		while (rs.next()) {
			ans[i] = rs.getString(1);
			info += "\n"+ans[i];
			i++;
			
		}
		logger.log(Level.INFO, info);
		
		rs.close();
		return ans;		
	}
	
	public ResultSet retriveLiteralDistribution(Rule rule, int literalArg) throws SQLException {
		logger.log(Level.DEBUG, "Retrieving literal(?"+(char)literalArg+") distribution from rule: "+rule.getRuleString());
		
		if (literalArg<65 || literalArg>=rule.getNextVariableNumber()) {
			throw new IllegalArgumentException("Variable ?"+(char)literalArg+" doesn't exist in rule: "+rule.getRuleString());
		}
		
		String body = rule.getBodyPatterns();
		body = body.substring(0,body.length()-2);
		
		String query = "SELECT COUNT ?"+(char)literalArg+" WHERE {{"+ body + "} match {" + rule.getHeadPattern() + "}} ORDER BY ASC(?"+(char)literalArg+") DESC(?match)";
		return (ResultSet) stmt.executeQuery(query);
	}
	
	public ResultSet retrieveHeadOfNumericConstant(Rule rule, int literalArg) throws SQLException {
		logger.log(Level.DEBUG, "Retrieving distribution on (?"+(char)literalArg+") from head: "+rule.getHeadPattern());
		
		ArrayList<Literal> bLits = rule.getBodyLiterals();
		Literal prop = bLits.get(bLits.size()-1);
		Literal head = rule.getHead();
		if (prop.getSecondArgument()!=literalArg || (prop.getFirstArgument()!=head.getFirstArgument() && prop.getFirstArgument()!= head.getSecondArgument())){
			throw new IllegalArgumentException("Variable ?"+(char)literalArg+" doesn't exist in head: "+rule.getHeadPattern());
		}

		String sparql = "SELECT COUNT ?"+(char)literalArg+" WHERE {"+rule.getHeadPattern()+prop.getSparqlPattern()+"} ORDER BY ASC(?"+(char)literalArg+")"; 

		return (ResultSet) stmt.executeQuery(sparql);
	}
	
	public ResultSet executeQuery(String query) throws SQLException {
		return (ResultSet) stmt.executeQuery(query);
	}
	
	public ResultSet retrieveDistribution(Literal x, Collection<Literal> collection) throws SQLException {
		logger.log(Level.DEBUG, "Retrieving distribution on "+x.getRelationName());
		
		String patterns = x.getSparqlPatternWithConstant();
		if (collection!= null || collection.isEmpty()) {
			for (Literal l: collection) 
				patterns += l.getSparqlPatternWithConstant();
		}
		
		String sparql = "SELECT COUNT ?"+(char)x.getSecondArgument()+" WHERE {"+patterns+"} ORDER BY ASC(?"+(char)x.getSecondArgument()+")";
		
		return (ResultSet) stmt.executeQuery(sparql);
	}
	
	public ResultSet retrieveGroupDistribution(Literal x, Literal group) throws SQLException {
		logger.log(Level.DEBUG, "Retrieving group distribution on "+x.getRelationName()+" by "+group.getRelationName());
		
		String patterns = x.getSparqlPatternWithConstant();
		if (x.getFirstArgument() == group.getFirstArgument()) 
			patterns += group.getSparqlPatternWithConstant();
		else
			throw new IllegalArgumentException("Literals should be joined by first argument");
		
		String sparql = "SELECT COUNT ?"+(char)group.getSecondArgument()+" ?"+(char)x.getSecondArgument()+" WHERE {"+patterns+"} ORDER BY ASC(?"+(char)group.getSecondArgument()+")";
		
		return (ResultSet) stmt.executeQuery(sparql);
	}
	
}

