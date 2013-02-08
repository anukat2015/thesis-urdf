package urdf.cl;

import java.sql.SQLException;

import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.Rule;
import urdf.rdf3x.ResultSet;

public class RuleTester {
	private QueryHandler qh;
	private Histogram histogram;
	private Literal rootLiteral;
	private CorrelationLattice lattice;
	
	public RuleTester(QueryHandler qh, Histogram histogram, Literal rootLiteral) {
		this.qh = qh;
		this.histogram = histogram.clone();
		this.histogram.reset();
		this.rootLiteral = rootLiteral;
	}
	
	public RuleTester(QueryHandler qh, Histogram histogram, Relation rootRelation) {
		this.qh = qh;
		this.histogram = histogram.clone();
		this.histogram.reset();
		this.rootLiteral = new Literal(rootRelation, (int)'A', (int)'B');
	}
	
	
	public RuleTester(CorrelationLattice lattice, QueryHandler qh) {
		this.lattice = lattice;
		this.rootLiteral = lattice.getRootLiteral();
		this.qh = qh;
		this.histogram = lattice.getRoot().getHistogram().clone();
		this.histogram.reset();
	}
	
	public void testRule(NumericalRule rule, int minSupport, float minAccuracy) throws SQLException {
		//System.out.println("!!!"+rule.getRuleString()+"!!!");
		
		String prediction = rule.getHead().getConstant();
		int positivesCount = 0;
		int negativesCount = 0;
		
		String sparql = createTestQuery(rule);
		
		//System.out.println(sparql);
		
		long t = System.currentTimeMillis();
		ResultSet rs = (ResultSet) qh.executeQuery(sparql);
		//System.out.println("Executed in "+(System.currentTimeMillis()-t));
	
		
		float overallAcc = ((float)ArrayTools.sum(ArrayTools.multiply(rule.getAccuracyDistribution(),rule.getSupportDistribution()))) / ((float)ArrayTools.sum(rule.getSupportDistribution()));
		
		float[] acc = ArrayTools.getAccuraciesWithMinSupport(rule.getSupportDistribution(), rule.getBodySupportDistribution(), minSupport);
		
		String ranges="";
		
		for (int i=0; i<acc.length; i++) {
			if (acc[i]>=minAccuracy)
				ranges += "+";
			else
				ranges += "-";
		}
		//System.out.println(ranges);
		
		
		//ArrayTools.print(acc);
		
		float lastNum = Float.NaN;
		while (rs.next()) {
			float num = rs.getFloat(1);
			String observation = rs.getString(2).replaceAll("\"", "");
			int count = rs.getInt(3);
			
			int bucket = histogram.getBucket(num);
			if (acc[bucket]>=minAccuracy) {
				if (prediction.equals(observation)) 
					positivesCount += count;
				else 
					negativesCount += count;	
			}
			
			lastNum = num;
		}
		
		int coveringCount = positivesCount + negativesCount;
		float predictionAccuracy = ((float) positivesCount)/(((float) (coveringCount)));
		//System.out.println(rule.getRuleString() + "\n[" + predictionAccuracy + "," + coveringCount + "] = ("+positivesCount+"/"+negativesCount+")");
		rule.observedConfidence = predictionAccuracy;
		rule.observedPositives = positivesCount;
		rule.observedNegatives = negativesCount;
		rule.observedOverallConfidence = overallAcc;
		
	}
	
	public String createTestQuery(NumericalRule rule) {
		String sparql = "";
		
		int headSecondArg = (int)'H';
		
		String headPattern = "?"+(char)rule.getHead().getFirstArgument()+" "+rule.getHead().getRelationName()+" ?"+(char)headSecondArg+" . ";
		
		String bodyLiterals = "";
		for (Literal l :rule.getBodyLiterals())
			bodyLiterals += l.getSparqlPatternWithConstant() + " . ";
		// Add projection
		sparql += "SELECT COUNT ?"+(char)rootLiteral.getSecondArgument()+" ?"+(char)headSecondArg+" ";
		sparql += "WHERE {"+ headPattern + bodyLiterals + rootLiteral.getSparqlPattern() +"} ";
		sparql += "ORDER BY ASC(?"+(char)rootLiteral.getSecondArgument()+") ASC(?"+(char)headSecondArg+")";
		
		
		return sparql;
	}
}
