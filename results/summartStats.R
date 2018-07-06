library(tidyverse)
library(gtools)


spag_plot <- function(samples, truth, obs) {

  x_axis <- 1:ncol(samples)
  sample_col <- alpha("#ADD8E6", 0.9)
  truth_col <- "red"

  #create plot
  plot(1, xlim=c(0, max(x_axis)), ylim=c(0, max(samples)), type='l', xlab="Number of iterations", ylab = "Number of agents")
  map(1:nrow(samples), function(x) lines(x_axis, samples[x,], type='l', col=sample_col))
  lines(x_axis, truth, type='l', lwd=2.5, col = truth_col)
  #legend("bottomright", c("Samples", "Truth"), lty=c(1,1), lwd=c(2.5,2.5),col=c(sample_col, truth_col))
}

dataDir = "."


#Find files
samples_files <- grep("Samples_OBSERVE", list.files(path = dataDir), value=TRUE)
truth_file <-  grep("Truth", list.files(path = dataDir), value=TRUE)

#Ensure files are in correct order (mixed sort includes numerical sorting of embeded numbers)
samples_files <- mixedsort(unlist(samples_files))

# Read all the files in
samples <- map(samples_files, function(x) read_csv(x, col_names = FALSE))
truth <- read_csv(truth_file[1], col_names = FALSE)

# plot all
par(mfrow=c(3,3))
map(samples, function(x) spag_plot(x, truth, FALSE))


#Summary stats
samples_Summary <- function(df) {
  df <- as.data.frame(df)
  mean_range <- mean(apply(df, 2, function(x) max(x) - min(x)))
  #mean_IQR <- mean(apply(df, 2, function(x) IQR(x)))
}

map(samples, samples_Summary)

