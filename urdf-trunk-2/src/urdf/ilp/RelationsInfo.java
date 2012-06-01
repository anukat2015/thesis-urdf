package urdf.ilp;



import java.io.Serializable;
import java.util.ArrayList;
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
public class RelationsInfo implements Serializable
{
	
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
	
	public RelationsInfo(ArrayList<Relation> relations,ArrayList<Type> types, 
			HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg2,HashMap<Relation,ArrayList<Relation>> arg1JoinOnArg1,
			HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg2,HashMap<Relation,ArrayList<Relation>> arg2JoinOnArg1,
			HashMap<String,Integer> dangerousRelations)
	{
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
	public Type getTypeFromTypes(String typeName)
	{
		return types.get(typeName);
	}
	public Relation getRelationFromRelations(String relationName)
	{
		return relations.get(relationName);
	}
	public Hashtable<String,Relation> getAllRelations()
	{
		return this.relations;
	}
	public Hashtable<String,Type> getAllTypes()
	{
		return this.types;
	}

}
