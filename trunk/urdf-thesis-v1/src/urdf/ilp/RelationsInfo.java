package urdf.ilp;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

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
	
	public  HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg2;
	public  HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg1;
	public  HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg2;
	public  HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg1;

	public static final Relation EQ = new Relation("=", null, null);
	public static final Relation NEQ = new Relation("!=", null, null);
	public static final Relation GT = new Relation(">", null, null);
	public static final Relation LT = new Relation("<", null, null);
	
	public RelationsInfo(Hashtable<String,Relation> relations, Hashtable<String,Type> types, 
			HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg2,HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg1,
			HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg2,HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg1,
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
			HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg2,HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg1,
			HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg2,HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg1,
			HashMap<String,Integer> dangerousRelations) {
		
		this.types=new Hashtable<String,Type>();
		this.relations=new Hashtable<String,Relation>();	
		
	
		for (int i=0, len=types.size();i<len;i++)
		{
			this.types.put(types.get(i).getName(), types.get(i));
		}
		
		for (int i=0, len=relations.size();i<len;i++)
		{
			this.relations.put(relations.get(i).getName(), relations.get(i));
		}
		
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
			System.out.println(r.getName() + "("+  
							   r.getDomain().getName() + ", " + 
							   r.getRange().getName()+") " +
							   "("+r.getMult(1)+","+r.getMult(2)+") " +
							   "("+r.getVar(1)+","+r.getVar(2)+") " +
							   "("+r.getDistinctEntities(1)+","+r.getDistinctEntities(2)+") " +
							   "("+r.getIdealMult(1)+","+r.getIdealMult(2)+")");
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
	
	public void persist() {
      try  {
    	  FileOutputStream fileOut;
		  fileOut = new FileOutputStream("relationsInfoForRdf3x.ser");  	  
	      ObjectOutputStream out = new ObjectOutputStream(fileOut);
	      out.writeObject(this);
	      out.close();
          fileOut.close();
      }
      catch(IOException i){
          i.printStackTrace();
      }
	}
	
	public static RelationsInfo readFromDisk() {	 
        try{
        	FileInputStream fileIn;
        	ObjectInputStream in;
        	fileIn =new FileInputStream("relationsInfoForRdf3x.ser");
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

}
