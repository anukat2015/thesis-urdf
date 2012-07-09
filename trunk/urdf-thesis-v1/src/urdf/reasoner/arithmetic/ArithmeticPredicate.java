package urdf.reasoner.arithmetic;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import urdf.api.UArgument;
import urdf.api.URelation;

public class ArithmeticPredicate {

  public static double compareBindings(UArgument argument1, UArgument argument2, UArgument relation, int compareValue) {
    boolean condition = false;
    double similarity = -1.0;
    String arg1 = argument1.getName();
    if (arg1.startsWith("\""))
      arg1 = arg1.substring(1).trim();
    if (arg1.endsWith("\""))
      arg1 = arg1.substring(0, arg1.length() - 1).trim();
    String arg2 = argument2.getName();
    if (arg2.startsWith("\""))
      arg2 = arg2.substring(1).trim();
    if (arg2.endsWith("\""))
      arg2 = arg2.substring(0, arg2.length() - 1).trim();
    if (relation == URelation.NOTEQUALS)
      condition = !(arg1.equals(arg2));
    else if (relation == URelation.EQUALS)
      condition = arg1.equals(arg2);
    else if (relation == URelation.GREATER)
      condition = Double.parseDouble(arg1) > Double.parseDouble(arg2);
    else if (relation == URelation.LOWER)
      condition = Double.parseDouble(arg1) < Double.parseDouble(arg2);
    else if (relation == URelation.GREATEREQ)
      condition = Double.parseDouble(arg1) >= Double.parseDouble(arg2);
    else if (relation == URelation.LOWEREQ)
      condition = Double.parseDouble(arg1) <= Double.parseDouble(arg2);
    else if (relation == URelation.YEARBEFORE)
      condition = Double.parseDouble(parseYear(arg1)) < Double.parseDouble(parseYear(arg2));
    // else if (predicateType == URelation.DATEBEFORE) // yet to be supported
    else if (relation == URelation.DIFFERENCELT)
      condition = ((Double.parseDouble(arg1) - Double.parseDouble(arg2)) <= compareValue);
    else if (relation == URelation.ISWITHINHOURS) {
      SimpleDateFormat df = new SimpleDateFormat("DD.MM.yyyy HH:mm:ss");
      try {
        condition = (df.parse(arg1).getTime() >= df.parse(arg2).getTime())
            && (df.parse(arg1).getTime() - df.parse(arg2).getTime() <= compareValue * 1000 * 3600);
        similarity = 1 - ((df.parse(arg1).getTime() - df.parse(arg2).getTime()) / ((double) (compareValue * 1000 * 3600)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (relation == URelation.ISWITHINMINUTES) {
      SimpleDateFormat df = new SimpleDateFormat("DD.MM.yyyy HH:mm:ss");
      try {
        condition = (df.parse(arg1).getTime() >= df.parse(arg2).getTime()) && (df.parse(arg1).getTime() - df.parse(arg2).getTime() <= compareValue * 1000 * 60);
        similarity = 1 - ((df.parse(arg1).getTime() - df.parse(arg2).getTime()) / ((double) (compareValue * 1000 * 60)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (relation == URelation.SAMEDAY) {
      SimpleDateFormat df = new SimpleDateFormat("DD.MM.yyyy HH:mm:ss");
      try {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(df.parse(arg1));
        Calendar c2 = Calendar.getInstance();
        c2.setTime(df.parse(arg2));
        condition = (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) && (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR));
        similarity = 1 - ((Math.abs(df.parse(arg1).getTime() - df.parse(arg2).getTime())) / ((double) (24 * 1000 * 3600)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (!condition)
      return -1.0;
    else if (similarity < 0)
      return 1.0;
    else
      return similarity;
  }

  private static String parseYear(String s) {
    int idx1 = s.indexOf("-"), idx2 = s.indexOf("#");
    if (idx1 > -1 && (idx2 == -1 || idx1 < idx2))
      s = s.substring(0, idx1);
    else if (idx1 > -1 && idx2 > -1 && idx1 > idx2) {
      s = s.substring(0, idx1);
      s = s.replaceAll("\\#", "0");
    }
    return s;
  }
}
