all: jar
.PHONY: lib clean sign

TARGET_JAR=lib/comfre.jar
CLASSPATH=src:../../src:lib/colt.jar:lib/prefuse.jar:lib/je.jar:lib/idx.jar:lib/teccli.jar:lib/jxbrowser-6.19.1.jar:lib/xbrowser-linux64-6.19.1.jar:lib/jxbrowser-win32-6.19.1.jar:lib/jxbrowser-mac-6.19.1.jar:lib/license.jar:lib/commons-io.jar
JAVACFLAGS=-g -d classes -classpath $(CLASSPATH) 
#JAVA_BIN=/usr/lib/jvm/java-8-oracle/bin/
JAVAC=$(JAVA_BIN)javac #/usr/lib/jvm/java-8-oracle/bin/javac
JAR=jar
JARFLAGS= cvmf comfre.mf 
JARREDS=.

## cert signing
TIMESAUTH=http://tsa.starfieldtech.com
#http://timestamp.verisign.com/scripts/timestamp.dll
#TIMESAUTH=http://timestamp.comodoca.com
KEYSTORE=../../security/gok-keystore
JARSIGNER=jarsigner
##  COMODO cert
#JARSIGNER_OPT=-tsa $(TIMESAUTH) -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass`
## Our own cert (CA needs to be imported into javaws)
JARSIGNER_OPT=-tsa $(TIMESAUTH) -keystore $(KEYSTORE) -storepass `cat ../gok.cert.pass` 
CERT_ALIAS=gok

class:
	$(JAVAC) $(JAVACFLAGS) src/org/modnlp/comfre/*.java 
	cp src/org/modnlp/comfre/*.js classes/org/modnlp/comfre/
	cp src/org/modnlp/comfre/*.html classes/org/modnlp/comfre/
	cp src/org/modnlp/comfre/*.csv classes/org/modnlp/comfre/

jar: class
#	$(JAR) $(JARFLAGS) $(TARGET_JAR) -C classes $(JARREDS) 
	java -jar ../autojar.jar -o $(TARGET_JAR) -vc classes:$(CLASSPATH) -b org/modnlp/comfre/* org/apache/commons/io/FileUtils.class

sign: jar
	$(JARSIGNER) $(JARSIGNER_OPT) $(TARGET_JAR) gok
#	jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
##	jarsigner  -tsa http://timestamp.comodoca.com -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass` $(TARGET_JAR) gok 
