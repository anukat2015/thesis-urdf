package urdf.cl;

import urdf.ilp.Literal;
import urdf.ilp.Relation;

public class HeadLiteral extends Literal{

	public HeadLiteral(Relation relation, int firstArgument, int secondArgument) {
		super(relation, firstArgument, secondArgument);
	}

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean equals(Object o) {
		try {
			Literal lit = (Literal) o;
			if (!this.getRelation().equals(lit.getRelation()))
				return false;
			else
				if ((this.getSecondArgument()==-1 && lit.getSecondArgument()!=-1) || (this.getSecondArgument()!=-1 && lit.getSecondArgument()==-1))
					return false;
				else 
					if (this.getSecondArgument()==-1 && lit.getSecondArgument()==-1 && !this.getConstant().equals(lit.getConstant()))
						return false;
					
		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int code = this.getRelation().hashCode();
		if (this.getSecondArgument()==-1)
			code += this.getConstant().hashCode();
		return code;
	}

}
