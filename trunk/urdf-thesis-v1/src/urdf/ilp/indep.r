# TODO: Add comment
# 
# Author: adeoliv
###############################################################################




isGood = FALSE;

pvalueThreshold = 0.05;

x=cbind(x1,x2);

if (dim(x)[2]==2) {
	pearsonTable = createChiSqTable(x,chisqBuckets,w);
	test = chisq.test(pearsonTable);
	pvalue = test$p.value;
	#if (pvalue > pvalueThreshold) { # If variables are dependent
		### Learn 2D loess =D
		model = loess(y~x,span=span,weights=w);

		res = 20;
		xgrid = build2Dgrid(x,res);
		ypred = predict(model,xgrid);
		
		ysupp = suppND(xgrid,x,s,span);
		ysupp = ysupp/max(ysupp);
		
		peak = optimize(function(x)predict(model,x), c(min(x),max(x)), maximum=TRUE);			
		#points(peak$maximum,peak$objective,pch=15,col=color);	
		#peak = optimize(function(x)predict(modelAcc,x), c(peak$max,max(dfOriginal$x)), maximum=TRUE);	
		#points(peak$maximum,peak$objective,pch=15,col=color);
		#peak = optimize(function(x)predict(modelAcc,x), c(peak$max + 0.1,max(dfOriginal$x)), maximum=TRUE);	
		#points(peak$maximum,peak$objective,pch=15,col=color);		
		
		plot = scatterplot3d(x[,1],x[,2],y,type="h",zlim=c(0,1));
		for (i in 0:(res-1)) plot$points3d(xgrid[(i*res+1):((i+1)*res),1],xgrid[(i*res+1):((i+1)*res),2],ypred[(i*res+1):((i+1)*res)],type="lines",col="red");
		for (i in 0:(res-1)) plot$points3d(xgrid[(i*res+1):((i+1)*res),1],xgrid[(i*res+1):((i+1)*res),2],ysupp[(i*res+1):((i+1)*res)],type="lines",col="blue");
		#plot$points3d(peak$maximum,peak$objective,pch=15,col=color);
		#plot$points3d(xgrid[,1],xgrid[,2],ypred,type="lines",col="blue");
		#plot$points3d(xgrid[,1],xgrid[,2],ysupp,type="lines",col="blue");
		
		plot3d(x[,1],x[,2],y,type="h",zlim=c(0,1));
		plot3d(xgrid[,1],xgrid[,2],ypred,col="red",add=TRUE,zlim=c(0,1));
		plot3d(xgrid[,1],xgrid[,2],ysupp,col="blue",add=TRUE,zlim=c(0,1));
		
		#rgl.postscript("/home/adeoliv/Desktop/test.svg",fmt="svg");
		#rgl.postscript("/home/adeoliv/Desktop/test.pdf",fmt="pdf");
	#}
}

