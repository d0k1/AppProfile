#include "agent.h"

#include "../../java_crw_demo/java_crw_demo.h"
#include "agentruntime.h"
#include "javaclassesinfo.h"
#include "javathreadsinfo.h"
#include "javathreadinfo.h"
#include "simplecallcounterprofiler.h"
#include <iostream>

/* ------------------------------------------------------------------- */
/* Some constant maximum sizes */

#define MAX_TOKEN_LENGTH        16
#define MAX_THREAD_NAME_LENGTH  512
#define MAX_METHOD_NAME_LENGTH  1024

#define MTRACE_class        Mtrace          /* Name of class we are using */
#define MTRACE_entry        method_entry    /* Name of java entry method */
#define MTRACE_exit         method_exit     /* Name of java exit method */
#define MTRACE_native_entry _method_entry   /* Name of java entry native */
#define MTRACE_native_exit  _method_exit    /* Name of java exit native */
#define MTRACE_engaged      engaged         /* Name of java static field */

/* C macros to create strings from tokens */
#define _STRING(s) #s
#define STRING(s) _STRING(s)

/* ------------------------------------------------------------------- */

using namespace std;

static AgentRuntime *runtime;
static JavaClassesInfo *classes = new JavaClassesInfo();
static JavaThreadsInfo *threads = new JavaThreadsInfo();
static SimpleCallCounterProfiler *tracingProfiler = new SimpleCallCounterProfiler();

/* Callback from java_crw_demo() that gives us mnum mappings */
static void mnum_callbacks ( unsigned cnum, const char **names, const char**sigs, int mcount ) {

    for ( int mnum = 0 ; mnum < mcount ; mnum++ ) {
        JavaMethodInfo *method = classes->addClassMethod ( cnum, names[mnum], sigs[mnum] );
	tracingProfiler->methodInstrumented(method);
    }
}

//https://bugs.openjdk.java.net/browse/JDK-7013347
JNIEXPORT void JNICALL JavaCritical_Mtrace__1method_1entry ( jint cnum, jint mnum ) {
  tracingProfiler->methodEntry ( cnum, mnum, nullptr );
}

//https://bugs.openjdk.java.net/browse/JDK-7013347
JNIEXPORT void JNICALL JavaCritical_Mtrace__1method_1exit ( jint cnum, jint mnum ) {
  tracingProfiler->methodExit ( cnum, mnum, nullptr );
}

JNIEXPORT void JNICALL Java_Mtrace__1method_1entry ( JNIEnv *env, jclass klass, jobject thread, jint cnum, jint mnum ) {
  tracingProfiler->methodEntry ( cnum, mnum, nullptr );
}
JNIEXPORT void JNICALL Java_Mtrace__1method_1exit ( JNIEnv *env, jclass klass, jobject thread, jint cnum, jint mnum ) {
  tracingProfiler->methodExit ( cnum, mnum, nullptr );
}

/* Callback for JVMTI_EVENT_VM_START */
static void JNICALL cbVMStart ( jvmtiEnv *jvmti, JNIEnv *env ) {
    runtime->agentGlobalLock();
    {
        jclass   klass;
        jfieldID field;
        int      rc;

        /* Java Native Methods for class */

        static JNINativeMethod registry[4] = {

	    {
                STRING ( MTRACE_native_entry ), "(Ljava/lang/Object;II)V",
                ( void* ) &JavaCritical_Mtrace__1method_1entry
            },
            {
                STRING ( MTRACE_native_entry ), "(Ljava/lang/Object;II)V",
                ( void* ) &Java_Mtrace__1method_1entry
            },
            {
                STRING ( MTRACE_native_exit ),  "(Ljava/lang/Object;II)V",
                ( void* ) &JavaCritical_Mtrace__1method_1exit
            },
	    
            {
                STRING ( MTRACE_native_exit ),  "(Ljava/lang/Object;II)V",
                ( void* ) &Java_Mtrace__1method_1exit
            }
        };

        /* The VM has started. */
        cout<< "VMStart" <<endl;

        /* Register Natives for class whose methods we use */
        klass = ( env )->FindClass ( STRING ( MTRACE_class ) );
        if ( klass == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot find %s with FindClass\n",
                          STRING ( MTRACE_class ) );
        }

        rc = ( env )->RegisterNatives ( klass, registry, 4 );
        if ( rc != 0 ) {
            fatal_error ( "ERROR: JNI: Cannot register native methods for %s\n",
                          STRING ( MTRACE_class ) );
        }

        /* Engage calls. */
        field = ( env )->GetStaticFieldID ( klass, STRING ( MTRACE_engaged ), "I" );
        if ( field == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot get field from %s\n",
                          STRING ( MTRACE_class ) );
        }
        ( env )->SetStaticIntField ( klass, field, 1 );

        /* Indicate VM has started */
        runtime->VmStarted();

    }
    runtime->agentGlobalUnlock ();
}

/* Callback for JVMTI_EVENT_VM_INIT */
static void JNICALL cbVMInit ( jvmtiEnv *jvmti, JNIEnv *env, jthread thread ) {
    runtime->agentGlobalLock();
    {
        static jvmtiEvent events[] = { JVMTI_EVENT_THREAD_START, JVMTI_EVENT_THREAD_END };

        /* The VM has started. */
        JavaThreadInfo info = runtime->getThreadInfo ( thread );
        cout << "VMInit " << info.getName() << endl;

        /* The VM is now initialized, at this time we make our requests
         *   for additional events.
         */

        for ( int i=0; i < ( int ) ( sizeof ( events ) /sizeof ( jvmtiEvent ) ); i++ ) {
            jvmtiError error;

            /* Setup event  notification modes */
            error = ( jvmti )->SetEventNotificationMode ( JVMTI_ENABLE,
                    events[i], ( jthread ) NULL );
            runtime->JVMTIExitIfError ( error, "Cannot set event notification" );
        }

    }
    runtime->agentGlobalUnlock();
}

/* Callback for JVMTI_EVENT_VM_DEATH */
static void JNICALL cbVMDeath ( jvmtiEnv *jvmti, JNIEnv *env ) {
    runtime->agentGlobalLock();
    {
        jclass   klass;
        jfieldID field;

        /* The VM has died. */
        cout<< "VMDeath" << endl;

        /* Disengage calls in MTRACE_class. */
        klass = ( env )->FindClass ( STRING ( MTRACE_class ) );
        if ( klass == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot find %s with FindClass\n",
                          STRING ( MTRACE_class ) );
        }
        field = ( env )->GetStaticFieldID ( klass, STRING ( MTRACE_engaged ), "I" );
        if ( field == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot get field from %s\n",
                          STRING ( MTRACE_class ) );
        }
        ( env )->SetStaticIntField ( klass, field, 0 );


        /* The critical section here is important to hold back the VM death
         *    until all other callbacks have completed.
         */

        /* Since this critical section could be holding up other threads
         *   in other event callbacks, we need to indicate that the VM is
         *   dead so that the other callbacks can short circuit their work.
         *   We don't expect any further events after VmDeath but we do need
         *   to be careful that existing threads might be in our own agent
         *   callback code.
         */
        runtime->VmDead();

        /* Dump out stats */
        tracingProfiler->printOnExit();
    }
    runtime->agentGlobalUnlock();

}

/* Callback for JVMTI_EVENT_THREAD_START */
static void JNICALL cbThreadStart ( jvmtiEnv *jvmti, JNIEnv *env, jthread thread ) {
    runtime->agentGlobalLock ();
    {
        /* It's possible we get here right after VmDeath event, be careful */
        if ( !runtime->isVmDead() ) {

            JavaThreadInfo info = runtime->getThreadInfo ( thread );
            threads->addThread ( info );
	    tracingProfiler->threadStarted(thread);
            cout << "ThreadStart " << info.getName() << endl;
        }
    }
    runtime->agentGlobalUnlock();
}

/* Callback for JVMTI_EVENT_THREAD_END */
static void JNICALL cbThreadEnd ( jvmtiEnv *jvmti, JNIEnv *env, jthread thread ) {
    runtime->agentGlobalLock();
    {
        /* It's possible we get here right after VmDeath event, be careful */
        if ( !runtime->isVmDead() ) {
            JavaThreadInfo info = runtime->getThreadInfo ( thread );
            threads->setThreadDead ( info );
	    tracingProfiler->threadStopped(thread);
	    
            cout << "ThreadEnd " << info.getName() << endl;
        }
    }
    runtime->agentGlobalUnlock();
}

/* Callback for JVMTI_EVENT_CLASS_FILE_LOAD_HOOK */
static void JNICALL cbClassFileLoadHook ( jvmtiEnv *jvmti, JNIEnv* env, jclass class_being_redefined, jobject loader, const char* name, jobject protection_domain, jint class_data_len, const unsigned char* class_data, jint* new_class_data_len, unsigned char** new_class_data ) {
    runtime->agentGlobalLock();
    {
        // It's possible we get here right after VmDeath event, be careful
        if ( !runtime->isVmDead() ) {

            const char *classname;

            // Name could be NULL
            if ( name == NULL ) {
                classname = java_crw_demo_classname ( class_data, class_data_len,
                                                      NULL );
                if ( classname == NULL ) {
                    fatal_error ( "ERROR: No classname inside classfile\n" );
                }
            } else {
                classname = strdup ( name );
                if ( classname == NULL ) {
                    fatal_error ( "ERROR: Out of malloc memory\n" );
                }
            }

            *new_class_data_len = 0;
            *new_class_data     = NULL;

            // The tracker class itself? 
            if ( !runtime->getOptions()->isClassExcluded(classname) && strcmp ( classname, STRING ( MTRACE_class ) ) != 0 ) {
		
		//cout << classname << " instrumenting " << endl;
		
                jint           cnum;
                int            system_class;
                unsigned char *new_image;
                long           new_length;

                cnum = classes->addClass ( classname );

                //  Is it a system class? If the class load is before VmStart then we will consider it a system class that should be treated carefully. (See java_crw_demo)
                system_class = 0;
                if ( !runtime->isVmStarted() ) {
                    system_class = 1;
                }

                new_image = NULL;
                new_length = 0;

                java_crw_demo ( cnum,
                                classname,
                                class_data,
                                class_data_len,
                                system_class,
                                STRING ( MTRACE_class ), "L" STRING ( MTRACE_class ) ";",
                                STRING ( MTRACE_entry ), "(II)V",
                                STRING ( MTRACE_exit ), "(II)V",
                                NULL, NULL,
                                NULL, NULL,
                                &new_image,
                                &new_length,
                                NULL,
                                &mnum_callbacks );

                // If we got back a new class image, return it back as "the" new class image. This must be JVMTI Allocate space.                
                if ( new_length > 0 ) {
                    unsigned char *jvmti_space;

                    jvmti_space = ( unsigned char * ) runtime->JVMTIAllocate ( ( jint ) new_length );
                    ( void ) memcpy ( ( void* ) jvmti_space, ( void* ) new_image, ( int ) new_length );
                    *new_class_data_len = ( jint ) new_length;
                    *new_class_data = jvmti_space; 
                }

                if ( new_image != NULL ) {
                    ( void ) free ( ( void* ) new_image ); 
                }
            }
            ( void ) free ( ( void* ) classname );
        }
    }
    runtime->agentGlobalUnlock();
}

JNIEXPORT jint JNICALL Agent_OnLoad ( JavaVM *vm, char *options, void *reserved ) {
    jvmtiEnv              *jvmti;
    jvmtiError             error;
    jint                   res;
    jvmtiCapabilities      capabilities;
    jvmtiEventCallbacks    callbacks;

    res = ( vm )->GetEnv ( ( void ** ) &jvmti, JVMTI_VERSION_1 );
    if ( res != JNI_OK ) {
        fatal_error ( "ERROR: Unable to access JVMTI Version 1 (0x%x),"
                      " is your JDK a 5.0 or newer version?"
                      " JNIEnv's GetEnv() returned %d\n",
                      JVMTI_VERSION_1, res );
    }

    runtime = new AgentRuntime ( jvmti );
    
    tracingProfiler->setData ( runtime, classes, threads );

    ( void ) memset ( &capabilities,0, sizeof ( capabilities ) );
    capabilities.can_generate_all_class_hook_events  = 1;
    error = ( jvmti )->AddCapabilities ( &capabilities );
    runtime->JVMTIExitIfError ( error, "Unable to get necessary JVMTI capabilities." );

    ( void ) memset ( &callbacks,0, sizeof ( callbacks ) );
    callbacks.VMStart           = &cbVMStart;
    callbacks.VMInit            = &cbVMInit;
    callbacks.VMDeath           = &cbVMDeath;
    callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
    callbacks.ThreadStart       = &cbThreadStart;
    callbacks.ThreadEnd         = &cbThreadEnd;
    error = ( jvmti )->SetEventCallbacks ( &callbacks, ( jint ) sizeof ( callbacks ) );
    runtime->JVMTIExitIfError ( error, "Cannot set jvmti callbacks" );

    error = ( jvmti )->SetEventNotificationMode ( JVMTI_ENABLE, JVMTI_EVENT_VM_START, ( jthread ) NULL );
    runtime->JVMTIExitIfError ( error, "Cannot set event notification" );
    error = ( jvmti )->SetEventNotificationMode ( JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, ( jthread ) NULL );
    runtime->JVMTIExitIfError ( error, "Cannot set event notification" );
    error = ( jvmti )->SetEventNotificationMode ( JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, ( jthread ) NULL );
    runtime->JVMTIExitIfError ( error, "Cannot set event notification" );
    error = ( jvmti )->SetEventNotificationMode ( JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, ( jthread ) NULL );
    runtime->JVMTIExitIfError ( error, "Cannot set event notification" );

    runtime->JVMTIAddJarToClasspath ( "mtrace.jar" );

    return JNI_OK;
}

JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* vm, char *options, void *reserved){
  return Agent_OnLoad(vm, options, reserved);
}

JNIEXPORT void JNICALL Agent_OnUnload ( JavaVM *vm ) {
}
