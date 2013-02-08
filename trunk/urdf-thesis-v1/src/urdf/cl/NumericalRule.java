package urdf.cl;

import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import urdf.ilp.LearningManager;
import urdf.ilp.Literal;
import urdf.ilp.Rule;

public class NumericalRule extends Rule implements Comparable{
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(LearningManager.loggerName);
	
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
		String s = this.getHead().toString() + " <- ";
		for (int i=0,len=this.getBodyLiterals().size();i<len;i++) 
			s += this.getBodyLiterals().get(i).toString();
		
		return s;
	}
	
	public boolean hasGainComparedToGeneralizations(float accuracyThreshold, int supportThreshold) {
		logger.log(Level.DEBUG,"checknig GAIN from "+this.getRuleString()+" compared to:");
		for (NumericalRule r: generalizations) {
			if ((r.getBodyLiterals().size()+1)==this.getBodyLiterals().size()) {
				logger.log(Level.DEBUG,r.getRuleString());
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
		else
			return false;
	}
	

}
