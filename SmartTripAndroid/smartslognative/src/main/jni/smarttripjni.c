#include <stdlib.h>

#include <jni.h>
#include <android/log.h>

#include "smarttrip.h"
#include "st_point.h"

#define APPNAME "TSP-Native"

static JavaVM* jvm;

static jclass class_point;
static jclass class_listener;

static jmethodID constructor_point;
static jmethodID method_get_point_id;
static jmethodID method_get_point_title;
static jmethodID method_get_point_lat;
static jmethodID method_get_point_lon;

static jmethodID method_listener_on_search_request_ready;

static jobject global_listener;

static JNIEnv* get_jni_env() {
    JNIEnv* env = NULL;

    jint res_env = (*jvm)->GetEnv(jvm, (void **) &env, JNI_VERSION_1_2);
    if (res_env == JNI_OK) {
        return env;
    }

    jint res = (*jvm)->AttachCurrentThread(jvm, &env, NULL);

    if (res != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't attach thread");
        return NULL;
    } else {
        return env;
    }
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm = vm;

    JNIEnv* env = get_jni_env();

    class_point = (*env)->FindClass(env, "org/fruct/oss/tsp/commondatatype/Point");
    class_point = (*env)->NewGlobalRef(env, class_point);

    class_listener = (*env)->FindClass(env, "org/fruct/oss/tsp/smartslognative/SmartSpaceNative$Listener");
    class_listener = (*env)->NewGlobalRef(env, class_listener);

    constructor_point = (*env)->GetMethodID(env, class_point, "<init>",
                                            "(Ljava/lang/String;Ljava/lang/String;DD)V");

    method_get_point_title = (*env)->GetMethodID(env, class_point, "getTitle", "()Ljava/lang/String;");
    method_get_point_id = (*env)->GetMethodID(env, class_point, "getId", "()Ljava/lang/String;");
    method_get_point_lat = (*env)->GetMethodID(env, class_point, "getLat", "()D");
    method_get_point_lon = (*env)->GetMethodID(env, class_point, "getLon", "()D");

    method_listener_on_search_request_ready
            = (*env)->GetMethodID(env, class_listener, "onSearchRequestReady",
                                  "([Lorg/fruct/oss/tsp/commondatatype/Point;)V");
    return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv* env = get_jni_env();

    (*env)->DeleteGlobalRef(env, class_point);
    (*env)->DeleteGlobalRef(env, class_listener);
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

    (*env)->ReleaseStringUTFChars(env, userId_, userId);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_shutdown(JNIEnv *env, jobject instance) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "shutdown called");
    st_shutdown();
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
    global_listener = (*env)->NewGlobalRef(env, listener);
}

// Callbacks
void st_on_search_request_ready(struct Point *points, int points_count) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "st_on_search_request_ready");

    JNIEnv* env = get_jni_env();

    jobjectArray array = (*env)->NewObjectArray(env, points_count, class_point, NULL);

    for (int i = 0; i < points_count; i++) {
        jstring id_str = (*env)->NewStringUTF(env, points[i].id);
        jstring title_str = (*env)->NewStringUTF(env, points[i].title);

        jobject point_object = (*env)->NewObject(env, class_point, constructor_point,
                                                 id_str, title_str, points[i].lat, points[i].lon);

        (*env)->SetObjectArrayElement(env, array, i, point_object);
    }

    (*env)->CallVoidMethod(env, global_listener, method_listener_on_search_request_ready, array);

    // TODO: should be check is current thread attached
    (*jvm)->DetachCurrentThread(jvm);
}

void st_on_schedule_request_ready(struct Movement *movements, int movements_count) {
    JNIEnv* env;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);


}