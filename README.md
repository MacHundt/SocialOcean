# SocialOcean
SocialOcean enables users to explore geo-tagged social media data.
In the context of my Master Thesis, it is a tailored to Echo Chamber detection.
Depending on the pre-processed features, it can be adapted for other purposes.
The tool utilizes a Lucene index and a corresponding Postgres database.
A script to create the Lucene index is included.
This repository is an Eclipse RCP project. So it possible to add further plugins.

The initial idea and a prototype was presented at the EuroVis2017.
A demonstration video, a poster and a short paper can be downloaded at: [http://socialocean.dbvis.de/eurovis2017/](http://socialocean.dbvis.de/eurovis2017/)


## Installation

###Downloads

- You will need a **Eclipse RCP** Version to run this project as an eclipse application:  
[http://www.eclipse.org/downloads/packages/eclipse-rcp-and-rap-developers/neon3](http://www.eclipse.org/downloads/packages/eclipse-rcp-and-rap-developers/neon3)  
One of the best **tutorials** to eclipse RCP are by Lars Vogella. They can be found [**here**](http://www.vogella.com/tutorials/EclipseRCP/article.html)
or from eclipse itself, see [**here**](http://wiki.eclipse.org/Eclipse4/RCP).

- Download [Postgres](https://www.postgresql.org) and install the [Postgis](http://postgis.net) extansion.

- If it is not yet included and you would like to have GUI tool for the database, you could download [PgAdmin](https://www.pgadmin.org)

- **Clone** this git reporsitory and import the project into Eclipse.

###Setup

Depending on the system that you use, you may have to adapt the configuration of the target **platform**.
But first try to change the settings at:

	SocialOcean.product --> Configuration --> Configuration File (maxosx, solaris, win32)
If this doesn't work, change go to:

	targetPlatform --> SocialOcean.target --> Environment --> Target Environment
and change the settings and click *Set as Target Platform*.

**Example data**  
The folder *example* includes a Lucene Index and a Postgres backup file.

- Create a database
- Load the .backup file into your database.
	- Section #1 (Pre-data, Daten, Post-data)


## Pre-Processing


## Used Libraries
- Lucene
- OSM
- JUNG
- GUAVA


## Further Reading

[http://wiki.eclipse.org/Eclipse4/RCP](http://wiki.eclipse.org/Eclipse4/RCP)

[http://www.vogella.com/tutorials/EclipseRCP/article.html](http://www.vogella.com/tutorials/EclipseRCP/article.html)
