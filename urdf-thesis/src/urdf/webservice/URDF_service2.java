package urdf.webservice;

import java.text.NumberFormat;
import java.util.ArrayList;
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

public class URDF_service2 extends MPII_SPARQL_service {

  private static final String triplet = "\\s*\\?{0,1}\\w+\\d*\\s*\\(\\s*((\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,1}\\w+\\d*\\s*))\\s*\\,\\s*((\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,1}\\w+\\d*\\s*))\\s*(\\,\\s*\\d+\\s*){0,1}\\)";
  private static final String arg = "(\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,1}\\w+\\d*\\s*)"; // quoted literals or strict alphanumeric only!

  private static final Pattern pattern0 = Pattern.compile("(\\s*" + triplet + "\\s*\\;\\s*)*" + triplet + "\\s*\\;{0,1}\\s*");
  private static final Pattern pattern1 = Pattern.compile(triplet);
  private static final Pattern pattern2 = Pattern.compile(arg);

  public String processQuery(String queryString) {

    ArrayList<USoftRule> softRules;
    ArrayList<UHardRule> hardRules;

    StringBuffer resultString = new StringBuffer();

    try {

      softRules = new ArrayList<USoftRule>();
      hardRules = new ArrayList<UHardRule>();

      System.out.println("URDF-QUERY: " + queryString + " " + parseQuery(queryString));
      URDF urdf = new URDF(this.getClass().getClassLoader().getResource("yago2.ini").openStream(), softRules, hardRules);

      List<UFactSet> resultFacts = new ArrayList<UFactSet>();
      List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();

      ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();
      urdf.ground(parseQuery(queryString), resultFacts, resultBindings, resultLineage);
      Sampling.getConfAll(resultLineage);

      int i = 0;
      for (UFactSet facts : resultFacts) {
        int j = 0;
        for (UFact fact : facts) {
          resultString.append(toString(fact));
          if (++j < facts.size())
            resultString.append(";");
        }
        resultString.append("\n");
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

  private static String toString(UFact fact) {
    return fact.getRelationName() + "(" + fact.getFirstArgumentName() + "," + fact.getSecondArgumentName() + ")[" + UFact.TRUTH_LABELS[fact.getTruthValue()]
        + "|" + NumberFormat.getInstance().format(fact.getBaseConfidence()) + "]";
  }

  public static void main(String[] args) {
    args = new String[] {"?r(Albert_Einstein,?x)"};
    for (String query : args)
      System.out.println(new URDF_service2().processQuery(query));
  }
}
