LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)//smart $(LOCAL_PATH)/smart/expat

LOCAL_CFLAGS := -DMTENABLE -DHAVE_MEMMOVE -fexceptions

LOCAL_CONLYFLAGS := -std=gnu99
LOCAL_CPPFLAGS := -std=c++11

LOCAL_MODULE = Smart

LOCAL_SRC_FILES := \
    Smart_wrap.cxx \
    smart/scew/writer.c \
    smart/scew/attribute.c \
    smart/scew/reader.c \
    smart/scew/xattribute.c \
    smart/scew/parser.c \
    smart/scew/xerror.c \
    smart/scew/str.c \
    smart/scew/list.c \
    smart/scew/element_attribute.c \
    smart/scew/reader_file.c \
    smart/scew/error.c \
    smart/scew/writer_buffer.c \
    smart/scew/element_search.c \
    smart/scew/tree.c \
    smart/scew/xparser.c \
    smart/scew/element_copy.c \
    smart/scew/printer.c \
    smart/scew/reader_buffer.c \
    smart/scew/writer_file.c \
    smart/scew/element_compare.c \
    smart/scew/element.c \
    smart/smartslog/ss_multisib.c \
    smart/smartslog/subscription_changes.c \
    smart/smartslog/ss_classes.c \
    smart/smartslog/subscription.c \
    smart/smartslog/structures.c \
    smart/smartslog/classes.c \
    smart/smartslog/kpi_interface.c \
    smart/smartslog/ss_func.c \
    smart/smartslog/property_changes.c \
    smart/smartslog/properties.c \
    smart/smartslog/ss_properties.c \
    smart/smartslog/ss_populate.c \
    smart/smartslog/patterns.c \
    smart/smartslog/utils/check_func.c \
    smart/smartslog/utils/kp_error.c \
    smart/smartslog/utils/list.c \
    smart/smartslog/utils/util_func.c \
    smart/ckpi/ckpi.c \
    smart/ckpi/compose_ssap_msg.c \
    smart/ckpi/parse_ssap_msg.c \
    smart/ckpi/process_ssap_cnf.c \
    smart/ckpi/sib_access_tcp.c \
    smart/ckpi/sskp_errno.c \
    smart/expat/xmltok_ns.c \
    smart/expat/xmlrole.c \
    smart/expat/xmlparse.c \
    smart/expat/xmltok.c \
    \
    ontology/ontology.c \
    \
    smart.cpp \
    util.c \


include $(BUILD_SHARED_LIBRARY)
