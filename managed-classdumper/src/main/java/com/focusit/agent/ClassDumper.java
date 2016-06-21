package com.focusit.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;


/**
 * ClassDumper.
 * Useful to find out permgen/metaspace issues.
 * It writes to file every loaded class fqn and Also log contains thread name and stack where class was loaded.
 * Besides it dumps content of class being loaded at the moment.
 */
public class ClassDumper implements ClassFileTransformer {

    private BufferedOutputStream logStream = new BufferedOutputStream(new FileOutputStream("class-loading-stat.log"));
    private PrintWriter logger = new PrintWriter(logStream);

    public ClassDumper() throws FileNotFoundException {
    }

    /**
     * add agent
     */
    public static void premain(final String agentArgument, final Instrumentation instrumentation) throws FileNotFoundException {
        instrumentation.addTransformer(new ClassDumper());
    }

    /**
     * instrument class
     */
    public byte[] transform(final ClassLoader loader, final String className, final Class clazz,
                            final java.security.ProtectionDomain domain, final byte[] bytes) {

        String loaderName = loader == null ? "system" : loader.toString();
        String clsName = className.replace("/", ".");
        logger.println("Load Class: " + clsName + " Thread: " + Thread.currentThread().getName() + " classloader: " + loaderName);
        getCurrentThreadStackTrace(logger);
        logger.println();
        logger.flush();

        String dumpBaseDir = "dumped.classes" + File.separator + loaderName;
        File dir = new File(dumpBaseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File cls = new File(dumpBaseDir + File.separator + clsName);
        if (cls.exists()) {
            cls.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(cls)) {
            fos.write(bytes);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void getCurrentThreadStackTrace(PrintWriter s) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        for (StackTraceElement traceElement : trace) {
            s.println("\tat " + traceElement);
        }
    }
}
