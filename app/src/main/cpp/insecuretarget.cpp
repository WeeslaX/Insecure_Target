#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_sg_insecure_insecuretarget_database_EncryptedDataProvider_getSecret(JNIEnv *env, jobject thiz) {
    std::string message = "Passw0rd123";
    return env->NewStringUTF(message.c_str());
}