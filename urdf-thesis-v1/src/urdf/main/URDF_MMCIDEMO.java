package urdf.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
import urdf.reasoner.Sampling;
import urdf.reasoner.URDF;

public class URDF_MMCIDEMO {

  static ArrayList<USoftRule> softRules;

  static ArrayList<UHardRule> hardRules;

  public static void main(String[] args) throws Exception {

    PrintStream Sysout = new PrintStream(System.out); // , true, "UTF-8"); // for UTF-8 console output

    URelation HASDESTINATION = URelation.valueOf("hasDestination");
    URelation HASEVENT = URelation.valueOf("hasEvent");
    URelation EVENTLOCATION = URelation.valueOf("eventLocation");
    URelation EVENTDATE = URelation.valueOf("eventDate");
    URelation JOINTCORRESPONDENCE = URelation.valueOf("hasEmailCorrespondence");
    URelation LOCATIONREFERENCE = URelation.valueOf("hasLocationReference");
    URelation LOCATIONNAME = URelation.valueOf("hasLocationName");
    URelation DATEREFERENCE = URelation.valueOf("hasDateReference");
    URelation HASEMAIL = URelation.valueOf("hasEmailAddress");
    URelation RECIPIENT = URelation.valueOf("hasRecipient");
    URelation SENDER = URelation.valueOf("hasSender");
    URelation ROOMMATES = URelation.valueOf("RoomMates");
    URelation HASPHONE = URelation.valueOf("hasPhone");
    URelation FIRSTNAME = URelation.valueOf("hasFirstName");
    URelation LASTNAME = URelation.valueOf("hasLastName");

    softRules = new ArrayList<USoftRule>(Arrays
        .asList(

        new USoftRule("S1", new ULiteral(ROOMMATES, "?LASTNAME_A", "?LASTNAME_B"), 1.0, new ULiteral(LASTNAME, "?ID_A", "?LASTNAME_A"), new ULiteral(FIRSTNAME,
            "?ID_A", "?FIRSTNAME_A"), new ULiteral(LASTNAME, "?ID_B", "?LASTNAME_B"), new ULiteral(FIRSTNAME, "?ID_B", "?FIRSTNAME_B"), new ULiteral(HASPHONE,
            "?ID_A", "?PHONE_A"), new ULiteral(HASPHONE, "?ID_B", "?PHONE_B"), new ULiteral(URelation.EQUALS, "?PHONE_A", "?PHONE_B"), new ULiteral(
            URelation.NOTEQUALS, "?ID_A", "?ID_B")),

        new USoftRule("S2", new ULiteral(JOINTCORRESPONDENCE, "?ID_A", "?ID_B"), 1.0, new ULiteral(HASEMAIL, "?ID_A", "?EMAIL_A"), new ULiteral(HASEMAIL,
            "?ID_B", "?EMAIL_B"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_A"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_B"), new ULiteral(
            URelation.NOTEQUALS, "?ID_A", "?ID_B"), new ULiteral(URelation.NOTEQUALS, "?EMAIL_A", "?EMAIL_B")),

        new USoftRule("S3", new ULiteral(JOINTCORRESPONDENCE, "?ID_A", "?ID_B"), 1.0, new ULiteral(HASEMAIL, "?ID_A", "?EMAIL_A"), new ULiteral(HASEMAIL,
            "?ID_B", "?EMAIL_B"), new ULiteral(SENDER, "?EVENTID", "?EMAIL_A"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_B"), new ULiteral(
            URelation.NOTEQUALS, "?ID_A", "?ID_B"), new ULiteral(URelation.NOTEQUALS, "?EMAIL_A", "?EMAIL_B")),

        new USoftRule("S4", new ULiteral(JOINTCORRESPONDENCE, "?ID_A", "?ID_B"), 1.0, new ULiteral(HASEMAIL, "?ID_A", "?EMAIL_A"), new ULiteral(HASEMAIL,
            "?ID_B", "?EMAIL_B"), new ULiteral(SENDER, "?EVENTID", "?EMAIL_B"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_A"), new ULiteral(
            URelation.NOTEQUALS, "?ID_A", "?ID_B"), new ULiteral(URelation.NOTEQUALS, "?EMAIL_A", "?EMAIL_B")),

        new USoftRule("S5", new ULiteral(HASDESTINATION, "?ID", "?LOCATION"), 1.0, new ULiteral(HASEMAIL, "?ID", "?EMAIL"), new ULiteral(RECIPIENT, "?EVENTID",
            "?EMAIL"), new ULiteral(LOCATIONREFERENCE, "?EVENTID", "?LOCATIONID"), new ULiteral(LOCATIONNAME, "?LOCATIONID", "?LOCATION"), new ULiteral(
            DATEREFERENCE, "?EVENTID", "?DATE")),

        new USoftRule("S6", new ULiteral(HASDESTINATION, "?ID", "?LOCATION"), 1.0, new ULiteral(HASEMAIL, "?ID", "?EMAIL"), new ULiteral(SENDER, "?EVENTID",
            "?EMAIL"), new ULiteral(LOCATIONREFERENCE, "?EVENTID", "?LOCATIONID"), new ULiteral(LOCATIONNAME, "?LOCATIONID", "?LOCATION"), new ULiteral(
            DATEREFERENCE, "?EVENTID", "?DATE")),

        new USoftRule("S7", new ULiteral(HASEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASEMAIL, "?ID", "?EMAIL"), new ULiteral(RECIPIENT, "?EVENTID",
            "?EMAIL")),

            new USoftRule("S8", new ULiteral(HASEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASEMAIL, "?ID", "?EMAIL"), new ULiteral(SENDER, "?EVENTID",
                "?EMAIL")),

            new USoftRule("S9", new ULiteral(EVENTLOCATION, "?EVENTID", "?LOCATIONID"), 1.0, new ULiteral(LOCATIONREFERENCE, "?EVENTID", "?LOCATIONID")),

            new USoftRule("S10", new ULiteral(EVENTDATE, "?EVENTID", "?DATE"), 1.0, new ULiteral(DATEREFERENCE, "?EVENTID", "?DATE"))

        ));

    hardRules = new ArrayList<UHardRule>();

    UQuery[] q = new UQuery[7];

    // Single fact

    q[0] = new UQuery("Q1", new ULiteral(ROOMMATES, "\"Feld\"", "?X"));

    q[1] = new UQuery("Q2", new ULiteral(JOINTCORRESPONDENCE, "Christoph_Stahl", "?X"));

    q[2] = new UQuery("Q3", new ULiteral(HASDESTINATION, "Christoph_Stahl", "?X"));

    q[3] = new UQuery("Q4", new ULiteral(HASEVENT, "Christoph_Stahl", "?EVENTID"), new ULiteral(EVENTLOCATION, "?EVENTID", "?LOCATIONID"), new ULiteral(
        EVENTDATE, "?EVENTID", "?DATE"), new ULiteral(URelation.ISWITHINHOURS, "?DATE", "\"10.11.2010 09:00:00\"", 2));

    q[4] = new UQuery("Q5", new ULiteral(HASEVENT, "Christoph_Stahl", "?EVENTID"), new ULiteral(EVENTLOCATION, "?EVENTID", "?LOCATIONID"), new ULiteral(
        EVENTDATE, "?EVENTID", "?DATE"), new ULiteral(URelation.ISWITHINMINUTES, "?DATE", "\"10.11.2010 09:00:00\"", 60));

    q[5] = new UQuery("Q6", new ULiteral(HASEVENT, "Christoph_Stahl", "?EVENTID"), new ULiteral(EVENTLOCATION, "?EVENTID", "?LOCATIONID"), new ULiteral(
        EVENTDATE, "?EVENTID", "?DATE"), new ULiteral(URelation.SAMEDAY, "?DATE", "\"10.11.2010 09:00:00\""));

    q[6] = new UQuery("Q7", new ULiteral(HASEVENT, "Christoph_Stahl", "?EVENTID"), new ULiteral(HASEVENT, "Michael_Feld", "?EVENTID"), new ULiteral(
        EVENTLOCATION, "?EVENTID", "?LOCATIONID"), new ULiteral(EVENTDATE, "?EVENTID", "?DATE"), new ULiteral(URelation.SAMEDAY, "?DATE",
        "\"10.11.2010 09:00:00\""));

    int i, j;
    long time;

    URDF urdf = new URDF(args[0], softRules, hardRules);

    List<UFactSet> resultFacts = new ArrayList<UFactSet>();
    List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
    ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();
    UQuery query = null;

    // DEMO MODE: Ground all atoms and rules, display lineage, warm up cache
    for (i = 0; i < q.length; i++) {
      // for (i = 0; i < 2; i++) {
      query = q[i];

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

      // This is a simple PW-based confidence sampling
      time = System.currentTimeMillis();
      Sampling.getConfAll(resultLineage);
      Sysout.println("PW-CONF [" + (System.currentTimeMillis() - time) + "ms]");
      Sysout.println("SLD STEPS: " + URDF.SLD_steps);

      j = 0;
      for (ULineageAnd result : resultLineage) {
        Sysout.println("\nRESULT [" + i + "|" + (j + 1) + "/" + resultLineage.size() + "]\t\t");
        Sysout.print(result.toString(0));
        j++;
      }

      Sysout.println();

      j = 0;
      for (UFactSet hardRule : urdf.hardRuleGroundings) {
        if (hardRule.size() > 1) {
          Sysout.println("HARDRULE[" + i + "|" + (j + 1) + "/" + urdf.hardRuleGroundings.size() + "]\t" + hardRule);
          j++;
        }
      }

      j = 0;
      for (UGroundedSoftRule softRule : urdf.softRuleGroundings) {
        Sysout.println("SOFTRULE[" + i + "|" + (j + 1) + "/" + urdf.softRuleGroundings.size() + "]\t" + softRule);
        j++;
      }

      // dump(String.valueOf(i), urdf.globalDependencyGraph);

      resultFacts.clear();
      resultBindings.clear();
      resultLineage.clear();
      urdf.clear();

      System.gc();
    }

    urdf.close();
  }

  static void dump(String id, UFactSet dependencyGraph) throws Exception {
    BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\graph_" + id + ".txt")); // should be "UTF-8"
    for (UFact fact : dependencyGraph) {
      writer.write(id + "\t" + (long) fact.hashCode() + "\t" + fact.getRelationName() + "\t" + fact.getFirstArgumentName() + "\t"
          + fact.getSecondArgumentName() + "\t" + fact.getBaseConfidence() + "\n");
    }
    writer.flush();
    writer.close();
  }

  public static String getSp(int level) {
    String s = String.valueOf(level) + "\t";
    for (int i = 0; i < level; i++)
      s += "|";
    return s;
  }
}
