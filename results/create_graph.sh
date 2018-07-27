# Creates a graph from GraphViz format
# Details: https://github.com/nickmalleson/keanu-post-hackathon/issues/2
# USAGE: ./create_graph.sp FILENAME.dot
dot -Tpdf $1 > $1.pdf
open $1.pdf 
