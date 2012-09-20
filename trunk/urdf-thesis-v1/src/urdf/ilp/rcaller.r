### 
isGood = FALSE;

dfOriginal = data.frame(x=x,y=y); 

### Calculates weighted average
avg = sum(y*w)/sum(w);

### Creates test data for plotting
xtest = seq(min(x), max(x), by=(max(x)-min(x))/numTestPoints);
dfpred = data.frame(x=xtest);

plot(dfOriginal,ylim=c(0,1));
#title(title);
lines(c(min(x),max(x)),c(avg,avg), col="red",lty=2);
lines(c(min(x),max(x)),array(accuracyThreshold,2),col="green",lty=2);

### Bucketizes data
dfAcc = accumulate(x,y,w,sum(w)/numBuckets);
lines(dfAcc,col="green");


### Calculate data weight distribution
dfHist = weighthist(x,array(1,length(x)),w,100);
lines(dfHist,col="yellow",type="s");

### Calculate entropy ratio
entp = entropy(dfOriginal$y);
maxentp = -log(1/length(dfOriginal$y));
ratio = entp/maxentp;
title(ratio)

### If ratio is smaller than threshould, apply Loess smoothing on data
#if (ratio < entropyThreshold) {
#for (span in seq(0.5,1.0,by=0.25)) {


color = c(rgb(span^2,0,0))
### Smooths y by some span
#modelAcc = loess(y~x,span=span,data=dfAcc,ylim=range(0,1));
modelAcc = loess(y~x,span=span,data=dfOriginal,weights=w);	
ypred = predict(modelAcc, dfpred);
dfSmoothed = data.frame(x=xtest,y=ypred);
lines(dfSmoothed,col=color);

ysupp = smoothsupp(xtest,x,s,span);
lines(xtest,ysupp/sum(s),col=color,lty=2);
#if (exists(xHead) && existis(wHead)) {
#	yhead = smoothsupp(xtest,xHead,wHead,span);
#} else {
yhead = array(headSize,length(xtest));
lines(xtest,ysupp/headSize,col="magenta");

#peak = optimize(function(x)predict(modelAcc,x), c(min(dfAcc$x),max(dfAcc$x)), maximum=TRUE);

#peak = optimize(function(x)predict(modelAcc,x), c(min(dfOriginal$x),max(dfOriginal$x)), maximum=TRUE);			
#points(peak$maximum,peak$objective,pch=15,col=color);	
#peak = optimize(function(x)predict(modelAcc,x), c(peak$max,max(dfOriginal$x)), maximum=TRUE);	
#points(peak$maximum,peak$objective,pch=15,col=color);
#peak = optimize(function(x)predict(modelAcc,x), c(peak$max + 0.1,max(dfOriginal$x)), maximum=TRUE);	
#points(peak$maximum,peak$objective,pch=15,col=color);					

#}
#}
