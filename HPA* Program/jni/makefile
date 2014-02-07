SRC=DCDTsrc
TGT=obj
INCLUDES=-IDCDTsrc DCDTWrapper.h HPAProgram.h
FLAGS=-stdlib=libstdc++ -lm -fPIC -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/ -v
SOURCES=$(wildcard $(SRC)/*.cpp) DCDTWrapper.cpp HPAProgram.cpp
OBJS=$(addprefix $(TGT)/, $(notdir $(SOURCES:.cpp=.o)))
CC=g++

# Define a variable for classpath
CLASS_PATH = ../bin

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)

all: libhpaprogram.dylib

$(TGT)/%.o: $(SRC)/%.cpp
	$(CC) $(FLAGS) -c $< -o $@

$(TGT)/%.o: %.cpp
	$(CC) $(FLAGS) -c $< -o $@

# $@ matches the target, $< matches the first dependancy
libhpaprogram.dylib: $(OBJS)
	libtool -arch x86_64 -lc -lstdc++ -ldl -macosx_version_min 10.9 -lm -dynamic -lSystem $(OBJS) -o libhpaprogram.dylib
	

	
HPAProgram.h : HPAProgram.class
	javah -classpath $(CLASS_PATH) $*
	
clean:
	rm -rf $(TGT)
	mkdir $(TGT)
	rm HPAProgram.h libhpaprogram.o libhpaprogram.dylib