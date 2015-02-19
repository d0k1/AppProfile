package com.focusit.agent.metrics.dump;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.SessionManager;
import com.focusit.agent.metrics.dump.file.JvmMonitoringDiskDumper;
import com.focusit.agent.metrics.dump.file.MethodsMapDiskDumper;
import com.focusit.agent.metrics.dump.netty.JvmMonitoringNettyDumper;
import com.focusit.agent.metrics.dump.netty.MethodsMapNettyDumper;

import java.io.IOException;

/**
 * Facade storage implementation.
 * <p/>
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class SamplesDumpManager implements SamplesDataDumper {

	private SamplesDataDumper storages[];

	public void startDiskDumpers() throws IOException {
		storages = new SamplesDataDumper[2];

		storages[0] = new MethodsMapDiskDumper();
		storages[1] = new JvmMonitoringDiskDumper();
	}

	public void startNettyDumpers() throws IOException, InterruptedException {
		SessionManager manager = new SessionManager();
		manager.start();

		storages = new SamplesDataDumper[2];

		storages[0] = new MethodsMapNettyDumper();
		storages[1] = new JvmMonitoringNettyDumper();
	}

	public SamplesDumpManager() throws IOException, InterruptedException {
		if(AgentConfiguration.getDumpType()== AgentConfiguration.DumpType.disk){
			startDiskDumpers();
		}else if(AgentConfiguration.getDumpType()== AgentConfiguration.DumpType.netty){
			startNettyDumpers();
		}
	}

	@Override
	public void dumpRest() throws InterruptedException {
		for (SamplesDataDumper s : storages) {
			s.dumpRest();

			System.out.println(String.format("Dumped %s samples by %s", s.getSamplesRead(), s.getName()));
		}
	}

	@Override
	public void exit() throws InterruptedException {
		for (SamplesDataDumper s : storages) {
			s.exit();
		}
	}

	@Override
	public void start() {
		for (SamplesDataDumper s : storages) {
			s.start();
		}
	}

	@Override
	public long getSamplesRead() {
		return 0;
	}

	@Override
	public String getName() {
		return "Dumpers facade";
	}
}
