#all: results graphs 


graphs: # Creates a graph from GraphViz format
	# Reads all .dot files in the directory and converts them into pdfs
	# Details: https://github.com/nickmalleson/keanu-post-hackathon/issues/2
	# Requires GraphViz (e.g. on mac `brew install graphviz`)
	for f in *.dot; do \
		echo "Converting file $$f" ; \
		dot -Tpdf $$f > $$f.pdf ; \
		open $$f.pdf ; \
	done 
	
clean:
	rm -r *.dot *.pdf *.csv *_cache *_files cache
	


resultsB:
	Rscript -e "require(knitr) ; require(markdown) ;knit('ResultsSummary-SimpleB.Rmd', 'ResultsSummary-SimpleB.md'); markdownToHTML('ResultsSummary-SimpleB.md', 'ResultsSummary-SimpleB.html');"
	rm ResultsSummary-SimpleB.md
	open ResultsSummary-SimpleB.html
