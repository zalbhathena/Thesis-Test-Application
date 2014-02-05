# Define a variable for classpath
CLASS_PATH = ../bin

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)

all: libhpaprogram.dylib

# $@ matches the target, $< matches the first dependancy
libhpaprogram.dylib:
	cc -v -c -fPIC -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/ HPAProgram.c++ -o libhpaprogram.o
	libtool -dynamic -lSystem libhpaprogram.o -o libhpaprogram.dylib
	
HPAProgram.h : HPAProgram.class
	javah -classpath $(CLASS_PATH) $*
	
clean:
	rm HPAProgram.h libhpaprogram.o libhpaprogram.dylib