### 
isGood = FALSE;

dfOriginal = data.frame(x=x,y=y); 

selfsuffX = array(0);
selfsuffY = array(0);
for (i in 1:length(s)) {
	if (s[i]>supportThreshold && y[i]>accuracyThreshold) {
		selfsuffX[i]=x[i];
		selfsuffY[i]=y[i]
	}
}

### Calculates weighted average
avg = sum(y*w)/sum(w);

### Creates test data for plotting
xtest = seq(min(x), max(x), by=(max(x)-min(x))/numTestPoints);
dfpred = data.frame(x=xtest);

plot(dfOriginal,ylim=c(0,1),col=c(rgb(1,1,1)));
maxw = max(w); for (i in 1:length(x)) {tone=1-w[i]/maxw; points(x[i],y[i],col=c(rgb(tone,tone,tone,tone)));}
points(selfsuffX,selfsuffY,col="red",pch=8);
#title(title);
lines(c(min(x),max(x)),c(avg,avg), col="red",lty=2);
lines(c(min(x),max(x)),array(accuracyThreshold,2),col="green",lty=2);

### Bucketizes data
dfAcc = accumulate(x,y,w,sum(w)/numBuckets);
points(dfAcc,col="blue");


### Calculate data weight distribution
dfHist = weighthist(x,array(1,length(x)),w,100);
lines(dfHist,col="yellow",type="s");

### Calculate entropy ratio
entp = entropy(dfAcc$y);
maxentp = -log(1/length(dfAcc$y));
ratio = entp/maxentp;
title(paste("ratio=",ratio," angle=",angleAvg(dfAcc$x,dfAcc$y)));


### If ratio is smaller than threshould, apply Loess smoothing on data
#if (ratio < entropyThreshold) {
#for (span in seq(0.5,1.0,by=0.25)) {


color = c(rgb(span^2,0,0))
### Smooths y by some span
#modelAcc = loess(y~x,span=span,data=dfAcc,ylim=range(0,1));
modelAcc = loess(y~x,span=span/2,data=dfOriginal,weights=w,degree=0);	
ypred = predict(modelAcc, dfpred);
dfSmoothed = data.frame(x=xtest,y=ypred);
lines(dfSmoothed,col=color);


ysupp = smoothsupp(xtest,x,s,span);
lines(xtest,ysupp/sum(s),col=color,lty=2);
#if (exists(xHead) && existis(wHead)) {
#	yhead = smoothsupp(xtest,xHead,wHead,span);
#} else {
#yhead = array(headSize,length(xtest));
#lines(xtest,ysupp/headSize,col="magenta");
lines(xtest,ysupp/sum(ysupp),col="magenta");


color = c(rgb(0,0,span^2));
modelBckt = loess(y~x,span=span/2,data=dfAcc,degree=0);	
xtest = seq(min(dfAcc$x), max(dfAcc$x), by=(max(dfAcc$x)-min(dfAcc$x))/numTestPoints);
ypred = predict(modelBckt, data.frame(x=xtest));
dfSmoothed = data.frame(x=xtest,y=ypred);
lines(dfSmoothed,col=color);

ysupp = smoothsupp(xtest,dfAcc$x,dfAcc$y*sum(w)/numBuckets,span);
lines(xtest,ysupp/sum(s),col=color,lty=2);


#peak = optimize(function(x)predict(modelAcc,x), c(min(dfAcc$x),max(dfAcc$x)), maximum=TRUE);

#peak = optimize(function(x)predict(modelAcc,x), c(min(dfOriginal$x),max(dfOriginal$x)), maximum=TRUE);			
#points(peak$maximum,peak$objective,pch=15,col=color);	
#peak = optimize(function(x)predict(modelAcc,x), c(peak$max,max(dfOriginal$x)), maximum=TRUE);	
#points(peak$maximum,peak$objective,pch=15,col=color);
#peak = optimize(function(x)predict(modelAcc,x), c(peak$max + 0.1,max(dfOriginal$x)), maximum=TRUE);	
#points(peak$maximum,peak$objective,pch=15,col=color);					

#}
#}
