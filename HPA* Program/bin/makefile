SRC=DCDTsrc
TGT=obj
INCLUDES=-IDCDTsrc DCDTWrapper.h HPAProgram.h
FLAGS=-stdlib=libstdc++ -std=c++0x -D GS_USE_TRACE2 -D GS_SYSTEM_RAND_LIBS -lm -fPIC -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/ -v
SOURCES=$(wildcard $(SRC)/*.cpp) DCDTWrapper.cpp HPAProgram.cpp
OBJS=$(addprefix $(TGT)/, $(notdir $(SOURCES:.cpp=.o)))
CC=g++

# Define a variable for classpath
CLASS_PATH = ../bin

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)


$(TGT)/%.o: $(SRC)/%.cpp
	$(CC) $(FLAGS) -c $< -o $@

$(TGT)/%.o: %.cpp
	$(CC) $(FLAGS) -c $< -o $@

# $@ matches the target, $< matches the first dependancy
libhpaprogram.dylib: $(OBJS)
	libtool -lc -lstdc++ -ldl -macosx_version_min 10.9 -lm -dynamic -lSystem $(OBJS) -o libhpaprogram.dylib
	

	
HPAProgram.h : HPAProgram.class
	javah -classpath $(CLASS_PATH) $*
	
clean:
	rm -rf $(TGT)
	mkdir $(TGT)
	if [ -a HPAProgram.h ] ; \
	then \
		rm HPAProgram.h ; \
	fi;
	if [ -a libhpaprogram.o ] ; \
	then \
		rm libhpaprogram.o ; \
	fi;
	if [ -a libhpaprogram.dylib ] ; \
	then \
		rm libhpaprogram.dylib ; \
	fi;
	
all:clean HPAProgram.h libhpaprogram.dylib