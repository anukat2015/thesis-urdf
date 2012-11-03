package urdf.arm;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.io.FileUtils;

import rcaller.RCaller;


public class ArrayTools {
	
	private static NumberFormat formatter = new DecimalFormat("0.0000");
	private static RCaller caller = new RCaller();
    static { caller.setRscriptExecutable("/home/adeoliv/Downloads/R-2.15.1/bin/Rscript"); }
	
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
	
	static int[] round(float[] x) {
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++) {
			result[i] = Math.round(x[i]);
		}
		return result;
	}

	static void print(int[] x) {
		for (int xi: x) System.out.print(xi+"\t");
		System.out.println();
	}
	
	static void print(float[] x) {
		for (float xi: x) System.out.print(formatter.format(xi)+"\t");
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
	
	static int[] abs(int[] x) {
		int[] result = new int[x.length];
		for (int i=0; i<x.length; i++)
			result[i] = Math.abs(x[i]);
		return result;
	}
	
	static float[] abs(float[] x) {
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++)
			result[i] = Math.abs(x[i]);
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
	
	static float[] multiply(float[] x, int[] y) {
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
		//float[] diff = susbtract(normalize(x),normalize(y));
		//return sum(divide(multiply(diff,diff),normalize(y)));
		int[] diff = substract(x,y);
		return sum(divide(multiply(diff,diff),y));
	}
	
	static float chisqDivergence(float[] x, float[] y) {
		//float[] diff = susbtract(normalize(x),normalize(y));
		//return sum(divide(multiply(diff,diff),normalize(y)));
		float[] diff = susbtract(x,y);
		return sum(divide(multiply(diff,diff),y));
	}
	
	static float indepMeasure(int[] x, int[] y) {
		//float[] diff = susbtract(normalize(x),normalize(y));
		//return sum(divide(multiply(diff,diff),normalize(y)));
		int[] diff = substract(x,y);
		return sum(divide(abs(diff),multiply(y,y)));
	}
	
	static void plot(float[] x1, float[] x2, String filename) throws IOException {		
		caller.cleanRCode();
		caller.addFloatArray("x1",x1);
		caller.addFloatArray("x2",x2);
	    
	    File file = caller.startPlot();
	    
	    caller.addRCode("plot(x1,type='s',col='red',ylim=c(0,1))");
	    caller.addRCode("points(x2,type='s',col='blue')");

	    caller.endPlot();
	    caller.runOnly();
	    caller.cleanRCode();

	    File plot = new File("/var/tmp/plots/"+filename+".png");
	    FileUtils.copyFile(file, plot);
	}
	
	static void plot(float[] x1, float[] x2, float[] boundaries, String filename) throws IOException {		
		caller.cleanRCode();
		caller.addFloatArray("x1",x1);
		caller.addFloatArray("x2",x2);
		caller.addFloatArray("b",boundaries);
	    
	    File file = caller.startPlot();
	    
	    caller.addRCode("plot(b,x1,type='s',col='red',ylim=c(0,1))");
	    caller.addRCode("points(b,x2,type='s',col='blue')");

	    caller.endPlot();
	    caller.runOnly();
	    caller.cleanRCode();

	    File plot = new File("/var/tmp/plots/"+filename+".png");
	    FileUtils.copyFile(file, plot);
	}
	
}
