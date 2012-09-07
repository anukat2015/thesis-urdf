
package urdf.ilp;


import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Christina Teflioudi *
 */
public class BodyPredicate 
{
	Relation relation;
	public  HashSet<Relation> arg1JoinOnArg2=new HashSet<Relation>();
	public  HashSet<Relation> arg1JoinOnArg1=new HashSet<Relation>();
	public  HashSet<Relation> arg2JoinOnArg2=new HashSet<Relation>();
	public  HashSet<Relation> arg2JoinOnArg1=new HashSet<Relation>();
	
	public BodyPredicate(Relation relation) {
		this.relation=relation;
	}
	
	public void addRelation(Relation relation, int joinCase) {
		switch(joinCase) {
			case 1: arg1JoinOnArg1.add(relation); break;
			case 2: arg1JoinOnArg2.add(relation); break;
			case 3: arg2JoinOnArg1.add(relation); break;
			case 4: arg2JoinOnArg2.add(relation); break;			
		}
	}
	
	public Relation getRelation() {
		return relation;
	}
	
	public boolean hasRelation(Relation rel) {
		if (this.relation.equals(rel))
			return true;
		return false;
	}
}
