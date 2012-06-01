package urdf.ilp;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import basic.configuration.DBConfig;
import basic.configuration.DBConfig.DatabaseParameters;

public class alephExp 
{
	String target;
	String[] body;
	private Connection conn;
	private Statement stmt;	
		
	public alephExp(String iniFile,String target,String[] body) throws Exception
	{
		try 
		{			
			this.target=target;
			this.body=body;
			initializeConnection(iniFile); //initialize the connection	
			this.stmt = conn.createStatement();				
		} 
		catch (ClassNotFoundException e) {e.printStackTrace();}	
		catch (SQLException e) {e.printStackTrace();}
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
	    
	}

  	public void predicateDeclarations(RelationsInfo info) throws IOException
  	{
  		String name,str, mode;  	 		
   	
  		FileWriter fstream = new FileWriter("modes.dat");	
  		BufferedWriter out = new BufferedWriter(fstream);
  		
  		out.write("% mode declarations \n");
  		
  		for (int i=0;i<body.length;i++)
  		{  		
  			name=body[i];   			
  			//str=name+"("+info.getRelationFromRelations(name).getDomain().getName()+", "+info.getRelationFromRelations(name).getRange().getName()+")";
  	  		mode=":-modeb(*, "+name+"(+"+info.getRelationFromRelations(name).getDomain().getName()+", +"+info.getRelationFromRelations(name).getRange().getName()+")).";  	  			
  	  		out.write(mode+"\n");  			
  		}
  		out.close();  		
  	}
  	public void determinations() throws IOException
  	{  	
  		String det,name;  		 		
  		FileWriter fstream = new FileWriter(target+".dat");	
  		BufferedWriter out = new BufferedWriter(fstream);
  		
		out.write("% determinations \n");
  		
		for (int i=0;i<body.length;i++)
  		{
  			name=body[i]; 
  			det=":-determination("+target+"/2, "+name+"/2).";
  	  		out.write(det+"\n"); 	  			
  		}  		
  		out.close();  		
  	}
  	

   	public void createBackgroundKnowledge() throws Exception
  	{ 	
  		ResultSet rs;
  		String rel,arg1,arg2, goodRels="(";
  		
  		rs=stmt.executeQuery("SELECT 1 FROM user_objects WHERE object_type = 'TABLE' AND object_name='ORDEREDFACTS'");

  		
  		for (int i=0;i<body.length;i++)
  		{
  			goodRels+="'"+body[i]+"'"+", ";
  		}
  		goodRels=goodRels.substring(0, goodRels.length()-2)+")";
  		
  		if (!rs.next()) // if it doesn't exist
  		{
  	  		// create the ordered table:
  	  		String tmp="CREATE TABLE orderedFacts AS (SELECT rownum r, relation, arg1,arg2 FROM (SELECT relation, arg1, arg2 FROM facts WHERE relation IN "+goodRels+" ORDER BY relation ASC) f)";
  			System.out.println(tmp); 			
  	  		
  	  		stmt.execute(tmp);
  	  		// create the index on rownum
  	  		String ind="CREATE INDEX ind_orderedFacts_r ON orderedFacts(r)";
  	  		stmt.execute(ind);
  	  		
  	  		ind="CREATE INDEX ind_orderedFacts_rel ON orderedFacts(relation)";
	  		stmt.execute(ind);
  		}
  		rs.close();
  		
  		// Read for paging
  		String bk="SELECT relation,arg1,arg2 FROM orderedFacts WHERE r BETWEEN ";
  		
  		FileWriter fstream = new FileWriter(target+"_facts.b");  
        BufferedWriter out = new BufferedWriter(fstream);
        

  		int min=1, pageSize=100,count=0;
  		int max;
  		
  		
  		out.write("% background knowledge \n");
  		for (;;)
  		{
  			max=min+pageSize-1;
  	  		String between=min+" AND "+max;
  	  		
  	  		rs=stmt.executeQuery(bk+between);
  	  		
  	  		if (!rs.next()) // no more pages
  	  		{
  	  			break;
  	  		} 	
  	  		
  	  		
  	  		do// write the data to the files
  	  		{
  	  			rel=repairString(rs.getString(1));
  	  			arg1=repairString(rs.getString(2));
  	  			arg2=repairString(rs.getString(3));  	  			
  	  		
  	  			out.write(rel+"(a"+arg1+", a"+arg2+").\n");
  	  			
  	  		}while (rs.next()); 
  	  		count++;
  	  		System.out.println(count);
  	  		//rs.close();
  	  		min+=pageSize;
  		}
  		
  		//Close the output stream
  		out.close();
  		
  		//stmt.execute("DROP TABLE orderedFacts");  		
  	}
   	private void readAndWrite(String inFile,BufferedWriter out)
   	{
   		BufferedReader in;
   		String read;

   		
   		try 
   		{
   	   		
   			in = new BufferedReader(new FileReader(inFile));
   			
/*   			while((read = in.readLine())!=null)//read a line from file and save into a string
   			{
   	   			//read = in.readLine();
   	   			out.write(read+"\n");
   			}*/
   			
   			for (;;)
   			{
   				read = in.readLine();
   				if (read==null)
   				{
   					break;
   				}
   				out.write(read+"\n");
   			}

   			in.close();//safely close the BufferedReader after use
   			
   		}
   		catch(IOException e)
   		{
   			System.out.println("There was a problem:" + e);
   			
   		}
   	}
   	public void debugBK() throws Exception
   	{
   		BufferedReader in;
   		String read;
   		int count=0;
   	   	in = new BufferedReader(new FileReader("facts.db"));
   	   	
		while((read = in.readLine())!=null)//read a line from file and save into a string
   		{	
			read = in.readLine();
			if (count==614021)
			{
				System.out.println(read);//print out the line
			}
   	   		count++;  	   		
   		}
   	}
   	// to be changed
   
   	public void createPositiveExamples() throws Exception
   	{
   		ResultSet rs;
   		String pos="SELECT relation, arg1,arg2 FROM (SELECT rownum r, relation,arg1,arg2 FROM( SELECT relation, arg1,arg2 " +
   				" FROM facts WHERE relation='"+target+"' ORDER BY arg1,arg2))WHERE r BETWEEN ";
   		
   		FileWriter fstream2 = new FileWriter(target+".f");	// for aleph
   		BufferedWriter out2 = new BufferedWriter(fstream2);
   		
  		int min=1, pageSize=100, count=0;
  		int max;
  		String arg1,arg2,rel;
  		
  		out2.write("% positive examples \n");
  		for (;;)
  		{
  			max=min+pageSize-1;
  	  		String between=min+" AND "+max;
  	  		
  	  		rs=stmt.executeQuery(pos+between);
  	  		
  	  		if (!rs.next()) // no more pages
  	  		{
  	  			break;
  	  		} 	
  	  		
  	  		
  	  		do// write the data to the files
  	  		{
  	  			
  	  			rel=repairString(rs.getString(1));
  	  			arg1=repairString(rs.getString(2));
  	  			arg2=repairString(rs.getString(3));
  	  			out2.write(rel+"(a"+arg1+", a"+arg2+").\n");
  	  			
  	  			count++;
  	  			
  	  		}while (rs.next()); 
 	
  	  		min+=pageSize;
  		}
  		
  		//Close the output stream
  		out2.close();
   		
   	}
  	
  	public void dropTables() throws SQLException
  	{
  		stmt.execute("DROP TABLE orderedFacts");
  	}
  	public void types(RelationsInfo info) throws Exception
  	{
  		ResultSet rs=null;
  		String typeName, sql, between, str1,str2;
  		HashMap<String,ArrayList<String>> typeDec=new HashMap<String,ArrayList<String>>();

  		  		
  		FileWriter fstream = new FileWriter(target+"_types.b");       
        BufferedWriter out = new BufferedWriter(fstream);
        
  		
  		int min=1, pageSize=100;
  		int max;
  		String arg1,arg2,rel;  		
  		
  		sql="SELECT relation, arg1,arg2 FROM orderedFacts WHERE r BETWEEN ";
  		
	 
  	  		for (;;)
  	  		{
  	  			max=min+pageSize-1;
  	  	  		between=min+" AND "+max;
  	  	  		
  	  	  		rs=stmt.executeQuery(sql+between);
  	  	  		
  	  	  		if (!rs.next()) // no more pages
  	  	  		{
  	  	  			break;
  	  	  		} 	
  	  	  		
  	  	  		
  	  	  		do// write the data to the files
  	  	  		{
  	  	  			rel=rs.getString(1);
  	  	  			arg1=repairString(rs.getString(2));
  	  	  			arg2=repairString(rs.getString(3));
  	  	  			
  	  	  			//insert domain
  	  	  			if (!typeDec.containsKey(info.getRelationFromRelations(rel).getDomain().getName()))
  	  	  			{
  	  	  				typeDec.put(info.getRelationFromRelations(rel).getDomain().getName(), new ArrayList<String>());
  	  	  			}
  	  	  			if (!typeDec.get(info.getRelationFromRelations(rel).getDomain().getName()).contains(arg1))
  	  	  			{
  	  	  				typeDec.get(info.getRelationFromRelations(rel).getDomain().getName()).add(arg1);
  	  	  			}
  	  	  			//insert range
  	  	  			if (!typeDec.containsKey(info.getRelationFromRelations(rel).getRange().getName()))
	  	  			{
	  	  				typeDec.put(info.getRelationFromRelations(rel).getRange().getName(), new ArrayList<String>());
	  	  			}
	  	  			if (!typeDec.get(info.getRelationFromRelations(rel).getRange().getName()).contains(arg1))
	  	  			{
	  	  				typeDec.get(info.getRelationFromRelations(rel).getRange().getName()).add(arg1);
	  	  			}
	  	  			
  	  	  			
  	  	  		}while (rs.next()); 
  	  	  		
  	  	  		min+=pageSize;
  	  		}
  	  		
  	  		Set<String> set=typeDec.keySet();
  	 		Iterator<String> it=set.iterator();
  	 		
  	 		out.write("% type declarations \n");
  	 		while (it.hasNext())
  	 		{
  	 			typeName=it.next();  	 			
  	 			
  	 			for (int i=0,len=typeDec.get(typeName).size();i<len;i++)
  	 			{  	 				
  	 				out.write(typeName+"(a"+typeDec.get(typeName).get(i)+"). \n");
  	 			}
  	 			
  	 		} 		
  	 		
	  	//Close the output stream	
	  	out.close();
	  	//rs.close();
  	}
  	/**
  	 * @param s
  	 * @return
  	 */
  	private String repairString(String s)
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
		return sb.toString();
  	}
  	public void createAnAlephFile() throws IOException, SQLException
  	{
  		FileWriter fstream = new FileWriter(target+".b");	// for alchemy
        BufferedWriter out = new BufferedWriter(fstream); 
        
        //settings
        out.write(":- set(clauselength,3).\n");
        out.write(":- set(evalfn,posonly).\n");
        out.write(":- set(newvars,3).\n");
        out.write(":- set(record,true).\n");
        out.write(":- set(recordfile,\'/home/chteflio/"+target+".out\').\n");
        out.write(":- set(good,true).\n");
        out.write(":- set(goodfile,good"+target+".out"+").\n");
        out.write("write_rules("+target+".out"+").\n");
        //out.write("set(noise,50).\n");
        
        //mode declarations
        readAndWrite("modes.dat",out);
        
        //determinations
        readAndWrite(target+".dat",out);
        
        //types
        readAndWrite(target+"_types.b",out);
        
        // background Knowledge
        readAndWrite(target+"_facts.b",out);
        
        out.close();

        stmt.execute("DROP TABLE orderedFacts");
  	}
  	public static void main(String[] args)
  	{
  		try 
  		{
  			String target="livesIn";
			String[] body={"isMarriedTo","livesIn", "hasAcademicAdvisor","isCitizenOf"};
			alephExp alephExp=new alephExp(args[0],target, body);
			
			
			RelationPreProcessor preprocessor=new RelationPreProcessor(args[0]);			
			RelationsInfo info=preprocessor.getRelationsInfo();
			
			System.out.println("create declarations...");
			alephExp.predicateDeclarations(info);
			
			System.out.println("create determinations...");
			alephExp.determinations();
			
			System.out.println("create positive examples...");
			alephExp.createPositiveExamples();
			
			System.out.println("create BK...");
			alephExp.createBackgroundKnowledge();
			//alephExp.debugBK();
			
			System.out.println("create types...");
			alephExp.types(info);
			
			System.out.println("create the file...");
			alephExp.createAnAlephFile();
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  	}

}
