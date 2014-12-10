package com.focusit.agent.metrics.dump;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.file.MethodsMapDiskDumper;
import com.focusit.agent.metrics.dump.file.StatisticDiskDumper;

import java.io.FileNotFoundException;

/**
 * Facade storage implementation.
 * <p/>
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class SamplesDumpManager implements SamplesDataDumper {

	private SamplesDataDumper storages[] = new SamplesDataDumper[2];

	public SamplesDumpManager() throws FileNotFoundException {
		storages[0] = new StatisticDiskDumper(AgentConfiguration.getStatisticsFile());
		storages[1] = new MethodsMapDiskDumper(AgentConfiguration.getMethodsMapFile());
	}

	@Override
	public void dumpRest() {
		for (SamplesDataDumper s : storages) {
			s.dumpRest();
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
}
