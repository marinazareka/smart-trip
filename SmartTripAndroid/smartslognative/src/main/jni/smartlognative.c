#include <jni.h>

JNIEXPORT jstring JNICALL
Java_org_fruct_oss_tsp_smartslognative_NativeTest_helloworld(JNIEnv *env, jclass type) {
    const char *str = "Hello world";
    return (*env)->NewStringUTF(env, str);
}

JNIEXPORT jint JNICALL
Java_org_fruct_oss_tsp_smartslognative_NativeTest_divide(JNIEnv *env, jclass type, jint a, jint b) {
    return a / b;
}