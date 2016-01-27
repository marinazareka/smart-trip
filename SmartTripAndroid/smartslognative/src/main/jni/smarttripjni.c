#include <stdlib.h>

#include <jni.h>
#include <android/log.h>

#include "smarttrip.h"
#include "common/st_point.h"
#include "st_movement.h"


static JavaVM* jvm;

static jclass class_point;
static jclass class_listener;
static jclass class_ioexception;
static jclass class_movement;
static jclass class_string;

static jmethodID constructor_movement;
static jmethodID constructor_point;
static jmethodID method_get_point_id;
static jmethodID method_get_point_title;
static jmethodID method_get_point_lat;
static jmethodID method_get_point_lon;

static jmethodID method_listener_on_search_request_ready;
static jmethodID method_listener_on_schedule_request_ready;
static jmethodID method_listener_on_search_history_ready;

static jmethodID method_listener_on_request_failed;

static jobject global_listener;

static JNIEnv* get_jni_env(int* status) {
    JNIEnv* env = NULL;

    jint res_env = (*jvm)->GetEnv(jvm, (void **) &env, JNI_VERSION_1_2);
    if (res_env == JNI_OK) {
        if (status) *status = 1;
        return env;
    }

    jint res = (*jvm)->AttachCurrentThread(jvm, &env, NULL);

    if (res != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Can't attach thread");
        if (status) *status = 2;
        return NULL;
    } else {
        if (status) *status = 0;
        return env;
    }
}

static void release_jni_env(int status) {
    if (status == 0) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm = vm;

    JNIEnv* env = get_jni_env(NULL);

    class_ioexception = (*env)->FindClass(env, "java/io/IOException");
    class_ioexception = (*env)->NewGlobalRef(env, class_ioexception);

    class_point = (*env)->FindClass(env, "org/fruct/oss/tsp/commondatatype/Point");
    class_point = (*env)->NewGlobalRef(env, class_point);

    class_movement = (*env)->FindClass(env, "org/fruct/oss/tsp/commondatatype/Movement");
    class_movement = (*env)->NewWeakGlobalRef(env, class_movement);

    class_listener = (*env)->FindClass(env, "org/fruct/oss/tsp/commondatatype/SmartSpaceNative$Listener");
    class_listener = (*env)->NewGlobalRef(env, class_listener);

    class_string = (*env)->FindClass(env, "java/lang/String");
    class_string = (*env)->NewGlobalRef(env, class_string);

    constructor_movement = (*env)->GetMethodID(env, class_movement, "<init>",
                                               "(Lorg/fruct/oss/tsp/commondatatype/Point;Lorg/fruct/oss/tsp/commondatatype/Point;)V");

    constructor_point = (*env)->GetMethodID(env, class_point, "<init>",
                                            "(Ljava/lang/String;Ljava/lang/String;DD)V");

    method_get_point_title = (*env)->GetMethodID(env, class_point, "getTitle", "()Ljava/lang/String;");
    method_get_point_id = (*env)->GetMethodID(env, class_point, "getId", "()Ljava/lang/String;");
    method_get_point_lat = (*env)->GetMethodID(env, class_point, "getLat", "()D");
    method_get_point_lon = (*env)->GetMethodID(env, class_point, "getLon", "()D");

    method_listener_on_search_request_ready
            = (*env)->GetMethodID(env, class_listener, "onSearchRequestReady",
                                  "([Lorg/fruct/oss/tsp/commondatatype/Point;)V");
    method_listener_on_schedule_request_ready
            = (*env)->GetMethodID(env, class_listener, "onScheduleRequestReady",
                                  "([Lorg/fruct/oss/tsp/commondatatype/Movement;)V");
    method_listener_on_search_history_ready
            = (*env)->GetMethodID(env, class_listener, "onSearchHistoryReady",
                                  "([Ljava/lang/String;)V");

    method_listener_on_request_failed
            = (*env)->GetMethodID(env, class_listener, "onRequestFailed",
                                  "(Ljava/lang/String;)V");

    return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv* env = get_jni_env(NULL);

    (*env)->DeleteGlobalRef(env, class_string);
    (*env)->DeleteGlobalRef(env, class_movement);
    (*env)->DeleteGlobalRef(env, class_ioexception);
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
                                                                      jstring userId_,
                                                                      jstring kpName_,
                                                                      jstring smartSpaceName_,
                                                                      jstring address_, jint port) {
    const char *userId = (*env)->GetStringUTFChars(env, userId_, 0);
    const char *kpName = (*env)->GetStringUTFChars(env, kpName_, 0);
    const char *smartSpaceName = (*env)->GetStringUTFChars(env, smartSpaceName_, 0);
    const char *address = (*env)->GetStringUTFChars(env, address_, 0);

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "initialize called %s %s %s %s %d",
                        userId, kpName, smartSpaceName, address, port);

    if (!st_initialize(userId, kpName, smartSpaceName, address, port)) {
        (*env)->ThrowNew(env, class_ioexception, "Can't initialize smartspace");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "successfully initialized");
    }

    // Release functions are safe to call with pending exception
    (*env)->ReleaseStringUTFChars(env, userId_, userId);
    (*env)->ReleaseStringUTFChars(env, kpName_, kpName);
    (*env)->ReleaseStringUTFChars(env, smartSpaceName_, smartSpaceName);
    (*env)->ReleaseStringUTFChars(env, address_, address);
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

    if (!st_update_user_location(lat, lon)) {
        (*env)->ThrowNew(env, class_ioexception, "Can't update user location");
    }
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_postSearchRequest(JNIEnv *env,
                                                                             jobject instance,
                                                                             jdouble radius,
                                                                             jstring pattern_) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "postSearchRequest called");

    const char *pattern = (*env)->GetStringUTFChars(env, pattern_, 0);

    if (!st_post_search_request(radius, pattern)) {
        (*env)->ThrowNew(env, class_ioexception, "Can't post search request");
    }

    (*env)->ReleaseStringUTFChars(env, pattern_, pattern);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_postScheduleRequest(JNIEnv *env,
                                                                               jobject instance,
                                                                               jobjectArray points,
                                                                               jstring tsp_type_) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "postScheduleRequest called");

    const char *tsp_type = (*env)->GetStringUTFChars(env, tsp_type_, 0);

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

    if (!st_post_schedule_request(array, array_size, tsp_type)) {
        (*env)->ThrowNew(env, class_ioexception, "Can't post schedule request");
    }

    (*env)->ReleaseStringUTFChars(env, tsp_type_, tsp_type);
}

JNIEXPORT void JNICALL
Java_org_fruct_oss_tsp_smartslognative_JniSmartSpaceNative_setListener(JNIEnv *env,
                                                                       jobject instance,
                                                                       jobject listener) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "setListener called");
    global_listener = (*env)->NewGlobalRef(env, listener);
}


static jobject create_point_object(JNIEnv* env, struct Point* point) {
    jstring id_str = (*env)->NewStringUTF(env, point->id);
    jstring title_str = (*env)->NewStringUTF(env, point->title);

    return (*env)->NewObject(env, class_point, constructor_point,
                             id_str, title_str, point->lat, point->lon);
}

// Callbacks
void st_on_search_request_ready(struct Point *points, int points_count) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "st_on_search_request_ready");

    int status;
    JNIEnv* env = get_jni_env(&status);

    jobjectArray array = (*env)->NewObjectArray(env, points_count, class_point, NULL);

    for (int i = 0; i < points_count; i++) {
        jobject point_object = create_point_object(env, &points[i]);

        (*env)->SetObjectArrayElement(env, array, i, point_object);
    }

    (*env)->CallVoidMethod(env, global_listener, method_listener_on_search_request_ready, array);

    release_jni_env(status);

}

static jobject create_movement_object(JNIEnv* env, struct Movement* movement) {
    jobject point_a = create_point_object(env, &movement->point_a);
    jobject point_b = create_point_object(env, &movement->point_b);

    return (*env)->NewObject(env, class_movement, constructor_movement, point_a, point_b);
}

void st_on_schedule_request_ready(struct Movement* movements, int movements_count) {
    int status;
    JNIEnv* env = get_jni_env(&status);

    jobjectArray movement_array = (*env)->NewObjectArray(env, movements_count, class_movement, NULL);

    for (int i = 0; i < movements_count; i++) {
        jobject movement = create_movement_object(env, &movements[i]);
        (*env)->SetObjectArrayElement(env, movement_array, i, movement);
    }

    (*env)->CallVoidMethod(env, global_listener, method_listener_on_schedule_request_ready, movement_array);

    release_jni_env(status);
}

void st_on_search_history_ready(const char* items[], int items_count) {
    int status;
    JNIEnv* env = get_jni_env(&status);

    jobjectArray pattern_array = (*env)->NewObjectArray(env, items_count, class_string, NULL);

    for (int i = 0; i < items_count; i++) {
        jstring pattern = (*env)->NewStringUTF(env, items[i]);
        (*env)->SetObjectArrayElement(env, pattern_array, i, pattern);
    }

    (*env)->CallVoidMethod(env, global_listener, method_listener_on_search_history_ready, pattern_array);

    release_jni_env(status);
}

void st_on_request_failed(const char* description) {
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "st_on_request_failed called");

    int status;
    JNIEnv* env = get_jni_env(&status);
    jstring description_jni = (*env)->NewStringUTF(env, description);
    (*env)->CallVoidMethod(env, global_listener, method_listener_on_request_failed, description_jni);
    release_jni_env(status);
}
