package urdf.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import urdf.db.DBConfig;
import urdf.db.DBConfig.DatabaseParameters;

public class TSVLoader {

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

    PreparedStatement pstmt = connection.prepareStatement("INSERT INTO ANCHORS (ID, RELATION, ARG1, ARG2, CONFIDENCE) VALUES (?,?,?,?,?)");

    File file = new File(args[1]);
    File[] files;
    if (file.isDirectory())
      files = file.listFiles();
    else
      files = new File[] { file };

    for (int f = 0; f < files.length; f++) {

      System.out.println(files[f]);

      // BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(files[f]), "UTF8"));

      int i = 0, j = 0;
      String line = null, s1, s2, s3 = files[f].toString().substring(files[f].toString().lastIndexOf(System.getProperty("file.separator")) + 1,
          files[f].toString().lastIndexOf("."));
      String[] spo;
      while ((line = br.readLine()) != null) {
        j++;
        try {
          spo = line.split("\t");
          if (spo[0].length() > 255 || spo[1].length() > 255 || spo[2].length() > 255) {
            //System.out.println("SKIP: " + line + " => " + spo[0] + "|" + spo[1] + "|" + spo[2]);
            continue;
          }
        } catch (Exception e) {
          e.printStackTrace();
          continue;
        }
        try {
          try {
            if (spo[1].startsWith("#"))
              s1 = spo[1].substring(1);
            else if (!spo[1].startsWith("\""))
              s1 = URLDecoder.decode(spo[1], "UTF-8");
            else
              s1 = spo[1];
            if (spo[2].startsWith("#"))
              s2 = spo[2].substring(1);
            else if (!spo[2].startsWith("\""))
              s2 = URLDecoder.decode(spo[2], "UTF-8");
            else
              s2 = spo[2];
            //System.out.println(">" + s1 + "<\t>" + s2 + "<\t>" + s3 + "<");
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("IllegalArgumentException: " + line);
            continue;
          }
          pstmt.setInt(1, Integer.valueOf(spo[0].substring(1)));
          pstmt.setString(2, s3);
          //pstmt.setString(3, s1);
          pstmt.setInt(3, Integer.valueOf(s1));
          pstmt.setString(4, s2);
          pstmt.setDouble(5, 1.0);
          pstmt.addBatch();
          if (i > 0 && i % 10000 == 0) {
            System.out.println(s3 + "\t" + i);
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
      System.out.println(j + " LINES IN FILE, " + i + " LINES OK.");
    }
    pstmt.close();
    connection.close();
  }
}
