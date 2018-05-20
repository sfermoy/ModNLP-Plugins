all: jar
.PHONY: lib clean sign

TARGET_JAR=lib/comfre.jar
CLASSPATH=src:../../src:lib/colt.jar:lib/prefuse.jar:lib/je.jar:lib/idx.jar:lib/teccli.jar
JAVACFLAGS=-g -d classes -classpath $(CLASSPATH) 
JAVAC=javac

class:
	$(JAVAC) $(JAVACFLAGS) src/org/modnlp/comfre/*.java

jar: class
	java -jar ../autojar.jar -o $(TARGET_JAR) -vc classes:lib/prefuse.jar:lib/colt.jar:lib/je.jar -b  org/modnlp/comfre/*.class

sign: jar
#	jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
	jarsigner  -tsa http://timestamp.comodoca.com -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass` $(TARGET_JAR) gok 
