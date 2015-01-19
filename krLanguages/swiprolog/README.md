SWI Prolog
==========

KR Implementation for GOAL, based on SWI Prolog.

Issues
======

There is a minor issue with this language regarding the handling of 
long (64 bit) integers. The JPL layer that is between our system and SWI Prolog
does not convert these properly. Therefore, all transportation between
SWI and GOAL will convert integers that do not fit into 32 bits to floating
point format. 64 bit integers can be used but only inside SWI Prolog.


Dependency information 
=====================

```
<repository>
 <id>goalhub-mvn-repo</id>
 <url>https://raw.github.com/goalhub/mvn-repo/master</url>
</repository>
```
	
```	
<dependency>
 <groupId>com.github.goalhub.krTools.krLanguages</groupId>
 <artifactId>swiprolog</artifactId>
 <version>1.1.2-SNAPSHOT</version>
</dependency>
```

Release Procedure
=============

Ensure your ~/.m2/settings.xml file is as follows:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
   			<id>github</id>
   			<username>YOUR_USERNAME</username>
   			<password>YOUR_PASSWORD</password>
		</server>
	</servers>
</settings>
```

Then call:

```
mvn versions:use-latest-versions -DallowSnapshots=true -DexcludeReactor=false && mvn deploy -DcreateChecksum=true
```

Note that you must have a public name and e-mail address set on GitHub for this to work correctly (https://github.com/settings/profile)