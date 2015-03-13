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

JNIEXPORT void JNICALL JavaCritical_Agent_native_1entry(jint, jint);
JNIEXPORT void JNICALL JavaCritical_Agent_native_1exit(jint, jint);

JNIEXPORT void JNICALL Java_Agent_native_1entry(JNIEnv *, jclass, jint, jint);
JNIEXPORT void JNICALL Java_Agent_native_1exit(JNIEnv *, jclass, jint, jint);

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved);
JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm);
JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* vm, char *options, void *reserved);

#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */

#endif