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
	SocialOcean.product --> Contents --> Add Required Plug-ins
If this doesn't work, go to:

	targetPlatform --> SocialOcean.target --> Environment --> Target Environment
and change the settings and click *Set as Target Platform*.

**Example data**  
The folder *example* includes a Lucene Index and a Postgres backup file.

- Create a database
- Load the .backup file into your database.
	- PgAdmin: right click on database --> 'Wiederherstellen.. (restore database..)' --> my2k.backup 
- Enter your login data into the settings/db\_config\_template.properties file and save it as settings/**db\_config.properties**

## Pre-Processing

There are three scripts, that offer some basic pre-processing.

	src/scripts:
		(1) AddCategoryScript.java
		(2) AddCategoryScript.java
		(3) IndexTweets.java

The first two (1) und (2) scripts need the following database fields:

	tweet_id, long
	tweet_content,  String

The indexing scripts (3) in the current form needs the following database fields:

	tweet_id, 			long
	tweet_creationdate,	String, timestamp of the form "yyyy-dd-MM hh:mm:ss", example: "2013-08-01 01:15:00"
	tweet_content,		String
	relationship,		String (Tweet, Followed)
	latitude,			double
	longitude, 			double
	hasurl, 			boolean
	source, 			String (=user_screenname)
	positive, 			int (result of SentiStrength.jar)
	negative, 			int (result of SentiStrength.jar)
	category, 			String (from AddCategoryScript.java)
	sentiment, 			String (from AddCategoryScript.java)

and yields the following indexed Lucene fields:

- type, StringField: what data type are you indexing. Here we use "twitter".
- id, StringField: we store the individual tweet\_id
- relationship
- category, StringField
- hasURL, StringField: boolean
- name, StringField: the user\_screenname
- sentiment, StringField (pos, neg, neu)
- neg, StringField
- pos, StringField
- tags, TextField: all #tags
- mention, TextField: all @mentions
- content, TextField: the tweet content
- geo, GeoPointField: taken from latitude and longitude

Depending on the data sources you use, you could change and adapt these fields.


## Further Reading

[http://wiki.eclipse.org/Eclipse4/RCP](http://wiki.eclipse.org/Eclipse4/RCP)

[http://www.vogella.com/tutorials/EclipseRCP/article.html](http://www.vogella.com/tutorials/EclipseRCP/article.html)
