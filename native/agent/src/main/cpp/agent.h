#ifndef GLOBALS_H
#define GLOBALS_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <stdarg.h>

#include <sys/types.h>

#include <jni.h>
#include <jvmti.h>

/* Utility functions */

#ifdef __cplusplus
extern "C" {
#endif
/*
void  fatal_error(const char * format, ...);
char *get_token(char *str, char *seps, char *buf, int max);

void  check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str);
void  deallocate(jvmtiEnv *jvmti, void *ptr);
void *allocate(jvmtiEnv *jvmti, jint len);
*/
JNIEXPORT void JNICALL JavaCritical_Agent__1method_1entry(jint, jint);
JNIEXPORT void JNICALL JavaCritical_Agent__1method_1exit(jint, jint);

JNIEXPORT void JNICALL Java_Agent__1method_1entry(JNIEnv *, jclass, jobject, jint, jint);
JNIEXPORT void JNICALL Java_Agent__1method_1exit(JNIEnv *, jclass, jobject, jint, jint);

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved);
JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm);
JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* vm, char *options, void *reserved);
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */

#endif