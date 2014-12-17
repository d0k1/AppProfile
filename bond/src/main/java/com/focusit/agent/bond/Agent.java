package com.focusit.agent.bond;

import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDumpManager;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

/**
 * Agent main class. Loading desired class transformer
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class Agent
{
    private static Instrumentation agentInstrumentation = null;
    private static String excludes[] = AgentConfiguration.getExcludeClasses();
    private static String ignoreExcludes[] = AgentConfiguration.getIgnoreExcludeClasses();

    public static void agentmain(String agentArguments, Instrumentation instrumentation) throws IOException,
            UnmodifiableClassException
    {
        try
        {
            agentInstrumentation = instrumentation;

            modifyBootstrapClasspathByArgs(agentArguments);

            if (!AgentConfiguration.isAgentEnabled())
            {
                System.out.println("Agent is disabled");
                return;
            }

            System.out.println("Loading bond agent");

            startSensors();

            startDumping();

            AgentConfiguration.Transformer transformer = AgentConfiguration.getAgentClassTransformer();

            // strange bu usage SWITCH causes IllegalAccessException but IF is OK
            if (transformer == AgentConfiguration.Transformer.asm)
            {
                agentInstrumentation.addTransformer(new AsmClassTransformer(excludes, ignoreExcludes, instrumentation),
                        true);
            }
            else if (transformer == AgentConfiguration.Transformer.javaassist)
            {
                agentInstrumentation.addTransformer(new JavaAssistClassTransformer(excludes, ignoreExcludes,
                        instrumentation), true);
            }
            else if (transformer == AgentConfiguration.Transformer.cglib)
            {
                agentInstrumentation.addTransformer(
                        new CGLibClassTransformer(excludes, ignoreExcludes, instrumentation), true);
            }

            retransformAlreadyLoadedClasses(instrumentation);

        }
        catch (Throwable e)
        {
            System.err.println("Error loading agent " + e.getMessage());
            throw e;
        }
    }

    public static void premain(String agentArguments, Instrumentation instrumentation) throws IOException,
            UnmodifiableClassException
    {
        try
        {
            agentmain(agentArguments, instrumentation);
        }
        catch (Throwable e)
        {
            System.err.println("Agent loading error " + e.getMessage());
            throw e;
        }
    }

    private static boolean isClassExcluded(String className)
    {

        if (excludes != null)
        {
            boolean skip = false;

            for (String exclude : excludes)
            {
                if (className.startsWith(exclude))
                {
                    skip = true;
                    break;
                }
            }

            for (String ignoreExclude : ignoreExcludes)
            {
                if (className.startsWith(ignoreExclude))
                {
                    skip = false;
                    break;
                }
            }

            if (skip)
            {
                return true;
            }
        }

        return false;
    }

    private static void modifyBootstrapClasspathByArgs(String agentArguments) throws IOException
    {
        if (System.getProperty("agent.jar") != null)
        {
            agentInstrumentation.appendToBootstrapClassLoaderSearch(new JarFile(System.getProperty("agent.jar")));
            agentInstrumentation.appendToSystemClassLoaderSearch(new JarFile(System.getProperty("agent.jar")));
        }
    }

    private static void retransformAlreadyLoadedClasses(Instrumentation instrumentation)
    {
        try{
            for (Class cls : instrumentation.getAllLoadedClasses())
            {

                if (isClassExcluded(cls.getName()))
                {
                    continue;
                }

                if (instrumentation.isModifiableClass(cls))
                {
                    try
                    {
                        instrumentation.retransformClasses(cls);
                    }
                    catch (UnmodifiableClassException e)
                    {
                        System.err.println("unmodifiable class " + cls.getName());
                    }
                }
            }
        }catch(Throwable e){
            System.err.println("Error retransforming class: " + e);
            throw e;
        }
    }

    private static void startDumping() throws IOException
    {
        GlobalTime gt = new GlobalTime(AgentConfiguration.getTimerPrecision());
        gt.start();

        final SamplesDumpManager dataDumper = new SamplesDumpManager();
        dataDumper.start();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
            try
            {
                dataDumper.exit();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            try
            {
                dataDumper.dumpRest();
            }
            catch (Throwable e)
            {
                System.err.println("Shutdown hook error: " + e.getMessage());
                throw e;
            }
            }
        });
    }

    private static void startSensors()
    {
        JvmMonitoring.getInstance().start();
    }
}
