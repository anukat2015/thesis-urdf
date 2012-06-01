package urdf.ilp;



import java.util.ArrayList;

/**
 * @author Christina Teflioudi 
 */

public class Clause implements Cloneable
{
	private Literal head;
	private ArrayList<Literal> literals;

	
	
	public Clause(Literal head, Literal literal)
	{
		this.head=head;
		this.literals=new ArrayList<Literal>();
		literals.add(literal);
	}
	public Clause(Literal head, ArrayList<Literal> literal)
	{
		this.head=head;
		this.literals=literal;
	}
	public void addLiteral(Literal literal)
	{
		this.literals.add(literal);
	}
	public boolean equals(Clause clause)
	{
		int count=0;
		if (!this.head.equals(clause.head))
			return false;
		for (int i=0, len=this.literals.size();i<len;i++)
		{
			for (int j=0, len1=clause.literals.size();j<len1;j++)
			{
				if (clause.literals.get(j).equals(this.literals.get(i)))
				{
					count++;
					break;
				}
			}
		}
		if (count<this.literals.size())
			return false;
		return true;
	}
	public Clause clone()
	{
	     Clause cloned;
		try {
			cloned = (Clause)super.clone();
			cloned.head = (Literal)head.clone();
		    cloned.literals=new ArrayList<Literal>();
		    
		    for (int i=0, len=literals.size();i<len;i++)
		    {
		    	cloned.literals.add(literals.get(i).clone());
		    }
		    
		    return cloned;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	    return null;
	}
  

	//************* GET METHODS ********************
	public Literal getLastLiteral()
	{
		return this.literals.get(literals.size()-1);
	}
	
	public ArrayList<Literal> getLiterals()
	{
		return this.literals;
	}
	public Literal getHead()
	{
		return this.head;
	}
	
	
	public void print()
	{
		String s=head.getRelation().getName()+"("+head.getFirstArgument()+","+head.getSecondArgument()+")<-";
		
		for (int i=0,len=this.literals.size();i<len;i++)
		{
			s+=this.literals.get(i).getRelation().getName()+"("+this.literals.get(i).getFirstArgument()+","+this.literals.get(i).getSecondArgument()+")";
			
		}
		
		System.out.println(s);
		
	}
	

	/**
	 * @return the next integer denoting a new variable in the rule, that could be used (unbinded variable)
	 */
	public int getNextVariableNumber()
	{
		return (this.getLastLiteral().getFirstArgument()<this.getLastLiteral().getSecondArgument()?this.getLastLiteral().getSecondArgument()+1:this.getLastLiteral().getFirstArgument()+1);
	}	
	
	
}
