package urdf.cl;

import java.io.Serializable;

public interface Histogram extends Cloneable, Serializable{
	
	public void reset();
	
	public int getBucket(float x);
	
	public void addDataPoint(float x, int y);
	
	public int[] getDistribution();
	
	public int getSupport();
	
	public float getMean();
	
	public float[] getNormalizedDistribution();
	
	public float[] getBoundaries();

	public Histogram clone();
	
	public void setMin(float min);
	public void setMax(float max);

}
