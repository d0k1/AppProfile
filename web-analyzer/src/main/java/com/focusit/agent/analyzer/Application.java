package com.focusit.agent.analyzer;

import com.focusit.agent.analyzer.data.netty.jvm.JvmDataImport;
import com.focusit.agent.analyzer.data.netty.jvm.NettyJvmData;
import com.focusit.agent.analyzer.data.netty.os.NettyOSData;
import com.focusit.agent.analyzer.data.netty.os.OSDataImport;
import com.focusit.agent.analyzer.data.netty.profiler.ProfilerControl;
import com.focusit.agent.analyzer.data.netty.session.NettySessionManager;
import com.focusit.agent.analyzer.data.netty.session.NettySessionStart;
import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.focusit"})
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		ExecutorService service = Executors.newFixedThreadPool(4, new NettyThreadFactory("NettyExecutorService"));

		JvmDataImport jvmDataImport = (JvmDataImport) ctx.getBean("jvmDataImport");
		OSDataImport osDataImport = (OSDataImport) ctx.getBean(OSDataImport.class);

		NettySessionManager sessionManager = (NettySessionManager) ctx.getBean("nettySessionManager");
		sessionManager.setImportsToNotify(jvmDataImport, osDataImport);

		service.submit(new NettySessionStart(sessionManager));
		service.submit(new NettyJvmData(jvmDataImport));
		service.submit(new NettyOSData(osDataImport));

		ProfilerControl ctrl = (ProfilerControl) ctx.getBean("profilerControl");

		service.submit(ctrl);
	}
}
