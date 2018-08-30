all: jar
.PHONY: lib clean sign

TARGET_JAR=lib/comfre.jar
CLASSPATH=src:../../src:lib/colt.jar:lib/prefuse.jar:lib/je.jar:lib/idx.jar:lib/teccli.jar:lib/jxbrowser-6.19.1.jar:lib/xbrowser-linux64-6.19.1.jar:lib/jxbrowser-win32-6.19.1.jar:lib/jxbrowser-mac-6.19.1.jar:lib/license.jar
JAVACFLAGS=-g -d classes -classpath $(CLASSPATH) 
JAVAC=javac
JAR=jar
JARFLAGS= cvmf comfre.mf 
JARREDS=.

class:
	$(JAVAC) $(JAVACFLAGS) src/org/modnlp/comfre/*.java 
	cp src/org/modnlp/comfre/*.js classes/org/modnlp/comfre/
	cp src/org/modnlp/comfre/*.html classes/org/modnlp/comfre/
	cp src/org/modnlp/comfre/*.csv classes/org/modnlp/comfre/

jar: class
	$(JAR) $(JARFLAGS) $(TARGET_JAR) -C classes $(JARREDS) 
#java -jar ../autojar.jar -o $(TARGET_JAR) -vc classes -b  org/modnlp/comfre/*.class

sign: jar
#	jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
	jarsigner  -tsa http://timestamp.comodoca.com -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass` $(TARGET_JAR) gok 
