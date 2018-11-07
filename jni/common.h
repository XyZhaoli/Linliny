#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <errno.h>
#include <android/log.h>
#include<pthread.h>

#include <string.h>

#ifndef TRUE
	#define TRUE		1
	#define true		1
#endif

#ifndef FALSE
	#define FALSE		0
	#define false		0
#endif

#if 01
	#define D(fmt,...)  LOGD(fmt,## __VA_ARGS__)
#else
	#define  D(...)   ((void)0)
#endif

#define LOGD(fmt,arg...)   __android_log_print(ANDROID_LOG_INFO, "串口", fmt, ##arg)
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "串口", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "串口", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "串口", __VA_ARGS__))


int uartSetSpeed(int fd, int baudrate);
int setUartConfig(int fd, int databits, int stopbits, int parity);
int UartWorkCard(int fd, unsigned char* buf);
