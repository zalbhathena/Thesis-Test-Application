/*
 * HPAProgram.c++
 *
 *  Created on: Feb 4, 2014
 *      Author: zalbhathena
 */

//=#include <jni.h>
#include <stdio.h>
#include "HPAProgram.h"
#include "DCDTWrapper.h"


JNIEXPORT jdoubleArray JNICALL Java_HPAProgram_getTriangulation (JNIEnv *env, jobject obj,
		jdoubleArray doubleArray) {

	printf("Hello World!\n");

	int input_size = (int)(env->GetArrayLength(doubleArray));
	printf("1\n");
	jdouble *doubleptr = env->GetDoubleArrayElements(doubleArray, NULL);
	double input[input_size];
	printf("2\n");
	for(int i = 0; i < input_size; i++) {
		input[i] = (double)doubleptr[i];
		printf("%i\n",input[i]);
	}
	printf("3\n");
	GsArray<GsPnt2> edges;
	create_dcdt(input, &edges);
	printf("1\n");

	jclass arraylist_class = (*env).FindClass("java/util/ArrayList");
	jclass point_class = (*env).FindClass("java/awt/Point");

   jmethodID init_point = (*env).GetMethodID(point_class, "<init>", "(II)V");

   int size = edges.size()*2;
   double point_list[size];
   jdoubleArray result = (jdoubleArray)env->NewDoubleArray(size);
   for (int n=0;n<edges.size();n++) {
	   GsPnt2 edge = edges.get(n);
	   double x1 = edge.x;
	   double y1 = edge.y;
   	   //jobject point_obj = (*env).NewObject(point_class, init_point,x,y);
   	   printf("4\n");
   	   point_list[n*2 + 0] = x1;
   	   point_list[n*2 + 1] = y1;
   	   printf("5\n");
   }
   (env)->SetDoubleArrayRegion(result, 0, size, point_list);
   env->ReleaseDoubleArrayElements(doubleArray, doubleptr, NULL);
   return result;
}


