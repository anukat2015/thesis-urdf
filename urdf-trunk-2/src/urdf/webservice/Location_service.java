package urdf.webservice;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import basics.Normalize;

public class Location_service {

  private static Connection connection1 = null;

  private static PreparedStatement pstmt1, pstmt2, pstmt3, pstmt4;

  private static void init() {
    try {
      //DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
      //connection1 = DriverManager.getConnection("jdbc:postgresql://infao5600:5432/yago2ned", "yago", "yago2itnyago");

      DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());
      connection1 = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=infao5501"
          + ")(PORT=1521)))(CONNECT_DATA=(SID=oracle)(server = dedicated)))", "yago2", "yago2");

      pstmt1 = connection1.prepareStatement("SELECT count(arg1) FROM facts WHERE relation = 'hasInternalWikipediaLinkTo' and arg1 = ?");
      pstmt2 = connection1.prepareStatement("SELECT n FROM inlink_stats WHERE target = ?");
      pstmt3 = connection1.prepareStatement("SELECT target, n FROM anchor_stats_nocase WHERE anchor = ? order by n desc");

      pstmt4 = connection1.prepareStatement("SELECT gaz1.arg2, gaz2.arg2, gaz3.arg2, gaz4.arg2 FROM " + "facts gaz1, facts gaz2, facts gaz3, facts gaz4"
          + " WHERE gaz1.arg1 = ? AND gaz1.relation = 'hasLatitude' AND gaz1.arg1 = gaz2.arg1 AND gaz2.relation = 'hasLongitude' "
          + " AND gaz3.arg1 = ? AND gaz3.relation = 'hasLatitude' AND gaz3.arg1 = gaz4.arg1 AND gaz4.relation = 'hasLongitude' ");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void close() {
    try {
      pstmt1.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      pstmt2.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      pstmt3.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      pstmt4.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      connection1.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    connection1 = null;
  }

  private static int getInlinks(String entity) {
    int n = 0;
    if (connection1 == null)
      init();
    try {
      pstmt2.setString(1, entity);
      ResultSet rset = pstmt2.executeQuery();
      if (rset.next())
        n = rset.getInt(1);
      rset.close();
    } catch (SQLException e) {
      e.printStackTrace();
      close();
    }
    return n;
  }

  private static double getDistance(String e1, String e2) throws Exception {
    pstmt4.setString(1, e1);
    pstmt4.setString(2, e2);
    ResultSet rset = pstmt4.executeQuery();
    double lat1 = 0;
    double lon1 = 0;
    double lat2 = 0;
    double lon2 = 0;
    if (rset.next()) {
      lat1 = Double.parseDouble(rset.getString(1).replaceAll("\"", ""));
      lon1 = Double.parseDouble(rset.getString(2).replaceAll("\"", ""));
      lat2 = Double.parseDouble(rset.getString(3).replaceAll("\"", ""));
      lon2 = Double.parseDouble(rset.getString(4).replaceAll("\"", ""));
    }
    rset.close();
    double distance = 6378.7 * java.lang.Math.acos(java.lang.Math.sin(lat1 / 57.2958) * java.lang.Math.sin(lat2 / 57.2958) + java.lang.Math.cos(lat1 / 57.2958)
        * java.lang.Math.cos(lat2 / 57.2958) * java.lang.Math.cos(lon2 / 57.2958 - lon1 / 57.2958));
    return distance;
  }

  public static Set<ComparableEntity> getInlinksForAnchor(String name) {
    if (connection1 == null)
      init();

    TreeSet<ComparableEntity> entities = new TreeSet<ComparableEntity>();

    String entity = Normalize.entity(name);
    int links = getInlinks(entity);
    //System.out.println(links);

    try {

      pstmt3.setString(1, "\"" + name.trim().toLowerCase() + "\"");
      ResultSet rset = pstmt3.executeQuery();
      boolean matched = false;
      while (rset.next()) {
        String e = rset.getString(1);
        int n = rset.getInt(2);
        if (links > 0 && entity.equals(e)) {
          entities.add(new ComparableEntity(entity, n + links));
          matched = true;
        } else {
          entities.add(new ComparableEntity(e, n));
        }
      }
      rset.close();

      if (!matched && links > 0) {
        entities.add(new ComparableEntity(entity, links));
        matched = true;
      }

    } catch (Exception e) {
      e.printStackTrace();
      close();
    }

    return entities;
  }

  public static String getDistancePairs(String name1, String name2) {
    String s = "";
    try {
      Set<ComparableEntity> s1 = Location_service.getInlinksForAnchor(name1);
      Set<ComparableEntity> s2 = Location_service.getInlinksForAnchor(name2);
      int i = 0;
      for (ComparableEntity e1 : s1) {
        int j = 0;
        for (ComparableEntity e2 : s2) {
          if (e1 == e2 || i > 3 || j > 3)
            continue;
          double distance = getDistance(e1.name, e2.name);
          if (distance > 0)
            s += e1.name + "\t" + e2.name + "\t" + distance + "\n";
          j++;
        }
        i++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return s;
  }

  public static void main(String[] args) throws Exception {
    System.out.println(getDistancePairs(args[0], args[1]));
  }

  private static class ComparableEntity implements Comparable<ComparableEntity> {

    protected String name;
    protected int weight;

    public ComparableEntity(String name, int weight) {
      this.name = name;
      this.weight = weight;
    }

    public int compareTo(ComparableEntity ce) {
      return Double.compare(ce.weight, this.weight);
    }
  }
}
