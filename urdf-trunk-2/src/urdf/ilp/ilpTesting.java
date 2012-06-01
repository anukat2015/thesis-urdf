package urdf.ilp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 	@author Christina Teflioudi
 *	
 *	Main entrance point for executing learning
 */
public class ilpTesting 
{

	public static void main(String[] args) 
	{
		try
		{	
			float supportThreshold=0.001f;
			float confidenceThreshold=0.10f;
			float specialityRatioThreshold=0.15f;
			
			int partitionNumber=3;
			int possiblePosToBeCovered=1;
			int positivesCovered=2;
			float functionThreshold=0.98f;
			float symmetryThreshold=0.4f;
			int smoothingMethod=0;
			float stoppingThreshold=0.98f;
			ArrayList<Relation> relations=null;
			ArrayList<Type> types=null;
			HashMap<Integer,String>relationsForConstants=null;
			int noise=0;
			
			int depth=2;
			boolean allowFreeVars=false;
			boolean tryConstants=false;
			boolean learnOnlyConstants=true;
			
			
			long timer = System.currentTimeMillis();
			
			
			// relations for yago
//			String[] relationsToBeLearned={"livesIn","hasChild","bornIn","hasPredecessor"};
//			int[] inputArg={1,1,1,1};
			
			String[] relationsToBeLearned={"hasChild"};
			int[] inputArg={1};
			
//			String[] relationsToBeLearned={"hasCapital","hasChild",
//					"isAffiliatedTo", "originatesFrom", "worksAt","diedIn"};
//			int[] inputArg={1,1,1,1,1,1};
			
			
//			String[] relationsToBeLearned={"livesIn","politicianOf","bornIn","graduatedFrom","hasPredecessor",
//					"hasSuccessor","locatedIn","isMarriedTo","influences","hasCapital","hasChild",
//					"isAffiliatedTo", "originatesFrom", "worksAt","diedIn"};
//			int[] inputArg={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
			


			
			// relations for yago2
//			String[] relationsToBeLearned={"politicianOf"};
//			int[] inputArg={1};
			
//			String[] relationsToBeLearned={"isPoliticianOf","graduatedFrom","directed","hasAcademicAdvisor",
//					"hasCapital","hasPredecessor","hasSuccessor","isKnownFor","isLocatedIn","isMarriedTo","hasChild", "worksAt"};
//			int[] inputArg={1,1,1,1,1,1,1,1,1,1,1,1};
			

			
			
			
			// for inputArg=1
			LearningManager lm= new LearningManager(args[0],  partitionNumber, supportThreshold,  confidenceThreshold, specialityRatioThreshold,  possiblePosToBeCovered,
					 positivesCovered,  functionThreshold,  symmetryThreshold,  smoothingMethod,stoppingThreshold,
					relations,types, relationsForConstants,noise,1, "facts", "facts");
			
			// for inputArg=2
//			LearningManager lm= new LearningManager(args[0],  partitionNumber, supportThreshold,  confidenceThreshold, generalityRatioThreshold,  possibleExamplesThreshold,
//					 exactExamplesThreshold,  functionThreshold,  symmetryThreshold,  smoothingMethod,stoppingThreshold,
//					relations,types, relationsForConstants,noise,2, "facts");
			
			
			lm.learnOnCrossValidation(relationsToBeLearned,inputArg, depth,  allowFreeVars,tryConstants,learnOnlyConstants);
			
			timer=System.currentTimeMillis()-timer;
			System.out.println("TIME ELAPSED: "+ timer);
	
			
		}
		catch(java.sql.SQLException e)
		{
			System.out.println(e.toString());
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
	


}
