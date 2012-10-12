package urdf.arm;


public class ArrayTools {
	
	static float max(float[] x) {
		float max = Float.NEGATIVE_INFINITY;
		for (float f: x) 
			if (f > max)
				max = f;
		return max;
	}
	
	static float min(float[] x) {
		float min = Float.POSITIVE_INFINITY;
		for (float f: x) 
			if (f < min)
				min = f;
		return min;
	}
	
	static int max(int[] x) {
		int max = Integer.MIN_VALUE;
		for (int f: x) 
			if (f > max)
				max = f;
		return max;
	}
	
	static int min(int[] x) {
		int min = Integer.MAX_VALUE;
		for (int f: x) 
			if (f < min)
				min = f;
		return min;
	}

	static void print(int[] x) {
		for (int xi: x) System.out.print(xi+"\t");
		System.out.println();
	}
	
	static void print(float[] x) {
		for (float xi: x) System.out.print(xi+"\t");
		System.out.println();
	}
	
	static int sum(int[] x) {
		int result = 0;
		for (int i=0; i<x.length; i++) 
			result += x[i];
		return result;
	}
	
	static float sum(float[] x) {
		float result = 0;
		for (int i=0; i<x.length; i++) 
			result += x[i];
		return result;
	}
	
	static float[] susbtract(float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] - y[i];
		
		return result;
	}
	
	static int[] substract(int[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] - y[i];
		
		return result;
	}
	
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
	
	static float[] multiply(float[] x, float y) {		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] * y;
		
		return result;
	}
	
	static int[] multiply(int[] x, int y) {		
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = x[i] * y;
		
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
		float sum = sum(x);
		
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
	
	static float chisqDivergence(int[] x, int[] y) {
		float[] diff = susbtract(normalize(x),normalize(y));
		return sum(divide(multiply(diff,diff),normalize(y)));
	}
	
	static float chisqDivergence(float[] x, float[] y) {
		float[] diff = susbtract(normalize(x),normalize(y));
		return sum(divide(multiply(diff,diff),normalize(y)));
	}
	
	
}
