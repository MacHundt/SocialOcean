# SocialOcean
SocialOcean enables users to explore geo-tagged social media data.
In the context of my Master Thesis, it is tailored to Echo Chamber detection.
Depending on the pre-processed features, it can be adapted for other purposes.
The tool utilizes a Lucene index and a corresponding Postgres database.
A script to create the Lucene index is included.
This repository is an Eclipse RCP project. So it enables plugin-creation.

![SocialOcean Tool Interface](./tool.png)


The initial idea and a prototype was presented at the EuroVis2017.
A demonstration video, a poster and a short paper can be downloaded at: [http://socialocean.dbvis.de/eurovis2017/](http://socialocean.dbvis.de/eurovis2017/)


## Installation

### Downloads

- You will need a **Eclipse RCP** Version to run this project as an eclipse application:  
[http://www.eclipse.org/downloads/packages/eclipse-rcp-and-rap-developers/neon3](http://www.eclipse.org/downloads/packages/eclipse-rcp-and-rap-developers/neon3)  
One of the best **tutorials** to eclipse RCP are by Lars Vogella. They can be found [**here**](http://www.vogella.com/tutorials/EclipseRCP/article.html)
or from eclipse itself, see [**here**](http://wiki.eclipse.org/Eclipse4/RCP).

- Download [Postgres](https://www.postgresql.org) and install the [Postgis](http://postgis.net) extansion.

- If it is not yet included and you would like to have GUI tool for the database, you could download [PgAdmin](https://www.pgadmin.org)


- **Clone** this git reporsitory and import the project into Eclipse.
### Setup

Depending on the system that you use, you may have to adapt the configuration of the target **platform**.
But first try to change the settings at:

	SocialOcean.product --> Configuration --> Configuration File (maxosx, solaris, win32)
	SocialOcean.product --> Contents --> Add Required Plug-ins

### Required Plug-ins

You have to ensure all required plug-ins selected. If not, you have to add all required plug-ins manually .(SocialOcean.product --> Contents --> Add Required Plug-ins)

- com.ibm.icu
- javax.annotation
- javax.inject
- javax.servlet
- javax.xml
- org.apache.batik.css
- org.apache.batik.util
- org.apache.commons.jxpath
- org.apache.commons.logging
- org.apache.felix.gogo.command
- org.apache.felix.gogo.runtime
- org.apache.felix.scr
- org.eclipse.ant.core
- org.eclipse.core.commands
- org.eclipse.core.conttenttype
- org.eclipse.core.databinding
- org.eclipse.core.databinding.beans
- org.eclipse.core.databinding.observable
- org.eclipse.core.databinding.property
- org.eclipse.core.expressions
- org.eclipse.core.filesystem
- org.eclipse.core.filesystem.win32.x86_64 ---> choose your own operating system
- org.eclipse.core.jobs
- org.eclipse.core.resources
- org.eclipse.core.resources.win32.x86_64
- org.eclipse.core.runtime
- org.eclipse.core.variables
- org.eclipse.e4.core.commands
- org.eclipse.e4.core.contexts
- org.eclipse.e4.core.di
- org.eclipse.e4.core.di.annotations
- org.eclipse.e4.core.di.extensions
- org.eclipse.e4.core.di.extensions.supplier
- org.eclipse.e4.core.services
- org.eclipse.e4.emf.xpath
- org.eclipse.e4.ui.bindings
- org.eclipse.e4.ui.css.core
- org.eclipse.e4.ui.css.swt
- org.eclipse.e4.ui.css.theme
- org.eclipse.e4.ui.di
- org.eclipse.e4.ui.model.workbench
- org.eclipse.e4.ui.services
- org.eclipse.e4.ui.widgets
- org.eclipse.e4.ui.workbench
- org.eclipse.e4.ui.workbench.addons.swt
- org.eclipse.e4.ui.workbench.renderers.swt
- org.eclipse.e4.ui.workbench.swt
- org.eclipse.e4.ui.workbench3
- org.eclipse.emf.common
- org.eclipse.emf.databinding
- org.eclipse.emf.ecore
- org.eclipse.emf.ecore.change
- org.eclipse.emf.ecore.xmi
- org.eclipse.equinox.app
- org.eclipse.equinox.bidi
- org.eclipse.equinox.common
- org.eclipse.equinox.concurrent
- org.eclipse.equinox.ds
- org.eclipse.equinox.event
- org.eclipse.equinox.preferences
- org.eclipse.equinox.registry
- org.eclipse.equinox.util
- org.eclipse.help
- org.eclipse.jface
- org.eclipse.jface.databinding
- org.eclipse.osgi
- org.eclipse.osgi.compatibility.state
- org.eclipse.osgi.services
- org.eclipse.osgi.util
- org.eclipse.swt
- org.eclipse.swt.win32.win32.x86_64 --> Choose your operating system
- org.eclipse.ui
- org.eclipse.ui.workbench
- org.w3c.css.sac
- org.w3c.dom.events
- org.w3c.dom.smil
- org.w3c.svg

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
	tweet_creationdate,		String, timestamp of the form "yyyy-dd-MM hh:mm:ss", example: "2013-08-01 01:15:00"
	tweet_content,			String
	relationship,			String (Tweet, Followed)
	latitude,			double
	longitude, 			double
	hasurl, 			boolean
	source, 			String (=user_screenname)
	positive, 			int (result of SentiStrength.jar)
	negative, 			int (result of SentiStrength.jar)
	category, 			String (from AddCategoryScript.java)
	sentiment, 			String (from AddCategoryScript.java)

and yields the following indexed Lucene fields:

- **type**, StringField: what data type are you indexing. Here we use "twitter".
- **id**, StringField: we store the individual tweet\_id
- **relationship**, StringField: what kind of link
- **category**, StringField
- **hasURL**, StringField: boolean
- **name**, StringField: the user\_screenname
- **sentiment**, StringField (pos, neg, neu)
- **neg**, StringField
- **pos**, StringField
- **tags**, TextField: all #tags
- **mention**, TextField: all @mentions
- **content**, TextField: the tweet content
- **geo**, GeoPointField: taken from latitude and longitude

Depending on the data sources you use, you could change and adapt these fields.


## Workflow


## Further Reading

[http://wiki.eclipse.org/Eclipse4/RCP](http://wiki.eclipse.org/Eclipse4/RCP)

[http://www.vogella.com/tutorials/EclipseRCP/article.html](http://www.vogella.com/tutorials/EclipseRCP/article.html)
