Cluster-Hunter
==============

This Cluster Hunter software is a stripped down implementation of the original Geographical Cluster Hunting algorithm developed by Professor Stan Openshaw as part of the Geographical Analysis Machine (GAM).

This implementation uses the same algorithm and calculation classes as the original however extensions have been implemented to allow handling of lat lon coordinate systems, dynamic aggregation of individual point data and the use of JSON files.

This implementation uses:
<ul>
<li>Geodesy geographical coordinate calculator developed by Mike Gavaghan available from: <a href = "http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula-java/">http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula-java/</a></li>
<li>JSON Java library available from: <a href = "http://www.json.org">http://www.json.org</a></li>
</ul>

The latest update optionally integrates the cluster hunter algorithm with the FMF (Note command line operation has not been altered). To take advantage of the new user interface simply download the latest FMF release and place the ClusterHunter.jar in the root of the FMF folder next to the exec.jar and run the FMF the menu and execution of the algorithm will be automatic. Instructions for use of the algorithm can be found in the Cluster Hunter.pdf document. The cluster hunter algorithm now has two dependencies (stored in the dependencies folder for easy access if you wish to develop further) SharedObjects.jar and swing-layout-1.0.4.jar.  These two jar files are deployed with the FMF so you do not need to redeploy if using with the interface only if stand alone command line operation is required.
