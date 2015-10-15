Liquibase QueryDSL Metadata Generator
=====================================

## Introduction

A code generator that supports the creation of QueryDSL metamodel classes
directly from Liquibase changelog files. To do that, the generator uses
an embedded H2 database and an Embedded Equinox OSGi container.

The solution is part of the Everit-Persistence methodology. To get more
information about the concept, check the README file of the
osgi-liquibase-bundle project.

## Generating code

The LQMG solution should be integrated to the OSGi development tools to
make it easier to use. E.g.: There is a [lqmg-maven-plugin][1] that makes it
possible to use the solution in the way, that bundles are configured
automatically based on maven dependencies.

It is also possible to use the solution from the command line. To do that,
download the distribution package that contains all the necessary
dependencies. To get the necessary information how to use it, run the
following command:

<pre><code>java -jar org.everit.persistence.lqmg-1.0.0.jar --help</code></pre>

## Configuration XMLs

It is possible to define special configuration fragments for each bundle
and also a global configuration file. The configuration files must match
the http://everit.org/lqmg XML schema. To get more information about the
XML structure, please see the lqmg-x.x.x.xml file in the META-INF folder
of the jar lqmg jar file.

[1]: https://github.com/everit-org/lqmg-maven-plugin
