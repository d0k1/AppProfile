#include <assert.h>
#include <dlfcn.h>
#include <jvmti.h>
#include <jni.h>
#include <stdint.h>
#include <signal.h>

#ifndef GLOBALS_H
#define GLOBALS_H

void logError(const char *__restrict format, ...);

#define AGENTEXPORT __attribute__((visibility("default"))) JNIEXPORT

// Gets us around -Wunused-parameter
#define IMPLICITLY_USE(x) (void) x;

// Wrap JVMTI functions in this in functions that expect a return
// value and require cleanup.
#define JVMTI_ERROR_CLEANUP_1(error, retval, cleanup)                          \
  {                                                                            \
    int err;                                                                   \
    if ((err = (error)) != JVMTI_ERROR_NONE) {                                 \
      logError("JVMTI error %d\n", err);                                       \
      cleanup;                                                                 \
      return (retval);                                                         \
    }                                                                          \
  }

// Wrap JVMTI functions in this in functions that expect a return value.
#define JVMTI_ERROR_1(error, retval)                                           \
  JVMTI_ERROR_CLEANUP_1(error, retval, /* nothing */)

// Wrap JVMTI functions in this in void functions.
#define JVMTI_ERROR(error) JVMTI_ERROR_CLEANUP(error, /* nothing */)

// Wrap JVMTI functions in this in void functions that require cleanup.
#define JVMTI_ERROR_CLEANUP(error, cleanup)                                    \
  {                                                                            \
    int err;                                                                   \
    if ((err = (error)) != JVMTI_ERROR_NONE) {                                 \
      logError("JVMTI error %d\n", err);                                       \
      cleanup;                                                                 \
      return;                                                                  \
    }                                                                          \
  }

#define DISALLOW_COPY_AND_ASSIGN(TypeName)                                     \
  TypeName(const TypeName &);                                                  \
  void operator=(const TypeName &)

#define DISALLOW_IMPLICIT_CONSTRUCTORS(TypeName)                               \
  TypeName();                                                                  \
  DISALLOW_COPY_AND_ASSIGN(TypeName)

template<class Dest, class Source> inline Dest bit_cast(const Source &source) {
    // Compile time assertion: sizeof(Dest) == sizeof(Source)
    // A compile error here means your Dest and Source have different sizes.
    typedef char VerifySizesAreEqual[sizeof(Dest) == sizeof(Source) ? 1 : -1]
            __attribute__((unused));

    Dest dest;
    memcpy(&dest, &source, sizeof(dest));
    return dest;
}

template<class T>
class JvmtiScopedPtr {
public:
    explicit JvmtiScopedPtr(jvmtiEnv *jvmti) : jvmti_(jvmti), ref_(NULL) {
    }

    JvmtiScopedPtr(jvmtiEnv *jvmti, T *ref) : jvmti_(jvmti), ref_(ref) {
    }

    ~JvmtiScopedPtr() {
        if (NULL != ref_) {
            JVMTI_ERROR(jvmti_->Deallocate((unsigned char *) ref_));
        }
    }

    T **GetRef() {
        assert(ref_ == NULL);
        return &ref_;
    }

    T *Get() {
        return ref_;
    }

    void AbandonBecauseOfError() {
        ref_ = NULL;
    }

private:
    jvmtiEnv *jvmti_;
    T *ref_;

    DISALLOW_IMPLICIT_CONSTRUCTORS(JvmtiScopedPtr);
};

// Accessors for getting the Jvm function for AsyncGetCallTrace.
class Accessors {
public:
    template<class FunctionType>
    static inline FunctionType GetJvmFunction(const char *function_name) {
        // get address of function, return null if not found
        return bit_cast<FunctionType>(dlsym(RTLD_DEFAULT, function_name));
    }
};

#endif // GLOBALS_H