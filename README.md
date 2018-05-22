Voronoi-treemaps-Visualisation
======
Visualisation of complex hierarchical data is often times cumbersome. 
Voronoi treemaps enable the user to quickly identify the contribution of specific datapoints to the whole dataset.
The hierarchical approach allows for easy filtering of the datasets and only the current level of interest is displayed.

Quick Setup
=====
1. <code>git clone https://github.com/qbicsoftware/voronoi-treemaps-GUI</code>
2. Import the project as a maven project by importing the pom.xml file
3. Create a maven run configuration: jetty:run
4. Run the program -> http://localhost:8080/ is the default localhost 

How-to
=====
1. Upload a csv or tsv file by clicking 'Choose file'. Examples can be obtained from here: [examples](https://github.com/qbicsoftware/voronoi-treemap-cli/tree/development/examples).
2. Select your columns of choice. For the a_24_cancer_pathway_2136_elements.tsv from the [examples](https://github.com/qbicsoftware/voronoi-treemap-cli/tree/development/examples), you could for example select:
title
pathway_id
ID 
X0h_dnE47dox0h_vs_dnE47noDox0hlog2FC
X0h_dnE47dox0h_vs_RFPdox0hlog2FC 
X0h_dnE47noDox0h_vs_RFPnoDox0hlog2FC
3. Press 'Create Treemap'. The treemap will now be created and displayed in the browser. If treemap creation never finished it is likely that you did not include any real data. 

Authors
=====
The original work was done by Matthias Raffeiner. It is now maintained by Lukas Heumos.
