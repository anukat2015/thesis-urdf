package urdf.ilp;

import javax.vecmath.GMatrix;
import javax.vecmath.GVector;

import com.gregdennis.drej.GaussianKernel;
import com.gregdennis.drej.Kernel;
import com.gregdennis.drej.Matrices;
import com.gregdennis.drej.PolynomialKernel;
import com.gregdennis.drej.Regression;
import com.gregdennis.drej.Representer;

public class DrejRegressionTest {

	/*static double[] x = {9.100, -1.300, -2.100, -3.400, 39.400, -4.000, 9.000, -5.700, 7.600, 3.100, 5.800, -2.900, 2.300,
						 1.500, 2.900, -4.000, 5.200, -0.739, -1.500, -4.900, 6.300, 5.600, 15.100, 4.700, 14.100, 7.000,
						 4.500, 8.310, 3.700, -17.800, 1.800, 3.700, -2.500, 1.700, 8.900, 3.500, 7.000, -16.800, 11.600,
						 -0.300, -5.000, 0.200, -4.500, 2.200, 3.800, 1.800, -1.000, 1.400, 4.300, 13.200, 11.800, -4.900,
						 5.000, 5.300, 5.600, -1.800, -15.100, -6.400, 7.300, -1.500, -7.200, 3.800, 4.700, -2.200, 4.200,
						 3.900, -3.100, 2.700, -3.300, -0.100, -7.800, -3.500, 4.600, -4.900, 2.000, -2.900, 2.600, -3.500,
						 -4.600, 1.800, -1.100, -0.600, -1.200, 6.100, 4.100, 4.800, 0.100, -0.200, -5.000, 1.600, -2.100,
						 -4.900, -5.000};
	
	static double[] y = {0.08333333, 0.09756098, 0.00000000, 0.13793103, 0.33333333, 0.00000000, 1.00000000, 0.47058824, 0.00000000, 0.00000000,
						 0.00000000, 0.00000000, 0.00000000, 0.00000000, 1.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.20000000,
						 0.00000000, 0.00000000, 0.50000000, 0.50000000, 0.00000000, 0.00000000, 0.00000000, 1.00000000, 0.16666667, 0.00000000,
						 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.12820513, 0.00000000, 0.00000000, 0.00000000, 0.00000000,
						 0.25000000, 0.11111111, 0.00000000, 0.00000000, 0.00000000, 1.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000,
						 0.00000000, 0.15000000, 0.00000000, 1.00000000, 0.00000000, 0.00000000, 0.06060606, 0.45000000, 0.35714286, 0.11538462,
						 0.27272727, 0.33333333, 0.16666667, 0.16666667, 0.27083333, 0.00000000, 0.05882353, 0.23529412, 0.25000000, 0.00000000,
						 0.23529412, 0.16666667, 0.05194805, 0.12500000, 0.53846154, 0.00000000, 0.14814815, 0.29268293, 0.25609756, 0.29629630,
						 0.37500000, 0.57142857, 0.23880597, 0.00000000, 0.17391304, 0.17391304, 0.21428571, 0.15384615, 0.31944444, 0.52908587,
						 0.25595238, 0.50000000, 0.10000000};
	
	static double[] w = {12, 41, 12, 29, 15, 1, 1, 17, 1, 1, 1, 1, 2, 5, 1, 17, 6, 3, 1, 5, 1, 5, 2, 2, 9, 3, 2,
						 1, 6, 4, 1, 2, 1, 1, 2, 39, 4, 5, 3, 1, 8, 9, 4, 3, 1, 1, 2, 1, 12, 1, 1, 20, 1, 1,
						 18, 2, 33, 20, 14, 26, 33, 3, 12, 12, 48, 22, 34, 34, 8, 11, 17, 18, 77, 24, 26, 8, 27, 41, 82, 27, 24,
						 7, 67, 7, 23, 23, 84, 52, 72, 361, 168, 280, 150};*/
	
	static double[] x = {0.0060, 0.0080, 0.0100, 0.0140, 0.0150, 0.0170, 0.0220, 0.0230, 0.0240, 0.0260,
	   0.0280, 0.0300, 0.0310, 0.0330, 0.0340, 0.0350, 0.0370, 0.0380, 0.0410, 0.0420,
	   0.0430, 0.0490, 0.0500, 0.0560, 0.0590, 0.0620, 0.0630, 0.0640, 0.0660, 0.0670,
	   0.0690, 0.0700, 0.0710, 0.0720, 0.0730, 0.0760, 0.0780, 0.0790, 0.0800, 0.0810,
	   0.0820, 0.0850, 0.0860, 0.0870, 0.0880, 0.0890, 0.0900, 0.0910, 0.0920, 0.0923,
	   0.0930, 0.0937, 0.0940, 0.0950, 0.0970, 0.0980, 0.1010, 0.1050, 0.1070, 0.1090,
	   0.1100, 0.1100, 0.1160, 0.1200, 0.1250, 0.1270, 0.1300, 0.1320, 0.1350, 0.1410,
	   0.1460, 0.1470, 0.1500, 0.1510, 0.1520, 0.1530, 0.1550, 0.1600, 0.1690, 0.1740,
	   0.1800, 0.1870, 0.2005, 0.2100, 0.2230, 0.2400, 0.2980, 0.3000, 0.3170, 0.3500,
	   0.4000, 0.5000, 0.5200, 0.8000, 0.8500, 0.9400};
	static double[] y = {0.00000000, 0.00000000, 0.05263158, 0.00000000, 0.00000000, 0.00000000,
		   0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.25000000, 0.00000000,
		   0.00000000, 0.03448276, 0.02380952, 0.00000000, 0.04773270, 0.83333333,
		   0.14748201, 0.01204819, 0.18181818, 0.05035971, 0.05128205, 0.04838710,
		   0.00000000, 0.01739130, 0.00000000, 0.00000000, 0.01923077, 0.16666667,
		   0.00000000, 0.11784512, 0.00000000, 0.09848485, 0.02830189, 0.04379562,
		   0.05632216, 0.00000000, 0.00000000, 0.04103967, 0.00000000, 0.12500000,
		   0.04121864, 0.09274194, 0.02702703, 0.03508772, 0.05714286, 0.04678363,
		   0.00000000, 0.00000000, 0.00000000, 0.03636364, 0.00000000, 0.00000000,
		   0.07692308, 0.06862745, 0.05361596, 0.02816901, 0.14033019, 0.00000000,
		   0.01492537, 0.06122449, 0.08333333, 0.00000000, 0.00000000, 0.15789474,
		   0.00000000, 0.01754386, 0.05882353, 0.15000000, 0.03389831, 0.00000000,
		   0.00000000, 0.00000000, 0.11063830, 0.00000000, 0.00000000, 0.00000000,
		   0.07142857, 0.03061224, 0.00000000, 0.00000000, 0.05325444, 0.50000000,
		   0.00000000, 0.02614379, 0.00000000, 0.00000000, 0.00000000, 0.00000000,
		   0.02325581, 0.00000000, 0.00000000, 0.33333333, 0.00000000, 0.19047619};
	static double[] w = {8,12,19,44,1,50,8,2,24,29,4,53,2,261,210,
			2,419,6,556,83,11,278,39,310,24,230,12,19,156,6,
			18,1188,48,132,424,137,3551,133,2,731,39,8,558,248,74,
			399,70,171,37,27,1,55,2,5,39,204,802,71,848,74,
			67,147,48,33,16,19,2,456,17,20,118,17,2,11,235,
			28,35,11,14,98,1,5,338,2,31,153,3,13,8,18,
			86,2,3,3,2,42};
	
	static GMatrix data;

	static GMatrix bData;
	static GVector values;

	static GVector bValues;
	static GMatrix test;
	
	
	static void bucketize(double[] x, double[] y, double[] w, double bucketSize) {
		double sum = 0;
		for (double e: w) sum += e;
		int length = (int) Math.floor(sum/bucketSize);
		bData = new GMatrix(1, length);
		bValues = new GVector(length);
		
		double remaining = bucketSize;
		
		double newX = 0;
		double newY = 0;
		
		int offset = 0;
		for (int i=0; i<w.length; i++) {
			double weight = w[i];
			double xvalue = x[i];
			double yvalue = y[i];
			while (weight > 0) {
				double pw = Math.min(remaining,weight);
				newX += pw*xvalue;
				newY += pw*yvalue;
				remaining -= pw;
				weight -= pw;
				if (remaining<=0) {
					bData.setElement(0, offset, newX/bucketSize);
					bValues.setElement(offset, newY/bucketSize);
					newX = 0;
					newY = 0;
					remaining = bucketSize;
					offset++;
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {


		 

		
		 //GMatrix data = new GMatrix(x.length, 2);
		 //data.setColumn(1, x);
		 //data.setColumn(2, y);
		 
		 data = new GMatrix(1, x.length);
		 data.setRow(0, x);
		 
		 values = new GVector(y);
		 
		 int testDataSize = 100;
		 test = new GMatrix(1,testDataSize);
		 for (int i=0; i<testDataSize; i++){
			 double v = ((double)i)/((double)testDataSize);
			 test.setElement(0, i, v);
		 }
		 System.out.println("test data: "+test.toString());


		 //Kernel kernel = new GaussianKernel(2);
		 Kernel kernel = new PolynomialKernel(4);

		 double lambda = 0.5;


		 Representer representer = Regression.solve(data, values, kernel, lambda);

		 System.out.println(representer.coeffs().toString());


		 // If you'd like to calculate how well the function fits the data, you can first calculate the vector of values the representer would predict for your data points, subtract from that the vector of actual values, and take the norm squared of that difference. Let's call this the "cost". The lower the cost, the better the function fits the data. You can try out different kernels, and see which one yields the best-fit curve (the lowest cost):
		 GVector predictedValues = Matrices.mapCols(representer, data);
		 System.out.println(predictedValues.toString());
		 System.out.println(values.toString());
		 predictedValues.sub(values);
		 double cost = predictedValues.normSquared();
		 
		 
		 
		 System.out.println("Bucketizing!");
		 bucketize(x, y, w, 10);
		 System.out.println("Ok, printing bucketized data:");
		 System.out.println(bData.toString());
		 System.out.println(bValues.toString());
		 
		 System.out.println("Learning Regression Model");
		 representer = Regression.solve(bData, bValues, kernel, lambda);
		 predictedValues = Matrices.mapCols(representer, test);
		 System.out.println(test.toString());
		 System.out.println(predictedValues.toString());
		 
		 String xx="xx=c(",yy="yy=c(";
		 for (int i=0; i<testDataSize; i++) {
			 xx+=test.getElement(0, i)+",";
			 yy+=predictedValues.getElement(i)+",";
		 }
		 xx += ");";
		 yy += ");";
		 xx = xx.replace(",);", ");");
		 yy = yy.replace(",);", ");");
		
		 System.out.println(xx);
		 System.out.println(yy);
			 

	}

}
