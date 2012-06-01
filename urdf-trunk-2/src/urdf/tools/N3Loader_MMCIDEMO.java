package urdf.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import urdf.db.DBConfig;
import urdf.db.DBConfig.DatabaseParameters;

public class N3Loader_MMCIDEMO {

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
          + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + p.inst + ")(server = dedicated)))", p.user, p.password);
    }

    PreparedStatement pstmt = connection.prepareStatement("INSERT INTO FACTS (ID, ARG1, RELATION, ARG2, CONFIDENCE) VALUES (?,?,?,?,?)");

    Pattern pattern = Pattern.compile("(\\<\\S*\\>)|(x\\:\\S*\\s)|(\\\".*\\\")|(\\s\\w+\\d*\\s)");

    String line = null;
    String[] spo = new String[4];

    int i = 1;
    for (int a = 1; a < args.length; a++) {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[a]), "UTF8"));

      while ((line = br.readLine()) != null) {
        System.out.println("\nLINE " + i + ": " + line);

        Matcher matcher = pattern.matcher(line);
        int j = 0;
        while (j < 4 && matcher.find()) {
          spo[j] = matcher.group();
          System.out.println(j + " >" + spo[j] + "<");
          j++;
        }
        if (j < 3)
          continue;

        try {

          pstmt.setInt(1, i);
          pstmt.setString(2, spo[0].substring(1, spo[0].length() - 1));
          pstmt.setString(3, spo[1].startsWith("x:") ? spo[1].substring(2, spo[1].length() - 1) : spo[1].substring(1, spo[1].length() - 1));
          pstmt.setString(4, spo[2].startsWith("\"") ? spo[2] : spo[2].substring(1, spo[2].length() - 1));
          pstmt.setDouble(5, j == 4 ? Double.parseDouble(spo[3].substring(1, spo[3].length() - 1)) : 1.0);
          pstmt.addBatch();

          if (i++ % 1000 == 0) {
            System.out.println(i - 1);
            pstmt.executeBatch();
            connection.commit();
          }

        } catch (Exception e) {
          pstmt.clearBatch();
          e.printStackTrace();
        }
      }
      br.close();
    }

    pstmt.executeBatch();
    connection.commit();
    pstmt.close();
    connection.close();
  }
}
