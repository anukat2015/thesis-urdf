package urdf.ilp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import edu.emory.mathcs.backport.java.util.Arrays;
import rcaller.RCaller;

import urdf.ilp.DataSet;
import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;

public class RuleWithNumericLiteralLearner {


	private RCaller caller;
	private QueryHandler queryHandler;
	private ThresholdChecker tChecker;
	private RelationsInfo info;
	
	private double[] span = {0.75};
	private double[] entropyRatioThreshold = {0.97};
	private double[] accuracyThreshold = {1.0};
	private int[] supportThreshold = {1};
	private double[] outliers = {0.25};
	private int[] plotResolution = {100};
	private int[] numBuckets = {100};
	
	
	
	@SuppressWarnings("deprecation")
	public RuleWithNumericLiteralLearner(QueryHandler queryHandler,ThresholdChecker tChecker, RelationsInfo info) {
		this.caller = new RCaller();
	    this.caller.setRscriptExecutable("/home/adeoliv/Downloads/R-2.15.1/bin/Rscript");
	    this.caller.cleanRCode();
	    this.caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/utils.r");
		this.info = info;
		this.queryHandler = queryHandler;
		this.tChecker = tChecker;
		if (tChecker!=null){
			this.accuracyThreshold[0] = tChecker.confidenceThreshold;
			this.supportThreshold[0] = tChecker.positivesCoveredThreshold;
		}
		
	}
	
	public void setSpan(double span) {
		this.span[0] = span;
	}
	
	public void setNumberOfBuckets(int n) {
		this.numBuckets[0] = n;
	}
	
	private DataSet extractDataDistribution(ResultSet rs) throws SQLException {
		DataSet ds = new DataSet(1);
		
		double x = Double.NaN;
		double y = Double.NaN;
		double w = Double.NaN;
		double s = Double.NaN;
		while (rs!=null && rs.next()) {
			x = rs.getFloat(1);
			w = rs.getFloat(2);
			ds.addDataPoint(x, y, w, s);
		}
		return ds;
	}
	
	private DataSet extractRegressionDataSet(ResultSet rs) throws SQLException {
		int xDim = rs.getMetaData().getColumnCount() - 2;
		if (xDim<=0 || !rs.getMetaData().getColumnName(xDim+1).equals("match")) 
			throw new SQLException("Invalid ResultSet format, it shoudl be X^n, match, count");
		
		DataSet ds = new DataSet(xDim);
		
		boolean firstRow = true;
		double[] x = new double[xDim];
		double[] value = new double[xDim];
		double y = Double.NaN;
		double w = Double.NaN;
		double s = Double.NaN;
		double pos = 0, neg = 0;

		while (rs!=null && rs.next()) {		
			
			for (int i=0; i<xDim; i++) 
				value[i] = rs.getFloat(i+1);
			boolean match = rs.getInt("match")==1;
			double count = rs.getFloat("count");
			
			if (!Arrays.equals(x, value) && !firstRow) {
				w = pos+neg;
				y = pos/(w);
				s = pos;
				//if (w>1 && y>0 && y<1)
				ds.addDataPoint(x, y, w, s);
				pos = neg = 0;				
			}
			
			firstRow = false;
			x = value.clone();
			if (match)
				pos = count;
			else
				neg = count;
		}
		return ds;
	}
	
	public synchronized boolean evaluateRule2D(RuleTreeNode node1, RuleTreeNode node2) throws SQLException, IOException {
		Rule rule1 = node1.getRule();
		Rule rule2 = node2.getRule();
		Rule newRule = rule1.clone();

		int literalArg1 = newRule.getLastBodyLiteral().getSecondArgument();
		int literalArg2 = newRule.getNextVariableNumber();
		Literal newLiteral = rule2.getLastBodyLiteral();
		newLiteral.setSecondArgument(literalArg2);
		newRule.addLiteral(newLiteral);
		
		
		String body = newRule.getBodyPatterns();
		body = body.substring(0,body.length()-2);
		String query = "SELECT COUNT ?"+(char)literalArg1+" ?"+(char)literalArg2+" WHERE {{"+ body + "} match {" + newRule.getHeadPattern() + "}} ORDER BY ASC(?"+(char)literalArg1+") ASC(?"+(char)literalArg2+") DESC(?match)";
		ResultSet rs = queryHandler.executeQuery(query);
		
		DataSet ds = extractRegressionDataSet(rs);
		
		if (ds.totalSupport()<numBuckets[0] || ds.size()<numBuckets[0]/2)
			return false;
		
		System.out.println("Positives:" + ds.totalSupport() + " Facts:" + ds.totalWeight() + " Points:" + ds.size());
		System.out.println(ds.toString());
		
		span[0] = 0.75;
		
		caller.addDoubleArray("x1", ds.getX(0));
		caller.addDoubleArray("x2", ds.getX(1));
	    caller.addDoubleArray("y", ds.getY());
	    caller.addDoubleArray("w", ds.getWeights());
	    caller.addDoubleArray("s", ds.getSupports());
	    caller.addDoubleArray("span", span);
	    
	    File file = caller.startPlot();
	    
	    caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/indep.r");
	    //caller.runAndReturnResult("ratio");
	    //double[] ratio = caller.getParser().getAsDoubleArray("ratio");
	    //System.out.println(ratio[0]);
	
	    caller.endPlot();
	    caller.runOnly();
	    
	    caller.cleanRCode();
	    caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/utils.r");

	 
	    
	    File plot = new File("rules/plots/"+rule1.getRuleString()+"&&"+rule2.getRuleString()+"["+numBuckets[0]+"].png");
	    FileUtils.copyFile(file, plot);

		return false;
	}
	
	@SuppressWarnings("deprecation")
	public synchronized boolean evaluateRule(RuleTreeNode node, Rule rule, int literalArg) throws SQLException, IOException {
		
		ResultSet rs;
		DataSet ds = null, dsHead = null;
		
		rs = queryHandler.retriveLiteralDistribution(rule, literalArg);
		ds = extractRegressionDataSet(rs);
		
		/*try {
			rs = queryHandler.retrieveHeadOfNumericConstant(rule, literalArg);
			dsHead = extractDataDistribution(rs);
		} catch (IllegalArgumentException e) {}
		*/
		
		
		span[0] = 0.75;
		entropyRatioThreshold[0] = 0.97;
		outliers[0] = 0.0;
		plotResolution[0] = 100;
		numBuckets[0] = 100;
		accuracyThreshold[0] = 0.35;
		double headSize[] = {rule.getHeadSize()};
		supportThreshold[0] = Math.max(supportThreshold[0], (int) Math.ceil(headSize[0]*tChecker.supportThreshold));
		
		if (ds.totalSupport()<numBuckets[0] || ds.size()<numBuckets[0]/2)
			return false;
		
		System.out.println("Positives:" + ds.totalSupport() + " Facts:" + ds.totalWeight() + " Points:" + ds.size());
		System.out.println(ds.toString());
			
		numBuckets[0] = 25;
		
		
		
		String[] title = new String[1];
		title[0] = rule.getRuleString();
		
		caller.addDoubleArray("x", ds.getX(0));
	    caller.addDoubleArray("y", ds.getY());
	    caller.addDoubleArray("w", ds.getWeights());
	    caller.addDoubleArray("s", ds.getSupports());
	    //if (dsHead!=null) {
		//    caller.addDoubleArray("xHead", dsHead.getX(0));
		//    caller.addDoubleArray("wHead", dsHead.getWeights());
	    //}
    	caller.addDoubleArray("headSize", headSize);
	    caller.addDoubleArray("span", span);
	    caller.addDoubleArray("entropyThreshold", entropyRatioThreshold);
	    caller.addDoubleArray("accuracyThreshold", accuracyThreshold);
	    caller.addIntArray("supportThreshold", supportThreshold);
	    caller.addDoubleArray("outliers", outliers);
	    caller.addStringArray("title", title);
	    caller.addIntArray("numTestPoints",plotResolution);
	    caller.addIntArray("numBuckets",numBuckets);
	     

	    File file = caller.startPlot();
	    
	    caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/rcaller.r");
	    //caller.runAndReturnResult("ratio");
	    //double[] ratio = caller.getParser().getAsDoubleArray("ratio");
	    //System.out.println(ratio[0]);
	
	    caller.endPlot();
	    caller.runOnly();
	    
	    caller.cleanRCode();
	    caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/utils.r");

	 
	    
	    File plot = new File("rules/plots/"+rule.getRuleString()+"["+numBuckets[0]+"].png");
	    FileUtils.copyFile(file, plot);
	    
	    
			    
		rule.setIsGood(true);
		rule.setHasNumericConstant(true);
		RuleTreeNode newNode = new RuleTreeNode(rule, node, node.getDepth()+1);
		newNode.attachVariableDistribution(literalArg, ds);
		node.addChild(newNode);
		
		return true;
		
	}
	
	public static void main(String[] args) throws Exception {
		Connection connPartition = Driver.connect("src/rdf3x-data91.properties");
		QueryHandler qh = new QueryHandler(connPartition);
		
		//String query = "select count ?inc ?v1 ?v2 ?v3 ?v4 ?v5 ?v6 ?v7 ?v8 ?v9 ?v10 ?v11 ?v12 ?v13 ?v14 ?v15 ?v16 where {?p <http://data-gov.tw.rpi.edu/vocab/p/91/pincp> ?inc . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/sex> ?v1. ?p <http://data-gov.tw.rpi.edu/vocab/p/91/st> ?v2. ?p <http://data-gov.tw.rpi.edu/vocab/p/91/sch> ?v3 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/rel> ?v4. ?p <http://data-gov.tw.rpi.edu/vocab/p/91/racwht> ?v5 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/racblk> ?v6 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/racasn> ?v7 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/qtrbir> ?v8 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/oc> ?v9 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/nativity> ?v10 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/mar> ?v11 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/lanp> ?v12 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/esr> ?v13 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/cit> ?v14 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/dphy> ?v15 . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/schl> ?v16} order by asc(?inc)";
		String query = "select count ?vps ?age where  {?p <http://data-gov.tw.rpi.edu/vocab/p/91/pwgtp> ?age . ?p <http://data-gov.tw.rpi.edu/vocab/p/91/pincp> ?vps} order by desc(?vps) asc(?age)";
		ResultSet rs = qh.executeQuery(query);
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/var/tmp/wei-inc.csv")));
		long t=System.currentTimeMillis();
		while (rs!=null && rs.next()) {
			bw.write(rs.getRowString()+"\n");
		}
		System.out.println("Done");
		bw.close();
		System.out.println(System.currentTimeMillis()-t);
		
		/*RuleWithNumericLiteralLearner learner = new RuleWithNumericLiteralLearner(qh, null, null);
		DataSet ds = learner.extractRegressionDataSet(rs);
		
		/*try {
			rs = queryHandler.retrieveHeadOfNumericConstant(rule, literalArg);
			dsHead = extractDataDistribution(rs);
		} catch (IllegalArgumentException e) {}
		*//*
		
		double[] span = {0.75};
		double[] entropyRatioThreshold = {0.97};
		double[] accuracyThreshold = {1.0};
		double[] outliers = {0.25};
		int[] plotResolution = {100};
		int[] numBuckets = {100};
		span[0] = 0.75;
		entropyRatioThreshold[0] = 0.97;
		outliers[0] = 0.0;
		plotResolution[0] = 100;
		numBuckets[0] = 100;
		accuracyThreshold[0] = 0.35;

		
		//if (ds.totalSupport()<numBuckets[0] || ds.size()<numBuckets[0]/2)
		//	return;
		
		System.out.println("Positives:" + ds.totalSupport() + " Facts:" + ds.totalWeight() + " Points:" + ds.size());
		System.out.println(ds.toString());
			
		numBuckets[0] = 10;
		
		
		
		String[] title = new String[1];
		title[0] = query;
		
		RCaller caller = new RCaller();
	    caller.setRscriptExecutable("/home/adeoliv/Downloads/R-2.15.1/bin/Rscript");
	    caller.cleanRCode();
	    caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/utils.r");
		caller.addDoubleArray("x", ds.getX(0));
	    caller.addDoubleArray("y", ds.getY());
	    caller.addDoubleArray("w", ds.getWeights());
	    caller.addDoubleArray("s", ds.getSupports());
	    //if (dsHead!=null) {
		//    caller.addDoubleArray("xHead", dsHead.getX(0));
		//    caller.addDoubleArray("wHead", dsHead.getWeights());
	    //}
    	//caller.addDoubleArray("headSize", headSize);
	    caller.addDoubleArray("span", span);
	    caller.addDoubleArray("entropyThreshold", entropyRatioThreshold);
	    caller.addDoubleArray("accuracyThreshold", accuracyThreshold);
	    caller.addDoubleArray("outliers", outliers);
	    caller.addStringArray("title", title);
	    caller.addIntArray("numTestPoints",plotResolution);
	    caller.addIntArray("numBuckets",numBuckets);
	     

	    File file = caller.startPlot();
	    
	    caller.R_source("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/rcaller.r");
	    //caller.runAndReturnResult("ratio");
	    //double[] ratio = caller.getParser().getAsDoubleArray("ratio");
	    //System.out.println(ratio[0]);
	    caller.endPlot();
	    caller.showPlot(file);
	    
	    caller.runOnly();
	    
	    File plot = new File("/home/adeoliv/Desktop/plot.png");
	    FileUtils.copyFile(file, plot);

	    
		
		return;*/
	}
	

}
