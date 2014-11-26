JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	FIVRServer/Server.java \
	FIVRClient/Client.java \
	FIVRModules/FIVRBuffer.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class \
	$(RM) FIVRServer/*.class \
	$(RM) FIVRClient/*.class \
	$(RM) FIVRModules/*.class