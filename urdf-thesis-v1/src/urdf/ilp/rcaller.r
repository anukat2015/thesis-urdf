 ### 
 isGood = FALSE;

 ### Removes outliers
 #if (outliers > 0) {
 #	 for (i in: 0:round(outliers*length(x))) {
 #	 	y=rm.outliers(y);
 #	 }
 #}

 dfOriginal = data.frame(x=x,y=y); 
 
 ### Calculates weighted average
 avg = sum(y*w)/sum(w);

 ### Creates test data for plotting
 xtest = seq(min(x), max(x), by=(max(x)-min(x))/numTestPoints);
 dfpred = data.frame(x=xtest);

 #dev.new(width=16,height=9)
 plot(dfOriginal);
 #title(title);
 lines(c(min(x),max(x)),c(avg,avg), col="yellow");
 lines(c(min(x),max(x)),array(accuracyThreshold,2), col="green");

 ### Bucketizes data
 dfAcc = accumulate(x,y,w,sum(w)/numBuckets);
 lines(dfAcc,col="blue")
 

 ### Calculate data weight distribution
 dfHist = weighthist(x,array(1,length(x)),w,100);
 lines(dfHist,col="cyan");
 
 ### Calculate entropy ratio
 #entp = entropy(dfAcc$y);
 #maxentp = -log(1/length(dfAcc$y));
 entp = entropy(dfOriginal$y);
 maxentp = -log(1/length(dfOriginal$y));
 ratio = entp/maxentp;
 title(ratio)

 ### If ratio is smaller than threshould, apply Loess smoothing on data
 #if (ratio < entropyThreshold) {
	 ### Smooths y by some span
	 #modelAcc = loess(y~x,span=span,data=dfAcc,ylim=range(0,1));
	 modelAcc = loess(y~x,span=span,data=dfOriginal,ylim=range(0,1));	
	 ypred = predict(modelAcc, dfpred);
	 dfSmoothed = data.frame(x=xtest,y=ypred);
	 lines(dfSmoothed,col="red");

	 #peak = optimize(function(x)predict(modelAcc,x), c(min(dfAcc$x),max(dfAcc$x)), maximum=TRUE);
	 peak = optimize(function(x)predict(modelAcc,x), c(min(dfOriginal$x),max(dfOriginal$x)), maximum=TRUE);			
	 points(peak$maximum,peak$objective,pch=15,col="red");	

	 ysupp = smoothsupp(xtest,x,s,span);
	 lines(xtest,ysupp/headSize,col="magenta");
	 lines(xtest,ysupp/sum(s),col="magenta",lty=2);
	 
	 #peak = nlminb(objective=function(x)predict(sAcc,x),start=c(min(dfAcc$x),max(dfAcc$x)))
	 #points(peak$maximum,peak$objective,pch=16,col="red");
 #}

