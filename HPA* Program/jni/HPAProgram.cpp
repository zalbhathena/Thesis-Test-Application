/*
 * HPAProgram.c++
 *
 *  Created on: Feb 4, 2014
 *      Author: zalbhathena
 */

//#include <jni.h>
#include <stdio.h>
#include "HPAProgram.h"
#include "DCDTWrapper.h"


JNIEXPORT void JNICALL Java_HPAProgram_sayHello (JNIEnv *env, jobject obj) {
   printf("Hello World!\n");
   create_dcdt();
}

