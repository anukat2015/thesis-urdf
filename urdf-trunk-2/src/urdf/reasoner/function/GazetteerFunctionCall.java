package urdf.reasoner.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import urdf.api.UArgument;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.URelation;

public class GazetteerFunctionCall extends FunctionCall {

  // Singleton
  private static GazetteerFunctionCall instance;

  private GazetteerFunctionCall() throws Exception {
  }

  private PreparedStatement gazDataStmt1 = null, gazDataStmt2 = null, gazDataStmt3 = null, gazDataStmt4 = null;

  private void init(Connection connection) throws Exception {

    // This is for unique UFI's
    this.gazDataStmt1 = connection.prepareStatement("SELECT lat, lon FROM gazetteer WHERE UFI = ?");
    gazDataStmt2 = connection
        .prepareStatement("SELECT gaz1.LAT, gaz1.LON, gaz1.UFI, gaz2.LAT, gaz2.LON FROM gazetteer gaz1, gazetteer gaz2 WHERE gaz2.UFI = ? AND gaz1.LAT >= gaz2.LAT - (? / 69.1) AND gaz1.LAT <= gaz2.LAT + (? / 69.1) AND gaz1.LON >= gaz2.LON - (? / 69.1) AND gaz1.LON <= gaz2.LON + (? / 69.1)");

    // This is for full names; caution: they are not disambiguated nor explicitly linked to Yago entities!
    gazDataStmt3 = connection.prepareStatement("SELECT lat, lon FROM gazetteer WHERE FULL_NAME = ?");
    gazDataStmt4 = connection
        .prepareStatement("SELECT gaz1.LAT, gaz1.LON, gaz1.FULL_NAME, gaz2.LAT, gaz2.LON FROM gazetteer gaz1, gazetteer gaz2 WHERE gaz2.FULL_NAME = ? AND gaz1.LAT >= gaz2.LAT - (? / 69.1) AND gaz1.LAT <= gaz2.LAT + (? / 69.1) AND gaz1.LON >= gaz2.LON - (? / 69.1) AND gaz1.LON <= gaz2.LON + (? / 69.1)");
  }

  public static FunctionCall getInstance(Object param) throws Exception {
    if (instance == null) {
      instance = new GazetteerFunctionCall();
      instance.init((Connection) param);
    }
    return instance;
  }

  public UFactSet call(UArgument argument1, UArgument argument2, URelation relation, int compareValue) throws Exception {

    UFactSet groundedFacts = new UFactSet();

    if (relation == URelation.gaz_ISCLOSE) {

      if (argument1 != null && argument2 != null) {
        groundedFacts = new UFactSet();
        double d = 0.0;
        if ((d = compareGeoDistance1(argument1, argument2, compareValue)) > 0) {
          UFact fact = new UFact(relation, argument1, argument2, d);
          groundedFacts.add(fact);
        }
      } else if (argument1 != null || argument2 != null) {
        groundedFacts = getNearPlaces1(argument1, argument2, relation, compareValue);
      } else
        throw new Exception("Insufficient bindings for function call!");

    } else if (relation == URelation.ISCLOSE) {

      if (argument1 != null && argument2 != null) {
        groundedFacts = new UFactSet();
        double d = 0.0;
        if ((d = compareGeoDistance2(argument1, argument2, compareValue)) > 0) {
          UFact fact = new UFact(relation, argument1, argument2, d);
          groundedFacts.add(fact);
        }
      } else if (argument1 != null || argument2 != null) {
        groundedFacts = getNearPlaces2(argument1, argument2, relation, compareValue);
      } else
        throw new Exception("Insufficient bindings for function call!");
    }

    return groundedFacts;
  }

  private double compareGeoDistance1(UArgument argument1, UArgument argument2, int compareValue) {
    double lat1 = 0, lon1 = 0, lat2 = 0, lon2 = 0;
    try {
      gazDataStmt1.setString(1, argument1.getName());
      ResultSet rset = gazDataStmt1.executeQuery();
      if (rset.next()) {
        lat1 = rset.getDouble(1);
        lon1 = rset.getDouble(2);
      }
      gazDataStmt1.setString(1, argument2.getName());
      rset = gazDataStmt1.executeQuery();
      if (rset.next()) {
        lat2 = rset.getDouble(1);
        lon2 = rset.getDouble(2);
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    double d = getDistance(lat1, lon1, lat2, lon2);
    if (d > compareValue)
      return -1;
    return 1 - (d / compareValue);
  }

  private double compareGeoDistance2(UArgument argument1, UArgument argument2, int compareValue) {
    double lat1 = 0, lon1 = 0, lat2 = 0, lon2 = 0;
    try {
      gazDataStmt3.setString(1, argument1.getName());
      ResultSet rset = gazDataStmt3.executeQuery();
      if (rset.next()) {
        lat1 = rset.getDouble(1);
        lon1 = rset.getDouble(2);
      }
      gazDataStmt3.setString(1, argument2.getName());
      rset = gazDataStmt3.executeQuery();
      if (rset.next()) {
        lat2 = rset.getDouble(1);
        lon2 = rset.getDouble(2);
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    double d = getDistance(lat1, lon1, lat2, lon2);
    if (d > compareValue)
      return -1;
    return 1 - (d / compareValue);
  }

  private UFactSet getNearPlaces1(UArgument argument1, UArgument argument2, UArgument relation, int compareValue) throws Exception {
    UFactSet groundedFacts = new UFactSet();
    gazDataStmt2.setString(1, argument1 != null ? argument1.getName() : argument2.getName());
    gazDataStmt2.setInt(2, compareValue);
    gazDataStmt2.setInt(3, compareValue);
    gazDataStmt2.setInt(4, compareValue);
    gazDataStmt2.setInt(5, compareValue);
    ResultSet rset = gazDataStmt2.executeQuery();
    while (rset.next()) {
      double lat1 = rset.getDouble(1);
      double lon1 = rset.getDouble(2);
      double lat2 = rset.getDouble(4);
      double lon2 = rset.getDouble(5);
      double d = getDistance(lat1, lon1, lat2, lon2);
      if (d > compareValue)
        continue;
      groundedFacts.add(new UFact((URelation) relation, argument1 != null ? argument1 : argument2, new UArgument(rset.getString(3)), 1 - (d / compareValue)));
    }
    rset.close();
    return groundedFacts;
  }

  private UFactSet getNearPlaces2(UArgument argument1, UArgument argument2, UArgument relation, int compareValue) throws Exception {
    UFactSet groundedFacts = new UFactSet();
    gazDataStmt4.setString(1, argument1 != null ? argument1.getName() : argument2.getName());
    gazDataStmt4.setInt(2, compareValue);
    gazDataStmt4.setInt(3, compareValue);
    gazDataStmt4.setInt(4, compareValue);
    gazDataStmt4.setInt(5, compareValue);
    ResultSet rset = gazDataStmt4.executeQuery();
    while (rset.next()) {
      double lat1 = rset.getDouble(1);
      double lon1 = rset.getDouble(2);
      double lat2 = rset.getDouble(4);
      double lon2 = rset.getDouble(5);
      double d = getDistance(lat1, lon1, lat2, lon2);
      if (d > compareValue)
        continue;
      groundedFacts.add(new UFact((URelation) relation, argument1 != null ? argument1 : argument2, new UArgument(rset.getString(3)), 1 - (d / compareValue)));
    }
    rset.close();
    return groundedFacts;
  }

  private double getDistance(double lat1, double lon1, double lat2, double lon2) {
    double distance = 6378.7 * java.lang.Math.acos(java.lang.Math.sin(lat1 / 57.2958) * java.lang.Math.sin(lat2 / 57.2958) + java.lang.Math.cos(lat1 / 57.2958)
        * java.lang.Math.cos(lat2 / 57.2958) * java.lang.Math.cos(lon2 / 57.2958 - lon1 / 57.2958));
    return distance;
  }

  public void close() throws Exception {
    if (this.gazDataStmt1 != null)
      this.gazDataStmt1.close();
    if (this.gazDataStmt2 != null)
      this.gazDataStmt2.close();
    if (this.gazDataStmt3 != null)
      this.gazDataStmt3.close();
    if (this.gazDataStmt4 != null)
      this.gazDataStmt4.close();
    gazDataStmt1 = gazDataStmt2 = gazDataStmt3 = gazDataStmt4 = null;
  }
}
