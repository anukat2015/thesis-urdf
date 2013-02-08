package urdf.cl;

import java.sql.SQLException;

import edu.emory.mathcs.backport.java.util.Arrays;

import urdf.rdf3x.ResultSet;

public class HistogramSupport implements Histogram{
	
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

	public HistogramSupport(float min, float max, float[] boundaries) {
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
	
	public HistogramSupport(ResultSet rs, int numOfBuckets) throws SQLException {
		this.numberOfBuckets = numOfBuckets;
		this.boundaries = new float[this.numberOfBuckets-1];
		this.count = new int[numberOfBuckets];
		this.normalized = new float[numberOfBuckets];
		
		float minRS = Float.POSITIVE_INFINITY;
		float maxRS = Float.NEGATIVE_INFINITY;
		rs.beforeFirst();
		while (rs.next()) {
			float n = rs.getFloat(1);
			if (n<minRS) minRS = n;
			if (n>maxRS) maxRS = n;
		}
		this.min = minRS;
		this.max = maxRS;
		
		// Sum facts and check if ResultSet is sorted;
		int count = 0;
		float last = Float.NEGATIVE_INFINITY;
		rs.beforeFirst();
		while (rs.next()) {
			try {
				count += rs.getInt("count");
			} catch (SQLException e) {
				count += 1;
			}
			if (rs.getFloat(1) < last)
				 throw new IllegalArgumentException("Result set should be sorted in ascending order");
			else
				last = rs.getFloat(1);
		}
		
		rs.beforeFirst();
		int bucketSupport = (int) Math.ceil(((float)count)/((float)numOfBuckets));
		int sum = 0;
		int bucket = 0;
		int lastBoundary = 0;
		while (rs.next() && bucket<boundaries.length) {
			try {
				sum += rs.getInt("count");
			} catch (SQLException e) {
				sum += 1;
			}
			if (sum >= lastBoundary+bucketSupport) {
				this.boundaries[bucket] = rs.getFloat(1);
				lastBoundary = sum;
				bucket++;
				bucketSupport = (int) Math.ceil(((float)(count-sum))/((float)(numOfBuckets-bucket)));
			}
		}
		//System.out.print("["+this.min+","+this.max+"]\t");
		
		rs.beforeFirst();
		while (rs.next()) {
			count = 1;
			try {
				count = rs.getInt("count");
			} catch (SQLException e) {}
			this.addDataPoint(rs.getFloat(1), count);
		}
	}
	
	@Override
	public int getBucket(float x) {
		int bucket;
		if (x > boundaries[numberOfBuckets-2])
			bucket = numberOfBuckets-1;
		else {
			//if (lastBucketAdded<boundaries.length && x < boundaries[lastBucketAdded])
			//	bucket = lastBucketAdded;
			//else
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
	}
	
	@Override
	public Histogram clone() {
		Histogram hist = new HistogramSupport(this.min, this.max, this.boundaries);
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
	

}
