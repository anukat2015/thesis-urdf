package urdf.cl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.lang.Math;

import org.apache.commons.io.FileUtils;

import rcaller.RCaller;


public class ArrayTools {
	
	private static NumberFormat formatter = new DecimalFormat("0.0000");
	private static RCaller caller = new RCaller();
    static { caller.setRscriptExecutable("/home/adeoliv/Downloads/R-2.15.1/bin/Rscript"); }
    
    public static int measure = 0;
	
	static float[] intToFloat(int[] x) {
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++)
			result[i] = (float)x[i];
		return result;
	}
    
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
	
	static String toString(int[] x) {
		String s = "";
		for (int xi: x) s += xi+"\t";
		return s;
	}
	
	static String toString(float[] x) {
		String s = "";
		for (float xi: x) s += formatter.format(xi)+"\t";
		return s;
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
			if (!Float.isNaN(x[i]))
				result += x[i];
		return result;
	}
	
	static float mean(int[] x) {
		return sum(x)/x.length;
	}
	
	static float mean(float[] x) {
		return sum(x)/x.length;
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
	
	static float[] average(float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = (x[i] + y[i])/2;
		
		return result;
	}
	
	static float[] average(int[] x, int[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		
		float[] result = new float[x.length];
		for (int i=0; i<x.length; i++) 
			result[i] = ((float)(x[i] + y[i]))/2;
		
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
	
	
	static float divergence (int[] p, int[] q) {
		switch(measure) {
			case 0: return sum(p);
			case 1: return kullbackLeiblerDivergence(p, q)*sum(p); 
			default: throw new IllegalArgumentException("Illegal measure");
		}
		
		//return jensenShanonDivergence(p, q)*sum(p);
		//return chiSquare(p, q);
	}
	
	static float divergence (float[] p, float[] q) {
		switch(measure) {
			case 0: return sum(p);
			case 1: return kullbackLeiblerDivergence(p, q)*sum(p); 
			default: throw new IllegalArgumentException("Illegal measure");
		}

		//return jensenShanonDivergence(p, q)*sum(p);
		//return chiSquare(p, q);
	}
	
	static float kullbackLeiblerDivergence (int[] p, int[] q) {
		return crossEntropy(p, q) - entropy(p);
	}
	
	static float kullbackLeiblerDivergence (float[] p, float[] q) {
		return crossEntropy(p, q) - entropy(p);
	}

	static float jensenShanonDivergence (int[] p, int[] q) {
		float[] m = average(p,q);
		float[] fp = intToFloat(p);
		float[] fq = intToFloat(q);
		return (float) (0.5*(kullbackLeiblerDivergence(fp, m)+kullbackLeiblerDivergence(fq, m)));
	}
	
	static float jensenShanonDivergence (float[] p, float[] q) {
		float[] m = average(p,q);
		return (float) (0.5*(kullbackLeiblerDivergence(p, m)+kullbackLeiblerDivergence(q, m)));
	}
	
	static float chiSquareDivergence(int[] x, int[] y) {
		return chiSquare(normalize(x), normalize(y));
	}
	
	static float chiSquareDivergence(float[] x, float[] y) {
		return chiSquare(normalize(x), normalize(y));
	}
	
	static float chiSquare(int[] x, int[] y) {
		int[] diff = substract(x,y);
		return sum(divide(multiply(diff,diff),y));
	}
	
	static float chiSquare(float[] x, float[] y) {
		float[] diff = susbtract(x,y);
		return sum(divide(multiply(diff,diff),y));
	}
	
	static float indepMeasure(int[] x, int[] y) {
		//float[] diff = susbtract(normalize(x),normalize(y));
		//return sum(divide(multiply(diff,diff),normalize(y)));
		int[] diff = substract(x,y);
		return sum(divide(abs(diff),multiply(y,y)));
	}
	
	static float covariance(int[] x, int[] y) {
		return covariance(intToFloat(x),intToFloat(y));
	}
	
	static float covariance(float[] x, float[] y) {
		if (x.length != y.length) 
			throw new IllegalArgumentException("Both arrays must be of same size");
		float xmean = mean(x);
		float ymean = mean(y);
		float sum = 0;
		for (int i=0; i<x.length; i++) {
			sum += (x[i]-xmean)*(y[i]-ymean);
		}
		return sum/x.length;
	}
	
	static float correlation(float[] x, float[] y) {
		return covariance(x, y)/(standardDeviation(x)*standardDeviation(y));
	}
	
	static float correlation(int[] x, int[] y) {
		return correlation(intToFloat(x),intToFloat(y));
	}
	
	static float variance(float[] x) {
		float sum = 0;
		float xmean = mean(x);
		for (int i=0; i<x.length; i++) {
			sum += Math.pow((x[i]-xmean),2);
		}
		return sum;
	}
	
	static float variance(int[] x) {
		return variance(intToFloat(x));
	}
	
	static float standardDeviation(float[] x) {
		return (float) Math.sqrt(variance(x));
	}
	
	static float standardDeviation(int[] x) {
		return standardDeviation(intToFloat(x));
	}
	
	static float[] getAccuraciesWithMinSupport(int[] supHead, int[] supBody, int minSupport ) { 
		float[] acc = ArrayTools.divide(supHead, supBody);
		float[] newAcc = new float[acc.length];
		for (int i=0; i<supHead.length; i++) {
			int k = 1;
			int sumSuppHead = 0;
			int sumSuppBody = 0; 
			float avgAcc = 0;
			if (supBody[i]>0 && !Float.isNaN(acc[i])) {
				sumSuppHead = supHead[i]; 
				sumSuppBody = supBody[i];
			}
			while (sumSuppHead < minSupport && k < supHead.length) {
				if (i>=k && supBody[i-k]>0) {
					sumSuppBody += supBody[i-k];
					sumSuppHead += supHead[i-k];
				}
				if (i<(supHead.length-k) && supBody[i+k]>0) {
					sumSuppBody += supBody[i+k];
					sumSuppHead += supHead[i+k];
				}
				k++;
			}
			newAcc[i] = ((float)sumSuppHead)/((float)sumSuppBody);
		}
		return newAcc;
	}
	
	static void plot(float[] x1, float[] x2, String filename) throws IOException {		
		caller.cleanRCode();
		caller.addFloatArray("x1",x1);
		caller.addFloatArray("x2",x2);
	    
	    File file = caller.startPlot();
	    
	    //caller.addRCode("plot(x1,type='s',col='red',ylim=c(0,1))");
	    caller.addRCode("plot(x1,type='s',col='red')");
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
	    
	    //caller.addRCode("plot(b,x1,type='s',col='red',ylim=c(0,1))");
	    caller.addRCode("plot(b,x1,type='s',col='red')");
	    caller.addRCode("points(b,x2,type='s',col='blue')");

	    caller.endPlot();
	    caller.runOnly();
	    caller.cleanRCode();

	    File plot = new File("/var/tmp/plots/"+filename+".png");
	    FileUtils.copyFile(file, plot);
	}
	
}
