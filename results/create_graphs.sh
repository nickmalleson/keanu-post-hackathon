# Creates a graph from GraphViz format
# Reads all .dot files in the directory and converts them into pdfs
# Details: https://github.com/nickmalleson/keanu-post-hackathon/issues/2
for file in *.dot; do
    echo "Converting file $file"
    dot -Tpdf $file > $file.pdf
    open $file.pdf 
done
