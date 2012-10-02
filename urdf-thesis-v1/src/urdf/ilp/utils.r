#setwd("/home/adeoliv/Documents/Thesis/r");
#setwd("C:/Users/ande01/workspace1/urdf-thesis/src/urdf/ilp/");
setwd("/home/adeoliv/workspace/urdf-thesis/src/urdf/ilp/");

library("stats")
library("entropy");
library("outliers");
library("scatterplot3d");
library("rgl");
#library("aspace");



######
###
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
### calculate N-dimensional Loess regression to points px, with (x,supp) as training data
suppND <- function(px,x,supp,span) {
	smoothed = array(0,dim(px)[1])
	weights = array(0,dim(x)[1]);
	d = dim(x)[2]; 
	
	minX = array(0,d);
	for (j in 1:dim(x)[2]) minX[j]=min(x[,j]);
	
	normX = x;
	for (j in 1:dim(x)[2]) normX[,j] = (normX[,j]-minX[j])/(max(x[,j])-minX[j]);
	normPX = px;
	for (j in 1:dim(px)[2]) normPX[,j] = (normPX[,j]-minX[j])/(max(x[,j])-minX[j]);
	
	for (p in 1:dim(px)[1]) {
		#if (px[d]<min(x) || px[d]>max(x)) {
		#	psupp[p] = NULL;
		#} else {			
			dists = array(0,dim(normX)[1]);
			
			for (i in 1:dim(normX)[1]) dists[i] = distance(normX[i,],normPX[p,]);	
			
			for (i in 1: dim(normX)[1]) weights[i] = weightfunction(dists[i],span*d^(1/d)/2);
			
			smoothed[p] = sum(weights * supp); 
		#}
	}	
	smoothed;
}

######
### weight function normalized to 1
weightfunction <- function(x,d) {
	if (x>d || -x>d) {
		0;
	} else {
		(1-(abs(x/d))^3)^3;
	}
}

#######
### calculates smoothed supp for a given point px
smoothsupp <- function(px,x,supp,span) {
	psupp = array(0,length(px));
	weights = array(0,length(x));
	range = max(x)-min(x);
	for (p in 1:length(px)) {
		if (px[p]<min(x) || px[p]>max(x)) {
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
#######
### calculates smoothed accuracy for a given point px
smoothacc <- function(px,x,acc,span) {
	pacc = array(0,length(px));
	weights = array(0,length(x));
	range = max(x)-min(x);
	for (p in 1:length(px)) {
		if (px[p]<min(x) || px[p]>max(x)) {
			pacc[p] = NULL;
		} else {			
			normalizedX = (x-px[p])/range;			
			for (i in 1: length(x)) {
				weights[i] = weightfunction(normalizedX[i],span/2);
			}
			
			pacc[p] = sum(weights * acc)/sum(weights); 
		}
	}	
	pacc;
}

######
###
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

### converts a table (query result format) into a Chi-Squared test table with N buckets
createChiSqTable <- function (data, numBuckets, w=array(1,dim(data)[1])) {
	minX1 = min(data[,1]);
	minX2 = min(data[,2]);
	rangeX1 = max(data[,1]) - minX1;
	rangeX2 = max(data[,2]) - minX2;
	buckIntervalX1 = rangeX1/numBuckets;
	buckIntervalX2 = rangeX2/numBuckets;
	output = matrix(0, numBuckets, numBuckets);
	for (i in 1:dim(data)[1]) {
		row = min( (data[i,1]-minX1) %/% buckIntervalX1 + 1, numBuckets);
		col = min( (data[i,2]-minX2) %/% buckIntervalX2 + 1, numBuckets);
		output[row,col] = output[row,col] + w[i];
	}
	data.frame(output);
}

build2Dgrid <- function (x, resolution) {
	points = resolution^2;
	minx1 = min(x[,1]);
	minx2 = min(x[,2]);
	x1step = (max(x[,1])-minx1)/(resolution-1);
	x2step = (max(x[,2])-minx2)/(resolution-1);
	xgrid = matrix(0,points,2);
	for (i in 0:(resolution-1)) {
		for (j in 0:(resolution-1)) {
			p = 1 + i*resolution + j;
			xgrid[p,1] = minx1 + i*x1step;
			xgrid[p,2] = minx2 + j*x2step;
		}
	}
	xgrid;
}

simple1stDerivative <- function(x,y) {
	yprime = array(0,length(y)-1);
	x = (x-min(x))/(max(x)-min(x));
	x = (x-min(x))/(max(x)-min(x));
	for (i in  1:(length(y)-1)) {
		yprime[i] = (y[i+1]-y[i])/(x[i+1]-x[i]);
	}
	yprime;
}

### Quick 2nd derivative calculation "O(n)"
simple2ndDerivative <- function (x,y) {
	diffs = 0;
	yprime = simple1stDerivative(x,y);
	for (i in 1:(length(yprime)-1)) {
		diffs = diffs + abs(yprime[i+1]-yprime[i]);
	}
	diffs;
}

angleAvg1 <- function(x,y) {
	sumAngles = 0;
	yprime = simple1stDerivative(x,y);
	for (i in 1:(length(yprime)-1)) {
		sumAngles = sumAngles + abs(atan(yprime[i+1])-atan(yprime[i]));
	}
	avgAngleRad = sumAngles/(length(yprime)-1);
	avgAngleRad/(2*pi)*360
}

angleAvg <- function(x,y) {
	sumAngles = 0;
	for (i in 1:(length(y)-2)) {
		arccosine = (x[i+1]-x[i])*(x[i+2]-x[i+1]) + (y[i+1]-y[i])*(y[i+2]-y[i+1])
		sumAngles = sumAngles + abs(pi-acos(arccosine));
	}
	avgAngleRad = sumAngles/(length(y)-2);
	avgAngleRad/(2*pi)*360
}

histogramize <- function(x,count,buckets,min,max) {
	bucketwidth = (max-min)/buckets;
	dist = array(0,buckets);
	for (i in 1:length(x)) {
		bucket = ceiling((x[i]-min) / bucketwidth);
		if (bucket==0) bucket = 1;
		dist[bucket] = dist[bucket] + count[i]; 
	}
	dist;
}

unihistogramize <- function(x,count,buckets,min,max) {
	df = data.frame(x=x, c=count);
	df = df[order(x),];
	countsPerBucket = sum(count)/buckets;
	separators = array(0,buckets);
	histogram = array(0,buckets);
	i=1;
	counted=0;
	for (c in 1:length(count)) {
		counted = counted + df$c[c];
		if (counted > i*countsPerBucket) {
			histogram[i] = histogram[i] + df$c[c]; 
			i = i+1;
			separators[i] = df$x[c];
		}
	}
	data.frame(sep=separators,count=histogram);
}

compareGroupDists <- function(x,count,group,buckets) {
	min = min(x);
	max = max(x);
	rootDist = normalize(histogramize(x,count,buckets,min,max));
	toplot = rootDist; toplot[length(toplot)+1] = toplot[length(toplot)];
	#plot(rootDist,type="s");
	#plot(histogramize(x[which(group==16)],count[which(group==16)],buckets,min,max)/histogramize(x,count,buckets,min,max),type="s",col="magenta");
	#plot(histogramize(x[which(group==16)],count[which(group==16)],buckets,min,max)/histogramize(x,count,buckets,min,max),type="s",col="magenta");
	plot(toplot,type="s");
	leavesDist = matrix(0,length(unique(group)),length(rootDist));
	kldiv = matrix(0,length(unique(group)),4);
	colors = rainbow(length(unique(group)));
	i=1;
	for (g in unique(group)) {
		leavesDist[i,] = normalize(histogramize(x[which(group==g)],count[which(group==g)],buckets,min,max));
		toplot = leavesDist[i,]; toplot[length(toplot)+1] = toplot[length(toplot)];
		#points(leavesDist[i,],type="s",col=g,lty=2);
		if (g==10 || g==11 || g==16) points(toplot,type="s",col=g,lty=2);		
			
		kldiv[i,1] = g;
		kldiv[i,2] = kldivergence(rootDist,leavesDist[i,]);
		kldiv[i,3] = crossentropy(rootDist,leavesDist[i,]);
		kldiv[i,4] = entropy(leavesDist[i,]);
		i = i+1;
	}
	points(histogramize(x[which(group==16)],count[which(group==16)],buckets,min,max)/histogramize(x,count,buckets,min,max),type="s",col="magenta");
	
	
	kldiv;
}

crossentropy <- function(x,y) {
	-sum((x/sum(x)) * log((y+0.00001)/sum(y)));
}

kldivergence <- function(x,y) {
	crossentropy(x,y) - entropy(x);
}

normalize <- function(x) {
	x/sum(x);
}
















