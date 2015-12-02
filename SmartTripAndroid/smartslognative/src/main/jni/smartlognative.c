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

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_initialize(JNIEnv *env, jobject instance,
                                                                      jstring userId_) {
    const char *userId = (*env)->GetStringUTFChars(env, userId_, 0);

    // TODO

    (*env)->ReleaseStringUTFChars(env, userId_, userId);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_shutdown(JNIEnv *env, jobject instance) {

    // TODO

}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_updateUserLocation(JNIEnv *env,
                                                                              jobject instance,
                                                                              jdouble lat,
                                                                              jdouble lon) {

    // TODO

}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_postSearchRequest(JNIEnv *env,
                                                                             jobject instance,
                                                                             jdouble radius,
                                                                             jstring pattern_) {
    const char *pattern = (*env)->GetStringUTFChars(env, pattern_, 0);

    // TODO

    (*env)->ReleaseStringUTFChars(env, pattern_, pattern);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_postScheduleRequest(JNIEnv *env,
                                                                               jobject instance,
                                                                               jobjectArray points) {

    // TODO

}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_setListener(JNIEnv *env,
                                                                       jobject instance,
                                                                       jobject listener) {

    // TODO

}