[![Licence](https://img.shields.io/github/license/CPGFinanceSystems/ebics-java.svg)](https://github.com/CPGFinanceSystems/ebics-java/blob/master/LICENSE.txt)
[![Java Development Kit 1.8](https://img.shields.io/badge/JDK-1.8-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.cpg.oss/ebics-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.cpg.oss/ebics-java)
[![Build Status](https://api.travis-ci.org/CPGFinanceSystems/ebics-java.svg?branch=master)](https://travis-ci.org/CPGFinanceSystems/ebics-java)
[![Coverage Status](https://coveralls.io/repos/CPGFinanceSystems/ebics-java/badge.svg)](https://coveralls.io/r/CPGFinanceSystems/ebics-java)

Java client for ebics protocol
==============================

The code was synced (with svn2git) and mavenfied

Original Project
----------------

http://sourceforge.net/projects/ebics/

Maven
-----

You need to add the following mirror in your ~/.m2/settings.xml otherwise stax could not be resolved.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
    <mirrors>
        <mirror>
            <id>simexplorer</id>
            <name>SimExplorer repository</name>
            <url>http://maven.simexplorer.org/repo/</url>
            <mirrorOf>central, SE-springsource-release, SE-springsource-external, SE-IN2P3, SEIS-codelutin, SE-nuiton, SE-restlet</mirrorOf>
        </mirror>
    </mirrors>
</settings>
```

Documentation
-------------

```
mvn javadoc:javadoc
```

HTML doc is generated to `target/site/apidocs/`.

Tested bank services
---------------------

<table>
<tr><th>BANK</th><th>COUNTRY</th><th>STATUS</th></tr>
<tr><td>Credit Agricole</td><td>France</td><td>OK</td></tr>
<tr><td>Société Générale</td><td>France</td><td>OK</td></tr>
<tr><td>Le crédit Lyonais</td><td>France</td><td>?</td></tr>
<tr><td>La banque postale</td><td>France</td><td>pending</td></tr>
</table>

Related Links
-------------

* [EBICS official website](http://www.ebics.org/)
* [EBICS Qualification](http://www.qualif-ebics.fr/)
* [LinuxFR news](http://linuxfr.org/news/enfin-un-client-ebics-java-libre)
