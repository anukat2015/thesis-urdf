package urdf.arm;


public class ArrayTools {

	
	static float[] sum(float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] + y[i];
		
		return result;
	}
	
	static int[] sum(int[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] + y[i];
		
		return result;
	}
	
	static float[] sum(float[] x, float y) {		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] + y;
		
		return result;
	}
	
	static int[] sum(int[] x, int y) {		
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] + y;
		
		return result;
	}
	
	static float[] divide(float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i]/y[i];
		
		return result;
	}
	
	static float[] divide(int[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = ((float) x[i])/((float) y[i]);
		
		return result;
	}
	
	static float[] multiply(float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i]*y[i];
		
		return result;
	}
	
	static int[] multiply(int[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i]*y[i];
		
		return result;
	}
	
	static float[] normalize(int[] x) {
		float[] result = new float[x.length];
		float sum = 0;
		for (int i=0; i<x.length; i++) 
			sum += x[i];
		
		for (int i=0; i<x.length; i++) 
			result[i] = ((float)x[i])/sum;
		
		return result;		
	}
	
	static float[] normalize(float[] x) {
		float[] result = new float[x.length];
		float sum = 0;
		for (int i=0; i<x.length; i++) 
			sum += x[i];
		
		for (int i=0; i<x.length; i++) 
			result[i] = x[i]/sum;
		
		return result;		
	}
	
	static float entropy (int[] x) {
		float[] norm = normalize(x);
		float result = 0;
		for (int i=0; i<x.length; i++)  
			result -= norm[i]*Math.log(norm[i]);
		
		return result;
	}
	
	static float entropy (float[] x) {
		float[] norm = normalize(x);
		float result = 0;
		for (int i=0; i<x.length; i++)  
			result -= norm[i]*Math.log(norm[i]);
		
		return result;
	}
	
	static int[] laplaceSmooth(int[] x) {
		return sum(x,1);
	}
	
	static float crossEntropy (int[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] xnorm = normalize(x);
		float[] ynorm = normalize(y);
		float result = 0;
		for (int i=0; i<x.length; i++)  
			result -= xnorm[i]*Math.log(ynorm[i]);
		
		return result;
	}
	
	static float crossEntropy (float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] xnorm = normalize(x);
		float[] ynorm = normalize(y);
		float result = 0;
		for (int i=0; i<x.length; i++)  
			result -= xnorm[i]*Math.log(ynorm[i]);
		
		return result;
	}
	
	static float klDivergence (int[] x, int[] y) {
		return crossEntropy(x, y) - entropy(x);
	}
	
	static float klDivergence (float[] x, float[] y) {
		return crossEntropy(x, y) - entropy(x);
	}
	
	
}
