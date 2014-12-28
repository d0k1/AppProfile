package com.focusit.agent.analyzer;

import com.focusit.agent.analyzer.data.netty.jvm.NettyJvmData;
import com.focusit.agent.analyzer.data.netty.methodmap.NettyMethodMapData;
import com.focusit.agent.analyzer.data.netty.statistics.NettyStatisticsData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
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
		ExecutorService service = Executors.newFixedThreadPool(4);
		service.submit(new NettyJvmData());
		service.submit(new NettyMethodMapData());
		service.submit(new NettyStatisticsData());

		SpringApplication.run(Application.class, args);
	}
}
