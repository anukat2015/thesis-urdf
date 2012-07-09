package urdf.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import urdf.db.DBConfig;
import urdf.db.DBConfig.DatabaseParameters;

public class N3Loader {

  public static void main(String[] args) throws Exception {

    // Initialize the connection and prepared statement
    DatabaseParameters p = DBConfig.databaseParameters(args[0]);
    Connection connection = null;

    if (p.system.toLowerCase().indexOf("postgres") >= 0) {

      DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
      connection = DriverManager.getConnection("jdbc:postgresql://" + p.host + ":" + p.port + (p.databaseName == null ? "" : "/" + p.databaseName), p.user,
          p.password);

    } else if (p.system.toLowerCase().indexOf("oracle") >= 0) {

      DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());
      connection = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + p.host
          + ")(PORT=1521)))(CONNECT_DATA=(SID=" + p.inst + ")(server = dedicated)))", p.user, p.password);
    }

    PreparedStatement pstmt = connection.prepareStatement("INSERT INTO FACTS (ID, RELATION, ARG1, ARG2, CONFIDENCE) VALUES (?,?,?,?,?)");
    // BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]), "UTF8"));

    int i = 0, j = 0;
    String line = null, s1, s2, s3;
    String[] spo;
    while ((line = br.readLine()) != null) {
      j++;
      try {
        spo = line.split(" |\\:");
        if ((!spo[0].startsWith("<") && !spo[0].startsWith("\"")) || spo[0].length() > 257
            || (!spo[1].equals("y") && !spo[1].equals("rdf") && !spo[1].equals("rdfs")) || spo[2].length() > 255
            || (!spo[3].startsWith("<") && !spo[3].startsWith("\"")) || spo[3].length() > 257) {
          System.out.println("SKIP: " + line + " => " + spo[0] + "|" + spo[1] + "|" + spo[2] + "|" + spo[3]);
          continue;
        }
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }
      try {
        try {
          s1 = URLDecoder.decode(spo[2], "UTF-8");
          s2 = URLDecoder.decode(spo[0].substring(1, spo[0].length() - 1), "UTF-8");
          s3 = URLDecoder.decode(spo[3].substring(1, spo[3].length() - 1), "UTF-8");
        } catch (IllegalArgumentException e) {
          //e.printStackTrace();
          System.err.println("IllegalArgumentException: " + line);
          continue;
        }
        pstmt.setInt(1, i);
        pstmt.setString(2, s1);
        pstmt.setString(3, s2);
        pstmt.setString(4, s3);
        pstmt.setDouble(5, 1.0);
        pstmt.addBatch();
        if (i % 10000 == 0) {
          System.out.println(i);
          pstmt.executeBatch();
          connection.commit();
          // break;
        }
        i++;
      } catch (Exception e) {
        e.printStackTrace();
        pstmt.clearBatch();
      }
    }
    pstmt.executeBatch();
    connection.commit();
    pstmt.close();
    connection.close();
    System.out.println(j + " LINES IN FILE, " + i + " LINES OK.");
  }
}
