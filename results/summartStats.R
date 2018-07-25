library(tidyverse)
library(gtools)


spag_plot <- function(samples, truth, obs) {

  x_axis <- 1:ncol(samples)
  sample_col <- alpha("#ADD8E6", 0.9)
  truth_col <- "red"

  #create plot
  plot(1, xlim=c(0, max(x_axis)), ylim=c(0, max(samples)), type='l',
       xlab="Number of iterations", ylab = "Number of agents",
       main = paste("Observation Interval =", obs, sep =' '))
  map(1:nrow(samples), function(x) lines(x_axis, samples[x,], type='l', col=sample_col))
  lines(x_axis, truth, type='l', lwd=2.5, col = truth_col)

  legend("bottomright", c("Samples", "Truth"), lty=c(1,1), lwd=c(2.5,2.5),col=c(sample_col, truth_col))
}

dataDir = "./plot"
setwd("/Users/nick/research_not_syncd/git_projects/keanu-post-hackathon/results/")


#Find files
samples_files <- grep("Samples_obInterval", list.files(path = dataDir), value=TRUE)
truth_file <-  grep("Truth", list.files(path = dataDir), value=TRUE)

#Ensure files are in correct order (mixed sort includes numerical sorting of embeded numbers)
samples_files <- mixedsort(unlist(samples_files))

# Read all the files in
samples <- map(samples_files, function(x) read_csv(paste("plot/", x, sep=""), col_names = FALSE))
truth <- read_csv(paste("plot/", truth_file[1], sep=""), col_names = FALSE)

# This should be greped instead
#obIntervals <- c(0,1,5,10)
#obIntervals <- c(0,1)
obIntervals <- c(0,1,2,5,10,20,50,100)


# plot all
par(mfrow=c(3,3))
map2(samples, obIntervals, function(x, obInterval) spag_plot(x, truth, obInterval))


#Summary stats
samples_Summary <- function(df, obInterval) {
  df <- as.data.frame(df)
  mean_range <- mean(apply(df, 2, function(x) max(x) - min(x)))
  median_range <-  median(apply(df, 2, function(x) max(x) - min(x)))
  mean_IQR <- mean(apply(df, 2, function(x) IQR(x)))
  median_IQR <- median(apply(df, 2, function(x) IQR(x)))
  mean_sd <- mean(apply(df, 2, function(x) sd(x)))
  median_sd <- median(apply(df, 2, function(x) sd(x)))
  data.frame(observation_Interval=obInterval,
             mean_sd=mean_sd, median_sd=median_sd,
             mean_IQR=mean_IQR, median_IQR=median_IQR,
             mean_range=mean_range, median_range=median_range
  )

}


summaryStats <- do.call("rbind", map2(samples, obIntervals, samples_Summary))
summaryStats

write_csv(summaryStats, "observation_intervals_summary.csv")


# See if later samples are closer to the truth.
s <- samples[[which(obIntervals == 1)]] # Just look at those with an observation interval of 1
# Calculate the euclidean distance of the sample away from the truth and see how this changes with sample number
x1 <- as.vector(t(truth)) # truth as a vector (not a load of tibble columns)
euclidean.dist <- sapply(X=1:nrow(s), FUN=function(x) {  
  x2 <- as.vector(t(s[x,])) # A row from the samples as a vector
  return(dist(rbind(x1,x2))) # Return euclidean distance (https://stackoverflow.com/questions/5559384/euclidean-distance-of-two-vectors)
  }  )
plot(euclidean.dist)







 XXXX DELETE BELOW .... 
 
 
 
 

plot(x=1:ncol(truth), y=as.vector(t(truth)), type='l')



plot(1, xlim=c(0, ncol(s)), ylim=c(0, max(s)), type='l',
     xlab="Number of iterations", ylab = "Number of agents")
lines(x=1:ncol(truth), y=truth, col="red")
lines(x=1:ncol(truth), y=s[1,], type='l', col="blue")





map(1:nrow(samples), function(x) lines(x_axis, samples[x,], type='l', col=sample_col))
lines(x_axis, truth, type='l', lwd=2.5, col = truth_col)