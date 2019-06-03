all: jar
.PHONY: lib clean sign

TARGET_JAR=lib/metafacet.jar
CLASSPATH=src:../../src:lib/colt.jar:lib/prefuse.jar:lib/je.jar:lib/idx.jar:lib/teccli.jar:lib/jxbrowser-6.19.1.jar:lib/xbrowser-linux64-6.19.1.jar:lib/jxbrowser-win32-6.19.1.jar:lib/jxbrowser-mac-6.19.1.jar:lib/license.jar:/usr/lib/jvm/java-8-oracle/jre/lib/plugin.jar
JAVACFLAGS=-g -d classes -classpath $(CLASSPATH) -encoding UTF-8
JAVAC=/usr/lib/jvm/java-8-oracle/bin/javac ## openjdk doesn't seem to include javax
JAR=jar
JARFLAGS= cvmf metafacet.mf 
JARREDS=.

class:
	$(JAVAC) $(JAVACFLAGS) src/org/modnlp/metafacet/*.java 
#	cp src/org/modnlp/metafacet/*.js classes/org/modnlp/metafacet/
	cp src/org/modnlp/metafacet/*.html classes/org/modnlp/metafacet/
#	cp src/org/modnlp/metafacet/*.csv classes/org/modnlp/metafacet/

jar: class
	$(JAR) $(JARFLAGS) $(TARGET_JAR) -C classes $(JARREDS) 
#java -jar ../autojar.jar -o $(TARGET_JAR) -vc classes -b  org/modnlp/metafacet/*.class

sign: jar
#	jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
	jarsigner  -tsa http://timestamp.comodoca.com -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass` $(TARGET_JAR) gok 
