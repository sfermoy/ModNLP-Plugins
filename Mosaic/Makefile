all: sign
.PHONY: lib clean sign

TARGET_JAR=lib/mosaic.jar

jar:
	java -jar ../autojar.jar -o $(TARGET_JAR) -vc classes:lib/prefuse.jar:lib/colt.jar:lib/je.jar -b  org/modnlp/mosaic/*.class

sign: jar
	jarsigner -keystore .keystore -storepass `cat tecplugin.cer.pass` $(TARGET_JAR) tecplugin
