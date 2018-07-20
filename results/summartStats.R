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


#Find files
samples_files <- grep("Samples_OBSERVE", list.files(path = dataDir), value=TRUE)
truth_file <-  grep("Truth", list.files(path = dataDir), value=TRUE)

#Ensure files are in correct order (mixed sort includes numerical sorting of embeded numbers)
samples_files <- mixedsort(unlist(samples_files))

# Read all the files in
samples <- map(samples_files, function(x) read_csv(paste("7000samples/", x, sep=""), col_names = FALSE))
truth <- read_csv(truth_file[1], col_names = FALSE)
truth <- read_csv(paste("7000samples/", truth_file[1], sep=""), col_names = FALSE)

# This should be greped instead
obIntervals <- c(0,1,5,10)


# plot all
par(mfrow=c(2,2))
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

