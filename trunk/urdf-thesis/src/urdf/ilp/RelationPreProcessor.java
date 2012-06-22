package urdf.ilp;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;

//import java.sql.Connection;
//import java.sql.Driver;
//import java.sql.ResultSet;
//import java.sql.Statement;


import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;


import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import urdf.rdf3x.CreateStatistics;

//import basic.configuration.DBConfig;
//import basic.configuration.DBConfig.DatabaseParameters;

/**
 * @author Christina Teflioudi 
 * 
 * Fills in the relationsInfo, which holds all important info about who can be joined with whom
 * 
 */

public class RelationPreProcessor implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Statement stmt;
	
	private ThresholdChecker tChecker;
	private RelationsInfo relationsInfo;
	
	private HashMap<Integer,String> relationsForConstants;	
	private Hashtable<String,Relation> relations = new Hashtable<String,Relation>();
	private Hashtable<String,Type> types = new Hashtable<String,Type>();
	
	private  HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg2=new HashMap<Relation,ArrayList<Relation>>();
	private  HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg1=new HashMap<Relation,ArrayList<Relation>>();
	private  HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg2=new HashMap<Relation,ArrayList<Relation>>();
	private  HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg1=new HashMap<Relation,ArrayList<Relation>>();
	
	private HashMap<String,Integer> dangerousRelations=new HashMap<String,Integer>();

	
	private static final String arg1JoinOnArg1OverlapSPARQL = "select count ?u where {?x $rel1 ?a . ?x $rel2 ?b}";				
	private static final String arg1JoinOnArg2OverlapSPARQL = "select count ?u where {?x $rel1 ?a . ?b $rel2 ?x}"; 				
	private static final String arg2JoinOnArg1OverlapSPARQL = "select count ?u where {?a $rel1 ?x . ?x $rel2 ?a}";
	private static final String arg2JoinOnArg2OverlapSPARQL = "select count ?u where {?a $rel1 ?x . ?b $rel2 ?x}";				
	
	
	public RelationPreProcessor(Connection conn, ThresholdChecker tChecker, ArrayList<Relation> relations,ArrayList<Type> types, HashMap<Integer,String>relationsForConstants) throws Exception {
		try {	
			this.tChecker=tChecker;			
			this.relationsForConstants=relationsForConstants;	
			this.stmt = (Statement) conn.createStatement();			
			for (Relation r: relations) this.relations.put(r.getName(), r);
			for (Type t: types) this.types.put(t.getName(), t);		
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		long time=System.currentTimeMillis();
		
		CreateStatistics.LoadStatistics(conn, "src/insert-stats-rdf3x");
		System.out.println("Statitics loaded in the DB successfully");
		
		preprocess();	

		relationsInfo=new RelationsInfo(this.relations,this.types, arg1JoinOnArg2, arg1JoinOnArg1, arg2JoinOnArg2, arg2JoinOnArg1, dangerousRelations);		
		
		persistRdf3x();

		time=System.currentTimeMillis()-time;
		System.out.println("time for preprocessing: "+time);
	}
	
	public RelationPreProcessor(String iniFile) throws SQLException, IOException{
		readFromDiskRdf3x();	
	}
	
	public RelationPreProcessor() throws SQLException, IOException{
		readFromDiskRdf3x();	
	}
	
	
	private void persistRdf3x() {
	      try
	      {
	    	  FileOutputStream fileOut;
    		  fileOut = new FileOutputStream("relationsInfoForRdf3x.ser");  	  
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
	private void readFromDiskRdf3x()
	{	 
        try{
        	FileInputStream fileIn;
        	ObjectInputStream in;
        	fileIn =new FileInputStream("relationsInfoForRdf3x.ser");
        	in = new ObjectInputStream(fileIn);
        	relationsInfo = (RelationsInfo) in.readObject();    
        	in.close();
        	fileIn.close();

       }
       catch(IOException i){
           i.printStackTrace();
           return;
       }
       catch(ClassNotFoundException c){
           System.out.println("RelationPreProcessor class not found");
           c.printStackTrace();
           return;
       }

	}
	
	
	private void preprocess() throws SQLException
	{	
		int constant=0;
		if (relationsForConstants==null || relationsForConstants.size()==0){
			relationsForConstants=defaultRelationsForConstantsRdf3x();
		}
		String constantIn1=relationsForConstants.get(1);
		String constantIn2=relationsForConstants.get(2);
		
		// the user hasn't given me anything, so search yago
		if (relations==null || relations.size()==0){
			relations = new Hashtable<String,Relation>();
			types = new Hashtable<String,Type>();
			
			String name, domain, range, subType, superType;
			Relation rel;
			int numberOfFacts;
			float mult1,mult2;
			
			//  Get the Types that appear in the DB
			ResultSet rs=getTypeHierarchyFromRdf3x();
			while(rs.next()){
				subType   = rs.getString(1);
				superType = rs.getString(2);
				
				if (!types.containsKey(superType))
					types.put(superType, new Type(superType));
	
				if(!types.containsKey(subType))
					types.put(subType, new Type(subType, types.get(superType)));
				else
					types.get(subType).setSuperType(types.get(superType));
				
			}
			rs.close();
			System.out.println("Type Hierarchy created successfully");
			

			//  Get the Relations that appear in the DB
			rs = getRelationsFromRdf3x();
			while (rs!=null && rs.next()){
				
				name = rs.getString(1);
				if(eliminateRelations(name)){
					continue;
				}
				
				domain=rs.getString(2);
				if (rs.wasNull()){
					domain=repairDomainOrRange(name,true);
				}
				
				range=rs.getString(3);
				if (rs.wasNull()){
					range=repairDomainOrRange(name,false);
				}
				
				if (!types.containsKey(domain))
					types.put(domain, new Type(domain));
				
				if (!types.containsKey(range))
					types.put(range, new Type(range));
				
				numberOfFacts = rs.getInt(4);
				mult1 = rs.getFloat(5);
				mult2 = rs.getFloat(6);
				if (constantIn1!=null && constantIn1.contains(name)){
					constant=1;
				}
				else if(constantIn2!=null && constantIn2.contains(name)){
					constant=2;
				}
				else{
					constant=0;
				}
				
				rel = new Relation(name, types.get(domain), types.get(range), numberOfFacts, mult1, mult2, constant);				
				relations.put(name,rel);		
			}
			rs.close();
			System.out.println("Relations created successfully");
					
			//repairTypes();
			//repairRelations();
		}
		dangerousRelations=getDangerousRelationsFromRdf3x();
		findJoinableRelationsRdf3x();
	}

	
	/**
	 *  candidate pairs are only pairs that share exactly the same type or the type of the one is parent of the type of the other
	 *  Then I also need to check the real overlap
	 */
	
	
	private void findJoinableRelationsRdf3x() throws SQLException{
		int j;
		int facts1 = 0, minFacts = 0;
		int facts2 = 0;
		float idealMult;
		double var;
		boolean sameFlag=false;
		
		ArrayList<String> keys = new ArrayList<String>();
		for (String k: relations.keySet()) keys.add(k);
		
		for (int i=0,len=relations.keySet().size();i<len;i++){
			// first find ideal multiplicities
			Relation iRelation = relations.get(keys.get(i));
			
			var = getVarMultRdf3x(iRelation,1)/iRelation.getDistinctEntities(1);
			idealMult = (float)(iRelation.getMult1() + Math.sqrt(var));
			iRelation.setIdealMult(idealMult, 1);
			
			var=getVarMultRdf3x(iRelation,2)/iRelation.getDistinctEntities(2);			
			idealMult=(float)(iRelation.getMult2()+Math.sqrt(var));
			iRelation.setIdealMult(idealMult, 2);
			
			j=i;
			
			if (arg1JoinOnArg2.get(iRelation)==null)
				arg1JoinOnArg2.put(iRelation, new ArrayList<Relation>());
			if (arg1JoinOnArg1.get(iRelation)==null)
				arg1JoinOnArg1.put(iRelation, new ArrayList<Relation>());
			if (arg2JoinOnArg2.get(iRelation)==null)
				arg2JoinOnArg2.put(iRelation, new ArrayList<Relation>());
			if (arg2JoinOnArg1.get(iRelation)==null)
				arg2JoinOnArg1.put(iRelation, new ArrayList<Relation>());
			
			facts1 = iRelation.getSize();
			
			//rel.setIsSymmetric(tChecker.isSymmetric(rel, facts1));
			
			if (facts1==0)
				continue;
			
			
			while(j<len){
				
				Relation jRelation = relations.get(keys.get(j));
				if (!iRelation.equals(jRelation)){
					sameFlag=false;
					facts2=jRelation.getSize();
					if (facts2==0){
						j++;
						continue;
					}
					minFacts = Math.min(facts1, facts2);
					
				}
				else{
					sameFlag=true;
					minFacts=facts1;
				}
				
				if (sameFlag){
					if (!arg1JoinOnArg1.containsKey(iRelation))
						arg1JoinOnArg1.put(relations.get(i),new ArrayList<Relation>());

					if (!arg1JoinOnArg1.get(iRelation).contains(jRelation))
						arg1JoinOnArg1.get(iRelation).add(jRelation);
					
					if (!arg2JoinOnArg2.containsKey(iRelation))
						arg2JoinOnArg2.put(iRelation,new ArrayList<Relation>());

					if (!arg2JoinOnArg2.get(iRelation).contains(jRelation))
						arg2JoinOnArg2.get(iRelation).add(jRelation);
				}
				else{
					// try to join i's 1st arg with j's 1st arg
					if (iRelation.getDomain().equals(jRelation.getDomain())
							||iRelation.getDomain().isChildOf(jRelation.getDomain())
							||jRelation.getDomain().isChildOf(iRelation.getDomain()))
					{				
						fillInJoinOnMapRdf3x(1, arg1JoinOnArg1, arg1JoinOnArg1,  iRelation,1, jRelation, 1,minFacts, sameFlag);
					}
					
					// try to join i's 2nd arg with j's 2nd arg
					if (iRelation.getRange().equals(jRelation.getRange())
							||iRelation.getRange().isChildOf(jRelation.getRange())
							||jRelation.getRange().isChildOf(iRelation.getRange()))
					{	
						fillInJoinOnMapRdf3x(4, arg2JoinOnArg2, arg2JoinOnArg2,  iRelation,2, jRelation, 2,minFacts, sameFlag);
					}

				}
				
				// try to join i's 1st arg with j's 2nd arg
				if (iRelation.getDomain().equals(jRelation.getRange())
						||iRelation.getDomain().isChildOf(jRelation.getRange())
						||jRelation.getRange().isChildOf(iRelation.getDomain()))
				{
					fillInJoinOnMapRdf3x( 2, arg1JoinOnArg2, arg2JoinOnArg1,  iRelation,1, jRelation, 2,minFacts,  sameFlag);
				}
				// try to join i's 2nd arg with j's 1st arg
				if (iRelation.getRange().equals(jRelation.getDomain())
						||iRelation.getRange().isChildOf(jRelation.getDomain())
						||jRelation.getDomain().isChildOf(iRelation.getRange()))
				{
					fillInJoinOnMapRdf3x(3, arg2JoinOnArg1, arg1JoinOnArg2,  iRelation,2, jRelation, 1,minFacts,  sameFlag);
				}
				j++;
			}
		}
	}
	
	
	
	private void fillInJoinOnMapRdf3x(int joinCase,HashMap<Relation,ArrayList<Relation>> map1,HashMap<Relation,ArrayList<Relation>> map2, Relation relation1,int arg1,Relation relation2, int arg2,int facts,  boolean sameFlag) throws SQLException
	{
		if (arg1==arg2){
			if (fireOverlapQueryRdf3x(joinCase, relation1, relation2, facts, tChecker.getSupportThreshold(),tChecker.getPossiblePosToBeCoveredThreshold())){
				System.out.println(relation1.getName() + "\t joins \t" + relation2.getName());
				if (!map1.containsKey(relation1))
					map1.put(relation1,new ArrayList<Relation>());
				if (!map1.get(relation1).contains(relation2))
					map1.get(relation1).add(relation2);					
				if (!map1.containsKey(relation2))
					map1.put(relation2,new ArrayList<Relation>());
				if (!map1.get(relation2).contains(relation1))
					map1.get(relation2).add(relation1);									
			}
		}
		else{
			if (fireOverlapQueryRdf3x(joinCase, relation1,  relation2,  facts, tChecker.getSupportThreshold(),tChecker.getPossiblePosToBeCoveredThreshold())){
				System.out.println(relation1.getName() + "\t joins \t" + relation2.getName());
				if (!map1.containsKey(relation1))
					map1.put(relation1,new ArrayList<Relation>());
				if (!map1.get(relation1).contains(relation2))
					map1.get(relation1).add(relation2);
				if (!sameFlag){
					if (!map2.containsKey(relation2))
						map2.put(relation2,new ArrayList<Relation>());
					if (!map2.get(relation2).contains(relation1))
						map2.get(relation2).add(relation1);		
				}
			}
		}
	}
	
	
	// ********* AUXILLIARY METHODS ***********

	
	
	public RelationsInfo getRelationsInfo()
	{
		return relationsInfo;
	}
	
	
	private void printTypesAndRelations()
	{
		System.out.println("Type Hierachy:");
		for (String k: types.keySet()) 
			System.out.println("type: "+types.get(k).getName()+" SuperType: "+(types.get(k).getSuperType()==null?"null":types.get(k).getSuperType().getName()));
	
		System.out.println("Relations:");
		for (String k: relations.keySet()) {
			System.out.println(relations.get(k).getName()+"("+relations.get(k).getDomain().getName()+", "+relations.get(k).getRange().getName()+")");
		}
	}

	private String repairDomainOrRange(String relationName, boolean isDomain)
	{
		String output;
		if (isDomain){
			if (relationName.equals("producedIn")||relationName.equals("hasProductionLanguage")||relationName.equals("hasImdb")){
				output="wordnet_movie_106613686";
			}
			else{
				output="wordnet_entity_100001740";
			}
		}
		else{
			if (relationName.equals("edited")||relationName.equals("directed")||relationName.equals("actedIn")){
				output="wordnet_movie_106613686";
			}
			else if(relationName.equals("worksAt")){
				output="wordnet_organization_108008335";
			}
			else if(relationName.equals("inTimeZone")){
				output="yagoNumber";
			}
			else if (relationName.equals("hasWonPrize")){
				output="wordnet_award_106696483";
			}
			else{
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
		types.get("wordnet_university_108286163").setSuperType(types.get("wordnet_organization_108008335"));
		types.get("wordnet_country_108544813").setSuperType(types.get("wordnet_location_100027167"));
		types.get("wordnet_organization_108008335").setSuperType(types.get("wordnet_physical_entity_100001930"));
		types.get("wordnet_award_106696483").setSuperType(types.get("wordnet_entity_100001740"));
		
		
		types.get("wordnet_physical_entity_100001930").setSuperType(types.get("wordnet_entity_100001740"));
	}
	
	
	private void repairRelations()
	{
		relations.put("hasPredecessor", new Relation("hasPredecessor", types.get("wordnet_person_100007846"), types.get("wordnet_person_100007846"),20515,1,(float)1.07,0));
		relations.put("hasSuccessor", new Relation("hasSuccessor", types.get("wordnet_person_100007846"), types.get("wordnet_person_100007846"),55535,1,(float)1.039,0));
		relations.put("produced", new Relation("produced", types.get("wordnet_person_100007846"), types.get("wordnet_entity_100001740"),41747,(float)4.3,(float)1.006,0));
	}
	
	
	private HashMap<Integer,String> defaultRelationsForConstantsRdf3x() {
		HashMap<Integer,String> map=new HashMap<Integer,String>();
		map.put(2, "<http://yago-knowledge.org/resource/bornIn>");
		map.put(2, "<http://yago-knowledge.org/resource/diedIn>");
		map.put(2, "<http://yago-knowledge.org/resource/graduatedFrom>");
		map.put(2, "<http://yago-knowledge.org/resource/hasCurrency>");
		map.put(2, "<http://yago-knowledge.org/resource/hasOfficialLanguage>");
		map.put(2, "<http://yago-knowledge.org/resource/hasProductionLanguage>");
		map.put(2, "<http://yago-knowledge.org/resource/hasUTCOffset>");
		map.put(2, "<http://yago-knowledge.org/resource/hasWonPrize>");
		map.put(2, "<http://yago-knowledge.org/resource/inLanguage>");
		map.put(2, "<http://yago-knowledge.org/resource/isAffiliatedTo>");
		map.put(2, "<http://yago-knowledge.org/resource/isCitizenOf>");
		map.put(2, "<http://yago-knowledge.org/resource/locatedIn>");
		map.put(2, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		return map;
	}
	
	private void repairConstants() {
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
	
  	
 	private Connection initializeRdf3xConnection(String propsFile) throws SQLException, FileNotFoundException, IOException{ 
 		Properties props = new Properties();
 		props.load(new FileInputStream(new File(propsFile)));
 		Driver drvr = new Driver();
 		String db = (String) props.get("Database");
 		Connection conn = (Connection) drvr.connect(db, props);
 		return conn;
	}

	private ResultSet getRelationsFromRdf3x() throws SQLException{
		String sparql = "SELECT ?r ?domain ?range ?n ?m1 ?m2 where {?r <http://yago-knowledge.org/resource/hasArg1Mult> ?m1 . " +
																   "?r <http://yago-knowledge.org/resource/hasArg2Mult> ?m2 . " +
																   "?r <http://yago-knowledge.org/resource/numberOfFacts> ?n . " +
																   "?r <http://www.w3.org/2000/01/rdf-schema#range> ?range . " +
																   "?r <http://www.w3.org/2000/01/rdf-schema#domain> ?domain}";
		//System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);		
		return rs;
	}
	
	// relations that are too  big can cause problems if there are two of them in the same sql statement
	private HashMap<String,Integer> getDangerousRelationsFromRdf3x()throws SQLException{
		HashMap<String,Integer> dangerousRelations=new HashMap<String,Integer>();
		String sparql = "SELECT ?r ?n WHERE {?r <http://yago-knowledge.org/resource/numberOfFacts> ?n . FILTER(?n > 150000) }";
		
		//System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		while (rs.next()) {
			dangerousRelations.put(rs.getString(1), rs.getInt(2));
		}
		
		rs.close();
		return dangerousRelations;
		
	}
	private ResultSet getTypeHierarchyFromRdf3x() throws SQLException{
		String sparql = "select ?sub ?sup where {?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup}";
		//String sparql = "SELECT ?sub ?sup WHERE {?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sub . " +
		//										"{{?r <http://www.w3.org/2000/01/rdf-schema#range> ?sub} UNION {?r <http://www.w3.org/2000/01/rdf-schema#domain> ?sup}} . " +
		//										"{{?r <http://www.w3.org/2000/01/rdf-schema#range> ?sup} UNION {?r <http://www.w3.org/2000/01/rdf-schema#domain> ?sup}}";
		//System.out.println(sparql);
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);	
		return rs;
	}

	
	private float getVarMultRdf3x(Relation r, int arg) throws SQLException
	{	
		if (arg!=1 && arg!=2) 
			throw new SQLException("Invalid Argument, it should be 1 or 2");
		
		String name = r.getName();		
		float avg = r.getMult(arg);
		
		String sparql = "SELECT count ?arg$n WHERE {?arg1 $rel ?arg2 }";
		sparql = sparql.replace("$n", Integer.toString(arg)).replace("$rel", name);
		
		//System.out.println(sparql);
		ResultSet rs=(ResultSet) stmt.executeQuery(sparql);
		float var = 0; int count = 0;
		while (rs.next()) {
			float diff = rs.getFloat(2) - avg;
			var += Math.pow(diff, 2);
			count++;
		}
		var /= count;
		
		return var;
	}
  	
	
	private boolean fireOverlapQueryRdf3x(int joinCase, Relation relation1, Relation relation2, int minFacts, float supportThreshold,int possibleExamplesThreshold) throws SQLException {
		String sparql = "";
		switch(joinCase) {
			case 1: sparql = arg1JoinOnArg1OverlapSPARQL; break;
			case 2: sparql = arg1JoinOnArg2OverlapSPARQL; break;
			case 3: sparql = arg2JoinOnArg1OverlapSPARQL; break;
			case 4: sparql = arg2JoinOnArg2OverlapSPARQL; break;
			default: throw new SQLException("Invalid joinCase parameter, it should be 1 for joining arg1:arg1, 2 for arg1:arg2, 3 for arg2:arg1 and 4 for arg2:arg2");
		}
		sparql = sparql.replace("$rel1", relation1.getName()).replace("$rel2", relation2.getName());
		
		ResultSet rs = (ResultSet) stmt.executeQuery(sparql);
		if (rs.next()) {
			int count = rs.getInt(2); 			
			boolean join = (count >= supportThreshold*minFacts && count > possibleExamplesThreshold);
			if (join) System.out.println("["+count+"]  -  " + sparql);
			return join;
		}
		else{
			return false;
		}
	}
	
	/*
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
	private static final String arg1JoinOnArg1OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg1=f2.arg1 HAVING count(*)>?*? AND count(*)>?";				
	private static final String arg1JoinOnArg2OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg1=f2.arg2 HAVING count(*)>?*? AND count(*)>?";				
	private static final String arg2JoinOnArg1OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg2=f2.arg1 HAVING count(*)>?*? AND count(*)>?";				
	private static final String arg2JoinOnArg2OverlapQueryString = "SELECT 1 FROM facts f1, facts f2 WHERE f1.relation=? AND f2.relation=? AND f1.arg2=f2.arg2 HAVING count(*)>?*? AND count(*)>?";				
	private final String multVarianceArg1QueryString = "SELECT sum((mult-average)*(mult-average)*numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation=? GROUP BY f0.arg1) GROUP BY mult), (SELECT mult1 average FROM rel_stats WHERE relation=?)";	
	private final String multVarianceArg2QueryString = "SELECT sum((mult-average)*(mult-average)*numOfObservations) FROM (SELECT mult, count(*) numOfObservations FROM (SELECT count(*) mult FROM "+baseTbl+" f0 WHERE f0.relation=? GROUP BY f0.arg2) GROUP BY mult), (SELECT mult2 average FROM rel_stats WHERE relation=?)";
	private final String distinctEntitiesArg1QueryString = "SELECT count(*) FROM (SELECT arg1 FROM "+baseTbl+" WHERE relation=? GROUP BY arg1)";
	private final String distinctEntitiesArg2QueryString = "SELECT count(*) FROM (SELECT arg2 FROM "+baseTbl+" WHERE relation=? GROUP BY arg2)";


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
					minFacts = Math.min(facts1, facts2);
					
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
  	
  	private void prepareStatements() throws SQLException
	{
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
		ResultSet rs = ps.executeQuery();
		
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
	
		public Type getTypeFromTypes(String typeName)
	{
		if (types != null && typeName != null) {
			for(int i=0,len=types.size();i<len;i++) {
				if (types.get(i).getName().equals(typeName)){
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
        try{
        	FileInputStream fileIn;
        	ObjectInputStream in;
        	        	
        	if (p.user.equals("yago")){
        		fileIn =new FileInputStream("relationsInfoForYago1.ser");
        	}
        	else{
        		fileIn =new FileInputStream("relationsInfoForYago2.ser");
        	}
        	
        	in = new ObjectInputStream(fileIn);
        	relationsInfo = (RelationsInfo) in.readObject();
           
        	in.close();
        	fileIn.close();

       }
       catch(IOException i){
           i.printStackTrace();
           return;
       }
       catch(ClassNotFoundException c){
           System.out.println("RelationPreProcessor class not found");
           c.printStackTrace();
           return;
       }

	}
	
	*/

}
