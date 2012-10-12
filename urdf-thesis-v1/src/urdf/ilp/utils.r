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
		#print(c(x[i], min, bucketwidth));
		bucket = ceiling((x[i]-min)/bucketwidth);
		#print(bucket);
		if (length(bucket)>0 && !is.na(bucket)) {
			if (bucket==0) bucket=1;
			dist[bucket] = dist[bucket] + count[i];
		}
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

compareGroupDists <- function(x,count,group,buckets,xlab="x",ylab="y",divThreshold=0.35, plot=FALSE) {
	min = min(x);
	max = max(x);
	rootDist = histogramize(x,count,buckets,min,max);
	rootSupp = sum(rootDist);
	
	if (plot==TRUE) {
		toplot = normalize(rootDist); toplot[length(toplot)+1] = toplot[length(toplot)];
		plot(toplot,type="s",xlab=xlab,ylab=ylab);
	}
	
	leavesDist = matrix(0,length(rootDist),length(unique(group)));
	col=array(0,length(unique(group)));
	groupProperties = data.frame(group=col,kldiv=col,crossent=col,entropy=col,supp=col,i=col);
	colors = rainbow(length(unique(group)));
	legend = array(0,1);
	legendcolors = array(0,1);
	ilegend = i;
	i=1;
	for (g in unique(group)) {
		print(paste("  (",g,")"));
		leavesDist[,i] = histogramize(x[which(group==g)],count[which(group==g)],buckets,min,max);
		kldiv = kldivergence(rootDist,leavesDist[,i]);
		
		if (plot==TRUE) {
			toplot = normalize(leavesDist[,i]); toplot[length(toplot)+1] = toplot[length(toplot)];
			if (kldiv >= divThreshold) {
				points(toplot,type="s",col=colors[i]);
				legend[ilegend] = kldiv;
				legendcolors[ilegend] = colors[i];
				ilegend = ilegend+1;
			} else {
				points(toplot,type="s",col=rgb(0.8,0.8,0.8,0.2));
			}
		}
		
		groupProperties$group[i] = g;
		groupProperties$kldiv[i] = kldiv;
		groupProperties$crossent[i] = crossentropy(rootDist,leavesDist[,i]);
		groupProperties$entropy[i] = entropy(leavesDist[,i]);
		groupProperties$supp[i] = sum(leavesDist[,i]);
		groupProperties$i[i] = i;
		
		print(paste("   kldiv =",groupProperties$kldiv[i]));
		print(paste("     acc =",groupProperties$supp[i]/rootSupp));
		
		
		i = i+1;
	}
	
	if (plot==TRUE) {
		legend("topright",legend=groupProperties$kldiv,col=colors,cex=0.7,pch=15,lty=2);
	}

	list(props=groupProperties[order(groupProperties$kldiv,decreasing=TRUE),],dist=leavesDist);
}

combineTwoGroups <- function (dataset, i1, i2, g1, g2, numBuckets=100, min, max, divThreshold=0.3, suppThreshold=25) {
	#if (g1==NULL) {
	#	g1 = compareGroupDists(dataset[,1],dataset[,dim(dataset)[2]],dataset[,i1],numBuckets,xlab=colnames(dataset)[1],ylab=colnames(dataset)[i1]);
	#} 
	#if (g2==NULL) {
	#	g2 = compareGroupDists(dataset[,1],dataset[,dim(dataset)[2]],dataset[,i2],numBuckets,xlab=colnames(dataset)[1],ylab=colnames(dataset)[i2]);
	#}
	
	row=1;
	for (const1 in g1$props$i) {
		constg1 = g1$props$group[const1];
		if (g1$props$kldiv[which(g1$props$group==constg1)]) {
			subset = dataset[which(dataset[,i1]==constg1),];
			col=1;
			for (const2 in g2$props$i) {
				constg2 = g2$props$group[const2];
				if (g2$props$kldiv[which(g2$props$group==constg2)]) {
					finalset = subset[which(subset[,i2]==constg2),]
					dist = histogramize(finalset[,1],finalset[,dim(dataset)[2]],buckets=numBuckets,min=min,max=max);
					kl1 = kldivergence(dist,g1$dist[,const1]);
					kl2 = kldivergence(dist,g2$dist[,const2]);
					
					minkl = min(kl1,kl2);
					maxkl = max(kl1,kl2);
					maxacc = 0.0;
					maxacc = max(max(dist/g1$dist[,const1]),max(dist/g2$dist[,const2]));
					minacc = 0.0;
					minacc = min(max(dist/g1$dist[,const1]),max(dist/g2$dist[,const2]));
					#print(paste(constg1,"and",constg2,"passed average threshold: kldiv1 =",kl1,"kldiv1 =",kl2));
					if (maxkl >= divThreshold) {
						print(paste("(",constg1,",",constg2,")","kl1 = ",kl1," kl2 =",kl2));
						if (maxkl > 1.0) {
							plot(normalize(dist),type="s",ylim=c(0,1));
							points(normalize(g1$dist[,const1]),type="s",col="red");
							points(normalize(g2$dist[,const2]),type="s",col="blue");
							#for (w in 1:10000000) {}
							savePlot(paste("/var/tmp/",constg1,"-",constg2,sep=""),type="png");
						}
						if (length(maxacc)>0 && !is.na(maxacc) && !is.nan(maxacc) && maxacc>=0.4) {
							print(paste(constg1,"and",constg2," kldiv =",minkl));
							plot(dist/max(dist),type="s",lty=2,col=c(rgb(.5,.5,.5,.5)),ylim=c(0,1));points(dist/g1$dist[,const1],type="s",col="red");points(dist/g2$dist[,const2],type="s",col="blue");
							
							for (i in 1:10000000){}
						}
					}
					col = col+1;
				}
			}
			
			row = row+1;
		}
	}
}



lookForSimilarConstants <- function(comparisonResult, divThreshold, simThreshold) {
	props = comparisonResult$props;
	dists = comparisonResult$dist;
	divergence = matrix(0,max(props$group),max(props$group));
	for (i in 1:(length(props$i)-1)) {
		for (j in (i+1):length(props$i)) {
			if (length(props$kldiv[i])>0 && !is.na(props$kldiv[i]) && props$kldiv[i]>divThreshold) {
				kldiv1 = kldivergence(dists[,props$i[i]] , dists[,props$i[j]]);	
				if (length(kldiv1)>0 && !is.na(kldiv1) && kldiv1<simThreshold) {
					print(paste(props$group[i],"is similar to",props$group[j],"kldiv =",kldiv1));
				}	
			}
		}
	}
	divergence;	
}

evaluateDataSet <- function(dataset, avgdivThreshold=0.1) {
	numCol = dim(dataset)[2]
	count = dataset[,numCol];
	x = dataset[,1];
	xmin = min(x);
	xmax = max(x);
	names = colnames(dataset);
	
	groups = array(list(NULL),numCol-1);
	
	for (i in 2:(numCol-1)) {
		print(paste("[",names[i],"]"));
		igroup = compareGroupDists(x,count,dataset[,i],100,xlab=names[1],ylab=names[i],0.2);
		#savePlot(paste("/home/adeoliv/Desktop/income-",names[i]),type="png");
		simThreshold = min(0.05,max(igroup$props$kldiv)/dim(igroup$props)[1]);
		isim = lookForSimilarConstants(igroup,avgdivThreshold,simThreshold);
		
		groups[[i]] = igroup;
	}
	
	print("Combining properties");
	i=14;j=17;
	#for (i in 2:(numCol-2)) {
		iAvgDiv = mean(groups[[i]]$props$kldiv);
		if (iAvgDiv >= avgdivThreshold) {
			#for (j in (i+1):(numCol-1)) {
				jAvgDiv = mean(groups[[j]]$props$kldiv);
				if (jAvgDiv >= avgdivThreshold) {
					print(paste(colnames(dataset)[i],"with",colnames(dataset)[j]));
					combineTwoGroups(df,i,j,groups[[i]],groups[[j]],numBuckets=100,min=xmin,max=xmax);
				} 
			#}
		}
	#}	
	
	#compareGroupDists(dataset, numBuckets=100, i1=14, i2=17, divThreshold=0.3, suppThreshold=25)
}


crossentropy <- function(x,y) {
	-sum((normalize(x)) * log(normalize(y)));
}

kldivergence <- function(x,y) {
	x = x + 1;
	y = y + 1;
	crossentropy(x,y) - entropy(x);
}

normalize <- function(x) {
	x/sum(x);
}





combineTwoGroupsStandAlone <-function (dataset, numBuckets=100, i1=14, i2=17, divThreshold=0.3, suppThreshold=25) {
	count = dataset[,dim(dataset)[2]];
	x = dataset[,1];
	xmin = min(x);
	xmax = max(x);
	
	#for (g in c(14,17)) {
	g1 = compareGroupDists(x,count,dataset[,i1],numBuckets,xlab=colnames(dataset)[1],ylab=colnames(dataset)[i1]);
	g2 = compareGroupDists(x,count,dataset[,i2],numBuckets,xlab=colnames(dataset)[1],ylab=colnames(dataset)[i2]);
	
	result = matrix(0,length(g1$props$group),length(g2$props$group));
	
	row=1;
	for (constg1 in g1$props$group) {
		if (g1$props$kldiv[which(g1$props$group==constg1)]) {
			subset = dataset[which(dataset[,i1]==constg1),];
			col=1;
			for (constg2 in g2$props$group) {
				if (g2$props$kldiv[which(g2$props$group==constg2)]) {
					finalset = subset[which(subset[,i2]==constg2),]
					dist = histogramize(finalset[,1],finalset[,dim(dataset)[2]],buckets=numBuckets,min=xmin,max=xmax);
					kl1 = kldivergence(dist,g1$dist[constg1]);
					kl2 = kldivergence(dist,g2$dist[constg2]);
					result[row,col] = min(kl1,kl2);
					col = col+1;
				}
			}
			
			row = row+1;
		}
	}
	
	#}
	result;
}














