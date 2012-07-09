package urdf.webservice;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

import basics.Normalize;

public class YAGO_service {

  private static Connection connection = null;

  private static PreparedStatement pstmt1, pstmt2, pstmt3;

  private static void init() {
    try {
      DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
      connection = DriverManager.getConnection("jdbc:postgresql://infao5600:5432/yago2ned", "yago", "yago2itnyago");
      pstmt1 = connection.prepareStatement("SELECT count(arg1) FROM facts WHERE relation = 'hasInternalWikipediaLinkTo' and arg1 = ?");
      pstmt2 = connection.prepareStatement("SELECT n FROM inlink_stats WHERE target = ?");
      pstmt3 = connection.prepareStatement("SELECT target, n FROM anchor_stats WHERE anchor = ? order by n desc");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void close() {
    try {
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    connection = null;
  }

  private static int getInlinks(String name) {
    int n = 0;
    if (connection == null)
      init();
    try {
      pstmt2.setString(1, Normalize.entity(name.trim()));
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

  /*
   * private static int getOutlinks(String name) { int n = 0; if (connection == null) init(); try { pstmt1.setString(1, Normalize.entity(name.trim()));
   * ResultSet rset = pstmt1.executeQuery(); if (rset.next()) n = rset.getInt(1); rset.close(); } catch (SQLException e) { e.printStackTrace(); close(); }
   * return n; }
   */

  public static String getInlinksForName(String name) {
    String s = "<" + name + "> ";
    if (connection == null)
      init();

    try {

      pstmt2.setString(1, Normalize.entity(name.trim()));
      ResultSet rset = pstmt2.executeQuery();
      if (rset.next())
        s += rset.getString(1);
      else
        s += "0";
      rset.close();

      // s = URLEncoder.encode(s, "UTF-8");

    } catch (Exception e) {
      e.printStackTrace();
      s = e.getMessage();
      close();
    }

    return s;
  }

  public static String getOutlinksForName(String name) {
    String s = "<" + name + "> ";
    if (connection == null)
      init();

    try {

      pstmt1.setString(1, Normalize.entity(name.trim()));
      ResultSet rset = pstmt1.executeQuery();
      if (rset.next())
        s += rset.getString(1);
      else
        s += "0";
      rset.close();

      // s = URLEncoder.encode(s, "UTF-8");

    } catch (Exception e) {
      e.printStackTrace();
      s = e.getMessage();
      close();
    }

    return s;
  }

  public static String getInlinksForAnchor(String name) {
    String s = "";
    if (connection == null)
      init();

    int links = getInlinks(name);// + getOutlinks(name);
    TreeSet<ComparableEntity> entities = new TreeSet<ComparableEntity>();
    System.out.println(links);
    try {

      pstmt3.setString(1, "\"" + name.trim().toLowerCase() + "\"");
      ResultSet rset = pstmt3.executeQuery();
      boolean matched = false;
      while (rset.next()) {
        String entity = Normalize.unEntity(rset.getString(1));
        int n = rset.getInt(2);
        if (links > 0 && name.equals(entity)) {
          entities.add(new ComparableEntity(entity, n + links));
          matched = true;
        } else {
          entities.add(new ComparableEntity(entity, n));
        }
      }
      rset.close();

      if (!matched && links > 0) {
        entities.add(new ComparableEntity(Normalize.unEntity(name), links));
        matched = true;
      }

      int i = 0;
      for (ComparableEntity entity : entities) {
        if (i > 0)
          s += "\n";
        // s += i;
        s += "<" + entity.name + "> " + entity.weight;
        if (++i >= 25)
          break;
      }

      // s = URLEncoder.encode(s, "UTF-8");

    } catch (Exception e) {
      e.printStackTrace();
      s = e.getMessage();
      close();
    }

    return s;
  }

  public static void main(String[] args) {
    System.out.println(YAGO_service.getInlinksForAnchor("Paris"));
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
