LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := linliny_uartmaster
LOCAL_SRC_FILES := native.c common.c

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := linliny_uartcardmaster
LOCAL_SRC_FILES := nativeUart1.c common.c

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
