#include "agent.h"

#include "../../java_crw_demo/java_crw_demo.h"
#include "agentruntime.h"
#include "javaclassesinfo.h"
#include "javathreadsinfo.h"
#include "javathreadinfo.h"
#include "abstracttracingprofiler.h"
#include "simplecallcounterprofiler.h"
#include "threadcallstackprofiler.h"
#include <iostream>

#define Agent_class        Agent           /* Name of class we are using */
#define Agent_method_entry        agent_entry    /* Name of java entry method */
#define Agent_method_exit         agent_exit     /* Name of java exit method */
#define Agent_native_method_entry native_entry   /* Name of java entry native */
#define Agent_native_method_exit  native_exit    /* Name of java exit native */

#define Agent_native_method_reset  native_reset    /* Name of java exit native */
#define Agent_native_method_pause  native_pause    /* Name of java exit native */
#define Agent_native_method_resume native_resume    /* Name of java exit native */
#define Agent_native_method_csv  native_csv    /* Name of java exit native */

#define Agent_VM_started      ready         /* Name of java static field */

/* C macros to create strings from tokens */
#define _STRING(s) #s
#define STRING(s) _STRING(s)

#include <atomic>

/* ------------------------------------------------------------------- */

using namespace std;

static AgentRuntime *runtime;
static JavaClassesInfo *classes = new JavaClassesInfo();
static JavaThreadsInfo *threads = new JavaThreadsInfo();

static atomic<bool> paused(false);

//static SimpleCallCounterProfiler *tracingProfiler = new SimpleCallCounterProfiler();
static ThreadCallStackProfiler *tracingProfiler = new ThreadCallStackProfiler();

/* Callback from java_crw_demo() that gives us mnum mappings */
static void mnum_callbacks ( unsigned cnum, const char **names, const char**sigs, int mcount ) {

    for ( int mnum = 0 ; mnum < mcount ; mnum++ ) {
        JavaMethodInfo *method = classes->addClassMethod ( cnum, names[mnum], sigs[mnum] );
	cout << "instrumented: "<< cnum <<":"<<mnum<<"="<<method->getClass()->getName() << "#" << method->getName()<<"#"<<method->getSignature() << endl;
	tracingProfiler->methodInstrumented(method);
    }
}

//https://bugs.openjdk.java.net/browse/JDK-7013347
JNIEXPORT void JNICALL JavaCritical_Agent_native_1entry( jint cnum, jint mnum ) {
  if(paused.load()) {
    return;
  }
  
  tracingProfiler->methodEntry ( cnum, mnum, nullptr );
}

//https://bugs.openjdk.java.net/browse/JDK-7013347
JNIEXPORT void JNICALL JavaCritical_Agent_native_1exit ( jint cnum, jint mnum ) {
  if(paused.load()) {
    return;
  }
  
  tracingProfiler->methodExit ( cnum, mnum, nullptr );
}

JNIEXPORT void JNICALL Java_Agent_native_1entry( JNIEnv *env, jclass klass, jint cnum, jint mnum ) {
  if(paused.load()) {
    return;
  }
  
  tracingProfiler->methodEntry ( cnum, mnum, nullptr );
}

JNIEXPORT void JNICALL Java_Agent_native_1exit( JNIEnv *env, jclass klass, jint cnum, jint mnum ) {
  if(paused.load()) {
    return;
  }
  
  tracingProfiler->methodExit ( cnum, mnum, nullptr );
}

JNIEXPORT void JNICALL Java_Agent_native_1reset(JNIEnv *, jclass){
  tracingProfiler->reset();
}

JNIEXPORT void JNICALL Java_Agent_native_1resume(JNIEnv *, jclass){
  paused.store(false);
}

JNIEXPORT void JNICALL Java_Agent_native_1pause(JNIEnv *, jclass){
  paused.store(true);
}

JNIEXPORT jstring JNICALL Java_Agent_native_1csv(JNIEnv *, jclass){
  return nullptr;
}

/* Callback for JVMTI_EVENT_VM_START */
static void JNICALL cbVMStart ( jvmtiEnv *jvmti, JNIEnv *env ) {
    runtime->agentGlobalLock();
    {
        jclass   klass;
        jfieldID field;
        int      rc;

        /* Java Native Methods for class */

        static JNINativeMethod registry[8] = {

	    {
                STRING ( Agent_native_method_entry ), "(II)V",
                ( void* ) &JavaCritical_Agent_native_1entry
            },
            {
                STRING ( Agent_native_method_entry ), "(II)V",
                ( void* ) &Java_Agent_native_1entry
            },
            {
                STRING ( Agent_native_method_exit ),  "(II)V",
                ( void* ) &JavaCritical_Agent_native_1exit
            },	    
            {
                STRING ( Agent_native_method_exit ),  "(II)V",
                ( void* ) &Java_Agent_native_1exit
            },
	    {
	      STRING ( Agent_native_method_reset ),  "()V",
	      ( void* ) &Java_Agent_native_1reset
	    },
	    {
	      STRING ( Agent_native_method_resume ),  "()V",
	      ( void* ) &Java_Agent_native_1resume
	    },
	    {
	      STRING ( Agent_native_method_pause ),  "()V",
	      ( void* ) &Java_Agent_native_1pause
	    },
	    {
	      "native_csv",  "()Ljava/lang/String;",
	      ( jstring* ) &Java_Agent_native_1csv
	    }
	};

        /* The VM has started. */
        cout<< "VMStart" <<endl;

        /* Register Natives for class whose methods we use */
        klass = ( env )->FindClass ( STRING ( Agent_class ) );
        if ( klass == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot find %s with FindClass\n",
                          STRING ( Agent_class ) );
        }

        rc = ( env )->RegisterNatives ( klass, registry, 8 );
        if ( rc != 0 ) {
            fatal_error ( "ERROR: JNI: Cannot register native methods for %s\n",
                          STRING ( Agent_class ) );
        }

        /* Engage calls. */
        field = ( env )->GetStaticFieldID ( klass, STRING ( Agent_VM_started ), "I" );
        if ( field == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot get field from %s\n",
                          STRING ( Agent_class ) );
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

        /* Disengage calls in Agent_class. */
        klass = ( env )->FindClass ( STRING ( Agent_class ) );
        if ( klass == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot find %s with FindClass\n",
                          STRING ( Agent_class ) );
        }
        field = ( env )->GetStaticFieldID ( klass, STRING ( Agent_VM_started ), "I" );
        if ( field == NULL ) {
            fatal_error ( "ERROR: JNI: Cannot get field from %s\n",
                          STRING ( Agent_class ) );
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
            if ( !runtime->getOptions()->isClassExcluded(classname) && strcmp ( classname, STRING ( Agent_class ) ) != 0 ) {
		
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
                                STRING ( Agent_class ), "L" STRING ( Agent_class ) ";",
                                STRING ( Agent_method_entry ), "(II)V",
                                STRING ( Agent_method_exit ), "(II)V",
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

    cout << "Adding helper jar at " << runtime->getOptions()->getHelperJar().c_str() << endl;
    runtime->JVMTIAddJarToClasspath ( runtime->getOptions()->getHelperJar().c_str() );

    return JNI_OK;
}

JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* vm, char *options, void *reserved){
  return Agent_OnLoad(vm, options, reserved);
}

JNIEXPORT void JNICALL Agent_OnUnload ( JavaVM *vm ) {
}
