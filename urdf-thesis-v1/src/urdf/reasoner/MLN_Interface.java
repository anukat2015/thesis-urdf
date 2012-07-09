package urdf.reasoner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import urdf.api.UEntity;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.USoftRule;
import urdf.api.UTriplet;

public class MLN_Interface {

  public static final int MAP = 0, MC_SAT = 1;
  public static final String ALCHEMY_DIR = "F:\\Alchemy";

  public static boolean ENFORCE_CLOSED_WORLD = false;

  public static boolean GROUNDED_FORMULAS = false;
  public static boolean CLOSED_WORLD = true;

  public static int INFERENCE_MODE = 1;

  private static String owPredicates = "-ow ", cwPredicates = "-cw ";

  public static double run(String file, UQuery query, UFactSet facts, List<USoftRule> softRules, Set<UGroundedSoftRule> softRuleGroundings,
      Set<UGroundedHardRule> hardRuleGroundings) throws Exception {

    if (ENFORCE_CLOSED_WORLD)
      dumpClosedWorld(file, query, facts, softRules, softRuleGroundings, hardRuleGroundings);
    else
      dumpOpenWorld(file, query, facts, softRules, softRuleGroundings, hardRuleGroundings);
    double time = 0;

    String queryString = "";
    int i = 0;
    for (UTriplet t : (ENFORCE_CLOSED_WORLD ? facts : query)) {
      if (i > 0)
        queryString += ",";
      if (ENFORCE_CLOSED_WORLD)
        queryString += getPredicateConstant(t) + "(C)";
      else
        queryString += t.getRelationName() + "(" + normalize(t.getFirstArgumentName()) + "," + normalize(t.getSecondArgumentName()) + ")";
      i++;
    }

    String runString = "";

    if (INFERENCE_MODE == MC_SAT) {

      // MC-SAT
      runString = "bin\\infer " + (cwPredicates.length() > 4 ? cwPredicates : "") + " " + (owPredicates.length() > 4 ? owPredicates : "")
          + " -ms -maxSteps 10000  -e ./data/" + file + ".db -i ./data/" + file + ".mln -r ./data/" + file + ".result -q \"" + queryString + "\"";

      //runString = "bin\\infer -ms -maxSteps 10000  -e ./data/" + file + ".db -i ./data/" + file + ".mln -r ./data/" + file + ".result -q \"" + queryString
      //    + "\"";

    } else {

      // MAP-INFERENCE
      runString = "bin\\infer " + (cwPredicates.length() > 4 ? cwPredicates : "") + " " + (owPredicates.length() > 4 ? owPredicates : "")
          + " -lazy -lifted -a -maxSteps 10000 -e ./data/" + file + ".db -i ./data/" + file + ".mln -r ./data/" + file + ".result -q \"" + queryString + "\"";

      //runString = "bin\\infer " + " -lazy -lifted -a -maxSteps 10000 -e ./data/" + file + ".db -i ./data/" + file + ".mln -r ./data/" + file + ".result -q \""
      //    + queryString + "\"";

    }

    //System.out.println(runString);

    BufferedWriter writer = new BufferedWriter(new FileWriter(ALCHEMY_DIR + "\\alchemy.bat"));
    writer.write(runString);
    writer.flush();
    writer.close();

    runString = ALCHEMY_DIR + "\\alchemy.bat";

    ProcessBuilder builder = new ProcessBuilder(runString);
    builder.directory(new File(ALCHEMY_DIR));
    builder.redirectErrorStream(true);
    Process process = builder.start();

    StringBuffer buffer = new StringBuffer();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    String line;
    while ((line = reader.readLine()) != null)
      System.err.println(line);

    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    while ((line = reader.readLine()) != null) {
      buffer.append(line + "\n");
      //System.out.println(line);
      try {
        if (line.startsWith("total time taken = ")) {
          System.out.println("ALCHEMY: " + line);
          if (line.indexOf("hr") >= 0) {
            String hrs = line.substring(line.lastIndexOf("=") + 1, line.lastIndexOf("hr"));
            String mins = line.substring(line.lastIndexOf("=") + 1, line.lastIndexOf("min"));
            time = Double.parseDouble(hrs) * 3600 + Double.parseDouble(mins) * 60 + 0.001;
          } else if (line.indexOf("min") >= 0) {
            String mins = line.substring(line.lastIndexOf("=") + 1, line.lastIndexOf("min"));
            String secs = line.substring(line.lastIndexOf(",") + 1, line.lastIndexOf("sec"));
            time = Double.parseDouble(mins) * 60 + Double.parseDouble(secs) + 0.001;
          } else
            time = Double.parseDouble(line.substring(line.lastIndexOf("=") + 1, line.lastIndexOf("sec"))) + 0.001;
        }
      } catch (Exception e) {
        time = Double.NEGATIVE_INFINITY;
      }
    }

    process.waitFor();
    if (process.exitValue() < 0)
      time = Double.NEGATIVE_INFINITY;
    if (time < 0)
      System.err.println(buffer);

    return time;
  }

  public static UFactSet getTruthAssignmentsFromFile(String file, UFactSet dependencyFacts) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(ALCHEMY_DIR + "\\data\\" + file + ".result"));
    // System.out.println("URDF: " + dependencyFacts);

    String line;
    while ((line = reader.readLine()) != null) {
      try {
        StringTokenizer tok = new StringTokenizer(line, "(), ");
        UFact f = new UFact(URelation.valueOf(tok.nextToken()), new UEntity(tok.nextToken()), new UEntity(tok.nextToken()), 1.0);
        if ((f = dependencyFacts.lookup(f)) != null) {
          boolean isTrue = tok.nextToken().equals("1");
          // if ((f.truth == URDF.TRUE && !isTrue) || (f.truth == URDF.FALSE && isTrue))
          // System.out.println("\tSWITCH:" + f);
          // System.out.println("\tSET:" + f + " > " + isTrue);
          f.setTruthValue(isTrue ? UFact.TRUE : UFact.FALSE);
        }
      } catch (Exception e) {
        System.err.println(e);
        // e.printStackTrace();
      }
    }

    // System.out.println("MLN:  " + dependencyFacts);
    reader.close();
    return dependencyFacts;
  }

  private static void dumpOpenWorld(String file, UQuery query, UFactSet facts, List<USoftRule> softRules, Set<UGroundedSoftRule> softRuleGroundings,
      Set<UGroundedHardRule> hardRuleGroundings) throws Exception {

    BufferedWriter writer = new BufferedWriter(new FileWriter(ALCHEMY_DIR + "\\data\\" + file + ".db"));
    if (CLOSED_WORLD || !GROUNDED_FORMULAS) {
      for (UFact f : facts) {
        //System.out.println("DB: " + f + "  " + f.getBaseConfidence());
        //if (f.getBaseConfidence() == 1.0)
        writer.write((f.getBaseConfidence() == 1.0 ? "" : "?") + f.getRelationName() + "(" + normalize(f.getFirstArgumentName()) + ","
            + normalize(f.getSecondArgumentName()) + ")" + "\n");
      }
    }
    writer.flush();
    writer.close();

    writer = new BufferedWriter(new FileWriter(ALCHEMY_DIR + "\\data\\" + file + ".mln"));

    HashSet<String> queryPredicates = new HashSet<String>();
    HashSet<String> relations = new HashSet<String>();
    HashSet<String> constants = new HashSet<String>();
    for (UFact f : facts) {
      relations.add(f.getRelationName());
      if (!f.getFirstArgument().isVariable())
        constants.add(normalize(f.getFirstArgumentName()));
      if (!f.getSecondArgument().isVariable())
        constants.add(normalize(f.getSecondArgumentName()));
    }
    for (ULiteral l : query) {
      queryPredicates.add(l.getRelationName());
      if (!l.getFirstArgument().isVariable())
        constants.add(normalize(l.getFirstArgumentName()));
      if (!l.getSecondArgument().isVariable())
        constants.add(normalize(l.getSecondArgumentName()));
    }

    if (CLOSED_WORLD || !GROUNDED_FORMULAS) {
      for (UGroundedSoftRule groundedSoftRule : softRuleGroundings) {
        USoftRule softRule = groundedSoftRule.getSoftRule();
        if (groundedSoftRule.getHead() == null || softRule == null || softRule.size() == 0)
          continue;
        for (ULiteral l : softRule.getLiterals()) {
          relations.add(l.getRelationName());
          if (!l.getFirstArgument().isVariable())
            constants.add(normalize(l.getFirstArgumentName()));
          if (!l.getSecondArgument().isVariable())
            constants.add(normalize(l.getSecondArgumentName()));
        }
      }
    }

    writer.write("entity={");
    int i = 0;
    for (String constant : constants) {
      if (i > 0)
        writer.write(",");
      writer.write(constant);
      i++;
    }
    writer.write("}\n\n");

    owPredicates = "-ow ";
    cwPredicates = "-cw ";

    if (relations.size() > 0) {
      int i1 = 0, i2 = 0;
      for (String relation : relations) {
        writer.write(relation + "(entity,entity)\n");
        if (CLOSED_WORLD && !queryPredicates.contains(relation)) {
          if (i1 > 0)
            cwPredicates += ",";
          cwPredicates += relation;
          i1++;
        } else {
          if (i2 > 0)
            owPredicates += ",";
          owPredicates += relation;
          i2++;
        }
      }
      writer.write("\n");
    }

    if (hardRuleGroundings.size() > 0) {
      for (UFactSet hardRule : hardRuleGroundings)
        if (hardRule.size() > 1)
          writer.write(getMuxSquare(hardRule));
    }

    if (CLOSED_WORLD || !GROUNDED_FORMULAS) {

      HashSet<USoftRule> mlnRules = new HashSet<USoftRule>();
      for (UGroundedSoftRule groundedSoftRule : softRuleGroundings) {
        USoftRule softRule = groundedSoftRule.getSoftRule();
        if (groundedSoftRule.getHead() == null || softRule == null || softRule.size() == 0 || mlnRules.contains(softRule))
          continue;

        ULiteral head = softRule.getHead();
        writer.write(NumberFormat.getInstance().format(softRule.getWeight()) + " ");
        i = 0;
        for (ULiteral l : softRule.getLiterals()) {
          if (i > 0)
            writer.write(" ^ ");
          writer.write(l.getRelationName() + "(" + normalize(l.getFirstArgumentName()) + "," + normalize(l.getSecondArgumentName()) + ")");
          i++;
        }
        if (head != null)
          writer.write((softRule.size() > 0 ? " => " : "") + head.getRelationName() + "(" + normalize(head.getFirstArgumentName()) + ","
              + normalize(head.getSecondArgumentName()) + ")" + "\n");
        else
          writer.write("\n");
        mlnRules.add(softRule);
      }
      writer.write("\n");

    } else {

      for (UGroundedSoftRule softRule : softRuleGroundings) {
        if (softRule.getHead() == null)
          continue;

        UFact head = softRule.getHead();
        if (softRule.size() > 0 || head.getBaseConfidence() != 1.0)
          writer.write(NumberFormat.getInstance().format(softRule.getWeight()) + " ");
        i = 0;
        for (UFact f : softRule) {
          if (i > 0)
            writer.write(" ^ ");
          writer.write(f.getRelationName() + "(" + normalize(f.getFirstArgumentName()) + "," + normalize(f.getSecondArgumentName()) + ")");
          i++;
        }
        if (head != null)
          writer.write((softRule.size() > 0 ? " => " : "") + head.getRelationName() + "(" + normalize(head.getFirstArgumentName()) + ","
              + normalize(head.getSecondArgumentName()) + ")");
        if (softRule.size() == 0 && head.getBaseConfidence() == 1.0)
          writer.write(" .");
        writer.write("\n");
      }
      writer.write("\n");

    }

    // for (UFact f : facts)
    // writer.write(NumberFormat.getInstance().format(f.getConfidence()) + " " + f.getRelationName() + "(" + normalize(f.getFirstArgumentName()) + ","
    // + normalize(f.getSecondArgumentName()) + ")\n");
    // writer.write("\n");

    writer.flush();
    writer.close();
  }

  private static void dumpClosedWorld(String file, UQuery query, UFactSet facts, List<USoftRule> softRules, Set<UGroundedSoftRule> softRuleGroundings,
      Set<UGroundedHardRule> hardRuleGroundings) throws Exception {

    BufferedWriter writer = new BufferedWriter(new FileWriter(ALCHEMY_DIR + "\\data\\" + file + ".db"));
    writer.write("\n");
    writer.flush();
    writer.close();

    writer = new BufferedWriter(new FileWriter(ALCHEMY_DIR + "\\data\\" + file + ".mln"));

    HashSet<String> predicateConstants = new HashSet<String>();
    for (UFact f : facts)
      predicateConstants.add(getPredicateConstant(f));
    for (ULiteral l : query)
      predicateConstants.add(getPredicateConstant(l));
    writer.write("constants={C}\n\n");

    for (String s : predicateConstants)
      writer.write(s + "(constants)\n");
    writer.write("\n");

    for (UFactSet hardRule : hardRuleGroundings)
      if (hardRule.size() > 1)
        writer.write(getMuxSquarePredicateConstant(hardRule));
    writer.write("\n");

    int i;
    for (UGroundedSoftRule softRule : softRuleGroundings) {
      if (softRule.size() == 0 || softRule.getWeight() <= 0)
        continue;

      UFact head = softRule.getHead();
      if (softRule.size() > 0 || head.getBaseConfidence() != 1.0)
        writer.write(NumberFormat.getInstance().format(softRule.getWeight()) + " ");
      i = 0;
      for (UFact f : softRule) {
        if (i > 0)
          writer.write(" ^ ");
        writer.write(getPredicateConstant(f) + "(C)");
        i++;
      }
      if (head != null)
        writer.write((softRule.size() > 0 ? " => " : "") + getPredicateConstant(head) + "(C)");
      if (softRule.size() == 0 && head.getBaseConfidence() == 1.0)
        writer.write(".");
      writer.write("\n");
    }
    writer.write("\n");

    for (UFact f : facts)
      writer.write(NumberFormat.getInstance().format(f.getBaseConfidence()) + " " + getPredicateConstant(f) + "(C)\n");
    writer.write("\n");

    writer.flush();
    writer.close();
  }

  private static String normalize(String s) {
    if (s == null)
      return null;
    if (s.length() == 0)
      return s;
    if (s.startsWith("?")) {
      s = s.substring(1).toLowerCase();
      if (!Character.isLetter(s.substring(0, 1).toCharArray()[0]))
        s = "x" + s;
      s = s.replaceAll("\\W", "x");
      return s;
    }
    if (!Character.isLetter(s.substring(0, 1).toCharArray()[0]))
      s = "X" + s;
    s = s.replaceAll("\\W", "x");
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  private static String getPredicateConstant(UTriplet f) {
    StringBuffer s = new StringBuffer();
    s.append(f.getRelationName().toUpperCase() + "_" + normalize(f.getFirstArgumentName()) + "_" + normalize(f.getSecondArgumentName()));
    return s.toString();
  }

  private static String getMuxSquare(UFactSet compSet) {
    StringBuffer s = new StringBuffer();
    // System.out.println(compSet);
    if (compSet.size() < 2)
      return "";

    for (int i = 0; i < compSet.size(); i++) {
      UFact f1 = compSet.getFact(i);
      for (int j = i + 1; j < compSet.size(); j++) {
        UFact f2 = compSet.getFact(j);
        s.append("(");
        s.append("!" + f1.getRelationName() + "(" + normalize(f1.getFirstArgumentName()) + "," + normalize(f1.getSecondArgumentName()) + ")");
        s.append(" v ");
        s.append("!" + f2.getRelationName() + "(" + normalize(f2.getFirstArgumentName()) + "," + normalize(f2.getSecondArgumentName()) + ")");
        s.append(")");
        s.append(".\n");
      }
    }

    return s.toString();
  }

  private static String getMuxSquarePredicateConstant(UFactSet compSet) {
    StringBuffer s = new StringBuffer();
    // System.out.println(compSet);
    if (compSet.size() < 2)
      return "";

    for (int i = 0; i < compSet.size(); i++) {
      UFact f1 = compSet.getFact(i);
      for (int j = i + 1; j < compSet.size(); j++) {
        UFact f2 = compSet.getFact(j);
        s.append("(");
        s.append("!" + getPredicateConstant(f1) + "(C)");
        s.append(" v ");
        s.append("!" + getPredicateConstant(f2) + "(C)");
        s.append(")");
        s.append(".\n");
      }
    }

    return s.toString();
  }
}
