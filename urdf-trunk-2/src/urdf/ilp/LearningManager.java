package urdf.ilp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class LearningManager 
{
	RelationPreProcessor preprocessor;
	RelationsInfo info;
	HeadSampler sampler;
	
	RuleLearner learner;
	QueryHandler queryHandler;
	ThresholdChecker tChecker;
	int noise;
	String iniFile;
	
	ArrayList<HeadPredicate> headPredicates;	
	HashMap<String,ArrayList<Rule>> rules;
	HashMap<String, RuleTreeNode> ruleNodes;
	ArrayList<Rule> goodRulesForPartition;
	
	
	int inputArg;// for sampling
	String  expDesc;
	
	
	public LearningManager(String iniFile, int partitionNumber,float supportThreshold, float confidenceThreshold,float specialityRatioThreshold, int possiblePosToBeCoveredThreshold,
			int positivesCoveredThreshold, float functionThreshold, float symmetryThreshold, int smoothingMethod, float stoppingThreshold,
			ArrayList<Relation> relations,ArrayList<Type> types, HashMap<Integer,String>relationsForConstants, int noise,int inputArg,String baseTbl, String headTbl) throws Exception
	{
		System.out.println(iniFile);
		queryHandler=new QueryHandler(iniFile, baseTbl, headTbl);
		this.iniFile=iniFile;
		
		setThresholds(supportThreshold, confidenceThreshold, specialityRatioThreshold, possiblePosToBeCoveredThreshold,
				positivesCoveredThreshold, functionThreshold, symmetryThreshold, smoothingMethod, stoppingThreshold, partitionNumber);		
		
		
		//preprocessor=new RelationPreProcessor(iniFile,tChecker,relations,types,relationsForConstants,baseTbl);		
		
		preprocessor=new RelationPreProcessor(iniFile);	
		//System.out.println("hi!");
		info=preprocessor.getRelationsInfo();	
		tChecker.setDangerousRelations(info.dangerousRelations);
		expDesc	="supp"	+supportThreshold+"_conf"+confidenceThreshold+"_spec"+specialityRatioThreshold+"_possPos"+possiblePosToBeCoveredThreshold;
		this.inputArg=inputArg;
		sampler=new HeadSampler(iniFile);
		sample(partitionNumber,false,noise);		

	}
	
 	public void createHeadPredicates(String[] rel,int[] inputArg, int depth)
	{
 		headPredicates=new ArrayList<HeadPredicate>();		
		for (int i=0,len=rel.length;i<len;i++)
		{	
			HeadPredicate hp=new HeadPredicate(info.getRelationFromRelations(rel[i]),depth, info, inputArg[i]);
			headPredicates.add(new HeadPredicate(info.getRelationFromRelations(rel[i]),depth, info, inputArg[i]));				
		}
		System.out.println("HeadPredicates created...");
	}
	
 	public void setThresholds(float supportThreshold, float confidenceThreshold,float specialityRatioThreshold, int possiblePosToBeCoveredThreshold,
			int positivesCoveredThreshold, float functionThreshold, float symmetryThreshold, int smoothingMethod, float stoppingThreshold,int partitionNumber)
	{
		tChecker=new ThresholdChecker(queryHandler, supportThreshold, confidenceThreshold, specialityRatioThreshold, possiblePosToBeCoveredThreshold,
				positivesCoveredThreshold, functionThreshold, symmetryThreshold, smoothingMethod, stoppingThreshold, partitionNumber);
	}
	
 	public void sample(int partitionNumber, boolean recreateIfExists,int noise) throws SQLException
	{
		sampler.sample(partitionNumber, recreateIfExists, noise, info, inputArg);
		this.noise=noise;
	}
	
	/**
	 * @param relationsToBeLearned
	 * @param inputArg 0: both arg 1: 1st arg 2: 2nd arg -1: just use the one proposed by the system
	 * @param depth
	 * @param allowFreeVars true is I like rules which bind head args but also have free variables
	 * @throws Exception
	 */
	public void learnOnCrossValidation(String[] relationsToBeLearned, int[] inputArg,int depth, boolean allowFreeVars, boolean tryConstants, boolean learnOnlyConstants) throws Exception
	{
		int gainMeasure=0;
		int beamWidth=0;
		int numOfPartitions=sampler.getNumOfPartitions();
		long time;
		
		// create the head predicates
		createHeadPredicates(relationsToBeLearned,inputArg, depth);		
		
		// initialize the learner
		learner=new RuleLearner(queryHandler,tChecker,info,allowFreeVars,gainMeasure,beamWidth,tryConstants);
		
		
		// learn for each training and testing partition
		for (int i=1;i<=numOfPartitions;i++)
		{	
			time= System.currentTimeMillis();
			goodRulesForPartition=new ArrayList<Rule>();
			learnForPartition(i,depth);	
			time = System.currentTimeMillis() - time;
			System.out.println("Time elapsed for the whole partition: "+time);
			
			//runUrdfExp(i);		
			break;
			
		}		
	}	
	
 	public void learnForPartition(int partitionNumber,int depth) throws Exception
	{
 		long time;
 		int count=0;
 		ArrayList<Rule> usefullRules;
 		rules=new HashMap<String,ArrayList<Rule>>();
 		ruleNodes=new HashMap<String, RuleTreeNode>();
 		
 		
 		
 		//assign the correct database tables (w/wo sampling)
		//String trainTbl="train"+inputArg+partitionNumber;
		//String trainTbl="facts"+(noise==0?"":"_"+noise);
		//String baseTbl="facts"+(noise==0?"":"_"+noise);
		
  		FileWriter fstream, fstreamForURDF;
  		BufferedWriter out,outForURDF;
				
		for (int i=0,len=headPredicates.size();i<len;i++)
		{	
			System.out.println("RELATION TO BE LEARNED: "+ headPredicates.get(i).getHeadRelation().getName());
			time= System.currentTimeMillis();
			
			// comment this
			//trainTbl="facts";
			learner.learnRule(headPredicates.get(i), depth);
			time = System.currentTimeMillis() - time; // time for one full round of training and testing
			
			//learner.printRules();
			//System.out.println("Time elapsed: "+time);
						
			rules.put(headPredicates.get(i).getHeadRelation().getName(), learner.getLearnedRules());
			ruleNodes.put(headPredicates.get(i).getHeadRelation().getName(), learner.getRuleNode());
			
			usefullRules=learner.getLearnedRules();
			
			for (int j=0;j<usefullRules.size();j++)
			{
				goodRulesForPartition.add(usefullRules.get(j));
			}
			
			fstream = new FileWriter(headPredicates.get(i).getHeadRelation().getName()+expDesc+".txt");	
			fstreamForURDF= new FileWriter(headPredicates.get(i).getHeadRelation().getName()+expDesc+"ForURDF.txt");	
			outForURDF= new BufferedWriter(fstreamForURDF);
			out = new BufferedWriter(fstream);
			
			out.write("TARGET PREDICATE: "+headPredicates.get(i).getHeadRelation().getName()+"\n \n");
			//out.write("Training Set: "+partitionNumber+1+"\n");
			
			
			// prints both in console and in file
			for (int j=0,len2=usefullRules.size();j<len2;j++)
			{
				//out.write("S"+count+":"+usefullRules.get(j).printForParser()+"\n");		
				out.write(usefullRules.get(j).printRule(false)+"\n");
				outForURDF.write(usefullRules.get(j).printForParser()+"\n");
				count++;
			}
			out.write("time elapsed for whole partition: "+time);
			out.close();
			outForURDF.close();
		}		
	}
	

	private void runUrdfExp(int partition) throws Exception
	{
		String s="";
		String path="conf050";
  		//FileWriter fstream=new FileWriter(path+"/"+"Rules_"+partition+".out");
  		FileWriter fstream=new FileWriter("Rules_"+partition+".out");
  		BufferedWriter out= new BufferedWriter(fstream);
  		
  		for (int i=0,len=goodRulesForPartition.size();i<len;i++)
  		{
  			s=goodRulesForPartition.get(i).printRule(false);
  			out.write(s+"\n");
  		}
  		
  		out.close();
  		
		//Urdf_exp exp=new Urdf_exp(goodRulesForPartition, iniFile, partition,info,inputArg,path);
	}
	
	
}
