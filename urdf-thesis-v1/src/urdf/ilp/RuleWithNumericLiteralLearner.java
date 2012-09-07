package urdf.ilp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import rcaller.RCaller;

import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;

public class RuleWithNumericLiteralLearner {
	public class DataSet {
		private ArrayList<Double> xList; // X values
		private ArrayList<Double> yList; // Accuracies
		private ArrayList<Double> wList; // Body Sizes
		private ArrayList<Double> sList; // Supports (positives)
		private double totalWeight;
		
		public DataSet() {
			xList = new ArrayList<Double>();
			yList = new ArrayList<Double>();
			wList = new ArrayList<Double>();
			sList = new ArrayList<Double>();
			totalWeight = 0;
		}

		private double[] toPrimitiveArray(ArrayList<Double> a) {
			double[] d = new double[a.size()];
			for (int i=0; i<a.size(); i++)
				d[i] = a.get(i).doubleValue();
			return d;
				
		}
		public void addDataPoint(double x, double y, double w, double s) {
			this.xList.add(x);
			this.yList.add(y);
			this.wList.add(w);
			this.sList.add(s);
			this.totalWeight += w;
		}
		public double[] getX() {
			return toPrimitiveArray(xList);
		}
		public double[] getY() {
			return toPrimitiveArray(yList);
		}
		public double[] getWeights() {
			return toPrimitiveArray(wList);
		}
		public double[] getSupports() {
			return toPrimitiveArray(sList);
		}
		public int size() {
			return xList.size();
		}
		public double totalWeight() {
			return totalWeight;
		}

	}
	

	private RCaller caller;
	private QueryHandler queryHandler;
	private ThresholdChecker tChecker;
	private RelationsInfo info;
	
	private double[] span = {0.75};
	private double[] entropyRatioThreshold = {0.97};
	private double[] accuracyThreshold = {1.0};
	private double[] outliers = {0.25};
	private int[] plotResolution = {100};
	private int[] numBuckets = {100};
	
	
	
	@SuppressWarnings("deprecation")
	public RuleWithNumericLiteralLearner(QueryHandler queryHandler,ThresholdChecker tChecker, RelationsInfo info) {
		this.caller = new RCaller();
	    this.caller.setRscriptExecutable("/home/adeoliv/Downloads/R-2.15.1/bin/Rscript");
	    this.caller.cleanRCode();
	    this.caller.R_source("/home/adeoliv/Documents/Thesis/r/utils.r");
		this.info = info;
		this.queryHandler = queryHandler;
		this.tChecker = tChecker;
		this.accuracyThreshold[0] = tChecker.confidenceThreshold;
	}
	
	public void setSpan(double span) {
		this.span[0] = span;
	}
	
	public void setNumberOfBuckets(int n) {
		this.numBuckets[0] = n;
	}
	
	private DataSet extractDataSet(ResultSet rs) throws SQLException {
		DataSet ds = new DataSet();
		
		boolean firstRow = true;
		double x = Double.NaN;
		double y = Double.NaN;
		double w = Double.NaN;
		double s = Double.NaN;
		double pos = 0;
		double neg = 0;
		while (rs.next()) {
			double value = rs.getFloat(1);
			boolean match = rs.getInt(2)==1;
			double count = rs.getFloat(3);
			
			if (value!=x && !firstRow) {
				w = pos+neg;
				y = pos/(w);
				s = pos;
				//if (w>1 && y>0 && y<1)
				ds.addDataPoint(x, y, w, s);
				pos = neg = 0;				
			}
			
			firstRow = false;
			x = value;
			if (match)
				pos = count;
			else
				neg = count;
		}
		return ds;
	}
	
	@SuppressWarnings("deprecation")
	public synchronized boolean evaluateRule(Rule rule, int literalArg) throws SQLException, IOException {
		
		ResultSet rs = queryHandler.retriveLiteralDistribution(rule, literalArg);
		
		DataSet ds = extractDataSet(rs);
		
		span[0] = 0.75;
		entropyRatioThreshold[0] = 0.97;
		outliers[0] = 0.0;
		plotResolution[0] = 100;
		numBuckets[0] = 100;
		accuracyThreshold[0] = 0.35;
		double headSize[] = {rule.getHeadSize()};
		
		System.out.println("Size="+ds.size()+" Facts="+ds.totalWeight());
		if (ds.totalWeight()<numBuckets[0] || ds.size()<numBuckets[0]/2)
			return false;
			
		numBuckets[0] = 100;
		
		String[] title = new String[1];
		title[0] = rule.getRuleString();
		
		
		
		//synchronized (info) {
		//	int buck = numBuckets[0];
		//	int save = buck;
		//	while (buck < 100) {
				caller.addDoubleArray("x", ds.getX());
			    caller.addDoubleArray("y", ds.getY());
			    caller.addDoubleArray("w", ds.getWeights());
			    caller.addDoubleArray("s", ds.getSupports());
			    caller.addDoubleArray("span", span);
			    caller.addDoubleArray("entropyThreshold", entropyRatioThreshold);
			    caller.addDoubleArray("accuracyThreshold", accuracyThreshold);
			    caller.addDoubleArray("outliers", outliers);
			    caller.addDoubleArray("headSize", headSize);
			    caller.addStringArray("title", title);
			    caller.addIntArray("numTestPoints",plotResolution);
			    caller.addIntArray("numBuckets",numBuckets);
			     
		
			    File file = caller.startPlot();
			    
			    caller.R_source("/home/adeoliv/Documents/Thesis/r/rcaller.r");
			
			    caller.endPlot();
			    caller.runOnly();
			    
			    caller.cleanRCode();
			    caller.R_source("/home/adeoliv/Documents/Thesis/r/utils.r");
			    
			    
			    File plot = new File("rules/plots/"+rule.getRuleString()+"["+numBuckets[0]+"]ROL.png");
			    FileUtils.copyFile(file, plot);
		//	    buck *= 2;
		//	    numBuckets[0] = buck;
		//	}
		//	numBuckets[0] = save;
		//}
		
		return true;
		
	}
	
	public static void main(String[] args) throws Exception {
		Connection connPartition = Driver.connect("src/rdf3x.properties");
		QueryHandler qh = new QueryHandler(connPartition);
		//RuleWithNumericLiteralLearner nl = new RuleWithNumericLiteralLearner(qh, null, null);
		//nl.
	}
	

}
