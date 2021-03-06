SWI Prolog Enabler
==================

Enabler code that installs SWI Prolog libraries (binary c++ code) so SWI Prolog can be used from a Java program. Uses the JPL interface that has been developed for this purpose for SWI Prolog.


Dependency information 
======================

```
<repository>
 <id>goalhub-mvn-repo</id>
 <url>https://raw.github.com/goalhub/mvn-repo/master</url>
</repository>
```
	
```	
<dependency>
 <groupId>com.github.goalhub.krTools</groupId>
 <artifactId>swiPrologEnabler</artifactId>
 <version>1.1.1</version>
</dependency>
```	

Release Procedure
=================

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
mvn versions:use-latest-versions -DallowSnapshots=true -DexcludeReactor=false
mvn deploy -DcreateChecksum=true
```

Note that you must have a public name and e-mail address set on GitHub for this to work correctly (https://github.com/settings/profile).
