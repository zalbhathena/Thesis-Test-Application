/*
 * HPAProgram.c++
 *
 *  Created on: Feb 4, 2014
 *      Author: zalbhathena
 */

#include <jni.h>
#include <stdio.h>
#include "HPAProgram.h"

JNIEXPORT void JNICALL Java_HelloJNI_sayHello(JNIEnv *env, jobject thisObj) {
   printf("Hello World!\n");
   return;
}

int main() {

}