package urdf.arm;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Histogram implements Cloneable{
	protected int[] count;
	protected float[] normalized;
	
	protected float[] boundaries;
	protected int numberOfBuckets;
	protected float min = Float.NaN;
	protected float max=  Float.NaN;
	private float bucketWidth; 
	protected int totalCount = 0;
	protected double xMean = 0;

	
	public Histogram(float min, float max, int numberOfBuckets) {
		this.min = min;
		this.max = max;
		this.numberOfBuckets = numberOfBuckets;
		this.boundaries = new float[this.numberOfBuckets-1];
		this.count = new int[numberOfBuckets];
		this.normalized = new float[numberOfBuckets];
		bucketWidth = (max-min)/((float)numberOfBuckets);
		float boundary = min;
		for (int i=0; i<(numberOfBuckets-1); i++) {
			boundary += bucketWidth;
			boundaries[i] = boundary;
		}
		this.reset();
	}
	
	public Histogram(float x[], int[] y, int numberOfBuckets) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		this.numberOfBuckets = numberOfBuckets;
		max=Float.NEGATIVE_INFINITY;
		min=Float.POSITIVE_INFINITY;
		for (int i=0; i<x.length; i++) {
			if (x[i]>max) max = x[i];
			if (x[i]<min) min = x[i];
		}
		bucketWidth = (max-min)/this.numberOfBuckets;
		loadData(x, y);
	}
	
	public void reset(){ 
		Arrays.fill(count, 0);
		Arrays.fill(normalized, 0);
		totalCount = 0;
		xMean = 0;
	}
	
	public int getBucket(float x) {
		int bucket = (int) Math.ceil((x-min)/bucketWidth);
		if (bucket>=numberOfBuckets) 
			return numberOfBuckets-1;
		if (bucket < 0)
			return 0;
		
		return bucket;
	}
	
	public void addDataPoint(float x, int y) {
		int bucket = -1;
		if (x<=max && x>=min) {
			bucket = getBucket(x);
			totalCount += y;
			xMean += y*x;
			count[bucket] += y;
			normalized[bucket] += y;
		} else {
			throw new IllegalArgumentException("Point "+x+" is out of bounds ["+min+","+max+"]");
		}

	}
	
	public int[] loadData(float[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		this.reset();
		
		for (int i=0; i<x.length; i++) {
			try {
				addDataPoint(x[i], y[i]);
			} catch (IllegalArgumentException e) {
				
			}
		}
		
		for (int i=0; i<numberOfBuckets; i++) {
			normalized[i] /= totalCount;
		}
		
		return count; 
	}
	
	public int[] getDistribution() {
		return count;
	}
	
	public float getMean() {
		return ((float)xMean)/((float)totalCount);
	}
	
	public float[] getNormalizedDistribution() {
		return normalized;
	}
	
	@Override
	public Histogram clone() {
		return new Histogram(this.min, this.max, this.numberOfBuckets);
	}
}
