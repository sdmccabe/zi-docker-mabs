#CPP
library(readr)
library(dplyr)
library(tidyr)
library(ggplot2)
library(scales)
library(ggthemes)
library(sitools)
library(magrittr)

perfplot <- function (i) {
  # take an argument 1, 2, 3
  if (i != 1 && i != 2 && i != 3) {
    stop("Invalid argument")
  }
  
  setwd(paste0("~/Dropbox/zi-docker/results/experiment-", i))
  
  languages <- c("cpp", "c", "c_openmp", "go", "scala",  "java_new", "java_old", "clojure") 

  #gather(dat, Type, Time, 2:4)
  #java_new_results <- read_csv("java-benchmark-new.csv")
  #java_old_results <- read_csv("java-benchmark-old.csv")
  for (x in languages) {
    assign(paste0(x, "_results"), 
           gather(read_csv(paste0(gsub("_", "-", x), "-benchmark.csv")), 
                  Type, Time, 2:4))
    assign(paste0(x, "_results"), 
           dplyr::mutate(get(paste0(x, "_results")), 
                         Language = x))
  }
  
  for (x in languages) {
    assign(paste0(x, "_wall_results"), 
           dplyr::filter(get(paste0(x, "_results")), 
                         Type == "real"))
  }
  
  big_wall_results <- data.frame()
  for (x in languages) {
        avg <- mean(get(paste0(x, "_wall_results"))[1:9,]$Time)
        print(avg)
        assign(paste0(x, "_wall_results"), 
               dplyr::mutate(get(paste0(x, "_wall_results")), 
                             speedup = avg/Time))
        big_wall_results <- rbind(big_wall_results, 
                                  get(paste0(x, "_wall_results")))
  }
  big_wall_results$Language <- factor(big_wall_results$Language)
  
  p1 <- ggplot(big_wall_results, aes(x=threads, y=Time, color = Language)) + 
    stat_summary(fun.y="mean", geom="line", size=1) + 
    xlab("\n Number of threads (log)") +
    ylab("Wall time (s)") + 
    ggtitle(paste("Performance of models\nExperiment", i)) + 
    theme_bw() + 
    scale_x_log10() + 
    scale_y_log10() + 
    theme(text=element_text(size=12, family="Georgia")) +
    annotation_logticks() 
  
  p2 <- ggplot(big_wall_results, aes(x=threads, y=speedup, color = Language)) + 
    stat_summary(fun.y="mean", geom="line", size=1) + 
    xlab("\n Number of threads (log)") +
    ylab("Speedup") + 
    ggtitle(paste("Speedup of models\nExperiment", i)) + 
    theme_bw() + 
    scale_x_log10() + 
    scale_y_log10() + 
    theme(text=element_text(size=12, family="Georgia")) +
    annotation_logticks(sides = "bl") 
  
   p3 <- ggplot(big_wall_results, aes(x=Language, y=memory, fill=Language)) + 
    stat_summary(fun.y="mean", geom="bar", size=1) + 
    xlab("Language") +
    ylab("Memory used") + 
    ggtitle(paste("Memory usage of models\nExperiment", i)) + 
    theme_bw() + 
    scale_y_log10("Memory used", labels = f2si, breaks = c(1e+03, 1e+06, 1e+09)) +
    theme(text=element_text(size=12, family="Georgia")) +
    annotation_logticks(sides="l") +
    scale_fill_discrete(guide=FALSE)
  #annotation_logticks(sides = "l") 
   
   ggsave(paste0("../experiment-", i, "-performance.png"), p1)
   ggsave(paste0("../experiment-", i, "-speedup.png"), p2)
   ggsave(paste0("../experiment-", i, "-memory.png"), p3)
}
perfplot(1)
perfplot(2)
perfplot(3)
