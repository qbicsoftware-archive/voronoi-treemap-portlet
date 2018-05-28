# Voronoi Treemap Portlet

[![Build Status](https://travis-ci.org/qbicsoftware/voronoi-treemap-portlet.svg?branch=master)](https://travis-ci.org/qbicsoftware/voronoi-treemap-portlet)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/voronoi-treemap-portlet/branch/master/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/voronoi-treemap-portlet)

Visualisation of complex hierarchical data is often times cumbersome. 
Voronoi treemaps enable the user to quickly identify the contribution of specific datapoints to the whole dataset.
The hierarchical approach allows for easy filtering of the datasets and only the current level of interest is displayed.
![voronoi_2](https://user-images.githubusercontent.com/21954664/40628895-1a1274ae-62c8-11e8-8c62-5764801cd9d7.png)

Quick Setup
=====
1. <code>git clone https://github.com/qbicsoftware/voronoi-treemaps-GUI</code>
2. Import the project as a maven project by importing the pom.xml file
3. Create a maven run configuration: jetty:run
4. Run the program: http://localhost:8080/ is the default localhost 
Note: If you are running the code from the commandline simply use: mvn jetty:run .

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
X0h_RFPdox0h_vs_RFPnoDox0hlog2FC  

The screenshot above shows the described selection.

3. Press 'Create Treemap'. The treemap will now be created and displayed in the browser. If treemap creation never finished it is likely that you did not include any real data in your column selection.

Authors
=====
The original work was done by Matthias Raffeiner. It is now maintained by Lukas Heumos.

