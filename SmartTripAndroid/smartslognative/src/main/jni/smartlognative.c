#include <jni.h>

JNIEXPORT jstring JNICALL
Java_org_fruct_oss_tsp_smartslognative_NativeTest_helloworld(JNIEnv *env, jclass type) {
    const char *str = "Hello world";
    return (*env)->NewStringUTF(env, str);
}