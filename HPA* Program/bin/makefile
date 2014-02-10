SRC=DCDTsrc
TGT=obj
INCLUDES=-IDCDTsrc DCDTWrapper.h SearchAlgorithms.h
FLAGS=-stdlib=libstdc++ -std=c++0x -D GS_SYSTEM_RAND_LIBS -lm -fPIC -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/ -v
SOURCES=$(wildcard $(SRC)/*.cpp) DCDTWrapper.cpp SearchAlgorithms.cpp
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
libsearchalgorithms.dylib: $(OBJS)
	libtool -lc -lstdc++ -ldl -macosx_version_min 10.9 -lm -dynamic -lSystem $(OBJS) -o libsearchalgorithms.dylib
	

	
SearchAlgorithms.h : SearchAlgorithms.class
	javah -classpath $(CLASS_PATH) $*
	
clean:
	rm -rf $(TGT)
	mkdir $(TGT)
	if [ -a SearchAlgorithms.h ] ; \
	then \
		rm SearchAlgorithms.h ; \
	fi;
	if [ -a libsearchalgorithms.o ] ; \
	then \
		rm libsearchalgorithms.o ; \
	fi;
	if [ -a libsearchalgorithms.dylib ] ; \
	then \
		rm libsearchalgorithms.dylib ; \
	fi;
	
all:clean SearchAlgorithms.h libsearchalgorithms.dylib