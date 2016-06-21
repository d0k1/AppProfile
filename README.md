# AppProfile
Tools to monitor and profile java applications

* [managed-classdumper](managed-classdumper) is a simple java agent to dump any loaded file, and save stack trace every time new class is being loaded.
* [native/agent](native/agent) is an example of usage java_crw_demo library. It uses low level JVMTI API to instrument bytecode and could write to stdout recorded callstacks.
