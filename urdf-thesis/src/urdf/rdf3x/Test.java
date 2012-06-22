package urdf.rdf3x;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Properties;




public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Driver 		drvr = new Driver();
		Properties 	info = new Properties();
		info.put("DIR", "/home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
		try {
			InputStream is = drvr.dumpDatabase("rdf3x://newDB", info);
			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line=bf.readLine())!=null) {
				System.out.println(line);
			}
					
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
