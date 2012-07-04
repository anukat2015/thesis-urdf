package urdf.ilp;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 	@author Christina Teflioudi
 * 
 *	The class ThresholdChecker checks for all thresholds. It checks if the rule:
 *		passes the support threshold (supportThreshold)
 *		passes the confidence threshold (confidenceThreshold)
 *		is POSSIBLE to cover more examples than possibleExamplesThreshold (related to support)
 *		cover more than exactExamplesThreshold (nonDistinct evaluation) (related to confidence)
 *	Moreover, the class contains methods that check if a relation is:
 *		symmetric (according to the symmetryThreshold)
 *		function (according to the functionThreshold)
 */
public class ThresholdChecker 
{
	private static Logger logger = Logger.getLogger(LearningManager.tcheckerLoggerName);
	
	QueryHandler queryHandler;
	
	
	float supportThreshold;
	float confidenceThreshold;
	float specialityRatioThreshold;
	float stoppingThreshold; // confidence with which I am satisfied 
	float functionThreshold; // between 1 and 0. If 1, I don't allow errors
	float symmetryThreshold; // about 0.4 is enough 
	int possiblePosToBeCoveredThreshold;
	int positivesCoveredThreshold;
	int smoothingMethod=0; // 0: no smothing, 1: laplace smoothing
	float upperOverlapThreshold=(float)0.80;
	int partitionNumber;
	HashMap<String,Integer> dangerousRelations=new HashMap<String,Integer>(); // name of relation->number of facts
	
	public ThresholdChecker(QueryHandler queryHandler, float supportThreshold, float confidenceThreshold,float specialityRatioThreshold, int possiblePosToBeCoveredThreshold,
			int positivesCoveredThreshold, float functionThreshold, float symmetryThreshold, int smoothingMethod, float stoppingThreshold, int partitionNumber) {
		
		PropertyConfigurator.configure(LearningManager.log4jConfig);
	
		this.queryHandler=queryHandler;
		this.confidenceThreshold=confidenceThreshold;
		this.supportThreshold=supportThreshold;
		this.specialityRatioThreshold=specialityRatioThreshold;
		this.positivesCoveredThreshold=positivesCoveredThreshold;
		this.possiblePosToBeCoveredThreshold=possiblePosToBeCoveredThreshold;
		this.functionThreshold=functionThreshold;
		this.symmetryThreshold=symmetryThreshold;
		this.smoothingMethod=smoothingMethod;
		this.stoppingThreshold=stoppingThreshold;
		this.partitionNumber=partitionNumber;
		//fillInDangerous();
	}
	
	public void setDangerousRelations(HashMap<String,Integer> dangerousRelations) {
		this.dangerousRelations=dangerousRelations;
	}
	
	public boolean checkSupportThreshold(Rule rule, int FactsForHead,int inputArg) throws Exception  {
		int examplesForSupport = (int) queryHandler.calcRuleProperties(rule, 6, inputArg);
		
		rule.setPossiblePosToBeCovered(examplesForSupport);
		
		int examplesCovered=(int)queryHandler.calcRuleProperties(rule, 1, inputArg);
		rule.setExamplesCovered(examplesCovered);
		
		int minExamples=(examplesForSupport<examplesCovered?examplesForSupport:examplesCovered);
		
		rule.setSupport((float)minExamples/(float)FactsForHead);
		
		if ((rule.getSupport()>this.supportThreshold && minExamples>=possiblePosToBeCoveredThreshold) && minExamples>=positivesCoveredThreshold) {
			rule.setPossiblePosToBeCovered(examplesForSupport);
			return true;
		}
		return false;
	}
	
	public boolean checkPositivesThreshold(Rule rule,int inputArg) throws Exception {
		
		if (rule.getExamplesForSupport()<positivesCoveredThreshold) 
			return false;// do not store the rule and don't extend it
		
		int positivesCovered=(int)queryHandler.calcRuleProperties(rule, 0, inputArg);
		rule.setPositivesCovered(positivesCovered);	
	
		if (positivesCovered < positivesCoveredThreshold) {
			logger.log(Level.DEBUG, "Rule doesn't reach positivesCovered Threshold: "+positivesCovered+"<"+positivesCoveredThreshold );
			return false;// do not store the rule and don't extend it
		}
		
		// the rule will be stored, but check the confidence now
		logger.log(Level.DEBUG, "Rule reaches positivesCovered Threshold: "+positivesCovered+">="+positivesCoveredThreshold );
		
		//use conf or accuracy
		calculateConfidence(rule, inputArg,positivesCovered);
		//calculateAccuracy(rule, inputArg, positivesCovered);
		
		return true;
	}

	public boolean checkComplementaryRule(Rule rule, Rule rule1,Rule rule2, int factsForHead,int inputArg) throws Exception
	{
		float conf;
		int examplesCovered=rule1.getExamplesCovered()-rule2.getExamplesCovered();
		int examplesForSupport=rule1.getExamplesForSupport()-rule2.getExamplesForSupport();
		int positivesCovered=rule1.getPositivesCovered()-rule2.getPositivesCovered();
		
		rule.setExamplesCovered(examplesCovered);
		rule.setSupport((float)examplesForSupport/(float)factsForHead);
		if (rule.getSupport()<this.supportThreshold || examplesCovered>=possiblePosToBeCoveredThreshold)
			return false;// do not store the rule and don't extend it
				
		rule.setPositivesCovered(positivesCovered);
		if (positivesCovered<positivesCoveredThreshold)
			return false;// do not store the rule and don't extend it
		
		// the rule will be stored, but check the confidence now
		
		switch(smoothingMethod) {
			case 0:
				// no smoothing
				conf=(float)(positivesCovered)/(float)rule.getExamplesCovered();				
				rule.setConfidence(conf,partitionNumber);
				break;
			default:  //1
				// laplace smoothing? Lidstone?
				rule.setConfidence(((float)positivesCovered+1)/((float)examplesCovered+2),partitionNumber);				
		}	
		
		if (rule.getConfidence()>this.confidenceThreshold) {// check if rule is above confidence threshold without pruning	
			calculateSpecialityRatio(rule, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
				rule.setIsGood(true);
			else
				rule.setIsGood(false);
		}
		else
			rule.setIsGood(false);
		
		return true;
	}
	
	private void calculateSpecialityRatio(Rule rule, int inputArg) throws Exception
	{
		logger.log(Level.INFO,"Calculating Speciality Ratio for " + rule.getRuleString());	
		
		int bodySize=rule.getBodySize();
		if (bodySize < 0) // BodySize from rule still not calculated
			bodySize = getBodySize(rule,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		boolean comesFromGeneral = rule.isTooGeneral();
		
		
		
		rule.setSpecialityRatio(((float)rule.getExamplesCovered())/(float)bodySize); 
		//rule.setGeneralityRatio(((float)rule.getExamplesCovered()*partitionNumber)/(float)bodySize);

		logger.log(Level.DEBUG,"SpecialityRatio=examplesCovered/BodySize=" + rule.getSpecialityRatio());
		
		if (rule.getSpecialityRatio() < specialityRatioThreshold) {
			if (rule.getBodySize() > rule.getHead().getRelation().getSize() && rule.getSupport() >= 0.50) {
				rule.setIsTooGeneral(false);
			}
			else {
				rule.setIsTooGeneral(true);
			}
		}
		else {
			rule.setIsTooGeneral(false);
			int minExamples=(rule.getExamplesForSupport() > rule.getExamplesCovered()?rule.getExamplesCovered():rule.getExamplesForSupport());
			if (comesFromGeneral && minExamples < possiblePosToBeCoveredThreshold)
				rule.setIsTooGeneral(true);
		}
		logger.log(Level.DEBUG,"Rule isTooGeneral=" + rule.isTooGeneral());

	}
	public void calculateSpecialityRatio2(Rule rule, String[] clauses, int inputArg) throws Exception
	{
		//int bodySize=rule.getBodySize();
		int bodySize=getBodySize(rule,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		boolean comesFromGeneral=rule.isTooGeneral();
		
		rule.setSpecialityRatio(((float)rule.getExamplesCovered())/(float)bodySize); 
		//rule.setGeneralityRatio(((float)rule.getExamplesCovered()*partitionNumber)/(float)bodySize); 
		
		if (rule.getSpecialityRatio()<specialityRatioThreshold)	{

			//check multiplicity distributions
			float headAvgMult = rule.getHead().getRelation().getMult(inputArg);
			float headVarMult = rule.getHead().getRelation().getVar(inputArg);
			
			float bodyAvgMultVarMult[] = queryHandler.getBodyAvgMultAndVar(rule, inputArg);
			float bodyAvgMult = bodyAvgMultVarMult[0];
			float bodyVarMult = (float)Math.sqrt((double)bodyAvgMultVarMult[1]);
			
			logger.log(Level.INFO,"headMult="+headAvgMult+" bodyMult="+bodyAvgMult);
			logger.log(Level.INFO,"headVar="+headVarMult+" bodyVar="+bodyVarMult);
			
			if(headAvgMult+headVarMult>bodyAvgMult || headAvgMult-headVarMult<bodyAvgMult || bodyAvgMult+bodyVarMult>headAvgMult || bodyAvgMult-bodyVarMult<headAvgMult) {
				logger.log(Level.DEBUG, "Rule is NOT too general, i.e. body Multiplicity is out of head Multiplicity bounds (avg +/- var)");
				rule.setIsTooGeneral(false);
			}
			else {
				logger.log(Level.DEBUG, "Rule is too general, i.e. body Multiplicity is in head Multiplicity bounds (avg +/- var)");
				rule.setIsTooGeneral(true);
			}
			
		}
		else {
			logger.log(Level.DEBUG, "Rule is NOT too general, i.e. GenralityRatio > SpecialityRatioThreshold");
			rule.setIsTooGeneral(false);
			//int minExamples=(rule.getExamplesForSupport()>rule.getExamplesCovered()?rule.getExamplesCovered():rule.getExamplesForSupport());
			//if (comesFromGeneral && minExamples<possiblePosToBeCoveredThreshold)
			//{
			//	rule.setIsTooGeneral(true);
			//}
		}

	}
	

	
	// Calculate Confidence with the Improved formula
	private void calculateConfidence(Rule rule, int inputArg, int positivesCovered) throws Exception
	{
		logger.log(Level.INFO,"Calculating confidence for arg"+inputArg+" in rule "+rule.getRuleString());
		
		float multHead1,multHead2,multHeadIdeal1,multHeadIdeal2,missingPairsFromHead, missingPairsFromHead1=0,missingPairsFromHead2=0, missingPairsFromBody=0, nom, denom, ratio1=0,ratio2=0,ratio, conf, idealExamplesCovered;
		
		float idealExamplesCovered1=0,idealExamplesCovered2=0,multBody1,multBody2,missingPairsFromBody1=0,missingPairsFromBody2=0,multBodyIdeal1,multBodyIdeal2;
		int n1,n2;
		int examplesCovered=rule.getExamplesCovered();		
		
		int bodySize = rule.getBodySize();
		if (bodySize < 0) // If rule body size was still not calculated
			bodySize = getBodySize(rule,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		
		//TODO Why this method it OrigConfig is used for nothing and not even have a GETTER method
		rule.setOrigConf(((float)positivesCovered)/(float)examplesCovered);
		
		logger.log(Level.DEBUG,"SpecialityRatio=examplesCovered/BodySize=" + rule.getSpecialityRatio());
		
		// case inputArg=1
		if (inputArg!=2) {
			if (!isDangerous(rule)) {
				
				float bodyAvgMultVarMult[] = queryHandler.getBodyAvgMultAndVar(rule, inputArg);
				multBody1 = bodyAvgMultVarMult[0];
				if (multBody1>1)
					multBodyIdeal1 = multBody1+(float)Math.sqrt((double)bodyAvgMultVarMult[1]);
				else
					multBodyIdeal1 = multBody1;
		
				missingPairsFromBody1=(multBodyIdeal1-multBody1)*examplesCovered/multBody1;
				
				rule.multBody=multBody1;
				rule.multBodyIdeal=multBodyIdeal1;
				rule.missingBodyFacts=missingPairsFromBody1;
			}
			
			idealExamplesCovered1=examplesCovered+missingPairsFromBody1;
	
			multHead1=rule.getHead().getRelation().getMult1();
			multHeadIdeal1=rule.getHead().getRelation().getIdealMult(1);
			
			n1=rule.getHead().getRelation().getDistinctEntities(1); // distinct entities in the whole head
			n1=n1*rule.getExamplesForSupport()/rule.getHead().getRelation().getSize(); // distinct entities in the overlap of head body
			missingPairsFromHead1=n1*(multHeadIdeal1-multHead1);
			
			if (multHead1==1)
				ratio1 = 0;
			else
				ratio1 = ((float)positivesCovered)/(float)rule.getExamplesForSupport();
		}
		if (inputArg!=1) {
			if (!isDangerous(rule)) {
				float bodyAvgMultVarMult[] = queryHandler.getBodyAvgMultAndVar(rule, inputArg);
				multBody2 = bodyAvgMultVarMult[0];
				if (multBody2>1)
					multBodyIdeal2=multBody2+(float)Math.sqrt((double)bodyAvgMultVarMult[1]);
				else
					multBodyIdeal2=multBody2;
				
				missingPairsFromBody2=(multBodyIdeal2-multBody2)*examplesCovered/multBody2;
				
				rule.multBody=multBody2;
				rule.multBodyIdeal=multBodyIdeal2;
				rule.missingBodyFacts=missingPairsFromBody2;
			}

			idealExamplesCovered2=examplesCovered+missingPairsFromBody2;			
			
			multHead2 = rule.getHead().getRelation().getMult2();
			multHeadIdeal2 = rule.getHead().getRelation().getIdealMult(2);
			
			n2 = rule.getHead().getRelation().getDistinctEntities(2); // distinct entities in the whole head
			n2 = n2*rule.getExamplesForSupport()/rule.getHead().getRelation().getSize(); // distinct entities in the overlap of head body
			missingPairsFromHead2 = n2*(multHeadIdeal2-multHead2);
			
			if (multHead2==1)
				ratio2=0;
			else
				ratio2=((float)positivesCovered)/(float)rule.getExamplesForSupport();
			
		}
		
		switch(inputArg) {
			case 1:
				ratio=ratio1;
				missingPairsFromHead=missingPairsFromHead1;
				idealExamplesCovered=idealExamplesCovered1;
				break;
			case 2:
				ratio=ratio2;
				missingPairsFromHead=missingPairsFromHead2;
				idealExamplesCovered=idealExamplesCovered2;
				break;
			default:
				if (ratio1>ratio2) {
					ratio=ratio1;
					missingPairsFromHead=missingPairsFromHead1;
					idealExamplesCovered=idealExamplesCovered1;
				}
				else {
					ratio=ratio2;
					missingPairsFromHead=missingPairsFromHead2;
					idealExamplesCovered=idealExamplesCovered2;
				}
				
		}
		
		rule.ratio=ratio;		
		
		//calculate for head only 
		missingPairsFromHead=(missingPairsFromHead<examplesCovered-positivesCovered?missingPairsFromHead:examplesCovered-positivesCovered);
		nom=(float)(positivesCovered +ratio*missingPairsFromHead);		
		rule.setHeadConf(nom/(float)examplesCovered);
		
		rule.missingHeadFactsOnlyHead=missingPairsFromHead;
		
		//calculate for head and body
		missingPairsFromHead=(missingPairsFromHead<idealExamplesCovered-positivesCovered?missingPairsFromHead:idealExamplesCovered-positivesCovered);
		nom=(float)(positivesCovered +ratio*missingPairsFromHead);
		denom=idealExamplesCovered;
		
		rule.missingHeadFactsHeadBody=missingPairsFromHead;
		
		
		conf=nom/denom;	
		rule.setConfidence(conf,partitionNumber);	
		
		logger.log(Level.DEBUG,"Confidence=" + rule.getConfidence() + " // Calculation in ThresholdChecker::CalculateConfidence(line=350)");
		
		//TODO Why -0.15??????????????????????????
		if (rule.getConfidence()>this.confidenceThreshold-0.15) {// check if rule is above confidence threshold without pruning
			calculateSpecialityRatio(rule, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold) {
				logger.log(Level.DEBUG, "Rule is good, i.e. RuleConfidence="+rule.getConfidence()+">"+confidenceThreshold);
				rule.setIsGood(true);
			}
			else {
				logger.log(Level.DEBUG, "Rule is NOT good, i.e. RuleConfidence="+rule.getConfidence()+"<"+confidenceThreshold);
				rule.setIsGood(false);
			}
		}
		else {
			logger.log(Level.DEBUG, "Rule is NOT good, i.e. RuleConfidence="+rule.getConfidence()+"<"+confidenceThreshold);
			rule.setIsGood(false);
		}
		
	}
	

	
	private void calculateAccuracy(Rule rule, int inputArg, int positivesCovered) throws Exception {
		
		logger.log(Level.INFO,"Calculating Accuracy for arg"+inputArg+" in rule "+rule.getRuleString());
		
		float  conf,nom,denom;
		int body=getBodySize(rule,inputArg);		
		
		//nom=(float)(positivesCovered +beta*(rule.getExamplesCovered()-positivesCovered));
		nom=(float)positivesCovered;
		denom=body;
		conf=nom/denom;				
		rule.setConfidence(conf,partitionNumber);	

		if (rule.getConfidence()>this.confidenceThreshold) {// check if rule is above confidence threshold without pruning
			calculateSpecialityRatio(rule, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
				rule.setIsGood(true);
			else
				rule.setIsGood(false);
		}
		else {
			rule.setIsGood(false);
		}
	}
	
	private  boolean isDangerous(Rule rule) {
		String rel;
		Set<String> set=dangerousRelations.keySet();
		Iterator<String> it;
		int counterOfDangerous=0;
		int counterOfRelations=0;
		int size=1;
		
		for (int i=0,len=rule.getBodyLiterals().size();i<len;i++) {
			if (!rule.getBodyLiterals().get(i).getRelation().isAuxiliary()) {
				counterOfRelations++;
				it=set.iterator();
				while(it.hasNext()) {
					rel=it.next();
					if (rel.equals(rule.getBodyLiterals().get(i).getRelationName())) {
						counterOfDangerous++;
						size*=dangerousRelations.get(rel);
						break;
					}
				}
			}
		}
		if (counterOfRelations==counterOfDangerous){
			logger.log(Level.DEBUG, "Rule is dangerous, i.e. all its body relations are dangerous");
			return true;
		}
		return false;
	}
	
	private int getBodySize(Rule rule, int inputArg) throws Exception {
		
		String rel;
		Set<String> set = dangerousRelations.keySet();
		Iterator<String> it;
		int counterOfDangerous = 0;
		int counterOfRelations = 0;
		int size=1;
		
		for (int i=0,len=rule.getBodyLiterals().size();i<len;i++) {
			if (!rule.getBodyLiterals().get(i).getRelation().isAuxiliary()) {
				counterOfRelations++;
				it=set.iterator();
				while(it.hasNext()) {
					rel = it.next();
					if (rel.equals(rule.getBodyLiterals().get(i).getRelationName())) {
						counterOfDangerous++;
						size *= dangerousRelations.get(rel);
						break;
					}
				}
			}
		}
		if (counterOfDangerous >= 2) {//(counterOfRelations==counterOfDangerous)
			//the body contained only dangerous relations so approximate the size
			logger.log(Level.DEBUG, "The body contained more than 1 dangerous relations, so approximate the size to " + size);
			return size;
		}
		else {
			size = (int) queryHandler.calcRuleProperties(rule, 2, inputArg);
			return size;
		}
		
	}
	
	// get Thresholds
	public int getPossiblePosToBeCoveredThreshold() {
		return this.possiblePosToBeCoveredThreshold;
	}
	
	public float getSupportThreshold() {
		return this.supportThreshold;
	}
	
	public float getStoppingThreshold() {
		return this.stoppingThreshold;
	}
	
	public float getUpperOverlapThreshold() {
		return this.upperOverlapThreshold;
	}
	
	public int getPositivesCoveredThreshold() {
		return this.positivesCoveredThreshold;
	}

	
	// Calculate Confidence with the Improved formula
	/*private void calculateConfidence(Rule rule, int inputArg, int positivesCovered) throws Exception {
		float multHead1,multHead2,multHeadIdeal1,multHeadIdeal2,multBody1=0,multBody2=0,multBodyIdeal1=0,multBodyIdeal2=0;
		float conf1=0,conf2=0,conf,idealExamplesCovered1,idealExamplesCovered2,idealPositivesCovered1,idealPositivesCovered2;
		
		int examplesCovered =rule.getExamplesCovered();		
		int bodySize = getBodySize(rule,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		rule.setOrigConf(((float)positivesCovered)/(float)examplesCovered);
		
		// case inputArg=1
		if (inputArg!=2){
			if (!isDangerous(rule)){
				multBody1=queryHandler.getBodyAvgMult(rule,inputArg);	
				if (multBody1>1)
					multBodyIdeal1=multBody1+(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, inputArg, multBody1));
				else
					multBodyIdeal1=multBody1;
				rule.multBody=multBody1;
				rule.multBodyIdeal=multBodyIdeal1;				
			}
			
			idealExamplesCovered1= (multBodyIdeal1/multBody1)*examplesCovered;			
			
			multHead1=rule.getHead().getRelation().getMult1();
			multHeadIdeal1=rule.getHead().getRelation().getIdealMult(1);
			
			idealPositivesCovered1=(multHeadIdeal1/multHead1)*positivesCovered;			
			conf1=idealPositivesCovered1/idealExamplesCovered1;
			
			if (conf1>1){
				idealPositivesCovered1=positivesCovered+((float)positivesCovered/(float)rule.getExamplesForSupport())*(idealExamplesCovered1-positivesCovered);	
				conf1=idealPositivesCovered1/idealExamplesCovered1;
			}
			rule.setHeadConf(((float)idealPositivesCovered1)/(float)examplesCovered);
			
		}
		if (inputArg!=1){
			if (!isDangerous(rule)){
				multBody2=queryHandler.getBodyAvgMult(rule,inputArg);
				if (multBody2>1)
					multBodyIdeal2=multBody2+(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, inputArg, multBody2))  ;	
				else
					multBodyIdeal2=multBody2;
							
			}

			idealExamplesCovered2=(multBodyIdeal2/multBody2)*examplesCovered;			
			
			multHead2=rule.getHead().getRelation().getMult2();
			multHeadIdeal2=rule.getHead().getRelation().getIdealMult(2);
			
			idealPositivesCovered2=(multHeadIdeal2/multHead2)*positivesCovered;
			conf2=idealPositivesCovered2/idealExamplesCovered2;
			
			if (conf2>1) {
				idealPositivesCovered2=positivesCovered+((float)positivesCovered/(float)rule.getExamplesForSupport())*(idealExamplesCovered2-positivesCovered);	
				conf2=idealPositivesCovered2/idealExamplesCovered2;
			}
			rule.setHeadConf(((float)idealPositivesCovered2)/(float)examplesCovered);
			
		}
		
		switch(inputArg) {
			case 1:
				conf=conf1;
				rule.setConfidence(conf,partitionNumber);
				break;
			case 2:
				conf=conf2;
				rule.setConfidence(conf,partitionNumber);
				break;
			default:
				if (conf1>conf2)
					rule.setConfidence(conf1,partitionNumber);
				else
					rule.setConfidence(conf2,partitionNumber);			
		}
		
		if (rule.getConfidence()>this.confidenceThreshold-0.15)// check if rule is above confidence threshold without pruning
		{
			calculateSpecialityRatio(rule, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
				rule.setIsGood(true);
			else
				rule.setIsGood(false);
		}
		else
			rule.setIsGood(false);
	}

	private void calculateConfidence(Rule rule, int inputArg, int positivesCovered) throws Exception {
	float  conf,nom,denom;
	int examplesCovered=rule.getExamplesCovered();		
	
	//nom=(float)(positivesCovered +beta*(rule.getExamplesCovered()-positivesCovered));
	nom=(float)positivesCovered;
	denom=examplesCovered;
	conf=nom/denom;				
	rule.setConfidence(conf,partitionNumber);	

	if (rule.getConfidence()>this.confidenceThreshold) {  // check if rule is above confidence threshold without pruning
		calculateSpecialityRatio(rule, inputArg);
		if (rule.getConfidence()>this.confidenceThreshold)
			rule.setIsGood(true);
		else
			rule.setIsGood(false);
	}
	else {
		calculateSpecialityRatio(rule, inputArg);
		rule.setIsGood(false);
	}
}*/
	
}
