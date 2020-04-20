all: jar
.PHONY: lib clean sign

TARGET_JAR=lib/mosaic.jar
CLASSPATH=src:../../src:lib/colt.jar:lib/prefuse.jar:lib/je.jar:lib/idx.jar:lib/teccli.jar
JAVACFLAGS=-g -target 7 -source 7 -d classes -classpath $(CLASSPATH) 
JAVAC=javac

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
	$(JAVAC) $(JAVACFLAGS) src/org/modnlp/mosaic/*.java

jar: class
	java -jar ../autojar.jar -o $(TARGET_JAR) -vc classes:lib/prefuse.jar:lib/colt.jar:lib/je.jar -b  org/modnlp/mosaic/*.class

sign: jar
	$(JARSIGNER) $(JARSIGNER_OPT) $(TARGET_JAR) gok 
##jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
#jarsigner  -tsa http://timestamp.comodoca.com -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass` $(TARGET_JAR) gok 
