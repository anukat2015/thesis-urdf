package urdf.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import urdf.api.UArgument;
import urdf.api.UBindingSet;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedSoftRule;
import urdf.api.UHardRule;
import urdf.api.ULineageAnd;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.USoftRule;
import urdf.reasoner.MAXSAT;
import urdf.reasoner.URDF;

public class URDF_LUBM_main {

	static ArrayList<USoftRule> softRules;

	static ArrayList<UHardRule> hardRules;

	public static void main(String[] args) throws Exception {

		PrintStream Sysout = new PrintStream(System.out, true, "UTF-8"); // for UTF-8 console output

		hardRules = new ArrayList<UHardRule>();

		softRules = new ArrayList<USoftRule>(Arrays.asList(new USoftRule("S1", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 30000 / 30000, new ULiteral(
		    URelation.NOTEQUALS, "Professor", "AssistantProfessor"), new ULiteral(URelation.lubm_TYPE, "?X", "AssistantProfessor")),

		new USoftRule("S2", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "Professor", "AssociateProfessor"),
		    new ULiteral(URelation.lubm_TYPE, "?X", "AssociateProfessor")),

		new USoftRule("S3", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "Professor", "FullProfessor"),
		    new ULiteral(URelation.lubm_TYPE, "?X", "FullProfessor")),

		new USoftRule("S4", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "Professor", "VisitingProfessor"),
		    new ULiteral(URelation.lubm_TYPE, "?X", "VisitingProfessor")),

		new USoftRule("S5", new ULiteral(URelation.lubm_TYPE, "?X", "Student"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "Student", "GraduateStudent"),
		    new ULiteral(URelation.lubm_TYPE, "?X", "GraduateStudent")),

		new USoftRule("S6", new ULiteral(URelation.lubm_TYPE, "?X", "Student"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "Student", "UndergraduateStudent"),
		    new ULiteral(URelation.lubm_TYPE, "?X", "UndergraduateStudent")),

		new USoftRule("S7", new ULiteral(URelation.lubm_ALLMEMBERSOF, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_MEMDEROF, "?X", "?Y")),

		new USoftRule("S8", new ULiteral(URelation.lubm_ALLMEMBERSOF, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_WORKSFOR, "?X", "?Y")),

		new USoftRule("S9", new ULiteral(URelation.lubm_SUBORGANISATIONOF, "?X", "?Z"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "?X", "?Y"), new ULiteral(
		    URelation.lubm_SUBORGANISATIONOF, "?X", "?Y"), new ULiteral(URelation.lubm_SUBORGANISATIONOF, "?Y", "?Z")),

		new USoftRule("S10", new ULiteral(URelation.lubm_CHAIR, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_HEADOF, "?X", "?Y")),

		new USoftRule("S11", new ULiteral(URelation.lubm_HASALUMNUS, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_DEGREEFROM, "?Y", "?X")),

		new USoftRule("S12", new ULiteral(URelation.lubm_DEGREEFROM, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_DOCTORALDEGREEFROM, "?X", "?Y")),

		new USoftRule("S13", new ULiteral(URelation.lubm_DEGREEFROM, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_MASTERSDEGREEFROM, "?X", "?Y")),

		new USoftRule("S14", new ULiteral(URelation.lubm_DEGREEFROM, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_UNDERGRADUATEDEGREEFROM, "?X", "?Y"))

		));

		softRules = new ArrayList<USoftRule>(Arrays.asList(new USoftRule("S1", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 30000 / 30000, new ULiteral(
		    URelation.lubm_TYPE, "?X", "AssistantProfessor")),

		new USoftRule("S2", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 1 / 1, new ULiteral(URelation.lubm_TYPE, "?X", "AssociateProfessor")),

		new USoftRule("S3", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 1 / 1, new ULiteral(URelation.lubm_TYPE, "?X", "FullProfessor")),

		new USoftRule("S4", new ULiteral(URelation.lubm_TYPE, "?X", "Professor"), 1 / 1, new ULiteral(URelation.lubm_TYPE, "?X", "VisitingProfessor")),

		new USoftRule("S5", new ULiteral(URelation.lubm_TYPE, "?X", "Student"), 1 / 1, new ULiteral(URelation.lubm_TYPE, "?X", "GraduateStudent")),

		new USoftRule("S6", new ULiteral(URelation.lubm_TYPE, "?X", "Student"), 1 / 1, new ULiteral(URelation.lubm_TYPE, "?X", "UndergraduateStudent")),

		new USoftRule("S7", new ULiteral(URelation.lubm_ALLMEMBERSOF, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_MEMDEROF, "?X", "?Y")),

		new USoftRule("S8", new ULiteral(URelation.lubm_ALLMEMBERSOF, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_WORKSFOR, "?X", "?Y")),

		new USoftRule("S9", new ULiteral(URelation.lubm_SUBORGANISATIONOF, "?X", "?Z"), 1 / 1, new ULiteral(URelation.NOTEQUALS, "?X", "?Y"), new ULiteral(
		    URelation.lubm_SUBORGANISATIONOF, "?X", "?Y"), new ULiteral(URelation.lubm_SUBORGANISATIONOF, "?Y", "?Z")),

		new USoftRule("S10", new ULiteral(URelation.lubm_CHAIR, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_HEADOF, "?X", "?Y")),

		new USoftRule("S11", new ULiteral(URelation.lubm_HASALUMNUS, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_DEGREEFROM, "?Y", "?X")),

		new USoftRule("S12", new ULiteral(URelation.lubm_DEGREEFROM, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_DOCTORALDEGREEFROM, "?X", "?Y")),

		new USoftRule("S13", new ULiteral(URelation.lubm_DEGREEFROM, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_MASTERSDEGREEFROM, "?X", "?Y")),

		new USoftRule("S14", new ULiteral(URelation.lubm_DEGREEFROM, "?X", "?Y"), 1 / 1, new ULiteral(URelation.lubm_UNDERGRADUATEDEGREEFROM, "?X", "?Y"))

		));

		UQuery[] q = new UQuery[14];

		q[0] = new UQuery("Q1", new ULiteral(URelation.lubm_TAKESCOURSE, "?X", "http://www.Department0.University0.edu/GraduateCourse0"));

		q[1] = new UQuery("Q2", new ULiteral(URelation.lubm_TYPE, "?X", "GraduateStudent"), new ULiteral(URelation.lubm_MEMDEROF, "?X", "?Z"), new ULiteral(
		    URelation.lubm_SUBORGANISATIONOF, "?Z", "?Y"), new ULiteral(URelation.lubm_UNDERGRADUATEDEGREEFROM, "?X", "?Y"));

		q[2] = new UQuery("Q3", new ULiteral(URelation.lubm_PUBLICATIONAUTHOR, "?X", "http://www.Department0.University0.edu/AssistantProfessor0"));

		q[3] = new UQuery("Q4", new ULiteral(URelation.lubm_WORKSFOR, "?X", "http://www.Department0.University0.edu"), new ULiteral(URelation.lubm_TYPE, "?X",
		    "Professor"), new ULiteral(URelation.lubm_NAME, "?X", "?Z1"), new ULiteral(URelation.lubm_EMAIL, "?X", "?Z2"), new ULiteral(URelation.lubm_TELEPHONE,
		    "?X", "?Z3"));

		q[4] = new UQuery("Q5", new ULiteral(URelation.lubm_ALLMEMBERSOF, "?X", "http://www.Department0.University0.edu"), new ULiteral(URelation.TYPE,
		    "http://www.Department0.University0.edu", "Department"));

		q[5] = new UQuery("Q6", new ULiteral(URelation.lubm_TYPE, "?X", "Student"));

		q[6] = new UQuery("Q7", new ULiteral(URelation.lubm_TYPE, "?X", "Student"), new ULiteral(URelation.lubm_TEACHEROF,
		    "http://www.Department0.University0.edu/AssociateProfessor0", "?Y"), new ULiteral(URelation.lubm_TAKESCOURSE, "?X", "?Y"));

		q[7] = new UQuery("Q8", new ULiteral(URelation.lubm_MEMDEROF, "?X", "?Y"), new ULiteral(URelation.lubm_SUBORGANISATIONOF, "?Y",
		    "http://www.University0.edu"), new ULiteral(URelation.lubm_EMAIL, "?X", "?Z"));

		q[8] = new UQuery("Q9", new ULiteral(URelation.lubm_TYPE, "?X", "Student"), new ULiteral(URelation.lubm_ADVISOR, "?X", "?Y"), new ULiteral(
		    URelation.lubm_TAKESCOURSE, "?X", "?Z"), new ULiteral(URelation.lubm_TEACHEROF, "?Y", "?Z"));

		q[9] = new UQuery("Q10", new ULiteral(URelation.lubm_TAKESCOURSE, "?X", "http://www.Department0.University0.edu/GraduateCourse0"));

		q[10] = new UQuery("Q11", new ULiteral(URelation.lubm_SUBORGANISATIONOF, "?X", "http://www.University0.edu"), new ULiteral(URelation.lubm_TYPE, "?X",
		    "ResearchGroup"));

		q[11] = new UQuery("Q12", new ULiteral(URelation.lubm_CHAIR, "?X", "?Y"), new ULiteral(URelation.lubm_WORKSFOR, "?X", "?Y"), new ULiteral(
		    URelation.lubm_SUBORGANISATIONOF, "?Y", "http://www.University0.edu"));
		q[12] = new UQuery("Q13", new ULiteral(URelation.lubm_HASALUMNUS, "http://www.University0.edu", "?X"));

		q[13] = new UQuery("Q14", new ULiteral(URelation.lubm_TYPE, "?X", "UndergraduateStudent"));

		URDF urdf = new URDF(args[0], softRules, hardRules);
		// URDF.DIRECT_EXPAND = true;

		int j, k, numOccurrencesSoft, numOccurrencesSoft2, numOccurrencesHard;
		long time1 = 0, time2 = 0, alg_time1 = 0, time;

		double urdf_MAX_SAT = 0;

		List<UFactSet> resultFacts = new ArrayList<UFactSet>();
		List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
		ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();

		// for (USoftRule rule : urdf.softRules)
		// Sysout.println(rule);
		// Sysout.println();

		// for (USoftRule rule : urdf.softRules)
		// URDF.getRuleWeightClosedWorld(urdf, rule);
		// Sysout.println();

		UQuery query = null;
		for (j = 0; j < q.length; j++) {
			// for (j = 5; j < 6; j++) {
			query = q[j];

			// Sysout.println(query);

			resultFacts.clear();
			resultBindings.clear();
			resultLineage.clear();

			// urdf.clear();
			urdf.sortBySelectivity(query, new UBindingSet());
			urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, true, true,
		         new ULineageAnd(null), resultLineage);
			urdf.invertRules(urdf.globalDependencyGraph);

			numOccurrencesSoft = urdf.globalDependencyGraph.size();
			for (UGroundedSoftRule softRule : urdf.softRuleGroundings)
				numOccurrencesSoft += softRule.size();

			numOccurrencesSoft2 = 0;
			for (UFact fact : urdf.globalDependencyGraph)
				numOccurrencesSoft2 += urdf.invSoftRules.get(fact).size();

			numOccurrencesHard = 0;
			for (UFactSet hardRule : urdf.hardRuleGroundings)
				numOccurrencesHard += hardRule.size();

			alg_time1 = 0;
			time1 = 0;
			time2 = 0;

			for (k = 0; k < 5; k++) {

				resultFacts.clear();
				resultBindings.clear();
				resultLineage.clear();
				urdf.clear();

				// Ground the query
				time = System.currentTimeMillis();
				urdf.sortBySelectivity(query, new UBindingSet());
			    urdf.ground(query, new UFactSet(), new UBindingSet(), resultFacts, resultBindings, 0, new HashSet<UArgument>(), new UFactSet(), null, 0, true, true,
			        new ULineageAnd(null), resultLineage);
				urdf.invertRules(urdf.globalDependencyGraph);
				time1 += System.currentTimeMillis() - time;
				time2 += System.currentTimeMillis() - time;

				time = System.currentTimeMillis();
				MAXSAT maxsat = new MAXSAT(urdf.softRuleGroundings, urdf.hardRuleGroundings, urdf.invSoftRules);
				maxsat.processMAXSAT();
				alg_time1 += System.currentTimeMillis() - time + 1;
				// urdf_MAX_SAT = maxsat.evalMaxSat(urdf.globalDependencyGraph);
			}

			Math.ceil(time1 /= k);
			Math.ceil(time2 /= k);
			Math.ceil(alg_time1 /= k);
			// Math.ceil(alg_time2 /= k);

			Sysout.println(j + "\tRESULTS:" + resultLineage.size() + "\tFACTS:" + urdf.globalDependencyGraph.size() + "\tSOFT-RULES:"
			    + urdf.softRuleGroundings.size() + "\tSOFT-RULES SIZE:" + numOccurrencesSoft + "\tHARD-RULES:" + urdf.hardRuleGroundings.size()
			    + "\tHARD-RULES SIZE:" + numOccurrencesHard + "\tHARD+SOFT-RULES SIZE:" + (numOccurrencesSoft + numOccurrencesHard) + "\tGROUND(ms):" + time1
			    + "\tURDF(ms):" + alg_time1 + "\tURDF-MAXSAT:" + urdf_MAX_SAT);

			System.gc();
		}
		urdf.close();
	}
}
