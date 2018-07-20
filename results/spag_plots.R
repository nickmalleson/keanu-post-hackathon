library(tidyverse)
library(reshape2)
library(scales)
library(RColorBrewer)
library(gridExtra)
library(lattice)


spag_plot <- function(samples, truth, obs) {

  x_axis <- 1:ncol(samples)
  sample_col <- alpha("#ADD8E6", 0.9)
  truth_col <- "red"
  sample_col <- brewer.pal(9, "Blues")

  #create plot
  plot(1, xlim=c(0, max(x_axis)), ylim=c(0, max(samples)), type='l', xlab="Number of iterations", ylab = "Number of agents",
       main = paste("Number of agents in simulation \n as sampled from posterior (obs = ", obs, ")", sep = ""))
  print(max(x_axis))
  map(1:nrow(samples), function(x) {lines(x_axis, samples[x,], type='l', col=sample_col[x / 3])
    print(sample_col[x / 10])})
  lines(x_axis, truth, type='l', lwd=2.5, col = truth_col)
  # confidence intervals
  #lines()
  legend("bottomright", c("Samples", "Truth"), lty=c(1,1), lwd=c(2.5,2.5),col=c(sample_col[4], truth_col))
}

matrix_cols <- function(i, data, len) {
  vec <- rep(0, len)
  df <- data[[i]]

  for(j in 1:len) {
    m <- match(j, unlist(df[,1]))
    if(!is.na(m)) {
      #print(m)
      vec[j] <- df[m, 2]
    }
  }
  #print(vec)
  return(unlist(vec))
}

dist_plot <- function(samples) {
  counts <- map(samples, function(x) type_convert(as.tibble(table(x))))
  m <- map(1:length(counts), matrix_cols, counts, max(samples))
  m <- matrix(unlist(m), ncol=length(counts))
  colours = c("white", brewer.pal(9,"YlOrRd"))
  heatmap(m, Colv = NA, Rowv = NA, col=colours, breaks = c(0,1,2,3,4,5,6,7,8,9,10), main="Distribution of people in simulation as sampled from posterior (no obs)")
}



par(mfrow=c(1,2))
# no obs
samples_no_obs <- read_csv("Samples_OBSERVEfalseobInterval0_numSamples100_numTimeSteps1000_numRandomDoubles10_totalNumPeople700_dropSamples0_downSample_sigmaNoise0.1_downsample3_timeStamp1532077346294.csv", col_names = FALSE)
truth_no_obs <- read_csv("Truth_OBSERVEfalseobInterval0_numSamples100_numTimeSteps1000_numRandomDoubles10_totalNumPeople700_dropSamples0_downSample_sigmaNoise0.1_downsample3_timeStamp1532077346294.csv", col_names = FALSE)
spag_plot(samples_no_obs, truth_no_obs, FALSE)


# with obs
samples_with_obs <- read_csv("Samples_OBSERVEtrueobInterval1_numSamples100_numTimeSteps1000_numRandomDoubles10_totalNumPeople700_dropSamples0_downSample_sigmaNoise0.1_downsample3_timeStamp1532077346294.csv", col_names = FALSE)
truth_with_obs <- read_csv("Truth_OBSERVEtrueobInterval1_numSamples100_numTimeSteps1000_numRandomDoubles10_totalNumPeople700_dropSamples0_downSample_sigmaNoise0.1_downsample3_timeStamp1532077346294.csv", col_names = FALSE)
spag_plot(samples_with_obs, truth_with_obs, TRUE)


# and distributions (needs tweaking)
dist_plot(samples_no_obs)

ncol(samples_with_obs)
