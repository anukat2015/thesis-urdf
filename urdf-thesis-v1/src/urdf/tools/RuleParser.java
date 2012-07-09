package urdf.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import urdf.api.UHardRule;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.USoftRule;

public class RuleParser {

  private static final String quotedWords = "\\s*\\\".*\\\"\\s*";
  private static final String wordsNumbers = "\\w+\\d*";
  private static final String URL = "(http://\\w+\\.\\w+){0,1}(/\\w+)*/" + wordsNumbers + "\\s*";// arguments like http://dbpedia.org/property/predecessor is
                                                                                                 // possible
  private static final String id = "\\s*\\?{0,2}(" + URL + "|" + "(" + wordsNumbers + "\\s*))";
  private static final String C = "\\s*(" + quotedWords + "|" + id + ")\\s*";
  private static final String triplet = id + "\\(" + C + "\\," + C + "(\\,\\s*\\d+\\s*){0,1}\\)";

  // private static final String triplet =
  // "\\s*\\?{0,2}\\w+\\d*\\s*\\(\\s*((\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,2}\\w+\\d*\\s*))\\s*\\,\\s*((\\s*\\\".*\\\"\\s*)|(\\s*\\?{0,2}\\w+\\d*\\s*))\\s*(\\,\\s*\\d+\\s*){0,1}\\)";
  private static final String arg = "(" + quotedWords + ")|(" + id + ")"; // only quoted literals or strict alphanumeric arguments!
  private static final String weight = "\\[\\d*(\\.\\d*){0,1}\\]";
  private static final String rule = "(\\s*(" + triplet + ")\\s*\\;\\s*)*(" + triplet + ")\\s*\\;{0,1}\\s*";

  private static final Pattern triplet_pattern = Pattern.compile(triplet);
  private static final Pattern id_pattern = Pattern.compile(id);
  private static final Pattern arg_pattern = Pattern.compile(arg);
  private static final Pattern weight_pattern = Pattern.compile(weight);

  private static final Pattern query_pattern = Pattern.compile("(" + id + "\\:){0,1}" + rule);
  private static final Pattern hardrule_pattern = Pattern.compile("(" + id + "\\:){0,1}" + rule);
  private static final Pattern softrule_pattern = Pattern.compile("(" + id + "\\:){0,1}" + triplet + "\\s*\\<\\=\\s*(" + rule + ")\\s*(" + weight
      + "){0,1}\\s*");

  public static UQuery parseQuery(String string) throws Exception {
    ArrayList<ULiteral> literals = new ArrayList<ULiteral>();
    Matcher matcher0 = query_pattern.matcher(string);
    if (!matcher0.matches())
      throw new Exception("MALFORMED QUERY: " + string);
    // int i = 0;
    Matcher matcher1 = triplet_pattern.matcher(string);
    while (matcher1.find()) {
      String literal = matcher1.group(), rel = null, arg1 = null, arg2 = null, arg3 = "0";
      // System.out.println("____" + literal);
      Matcher matcher2 = arg_pattern.matcher(literal);
      if (matcher2.find())
        rel = matcher2.group().trim();
      else
        throw new Exception("MISSING RELATION: " + literal);
      if (matcher2.find())
        arg1 = matcher2.group().trim().replaceAll("\"", "");
      else
        throw new Exception("MISSING FIRST ARGUMENT: " + literal);
      if (matcher2.find())
        arg2 = matcher2.group().trim().replaceAll("\"", "");
      else
        throw new Exception("MISSING SECOND ARGUMENT!" + literal);
      if (matcher2.find())
        arg3 = matcher2.group().trim().replaceAll("\"", "");
      try {
        literals.add(new ULiteral(URelation.valueOf(rel), arg1, arg2, Integer.parseInt(arg3)));
      } catch (Exception e) {
        e.printStackTrace();
        throw new Exception("MALFORMED QUERY ATOM: " + literal);
      }
      // i++;
    }
    String id = null;
    Matcher matcher3 = id_pattern.matcher(string);
    if (matcher3.find())
      id = matcher3.group().trim();
    return new UQuery(id, literals);
  }

  public static UHardRule parseHardRule(String string) throws Exception {
    ArrayList<ULiteral> literals = new ArrayList<ULiteral>();
    Matcher matcher0 = hardrule_pattern.matcher(string);
    if (!matcher0.matches())
      throw new Exception("MALFORMED HARDRULE: " + string);
    Matcher matcher1 = triplet_pattern.matcher(string);
    while (matcher1.find()) {
      String literal = matcher1.group(), rel = null, arg1 = null, arg2 = null, arg3 = "0";
      Matcher matcher2 = arg_pattern.matcher(literal);
      if (matcher2.find())
        rel = matcher2.group().trim();
      else
        throw new Exception("MISSING RELATION: " + literal);
      if (matcher2.find())
        arg1 = matcher2.group().trim().replaceAll("\"", "");
      else
        throw new Exception("MISSING FIRST ARGUMENT: " + literal);
      if (matcher2.find())
        arg2 = matcher2.group().trim().replaceAll("\"", "");
      else
        throw new Exception("MISSING SECOND ARGUMENT!" + literal);
      if (matcher2.find())
        arg3 = matcher2.group().trim().replaceAll("\"", "");
      try {
        literals.add(new ULiteral(URelation.valueOf(rel), arg1, arg2, Integer.parseInt(arg3)));
      } catch (Exception e) {
        e.printStackTrace();
        throw new Exception("MALFORMED LITERAL: " + literal);
      }
    }
    String id = null;
    Matcher matcher3 = id_pattern.matcher(string);
    if (matcher3.find())
      id = matcher3.group().trim();
    return new UHardRule(id, literals);
  }

  public static USoftRule parseSoftRule(String string) throws Exception {
    ULiteral head = null;
    ArrayList<ULiteral> body = new ArrayList<ULiteral>();
    Matcher matcher0 = softrule_pattern.matcher(string);
    if (!matcher0.matches())
      throw new Exception("MALFORMED SOFTRULE: " + string);
    int i = 0;
    Matcher matcher1 = triplet_pattern.matcher(string);
    while (matcher1.find()) {
      String literal = matcher1.group(), rel = null, arg1 = null, arg2 = null, arg3 = "0";
      Matcher matcher2 = arg_pattern.matcher(literal);
      if (matcher2.find())
        rel = matcher2.group().trim();
      else
        throw new Exception("MISSING RELATION: " + literal);
      if (matcher2.find())
        arg1 = matcher2.group().trim().replaceAll("\"", "");
      else
        throw new Exception("MISSING FIRST ARGUMENT: " + literal);
      if (matcher2.find())
        arg2 = matcher2.group().trim().replaceAll("\"", "");
      else
        throw new Exception("MISSING SECOND ARGUMENT!" + literal);
      if (matcher2.find())
        arg3 = matcher2.group().trim().replaceAll("\"", "");
      try {
        ULiteral uLiteral = new ULiteral(URelation.valueOf(rel), arg1, arg2, Integer.parseInt(arg3));
        if (i == 0)
          head = uLiteral;
        else
          body.add(uLiteral);
      } catch (Exception e) {
        e.printStackTrace();
        throw new Exception("MALFORMED LITERAL: " + literal);
      }
      i++;
    }
    String id = null;
    Matcher matcher3 = id_pattern.matcher(string);
    if (matcher3.find())
      id = matcher3.group().trim();
    double weight = 1.0;
    Matcher matcher4 = weight_pattern.matcher(string);
    if (matcher4.find()) {
      String w = matcher4.group().trim();
      weight = Double.parseDouble(w.substring(1, w.length() - 1));
    }

    return new USoftRule(id, head, weight, body);
  }

  public static String printQuery(UQuery q) {
    StringBuffer b = new StringBuffer();
    b.append(q.getName());
    b.append(":");
    for (ULiteral l : q.getLiterals()) {
      b.append(l.toString());
      b.append(";");
    }
    return b.toString();
  }

  public static String printHardRule(UHardRule r) {
    StringBuffer b = new StringBuffer();
    b.append(r.getName());
    b.append(":");
    for (ULiteral l : r.getLiterals()) {
      b.append(l.toString());
      b.append(";");
    }
    return b.toString();
  }

  public static String printSoftRule(USoftRule s) {
    StringBuffer b = new StringBuffer();
    b.append(s.getName());
    b.append(":");
    b.append(s.getHead().toString());
    b.append("<=");
    for (ULiteral l : s.getLiterals()) {
      b.append(l.toString());
      b.append(";");
    }
    b.append("[");
    b.append(s.getWeight());
    b.append("]");
    return b.toString();
  }

  public static void parseAll(InputStream inStream, List<USoftRule> softRules, List<UHardRule> hardRules, List<UQuery> queries) throws Exception {
    BufferedReader r = new BufferedReader(new InputStreamReader(inStream));
    String s;
    while ((s = r.readLine()) != null) {
      s = s.trim();
      if (s.startsWith("S"))
        softRules.add(parseSoftRule(s));
      else if (s.startsWith("H"))
        hardRules.add(parseHardRule(s));
      if (s.startsWith("Q"))
        queries.add(parseQuery(s));

    }
    r.close();
  }

  public static void scanDir(File file, List<USoftRule> softRules, List<UHardRule> hardRules, List<UQuery> queries) throws Exception {
    if (file.isFile() && file.toString().toLowerCase().endsWith(".txt"))
      parseAll(new FileInputStream(file), softRules, hardRules, queries);
    else if (file.isDirectory()) {
      File[] list = file.listFiles();
      for (int i = 0; list != null && i < list.length; i++)
        scanDir(list[i], softRules, hardRules, queries);
    }
  }
}
