package urdf.arm;

import java.util.ArrayList;

import urdf.ilp.Literal;
import urdf.ilp.Rule;

public class NumericalRule extends Rule implements Comparable{
	private static final long serialVersionUID = 1L;
	
	public int observedPositives = -1;
	public int observedNegatives = -1;
	public float observedConfidence = Float.NaN; 
	public float observedOverallConfidence = Float.NaN;
	
	private int numOfBuckets;
	private int[] support;
	private float[] accuracy;
	private int[] bodySupport;
	public float overallAccuracy;
	public ArrayList<NumericalRule> generalizations = new ArrayList<NumericalRule>();
	
	public String bodyFilter = "";
	public String headFilter = "";
	
	public NumericalRule(Literal head, int inputArg) {
		super(head, inputArg);
	}

	public void setSupportAndAccuracyDistribution(int[] support, int[] bodySupport, float[] accuracy) {
		if (support.length!=accuracy.length || accuracy.length!=bodySupport.length) {
			throw new IllegalArgumentException("Support and accuracy arrays should have the same size");
		} else {
			this.numOfBuckets = support.length;
			this.support = support;
			this.bodySupport = bodySupport;
			this.accuracy = accuracy;
			this.overallAccuracy = ArrayTools.sum(accuracy);
		}
	}
	
	public int[] getSupportDistribution() {
		return this.support;
	}
	
	public int[] getBodySupportDistribution() {
		return this.bodySupport;
	}
	
	public float[] getAccuracyDistribution() {
		return this.accuracy;
	}
	
	@Override
	public String getRuleString() {
		String s = this.getHead().getRelation().getSimpleName()+"(";
		s += (char)this.getHead().getFirstArgument()+",";
		if (this.getHead().getSecondArgument()==-1) 
			s += this.getHead().getConstant()+")"+headFilter+"<-";
		else
			s += (char)this.getHead().getSecondArgument()+")"+headFilter+"<-";	
		
		for (int i=0,len=this.getBodyLiterals().size();i<len;i++) {
			s += this.getBodyLiterals().get(i).getRelation().getSimpleName()+
				 "("+
					(char)this.getBodyLiterals().get(i).getFirstArgument()+ ","+
					(this.getBodyLiterals().get(i).getSecondArgument()>0?(char)this.getBodyLiterals().get(i).getSecondArgument():this.getBodyLiterals().get(i).getConstant())
				 +")";
		}
		return s + bodyFilter;
	}
	
	public boolean hasGainComparedToGeneralizations(float accuracyThreshold, int supportThreshold) {
		System.out.println("checknig GAIN from "+this.getRuleString()+" compared to:");
		for (NumericalRule r: generalizations) {
			System.out.println(r.getRuleString());
			if (this.overallAccuracy > (r.overallAccuracy+0.05))
				continue;
			else {
				int countSignificanceSupport = 0;
				for (int i=0; i<accuracy.length; i++) {
					if (this.accuracy[i]>accuracyThreshold && this.accuracy[i]> (r.accuracy[i]+0.05)) {
						countSignificanceSupport += this.support[i];
					}
				}
				if (countSignificanceSupport < supportThreshold)
					return false;
			}
		}
		return true;
	}
	

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean isSpecializationOf(NumericalRule rule) {
		if (this.getHead().equals(rule.getHead())) {
			if (this.getBodyLiterals().size()!=(rule.getBodyLiterals().size()+1))
				return false;
			for (Literal l: rule.getBodyLiterals()) {
				if (!this.getBodyLiterals().contains(l)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	

}
