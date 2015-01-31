package com.focusit.agent.analyzer;

import com.focusit.agent.analyzer.data.netty.jvm.JvmDataImport;
import com.focusit.agent.analyzer.data.netty.jvm.NettyJvmData;
import com.focusit.agent.analyzer.data.netty.methodmap.MethodMapImport;
import com.focusit.agent.analyzer.data.netty.methodmap.NettyMethodMapData;
import com.focusit.agent.analyzer.data.netty.session.NettySessionManager;
import com.focusit.agent.analyzer.data.netty.session.NettySessionStart;
import com.focusit.agent.analyzer.data.netty.statistics.NettyStatisticsData;
import com.focusit.agent.analyzer.data.netty.statistics.StatisticReportImport;
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

		MethodMapImport methodMapImport = (MethodMapImport) ctx.getBean("methodMapImport");
		JvmDataImport jvmDataImport = (JvmDataImport) ctx.getBean("jvmDataImport");
		//StatisticsImport statisticsImport = (StatisticsImport) ctx.getBean("statisticsImport");
		StatisticReportImport statisticReportImport = (StatisticReportImport) ctx.getBean("statisticReportImport");

		NettySessionManager sessionManager = (NettySessionManager) ctx.getBean("nettySessionManager");
//		sessionManager.setImportsToNotify(methodMapImport, jvmDataImport, statisticsImport);
		sessionManager.setImportsToNotify(methodMapImport, jvmDataImport, statisticReportImport);

		service.submit(new NettySessionStart(sessionManager));
		service.submit(new NettyMethodMapData(methodMapImport));
		service.submit(new NettyJvmData(jvmDataImport));
//		service.submit(new NettyStatisticsData(statisticsImport));
		service.submit(new NettyStatisticsData(statisticReportImport));
	}
}
