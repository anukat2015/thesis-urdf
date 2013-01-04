package urdf.ilp;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import urdf.rdf3x.Connection;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;

/**
 * 	@author Christina Teflioudi
 * 
 * 	The class RelationsInfo contains the basic information about the relations:
 * 		Which relations are there in my background knowledge? (relations)
 * 		Which types are there for the arguments of these relations? (types)
 * 		How can I join the relations with each other? (arg1JoinOnArg2,arg1JoinOnArg1,arg2JoinOnArg2,arg2JoinOnArg1)
 * 
 * It also gives me some important relations that will be useful in general and are independent of the domain:
 * 		EQ for equal
 * 		NEQ for non equal
 * 		GT for greater than
 * 		LT for lower than
 *
 */
public class RelationsInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Hashtable<String,Relation> relations;
	private Hashtable<String,Type> types;
	public 	HashMap<String,Integer> dangerousRelations;
	
	public  HashMap<Relation,HashSet<Relation>> arg1JoinOnArg2;
	public  HashMap<Relation,HashSet<Relation>> arg1JoinOnArg1;
	public  HashMap<Relation,HashSet<Relation>> arg2JoinOnArg2;
	public  HashMap<Relation,HashSet<Relation>> arg2JoinOnArg1;

	public static final Relation EQ = new Relation("=", null, null);
	public static final Relation NEQ = new Relation("!=", null, null);
	public static final Relation GT = new Relation(">", null, null);
	public static final Relation LT = new Relation("<", null, null);
	
	public RelationsInfo(Hashtable<String,Relation> relations, Hashtable<String,Type> types, 
			HashMap<Relation,HashSet<Relation>> arg1JoinOnArg2,HashMap<Relation,HashSet<Relation>> arg1JoinOnArg1,
			HashMap<Relation,HashSet<Relation>> arg2JoinOnArg2,HashMap<Relation,HashSet<Relation>> arg2JoinOnArg1,
			HashMap<String,Integer> dangerousRelations) {
		
		this.types = types;
		this.relations = relations;
		this.arg1JoinOnArg1=arg1JoinOnArg1;
		this.arg1JoinOnArg2=arg1JoinOnArg2;
		this.arg2JoinOnArg1=arg2JoinOnArg1;
		this.arg2JoinOnArg2=arg2JoinOnArg2;
		this.dangerousRelations=dangerousRelations;
		
		
	}
	
	public RelationsInfo(ArrayList<Relation> relations,ArrayList<Type> types, 
			HashMap<Relation,HashSet<Relation>> arg1JoinOnArg2,HashMap<Relation,HashSet<Relation>> arg1JoinOnArg1,
			HashMap<Relation,HashSet<Relation>> arg2JoinOnArg2,HashMap<Relation,HashSet<Relation>> arg2JoinOnArg1,
			HashMap<String,Integer> dangerousRelations) {
		
		this.types=new Hashtable<String,Type>();
		this.relations=new Hashtable<String,Relation>();	
		
	
		for (int i=0, len=types.size();i<len;i++)
			this.types.put(types.get(i).getName(), types.get(i));
		
		for (int i=0, len=relations.size();i<len;i++)
			this.relations.put(relations.get(i).getName(), relations.get(i));
		
		this.arg1JoinOnArg1=arg1JoinOnArg1;
		this.arg1JoinOnArg2=arg1JoinOnArg2;
		this.arg2JoinOnArg1=arg2JoinOnArg1;
		this.arg2JoinOnArg2=arg2JoinOnArg2;
		this.dangerousRelations=dangerousRelations;
		
		
	}
	public Type getTypeFromTypes(String typeName){
		return types.get(typeName);
	}
	
	public Relation getRelationFromRelations(String relationName){
		return relations.get(relationName);
	}
	
	public Hashtable<String,Relation> getAllRelations(){
		return this.relations;
	}
	
	public Hashtable<String,Type> getAllTypes(){
		return this.types;
	}
	
	
	public static void printTypesAndRelations(RelationsInfo relationsInfo){
		printTypes(relationsInfo);
		printRelations(relationsInfo);
	}
	
	public static void printTypes(RelationsInfo relationsInfo) {
		System.out.println("Type Hierachy:");	
		for (String k: relationsInfo.getAllTypes().keySet()) 
			System.out.println("Type: " + relationsInfo.getAllTypes().get(k).getName() + 
							   " SuperType: " + (relationsInfo.getAllTypes().get(k).getSuperType()==null ? "null" : relationsInfo.getAllTypes().get(k).getSuperType().getName()));
	}
	
	public static void printRelations(Collection<Relation> relations) {
		System.out.println("Relations:");
		for (Relation r: relations) {
			String s = r.getName();
			if (r.getDomain()!=null) s += "("+ r.getDomain().getName() + ", "; 
			if (r.getRange()!=null) s+= r.getRange().getName()+") ";
		    s += "("+r.getMult(1)+","+r.getMult(2)+") " +
		    "("+r.getVar(1)+","+r.getVar(2)+") " +
		    "("+r.getDistinctEntities(1)+","+r.getDistinctEntities(2)+") " +
		    "("+r.getIdealMult(1)+","+r.getIdealMult(2)+")";
			System.out.println(s);
		}
	}
	
	public static void printRelations(Hashtable<String,Relation> relations) {		
		printRelations(relations.values());
	}
	
	public static void printRelations(RelationsInfo relationsInfo) {
		printRelations(relationsInfo.getAllRelations());
	}
	
	public void printJoinableRelations(String relationName) {
		printJoinableRelations(relations.get(relationName));
	}
	
	public void printJoinableRelations(Relation relation) {
		System.out.println("Arg1 join Arg1");
		for (Relation r: this.arg1JoinOnArg1.get(relation)) {
			System.out.println("\t" + r.getName());
		}
		
		System.out.println("Arg1 join Arg2");
		for (Relation r: this.arg1JoinOnArg2.get(relation)) {
			System.out.println("\t" + r.getName());
		}
		
		System.out.println("Arg2 join Arg1");
		for (Relation r: this.arg2JoinOnArg1.get(relation)) {
			System.out.println("\t" + r.getName());
		}
		
		System.out.println("Arg2 join Arg2");
		for (Relation r: this.arg2JoinOnArg2.get(relation)) {
			System.out.println("\t" + r.getName());
		}
	}
	
	public void persist(String path) {
      try  {
    	  FileOutputStream fileOut;
		  fileOut = new FileOutputStream(path);  	  
	      ObjectOutputStream out = new ObjectOutputStream(fileOut);
	      out.writeObject(this);
	      out.close();
          fileOut.close();
      }
      catch(IOException i){
          i.printStackTrace();
      }
	}
	
	public static RelationsInfo readFromDisk(String path) {	 
        try{
        	FileInputStream fileIn;
        	ObjectInputStream in;
        	fileIn =new FileInputStream(path);
        	in = new ObjectInputStream(fileIn);
        	RelationsInfo relationsInfo = (RelationsInfo) in.readObject();    
        	in.close();
        	fileIn.close();
        	return relationsInfo;

       }
       catch(IOException i){
           i.printStackTrace();
           return null;
       }
       catch(ClassNotFoundException c){
           System.out.println("RelationPreProcessor class not found");
           c.printStackTrace();
           return null;
       }

	}
	
	public void calculateMinAndMaxLiterals(Connection conn) throws SQLException {
		Statement stmt = (Statement) conn.createStatement();
		ResultSet rs;
		System.out.println("Relations with Literal Range");
		for (Relation relation: relations.values()) {
			Type range = relation.getRange();				
			if (range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoNumber>") ||
				range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoQuantity>") ||
				range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoGeoCoordinatePair>") ||
				range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoGeoCoordinate>") ||
				range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoString>") ||
				range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoDate>")) 
			{
				System.out.println(relation.getName());
				relation.setRangeIsLiteral(true);
				if (range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoNumber>") || range.equalsOrChildOf("<http://yago-knowledge.org/resource/yagoQuantity>")) {
					String queryMin = "SELECT ?min WHERE {?x "+relation.getName()+" ?min} ORDER BY ASC(?min) LIMIT 1";
					
					rs = (ResultSet) stmt.executeQuery(queryMin);
					rs.first();
					relation.setMinValue(rs.getFloat(1));
					
					String queryMax = "SELECT ?max WHERE {?x "+relation.getName()+" ?max} ORDER BY DESC(?max) LIMIT 1";
					rs = (ResultSet) stmt.executeQuery(queryMax);
					rs.first();
					relation.setMaxValue(rs.getFloat(1));
				}
			}
			else {
				relation.setRangeIsLiteral(false);
			}
		}
	}
	
	public void updateJoinableRelations(Connection conn, int minFacts) throws SQLException {
		Relation[] rels = new Relation[relations.values().size()];
		int i=0; 
		for (Relation r: relations.values()) 
			rels[i++] = r;
			
		String query;
		ResultSet rs;
		Statement stmt = (Statement) conn.createStatement();
		for (i=0; i<rels.length; i++) {
			System.out.println("i="+rels[i].getName());
			arg1JoinOnArg1.get(rels[i]).add(rels[i]);
			arg2JoinOnArg2.get(rels[i]).add(rels[i]);
			if (rels[i].domainTypesIntersects(rels[i].getRangeTypes())) {
				query = "SELECT COUNTDISTINCT ?x WHERE {?x "+rels[i].getName()+" ?a . ?b "+rels[i].getName()+" ?x}";
				rs = (ResultSet) stmt.executeQuery(query);
				rs.first();
				if (rs.getInt(1) >= minFacts) {
					arg1JoinOnArg2.get(rels[i]).add(rels[i]);
					arg2JoinOnArg1.get(rels[i]).add(rels[i]);
				}
			}
			
			for (int j=i+1; j<rels.length; j++) {				
				// arg1 joins arg1
				if (rels[i].domainTypesIntersects(rels[j].getDomainTypes())) {
					query = "SELECT COUNTDISTINCT ?x WHERE {?x "+rels[i].getName()+" ?a . ?x "+rels[j].getName()+" ?b}";
					rs = (ResultSet) stmt.executeQuery(query);
					rs.first();
					if (rs.getInt(1) >= minFacts) {
						arg1JoinOnArg1.get(rels[i]).add(rels[j]);
						arg1JoinOnArg1.get(rels[j]).add(rels[i]);
					}
				}
				// arg1 joins arg2
				if (rels[i].domainTypesIntersects(rels[j].getRangeTypes())) {
					query = "SELECT COUNTDISTINCT ?x WHERE {?x "+rels[i].getName()+" ?a . ?b "+rels[j].getName()+" ?x}";
					rs = (ResultSet) stmt.executeQuery(query);
					rs.first();
					if (rs.getInt(1) >= minFacts) {
						arg1JoinOnArg2.get(rels[i]).add(rels[j]);
						arg2JoinOnArg1.get(rels[j]).add(rels[i]);
					}
				}
				// arg2 joins arg1
				if (rels[i].rangeTypesIntersects(rels[j].getDomainTypes())) {
					query = "SELECT COUNTDISTINCT ?x WHERE {?a "+rels[i].getName()+" ?x . ?x "+rels[j].getName()+" ?b}";
					rs = (ResultSet) stmt.executeQuery(query);
					rs.first();
					if (rs.getInt(1) >= minFacts) {
						arg2JoinOnArg1.get(rels[i]).add(rels[j]);
						arg1JoinOnArg2.get(rels[j]).add(rels[i]);
					}
				}
				// arg2 joins arg2
				if (rels[i].rangeTypesIntersects(rels[j].getRangeTypes())) {
					query = "SELECT COUNTDISTINCT ?x WHERE {?a "+rels[i].getName()+" ?x . ?b "+rels[j].getName()+" ?x}";
					rs = (ResultSet) stmt.executeQuery(query);
					rs.first();
					if (rs.getInt(1) >= minFacts) {
						arg2JoinOnArg2.get(rels[i]).add(rels[j]);
						arg2JoinOnArg2.get(rels[j]).add(rels[i]);
					}
				}
			}
		}
	}

}
