all: jar
.PHONY: lib clean sign jar

TARGET_JAR=lib/wordleconc.jar
CLASSPATH=src:../../src:lib/WordCram.jar:lib/core.jar:lib/cue.language.jar:lib/gluegen-rt-natives-linux-i586.jar:lib/gluegen-rt.jar:lib/jogl-all-natives-linux-i586.jar:lib/jogl-all.jar:lib/jsoup-1.3.3.jar:lib/idx.jar:lib/teccli.jar
JAVACFLAGS=-g -d classes -classpath $(CLASSPATH) 
JAVAC=javac

class:
	$(JAVAC) $(JAVACFLAGS) src/org/modnlp/wordleconc/WordleConc.java

jar: class
	java -jar ../autojar.jar -o $(TARGET_JAR) -vc lib/cue.language.jar:lib/gluegen-rt.jar:lib/jogl-all.jar:lib/jsoup-1.3.3.jar:lib/WordCram.jar:lib/core.jar:classes -b  org/modnlp/wordleconc/*.class cue/lang/stop/* processing/opengl/*
#../../lib/idx.jar:../../lib/teccli.jar:

sign: jar
#	jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
	jarsigner  -tsa http://timestamp.comodoca.com -storetype pkcs12 -keystore ../gok-cert.pkcs.p12 -storepass `cat ../gok.cert.pass` $(TARGET_JAR) gok 
