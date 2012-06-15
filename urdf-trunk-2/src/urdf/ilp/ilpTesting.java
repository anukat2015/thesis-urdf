package urdf.ilp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

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

			int depth = 2;
			boolean allowFreeVars = false;
			boolean tryConstants = false;
			boolean learnOnlyConstants = true;

			long timer = System.currentTimeMillis();
			/*
			Connection conn = QueryHandler.getConnection(args[0]);
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			rs= stmt.executeQuery("SELECT DISTINCT(relation) FROM rel_stats");
			while (rs.next()) {
				Relation r = new Relation(rs.getString(1), conn);
				System.out.println(r.getName() + r.getRange().getName() + r.getDomain().getName());
				relations.add(r);		
			}
			System.out.println("Relations created");
			
			rs = stmt.executeQuery("SELECT DISTINCT(arg2) FROM facts WHERE relation='hasRange' OR relation='hasDomain'");
			while (rs!=null && rs.next()) {
				types.add(new Type(rs.getString(1), conn));			
			}
			System.out.println("Types created");
			*/
			
			

			// relations for yago
			// String[]
			// relationsToBeLearned={"livesIn","hasChild","bornIn","hasPredecessor"};
			// int[] inputArg={1,1,1,1};

			// String[] relationsToBeLearned={"hasChild"};
			// int[] inputArg={1};

			// String[] relationsToBeLearned={"hasCapital","hasChild",
			// "isAffiliatedTo", "originatesFrom", "worksAt","diedIn"};
			// int[] inputArg={1,1,1,1,1,1};

			// String[]
			// relationsToBeLearned={"livesIn","politicianOf","bornIn","graduatedFrom","hasPredecessor",
			// "hasSuccessor","locatedIn","isMarriedTo","influences","hasCapital","hasChild",
			// "isAffiliatedTo", "originatesFrom", "worksAt","diedIn"};
			// int[] inputArg={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

			// relations for yago2
			String[] relationsToBeLearned={"politicianOf"};
			int[] inputArg={1};

			//String[] relationsToBeLearned = { "isPoliticianOf",
			//		"graduatedFrom", "directed", "hasAcademicAdvisor",
			//		"hasCapital", "hasPredecessor", "hasSuccessor",
			//		"isKnownFor", "isLocatedIn", "isMarriedTo", "hasChild",
			//		"worksAt" };
			//int[] inputArg = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

			// for inputArg=1
			LearningManager lm = new LearningManager(args[0], partitionNumber,
					supportThreshold, confidenceThreshold,
					specialityRatioThreshold, possiblePosToBeCovered,
					positivesCovered, functionThreshold, symmetryThreshold,
					smoothingMethod, stoppingThreshold, relations, types,
					relationsForConstants, noise, 1, "facts", "facts");

			// for inputArg=2
			// LearningManager lm= new LearningManager(args[0], partitionNumber,
			// supportThreshold, confidenceThreshold, generalityRatioThreshold,
			// possibleExamplesThreshold,
			// exactExamplesThreshold, functionThreshold, symmetryThreshold,
			// smoothingMethod,stoppingThreshold,
			// relations,types, relationsForConstants,noise,2, "facts");
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
