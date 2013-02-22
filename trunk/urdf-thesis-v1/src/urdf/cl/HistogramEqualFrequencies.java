package urdf.cl;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import javatools.datatypes.Tree;

import edu.emory.mathcs.backport.java.util.Arrays;

import urdf.rdf3x.ResultSet;

public class HistogramEqualFrequencies implements Histogram{
	
	private static final long serialVersionUID = 1L;
	
	protected int[] count;
	protected float[] normalized;
	
	protected float[] boundaries;
	protected int numberOfBuckets;
	protected float min = Float.NaN;
	protected float max=  Float.NaN;
	protected int totalCount = 0;
	protected float xMean = 0;
	
	private int lastBucketAdded = 0; // for optimization in case data is inserted in ascending order 
	private float lastElementAdded = Float.NEGATIVE_INFINITY;

	public HistogramEqualFrequencies(float min, float max, float[] boundaries) {
		this.min = min;
		this.max = max;
		this.numberOfBuckets = boundaries.length+1;
		this.boundaries = new float[this.numberOfBuckets-1];
		this.count = new int[numberOfBuckets];
		this.normalized = new float[numberOfBuckets];
		this.reset();
		if (!(min < boundaries[0] && max > boundaries[boundaries.length-1])) {
			throw new IllegalArgumentException("min, max and boundaries don't match");		
		}
		this.boundaries = boundaries;
		
	}
	
	public HistogramEqualFrequencies(ResultSet rs, int numOfBuckets) throws SQLException {
		this.numberOfBuckets = numOfBuckets;
		this.boundaries = new float[this.numberOfBuckets-1];
		this.count = new int[numberOfBuckets];
		this.normalized = new float[numberOfBuckets];
		
		TreeMap<Float,Integer> sortedEntries = new TreeMap<Float, Integer>();
		rs.beforeFirst();
		int total = 0;
		while (rs.next()) {
			float n = rs.getFloat(1);
			int count = 1;
			try {
				count += rs.getInt("count");
			} catch (SQLException e) {}
			total += count;
			if (sortedEntries.containsKey(n)) 
				sortedEntries.put(n, count+sortedEntries.get(n));
			else
				sortedEntries.put(n, count);
		}
		this.min = sortedEntries.firstKey();
		this.max = sortedEntries.lastKey();
		
		
		int bucketSupport = (int) Math.ceil(((float)total)/((float)numOfBuckets));
		int sum = 0;
		int bucket = 0;
		int lastBoundary = 0;
		for (Entry<Float,Integer> e: sortedEntries.entrySet()) {
			sum += e.getValue();
			if (bucket < boundaries.length && sum >= lastBoundary+bucketSupport) {
				this.boundaries[bucket] = e.getKey();
				lastBoundary = sum;
				bucket++;
				bucketSupport = (int) Math.ceil(((float)(total-sum))/((float)(numOfBuckets-bucket)));
			}
		}
		
		for (Entry<Float,Integer> e: sortedEntries.entrySet()) {
			this.addDataPoint(e.getKey(), e.getValue());
		}
		
		rs.beforeFirst();
	}
	
	@Override
	public int getBucket(float x) {
		int bucket;
		if (x > boundaries[numberOfBuckets-2])
			bucket = numberOfBuckets-1;
		else {
			if (x>lastElementAdded)
				bucket = lastBucketAdded;
			else
				bucket = 0;
			
			while (bucket<(numberOfBuckets-1) && boundaries[bucket]<x) {
				bucket++;
			}
		}		
		return bucket;
	}
	
	@Override
	public void addDataPoint(float x, int y) {
		if (x<=max && x>=min) {
			int i = getBucket(x);			
			totalCount += y;
			xMean += y*x;
			count[i] += y;
			normalized[i] += y;
			lastBucketAdded = i;
			lastElementAdded = x;
		} else {
			throw new IllegalArgumentException("Point "+x+" is out of bounds ["+min+","+max+"]");
		}
			
	}
	
	@Override
	public void reset() {
		Arrays.fill(count, 0);
		Arrays.fill(normalized, 0);
		totalCount = 0;
		xMean = 0;
		lastBucketAdded = 0;
		lastElementAdded = Float.NEGATIVE_INFINITY;
	}
	
	@Override
	public Histogram clone() {
		Histogram hist = new HistogramEqualFrequencies(this.min, this.max, this.boundaries);
		return hist;
	}

	@Override
	public int[] getDistribution() {
		return count;
	}

	@Override
	public int getSupport() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMean() {
		return xMean;
	}

	@Override
	public float[] getNormalizedDistribution() {
		return normalized;
	}

	@Override
	public float[] getBoundaries() {
		return boundaries;
	}
	
	@Override
	public void setMin(float min) {
		this.min=min;
	}
	@Override
	public void setMax(float max) {
		this.max=max;
	}
	

}
