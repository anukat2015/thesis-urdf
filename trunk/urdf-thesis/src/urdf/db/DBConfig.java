package urdf.db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class is part of the YAGO converters (http://mpii.de/yago). It is licensed under the Creative Commons Attribution License
 * (http://creativecommons.org/licenses/by/3.0) by the YAGO team (http://mpii.de/yago).
 * 
 * Returns the database connection from yago.ini
 * 
 * @author Fabian M. Suchanek
 */
public class DBConfig {

	/** Holds the information that is necessary to connect to a database */
	public static class DatabaseParameters {
		public String system = null;
		public String host = null;
		public String port = null;
		public String user = null;
		public String password = null;
		public String inst = null;
		public String databaseName = null;
	}

	/** Returns the database parameters for an ini-File. Initializes from the ini-File, if necessary. */
	public static DatabaseParameters databaseParameters(String iniFile) throws IOException {
		return databaseParameters(new FileInputStream(iniFile));
	}

	public static DatabaseParameters databaseParameters(InputStream iniStream) throws IOException {
		DatabaseParameters p = new DatabaseParameters();
		String s;
		BufferedReader r = new BufferedReader(new InputStreamReader(iniStream));
		while ((s = r.readLine()) != null) {
			String[] param = s.split("\\=");
			if (param[0].trim().toLowerCase().equals("databasesystem"))
				p.system = param[1].trim();
			else if (param[0].trim().toLowerCase().equals("databasehost"))
				p.host = param[1].trim();
			else if (param[0].trim().toLowerCase().equals("databaseport"))
				p.port = param[1].trim();
			else if (param[0].trim().toLowerCase().equals("databaseuser"))
				p.user = param[1].trim();
			else if (param[0].trim().toLowerCase().equals("databasepassword"))
				p.password = param[1].trim();
			else if (param[0].trim().toLowerCase().equals("databasesid"))
				p.inst = param[1].trim();
			else if (param[0].trim().toLowerCase().equals("databasedatabase"))
				p.databaseName = param[1].trim();
		}
		return p;
	}
}
