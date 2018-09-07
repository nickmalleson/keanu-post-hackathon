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
	

results:
	Rscript -e "require(knitr) ; require(markdown) ;knit('ResultsSummary-Simple.Rmd', 'ResultsSummary-Simple.md'); markdownToHTML('ResultsSummary-Simple.md', 'ResultsSummary-Simple.html');"
	open ResultsSummary-Simple.html
