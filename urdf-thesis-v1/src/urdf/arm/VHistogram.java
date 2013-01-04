package urdf.arm;

import java.sql.SQLException;

import edu.emory.mathcs.backport.java.util.Arrays;

import urdf.rdf3x.ResultSet;

public class VHistogram extends Histogram{
	
	private static final long serialVersionUID = 1L;
	
	private int lastBucketAdded = 0; // for optimization in case data is inserted in ascending order 

	public VHistogram(float min, float max, float[] boundaries) {
		super(min,max,boundaries.length+1);
		if (!(min < boundaries[0] && max > boundaries[boundaries.length-1])) {
			throw new IllegalArgumentException("min, max and boundaries don't match");		
		}
		this.boundaries = boundaries;
		
	}
	
	public VHistogram(ResultSet rs, float min, float max, int numOfBuckets) throws SQLException {
		super(min,max,numOfBuckets);
		rs.first();
		float minRS = rs.getFloat(1);
		rs.last();
		float maxRS = rs.getFloat(1);
		if (min!=minRS || max!=maxRS)
			throw new IllegalArgumentException("Min and Max values parameters do not accord to ResultSet data");
		
		// Sum facts and check if ResultSet is sorted;
		int count = 0;
		float last = Float.NEGATIVE_INFINITY;
		rs.beforeFirst();
		while (rs.next()) {
			count += rs.getInt(2);
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
			sum += rs.getInt(2);
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
			this.addDataPoint(rs.getFloat(1), rs.getInt(2));
		}
	}
	
	@Override
	public int getBucket(float x) {
		int bucket;
		if (x > boundaries[numberOfBuckets-2])
			bucket = numberOfBuckets-1;
		else {
			if (lastBucketAdded<boundaries.length && x < boundaries[lastBucketAdded])
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
		} else {
			throw new IllegalArgumentException("Point "+x+" is out of bounds ["+min+","+max+"]");
		}
			
	}
	
	@Override
	public void reset() {
		super.reset();
		Arrays.fill(count, 0);
		Arrays.fill(normalized, 0);
		totalCount = 0;
		xMean = 0;
		lastBucketAdded = 0;
	}
	
	@Override
	public VHistogram clone() {
		return new VHistogram(this.min, this.max, this.boundaries);
	}
	

}
