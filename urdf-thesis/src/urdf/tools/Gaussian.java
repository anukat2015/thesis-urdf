package urdf.tools;

public class Gaussian {

	// return phi(x) = standard Gaussian pdf
	public static double phi(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	// return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
	public static double phi(double x, double mu, double sigma) {
		return phi((x - mu) / sigma) / sigma;
	}

	// return Phi(z) = standard Gaussian cdf using Taylor approximation
	public static double Phi(double z) {
		if (z < -8.0)
			return 0.0;
		if (z > 8.0)
			return 1.0;
		double sum = 0.0, term = z;
		for (int i = 3; sum + term != sum; i += 2) {
			sum = sum + term;
			term = term * z * z / i;
		}
		return 0.5 + sum * phi(z);
	}

	// return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
	public static double Phi(double z, double mu, double sigma) {
		return Phi((z - mu) / sigma);
	}

	// Compute z such that Phi(z) = y via bisection search
	public static double PhiInverse(double y) {
		return PhiInverse(y, .00000001, -8, 8);
	}

	// bisection search
	private static double PhiInverse(double y, double delta, double lo, double hi) {
		double mid = lo + (hi - lo) / 2;
		if (hi - lo < delta)
			return mid;
		if (Phi(mid) > y)
			return PhiInverse(y, delta, lo, mid);
		return PhiInverse(y, delta, mid, hi);
	}

	public static void main(String[] args) {
		double sigma = 1.0, mu = 0.9;
		for (int i = 0; i <= 100; i++) {
			double x = mu + ((i - (100 / 2)) * sigma);
			if (x == mu)
				continue;
			System.out.println(x + ": " + Gaussian.phi(x, mu, sigma));
		}
	}
}