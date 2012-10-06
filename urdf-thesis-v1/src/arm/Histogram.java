package arm;

import java.util.ArrayList;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Histogram {
	private int[] count;
	private float[] normalized;
	
	private float[] boundaries;
	private int numberOfBuckets;
	private float min;
	private float max;
	private float bucketWidth; 
	private int totalCount = 0;
	
	public Histogram(float min, float max, int numberOfBuckets) {
		boundaries = new float[numberOfBuckets-1];
		count = new int[numberOfBuckets];
		bucketWidth = (max-min)/((float)numberOfBuckets);
		float boundary = min;
		for (int i=0; i<numberOfBuckets; i++) {
			boundary += bucketWidth;
			boundaries[i] = boundary;
		}
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
	
	public int[] loadData(float[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		Arrays.fill(count, 0);
		Arrays.fill(normalized, 0);
		totalCount = 0;
		
		for (int i=0; i<x.length; i++) {
			int bucket = -1;
			if (x[i]<max && x[i]>=min) {
				bucket = (int) Math.ceil((x[i]-min)/bucketWidth);
			} else {
				if (x[i]==max) 
					bucket = numberOfBuckets-1;
				else
					continue;
			}
			totalCount += y[i];
			count[bucket] += y[i];
			normalized[bucket] += y[i];
		}
		
		for (int i=0; i<numberOfBuckets; i++) {
			normalized[i] /= totalCount;
		}
		
		return count; 
	}
	
	public int[] getDistribution() {
		return count;
	}
	
	public float[] getNormalizedDistribution() {
		return normalized;
		
	}
}
