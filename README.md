db-liquibase-querydsl-generator
===============================

A generator that supports creating QueryDSL metamodel from Liquibase changelog files via an embedded H2 database from the command line

## Introduction

The aim of the db-lqmg project is to speed up development and supports creating QueryDSL metamodel from LiguiBase changelog files. A generator that supports creating QueryDSL metamodel from Liquibase changelog files via an embedded H2 database from the command line.

A db-lqmg is as simple as possible, howeve has some special dependency.  The special dependencies:
* com.h2database:h2:1.3.174
* org.liquibase:liquibase-core:3.0.7
* com.mysema.querydsl:querydsl-sql-codegen:3.2.4
and also their dependecies.

Not to worry, you do not have to collect dependencies, beacuse the project collect them and copying the ${project.basedir}/target/dist folder when compline the project.

## How do I use?

Simplest way to use is copying the org.everit.db.lqmg-1.0.0.jar file and jar files in the dist folder the same place (for example C:\lqmg). Run the command promt and navigating the folder where copied the jar files. Running the following command

<pre><code>java -jar org.everit.db.lqmg-1.0.0.jar --help</code></pre>

This command print help note. Wrote the example usage and describe the available arguments.

Available arguments:
* changeLogFile: path to the liquibase changelog file.
* targetFolder: the folder where source will be generated to
* packageName: the java package of the generated QueryDSL metamodel classes.
* schemaPattern: a schema name pattern; must match the schema name as it is stored in the database.
* schemaToPackage: the schema to package or not.
* help: wrote the help note.

Reguired arguments the changeLogFile and targetFolder. The other argument is optional.

## Examples

This section find some example how to using the generator. The first example in the above example which print help note. 

The commands are presented in the following directory structure and command run the C:\lqmg folder:
<pre><code>
C:\lqmg
  - targetFolder (should not exist folder, beacuse the generator creates folder(s))
  - changeLogFolder
    - changeLogFile.xml
  - ... all jar file from the project/target/dist folder and the org.everit.db.lqmg-1.0.0.jar file.
</code></pre>

The changeLogFile contents:
<pre><code>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;

&lt;databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd
        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd"
    objectQuotingStrategy="QUOTE_ALL_OBJECTS" logicalFilePath="org.everit.person"&gt;

    &lt;changeSet id="1.schemaCreation" author="balazs-zsoldos"&gt;
        &lt;sql dbms="h2"&gt;CREATE SCHEMA IF NOT EXISTS "org.everit.person"&lt;/sql&gt;
		    &lt;sql dbms="h2"&gt;CREATE SCHEMA IF NOT EXISTS "org.everit.example"&lt;/sql&gt;
    &lt;/changeSet&gt;

    &lt;changeSet id="1" author="nvoxland"&gt;
        &lt;createTable tableName="person" schemaName="org.everit.person"&gt;
            &lt;column name="id" type="int" autoIncrement="true"&gt;
                &lt;constraints primaryKey="true" nullable="false" /&gt;
            &lt;/column&gt;
            &lt;column name="firstname" type="varchar(50)" /&gt;
            &lt;column name="lastname" type="varchar(50)"&gt;
                &lt;constraints nullable="false" /&gt;
            &lt;/column&gt;
            &lt;column name="state" type="char(2)" /&gt;
        &lt;/createTable&gt;
    &lt;/changeSet&gt;

    &lt;changeSet id="2" author="nvoxland"&gt;
        &lt;addColumn tableName="person" schemaName="org.everit.person"&gt;
            &lt;column name="username" type="varchar(8)" /&gt;
        &lt;/addColumn&gt;
    &lt;/changeSet&gt;
	
	&lt;changeSet id="3" author="nvoxland"&gt;
        &lt;createTable tableName="example" schemaName="org.everit.example"&gt;
            &lt;column name="id" type="int" autoIncrement="true"&gt;
                &lt;constraints primaryKey="true" nullable="false" /&gt;
            &lt;/column&gt;
            &lt;column name="example" type="varchar(50)" /&gt;
        &lt;/createTable&gt;
    &lt;/changeSet&gt;
&lt;/databaseChangeLog&gt;
</code></pre>

### Print help note

<pre><code>java -jar org.everit.db.lqmg-1.0.0.jar --help</code></pre>

equal:

<pre><code>java -jar C:\lqmg\org.everit.db.lqmg-1.0.0.jar --help</code></pre>

This command print help note.

### Required arguments

<pre><code>java -jar org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=targetFolder</code></pre>

equal

<pre><code>java -jar C:\lqmg\org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=C:\lqmg\targetFolder</code></pre>

Using realtive or absolute path all working.

Command result is creating folder(s) and the QueryDSL metamodel(s).
<pre><code>C:\lqmg\targetFolder
  - org
    - everit
	  - person
	    - QPerson.java
	  - example
	    - QExample.java
  - public_
    - QDatabasechangelog.java
	- QDatabasechangeloglock.java
</code></pre>
This case generate all models regardless of scheme name. 

### Required arguments and package argument

<pre><code>java -jar org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=targetFolder --packageName=com.everit.example</code></pre>

equal

<pre><code>java -jar C:\lqmg\org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=C:\lqmg\targetFolder --packageName=com.everit.example</code></pre>

Command result is creating folder(s) and the QueryDSL metamodel(s).
<pre><code>C:\lqmg\targetFolder
  - com
    - everit
      - example
	    - org
          - everit
	        - person
	          - QPerson.java
	        - example
	          - QExample.java
        - public_
          - QDatabasechangelog.java
	      - QDatabasechangeloglock.java
		  </code></pre>
This command generate all models regardless of schema name, but we add package name to the result. For this reason the result target folder is targetFolder/{packgeName}/schemaName.

### Required arguments and schemaPattern argument

<pre><code>java -jar org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=targetFolder --schemaPattern=org.everit.person</code></pre>

equal

<pre><code>java -jar C:\lqmg\org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=C:\lqmg\targetFolder --schemaPattern=org.everit.person</code></pre>

Command result is creating folder(s) and the QueryDSL metamodel(s).
<pre><code>C:\lqmg\targetFolder
  - com
    - everit
      - example
	    - org
          - everit
	        - person
	          - QPerson.java
</code></pre>
This case only generate models which belongs to the specified scheme.

### Required arguments and schemaToPackage argument

<pre><code>java -jar org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=targetFolder --schemaToPackage=false</code></pre>

equal

<pre><code>java -jar C:\lqmg\org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=C:\lqmg\targetFolder --schemaToPackage=false</code></pre>

Command result is creating folder(s) and the QueryDSL metamodel(s).
<pre><code>
C:\lqmg\targetFolder
  - QPerson.java
  - QExample.java
  - QDatabasechangelog.java
  - QDatabasechangeloglock.java
</code></pre>
  
This case generate all models regardless of schema name and ignored the scheme name when copied the JAVA classes the target folder.

### All arguments

<pre><code>java -jar org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=targetFolder --packageName=com.everit.example --schemaPattern=org.everit.person --schemaToPackage=false</code></pre>

equal

<pre><code>java -jar C:\lqmg\org.everit.db.lqmg-1.0.0-SNAPSHOT.jar --changeLogFile=changeLogFolder/changeLogFile.xml --targetFolder=C:\lqmg\targetFolder --packageName=com.everit.example --schemaPattern=org.everit.person --schemaToPackage=false</code></pre>

Command result is creating folder(s) and the QueryDSL metamodel(s).
<pre><code>C:\lqmg\targetFolder
  - com
    - everit
      - example
        - QPerson.java
		</code></pre>
This case nly generate models which belongs to the specified scheme and the models targetFolder is started targetFolder/com.everit.example and removing the "default" sheme name folders the target folder.
