package urdf.rdf3x;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

// RDF-3X
// (c) 2009 Thomas Neumann. Web site: http://www.mpi-inf.mpg.de/~neumann/rdf3x
//
// This work is licensed under the Creative Commons
// Attribution-Noncommercial-Share Alike 3.0 Unported License. To view a copy
// of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
// or send a letter to Creative Commons, 171 Second Street, Suite 300,
// San Francisco, California, 94105, USA.

public final class Driver implements java.sql.Driver
{
   // The default process
   private static final String process = "rdf3xembedded";

	
   private static boolean registered = false;
   {
      if (!registered) {
         registered=true;
         try {
            java.sql.DriverManager.registerDriver(new Driver());
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }
   }
   // Does the URL look reasonable?
   public boolean acceptsURL(String url)
   {
      if (!url.startsWith("rdf3x://"))
         return false;
      return (new File(url.substring(8))).isFile();
   }

   // Open a connection
   private static java.sql.Connection buildConnection(String process,String fileName, Properties info) throws SQLException
   {
      // Start the process
      Connection c;
      try {
    	  ProcessBuilder pb = new ProcessBuilder(process,fileName);  	  
    	  if (info.containsKey("DIR")) {
    		  pb.directory(new File((String) info.get("DIR")));  
    	  }
    	  
          c=new Connection(pb.start());
      } catch (java.io.IOException e) {
         return null;
      }

      // Read the server greeting
      String greeting=c.readLine();
      if (greeting.equals("RDF-3X protocol 1")) {
         // Everything ok
         return c;
      }
      if (greeting.startsWith("RDF-3X protocol "))
         throw new SQLException("incompatible RDF-3X version");
      throw new SQLException("unable to open database: "+greeting);
   }

	public static Connection connect(String propsFile) throws SQLException, FileNotFoundException, IOException{ 
 		Properties props = new Properties();
 		props.load(new FileInputStream(new File(propsFile)));
 		Driver drvr = new Driver();
 		String db = (String) props.get("Database");
 		Connection conn = (Connection) drvr.connect(db, props);
 		return conn;
	}
   
   // Try to open a connection
   public java.sql.Connection connect(String url,Properties info) throws SQLException
   {
      // Check the URL
      if (!url.startsWith("rdf3x://"))
         return null;
      String fileName=url.substring(8);

   	  if (info.containsKey("DIR")) {
		 fileName = info.get("DIR") + "/" + fileName;
	  }
      
      // Safety check to locate the database
      if (!((new File(fileName)).isFile()))
         throw new SQLException("database file "+fileName+" not found");

      // Now try to connect
      java.sql.Connection c;
      if (info.containsKey(process)) {
         c=buildConnection((String)info.get(process),fileName, info);
         if (c!=null) return c;
      }
      c=buildConnection(process, fileName, info);
      if (c!=null) return c;
      c=buildConnection("."+File.separator+process, fileName, info);
      if (c!=null) return c;

      throw new SQLException("unable to start "+process+", check the PATH");
   }
   
	public InputStream dumpDatabase(String url, Properties info) throws SQLException, IOException {
      // Check the URL
      if (!url.startsWith("rdf3x://"))
         return null;
      
      String fileName=url.substring(8);

   	  if (info.containsKey("DIR")) 
		 fileName = info.get("DIR") + "/" + fileName;
      
      // Safety check to locate the database
      if (!((new File(fileName)).isFile()))
         throw new SQLException("database file "+fileName+" not found");

  	  ProcessBuilder pb = new ProcessBuilder("rdf3xdump",fileName);  	  
  	  if (info.containsKey("DIR")) {
  		  String dir = (String) info.get("DIR");
  		  pb.directory(new File(dir));  
  		 // pb.environment().put("PATH", dir);
  	  }
  	  Process p = pb.start();
  	  return p.getInputStream();
	}

   /// Driver version
   public int getMajorVersion() { return 0; }
   /// Driver version
   public int getMinorVersion() { return 4; }
   /// Property info
   public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new java.sql.DriverPropertyInfo[0]; }
   // JDBC compliant?
   public boolean jdbcCompliant() { return false; }

   /// Wrapper?
   public boolean isWrapperFor(Class<?> iface) { return false; }
   /// Unwrap
   public <T> T	unwrap(Class<T> iface) throws SQLException { throw new SQLException(); }
}
