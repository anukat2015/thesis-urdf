package urdf.ilp.old;



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

public class FileCreator 
{
	private Connection conn;
	private Statement stmt;
	private PreparedStatement bk;
	private String dumpRel=" ('domain','range','isCalled','means','subPropertyOf','subClassOf','type','foundIn','during','since','until','inUnit','inLanguage','using','gez_hasLatitude','gez_hasLongitude','gez_hasName','gez_islocatedIn') ";
	private String[] dumps={"domain","range","isCalled","means","subPropertyOf","subClassOf","type","foundIn","during","since","until","inUnit","inLanguage","using","gez_hasLatitude","gez_hasLongitude","gez_hasName","gez_islocatedIn"};
	
	
	public FileCreator(String iniFile) throws Exception
	{
		try 
		{			
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
  		Set<String> set=info.getAllRelations().keySet();
  		Iterator<String> it=set.iterator();
  		String name,str, mode;  	
  		boolean flag;
  		
  		FileWriter fstream1 = new FileWriter("facts-empty.mln");	
  		BufferedWriter out1 = new BufferedWriter(fstream1);   	
  		FileWriter fstream2 = new FileWriter("modes.dat");	
  		BufferedWriter out2 = new BufferedWriter(fstream2);
  		
  		out2.write("% mode declarations \n");
  		
  		while (it.hasNext())
  		{
  			flag=false;
  			name=it.next();
  			for (int i=0,len=dumps.length;i<len;i++)
  			{
  				if (dumps[i].equals(name))
  				{
  					flag=true;
  					break;
  				}
  			}
  			if (!flag)
  			{
  				str=name+"("+info.getRelationFromRelations(name).getDomain().getName()+", "+info.getRelationFromRelations(name).getRange().getName()+")";
  	  			mode=":-modeb(*, "+name+"(+"+info.getRelationFromRelations(name).getDomain().getName()+", +"+info.getRelationFromRelations(name).getRange().getName()+")";
  	  			out1.write(str+"\n");  	
  	  			out2.write(mode+"\n");
  			}
  			
  		}
  		
  		out1.close();
  		out2.close();
  		
  	}
  	public void determinations(String targetPredicate, RelationsInfo info) throws IOException
  	{
  		Set<String> set=info.getAllRelations().keySet();
  		Iterator<String> it=set.iterator();
  		String det,name;
  		boolean flag;
  		
  		FileWriter fstream2 = new FileWriter(targetPredicate+".dat");	
  		BufferedWriter out2 = new BufferedWriter(fstream2);
  		
		out2.write("% determinations \n");
  		
  		while (it.hasNext())
  		{
  			flag=false;
  			name=it.next();
  			for (int i=0,len=dumps.length;i<len;i++)
  			{
  				if (dumps[i].equals(name))
  				{
  					flag=true;
  					break;
  				}
  			}
  			if (!flag)
  			{
  				det=":-determination("+targetPredicate+"/2, "+name+"/2).";
  	  			out2.write(det+"\n");
  			}
  			
  		}  		
  		out2.close();  		
  	}
  	

   	public void createBackgroundKnowledge(String baseTbl, RelationsInfo info) throws Exception
  	{ 	
  		ResultSet rs;
  		String rel,arg1,arg2, goodRels="(";
  		
  		rs=stmt.executeQuery("SELECT 1 FROM user_objects WHERE object_type = 'TABLE' AND object_name='ORDEREDFACTS'");
  		Set <String> set=info.getAllRelations().keySet();
  		Iterator<String> it=set.iterator();
  		
  		while (it.hasNext())
  		{
  			goodRels+=it.next()+", ";
  		}
  		goodRels=goodRels.substring(0, goodRels.length()-2)+")";
  		
  		if (!rs.next()) // if it doesn't exist
  		{
  	  		// create the ordered table:
  	  		String tmp="CREATE TABLE orderedFacts AS (SELECT rownum r, relation, arg1,arg2 FROM (SELECT relation, arg1, arg2 FROM facts "+baseTbl+
  	  					" WHERE relation NOT IN "+dumpRel+" AND relation IN "+goodRels+" ORDER BY relation ASC) f)";
  			
  			
  	  		
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
  		
  		FileWriter fstream1 = new FileWriter("facts.db");	// for alchemy
  		FileWriter fstream2 = new FileWriter("facts.b");	// for aleph
        BufferedWriter out1 = new BufferedWriter(fstream1); 
        BufferedWriter out2 = new BufferedWriter(fstream2);
        

  		int min=1, pageSize=100;
  		int max;
  		
  		
  		out2.write("% background knowledge \n");
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
  	  			
  	  			
  	  			out1.write(rel+"(A"+arg1+", A"+arg2+")\n");
  	  			out2.write(rel+"(A"+arg1+", A"+arg2+").\n");
  	  			
  	  		}while (rs.next()); 
  	  		//rs.close();
  	  		min+=pageSize;
  		}
  		
  		//Close the output stream
  		out1.close();
  		out2.close();
  		
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
   	//public void createPositiveExamples(String targetPredicate, String tbl, int sampleSize) throws Exception
   	public void createPositiveExamples(String targetPredicate, String tbl) throws Exception
   	{
   		ResultSet rs;
   		String pos="SELECT relation, arg1,arg2 FROM (SELECT rownum r, relation,arg1,arg2 FROM( SELECT relation, arg1,arg2 " +
   				" FROM "+tbl+" WHERE relation='"+targetPredicate+"' ORDER BY arg1,arg2))WHERE r BETWEEN ";
   		
   		FileWriter fstream2 = new FileWriter(targetPredicate+".f");	// for aleph
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
  	  			out2.write(rel+"(A"+arg1+", A"+arg2+").\n");
  	  			
  	  			count++;
//  	  			if (count>=sampleSize)
//  	  			{
//  	  				break;
//  	  			}
  	  			
  	  		}while (rs.next()); 
  	  		
//  	  		if (count>=sampleSize)
//			{
//				break;
//			}
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

  		
  		FileWriter fstream1 = new FileWriter("types.mln");	// for alchemy
  		FileWriter fstream2 = new FileWriter("types.b");	// for aleph
        BufferedWriter out1 = new BufferedWriter(fstream1); 
        BufferedWriter out2 = new BufferedWriter(fstream2);
        
  		
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
  	 		
  	 		out2.write("% type declarations \n");
  	 		while (it.hasNext())
  	 		{
  	 			typeName=it.next();
  	 			out1.write(typeName+" = { ");
  	 			
  	 			for (int i=0,len=typeDec.get(typeName).size();i<len;i++)
  	 			{
  	 				if (i!=0)
  	 				{
  	 					out1.write(",");
  	 				}
  	 				out1.write("A"+typeDec.get(typeName).get(i));
  	 				out2.write(typeName+"(A"+typeDec.get(typeName).get(i)+"). \n");
  	 			}
  	 			out1.write(" } \n");
  	 		}
  	  		
  	  		
  	  		
  	  		out1.write(" } \n");
  		
  	  		
  	  		
  	  		
/*  		while (it.hasNext())
  		{
  			typeName=it.next();
  			if (typeName.equals("YagoFact"))
  			{
  				continue;
  			}
  	  		sql="SELECT relation, arg1,arg2 FROM orderedFacts WHERE r BETWEEN ";
  	  		
  			
  	  		out1.write(typeName+" = { ");
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
  	  	  			str2=rs.getString(1);  
  	  	  			str1=rs.getString(2);    	  	  			
  	  	  			out2.write(str2+"\n");
  	  	  			out1.write(str1 +", ");
  	  	  			
  	  	  		}while (rs.next()); 
  	  	  		
  	  	  		min+=pageSize;
  	  		}
  	  		out1.write(" } \n");

  			
  			
  			
  		}*/
  		
	  	//Close the output stream
	  	out1.close();
	  	out2.close();
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
  	public void createAnAlephFile(String target) throws IOException
  	{
  		FileWriter fstream = new FileWriter(target+".b");	// for alchemy
        BufferedWriter out = new BufferedWriter(fstream); 
        
        //settings
        out.write("set(clauselength,3).\n");
        out.write("set(evalfn,posonly).\n");
        out.write("set(newvars,3).\n");
        out.write("set(good,true).\n");
        out.write("set(goodfile,good"+target+".out"+").\n");
        out.write("write_rules("+target+".out"+").\n");
        //out.write("set(noise,50).\n");
        
        //mode declarations
        readAndWrite("modes.dat",out);
        
        //determinations
        readAndWrite(target+".dat",out);
        
        //types
        readAndWrite("types.b",out);
        
        // background Knowledge
        readAndWrite("facts.b",out);
        
        out.close();

  	}
  	public static void main(String[] args)
  	{
  		try 
  		{
			FileCreator fCreator=new FileCreator(args[0]);
			String[] bk={"hasSuccessor","hasChild"};
			
			RelationPreProcessor preprocessor=new RelationPreProcessor(args[0]);			
			RelationsInfo info=preprocessor.getRelationsInfo();
			
			System.out.println("create declarations...");
			fCreator.predicateDeclarations(info);
			
			System.out.println("create determinations...");
			fCreator.determinations("hasPredecessor", info);
			
			System.out.println("create positive examples...");
			fCreator.createPositiveExamples("hasPredecessor", "facts");
			
			System.out.println("create BK");
			fCreator.createBackgroundKnowledge("facts", info);
			fCreator.debugBK();
			
			System.out.println("create types...");
			fCreator.types(info);
			
			System.out.println("create the file...");
			fCreator.createAnAlephFile("hasPredecessor");
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  	}

}
