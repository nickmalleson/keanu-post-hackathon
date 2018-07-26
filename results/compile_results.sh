Rscript -e "require(knitr) ; require(markdown) ;knit('ResultsSummary.Rmd', 'ResultsSummary.md'); markdownToHTML('ResultsSummary.md', 'ResultsSummary.html');"
rm ResultsSummary.md
