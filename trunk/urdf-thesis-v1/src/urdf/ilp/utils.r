 setwd("/home/adeoliv/Documents/Thesis/r");
 library("stats")
 library("entropy");
 library("outliers");
 
 weighthist <- function(x, y, w, numBuckets) {
	max = max(x)+0.01;
	min = min(x);
	bucketLength = (max-min)/numBuckets;
	resultX = min + c(1:numBuckets)*bucketLength;
	resultY = array(0,numBuckets);		
	for (i in 1:length(w)) {
		diff = (x[i]-min);
		bucket = (diff %/% bucketLength);
		bucket
		resultY[bucket] = resultY[bucket] + w[i]*y[i];
	}
	
	data.frame(x=resultX,y=resultY/max(resultY));
	
 }

 ###### Hyperdimensional Loess
 ######
 ### hyperdimensional euclidean distance
 distance <- function(x1,x2,n=2) sum(abs(x1-x2)^n)^(1/n);
 
 loessND <- function(px,x,supp,span) {
 	smoothed = array(0,length(px))
	weights = array(0,length(x));
	d = dim(x)[2]; 
	normX = x;
	for (j in 1:dim(x)[2]) normx[,j] = normx[,j]/(max(x[,j])-min(x[,j]));

	for (p in 1:length(px)) {
		if (px[d]<min(x) || px[d]>max(x)) {
			psupp[p] = NULL;
		} else {			
			dists = array(0,length(normX));
			for (i in 1:dim(dists)[2]) dists[i] = distance(normX[i,],px[p,]);	

			for (i in 1: length(x)) weights[i] = weightfunction(dists[i],span*d^(1/d)/2);
		
			smoothed[p] = sum(weights * supp); 
		}
	}	
	smoothed;
 }

 ### weight function normalized to 1
 weightfunction <- function(x,d) {
 	if (x>d || -x>d) {
		0;
	} else {
		(1-(abs(x/d))^3)^3;
	}
 }

 ### calculates smoothed supp for a given point px
 smoothsupp <- function(px,x,supp,span) {
	psupp = array(0,length(px));
	weights = array(0,length(x));
	range = max(x)-min(x);
	for (p in 1:length(px)) {
		if (px[d]<min(x) || px[d]>max(x)) {
			psupp[p] = NULL;
		} else {			
			normalizedX = (x-px[p])/range;			
			for (i in 1: length(x)) {
				weights[i] = weightfunction(normalizedX[i],span/2);
			}
		
			psupp[p] = sum(weights * supp); 
		}
	}	
	psupp;
 }

 accumulate <- function (x, y, w, bucketSize) {
	remaining = bucketSize;
	
	resultX = array();
	resultY = array();
	newX = 0;
	newY = 0;
	
	iResult = 1;
	
	for (i in 1:length(w)) {
		weight = w[i];
		xvalue = x[i];
		yvalue = y[i];
		while (weight > 0) {
			pw = min(remaining,weight);
			newX = newX + pw*xvalue;
			newY = newY + pw*yvalue;
			remaining = remaining - pw;
			weight = weight - pw;
			if (remaining<=0) {
				resultX[iResult] = newX/bucketSize;
				resultY[iResult] = newY/bucketSize;
				iResult = iResult+1;
				newX = 0;
				newY = 0;
				remaining = bucketSize;
			}
		}
	}
	
	data.frame(x=resultX,y=resultY);
 }
