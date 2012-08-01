package urdf.ilp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.ilp.ThresholdChecker;
import urdf.ilp.RelationPreProcessor;

public class LearningManager 
{
	public static String loggerName = "logger1";
	public static String queriesLoggerName = "queries";
	public static String tcheckerLoggerName = "tchecker";
	
	public static String log4jConfig = "src/log4j.properties";
	
	private static int gainMeasure = 3;
	
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
	
	private static Logger logger = Logger.getLogger(loggerName);
	
	int partitionNumber;
	
	int inputArg;// for sampling
	String  expDesc;
	
	
	public LearningManager(String iniFile, int partitionNumber,float supportThreshold, float confidenceThreshold,float specialityRatioThreshold, int possiblePosToBeCoveredThreshold,
			int positivesCoveredThreshold, float functionThreshold, float symmetryThreshold, int smoothingMethod, float stoppingThreshold,
			ArrayList<Relation> relations,ArrayList<Type> types, HashMap<Integer,String>relationsForConstants, int noise,int inputArg,String baseTbl, String headTbl) throws Exception
	{
		PropertyConfigurator.configure(log4jConfig);
		
		logger.log(Level.INFO, iniFile);
		
		
		Connection conn = Driver.connect(iniFile);
		Connection connPartition = Driver.connect("src/rdf3x.properties");
		
		
		queryHandler = new QueryHandler(connPartition);
		
		this.iniFile=iniFile;
		
		tChecker=new ThresholdChecker(queryHandler, supportThreshold, confidenceThreshold, specialityRatioThreshold, possiblePosToBeCoveredThreshold,
				  positivesCoveredThreshold, functionThreshold, symmetryThreshold, smoothingMethod, stoppingThreshold, partitionNumber);

		
		
		//preprocessor=new RelationPreProcessor(conn,tChecker,relations,types,relationsForConstants);		
		//info = preprocessor.getRelationsInfo();
		
		info = RelationsInfo.readFromDisk();
		//info.calculateMinAndMaxLiterals(conn);
		//info.persist();

		RelationsInfo.printRelations(info);
				
		tChecker.setDangerousRelations(info.dangerousRelations);
		
		
		
		expDesc	= " supp"	+supportThreshold + 
				  " conf" + confidenceThreshold + 
				  " spec" + specialityRatioThreshold + 
				  " possPos" + possiblePosToBeCoveredThreshold;
		logger.log(Level.INFO, expDesc);
		
		this.inputArg = inputArg;
		this.partitionNumber = partitionNumber;
		
		//sampler = new HeadSampler(conn);
		//sample(partitionNumber,false, noise);
		
		// Rdf3x doesn't have RANGE and DOMAIN of TYPE relation, gotta add manually (not necessary now because )
		/*String typeName = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		Type typeRange = info.getTypeFromTypes("<http://yago-knowledge.org/resource/yagoClass>");
		Type typeDomain = info.getTypeFromTypes("<http://yago-knowledge.org/resource/wordnet_entity_100001740>");
		Relation typeRelation = new Relation(typeName, typeDomain, typeRange, 8419005, (float)3.1789155, (float)28.74744415283203, 0);
		info.getAllRelations().put(typeName, typeRelation);*/
		
		
		
		logger.log(Level.INFO, "Learning Manager Constructed Successfully");

	}
	
 	public void createHeadPredicates(String[] rel,int[] inputArg, int depth) {
 		headPredicates=new ArrayList<HeadPredicate>();

		for (int i=0; i<rel.length; i++){	
			Relation headRelation = info.getRelationFromRelations(rel[i]);
			HeadPredicate hp = new HeadPredicate(headRelation,depth, info, inputArg[i]);
			headPredicates.add(hp);				
		}
		logger.log(Level.INFO, "HeadPredicates created successfully");
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
	public void learnOnCrossValidation(String[] relationsToBeLearned, int[] inputArg,int depth, boolean allowFreeVars, boolean tryConstants, boolean learnOnlyConstants) throws Exception {
		
		int beamWidth=0;
		
		int numOfPartitions = partitionNumber;
		
		long time;
		
		logger.log(Level.INFO, "Creating Head Predicates");
		createHeadPredicates(relationsToBeLearned,inputArg, depth);		

		logger.log(Level.INFO, "Constructing Rule Learner");
		learner = new RuleLearner(queryHandler,tChecker,info,allowFreeVars,gainMeasure,beamWidth,tryConstants);
		
		logger.log(Level.INFO, "Learn for each training and testing partition");
		for (int i=1;i<=numOfPartitions;i++) {	
			time= System.currentTimeMillis();
			goodRulesForPartition=new ArrayList<Rule>();
			learnForPartition(i,depth);	
			time = System.currentTimeMillis() - time;
			logger.log(Level.INFO, i + ") Time elapsed for the whole partition: "+time);
			
			break;
			
		}		
	}	
	
 	public void learnForPartition(int partitionNumber,int depth) throws Exception
	{
 		long time;
 		int count = 0;
 		ArrayList<Rule> usefullRules;
 		rules = new HashMap<String,ArrayList<Rule>>();
 		ruleNodes = new HashMap<String, RuleTreeNode>();
 		
 		
 		
 		//assign the correct database tables (w/wo sampling)
		//String trainTbl="train"+inputArg+partitionNumber;
		//String trainTbl="facts"+(noise==0?"":"_"+noise);
		//String baseTbl="facts"+(noise==0?"":"_"+noise);
		
  		FileWriter fstream, fstreamForURDF;
  		BufferedWriter out,outForURDF;
				
		for (int i=0,len=headPredicates.size(); i<len; i++) {
			 
			logger.log(Level.INFO,"RELATION TO BE LEARNED: "+ headPredicates.get(i).getHeadRelation().getName() + " INPUT ARG:" + headPredicates.get(i).getInputArg());
			info.printJoinableRelations(headPredicates.get(i).getHeadRelation());
			
			time= System.currentTimeMillis();
			
			learner.learnRule(headPredicates.get(i), depth);
			time = System.currentTimeMillis() - time; // time for one full round of training and testing
		
						
			rules.put(headPredicates.get(i).getHeadRelation().getName(), learner.getLearnedRules());
			ruleNodes.put(headPredicates.get(i).getHeadRelation().getName(), learner.getRuleNode());
			
			usefullRules=learner.getLearnedRules();
			
			String toLog = "Useful Rules: ";
			for (int j=0;j<usefullRules.size();j++) {
				goodRulesForPartition.add(usefullRules.get(j));
				toLog += "\n" + usefullRules.get(j).printRule(false);
			}
			logger.log(Level.INFO, toLog);
			
			fstream = new FileWriter("rules/"+headPredicates.get(i).getHeadRelation().getSimpleName()+expDesc+"depth"+depth+".txt");	
			fstreamForURDF= new FileWriter("rules/"+headPredicates.get(i).getHeadRelation().getSimpleName()+expDesc+"depth"+depth+"ForURDF.txt");	
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
	
	
}
