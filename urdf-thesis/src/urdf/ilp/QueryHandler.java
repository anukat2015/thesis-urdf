package urdf.ilp;

import java.sql.SQLException;
import java.util.ArrayList;


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
public class QueryHandler 
{
	private static Logger logger = Logger.getLogger(LearningManager.queriesLoggerName);
	
	private Statement stmt;
	private RelationsInfo relationsInfo;
	
	 
	public QueryHandler(Connection conn, String baseTbl, String headTbl) throws Exception {
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		this.stmt = (Statement) conn.createStatement();
	}
	
	public QueryHandler(Connection conn, RelationsInfo relationsInfo) throws Exception {
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		this.relationsInfo = relationsInfo;
		this.stmt = (Statement) conn.createStatement();
	}

	//***************************** FOR RULELEARNER ********************************************
	
	/**
	 * @param forWhat 0: for PositivesCovered, 1: for examplesCovered 2: for body 4: for mult1 5: for mult2 6: for possiblePosToBeCovered
	 * @param rule
	 * @param inputArg 0: both, 1: first, 2: second
	 * @return
	 * @throws SQLException 
	 * @throws Exception
	 */
	public float calcRuleProperties(Rule rule, int forWhat, int inputArg) throws IllegalArgumentException, SQLException {
		String sparql, info;
		ResultSet rs;
		float result = -1;
		
		switch(forWhat){
			case 0:	// positivesCovered
				sparql = rule.positivesCoveredQuery();
				info = "PositvesCovered = "+rule.getRuleString();
				System.out.println(sparql);
				rs = (ResultSet) stmt.executeQuery(sparql);
				if (rs.next()) 
					result = rs.getFloat(2);		
				break;
				
			case 1: // examplesCovered
				sparql = rule.examplesCoveredQuery(inputArg);
				info = "ExamplesCovered = "+rule.getRuleString();
				rs = (ResultSet) stmt.executeQueryCountRows(sparql);
				if (rs.next()) 
					result = rs.getFloat(1);		
				break;
				
			case 2: // body
				sparql = rule.bodySupportQuery();
				info = "BodySize = "+rule.getRuleString();
				rs = (ResultSet) stmt.executeQuery(sparql);
				if (rs.next()) 
					result = rs.getFloat(2);
				break;
				
			case 6: // possiblePositivesToBeCovered
				sparql = rule.possiblePositivesToBeCoveredQuery(inputArg);
				info = "PossiblePositivesToBeCovered = "+rule.getRuleString();
				rs = (ResultSet) stmt.executeQueryCountRows(sparql);
				if (rs.next()) 
					result = rs.getFloat(1);		
				break;
				
			default: 
				throw new IllegalArgumentException("forWhat argument should be:  0=PositivesCovered, 1=examplesCovered 2=body 4=mult1 5=mult2 6=possiblePosToBeCovered");

		}
		logger.log(Level.INFO, info);
		
		rs.close();
		return result;
	}
	
	public float[] getBodyAvgMultAndVar(Rule rule, int inputArg) throws SQLException {
		String selectArg = (inputArg==1)? rule.getHead().getFirstArgumentVariable() : rule.getHead().getSecondArgumentVariable();
		String patterns = rule.getBodyPatterns();
		patterns = patterns.substring(0,patterns.length()-2);

		String sparql = "SELECT COUNT "+selectArg+" WHERE {"+patterns+"}";
		String info = "Get Avg(Mult) and Var(Mult) from arg"+ inputArg + " for  " + rule.getRuleString();
		logger.log(Level.INFO, info);

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

		return result;
	}
	
	public float[] getBodyAvgMultAndVar(String relation, int inputArg) throws SQLException {

		String sparql = "SELECT COUNT ?arg"+inputArg+" WHERE {?arg1 "+relation+" ?arg2}";

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

		return result;
	}
	
	public ArrayList<String> findConstants(Rule rule, int possiblePosToBeCoveredThershold, int positivesCoveredThreshold, float supportThreshold, int factsForHead, int inputArg,int depthOfRule) throws SQLException
	{
		/*String arg,put, finalClause, key;
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
		return outConstants;	*/
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
	
	public int calcOverlap(Rule rule1, Rule rule2) throws SQLException {
		String sparql = "SELECT count ?count WHERE {" + rule1.getBodyPatterns() + rule2.getBodyPatterns() + "}";
		String info = "Calculate Overlap of  "+ rule1.getRuleString() + " and " + rule2.getRuleString();
		logger.log(Level.INFO, info);
		
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


	public int fireSampleQuery(Relation relation) throws SQLException
	{
		String sparql = "SELECT count ?count WHERE {?s "+relation.getName()+" ?o}";
		String info = "Sample query for "+relation.getName();
		logger.log(Level.INFO, info);
		
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		if (rs.next()) {
			int result = rs.getInt(2);
			rs.close();
			return result;
		}
		rs.close();
		return -1;		
	}
	
	public String[] getTypeConstants(String target, int numOfTries, int arg) throws SQLException {
		String sparql = "SELECT count ?type WHERE { ?arg1"+target+"?arg2 . ?arg"+arg+" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type } order by desc(count) limit "+numOfTries;	
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

	// TODO Substitute for RelationsInfo.getVar(arg)
	/*public float getVarMult(String relationName, int arg) throws SQLException {
		return relationsInfo.getRelationFromRelations(relationName).getVar(arg);
	}*/
	
	public float getParamForGeneralityRatio(String target, int inputArg) throws SQLException {	
		String info = "GeneralityRation (#distinctRatio arg"+inputArg+") from "+target;
		logger.log(Level.INFO, info);
		
		Relation targetInfo = relationsInfo.getRelationFromRelations(target);
		if (inputArg==1 || inputArg==2) {
			String sparql = "SELECT DISTINCT ?arg"+inputArg+" WHERE { ?arg1 "+target+" ?arg2 }";
			
			float nominator = 0;
			float denominator = targetInfo.getDistinctEntities(inputArg);
			
			ResultSet rs = (ResultSet) stmt.executeQueryCountRows(sparql);
			
			if (rs.next()) 
				nominator = rs.getFloat(1);		
				
			rs.close();
			
			return nominator/denominator;
		}
		else {
			float generalityRatio1 = targetInfo.getDistinctEntities(1);
			float generalityRatio2 = targetInfo.getDistinctEntities(2);
			return Math.max(generalityRatio1, generalityRatio2);
		}
	}
	
}

