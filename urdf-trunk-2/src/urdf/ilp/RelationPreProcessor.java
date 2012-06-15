package urdf.ilp;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import basic.configuration.DBConfig;
import basic.configuration.DBConfig.DatabaseParameters;

/**
 * @author Christina Teflioudi 
 * 
 * Fills in the relationsInfo, which holds all important info about who can be joined with whom
 * 
 */

public class RelationPreProcessor implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Connection conn;
	private Statement stmt;
	DatabaseParameters p;
	
	private ThresholdChecker tChecker;
	private RelationsInfo relationsInfo;
	private String baseTbl;
	
	private HashMap<Integer,String> relationsForConstants;	
	private ArrayList<Relation> relations=new ArrayList<Relation>();
	private ArrayList<Type> types=new ArrayList<Type>();
	
	private  HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg2=new HashMap<Relation,ArrayList<Relation>>();
	private  HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg1=new HashMap<Relation,ArrayList<Relation>>();
	private  HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg2=new HashMap<Relation,ArrayList<Relation>>();
	private  HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg1=new HashMap<Relation,ArrayList<Relation>>();
	
	private HashMap<String,Integer> dangerousRelations=new HashMap<String,Integer>();
	
	//private PreparedStatement factsQuery;							// used by RelationPreProcessor for evaluating the size of a relation
	private PreparedStatement arg1JoinOnArg1Overlap;				// used by RelationPreProcessor for evaluating the overlap between 2 relations
	private PreparedStatement arg1JoinOnArg2Overlap;				// used by RelationPreProcessor for evaluating the overlap between 2 relations
	private PreparedStatement arg2JoinOnArg1Overlap;				// used by RelationPreProcessor for evaluating the overlap between 2 relations
	private PreparedStatement arg2JoinOnArg2Overlap;				// used by RelationPreProcessor for evaluating the overlap between 2 relations
	private PreparedStatement multVarianceArg1;	
	private PreparedStatement multVarianceArg2;
	private PreparedStatement distinctEntitiesArg1;
	private PreparedStatement distinctEntitiesArg2;
	
	//private PreparedStatement factsQuery;							
	private String arg1JoinOnArg1OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg1=f2.arg1 HAVING count(*)>?*? AND count(*)>?";				
	private String arg1JoinOnArg2OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg1=f2.arg2 HAVING count(*)>?*? AND count(*)>?";				
	private String arg2JoinOnArg1OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg2=f2.arg1 HAVING count(*)>?*? AND count(*)>?";				
	private String arg2JoinOnArg2OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg2=f2.arg2 HAVING count(*)>?*? AND count(*)>?";				
	private String multVarianceArg1QueryString = "SELECT sum((mult-average)*(mult-average)*numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation=? GROUP BY f0.arg1) GROUP BY mult), (SELECT mult1 average FROM rel_stats WHERE relation=?)";	
	private String multVarianceArg2QueryString = "SELECT sum((mult-average)*(mult-average)*numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation=? GROUP BY f0.arg2) GROUP BY mult), (SELECT mult2 average FROM rel_stats WHERE relation=?)";
	private String distinctEntitiesArg1QueryString = "SELECT count(*) FROM (SELECT arg1 FROM "+baseTbl+" WHERE relation=? GROUP BY arg1)";
	private String distinctEntitiesArg2QueryString = "SELECT count(*) FROM (SELECT arg2 FROM "+baseTbl+" WHERE relation=? GROUP BY arg2)";	
	
	
	public RelationPreProcessor(String iniFile, ThresholdChecker tChecker, ArrayList<Relation> relations,ArrayList<Type> types, HashMap<Integer,String>relationsForConstants,String baseTbl) throws Exception
	{
		try 
		{	
			this.tChecker=tChecker;	
			this.baseTbl=baseTbl;
			this.relations=relations;
			this.types=types;
			this.relationsForConstants=relationsForConstants;
			initializeConnection(iniFile); //initialize the connection		
			System.out.println("Connected to DB");
			prepareStatements();// Prepare the PreparedStatements
			this.stmt = conn.createStatement();				
		} 
		catch (ClassNotFoundException e) {e.printStackTrace();}	
		catch (SQLException e) {e.printStackTrace();}
		
		long time=System.currentTimeMillis();
		preprocess();	
		relationsInfo=new RelationsInfo(this.relations,this.types, arg1JoinOnArg2, arg1JoinOnArg1, arg2JoinOnArg2, arg2JoinOnArg1, dangerousRelations);		
		persist();
		time=System.currentTimeMillis()-time;
		System.out.println("time for preprocessing: "+time);
		System.out.println("relation preprocessing done...");
	}
	public RelationPreProcessor(String iniFile) throws SQLException, IOException
	{
		//use already calculated and persisted data
		p = DBConfig.databaseParameters(iniFile);
		readFromDisk();	
	}
	private void persist()
	{
	      try
	      {
	    	  FileOutputStream fileOut;
	    	  if (p.user.equals("yago"))
	    	  {
	    		  fileOut = new FileOutputStream("relationsInfoForYago1.ser");
	    	  }
	    	  else//yago2
	    	  {
	    		  fileOut = new FileOutputStream("relationsInfoForYago2.ser");
	    	  }	    	  
		      ObjectOutputStream out = new ObjectOutputStream(fileOut);
		      out.writeObject(relationsInfo);
		         
		      out.close();
	          fileOut.close();
	      }
	      catch(IOException i)
	      {
	          i.printStackTrace();
	      }

	}
	private void readFromDisk()
	{
		 
        try
        {
        	FileInputStream fileIn;
        	ObjectInputStream in;
        	        	
        	if (p.user.equals("yago"))
        	{
        		fileIn =new FileInputStream("relationsInfoForYago1.ser");
        	}
        	else//yago2
        	{
        		fileIn =new FileInputStream("relationsInfoForYago2.ser");
        	}
        	
        	in = new ObjectInputStream(fileIn);
        	relationsInfo = (RelationsInfo) in.readObject();
           
        	in.close();
        	fileIn.close();
       }
       catch(IOException i)
       {
           i.printStackTrace();
           return;
       }
       catch(ClassNotFoundException c)
       {
           System.out.println("RelationPreProcessor class not found");
           c.printStackTrace();
           return;
       }

	}
	private void preprocess() throws SQLException
	{	
		int constant=0;
		if (relationsForConstants==null || relationsForConstants.size()==0)
		{
			relationsForConstants=defaultRelationsForConstants();
		}
		String constantIn1=relationsForConstants.get(1);
		String constantIn2=relationsForConstants.get(2);
		if (relations==null || relations.size()==0)// the user hasn't given me anything, so search yago
		{
			relations=new ArrayList<Relation>();
			types=new ArrayList<Type>();
			
			String name,domain,range,subType,superType;
			Relation rel;
			int numberOfFacts;
			float mult1,mult2;
			
			ArrayList<String> typeName=new ArrayList<String>();
			
			// Get the types and their hierarchy
			ResultSet rs=getTypeHierarchyFromDB();
			
			while(rs.next())
			{
				subType=rs.getString(1);
				superType=rs.getString(2);
				
				if (!typeName.contains(superType))
				{
					typeName.add(superType);
					types.add(new Type(superType));
				}
				if(!typeName.contains(subType))
				{
					typeName.add(subType);
					types.add(new Type(subType, getTypeFromTypes(superType)));
				}
				else
				{
					getTypeFromTypes(subType).setSuperType(getTypeFromTypes(superType));
				}
			}
			
			
			rs.close();
			
			//  Get the relations that appear in the DB
			rs=getRelationsFromDB();	
			
			while(rs!=null && rs.next())
			{
				name=rs.getString(1);
				if(eliminateRelations(name))
				{
					continue;
				}
				domain=rs.getString(2);
				if (rs.wasNull())
				{
					domain=repairDomainOrRange(name,true);
				}
				range=rs.getString(3);
				if (rs.wasNull())
				{
					range=repairDomainOrRange(name,false);
				}
				
				if (!typeName.contains(domain))
				{
					typeName.add(domain);
					types.add(new Type(domain));
				}
				if (!typeName.contains(range))
				{
					typeName.add(range);
					types.add(new Type(range));
				}
				numberOfFacts=rs.getInt(4);
				mult1=rs.getFloat(5);
				mult2=rs.getFloat(6);
				if (constantIn1!=null && constantIn1.contains(name))
				{
					constant=1;
				}
				else if(constantIn2!=null && constantIn2.contains(name))
				{
					constant=2;
				}
				else
				{
					constant=0;
				}
				rel=new Relation(name, getTypeFromTypes(domain), getTypeFromTypes(range),numberOfFacts,mult1,mult2,constant);				
				relations.add(rel);		
			}
			rs.close();
					
			repairTypes();
			repairRelations();
		}
		

		//printTypesAndRelations();
		
		dangerousRelations=getDangerousRelations();
		
		// fill in join HashMaps
		findJoinableRelations();
		
		// close everything
		closePreparedStatements();
	}
	
	/**
	 *  candidate pairs are only pairs that share exactly the same type or the type of the one is parent of the type of the other
	 *  Then I also need to check the real overlap
	 */
	private void findJoinableRelations() throws SQLException
	{
		int j;
		int facts1=0,minFacts=0,entities;
		int facts2=0;
		float idealMult;
		double var;
		boolean sameFlag=false;
		
		
		for (int i=0,len=relations.size();i<len;i++)
		{
			// first find ideal multiplicities
			entities=getDistinctEntities(relations.get(i).getName(),1);
			relations.get(i).setDistinctEntities(1,entities);
			var=getVarMult(relations.get(i).getName(),1)/entities;
			idealMult=(float)(relations.get(i).getMult1()+Math.sqrt(var));
			relations.get(i).setIdealMult(idealMult, 1);
			
			entities=getDistinctEntities(relations.get(i).getName(),2);
			relations.get(i).setDistinctEntities(2,entities);
			var=getVarMult(relations.get(i).getName(),2)/entities;			
			idealMult=(float)(relations.get(i).getMult2()+Math.sqrt(var));
			relations.get(i).setIdealMult(idealMult, 2);
			
			j=i;
			
			if (arg1JoinOnArg2.get(relations.get(i))==null)
				arg1JoinOnArg2.put(relations.get(i), new ArrayList<Relation>());
			if (arg1JoinOnArg1.get(relations.get(i))==null)
				arg1JoinOnArg1.put(relations.get(i), new ArrayList<Relation>());
			if (arg2JoinOnArg2.get(relations.get(i))==null)
				arg2JoinOnArg2.put(relations.get(i), new ArrayList<Relation>());
			if (arg2JoinOnArg1.get(relations.get(i))==null)
				arg2JoinOnArg1.put(relations.get(i), new ArrayList<Relation>());
			
			facts1=relations.get(i).getSize();
			
			//relations.get(i).setIsSymmetric(tChecker.isSymmetric(relations.get(i), facts1));
			
			if (facts1==0)
				continue;
			
			
			while(j<len)
			{
				if (!relations.get(i).equals(relations.get(j)))
				{
					sameFlag=false;
					facts2=relations.get(j).getSize();
					System.out.println(relations.get(j).getName());
					if (facts2==0)
					{
						j++;
						continue;
					}
						
					//minFacts=(facts1>facts2?facts2:facts1);
					minFacts=Math.min(facts1, facts2);
					System.out.println("Facts1: "+facts1+" facts2: "+facts2);
					
				}
				else
				{
					sameFlag=true;
					//facts2=facts1;
					minFacts=facts1;
				}
				
				if (sameFlag)
				{
					if (!arg1JoinOnArg1.containsKey(relations.get(i)))
					{
						arg1JoinOnArg1.put(relations.get(i),new ArrayList<Relation>());
					}
					if (!arg1JoinOnArg1.get(relations.get(i)).contains(relations.get(j)))
					{
						arg1JoinOnArg1.get(relations.get(i)).add(relations.get(j));
					}
					
					if (!arg2JoinOnArg2.containsKey(relations.get(i)))
					{
						arg2JoinOnArg2.put(relations.get(i),new ArrayList<Relation>());
					}
					if (!arg2JoinOnArg2.get(relations.get(i)).contains(relations.get(j)))
					{
						arg2JoinOnArg2.get(relations.get(i)).add(relations.get(j));
					}
				}
				else
				{
					// try to join i's 1st arg with j's 1st arg
					if (relations.get(i).getDomain().equals(relations.get(j).getDomain())
							||relations.get(i).getDomain().isChildOf(relations.get(j).getDomain())
							||relations.get(j).getDomain().isChildOf(relations.get(i).getDomain()))
					{				
						fillInJoinOnMap(1, arg1JoinOnArg1, arg1JoinOnArg1,  relations.get(i),1, relations.get(j), 1,minFacts, sameFlag);
					}
					
					// try to join i's 2nd arg with j's 2nd arg
					if (relations.get(i).getRange().equals(relations.get(j).getRange())
							||relations.get(i).getRange().isChildOf(relations.get(j).getRange())
							||relations.get(j).getRange().isChildOf(relations.get(i).getRange()))
					{	
						fillInJoinOnMap(4, arg2JoinOnArg2, arg2JoinOnArg2,  relations.get(i),2, relations.get(j), 2,minFacts, sameFlag);
					}

				}
				
				// try to join i's 1st arg with j's 2nd arg
				if (relations.get(i).getDomain().equals(relations.get(j).getRange())
						||relations.get(i).getDomain().isChildOf(relations.get(j).getRange())
						||relations.get(j).getRange().isChildOf(relations.get(i).getDomain()))
				{
					fillInJoinOnMap( 2, arg1JoinOnArg2, arg2JoinOnArg1,  relations.get(i),1, relations.get(j), 2,minFacts,  sameFlag);
				}
				// try to join i's 2nd arg with j's 1st arg
				if (relations.get(i).getRange().equals(relations.get(j).getDomain())
						||relations.get(i).getRange().isChildOf(relations.get(j).getDomain())
						||relations.get(j).getDomain().isChildOf(relations.get(i).getRange()))
				{
					fillInJoinOnMap(3, arg2JoinOnArg1, arg1JoinOnArg2,  relations.get(i),2, relations.get(j), 1,minFacts,  sameFlag);
				}
				j++;
			}
		}
	}
	private void fillInJoinOnMap(int joinCase,HashMap<Relation,ArrayList<Relation>> map1,HashMap<Relation,ArrayList<Relation>> map2, Relation relation1,int arg1,Relation relation2, int arg2,int facts,  boolean sameFlag) throws SQLException
	{
		if (arg1==arg2)
		{
			if (fireOverlapQuery(joinCase, relation1, relation2, facts, tChecker.getSupportThreshold(),tChecker.getPossiblePosToBeCoveredThreshold()))
			{
				if (!map1.containsKey(relation1))
				{
					map1.put(relation1,new ArrayList<Relation>());
				}
				if (!map1.get(relation1).contains(relation2))
				{
					map1.get(relation1).add(relation2);
				}
					
					
				if (!map1.containsKey(relation2))
				{
					map1.put(relation2,new ArrayList<Relation>());
				}
				if (!map1.get(relation2).contains(relation1))
				{
					map1.get(relation2).add(relation1);
				}							
				
			}
		}
		else
		{
			if (fireOverlapQuery(joinCase, relation1,  relation2,  facts, tChecker.getSupportThreshold(),tChecker.getPossiblePosToBeCoveredThreshold()))
			{
				if (!map1.containsKey(relation1))
				{
					map1.put(relation1,new ArrayList<Relation>());
				}
				if (!map1.get(relation1).contains(relation2))
				{
					map1.get(relation1).add(relation2);
				}
					
				if (!sameFlag)
				{	
					if (!map2.containsKey(relation2))
					{
						map2.put(relation2,new ArrayList<Relation>());
					}
					if (!map2.get(relation2).contains(relation1))
					{
						map2.get(relation2).add(relation1);
					}			
				}
			}
		}
	}
	
	
	// ********* AUXILLIARY METHODS ***********
	public Type getTypeFromTypes(String typeName)
	{
		if (types != null && typeName != null) {
			for(int i=0,len=types.size();i<len;i++)
			{
					if (types.get(i).getName().equals(typeName))
					{
						return types.get(i);
					}
			}
		}
		return null;
	}
	public Relation getRelationFromRelations(String relationName)
	{
		for(int i=0,len=relations.size();i<len;i++)
		{
			if (relations.get(i).getName().equals(relationName))
			{
				return relations.get(i);
			}
		}
		return null;
	}
	public RelationsInfo getRelationsInfo()
	{
		return relationsInfo;
	}
	private void printTypesAndRelations()
	{
		System.out.println("Type Hierachy:");
		for(int i=0,len=types.size();i<len;i++)
		{
			System.out.println("type: "+types.get(i).getName()+" SuperType: "+(types.get(i).getSuperType()==null?"null":types.get(i).getSuperType().getName()));
		}
		System.out.println("Relations:");
		for(int i=0,len=relations.size();i<len;i++)
		{
			System.out.println(relations.get(i).getName()+"("+relations.get(i).getDomain().getName()+", "+relations.get(i).getRange().getName()+")");
		}
	}

	private String repairDomainOrRange(String relationName, boolean isDomain)
	{
		String output;
		if (isDomain)
		{
			if (relationName.equals("producedIn")||relationName.equals("hasProductionLanguage")||relationName.equals("hasImdb"))
			{
				output="wordnet_movie_106613686";
			}
			else
			{
				output="wordnet_entity_100001740";
			}
		}
		else
		{
			if (relationName.equals("edited")||relationName.equals("directed")||relationName.equals("actedIn"))
			{
				output="wordnet_movie_106613686";
			}
			else if(relationName.equals("worksAt"))
			{
				output="wordnet_organization_108008335";
			}
			else if(relationName.equals("inTimeZone"))
			{
				output="yagoNumber";
			}
			else if (relationName.equals("hasWonPrize"))
			{
				output="wordnet_award_106696483";
			}
			else
			{
				output="wordnet_entity_100001740";
			}
			
		}
		return output;
	}
	
	private boolean eliminateRelations(String relationName)
	{
		// yago specific
		if (relationName.equals("isCalled")||relationName.equals(">")||relationName.equals("<")||relationName.equals("means")
				||relationName.equals("range")||relationName.equals("domain")||relationName.equals("subClassOf")||relationName.equals("subPropertyOf"))
			return true;
		
		// time specific
		if (relationName.equals("after")||relationName.equals("before")||relationName.equals("sameYear")||relationName.equals("establishedOnDate")
				|| relationName.equals("bornOnDate")||relationName.equals("createdOnDate")||relationName.equals("diedOnDate")
				|| relationName.equals("happenedIn")||relationName.equals("publishedOnDate")||relationName.equals("since")
				|| relationName.equals("until") || relationName.equals("writtenInYear"))
			return true;
		
		// gazeteer specific
		if (relationName.equals("gaz_hasLatitude")||relationName.equals("gaz_hasLongitude")||relationName.equals("gaz_hasName")
				||relationName.equals("gaz_isLocatedIn"))
			return true;
			
		// dangerous or wrong
		if (relationName.equals("hasMilitary")||relationName.equals("published")||relationName.equals("describes")
				||relationName.equals("foundIn"))
			return true;
		
		
		return false;
	}
	
	private void repairTypes()
	{
		getTypeFromTypes("wordnet_university_108286163").setSuperType(getTypeFromTypes("wordnet_organization_108008335"));
		getTypeFromTypes("wordnet_country_108544813").setSuperType(getTypeFromTypes("wordnet_location_100027167"));
		getTypeFromTypes("wordnet_organization_108008335").setSuperType(getTypeFromTypes("wordnet_physical_entity_100001930"));
		getTypeFromTypes("wordnet_award_106696483").setSuperType(getTypeFromTypes("wordnet_entity_100001740"));
		
		if(p.user.equals("yago"))
		{
			getTypeFromTypes("wordnet_yagoActorGeo_1").setSuperType(getTypeFromTypes("wordnet_physical_entity_100001930"));
		}
		
		
		getTypeFromTypes("wordnet_physical_entity_100001930").setSuperType(getTypeFromTypes("wordnet_entity_100001740"));
	}
	private void repairRelations()
	{
		relations.add(new Relation("hasPredecessor",getTypeFromTypes("wordnet_person_100007846"),getTypeFromTypes("wordnet_person_100007846"),20515,1,(float)1.07,0));
		relations.add(new Relation("hasSuccessor",getTypeFromTypes("wordnet_person_100007846"),getTypeFromTypes("wordnet_person_100007846"),55535,1,(float)1.039,0));
		relations.add(new Relation("produced",getTypeFromTypes("wordnet_person_100007846"),getTypeFromTypes("wordnet_entity_100001740"),41747,(float)4.3,(float)1.006,0));
	}
	private HashMap<Integer,String> defaultRelationsForConstants()
	{
		HashMap<Integer,String> map=new HashMap<Integer,String>();
		map.put(2, "bornIn");
		map.put(2, "diedIn");
		map.put(2, "graduatedFrom");
		map.put(2, "hasCurrency");
		map.put(2, "hasOfficialLanguage");
		map.put(2, "hasProductionLanguage");
		map.put(2, "hasUTCOffset");
		map.put(2, "hasWonPrize");
		map.put(2, "inLanguage");
		map.put(2, "isAffiliatedTo");
		map.put(2, "isCitizenOf");
		map.put(2, "locatedIn");
		map.put(2, "type");
		return map;
	}
	private void repairConstants()
	{
		relationsInfo.getRelationFromRelations("bornIn").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("diedIn").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("graduatedFrom").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("hasCurrency").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("hasOfficialLanguage").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("hasUTCOffset").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("hasWonPrize").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("inLanguage").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("isAffiliatedTo").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("locatedIn").setConstantInArg(2);
		relationsInfo.getRelationFromRelations("actedIn").setConstantInArg(1);
		
	}
	// ************** DB COMMUNICATION **************
  	private void initializeConnection(String iniFile) throws Exception
	{ 
	    p = DBConfig.databaseParameters(iniFile);

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
	      
	      System.out.println("The current knowledge base is "+p.user);

	    } 
	    else
	      throw new Exception("UNKNOWN DB DRIVER!");
	    
	}
	private void prepareStatements() throws SQLException
	{
		// Prepare the PreparedStatements
		//factsQuery=conn.prepareStatement("SELECT count(*) FROM facts WHERE relation =? ");
		
		/*
		arg1JoinOnArg1Overlap=conn.prepareStatement("SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg1=f2.arg1 HAVING count(*)>?*? AND count(*)>?");
		arg1JoinOnArg2Overlap=conn.prepareStatement("SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg1=f2.arg2 HAVING count(*)>?*? AND count(*)>?");
		arg2JoinOnArg1Overlap=conn.prepareStatement("SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg2=f2.arg1 HAVING count(*)>?*? AND count(*)>?");
		arg2JoinOnArg2Overlap=conn.prepareStatement("SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg2=f2.arg2 HAVING count(*)>?*? AND count(*)>?");
		
		multVarianceArg1=conn.prepareStatement("SELECT sum((mult-average)*(mult-average)*numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation=? GROUP BY f0.arg1) GROUP BY mult), (SELECT mult1 average FROM rel_stats WHERE relation=?)");
		multVarianceArg2=conn.prepareStatement("SELECT sum((mult-average)*(mult-average)*numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation=? GROUP BY f0.arg2) GROUP BY mult), (SELECT mult2 average FROM rel_stats WHERE relation=?)");
		distinctEntitiesArg1=conn.prepareStatement("SELECT count(*) FROM (SELECT arg1 FROM "+baseTbl+" WHERE relation=? GROUP BY arg1)");
		distinctEntitiesArg2=conn.prepareStatement("SELECT count(*) FROM (SELECT arg2 FROM "+baseTbl+" WHERE relation=? GROUP BY arg2)");
		*/
		arg1JoinOnArg1Overlap=conn.prepareStatement(arg1JoinOnArg1OverlapQueryString);
		arg1JoinOnArg2Overlap=conn.prepareStatement(arg1JoinOnArg2OverlapQueryString);
		arg2JoinOnArg1Overlap=conn.prepareStatement(arg2JoinOnArg1OverlapQueryString);
		arg2JoinOnArg2Overlap=conn.prepareStatement(arg2JoinOnArg2OverlapQueryString);
		
		multVarianceArg1=conn.prepareStatement(multVarianceArg1QueryString);
		multVarianceArg2=conn.prepareStatement(multVarianceArg2QueryString);
		distinctEntitiesArg1=conn.prepareStatement(distinctEntitiesArg1QueryString);
		distinctEntitiesArg2=conn.prepareStatement(distinctEntitiesArg2QueryString);
	}
	private ResultSet getRelationsFromDB() throws SQLException
	{
		String selectStmt="SELECT NVL(tbl1.arg1,tbl2.arg1) as relation, tbl2.arg2 as domain, tbl1.arg2 as rang ";
		//String tbl1="(SELECT * FROM facts f0 WHERE f0.relation='range') tbl1";
		//String tbl2="(SELECT * FROM facts f1 WHERE f1.relation='domain') tbl2 ";
		String tbl1="(SELECT * FROM facts f0 WHERE f0.relation='hasRange') tbl1";
		String tbl2="(SELECT * FROM facts f1 WHERE f1.relation='hasDomain') tbl2 ";
		String r1="("+selectStmt+"FROM "+ tbl1+ " FULL OUTER JOIN "+tbl2+" ON tbl1.arg1=tbl2.arg1) r1";
		String r2=" INNER JOIN rel_stats  r2 ON r1.relation=r2.relation";

		String finalClause="SELECT r1.relation, r1.domain, r1.rang, r2.n, r2.mult1,r2.mult2 FROM"+r1+r2;
		
		System.out.println(finalClause);
		ResultSet rs = stmt.executeQuery(finalClause);
		System.out.println("Query successful");
		
		return rs;
	}
	// relations that are too  big can cause problems if there are two of them in the same sql statement
	private HashMap<String,Integer> getDangerousRelations()throws SQLException
	{
		HashMap<String,Integer> dangerousRelations=new HashMap<String,Integer>();
		String sql="SELECT Relation, n FROM rel_stats WHERE N>150000";
		String rel;
		int n;
		
		System.out.println(sql);
		ResultSet rs = stmt.executeQuery(sql);
		
		//System.out.println("Dangerous relations:");
	
		while (rs.next())
		{
			rel=rs.getString(1);
			n=rs.getInt(2);
			dangerousRelations.put(rel, n);
			//System.out.println(rel);
			
		}
		rs.close();
		return dangerousRelations;
		
	}
	private ResultSet getTypeHierarchyFromDB() throws SQLException
	{
		//String rangeStmt="(SELECT arg2 FROM facts WHERE relation='range')";
		//String domainStmt="(SELECT arg2 FROM facts WHERE relation='domain')";
		String rangeStmt="(SELECT arg2 FROM facts WHERE relation='hasRange')";
		String domainStmt="(SELECT arg2 FROM facts WHERE relation='hasDomain')";
		String mainStmt="("+rangeStmt+" UNION "+domainStmt+")";		
		String finalClause="SELECT arg1 as sub, arg2 as super FROM facts WHERE relation='subclassOf' AND arg1 IN "+mainStmt+ " AND arg2 IN "+mainStmt;
		
		System.out.println(finalClause);
		ResultSet rs = stmt.executeQuery(finalClause);
		
		return rs;
		
	}
	private float getVarMult(String target,int arg) throws SQLException
	{
		PreparedStatement ps;
		String psString;
		
		switch(arg)
		{
			case 1:
				ps=multVarianceArg1;
				psString=multVarianceArg1QueryString;
				break;
			default: // 2
				ps=multVarianceArg2;
				psString=multVarianceArg2QueryString;
		}
		
		ps.setString(1, target);   	
		psString=psString.replaceFirst("?", target);
		
		ps.setString(2, target);	
		psString=psString.replaceFirst("?", target);

		System.out.println(psString);
		ResultSet rs=ps.executeQuery();
		
		rs.next();
		float ans=rs.getFloat(1);
		rs.close();
		return ans;
	}
  	private int getDistinctEntities(String target,int arg) throws SQLException
  	{
  		PreparedStatement ps;
  		String psString;
		
		switch(arg)
		{
			case 1:
				ps=distinctEntitiesArg1;
				psString=distinctEntitiesArg1QueryString;
				break;
			default: // 2
				ps=distinctEntitiesArg2;
				psString=distinctEntitiesArg2QueryString;
		}
		
		ps.setString(1, target);
		psString=psString.replaceFirst("?", target);
		
		System.out.println(psString);
		ResultSet rs=ps.executeQuery();
		
		rs.next();
		int ans=rs.getInt(1);
		rs.close();
		return ans;
  	}
	private boolean fireOverlapQuery(int joinCase, Relation relation1, Relation relation2, int minFacts, float supportThreshold,int possibleExamplesThreshold) throws SQLException
	{
 		PreparedStatement ps=null;
 		String psString = "";
 		
		switch(joinCase)
		{
			case 1:
				ps=arg1JoinOnArg1Overlap;
				psString=arg1JoinOnArg1OverlapQueryString;
				break;
			case 2:
				ps=arg1JoinOnArg2Overlap;
				psString=arg1JoinOnArg2OverlapQueryString;
				break;
			case 3:
				ps=arg2JoinOnArg1Overlap;
				psString=arg2JoinOnArg1OverlapQueryString;
				break;
			case 4:
				ps=arg2JoinOnArg2Overlap;
				psString=arg2JoinOnArg2OverlapQueryString;
				
		}
		ps.setString(1, relation1.getName());
		psString=psString.replaceFirst("?", relation1.getName());
		
		ps.setString(2, relation2.getName());
		psString=psString.replaceFirst("?", relation2.getName());
		
		ps.setFloat(3,supportThreshold);
		psString=psString.replaceFirst("?", Float.toString(supportThreshold));
		
		ps.setInt(4,minFacts);
		psString=psString.replaceFirst("?", Integer.toString(minFacts));
		
		ps.setInt(5,possibleExamplesThreshold);
		psString=psString.replaceFirst("?", Integer.toString(possibleExamplesThreshold));
		
		System.out.println(psString);
		ResultSet  rs = ps.executeQuery();
		
		if (rs.next())
		{
			rs.close();
			return true;
		}
		else
		{
			rs.close();
			return false;
		}
			
	
	}
  	private void closePreparedStatements() throws SQLException
  	{
		arg1JoinOnArg1Overlap.close();
		arg1JoinOnArg2Overlap.close();
		arg2JoinOnArg1Overlap.close();
		arg2JoinOnArg2Overlap.close();
		multVarianceArg1.close();
		multVarianceArg2.close();
		
		distinctEntitiesArg1.close();
		distinctEntitiesArg2.close();
		
		stmt.close();
		conn.close();
  	}

}
