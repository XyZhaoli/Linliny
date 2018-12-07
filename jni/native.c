#include <jni.h>
#include <pthread.h>
#include <android/log.h>

#include "common.h"

#define YCF_UART_PORT		"/dev/ttyUSB0"
#define YCF_UART_PORT1		"/dev/ttyS0"

static volatile int fd = -1;
static volatile int is_thread_running = 0;
pthread_t thread_id;
JavaVM * gJavaVM;
jobject gJavaObj;


static void data_callback(JNIEnv* env, jmethodID javaCallback, uint8_t *buf, int len) {
	int i;
	jbyteArray array = (*env)->NewByteArray(env, len);

	if (array == NULL) {
		LOGE("data_callback: NewCharArray error.");
		return;
	}

	(*env)->SetByteArrayRegion(env, array, 0, len, buf);
	(*env)->CallVoidMethod(env, gJavaObj, javaCallback, array, len);
	(*env)->DeleteLocalRef(env, array);
}

static void *thread_uart(void* arg) {
	JNIEnv *env;
	unsigned char tmp_buf[32];
	int len = 0;

	(*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL);

	jclass javaClass = (*env)->GetObjectClass(env, gJavaObj);
	if (javaClass == NULL) {
		LOGE("Fail to find javaClass");
		return 0;
	}

	jmethodID javaCallback = (*env)->GetMethodID(env, javaClass,
			"onNativeCallback", "([B)V");
	if (javaCallback == NULL) {
		LOGE("Fail to find method onNativeCallback");
		return 0;
	}

	while (is_thread_running) {
		if ((len = UartWork(fd, tmp_buf)) > 0) {
			//(*env)->CallVoidMethod(env, gJavaObj, javaCallback, tmp_buf);
			data_callback(env, javaCallback, tmp_buf, len);
		}
		usleep(10 * 1000);
	}

	(*gJavaVM)->DetachCurrentThread(gJavaVM);
	pthread_exit(0);
}

JNIEXPORT jint JNICALL Java_uartJni_Uartjni_UartWriteCmd(JNIEnv *env, jobject thiz, jbyteArray arr, jint len) {
	int tempLen = -1;
	//unsigned char data[32] = {0};
	unsigned char *buf = (*env)->GetByteArrayElements(env, arr, false);
	int i = 0;

	LOGD(" Uart write cmd. len = %d", len);
	tempLen = write(fd, buf, len);
	//LOGD(" Uart write: 0x%02x", buf[0]);
	for (i = 0; i < len; i++) {
		LOGD("cmd[%d] =0x%02x, ", i, buf[i]);
	}

	(*env)->ReleaseByteArrayElements(env, arr, buf, 0);
	return tempLen;
}

JNIEXPORT void JNICALL Java_uartJni_Uartjni_nativeInitilize(JNIEnv *env, jobject thiz)
{
	(*env)->GetJavaVM(env, &gJavaVM);
	gJavaObj = (*env)->NewGlobalRef(env, thiz);
}

JNIEXPORT jint JNICALL Java_uartJni_Uartjni_BoardThreadStart(JNIEnv *env, jobject thiz) {
	fd = open(YCF_UART_PORT1, O_RDWR | O_NOCTTY | O_NDELAY);
	if (fd == -1) {
		LOGE("open %s failed.\n", YCF_UART_PORT1);
		fd = open(YCF_UART_PORT, O_RDWR | O_NOCTTY | O_NDELAY);
		if (fd == -1) {
			LOGE("open %s failed.\n", YCF_UART_PORT);
			return -1;
		}
	}
	LOGE("open ----机器---- success");
	uartSetSpeed(fd, 9600);
	if (!setUartConfig(fd, 8, 1, 'N')) {
		LOGE("master uart config error 1 !");
		return -1;
	}

	is_thread_running = 1;
	if (pthread_create(&thread_id, NULL, thread_uart, NULL) != 0) {
		LOGE("Create master thread fail!");
		return -1;
	}
	return 0;
}


JNIEXPORT void JNICALL Java_uartJni_Uartjni_NativeThreadStop(JNIEnv *env, jobject thiz)
{
	int kill_rc;

	is_thread_running = 0;
	kill_rc = pthread_join(thread_id, NULL);
	LOGE("机器串口关闭");
	close(fd);
}

