package com.focusit.agent.bond;

import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDumpManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Agent main class. Loading desired class transformer
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class Agent
{
    private static Instrumentation agentInstrumentation = null;
    private static final Logger LOG = Logger.getLogger(Agent.class.getName());
    private static String excludes[] = AgentConfiguration.getExcludeClasses();
    private static String ignoreExcludes[] = AgentConfiguration.getIgnoreExcludeClasses();

    public static void agentmain(String agentArguments, Instrumentation instrumentation) throws IOException,
            UnmodifiableClassException
    {
        try
        {
            agentInstrumentation = instrumentation;

            modifyBootstrapClasspathByArgs(agentArguments);
            modifyBootstrapClasspath(instrumentation);

            if (!AgentConfiguration.isAgentEnabled())
            {
                LOG.info("Agent is disabled");
                return;
            }

            LOG.info("Loading bond agent");

            startSensors();

    	    LOG.info("!!Dumping");

            startDumping();

	        LOG.info("!!Start transformer");

            String excludes[] = AgentConfiguration.getExcludeClasses();
            String ignoreExcludes[] = AgentConfiguration.getIgnoreExcludeClasses();

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

    	    LOG.info("!!Retransform");
            retransformAlreadyLoadedClasses(instrumentation);

        }
        catch (Throwable e)
        {
            LOG.severe("Error loading agent " + e.getMessage());
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
            LOG.severe("Agent loading error " + e.getMessage());
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

    private static void modifyBootstrapClasspath(Instrumentation instrumentation) throws IOException
    {
        //		if (AgentManager.agentJar != null) {
        //			instrumentation.appendToBootstrapClassLoaderSearch(AgentManager.agentJar);
        //		}
        //
        //		URLClassLoader loader = AgentManager.appClassloader;
        //
        //		if (loader == null)
        //			loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        //
        //		for (URL url : loader.getURLs()) {
        //			if (AgentManager.agentJar == null && url.getFile().startsWith("bond") && url.getFile().endsWith(".jar")) {
        //				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
        //			}
        //			if (url.getFile().contains("slf4j-api")) {
        //				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
        //			} else if (url.getFile().contains("slf4j-log4j12")) {
        //				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
        //			} else if (url.getFile().contains("log4j-")) {
        //				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
        //			} else if (url.getFile().contains("commons-lang3-")) {
        //				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
        //			} else if (url.getFile().contains("javassist")) {
        //				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getPath()));
        //			}
        //		}
    }

    private static void modifyBootstrapClasspathByArgs(String agentArguments) throws IOException
    {
        if (System.getProperty("agent.jar") != null)
        {
            //LOG.severe("Adding agent.jar to bootstrap classpath " + System.getProperty("agent.jar"));
            agentInstrumentation.appendToBootstrapClassLoaderSearch(new JarFile(System.getProperty("agent.jar")));
            agentInstrumentation.appendToSystemClassLoaderSearch(new JarFile(System.getProperty("agent.jar")));
            //modifySystemClassloader(System.getProperty("agent.jar"));
        }
    }

    private static void modifySystemClassloader(String... jars)
    {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        Class[] parameters = new Class[] { URL.class };

        try
        {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            for (String jar : jars)
            {
                method.invoke(sysloader, new File(jar).toURL());
            }
        }
        catch (Throwable e)
        {
            //LOG.severe("Error modifing system classloader: " + e.getMessage());
            throw new RuntimeException(e);
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
                        //LOG.info("Retransform class: "+cls.getName());
                        instrumentation.retransformClasses(cls);
                    }
                    catch (UnmodifiableClassException e)
                    {
                        LOG.severe("unmodifiable class " + cls.getName());
                    }
                }
            }
        }catch(java.lang.Error e){
            LOG.severe("Error retransforming class: " + e);
        }
    }

    private static void startDumping() throws FileNotFoundException
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
                    //LOG.severe("Shutdown hook error: " + e.getMessage());
                }
            }
        });
    }

    private static void startSensors()
    {
        JvmMonitoring.getInstance().start();
    }
}
