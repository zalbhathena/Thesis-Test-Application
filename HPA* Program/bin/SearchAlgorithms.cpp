/*
 * HPAProgram.c++
 *
 *  Created on: Feb 4, 2014
 *      Author: zalbhathena
 */

//=#include <jni.h>
# define END -12345.6

#include <stdio.h>
#include "SearchAlgorithms.h"
#include "DCDTWrapper.h"


JNIEXPORT jdoubleArray JNICALL Java_SearchAlgorithms_getTriangulation (JNIEnv *env, jobject obj,
		jdoubleArray doubleArray) {
	printf("-1");
	int input_size = (int)(env->GetArrayLength(doubleArray));
	printf("0");
	jdouble *doubleptr = env->GetDoubleArrayElements(doubleArray, NULL);
	double input[input_size];

	for(int i = 0; i < input_size; i++) {
		input[i] = (double)doubleptr[i];
	}
	printf("1\n");
	GsArray<SeDcdtFace*> faces;
	GsArray<GsPnt2> constr_edges;

	create_dcdt(input, faces, constr_edges);

	jclass point_class = (*env).FindClass("java/awt/Point");

   jmethodID init_point = (*env).GetMethodID(point_class, "<init>", "(II)V");

   int size = faces.size()*6 + constr_edges.size()*2 + 1;
   double point_list[size];
   jdoubleArray result = (jdoubleArray)env->NewDoubleArray(size);

   for(int n=0; n<constr_edges.size();n++) {
	   GsPnt2 edge = constr_edges.get(n);
	   double x1 = edge.x;
	   double y1 = edge.y;

	   point_list[n*2 + 0] = x1;
	   point_list[n*2 + 1] = y1;
   }
   point_list[constr_edges.size()*2] = END;
   int base = constr_edges.size()*2 + 1;
   for (int n=0;n<faces.size();n++) {
	   SeDcdtFace* face = faces.get(n);
	   SeBase* first_se =face->se();
	   SeBase* next_se =first_se ->nxt();
	   SeBase* last_se =first_se ->nxn();

	   GsPnt2 p = ((SeDcdtVertex*)first_se->vtx())->p;
	   double x1 = p.x;
	   double y1 = p.y;

	   p = ((SeDcdtVertex*)next_se->vtx())->p;
	   double x2 = p.x;
	   double y2 = p.y;

	   p = ((SeDcdtVertex*)last_se->vtx())->p;
	   double x3 = p.x;
	   double y3 = p.y;



   	   //jobject point_obj = (*env).NewObject(point_class, init_point,x,y);

   	   point_list[base + n*6 + 0] = x1;
   	   point_list[base + n*6 + 1] = y1;
   	   point_list[base + n*6 + 2] = x2;
   	   point_list[base + n*6 + 3] = y2;
   	   point_list[base + n*6 + 4] = x3;
   	   point_list[base + n*6 + 5] = y3;

   }
   (env)->SetDoubleArrayRegion(result, 0, size, point_list);
   env->ReleaseDoubleArrayElements(doubleArray, doubleptr, NULL);
   return result;
}


