# TODO: Add comment
# 
# Author: adeoliv
###############################################################################

isGood = FALSE;

pvalueThreshold = 0.05;

x=cbind(x1,x2);

if (dim(x)[2]==2) {
	#pearsonTable = createChiSqTable(x,chisqBuckets,w);
	#pvalue = chisq.test(pearsonTable)$p.value;
	#if (pvalue > pvalueThreshold) { # If variables are dependent
		### Learn 2D loess =D
		model = loess(y~x,span=span,weights=w);
		resolution = 20;
		points = resolution^2;
		minx1 = min(x[,1]);
		minx2 = min(x[,2]);
		x1step = (max(x[,1])-minx1)/resolution;
		x2step = (max(x[,2])-minx2)/resolution;
		xgrid = matrix(0,points,2);
		for (i in 0:resolution-1) {
			for (j in 0:resolution-1) {
				p = 1 + i*resolution + j;
				xgrid[p,1] = minx1 + i*x1step;
				xgrid[p,2] = minx2 + j*x2step;
			}
		}
		ypred = predict(model,xgrid);
		scatterplot3d(xgrid[,1],xgrid[,2],ypred,type="lines");
		#points3d(x[,1],x[,2],y);
		
	#}
}

