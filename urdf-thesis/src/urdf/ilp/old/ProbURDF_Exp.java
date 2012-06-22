package urdf.ilp.old;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import urdf.api.UArgument;
import urdf.api.UBindingSet;
import urdf.api.UEntity;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.api.UHardRule;
import urdf.api.ULineageAnd;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.URule;
import urdf.api.USoftRule;
import urdf.main.URDF_main;
import urdf.reasoner.MAXSAT;
import urdf.reasoner.Sampling;
import urdf.reasoner.URDF;
import urdf.tools.Gaussian;
import urdf.tools.RuleParser;



public class ProbURDF_Exp 
{
	private static int constId = 0;

	public static int EXPANSION_MODE = 0;

	public static int NOISE_LEVEL_RULES = 0, NOISE_LEVEL_FACTS = 0, DUMMY_POOL = 1000;

	/**
	 * @param iniFile
	 * @param caseNumber 0: onlySpecNoConst 1:noSpecNoConst 2: specTypesNoConst 3:  specTypesConst
	 * @param conf
	 * @throws Exception
	 */
	public ProbURDF_Exp(String iniFile,String file) throws Exception
	{			
		//===========================================================
	    PrintStream Sysout = new PrintStream(System.out, true, "UTF-8"); // for UTF-8 console output

	    ArrayList<USoftRule> softRules = new ArrayList<USoftRule>();
	    ArrayList<UHardRule> hardRules = new ArrayList<UHardRule>();
	    ArrayList<UQuery> queries = new ArrayList<UQuery>();
	    
	    InputStream inStream = new FileInputStream(file);
	    RuleParser.parseAll(inStream, softRules, hardRules, queries);
	    //RuleParser.parseAll(new URDF_main().getClass().getClassLoader().getResource(file).openStream(), softRules, hardRules, queries);

	    int i, j;
	    long time;

	    List<UFactSet> resultFacts = new ArrayList<UFactSet>();
	    List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
	    ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();

	    URDF urdf = new URDF(iniFile, softRules, hardRules);

	    // DEMO MODE: Ground all atoms and rules, display lineage, warm up cache
	    long totalTime = System.currentTimeMillis();

	    for (i = 0; i < queries.size(); i++) {
	      UQuery query = queries.get(i);

	      // Ground the query top-down
	      Sysout.println("\nQUERY " + i + "\t\t\t" + query);

	      time = System.currentTimeMillis();
	      urdf.sortBySelectivity(query, new UBindingSet());
	      Sysout.println("SORT [" + (System.currentTimeMillis() - time) + "ms]\t\t" + query.size() + " ATOMS");

	      time = System.currentTimeMillis();
	      urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, true, true,
	          new ULineageAnd(null), resultLineage);
	      Sysout.println("GROUND [" + (System.currentTimeMillis() - time) + "ms]\t\t" + resultFacts.size() + " DISTINCT RESULT SETS");

	      time = System.currentTimeMillis();
	      urdf.invertRules(urdf.globalDependencyGraph);
	      Sysout.println("INV-RULES [" + (System.currentTimeMillis() - time) + "ms]\t\t" + urdf.softRuleGroundings.size() + " GROUNDED SOFT RULES, "
	          + urdf.hardRuleGroundings.size() + " GROUNDED HARD RULES, " + urdf.globalDependencyGraph.size() + " FACTS");

	      time = System.currentTimeMillis();
	      new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).processMAXSAT();
	      Sysout.println("MAX-SAT [" + (System.currentTimeMillis() - time) + "ms]");

	      // This is a simple PW-based sampling algorithm
	      time = System.currentTimeMillis();

	      Sampling.getConfAll(urdf.globalDependencyGraph);
	      Sampling.getConfAll(resultLineage);

	      Sysout.println("PW-CONF [" + (System.currentTimeMillis() - time) + "ms]");
	      Sysout.println("SLD STEPS: " + URDF.SLD_steps);
	      
		  // ==========================================				
		  storeResults(resultFacts,resultLineage, query);				
		  // ==========================================

//	      j = 0;
//	      for (ULineageAnd result : resultLineage) {
//	        Sysout.println("\nRESULT [" + i + "|" + (j + 1) + "/" + resultLineage.size() + "]\t\t");
//	        Sysout.print(result.toString(-1));
//	        // result.writeGraphVizFile(i); // write a graph viz file including the hard rule lineage
//	        j++;
//	      }
//
//	      Sysout.println();
//
//	      j = 0;
//	      for (UFactSet hardRule : urdf.hardRuleGroundings) {
//	        // if (hardRule.size() > 1)
//	        Sysout.println("HARDRULE[" + i + "|" + (j + 1) + "/" + urdf.hardRuleGroundings.size() + "]\t" + hardRule);
//	        j++;
//	      }
//
//	      j = 0;
//	      for (UGroundedSoftRule softRule : urdf.softRuleGroundings) {
//	        // if (softRule.size() > 0)
//	        Sysout.println("SOFTRULE[" + i + "|" + (j + 1) + "/" + urdf.softRuleGroundings.size() + "]\t" + softRule);
//	        j++;
//	      }

	      // Use this to run comparisons with Alchemy (needs to be installed on your machine, see MLN.java)
	      // String file = "urdf_" + i;
	      // System.out.println(MLN_Interface.run(file, query, urdf.globalDependencyGraph, softRules, urdf.softRuleGroundings, urdf.hardRuleGroundings));
	      // System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(urdf.globalDependencyGraph));
	      // System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(MLN.getTruthAssignmentsFromFile(file,
	      // urdf.globalDependencyGraph)));

	      resultFacts.clear();
	      resultBindings.clear();
	      resultLineage.clear();
	      urdf.softRuleGroundings.clear();
	      urdf.hardRuleGroundings.clear();
	      urdf.clear();

	      System.gc();
	    }
	    totalTime = System.currentTimeMillis() - totalTime;
	    Sysout.println("overall time: " + totalTime + " ms");
	    urdf.close();
		
		
		
		
		//===========================================================

//			PrintStream Sysout = new PrintStream(System.out, true, "UTF-8"); // for UTF-8 console output
//
//			ArrayList<USoftRule> softRules = new ArrayList<USoftRule>();
//			ArrayList<UHardRule> hardRules = new ArrayList<UHardRule>();
//			ArrayList<UQuery> queries = new ArrayList<UQuery>();
//
//			System.out.println(System.getProperty("java.class.path"));
//			RuleParser.parseAll(new URDF_main().getClass().getClassLoader().getResource(file).openStream(), softRules, hardRules, queries);
//			
//			int i, j;
//			long time;
//
//			List<UFactSet> resultFacts = new ArrayList<UFactSet>();
//			List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
//			ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();
//
//			URDF urdf = new URDF(iniFile, softRules, hardRules);
//
//			// DEMO MODE: Ground all atoms and rules, display lineage, warm up cache
//			long totalTime = System.currentTimeMillis();
//			
//			for (i = 0; i < queries.size(); i++) 
//			{
//			//for (i = 0; i < 1; i++) {
//				UQuery query = queries.get(i);
//				//UQuery query = new UQuery("Q1", new ULiteral(URelation.LIVESIN, "Bill_Gates", "?X"), new ULiteral(URelation.LOCATEDIN, "?X", "?Y"));
//
//				// Ground the query top-down
//				Sysout.println("\nQUERY " + i + "\t\t\t" + query);
//
//				time = System.currentTimeMillis();
//				urdf.sortBySelectivity(query, new UBindingSet());
//				Sysout.println("SORT [" + (System.currentTimeMillis() - time) + "ms]\t\t" + query.size() + " ATOMS");
//
//				time = System.currentTimeMillis();
//				urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, false,
//				    new ULineageAnd(null), resultLineage);
//				Sysout.println("GROUND [" + (System.currentTimeMillis() - time) + "ms]\t\t" + resultFacts.size() + " DISTINCT RESULT SETS");
//
//				time = System.currentTimeMillis();
//				urdf.invertRules(urdf.globalConstants, urdf.globalDependencyGraph);
//				Sysout.println("INV-RULES [" + (System.currentTimeMillis() - time) + "ms]\t\t" + urdf.softRuleGroundings.size() + " GROUNDED SOFT RULES, "
//				    + urdf.hardRuleGroundings.size() + " GROUNDED HARD RULES, " + urdf.globalDependencyGraph.size() + " FACTS");
//
//				time = System.currentTimeMillis();
//				
//				
//				
//				new MAXSAT(urdf.softRuleGroundings,urdf.hardRuleGroundings,urdf.invSoftRules).processMAXSAT();
//				Sysout.println("MAX-SAT [" + (System.currentTimeMillis() - time) + "ms]");
//
//				// This is a simple PW-based sampling algorithm
//				time = System.currentTimeMillis();
//				Sampling.getConfAll(resultLineage);
//				Sysout.println("PW-CONF [" + (System.currentTimeMillis() - time) + "ms]");
//				Sysout.println("SLD STEPS: " + URDF.SLD_steps);
//				
//				// ==========================================				
//				storeResults(resultFacts,resultLineage, query);				
//				// ==========================================
//
///*				j = 0;
//				for (ULineageAnd result : resultLineage) 
//				{
//					Sysout.println("\nRESULT [" + i + "|" + (j + 1) + "/" + resultLineage.size() + "]\t\t");
//					Sysout.print(result.toString(-1));
//					// result.writeGraphVizFile(i); // write a graph viz file including the hard rule lineage					
//					j++;
//				}
//
//				Sysout.println();
//
//				j = 0;
//				for (UFactSet hardRule : urdf.hardRuleGroundings) {
//					// if (hardRule.size() > 1)
//					Sysout.println("HARDRULE[" + i + "|" + (j + 1) + "/" + urdf.hardRuleGroundings.size() + "]\t" + hardRule);
//					j++;
//				}
//
//				j = 0;
//				for (UGroundedSoftRule softRule : urdf.softRuleGroundings) {
//					// if (softRule.size() > 0)
//					Sysout.println("SOFTRULE[" + i + "|" + (j + 1) + "/" + urdf.softRuleGroundings.size() + "]\t" + softRule);
//					j++;
//				}*/
//
//				// Use this to run comparisons with Alchemy (needs to be installed on your machine, see MLN.java)
//				// String file = "urdf_" + i;
//				// System.out.println(MLN_Interface.run(file, query, urdf.globalDependencyGraph, softRules, urdf.softRuleGroundings, urdf.hardRuleGroundings));
//				// System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(urdf.globalDependencyGraph));
//				// System.out.println(new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules).evalMaxSat(MLN.getTruthAssignmentsFromFile(file,
//				// urdf.globalDependencyGraph)));
//
//				resultFacts.clear();
//				resultBindings.clear();
//				resultLineage.clear();
//				urdf.softRuleGroundings.clear();
//				urdf.hardRuleGroundings.clear();
//				urdf.clear();
//
//				System.gc();
//			}
//			totalTime = System.currentTimeMillis() - totalTime;
//			Sysout.println("overal time: " + totalTime + " ms");
//			urdf.close();
	}
	private void storeResults(List<UFactSet> resultFacts,ArrayList<ULineageAnd> resultLineage, UQuery query) throws SQLException, IOException
	{
		String relation,arg1,arg2;
		int factsInDBOnly=0,factsInDBAndRules=0,factsFromRulesOnly=0,sum;
		double factBaseConf,factConf;
		
		relation=query.getLiterals().get(0).getRelationName();
		
		FileWriter fstream1 = new FileWriter("/home/chteflio/MasterThesis/"+relation+"OnlyRules.out");
		FileWriter fstream2 = new FileWriter("/home/chteflio/MasterThesis/"+relation+"RulesAndDB.out");
		
		BufferedWriter out1 = new BufferedWriter(fstream1);
		BufferedWriter out2 = new BufferedWriter(fstream2);
		

		
		for (int i=0,len=resultFacts.size();i<len;i++) 
		{
			factBaseConf=resultFacts.get(i).getFact(0).getBaseConfidence();
			factConf=resultLineage.get(i).getConf();
			arg1=resultFacts.get(i).getFact(0).getFirstArgumentName();
			arg2=resultFacts.get(i).getFact(0).getSecondArgumentName();
			
			//System.out.println(relation+"("+arg1+", "+arg2+")@"+factConf);
			
			if (resultFacts.get(i).getFact(0).getTruthValue()==1)// if I pass the Max-Sat
			{
				if (factBaseConf==-1) // the current fact is created by a rule only
				{
					System.out.println("fact base conf:"+factBaseConf);
					System.out.println("fact final conf:"+factConf);
					factsFromRulesOnly++;
					out1.write(relation+"("+arg1+", "+arg2+")@"+factConf+"\n");
				}
				else if (factBaseConf!=factConf) // the current fact is created by a rule but exists also in the DB
				{
					System.out.println("fact base conf:"+factBaseConf);
					System.out.println("fact final conf:"+factConf);
					factsInDBAndRules++;
					out2.write(relation+"("+arg1+", "+arg2+")@"+factConf+"\n");
				}
				else // the current fact exists  in the DB and is not created by any rule
				{
					factsInDBOnly++;
				}
			}
			

						
		}
		System.out.println("/home/chteflio/MasterThesis/"+relation+"RulesAndDB.out");		
		System.out.println("Result set size: "+resultFacts.size());
		System.out.println("Facts produced by rules only: "+factsFromRulesOnly);
		System.out.println("Facts produced by rules and DB: "+factsInDBAndRules);
		System.out.println("Facts in DB only: "+factsInDBOnly);
		
		out1.write("\n \n");
		out1.write("Facts produced by rules only: "+factsFromRulesOnly+"\n");
		out1.write("Facts produced by rules and DB: "+factsInDBAndRules+"\n");
		sum=factsFromRulesOnly+factsInDBAndRules;
		out1.write("Sum these two: "+sum+"\n");
		out1.write("Facts in DB only: "+factsInDBOnly+"\n");
		
		out2.write("\n \n");
		out2.write("Facts produced by rules only: "+factsFromRulesOnly+"\n");
		out2.write("Facts produced by rules and DB: "+factsInDBAndRules+"\n");
		out2.write("Sum these two: "+sum+"\n");
		out2.write("Facts in DB only: "+factsInDBOnly+"\n");
		
		out1.flush();
		out2.flush();
		out1.close();
		out2.close();
		
		
	}
	
	
	public static void main(String[] args)
	{
		try 
		{
			//String file="/home/chteflio/MasterThesis/bornInsupp0.0010_conf0.1_spec0.15_possPos1ForURDF.txt";
			String file="/home/chteflio/MasterThesis/livesIn.txt";
			//String file="D:/My Workspace/JavaWorkspace/URDF/livesIn.txt";
			//String file="D:/My Workspace/JavaWorkspace/URDF/withConfidence/spec0.15/bornIn.txt";
			ProbURDF_Exp exp = new ProbURDF_Exp (args[0],file);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
