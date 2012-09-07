package urdf.ilp;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class RegressionTestLoess {
	/*static double[] x={-15.45460993,-12.11426783,-7.90000000,-7.9000000,-7.87142261,
						-7.45406758,-7.04013350,-6.14418023,-5.24063413,-5.00000000,
						-5.00000000,-5.00000000,-5.00000000,-5.00000000,-5.00000000,
						-5.00000000,-5.00000000,-4.99390905,-4.90000000,-4.90000000,
						-4.90000000,-4.90000000,-4.90000000,-4.90000000,-4.90000000,
						-4.90000000,-4.90000000,-4.90000000,-4.90000000,-4.90000000,
						-4.90000000,-4.79937422,-4.60000000,-4.60000000,-4.60000000,
						-4.13475177,-3.50166875,-3.50000000,-3.44647476,-3.36745932,
						-3.10166875,-2.85944931,-2.13445974,-2.10000000,-2.10000000,
						-2.10000000,-2.10000000,-2.10000000,-2.10000000,-2.03742178,
						-1.50000000,-1.31276596,-1.27743012,-1.20000000,-1.20000000,
						-1.15694618,-0.93612849,-0.23270755,-0.20000000,-0.07651231,
						0.10000000,0.10000000,0.10000000,0.67655403,1.60000000,
						1.60000000,1.60000000,1.60000000,1.60000000,1.60000000,
						1.60000000,1.60000000,1.60000000,1.60000000,1.60000000,
						1.60000000,1.60000000,1.60000000,1.60942845,1.80000000,
						1.96328744,2.36395494,2.63967459,2.70000000,3.44092616,
						3.55356696,3.87012933,4.06987902,4.18898623,4.20000000,
						4.39641218,4.60000000,4.60000000,4.60909470,4.75068836,
						5.10020859,5.71451815,7.07580309,10.00738423,30.01572799};

	static double[] y= {0.04796400,0.11288151,0.18666667,0.18666667,0.20056312,0.25687635,
						0.30815224,0.45752411,0.34822497,0.31944444,0.31944444,0.24950053,
						0.10000000,0.10000000,0.10000000,0.10000000,0.10000000,0.10609095,
						0.15693575,0.13125782,0.50000000,0.50000000,0.50000000,0.50000000,
						0.50000000,0.50000000,0.50000000,0.50000000,0.50000000,0.50000000,
						0.50000000,0.41819042,0.25609756,0.25609756,0.25609756,0.02190238,
						0.19707561,0.29268293,0.20985161,0.17439903,0.06041866,0.05110964,
						0.09704839,0.25595238,0.25595238,0.25595238,0.25595238,0.25595238,
						0.25595238,0.22098613,0.11538462,0.09869865,0.12943980,0.23880597,
						0.23880597,0.29744270,0.34012754,0.17721786,0.15384615,0.09887818,
						0.21428571,0.21428571,0.21428571,0.16235825,0.52908587,0.52908587,
						0.52908587,0.52908587,0.52908587,0.52908587,0.52908587,0.52908587,
						0.52908587,0.52908587,0.52908587,0.52908587,0.52908587,0.52908587,
						0.49310715,0.32108036,0.49400901,0.22194766,0.18272296,0.23529412,
						0.16154998,0.12460019,0.05270477,0.14772088,0.26015876,0.27083333,
						0.04626251,0.05194805,0.05194805,0.09019391,0.17033974,0.12044041,
						0.00000000,0.20859408,0.12515645,0.25031289};*/
	
	static double[] x={-15.52500,-7.90000,-7.71000,-6.40000,-5.59500,-5.00000,
		-4.90000,-4.60000,-4.10000,-3.50000,-3.40000,
		-3.18000,-2.53000,-2.10000,-1.53000,-1.30000,-1.20000,-1.10000,
		-0.50585,-0.01000,0.93500,1.79500,2.00000,2.51000,2.70000,
		3.45000,3.80000,4.10000,4.20000,4.41000,4.73000,5.43500,
		6.65000,9.33550,22.89500};
	static double[] y={0.04545455,0.18666667,0.24090909,0.45000000,0.43750000,0.31944444,
		0.16250000,0.12500000,0.00000000,
		0.17926829,0.13793103,0.13529412,0.08333333,0.10238095,0.09807692,
		0.09756098,0.23880597,0.37500000,0.25384615,0.09642857,0.18227147,
		0.30185185,0.53846154,0.11111111,0.23529412,0.16538462,0.10000000,
		0.17391304,0.27083333,0.01558442,0.20217391,0.05000000,0.07142857,
		0.15000000,0.16666667};


	public static void main(String[] args) {
		LoessInterpolator li = new LoessInterpolator(0.75, 50);
		double[] p = li.smooth(x, y);		
		System.out.println("Smooth");
		for (double d: p)System.out.println(d);
		
		
		PolynomialSplineFunction psf = li.interpolate(x, y);
		System.out.println("Knots");
		for (double d: psf.getKnots()) System.out.println(d);
		System.out.println("N="+psf.getN());
		PolynomialFunction[] pfs = psf.getPolynomials();
		for (PolynomialFunction pf: pfs) System.out.println(pf.degree()+")"+pf.toString());
		
	}
}