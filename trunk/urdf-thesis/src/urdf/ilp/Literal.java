package urdf.ilp;



/**
 * @author Christina Teflioudi *
 */

public class Literal implements Cloneable
{
	private static final String prefix = "<http://yago-knowledge.org/resource/";
	private static final String suffix = ">";
	
	private Relation relation;
	private char firstArgument;
	private char secondArgument;
	// modes for first and second arg. 0:new variable 1: old variable
	private int firstMode=0;
	private int secondMode=0;
	private String constant=null;
	private int freeVariable=0; // 0: no free variable in this literal 1: the firstArgument is a free variable 2: the secondArgument is a free variable
	
	
	public Literal(Relation relation, int firstArgument,int firstMode, int secondArgument,int secondMode, int freeVariable) throws Exception {
		if (firstArgument>0 && secondArgument>0 && (firstArgument<65 || firstArgument>90 ||secondArgument<65 || secondArgument>90))
			throw new Exception("argument numbers should be between 65 and 90");
		this.relation=relation;
		this.firstArgument = (char) firstArgument;
		this.firstMode=firstMode;
		this.secondArgument = (char) secondArgument;
		this.secondMode=secondMode;
		this.freeVariable=freeVariable;
		
	}
	
	public Literal(Relation relation, int firstArgument, int secondArgument) throws Exception {
		this(relation, firstArgument, 1, secondArgument, 1, 0);
	}
	
	public Literal(Relation relation, int firstArgument,int firstMode, int secondArgument,int secondMode,String constant) throws Exception {
		this(relation, firstArgument, firstMode, secondArgument, secondMode, 0);
		if (!(relation.equals(RelationsInfo.EQ)||relation.equals(RelationsInfo.GT)||relation.equals(RelationsInfo.LT)))
			throw new Exception("constant does not make sense for relation other than EQ,GT,LT");
		this.constant=constant;
	}
	
	public Literal clone()
	{
		Literal cloned;
		try {
			cloned = (Literal)super.clone();

		    return cloned;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean equals(Literal lit)
	{
		if (!this.relation.equals(lit.relation))
			return false;
		if (this.firstArgument!=lit.firstArgument)
			return false;
		if (this.secondArgument!=lit.secondArgument)
			return false;
		
		return true;
	}
	
	//***************** GET METHODS *****************
	public Relation getRelation(){
		return this.relation;
	}
	
	public int getFirstArgument(){
		return this.firstArgument;
	}
	
	public int getSecondArgument(){
		return this.secondArgument;
	}
	
	public String getFirstArgumentVariable(){
		return "?" + this.firstArgument;
	}
	
	public String getSecondArgumentVariable(){
		return "?" + this.secondArgument;
	}
	
	public String getRelationName(){
		return this.relation.getName();
	}
	
	public int getFirstMode(){
		return this.firstMode;
	}
	
	public int getSecondMode(){
		return this.secondMode;
	}
	
	public String getConstant(){
		return this.constant;
	}
	
	public int getFreeVariable(){
		return this.freeVariable;
	}

	//***************** SET METHODS *****************
	public void setFirstArgument(int arg) {
		this.firstArgument = (char) arg;
	}
	
	public void setSecondArgument(int arg) {
		this.secondArgument= (char) arg;
	}
	public void setFirstMode(int mode) {
		this.firstMode=mode;
	}
	
	public void getSecondMode(int mode) {
		this.secondMode=mode;
	}
	
	public void setConstant(String constant) {
		this.constant=constant;
	}
	
	public void setFreeVariable(int freeVariable) {
		this.freeVariable=freeVariable;
	}
	
	public String getRelationNameRdf3x() {
		String name = relation.getName();
		if (!name.contains(prefix)) 
			name = prefix + name + suffix;
		return name; 
	}
	
	public String getSparqlPattern() {
		return geSparqlPattern(0);
	}
	
	public String geSparqlPattern(int inputArg) {
		switch (inputArg) {
			case 1:  return "?" + firstArgument + " " + getRelationNameRdf3x() + " ?free . ";
			case 2:  return "?free " + getRelationNameRdf3x() + " ?" + secondArgument + " . ";
			default: return "?" + firstArgument + " " + getRelationNameRdf3x() + " ?" + secondArgument + " . ";
		}
	}
	

}
