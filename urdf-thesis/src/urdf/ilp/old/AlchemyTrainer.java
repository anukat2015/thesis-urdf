package urdf.ilp.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import basic.configuration.DBConfig;
import basic.configuration.DBConfig.DatabaseParameters;

import urdf.api.UHardRule;
import urdf.api.ULiteral;
import urdf.api.UQuery;
import urdf.api.URelation;
import urdf.api.USoftRule;

// it assumes that I have created in the DB, table basicX
// which hold a sample of the training data according to the soft rules learned.
// for an example see separate sql script

public class AlchemyTrainer 
{

	
	
	private Connection conn;
	private Statement stmt;
	
	static ArrayList<USoftRule> softRules=new ArrayList<USoftRule>();
	ArrayList<UHardRule> hardRules=new ArrayList<UHardRule>();
	ArrayList<UQuery> queries=new ArrayList<UQuery>();
	

	
	ArrayList<String> constants = new ArrayList<String>();
	
	public AlchemyTrainer(String iniFile,String relation, int caseNumber,int conf, boolean forTrain) throws Exception
	{
		try 
		{			
			initializeConnection(iniFile); //initialize the connection					
		} 
		catch (ClassNotFoundException e) {e.printStackTrace();}	
		catch (SQLException e) {e.printStackTrace();}
		
		//prepare the hard rules
		hardRules.add(new UHardRule("H1", new ULiteral(URelation.BORNIN, "?X", "??Y")));
		hardRules.add(new UHardRule("H2", new ULiteral(URelation.DIEDIN, "?X", "??Y")));	
		
		createDBFile(relation, caseNumber,forTrain,conf);
		createMLNFile(relation, caseNumber, forTrain,conf);
		
		
		
		
	}
	// caseNumber=> 0: only specialization: 1: no Specialization 2: Specialization + Types 3: Specialization +Types+Constants
	private void createMLNFile(String relation, int caseNumber, boolean forTrain, int conf) throws IOException, SQLException
	{
		BufferedWriter writer;
		BufferedReader reader;
		String sql, person,location,category, uni,tbl;
		ResultSet rs;
		
		if (forTrain)
		{
			writer = new BufferedWriter(new FileWriter(relation+"RulesForTrain"+conf+""+caseNumber+".mln"));
			reader=new BufferedReader(new FileReader(relation+"Rules.txt"));
			//tbl="basic"+conf+"Train"+caseNumber;
			tbl="tbl"+caseNumber+""+conf;
		}
		else
		{
			writer = new BufferedWriter(new FileWriter(relation+"RulesForTest"+conf+""+caseNumber+".mln"));
			reader=new BufferedReader(new FileReader(relation+"TrainedRules.txt"));
			//tbl="basic"+conf+"Test"+caseNumber;
			tbl="tbl"+caseNumber+""+conf;
		}
		
		
		System.out.println("Writing constants");
		
		// write constants of type person
		sql = "SELECT  distinct arg1 FROM "+tbl+" WHERE relation NOT IN ('locatedIn','hasCapital') UNION SELECT  distinct arg2 FROM "+tbl+" WHERE relation IN ('hasAcademicAdvisor','hasChild', 'hasPredecessor', 'isMarriedTo', 'hasSuccessor' )";
		
		rs=stmt.executeQuery(sql);
		
		writer.write("person={ ");
		int i=0;
		
		while (rs.next())
		{
			
			person=repairString(rs.getString(1));
			if (i!=0)
			{
				writer.write(", ");
			}
			i++;
			writer.write(person);			
			if ((i%21)==0)
			{
				writer.write("\n");
			}
		}
		writer.write(" }\n \n");
		rs.close();
		
/*		// write constants of type location
		sql = "SELECT  distinct arg1 FROM "+tbl+" WHERE relation IN ('locatedIn','hasCapital') UNION SELECT distinct arg2 FROM "+tbl+" WHERE relation IN ('bornIn','diedIn', 'locatedIn', 'politicianOf', 'isLeaderOf','isCitizenOf','livesIn' )";
		
		rs=stmt.executeQuery(sql);
		
		writer.write("location={ ");
		i=0;
		
		while (rs.next())
		{
			
			location=repairString(rs.getString(1));
			if (i!=0)
			{
				writer.write(", ");
			}
			i++;			
			writer.write(location);			
			if ((i%21)==0)
			{
				writer.write("\n");
			}
		}
		writer.write(" }\n \n");
		
		rs.close();*/
		
/*		if (conf<80)
		{
			// write constants of type institution
			sql = "SELECT distinct arg2 FROM "+tbl+" WHERE relation IN ('graduatedFrom', 'worksAt' )";
			
			rs=stmt.executeQuery(sql);
			
			writer.write("uni={ ");
			i=0;
			
			while (rs.next())
			{
				
				uni=repairString(rs.getString(1));
				if (i!=0)
				{
					writer.write(", ");
				}
				
				i++;
				writer.write(uni);			
				if ((i%2)==0)
				{
					writer.write("\n");
				}			

			}
			writer.write(" }\n \n");
		}*/
		
		// write constants of type category
		if (caseNumber==2)
		{
			rs.close();
			
			sql = "SELECT distinct arg2 FROM "+tbl+" WHERE relation='type'";
			
			rs=stmt.executeQuery(sql);
			
			writer.write("category={ ");
			i=0;
			
			while (rs.next())
			{
				
				category=repairString(rs.getString(1));
				if (i!=0)
				{
					writer.write(", ");
				}
				
				i++;
				writer.write(category);			
				if ((i%20)==0)
				{
					writer.write("\n");
				}			

			}
			writer.write(" }\n \n");
		}
		
		
		System.out.println("Writing predicate declaration");
		// write the predicate declaration
		
		if (caseNumber>=2)
		{
			writer.write("type(person, category) \n");
		}
		if (conf<80)
		{
//			writer.write("worksAt(person, uni) \n");
//			writer.write("graduatedFrom(person, uni) \n");
		}

		
//		writer.write("bornIn(person, location) \n");
		
		writer.write("hasAcademicAdvisor(person, person) \n");
//		writer.write("politicianOf(person, location) \n");
		writer.write("hasChild(person, person) \n");
		
		writer.write("hasPredecessor(person, person) \n");
		writer.write("isMarriedTo(person, person) \n");
		writer.write("hasSuccessor(person, person) \n");
//		writer.write("isLeaderOf(person, location) \n");
//		writer.write("diedIn(person, location) \n");
//		writer.write("hasCapital(location, location) \n");
//		writer.write("isCitizenOf(person, location) \n");
//		writer.write("livesIn(person, location) \n");
//		writer.write("locatedIn(location, location) \n \n");
		
		writer.write("\n");
		
		System.out.println("Writing the rules");
		// Now write the rules
		String read="";		
		read=reader.readLine();		
		while(read!=null)
		{
			writer.write(read+" \n \n");
			
			read=reader.readLine();
		}

		writer.flush();
		writer.close();	
	
	}
	
	
	
	private static String repairString(String s)
	{
		StringBuffer sb = new StringBuffer();
		for(int x = 0 ; x < s.length() ; x++) 
		{
			char c = s.charAt(x);
			if(!((c>=65 && c<=90)||(c>=97 && c<=122)))
			{
				//Here you can append anything, or you can simply append nothing as I did.
				// sb.append("&#"); 
				// sb.append((int) c);
				sb.append("");
			}
			else 
			{
				sb.append(c);
			}
		}
		return "A"+sb.toString();
	}
	private void createDBFile(String relation, int caseNumber, boolean forTrain, int conf) throws IOException, SQLException
	{
		BufferedWriter writer;
		String sql, relation1, arg1, arg2,tbl;
		
		if (forTrain)
		{
			writer = new BufferedWriter(new FileWriter(relation+"Train"+conf+""+caseNumber+".db"));
			//tbl="basic"+conf+"Train"+caseNumber;
			tbl="tbl"+caseNumber+""+conf;
		}
		else
		{
			writer = new BufferedWriter(new FileWriter(relation+"Test"+conf+""+caseNumber+".db"));
			//tbl="basic"+conf+"Test"+caseNumber;
			tbl="tbl"+caseNumber+""+conf;
		}
		
		
		
		sql="SELECT distinct relation, arg1, arg2 FROM "+tbl;
		
		ResultSet rs=stmt.executeQuery(sql);
		
		
		
		while (rs.next())
		{
			relation1=rs.getString(1);
			arg1=repairString(rs.getString(2));
			arg2=repairString(rs.getString(3));
			
			writer.write(relation1+"("+arg1+", "+arg2+") \n");
		}
		
		rs.close();
		writer.flush();
		writer.close();		
		
	}
	
 	private void initializeConnection(String iniFile) throws Exception
	{ 
	    DatabaseParameters p = DBConfig.databaseParameters(iniFile);

	    if (p.system.toLowerCase().indexOf("postgres") >= 0) 
	    {
	    
	      DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());

	      this.conn = DriverManager.getConnection("jdbc:postgresql://" + p.host + ":" + p.port + (p.database == null ? "" : "/" + p.database), p.user,
	          p.password);

	    } 
	    else if (p.system.toLowerCase().indexOf("oracle") >= 0) 
	    {
	    	
	      DriverManager.registerDriver((Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance());

	      this.conn = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + p.host
	          + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + p.inst + ")(server = dedicated)))", p.user, p.password);

	    } 
	    else
	      throw new Exception("UNKNOWN DB DRIVER!");
	    this.stmt = conn.createStatement();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try 
		{
			boolean forTrain=false;
			int caseNumber=3;
			int conf=50;
			
			AlchemyTrainer at=new AlchemyTrainer(args[0],"hasChild",caseNumber,conf,forTrain);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
