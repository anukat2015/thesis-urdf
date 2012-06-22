package urdf.webservice;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import urdf.api.UBindingSet;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UHardRule;
import urdf.api.ULineageAnd;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.USoftRule;
import urdf.reasoner.Sampling;
import urdf.reasoner.URDF;
import MPII_SPARQL_endpoint.services.MPII_SPARQL_service;

public class URDF_service extends MPII_SPARQL_service {

  String triplet = "\\s*\\?{0,1}\\w+\\d*\\s*\\(\\s*((\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,1}\\w+\\d*\\s*))\\s*\\,\\s*((\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,1}\\w+\\d*\\s*))\\s*(\\,\\s*\\d+\\s*){0,1}\\)";
  String arg = "(\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,1}\\w+\\d*\\s*)"; // quoted literals or strict alphanumeric only!

  Pattern pattern0 = Pattern.compile("(\\s*" + triplet + "\\s*\\;\\s*)*" + triplet + "\\s*\\;{0,1}\\s*");
  Pattern pattern1 = Pattern.compile(triplet);
  Pattern pattern2 = Pattern.compile(arg);

  public String processQuery(String queryString) {

    ArrayList<USoftRule> softRules;
    ArrayList<UHardRule> hardRules;

    StringBuffer resultString = new StringBuffer();

    try {

      softRules = new ArrayList<USoftRule>();

      /*Arrays.asList(

      new USoftRule("S1", new ULiteral(URelation.DIFFERENCELT, "?Y", "?Z", 85), 165847.0 / 192435.0, new ULiteral(URelation.BORNONDATE, "?X", "?Y"),
          new ULiteral(URelation.DIEDONDATE, "?X", "?Z")),

      new USoftRule("S2", new ULiteral(URelation.DIFFERENCELT, "?Y", "?Z", 65), 48594.0 / 124999.0, new ULiteral(URelation.BORNONDATE, "?X", "?Y"),
          new ULiteral(URelation.YEARBEFORE, "?Y", "1900"), new ULiteral(URelation.DIEDONDATE, "?X", "?Z")),

      new USoftRule("S3", new ULiteral(URelation.YEARBEFORE, "?Z", "?A"), 2715.0 / 2793.0, new ULiteral(URelation.HASCHILD, "?X", "?Y"), new ULiteral(
          URelation.BORNONDATE, "?Y", "?Z"), new ULiteral(URelation.DIEDONDATE, "?X", "?A")),

      new USoftRule("S4", new ULiteral(URelation.DIEDIN, "?X", "?Y"), 77.0 / 588.0, new ULiteral(URelation.BORNIN, "?X", "?Y"), new ULiteral(URelation.LIVESIN,
          "?X", "?Y")),

      new USoftRule("S5", new ULiteral(URelation.BORNIN, "?X", "?Z"), 4.0 / 51.0, new ULiteral(URelation.ISCITIZENOF, "?X", "?Y"), new ULiteral(
          URelation.LOCATEDIN, "?Z", "?Y"), new ULiteral(URelation.LIVESIN, "?X", "?Z")),

      new USoftRule("S6", new ULiteral(URelation.YEARBEFORE, "?Z", "?A"), 4528.0 / 4791.0, new ULiteral(URelation.ALMAMATER, "?X", "?Y"), new ULiteral(
          URelation.ESTABLISHEDONDATE, "?Y", "?Z"), new ULiteral(URelation.BORNONDATE, "?X", "?A")),

      new USoftRule("S7", new ULiteral(URelation.WORKSAT, "?Z", "?Y"), 165.0 / 859.0, new ULiteral(URelation.ALMAMATER, "?X", "?Y"), new ULiteral(
          URelation.ACADEMICADVISOR, "?X", "?Z")),

      new USoftRule("S8", new ULiteral(URelation.ALMAMATER, "?Z", "?A"), 1072.0 / 2130.0, new ULiteral(URelation.ACADEMICADVISOR, "?X", "?Y"), new ULiteral(
          URelation.ACADEMICADVISOR, "?Z", "?Y"), new ULiteral(URelation.ALMAMATER, "?X", "?A")),

      new USoftRule("S9", new ULiteral(URelation.BORNIN, "?X", "?Z"), 59.0 / 589.0, new ULiteral(URelation.MARRIEDTO, "?X", "?Y"), new ULiteral(
          URelation.BORNIN, "?Y", "?Z")),

      new USoftRule("S10", new ULiteral(URelation.LIVESIN, "?Y", "?Z"), 37.0 / 843.0, new ULiteral(URelation.MARRIEDTO, "?X", "?Y"), new ULiteral(
          URelation.LIVESIN, "?X", "?Z")),

      new USoftRule("S11", new ULiteral(URelation.LIVESIN, "?Y", "?Z"), 0.0 / 59.0, new ULiteral(URelation.MARRIEDTO, "?X", "?Y"), new ULiteral(
          URelation.BORNIN, "?X", "?Z"), new ULiteral(URelation.BORNIN, "?Y", "?Z")),

      new USoftRule("S12", new ULiteral(URelation.MARRIEDTO, "?X", "?Y"), 820.0 / 2172.0, new ULiteral(URelation.HASCHILD, "?X", "?Z"), new ULiteral(
          URelation.HASCHILD, "?Y", "?Z"), new ULiteral(URelation.NOTEQUALS, "?X", "?Y")),

      new USoftRule("S13", new ULiteral(URelation.NOTEQUALS, "?Y", "?Z"), 1242.0 / 1251.0, new ULiteral(URelation.HASCHILD, "?X", "?Y"), new ULiteral(
          URelation.MARRIEDTO, "?X", "?Z")),

      new USoftRule("S14", new ULiteral(URelation.BORNIN, "?C", "?Y"), 1.0 / 4.0, new ULiteral(URelation.LIVESIN, "?X", "?Y"), new ULiteral(URelation.LIVESIN,
          "?Z", "?Y"), new ULiteral(URelation.MARRIEDTO, "?X", "?Z"), new ULiteral(URelation.HASCHILD, "?X", "?C"),
          new ULiteral(URelation.HASCHILD, "?Z", "?C")),

      new USoftRule("S15", new ULiteral(URelation.NOTEQUALS, "?X", "?Z"), 16304.0 / 16626.0, new ULiteral(URelation.DIRECTED, "?X", "?Y"), new ULiteral(
          URelation.ACTEDIN, "?Z", "?Y")),

      new USoftRule("S16", new ULiteral(URelation.MARRIEDTO, "?X", "?Z"), 17.0 / 10346.0, new ULiteral(URelation.ACTEDIN, "?X", "?Y"), new ULiteral(
          URelation.ACTEDIN, "?Z", "?Y"), new ULiteral(URelation.NOTEQUALS, "?X", "?Z"))));*/

      hardRules = new ArrayList<UHardRule>();

      /*Arrays.asList(

      new UHardRule("H2", new ULiteral(URelation.BORNIN, "?X", "??Y")),

      new UHardRule("H3", new ULiteral(URelation.DIEDIN, "?X", "??Y")),

      new UHardRule("H4", new ULiteral(URelation.BORNONDATE, "?X", "??Y")),

      new UHardRule("H4", new ULiteral(URelation.MARRIEDTO, "?X", "??Y")),

      new UHardRule("H5", new ULiteral(URelation.DIEDONDATE, "?X", "??Y"))

      ));*/

      System.out.println("URDF-QUERY: " + queryString);

      URDF urdf = new URDF(this.getClass().getClassLoader().getResource("yago.ini").openStream(), softRules, hardRules);

      List<UFactSet> resultFacts = new ArrayList<UFactSet>();
      List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();

      ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();
      urdf.ground(parseQuery(queryString), resultFacts, resultBindings, resultLineage);
      Sampling.getConfAll(resultLineage);

      nid = 0;
      eid = 0;

      int i = 0;
      for (UFactSet facts : resultFacts) {

        int j = 0;

        n = new StringBuffer();
        s = new StringBuffer();

        nodes = new HashMap<String, String>();
        edges = new HashMap<String, String>();

        resultString.append("ANSWER" + i + "\t");

        for (UFact fact : facts) {
          resultString.append("N" + nid + ":" + toString(fact));
          for (ULineageAnd result : resultLineage)
            System.out.println(result.toString(-1));
          j++;
          if (j < facts.size())
            resultString.append(";");
        }

        printDependencyGraph(facts/*urdf.globalDependencyGraph*/);

        resultString.append("\n");
        resultString.append(n);
        resultString.append(s);

        i++;
      }

      // System.out.println(resultString.toString());

      resultFacts.clear();
      resultBindings.clear();
      urdf.clear();
      urdf.close();

    } catch (Exception e) {
      e.printStackTrace();
      return e.getMessage();
    }

    return resultString.toString();
  }

  public UQuery parseQuery(String queryString) throws Exception {
    ArrayList<ULiteral> literals = new ArrayList<ULiteral>();
    Matcher matcher0 = pattern0.matcher(queryString);
    if (!matcher0.matches())
      throw new Exception("MALFORMED QUERY: " + queryString);
    int i = 0;
    Matcher matcher1 = pattern1.matcher(queryString);
    while (matcher1.find()) {
      String literal = matcher1.group(), rel = null, arg1 = null, arg2 = null, arg3 = "0";
      // System.out.println("____" + literal);
      Matcher matcher2 = pattern2.matcher(literal);
      if (matcher2.find())
        rel = matcher2.group().trim();
      else
        throw new Exception("MISSING RELATION: " + literal);
      if (matcher2.find())
        arg1 = matcher2.group().trim();
      else
        throw new Exception("MISSING FIRST ARGUMENT: " + literal);
      if (matcher2.find())
        arg2 = matcher2.group().trim();
      else
        throw new Exception("MISSING SECOND ARGUMENT!" + literal);
      if (matcher2.find())
        arg3 = matcher2.group().trim();
      try {
        if (rel.startsWith("?"))
          literals.add(new ULiteral(URelation.valueOf(rel), arg1, arg2, Integer.parseInt(arg3)));
        else
          literals.add(new ULiteral(URelation.valueOf(rel), arg1, arg2, Integer.parseInt(arg3)));
      } catch (Exception e) {
        e.printStackTrace();
        throw new Exception("MALFORMED QUERY ATOM: " + literal);
      }
      i++;
    }
    return new UQuery("WebServiceQuery", literals);
  }

  static int nid, eid;
  static StringBuffer n, s;

  static HashMap<String, String> nodes;
  static HashMap<String, String> edges;

  public static void printDependencyGraph(UFactSet facts) {

    for (UFact fact : facts) {
      if (nodes.get(fact.getFirstArgumentName()) == null || nodes.get(fact.getSecondArgumentName()) == null
          || nodes.get(fact.getFirstArgumentName()).equals(nodes.get(fact.getSecondArgumentName())))
        continue;

      edges.put(fact.getFirstArgumentName().compareTo(fact.getSecondArgumentName()) < 0 ? fact.getFirstArgumentName() + "$" + fact.getSecondArgumentName()
          : fact.getSecondArgumentName() + "$" + fact.getFirstArgumentName(), "O" + (eid++) + ":" + nodes.get(fact.getFirstArgumentName()) + ";"
          + nodes.get(fact.getSecondArgumentName()));
    }

    for (String edge : edges.values())
      s.append(edge + "\n");

  }

  private static String toString(UFact fact) {
    return fact.getRelationName() + "(" + fact.getFirstArgumentName() + "," + fact.getSecondArgumentName() + ")[" + fact.getTruthValue() + "|"
        + NumberFormat.getInstance().format(fact.getBaseConfidence()) + "]";
  }

  public static void main(String[] args) {
    String query = "?r1(Alan_Turing,?x);?r1(Konrad_Zuse,?x)";
    // String query = "livesIn(Al_Gore,?x)";
    // String query = "livesIn(Woody_Allen,?x)";
    System.out.println(new URDF_service().processQuery(query));
  }
}
