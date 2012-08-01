package urdf.ilp;



import java.sql.SQLException;
import java.util.ArrayList;

import javatools.administrative.Announce.Level;


/**
 * @author Christina Teflioudi
 *
 */
public class Rule implements Cloneable{
	
	private Literal head;
	private int inputArg;
	
	private int examplesCovered = -1;			// N(c)=N+(c)+N-(c)
	private int positivesCovered = -1;	  		// N+(c)
	private int possiblePosToBeCovered = -1;	// E+(c)
	private int headSize = -1;
	private int bodySize = -1;
	public float bodyAvgMult = -1;
	public float bodyIdealMult = -1;
	
	private int constantInArg = 0;
	private String constant = null;
	
	
	private float confidence = 0;				
	private float support = 0;				// (N+(c) or N(c))/Size(head)
	private float specialityRatio;			// (N+(c)/Size(body))
	public float origConf;
	public float headConf;
	private float ratio = -1;
	
	public float missingHeadFactsOnlyHead;
	public float missingHeadFactsHeadBody;
	public float missingBodyFacts;

	private double gain = -1;// if -1 this is the first rule, so do not prune it

	private boolean hasFreeVariables = true; //A rule can have freeVariables but at the same time it can also bind the head variables in the body (if a rule binds the head variables, all its children do also.)
	private boolean bindsHeadVariables = false; //if a rule binds the head variables, it makes sense to calculate confidence, otherwise it doesn't
	private boolean isGood = true;	// if the rule passes the confidence threshold and is better in accuracy than its parents, isGood=true
	private boolean isInBeam = false;
	private boolean isTooGeneral = false;
	
	private int numOfFreeVariables = 2;
	
	ArrayList<Literal> bodyLiterals = new ArrayList<Literal>();
	
	public Rule (Literal head, int inputArg) {
		this.head=head;
		this.inputArg=inputArg;
	}
	
	//***************** GET METHODS WHICH CALCULATES VALUE IF NOT INITIALIZED ****************
 	public int getPossiblePositivesToBeCovered(QueryHandler qh) {
 		if (this.possiblePosToBeCovered < 0) {
 			try {
				this. possiblePosToBeCovered = qh.calculatePossiblePositivesToBeCovered(this, inputArg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.possiblePosToBeCovered;
 	}
 	
	public int getPositivesCovered(QueryHandler qh) {
 		if (this.positivesCovered < 0) {
 			try {
				this. positivesCovered = qh.calculatePositivesCovered(this, inputArg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.positivesCovered;
	}
	
	public int getExamplesCovered(QueryHandler qh) {
 		if (this.examplesCovered < 0) {
 			try {
				this. examplesCovered = qh.calculateExamplesCovered(this, inputArg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.examplesCovered;
	}
	
	public int getBodySize(QueryHandler qh) {
 		if (this.bodySize < 0) {
 			try {
				this. bodySize = qh.calculateBodySize(this, inputArg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.bodySize;
	}
	
	public int getHeadSize(QueryHandler qh) {
 		if (this.headSize < 0) {
 			try {
				this.headSize = qh.calculateHeadSize(this);
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.headSize;
	}
	
	public float getBodyAvgMult(QueryHandler qh) {
		if (this.bodyAvgMult < 0) {
 			try {
 				float avgvar[] = qh.calculateBodyAvgMultAndVar(this, inputArg);
				this.bodyAvgMult = avgvar[0];
				float standardDeviation = (float) Math.sqrt(avgvar[1]);
				this.bodyIdealMult = this.bodyAvgMult + standardDeviation;
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.bodyAvgMult;
	}
	
	public float getRatio(QueryHandler qh) {
		if (this.ratio < 0) {		
			this.ratio = ((float)this.getPositivesCovered(qh)) / ((float)this.getPossiblePositivesToBeCovered(qh));
			if (head.getRelation().getMult(inputArg)==1.0)
				ratio = (float) 0.0;
			System.out.println("Ration="+ratio+"="+this.getPositivesCovered()+"/"+this.getPossiblePositivesToBeCovered());
		}
		return ratio;
	}
	
	public float getBodyIdealMult(QueryHandler qh) {
		if (this.bodyIdealMult < 0) {
 			try {
 				//TODO Do this shit porperly
 				int adequateInputArg;
 				if (inputArg!=1 && inputArg!=2) {
 					if (head.getRelation().getMult1() > head.getRelation().getMult2()) 
 						adequateInputArg = 1;
 					else
 						adequateInputArg = 2;				
 				}
 				else {
 					adequateInputArg = this.inputArg;
 				}
 					
 				float avgvar[] = qh.calculateBodyAvgMultAndVar(this, adequateInputArg);
				this.bodyAvgMult = avgvar[0];
				float standardDeviation = (float) Math.sqrt(avgvar[1]);
				this.bodyIdealMult = this.bodyAvgMult + standardDeviation;
			} catch (SQLException e) {
				e.printStackTrace();
			}
 		}
 		return this.bodyIdealMult;
	}
	
	//***************** GET METHODS ****************
 	
 	public int getPossiblePositivesToBeCovered() {
 		return this.possiblePosToBeCovered;
 	}
 	
	public int getPositivesCovered() {
		return this.positivesCovered;
	}
	
	public int getExamplesCovered() {
		return this.examplesCovered;
	}
	
	public int getBodySize() {
		return this.bodySize;
	}
	
	public int getHeadSize() {
		return this.headSize;
	}

	public float getSupport() {
		return this.support;
	}
	
	public float getConfidence() {
		return this.confidence;
	}
	
	public float getSpecialityRatio() {
		return this.specialityRatio;
	}
	
	public double getGain() {
		return this.gain;
	}
	
	public boolean isInBeam() {
		return this.isInBeam;
	}

	public boolean hasFreeVariables() {
		return this.hasFreeVariables;
	}
 	
	public boolean bindsHeadVariables() {
		return this.bindsHeadVariables;
	}
	
	public boolean isGood() {
		return this.isGood;
	}	
	
	public boolean isTooGeneral() {
		return this.isTooGeneral;
	}
		
	public ArrayList<Literal> getBodyLiterals() {
		return this.bodyLiterals;
	}
	
	public Literal getHead() {
		return this.head;
	}
	
	public int getConstantInArg() {
		return this.constantInArg;
	}
	
	public String getConstant() {
		return this.constant;
	}


	//***************** SET METHODS ****************
	public void setConstantInArg(int arg) {
		if (arg!=this.constantInArg) {
			this.constantInArg = arg;
			this.headSize = -1;
		}
	}
	
	public void setConstant(String constant) {
		if (constant!=this.constant) {
			this.constant = constant;
			this.headSize = -1;
			head.setConstant(constant);
		}
	}
	
	public void setOrigConf(float val) {
		this.origConf=val;
	}
	
	public void setHeadConf(float val) {
		this.headConf=val;
	}
	
 	public void setExamplesCovered(int groundings) {
		this.examplesCovered=groundings;
	}
 	
 	public void setPossiblePositivesToBeCovered(int groundings) {
 		this.possiblePosToBeCovered=groundings;
 	}
 	
	public void setPositivesCovered(int groundings) {
		this.positivesCovered=groundings;
	}
	
	public void setHeadSize(int size) {
		this.headSize=size;
	}
	
	public void setBodySize(int size) {
		this.bodySize=size;
	}
	
	public void setSupport(float supp) {
		this.support=supp;
	}
	
	public void setConfidence(float conf) {
		this.confidence=conf;		
	}	
	
	public void setIsGood(boolean flag) {
		this.isGood=flag;
	}
	
	public void setIsInBeam(boolean flag) {
		this.isInBeam=flag;
	}
	
	public void setGain(double gain) {
		this.gain=gain;
	}
	
	public void setSpecialityRatio(float ratio) {
		this.specialityRatio=ratio;
	}
	
	public void setIsTooGeneral(boolean flag) {
		this.isTooGeneral=flag;
	}
	
	public void setHasFreeVariables(boolean flag) {
		this.hasFreeVariables=flag;
	}
	
	// *************** OTHER METHODS *****************
  	
	@Override
	public boolean equals(Object obj) {
		Rule r = (Rule) obj;
		return this.equals(r);
	}
	
	public boolean equals(Rule rule) {
		boolean flag=false;
		if (!this.head.equals(rule.head))
			return false;
		
		if (this.bodyLiterals.size()!=rule.bodyLiterals.size())
			return false;
		
		for (int i=0,len=this.bodyLiterals.size();i<len;i++) {
			
			flag=false;
			for (int j=0;j<rule.bodyLiterals.size();j++) {
				if(this.bodyLiterals.get(i).equals(rule.bodyLiterals.get(j))) {
					flag=true;
					break;
				}
			}
			if (!flag)
				return false;
			
		}	
		return true;
	}
	
	/**
	 * @param literal: the literal to be add to the rule
	 * @param position: the index in bodyLiterals of the literal on which the new Literal is connected
	 */
	public void addLiteral(Literal literal,int position) {
		//reset statistics as body is changed
		resetStatistics();
		
		this.bodyLiterals.add(literal);
		
		// set the flags bindsHeadVariables and hasFreeVariables
		setFlags();
		
		// repair the literal on which the new literal is connected. Check if it had a free variable and now it is connected
		updatePreviousLiteralForFreeVars(position,literal);
	}
	/**
	 * @param literal: the literal to be add to the rule
	 *  To be used only for adding auxiliary relations in a rule e.g. NEQ
	 *  
	 *  CHECK AGAIN FOR EQ and constants
	 */
	public void resetStatistics() {
		this.bodySize = -1;
		this.examplesCovered = -1;			// N(c)=N+(c)+N-(c)
		this.positivesCovered = -1;	  		// N+(c)
		this.possiblePosToBeCovered = -1;	// E+(c)	
		this.confidence = 0;				
		this.support = 0;					// (N+(c) or N(c))/Size(head)
		this.specialityRatio = 0;			// (N+(c)/Size(body))
		this.ratio = -1;
		this.bodyAvgMult = -1;
		this.bodyIdealMult = -1;
	}
	
	public void addLiteral(Literal literal) {
		//reset statistics as body is changed
		resetStatistics();
		
		this.bodyLiterals.add(literal);

		// set the flags bindsHeadVariables and hasFreeVariables
		setFlags();	
	}
	
	public String getRuleString() {
		String s=head.getRelation().getSimpleName()+"("+(char)head.getFirstArgument()+","+(char)head.getSecondArgument()+")<-";		
		for (int i=0,len=this.bodyLiterals.size();i<len;i++) {
			s += this.bodyLiterals.get(i).getRelation().getSimpleName()+
				 "("+
					(char)this.bodyLiterals.get(i).getFirstArgument()+ ","+
					(this.bodyLiterals.get(i).getSecondArgument()>0?(char)this.bodyLiterals.get(i).getSecondArgument():this.bodyLiterals.get(i).getConstant())
				 +")";
		}
		return s;
	}
	
	public String getConfSuppSpec() {
		String s =  "confidence: "+confidence+
					" support: "+support+
					" specialityRatio: "+specialityRatio;
		return s;
	}
	
	public String getExamplesStats() {
		String s = "N+(c): "+positivesCovered+
				 // " E+(c): "+possiblePosToBeCovered+
				 // " N(c): "+examplesCovered+
				  " B(c): "+bodySize+
				  " E+: "+headSize;
		return s;
	}
	
	/**
	 * Prints the rule
	 * @param ruleOnly: if false, print also confidence and support, otherwise only the rule itself
	 */
	public String printRule(boolean ruleOnly) {				
		String s = getRuleString(); 
		if (!ruleOnly) { // printing for the simple case of confidence
			s += "\n" + getConfSuppSpec();
			s += "\n" + getExamplesStats();
		}
		return s;
	}
	
	public String printForParser() {
		String s=head.getRelation().getName()+"(?"+new Character((char)head.getFirstArgument()).toString()+",?"+new Character((char)head.getSecondArgument()).toString()+",1)<=";
		String relation;
		for (int i=0,len=this.bodyLiterals.size();i<len;i++) {
			
			relation=this.bodyLiterals.get(i).getRelation().getName();
			if (relation.equals("!="))
				relation="notEquals";
			else if (relation.equals("="))
				relation="equals";
			
			s+=relation+"(?"+new Character((char)this.bodyLiterals.get(i).getFirstArgument()).toString()+","+(this.bodyLiterals.get(i).getSecondArgument()>0?"?"+new Character((char)this.bodyLiterals.get(i).getSecondArgument()).toString():this.bodyLiterals.get(i).getConstant())+",1);";
		}
		s+="["+this.confidence+"]";		
		
		return s;
	}
	
	public Rule clone() {
	    Rule cloned;
		try {
			cloned = (Rule)super.clone();
			cloned.head = (Literal)head.clone();
		    cloned.bodyLiterals=new ArrayList<Literal>();
		    cloned.bindsHeadVariables=bindsHeadVariables;
		    cloned.isTooGeneral=isTooGeneral;
		    cloned.headSize=headSize;
		    cloned.constantInArg=constantInArg;
		    cloned.constant=constant;
		    
		    for (int i=0, len=bodyLiterals.size();i<len;i++)
		    	cloned.bodyLiterals.add(bodyLiterals.get(i).clone());
		    
		    return cloned;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public int getNextVariableNumber() {
		int max=66;
		if (bodyLiterals.size()>0) {
			for (int i=0,len=bodyLiterals.size();i<len;i++) {
				if (bodyLiterals.get(i).getFirstArgument()>max)
					max=bodyLiterals.get(i).getFirstArgument();
				if (bodyLiterals.get(i).getSecondArgument()>max)
					max=bodyLiterals.get(i).getSecondArgument();
			}
			max++;
			return max;
		}
		return 67;// head(65,66)		
	}
	
	/**
	 * 	checks the condition of the rule after each addition of a literal and sets the flags: 
	 * 		bindsHeadVariables
	 *  	hasFreeVariables
	 *  accordingly
	 */
	private void setFlags() {	
		boolean exist1st=false,exist2nd=false;
		// first check for binding the head variables
		if (!this.bindsHeadVariables) {
			
			this.isInBeam=true; // to be change for relational info gain
			
			for (int i=0,len=bodyLiterals.size();i<len;i++) {
				if (bodyLiterals.get(i).getRelation().isAuxiliary())
					continue;

				if(bodyLiterals.get(i).getFirstArgument()==66 ||bodyLiterals.get(i).getSecondArgument()==66) {
					exist2nd = true;
					if (exist1st) {
						this.bindsHeadVariables=true;
						break;
					}
					
				}
				if(bodyLiterals.get(i).getFirstArgument()==65 ||bodyLiterals.get(i).getSecondArgument()==65) {
					exist1st = true;
					if (exist2nd) {
						this.bindsHeadVariables = true;
						break;
					}
					
				}
			}
		}
		if (!this.bindsHeadVariables) {
			this.isGood = false;
		}

		// then check for free variables
		countNumberOfFreeVariables();
		if(numOfFreeVariables>0) {
			hasFreeVariables=true;
			this.isInBeam=true; // to be change for relational info gain
		}
		else{
			hasFreeVariables=false;
		}
	}
	/**
	 * @return the number of free variables in the rule
	 */
	public int getNumOfFreeVariables() {
		return this.numOfFreeVariables;
	}
	/**
	 * repair the literal on which the new literal is connected. Check if it had a free variable and now it is connected
	 * @param position: the index in bodyLiterals of the literal on which the new Literal is connected
	 * @param newLiteral
	 */
	private void updatePreviousLiteralForFreeVars(int position,Literal newLiteral) {
		Literal previousLiteral=(position==-1?head:bodyLiterals.get(position));
		
		// if the previous literal had a free variable check if it is now connected
		switch (previousLiteral.getFreeVariable()) {
			case 1:
				if (previousLiteral.getFirstArgument()==newLiteral.getFirstArgument() ||previousLiteral.getFirstArgument()==newLiteral.getSecondArgument())
					previousLiteral.setFreeVariable(0);
				break;
			case 2:
				if (previousLiteral.getSecondArgument()==newLiteral.getFirstArgument() ||previousLiteral.getSecondArgument()==newLiteral.getSecondArgument())
					previousLiteral.setFreeVariable(0);
				break;
			default: // 0
				// do nothing
		}
		
	}

	/**
	 * counts the number of free variables in the rule and stores the result in
	 * this.numOfFreeVariables
	 */
	private void countNumberOfFreeVariables() {		
		
		ArrayList<Integer> freeVariables=new ArrayList<Integer>();
		ArrayList<Integer> bindedVariables=new ArrayList<Integer>();
		int count=0;
		freeVariables.add(65);
		freeVariables.add(66);

		for (int i=0,len=bodyLiterals.size();i<len;i++) {
			
			if (bodyLiterals.get(i).getRelation().isAuxiliary())
				continue;
	
			if (freeVariables.contains(bodyLiterals.get(i).getFirstArgument()))
				bindedVariables.add(bodyLiterals.get(i).getFirstArgument());
			else
				freeVariables.add(bodyLiterals.get(i).getFirstArgument());
			
			if (freeVariables.contains(bodyLiterals.get(i).getSecondArgument()))
				bindedVariables.add(bodyLiterals.get(i).getSecondArgument());
			else
				freeVariables.add(bodyLiterals.get(i).getSecondArgument());
		}
		
		for (int i=0, len=freeVariables.size();i<len;i++)
			if (!bindedVariables.contains(freeVariables.get(i)))
				count++;
		
		this.numOfFreeVariables=count;
	}
	
	public String getHeadPattern() {
		String pattern = head.getSparqlPattern();
		switch (constantInArg) {
			case 1: return pattern.replace(head.getFirstArgumentVariable(), this.constant);
			case 2: return pattern.replace(head.getSecondArgumentVariable(), this.constant);
			default: return head.getSparqlPattern();
		}
	}
	
	public String getBodyPatterns() {
		String patterns = "";
		// RDF3x requires filter patterns to be at the end
		int firstAuxIndex = -1;
		int countNEQ = 0;
		for (int i=0; i<bodyLiterals.size(); i++) {
			Literal literal = bodyLiterals.get(i);
			if (literal.getRelation().isAuxiliary()) {
				if (firstAuxIndex < 0) 
					firstAuxIndex = i;
				if (literal.getRelation().equals(RelationsInfo.NEQ))
					countNEQ++;
			} 
			else
				patterns += literal.getSparqlPattern();
		}
		boolean existNEQ = false;
		if (firstAuxIndex >= 0) {
			for (int i=firstAuxIndex; i<bodyLiterals.size(); i++) {
				Literal literal = bodyLiterals.get(i);
				if (literal.getRelation().isAuxiliary())
					// If relation is EQ with a constant in second argument, substitute argument by variable instead of add filter clause
					if (literal.getRelation().equals(RelationsInfo.EQ) && (literal.getSecondArgument() < 0)) {
						patterns = patterns.replaceAll("\\"+literal.getFirstArgumentVariable(), literal.getConstant());
					}
					else
						if (literal.getRelation().equals(RelationsInfo.NEQ)) {
							if (existNEQ == true && literal.getConstant()==null) {
								patterns = patterns.replaceAll("\\"+literal.getFirstArgumentVariable(), literal.getSecondArgumentVariable());
							}
							else {
								patterns += literal.getSparqlPattern();							
								existNEQ = true;
							}
						}
						else
							patterns += literal.getSparqlPattern();
			}
		}


		return patterns;
	}
	
	public String positivesCoveredQuery() {
		String patterns = getHeadPattern();
		patterns += getBodyPatterns();
		patterns = patterns.substring(0,patterns.length()-2);
		return "SELECT COUNTDISTINCT ?A ?B WHERE {"+patterns+"}";
	}
	
	public String examplesCoveredQuery(int inputArg) {
		if (constantInArg!=0)
			throw new IllegalArgumentException("Examples Covered should doesn't apply for rules with constant case (it's same as Body Size)");
		String patterns = head.getSparqlPattern(inputArg);
		patterns += getBodyPatterns();
		patterns = patterns.substring(0,patterns.length()-2);
		return "SELECT COUNTDISTINCT "+head.getFirstArgumentVariable()+" "+head.getSecondArgumentVariable()+" WHERE {"+patterns+"}";
		
	}
	
	public String possiblePositivesToBeCoveredQuery(int inputArg) {
		if (constantInArg!=0)
			throw new IllegalArgumentException("Possible Positives To Be Covered should doesn't apply for rules with constant case (it's same as Head Size)");
		
		String patterns = head.getSparqlPattern(inputArg);
		patterns += getBodyPatterns(); 
		patterns = patterns.substring(0,patterns.length()-2);
		switch (inputArg) {
			case 1:  return "SELECT COUNTDISTINCT "+head.getFirstArgumentVariable()+" ?free WHERE {"+patterns+"}";
			case 2:  return "SELECT COUNTDISTINCT ?free "+head.getSecondArgumentVariable()+" WHERE {"+patterns+"}";
			default: return "SELECT COUNTDISTINCT "+head.getFirstArgumentVariable()+" "+head.getSecondArgumentVariable()+" WHERE {"+patterns+"}";
		}
	}

	public String bodySizeQuery() {
		String patterns = getBodyPatterns();
		patterns = patterns.substring(0,patterns.length()-2);
		return "SELECT COUNTDISTINCT ?A ?B WHERE {"+patterns+"}" ;
	}
	
	public String findConstantsQuery(int constantInArg) {
		String patterns = head.getSparqlPattern();
		patterns += getBodyPatterns();
		patterns = patterns.substring(0,patterns.length()-2);
		String typeArg = "";
		switch (constantInArg) {
			case 1:  typeArg = head.getSecondArgumentVariable(); break;
			case 2:  typeArg = head.getFirstArgumentVariable(); break;
			default: throw new IllegalArgumentException("Illegal inputArg="+constantInArg+", it should be 1 or 2");
		}
		return "SELECT COUNT "+typeArg+" WHERE {"+patterns+"} ORDER BY DESC(COUNT)";
	}
}
