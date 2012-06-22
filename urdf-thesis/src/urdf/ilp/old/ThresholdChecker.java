package urdf.ilp.old;



import java.sql.SQLException;
import java.util.ArrayList;
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
			int positivesCoveredThreshold, float functionThreshold, float symmetryThreshold, int smoothingMethod, float stoppingThreshold, int partitionNumber)
	{
	
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
	public void setDangerousRelations(HashMap<String,Integer> dangerousRelations)
	{
		this.dangerousRelations=dangerousRelations;
	}
	public boolean checkSupportThreshold(Rule rule, int FactsForHead, String[] clauses,int inputArg) throws Exception
	{
		int examplesForSupport=(int)queryHandler.calcRuleProperties(rule, clauses, 6, inputArg);
		rule.setPossiblePosToBeCovered(examplesForSupport);
		
		int examplesCovered=(int)queryHandler.calcRuleProperties(rule, clauses, 1, inputArg);
		rule.setExamplesCovered(examplesCovered);
		
		int minExamples=(examplesForSupport<examplesCovered?examplesForSupport:examplesCovered);
		
		rule.setSupport((float)minExamples/(float)FactsForHead);
		
		if ((rule.getSupport()>this.supportThreshold && minExamples>=possiblePosToBeCoveredThreshold) && minExamples>=positivesCoveredThreshold) // made it &&
		{
			rule.setPossiblePosToBeCovered(examplesForSupport);
			return true;
		}
		return false;
	}
	public boolean checkPositivesThreshold(Rule rule,String[] clauses,int inputArg) throws Exception
	{	
		if (rule.getExamplesForSupport()<positivesCoveredThreshold)
		{
			return false;// do not store the rule and don't extend it
		}
		int positivesCovered=(int)queryHandler.calcRuleProperties(rule, clauses, 0, inputArg);
		rule.setPositivesCovered(positivesCovered);	
		
		if (positivesCovered<positivesCoveredThreshold)
		{
			return false;// do not store the rule and don't extend it
		}
		
		// the rule will be stored, but check the confidence now	
		
		//use conf or accuracy
		calculateConfidence(rule,clauses, inputArg,positivesCovered);
		//calculateAccuracy(rule,clauses,inputArg,positivesCovered);
		
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
		{
			return false;// do not store the rule and don't extend it
		}			
		rule.setPositivesCovered(positivesCovered);
		if (positivesCovered<positivesCoveredThreshold)
		{
			return false;// do not store the rule and don't extend it
		}
		// the rule will be stored, but check the confidence now

		String[] clauses=queryHandler.parseRule(rule, inputArg);
		
		switch(smoothingMethod)
		{
			case 0:
				// no smoothing
				conf=(float)(positivesCovered)/(float)rule.getExamplesCovered();				
				rule.setConfidence(conf,partitionNumber);
				break;
			default:  //1
				// laplace smoothing? Lidstone?
				rule.setConfidence(((float)positivesCovered+1)/((float)examplesCovered+2),partitionNumber);				
		}	
		
		if (rule.getConfidence()>this.confidenceThreshold)// check if rule is above confidence threshold without pruning
		{			
			calculateSpecialityRatio(rule, clauses, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
			{
				rule.setIsGood(true);
			}
			else
			{
				rule.setIsGood(false);
			}
		}
		else
		{
			rule.setIsGood(false);
		}
		return true;
	}
	
	private void calculateSpecialityRatio(Rule rule, String[] clauses, int inputArg) throws Exception
	{
		//int bodySize=rule.getBodySize();
		int bodySize=getBodySize(rule,clauses,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		boolean comesFromGeneral=rule.isTooGeneral();
		
		rule.setSpecialityRatio(((float)rule.getExamplesCovered())/(float)bodySize); 
		//rule.setGeneralityRatio(((float)rule.getExamplesCovered()*partitionNumber)/(float)bodySize); 
		
		if (rule.getGeneralityRatio()<specialityRatioThreshold)
		{
			if (rule.getBodySize()>rule.getHead().getRelation().getSize() && rule.getSupport()>=0.50)
			{
				rule.setIsTooGeneral(false);
			}
			else
			{
				rule.setIsTooGeneral(true);
			}			
		}
		else
		{
			rule.setIsTooGeneral(false);
			int minExamples=(rule.getExamplesForSupport()>rule.getExamplesCovered()?rule.getExamplesCovered():rule.getExamplesForSupport());
			if (comesFromGeneral && minExamples<possiblePosToBeCoveredThreshold)
			{
				rule.setIsTooGeneral(true);
			}
		}

	}
	private void calculateSpecialityRatio2(Rule rule, String[] clauses, int inputArg) throws Exception
	{
		//int bodySize=rule.getBodySize();
		int bodySize=getBodySize(rule,clauses,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		boolean comesFromGeneral=rule.isTooGeneral();
		
		rule.setSpecialityRatio(((float)rule.getExamplesCovered())/(float)bodySize); 
		//rule.setGeneralityRatio(((float)rule.getExamplesCovered()*partitionNumber)/(float)bodySize); 
		
		if (rule.getGeneralityRatio()<specialityRatioThreshold)
		{
			
			//check multiplicity distributions
			float headAvgMult=rule.getHead().getRelation().getMult(inputArg);
			float bodyAvgMult=queryHandler.getBodyAvgMult(rule, clauses,inputArg);
			System.out.println("headMult="+headAvgMult+" bodyMult="+bodyAvgMult);
			float headVarMult=rule.getHead().getRelation().getVar(inputArg);			
			float bodyVarMult=(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, clauses,inputArg, bodyAvgMult));
			System.out.println("headVar="+headVarMult+" bodyVar="+bodyVarMult);
			
			if(headAvgMult+headVarMult>bodyAvgMult || headAvgMult-headVarMult<bodyAvgMult || bodyAvgMult+bodyVarMult>headAvgMult || bodyAvgMult-bodyVarMult<headAvgMult)
			{
				rule.setIsTooGeneral(false);
			}
			else
			{
				rule.setIsTooGeneral(true);
			}		
			
		}
		else
		{
			rule.setIsTooGeneral(false);
			//int minExamples=(rule.getExamplesForSupport()>rule.getExamplesCovered()?rule.getExamplesCovered():rule.getExamplesForSupport());
			//if (comesFromGeneral && minExamples<possiblePosToBeCoveredThreshold)
			//{
			//	rule.setIsTooGeneral(true);
			//}
		}

	}
	
	// Calculate Confidence with the Improved formula
/*	private void calculateConfidence(Rule rule,String[] clauses, int inputArg, int positivesCovered,String baseTbl) throws Exception
	{
		float multHead1,multHead2,multHeadIdeal1,multHeadIdeal2,multBody1=0,multBody2=0,multBodyIdeal1=0,multBodyIdeal2=0;
		float conf1=0,conf2=0,conf,idealExamplesCovered1,idealExamplesCovered2,idealPositivesCovered1,idealPositivesCovered2;
		
		int examplesCovered=rule.getExamplesCovered();		
		int bodySize=getBodySize(rule,clauses,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		rule.setOrigConf(((float)positivesCovered)/(float)examplesCovered);
		
		// case inputArg=1
		if (inputArg!=2)
		{
			if (!isDangerous(rule))
			{
				multBody1=queryHandler.getBodyAvgMult(rule, clauses,inputArg);	
				if (multBody1>1)
				{
					multBodyIdeal1=multBody1+(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, clauses, inputArg, multBody1));
				}
				else
				{
					multBodyIdeal1=multBody1;
				}
				rule.multBody=multBody1;
				rule.multBodyIdeal=multBodyIdeal1;				
			}
			
			idealExamplesCovered1= (multBodyIdeal1/multBody1)*examplesCovered;			
			
			multHead1=rule.getHead().getRelation().getMult1();
			multHeadIdeal1=rule.getHead().getRelation().getIdealMult(1);
			
			idealPositivesCovered1=(multHeadIdeal1/multHead1)*positivesCovered;			
			conf1=idealPositivesCovered1/idealExamplesCovered1;
			
			if (conf1>1)
			{
				idealPositivesCovered1=positivesCovered+((float)positivesCovered/(float)rule.getExamplesForSupport())*(idealExamplesCovered1-positivesCovered);	
				conf1=idealPositivesCovered1/idealExamplesCovered1;
			}
			rule.setHeadConf(((float)idealPositivesCovered1)/(float)examplesCovered);
			
		}
		if (inputArg!=1)
		{
			if (!isDangerous(rule))
			{
				multBody2=queryHandler.getBodyAvgMult(rule, clauses,inputArg);
				if (multBody2>1)
				{
					multBodyIdeal2=multBody2+(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, clauses, inputArg, multBody2))  ;	
				}
				else
				{
					multBodyIdeal2=multBody2;
				}
							
			}

			idealExamplesCovered2=(multBodyIdeal2/multBody2)*examplesCovered;			
			
			multHead2=rule.getHead().getRelation().getMult2();
			multHeadIdeal2=rule.getHead().getRelation().getIdealMult(2);
			
			idealPositivesCovered2=(multHeadIdeal2/multHead2)*positivesCovered;
			conf2=idealPositivesCovered2/idealExamplesCovered2;
			
			if (conf2>1)
			{
				idealPositivesCovered2=positivesCovered+((float)positivesCovered/(float)rule.getExamplesForSupport())*(idealExamplesCovered2-positivesCovered);	
				conf2=idealPositivesCovered2/idealExamplesCovered2;
			}
			rule.setHeadConf(((float)idealPositivesCovered2)/(float)examplesCovered);
			
		}
		
		switch(inputArg)
		{
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
				{
					rule.setConfidence(conf1,partitionNumber);
				}
				else
				{
					rule.setConfidence(conf2,partitionNumber);
				}				
		}
		
		if (rule.getConfidence()>this.confidenceThreshold-0.15)// check if rule is above confidence threshold without pruning
		{
			calculateSpecialityRatio(rule, clauses, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
			{
				rule.setIsGood(true);
			}
			else
			{
				rule.setIsGood(false);
			}
		}
		else
		{
			rule.setIsGood(false);
		}
	}*/
	
	// Calculate Confidence with the Improved formula
/*	private void calculateConfidence(Rule rule,String[] clauses, int inputArg, int positivesCovered,String baseTbl) throws Exception
	{
		float multHead1,multHead2,multHeadIdeal1,multHeadIdeal2,missingPairsFromHead, missingPairsFromHead1=0,missingPairsFromHead2=0, missingPairsFromBody=0, nom, denom, ratio1=0,ratio2=0,ratio, conf, idealExamplesCovered;
		
		float idealExamplesCovered1=0,idealExamplesCovered2=0,multBody1,multBody2,missingPairsFromBody1=0,missingPairsFromBody2=0,multBodyIdeal1,multBodyIdeal2;
		int n1,n2;
		int examplesCovered=rule.getExamplesCovered();		
		int bodySize=getBodySize(rule,clauses,inputArg);// be careful with the body size: I might have dangerous relations
		rule.setBodySize(bodySize);
		
		rule.setOrigConf(((float)positivesCovered)/(float)examplesCovered);
		
		// case inputArg=1
		if (inputArg!=2)
		{
			if (!isDangerous(rule))
			{
				multBody1=queryHandler.getBodyAvgMult(rule, clauses,inputArg);				
				if (multBody1>1)
				{
					multBodyIdeal1=multBody1+(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, clauses, inputArg, multBody1));
				}
				else
				{
					multBodyIdeal1=multBody1;
				}			
				missingPairsFromBody1=(multBodyIdeal1-multBody1)*examplesCovered/multBody1;
				
				rule.multBody=multBody1;
				rule.multBodyIdeal=multBodyIdeal1;
				rule.missingBodyFacts=missingPairsFromBody1;
			}
			
			idealExamplesCovered1=examplesCovered+missingPairsFromBody1;
			//System.out.println("idealExamplesCovered: "+idealExamplesCovered);
			
			
			multHead1=rule.getHead().getRelation().getMult1();
			multHeadIdeal1=rule.getHead().getRelation().getIdealMult(1);
			
			n1=rule.getHead().getRelation().getDistinctEntities(1); // distinct entities in the whole head
			n1=n1*rule.getExamplesForSupport()/rule.getHead().getRelation().getSize(); // distinct entities in the overlap of head body
			missingPairsFromHead1=n1*(multHeadIdeal1-multHead1);
			
			if (multHead1==1)
			{
				ratio1=0;
			}
			else
			{
				ratio1=((float)positivesCovered)/(float)rule.getExamplesForSupport();
			}			
		}
		if (inputArg!=1)
		{
			if (!isDangerous(rule))
			{
				multBody2=queryHandler.getBodyAvgMult(rule, clauses,inputArg);
				if (multBody2>1)
				{
					multBodyIdeal2=multBody2+(float)Math.sqrt((double)queryHandler.getBodyMultVar(rule, clauses, inputArg, multBody2))  ;
				}
				else
				{
					multBodyIdeal2=multBody2;
				}
				
				missingPairsFromBody2=(multBodyIdeal2-multBody2)*examplesCovered/multBody2;
				
				rule.multBody=multBody2;
				rule.multBodyIdeal=multBodyIdeal2;
				rule.missingBodyFacts=missingPairsFromBody2;
			}

			idealExamplesCovered2=examplesCovered+missingPairsFromBody2;
			//System.out.println("idealExamplesCovered: "+idealExamplesCovered);
			
			
			multHead2=rule.getHead().getRelation().getMult2();
			multHeadIdeal2=rule.getHead().getRelation().getIdealMult(2);
			
			n2=rule.getHead().getRelation().getDistinctEntities(2); // distinct entities in the whole head
			n2=n2*rule.getExamplesForSupport()/rule.getHead().getRelation().getSize(); // distinct entities in the overlap of head body
			missingPairsFromHead2=n2*(multHeadIdeal2-multHead2);
			
			if (multHead2==1)
			{
				ratio2=0;
			}
			else
			{
				ratio2=((float)positivesCovered)/(float)rule.getExamplesForSupport();
			}
		}
		
		switch(inputArg)
		{
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
				if (ratio1>ratio2)
				{
					ratio=ratio1;
					missingPairsFromHead=missingPairsFromHead1;
					idealExamplesCovered=idealExamplesCovered1;
				}
				else
				{
					ratio=ratio2;
					missingPairsFromHead=missingPairsFromHead2;
					idealExamplesCovered=idealExamplesCovered2;
				}
				
		}
		
		rule.ratio=ratio;
		//System.out.println("ratio: "+ratio);
		
		
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
		
		if (rule.getConfidence()>this.confidenceThreshold-0.15)// check if rule is above confidence threshold without pruning
		{
			calculateSpecialityRatio(rule, clauses, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
			{
				rule.setIsGood(true);
			}
			else
			{
				rule.setIsGood(false);
			}
		}
		else
		{
			rule.setIsGood(false);
		}
	}*/
	
	private void calculateConfidence(Rule rule,String[] clauses, int inputArg, int positivesCovered) throws Exception
	{
		float  conf,nom,denom;
		int examplesCovered=rule.getExamplesCovered();		
		
		//nom=(float)(positivesCovered +beta*(rule.getExamplesCovered()-positivesCovered));
		nom=(float)positivesCovered;
		denom=examplesCovered;
		conf=nom/denom;				
		rule.setConfidence(conf,partitionNumber);	

		if (rule.getConfidence()>this.confidenceThreshold)// check if rule is above confidence threshold without pruning
		{
			calculateSpecialityRatio(rule, clauses, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
			{
				rule.setIsGood(true);
			}
			else
			{
				rule.setIsGood(false);
			}
		}
		else
		{
			calculateSpecialityRatio(rule, clauses, inputArg);
			rule.setIsGood(false);
		}
	}
	private void calculateAccuracy(Rule rule,String[] clauses, int inputArg, int positivesCovered) throws Exception
	{
		float  conf,nom,denom;
		int body=getBodySize(rule,clauses,inputArg);		
		
		//nom=(float)(positivesCovered +beta*(rule.getExamplesCovered()-positivesCovered));
		nom=(float)positivesCovered;
		denom=body;
		conf=nom/denom;				
		rule.setConfidence(conf,partitionNumber);	

		if (rule.getConfidence()>this.confidenceThreshold)// check if rule is above confidence threshold without pruning
		{
			calculateSpecialityRatio(rule, clauses, inputArg);
			if (rule.getConfidence()>this.confidenceThreshold)
			{
				rule.setIsGood(true);
			}
			else
			{
				rule.setIsGood(false);
			}
		}
		else
		{
			rule.setIsGood(false);
		}
	}
	private  boolean isDangerous(Rule rule)
	{
		String rel;
		Set<String> set=dangerousRelations.keySet();
		Iterator<String> it;
		int counterOfDangerous=0,counterOfRelations=0,size=1;
		
		for (int i=0,len=rule.getBodyLiterals().size();i<len;i++)
		{
			if (!rule.getBodyLiterals().get(i).getRelation().isAuxiliary())
			{
				counterOfRelations++;
				it=set.iterator();
				while(it.hasNext())
				{
					rel=it.next();
					if (rel.equals(rule.getBodyLiterals().get(i).getRelationName()))
					{
						counterOfDangerous++;
						size*=dangerousRelations.get(rel);
						break;
					}
				}
			}
		}
		if (counterOfRelations==counterOfDangerous)
		{
			//the body contained only dangerous relations so approximate the size
			return true;
		}
		return false;
	}
	
	private int getBodySize(Rule rule,String[] clauses,int inputArg) throws Exception
	{
		String rel;
		Set<String> set=dangerousRelations.keySet();
		Iterator<String> it;
		int counterOfDangerous=0,counterOfRelations=0,size=1;
		
		for (int i=0,len=rule.getBodyLiterals().size();i<len;i++)
		{
			if (!rule.getBodyLiterals().get(i).getRelation().isAuxiliary())
			{
				counterOfRelations++;
				it=set.iterator();
				while(it.hasNext())
				{
					rel=it.next();
					if (rel.equals(rule.getBodyLiterals().get(i).getRelationName()))
					{
						counterOfDangerous++;
						size*=dangerousRelations.get(rel);
						break;
					}
				}
			}
		}
		if (counterOfDangerous>=2)//(counterOfRelations==counterOfDangerous)
		{
			//the body contained only dangerous relations so approximate the size
			return size;
		}
		else
		{
			size=(int)queryHandler.calcRuleProperties(rule, clauses, 2, inputArg);
			return size;
		}
		
	}
	
	private void fillInDangerous()
	{
		
		// dangerous relations for yago
		dangerousRelations.put("bornOnDate", 441273);
		dangerousRelations.put("describes", 2122546);
		dangerousRelations.put("diedOnDate", 205469);
		dangerousRelations.put("during", 21287651);
		dangerousRelations.put("establishedOnDate", 110830);
		dangerousRelations.put("familyNameOf", 569410);
		dangerousRelations.put("foundIn", 21216121);
		dangerousRelations.put("givenNameOf", 568852);
		dangerousRelations.put("hasValue", 111961);
		dangerousRelations.put("hasWebsite", 130070);
		dangerousRelations.put("inLanguage", 3563111);
		dangerousRelations.put("inUnit", 111961);
		dangerousRelations.put("isCalled", 2185728);
		dangerousRelations.put("isOfGenre", 106797);
		dangerousRelations.put("means", 5346656);
		dangerousRelations.put("subClassOf", 249444);
		dangerousRelations.put("type", 4504775);
		dangerousRelations.put("using", 21224081);
		dangerousRelations.put("hasUTCOffset", 52212);
		
		// dangerous relations for yago2
//		dangerousRelations.put("isLocatedIn",212584 );
//		dangerousRelations.put("label",6790862 );
//		dangerousRelations.put("hasWikipediaUrl",2635452 );
//		dangerousRelations.put("hasFamilyName",747597 );
//		dangerousRelations.put("wasBornOnDate", 685132);
//		dangerousRelations.put("wasCreatedOnDate", 477101);
//		dangerousRelations.put("hasGender", 803685);
//		dangerousRelations.put("hasPreferredMeaning",2721839 );
//		dangerousRelations.put("hasPreferredName",2703588 );
//		dangerousRelations.put("hasGivenName", 745438);

	}
	
	// get Thresholds
	public int getPossiblePosToBeCoveredThreshold()
	{
		return this.possiblePosToBeCoveredThreshold;
	}
	public float getSupportThreshold()
	{
		return this.supportThreshold;
	}
	public float getStoppingThreshold()
	{
		return this.stoppingThreshold;
	}
	public float getUpperOverlapThreshold()
	{
		return this.upperOverlapThreshold;
	}
	public int getPositivesCoveredThreshold()
	{
		return this.positivesCoveredThreshold;
	}

	
}
