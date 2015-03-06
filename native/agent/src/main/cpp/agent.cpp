#include "globals.h"
#include "java_crw_demo.h"

#define MTRACE_class        Mtrace          /* Name of class we are using */
#define MTRACE_entry        method_entry    /* Name of java entry method */
#define MTRACE_exit         method_exit     /* Name of java exit method */
#define MTRACE_native_entry _method_entry   /* Name of java entry native */
#define MTRACE_native_exit  _method_exit    /* Name of java exit native */
#define MTRACE_engaged      engaged         /* Name of java static field */


typedef struct {
    /* JVMTI Environment */
    jvmtiEnv      *jvmti;
    jboolean       vm_is_dead;
    jboolean       vm_is_started;
    /* Data access Lock */
    jrawMonitorID  lock;
} GlobalAgentData;

static GlobalAgentData *gdata;

void stdout_message(const char * format, ...)
{
    va_list ap;

    va_start(ap, format);
    (void)vfprintf(stdout, format, ap);
    va_end(ap);
}

void fatal_error(const char * format, ...)
{
    va_list ap;

    va_start(ap, format);
    (void)vfprintf(stderr, format, ap);
    (void)fflush(stderr);
    va_end(ap);
    exit(3);
}

void check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str)
{
    if ( errnum != JVMTI_ERROR_NONE ) {
        char       *errnum_str;

        errnum_str = NULL;
        (jvmti)->GetErrorName(errnum, &errnum_str);

        fatal_error("ERROR: JVMTI: %d(%s): %s\n", errnum,
                (errnum_str==NULL?"Unknown":errnum_str),
                (str==NULL?"":str));
    }
}

void
deallocate(jvmtiEnv *jvmti, void *ptr)
{
    jvmtiError error;

    error = (jvmti)->Deallocate((unsigned char *)ptr);
    check_jvmti_error(jvmti, error, "Cannot deallocate memory");
}

/* Allocation of JVMTI managed memory */
void *
allocate(jvmtiEnv *jvmti, jint len)
{
    jvmtiError error;
    void      *ptr;

    error = (jvmti)->Allocate(len, (unsigned char **)&ptr);
    check_jvmti_error(jvmti, error, "Cannot allocate memory");
    return ptr;
}

void
add_demo_jar_to_bootclasspath(jvmtiEnv *jvmti, char *demo_name)
{
    jvmtiError error;
    char      *file_sep;
    int        max_len;
    char      *java_home;
    char       jar_path[FILENAME_MAX+1];

    java_home = NULL;
    error = (jvmti)->GetSystemProperty("java.home", &java_home);
    check_jvmti_error(jvmti, error, "Cannot get java.home property value");
    if ( java_home == NULL || java_home[0] == 0 ) {
        fatal_error("ERROR: Java home not found\n");
    }

#ifdef WIN32
    file_sep = "\\";
#else
    file_sep = "/";
#endif

    max_len = (int)(strlen(java_home) + strlen(demo_name)*2 +
                         strlen(file_sep)*5 +
                         16 /* ".." "demo" "jvmti" ".jar" NULL */ );
    if ( max_len > (int)sizeof(jar_path) ) {
        fatal_error("ERROR: Path to jar file too long\n");
    }
    (void)strcpy(jar_path, java_home);
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, "demo");
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, "jvmti");
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, demo_name);
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, demo_name);
    (void)strcat(jar_path, ".jar");
    error = (jvmti)->AddToBootstrapClassLoaderSearch((const char*)jar_path);
    check_jvmti_error(jvmti, error, "Cannot add to boot classpath");

    (void)strcpy(jar_path, java_home);
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, "..");
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, "demo");
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, "jvmti");
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, demo_name);
    (void)strcat(jar_path, file_sep);
    (void)strcat(jar_path, demo_name);
    (void)strcat(jar_path, ".jar");

    error = (jvmti)->AddToBootstrapClassLoaderSearch((const char*)jar_path);
    check_jvmti_error(jvmti, error, "Cannot add to boot classpath");
}
/* Enter a critical section by doing a JVMTI Raw Monitor Enter */
static void enter_critical_section(jvmtiEnv *jvmti)
{
    jvmtiError error;

    error = jvmti->RawMonitorEnter(gdata->lock);
    check_jvmti_error(jvmti, error, "Cannot enter with raw monitor");
}

/* Exit a critical section by doing a JVMTI Raw Monitor Exit */
static void exit_critical_section(jvmtiEnv *jvmti)
{
    jvmtiError error;

    error = jvmti->RawMonitorExit(gdata->lock);
    check_jvmti_error(jvmti, error, "Cannot exit with raw monitor");
}

static void MTRACE_native_entry(JNIEnv *env, jclass klass, jobject thread, jint cnum, jint mnum)
{
}

static void MTRACE_native_exit(JNIEnv *env, jclass klass, jobject thread, jint cnum, jint mnum)
{
}

static void JNICALL cbVMStart(jvmtiEnv *jvmti, JNIEnv *env)
{
}

static void JNICALL cbVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
}

static void JNICALL cbVMDeath(jvmtiEnv *jvmti, JNIEnv *env)
{
}

static void JNICALL cbThreadStart(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
}

static void JNICALL cbThreadEnd(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
}

static void JNICALL cbClassFileLoadHook(jvmtiEnv *jvmti, JNIEnv* env, jclass class_being_redefined, jobject loader, const char* name, jobject protection_domain, jint class_data_len, const unsigned char* class_data, jint* new_class_data_len, unsigned char** new_class_data)
{
}

JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm)
{
}

JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
    static GlobalAgentData data;
    jvmtiEnv              *jvmti;
    jvmtiError             error;
    jint                   res;
    jvmtiCapabilities      capabilities;
    jvmtiEventCallbacks    callbacks;

        /* Setup initial global agent data area
     *   Use of static/extern data should be handled carefully here.
     *   We need to make sure that we are able to cleanup after ourselves
     *     so anything allocated in this library needs to be freed in
     *     the Agent_OnUnload() function.
     */
    (void)memset((void*)&data, 0, sizeof(data));
    gdata = &data;

    /* First thing we need to do is get the jvmtiEnv* or JVMTI environment */
    res = vm->GetEnv((void **)&jvmti, JVMTI_VERSION_1);
    if (res != JNI_OK) {
        /* This means that the VM was unable to obtain this version of the
         *   JVMTI interface, this is a fatal error.
         */
        fatal_error("ERROR: Unable to access JVMTI Version 1 (0x%x),"
                " is your JDK a 5.0 or newer version?"
                " JNIEnv's GetEnv() returned %d\n",
               JVMTI_VERSION_1, res);
    }

    /* Here we save the jvmtiEnv* for Agent_OnUnload(). */
    gdata->jvmti = jvmti;

        /* Immediately after getting the jvmtiEnv* we need to ask for the
     *   capabilities this agent will need. In this case we need to make
     *   sure that we can get all class load hooks.
     */
    (void)memset(&capabilities,0, sizeof(capabilities));
    capabilities.can_generate_all_class_hook_events  = 1;
    error = (jvmti)->AddCapabilities(&capabilities);
    check_jvmti_error(jvmti, error, "Unable to get necessary JVMTI capabilities.");

    /* Next we need to provide the pointers to the callback functions to
     *   to this jvmtiEnv*
     */
    (void)memset(&callbacks,0, sizeof(callbacks));
    /* JVMTI_EVENT_VM_START */
    callbacks.VMStart           = &cbVMStart;
    /* JVMTI_EVENT_VM_INIT */
    callbacks.VMInit            = &cbVMInit;
    /* JVMTI_EVENT_VM_DEATH */
    callbacks.VMDeath           = &cbVMDeath;
    /* JVMTI_EVENT_CLASS_FILE_LOAD_HOOK */
    callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
    /* JVMTI_EVENT_THREAD_START */
    callbacks.ThreadStart       = &cbThreadStart;
    /* JVMTI_EVENT_THREAD_END */
    callbacks.ThreadEnd         = &cbThreadEnd;
    error = (jvmti)->SetEventCallbacks(&callbacks, (jint)sizeof(callbacks));
    check_jvmti_error(jvmti, error, "Cannot set jvmti callbacks");

    /* At first the only initial events we are interested in are VM
     *   initialization, VM death, and Class File Loads.
     *   Once the VM is initialized we will request more events.
     */
    error = (jvmti)->SetEventNotificationMode(JVMTI_ENABLE,
                          JVMTI_EVENT_VM_START, (jthread)NULL);
    check_jvmti_error(jvmti, error, "Cannot set event notification");
    error = (jvmti)->SetEventNotificationMode(JVMTI_ENABLE,
                          JVMTI_EVENT_VM_INIT, (jthread)NULL);
    check_jvmti_error(jvmti, error, "Cannot set event notification");
    error = (jvmti)->SetEventNotificationMode(JVMTI_ENABLE,
                          JVMTI_EVENT_VM_DEATH, (jthread)NULL);
    check_jvmti_error(jvmti, error, "Cannot set event notification");
    error = (jvmti)->SetEventNotificationMode(JVMTI_ENABLE,
                          JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, (jthread)NULL);
    check_jvmti_error(jvmti, error, "Cannot set event notification");

    /* Here we create a raw monitor for our use in this agent to
     *   protect critical sections of code.
     */
    error = (jvmti)->CreateRawMonitor("agent data", &(gdata->lock));
    check_jvmti_error(jvmti, error, "Cannot create raw monitor");

    /* Add demo jar file to boot classpath */
    //add_demo_jar_to_bootclasspath(jvmti, "mtrace");

    
    /* We return JNI_OK to signify success */
    return JNI_OK;
}