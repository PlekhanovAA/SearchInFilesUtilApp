<h1>SearchInFilesUtilApp</h1>

An application for searching strings in files from a folder.

<h3>Preparation</h3>

Configure the startup parameters in the file **config.properties**:

* parameter <em>multiThread.mode</em> specifies the mode of operation of the application (multithreaded or single-threaded);
* parameter <em>folder.path</em> specifies the folder with the files for which the application should work;
* parameter <em>what.need.to.find</em> specifies which string to find.

<h3>Launch</h3>

* take the compiled code and package it in **SearchInFilesUtilApp-jar-with-dependencies.jar** file (maven <em>package</em> goal);
* launch the application from the console with the command below.
```
java -jar SearchInFilesUtilApp-jar-with-dependencies.jar
```