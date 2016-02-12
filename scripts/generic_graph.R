#CPP
library(readr)
library(dplyr)
library(tidyr)
library(ggplot2)

dat <- read_csv("~/R/c-openmp-benchmark.csv")
tall_dat <- gather(dat, Type, Time, 2:4)
real_dat <- dplyr::filter(tall_dat, Type=="real")
ggplot(dplyr::filter(real_dat, threads <1000), aes(x=threads,y=Time,color=Type)) + stat_summary(fun.y="mean", geom="line", size=1) + xlab("\n Number of threads (log)") +
  ylab("Time (s)\n") + ggtitle("Performance of C/OpenMP model\n") + theme_bw() + scale_x_log10() + scale_y_log10() + theme(text=element_text(size=16, family="Georgia"))
