#include <jni.h>
#include <android/log.h>

#include "smarttrip.h"
#include "st_point.h"

#define APPNAME "TSP-Native"

static jclass class_point;

static jmethodID method_get_point_id;
static jmethodID method_get_point_title;
static jmethodID method_get_point_lat;
static jmethodID method_get_point_lon;

static void initialize_jni(JNIEnv *env) {
    class_point = (*env)->FindClass(env, "org/fruct/oss/tsp/commondatatype/Point");
    class_point = (*env)->NewGlobalRef(env, class_point);

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Get getTitle");
    method_get_point_title = (*env)->GetMethodID(env, class_point, "getTitle", "()Ljava/lang/String;");
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Get getId");
    method_get_point_id = (*env)->GetMethodID(env, class_point, "getId", "()Ljava/lang/String;");
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Get getLat");
    method_get_point_lat = (*env)->GetMethodID(env, class_point, "getLat", "()D");
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "Get getLon");
    method_get_point_lon = (*env)->GetMethodID(env, class_point, "getLon", "()D");
}

static void shutdown_jni(JNIEnv* env) {
    (*env)->DeleteGlobalRef(env, class_point);
}

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
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "initialize called");

    const char *userId = (*env)->GetStringUTFChars(env, userId_, 0);

    st_initialize(userId);
    initialize_jni(env);

    (*env)->ReleaseStringUTFChars(env, userId_, userId);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_shutdown(JNIEnv *env, jobject instance) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "shutdown called");
    st_shutdown();
    shutdown_jni(env);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_updateUserLocation(JNIEnv *env,
                                                                              jobject instance,
                                                                              jdouble lat,
                                                                              jdouble lon) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "updateUserLocation called");

    st_update_user_location(lat, lon);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_postSearchRequest(JNIEnv *env,
                                                                             jobject instance,
                                                                             jdouble radius,
                                                                             jstring pattern_) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "postSearchRequest called");

    const char *pattern = (*env)->GetStringUTFChars(env, pattern_, 0);

    st_post_search_request(radius, pattern);

    (*env)->ReleaseStringUTFChars(env, pattern_, pattern);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_postScheduleRequest(JNIEnv *env,
                                                                               jobject instance,
                                                                               jobjectArray points) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "postScheduleRequest called");

    jsize array_size = (*env)->GetArrayLength(env, points);
    struct Point array[array_size];

    for (int i = 0; i < array_size; i++) {
        jobject point = (*env)->GetObjectArrayElement(env, points, i);

        jstring j_id = (*env)->CallObjectMethod(env, point, method_get_point_id);
        const char* id = (*env)->GetStringUTFChars(env, j_id, 0);

        jstring j_title = (*env)->CallObjectMethod(env, point, method_get_point_title);
        const char* title = (*env)->GetStringUTFChars(env, j_title, 0);

        double lat = (*env)->CallDoubleMethod(env, point, method_get_point_lat);
        double lon = (*env)->CallDoubleMethod(env, point, method_get_point_lon);

        st_init_point(&array[i], id, title, lat, lon);

        (*env)->ReleaseStringUTFChars(env, j_id, id);
        (*env)->ReleaseStringUTFChars(env, j_title, title);
    }

    st_post_schedule_request(array, array_size);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_setListener(JNIEnv *env,
                                                                       jobject instance,
                                                                       jobject listener) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "setListener called");
    // TODO

}
