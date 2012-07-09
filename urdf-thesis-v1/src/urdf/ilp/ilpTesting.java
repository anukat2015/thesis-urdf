package urdf.ilp;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Christina Teflioudi
 * 
 *         Main entrance point for executing learning
 */
public class ilpTesting {

	public static void main(String[] args) {
		try {
			float supportThreshold = 0.001f;
			float confidenceThreshold = 0.10f;
			float specialityRatioThreshold = 0.15f;

			int partitionNumber = 3;
			int possiblePosToBeCovered = 1;
			int positivesCovered = 2;
			float functionThreshold = 0.98f;
			float symmetryThreshold = 0.4f;
			int smoothingMethod = 0;
			float stoppingThreshold = 0.98f;
			ArrayList<Relation> relations = new ArrayList<Relation>();
			ArrayList<Type> types = new ArrayList<Type>();
			HashMap<Integer, String> relationsForConstants = null;
			int noise = 0;

			int depth = 3;
			boolean allowFreeVars = false;
			boolean tryConstants = true;
			boolean learnOnlyConstants = true;

			long timer = System.currentTimeMillis();


			/* String[]
			 relationsToBeLearned = { "<http://yago-knowledge.org/resource/isPoliticianOf>",
					 				  "<http://yago-knowledge.org/resource/livesIn>",
					 				  "<http://yago-knowledge.org/resource/wasBornIn>",
					 				  "<http://yago-knowledge.org/resource/isLocatedIn>",
					 				  "<http://yago-knowledge.org/resource/isMarriedTo>",
					 				  "<http://yago-knowledge.org/resource/hasCapital>",
					 				  "<http://yago-knowledge.org/resource/hasChild>",
					 				  "<http://yago-knowledge.org/resource/worksAt>",
					 				  "<http://yago-knowledge.org/resource/hasPoverty>",
					 				  "<http://yago-knowledge.org/resource/hasOfficialLanguage>",
					 				  "<http://yago-knowledge.org/resource/produced>",
					 				  "<http://yago-knowledge.org/resource/created>",
					 				  "<http://yago-knowledge.org/resource/actedIn>",
					 				  "<http://yago-knowledge.org/resource/directed>",
					 				  "<http://yago-knowledge.org/resource/diedIn>"};
			// int[] inputArg = {2,0,1,2,0,2,2,0,1,2,0,1,2,0,1};
			 int[] inputArg =   {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
			*/
			
			// relations for yago2
			//String[] relationsToBeLearned={"<http://yago-knowledge.org/resource/livesIn>"};
			String[] relationsToBeLearned={"<http://yago-knowledge.org/resource/isPoliticianOf>"};
			//String[] relationsToBeLearned={"<http://yago-knowledge.org/resource/diedIn>"};
			//String[] relationsToBeLearned={"<http://yago-knowledge.org/resource/directed>"};
			int[] inputArg={1};

			// for inputArg=1
			LearningManager lm = new LearningManager(args[0], partitionNumber,
					supportThreshold, confidenceThreshold,
					specialityRatioThreshold, possiblePosToBeCovered,
					positivesCovered, functionThreshold, symmetryThreshold,
					smoothingMethod, stoppingThreshold, relations, types,
					relationsForConstants, noise, 1, "facts", "facts");


			System.out.println("Start Learning on Cross Validation");
			
			lm.learnOnCrossValidation(relationsToBeLearned, inputArg, depth,
					allowFreeVars, tryConstants, learnOnlyConstants);

			timer = System.currentTimeMillis() - timer;
			System.out.println("TIME ELAPSED: " + timer);

		} catch (java.sql.SQLException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

}
