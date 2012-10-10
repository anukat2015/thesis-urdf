package urdf.ilp;

import java.util.ArrayList;

public class DataSet {
	private ArrayList<ArrayList<Double>> xList; // X values
	private ArrayList<Double> yList; // Accuracies
	private ArrayList<Double> wList; // Body Sizes
	private ArrayList<Double> sList; // Supports (positives)
	private double totalWeight;
	private double totalSupport;
	int xDim = 1;
	
	public DataSet() {
		this.xList = new ArrayList<ArrayList<Double>>();
		this.yList = new ArrayList<Double>();
		this.wList = new ArrayList<Double>();
		this.sList = new ArrayList<Double>();
		this.totalWeight = 0;
		this.totalSupport = 0;
	}
	
	public DataSet(int xDim) {
		this();
		this.xDim = xDim;
	}
	
	@Override
	public String toString() {
		String output = "";
		int size = xList.size();
		for (int j=0; j<xDim; j++) {
			output+= "x["+j+"]=c("; for (int i=0; i<size; i++) output += xList.get(i).get(j) + ","; output += ")\n";
		}
		output+= "y=c("; for (int i=0; i<size; i++) output += yList.get(i) + ",";
		output+= ")\nw=c("; for (int i=0; i<size; i++) output += wList.get(i) + ",";
		output+= ")\ns=c("; for (int i=0; i<size; i++) output += sList.get(i) + ",";
		output+= ")\n";
		return output.replace(",)\n", ")\n");
	}

	private double[] toPrimitiveArray(ArrayList<Double> a) {
		double[] d = new double[a.size()];
		for (int i=0; i<a.size(); i++)
			d[i] = a.get(i).doubleValue();
		return d;
			
	}
	
	private void addX(double[] x) {
		ArrayList<Double> point = new ArrayList<Double>();
		for (double e: x) 
			point.add(new Double(e));	
		xList.add(point);
	}
	
	public void addDataPoint(double x, double y, double weight, double support) {
		if (xDim!=1) 
			throw new IllegalArgumentException("x should have "+xDim+" dimensions, it must be an array");
		double[] xArray = {x};
		addDataPoint(xArray, y, weight, support);
	}
	public void addDataPoint(double[] x, double y, double weight, double support) {
		if (x.length!=xDim) 
			throw new IllegalArgumentException("x should have "+xDim+" dimensions");
		this.addX(x);
		this.yList.add(y);
		this.wList.add(weight);
		this.sList.add(support);
		this.totalWeight += weight;
		this.totalSupport += support;
	}
	public double[] getX(int column) {
		if (column<0 || column>=xList.size())
			throw new IllegalArgumentException("Column should be between 0 and "+(xDim-1));
		double[] x = new double[xList.size()];
		for (int i=0; i<x.length; i++)
			x[i] = xList.get(i).get(column);
		
		return x;
	}
	public double[][] getX() {
		int rows = xList.size();
		if (rows==0) 
			return null;
		int cols = xList.get(0).size();
		
		double[][] matrix = new double[rows][cols];
		for (int row=0; row<rows; row++)
			for (int col=0; col<cols; col++)
				matrix[row][col] = xList.get(row).get(col);
		
		return matrix;
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
	public double totalSupport() {
		return totalSupport;
	}

}
