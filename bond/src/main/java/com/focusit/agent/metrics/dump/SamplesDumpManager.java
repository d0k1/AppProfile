package com.focusit.agent.metrics.dump;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.file.JvmMonitoringDiskDumper;
import com.focusit.agent.metrics.dump.file.MethodsMapDiskDumper;
import com.focusit.agent.metrics.dump.file.StatisticDiskDumper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * Facade storage implementation.
 * <p/>
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class SamplesDumpManager implements SamplesDataDumper {

	private final static Logger LOG = LoggerFactory.getLogger(SamplesDumpManager.class);

	private SamplesDataDumper storages[] = new SamplesDataDumper[3];

	public SamplesDumpManager() throws FileNotFoundException {
		storages[0] = new StatisticDiskDumper(AgentConfiguration.getStatisticsFile());
		storages[1] = new MethodsMapDiskDumper(AgentConfiguration.getMethodsMapFile());
		storages[2] = new JvmMonitoringDiskDumper(AgentConfiguration.getJvmMonitoringFile());
	}

	@Override
	public void dumpRest() {
		for (SamplesDataDumper s : storages) {
			s.dumpRest();

			LOG.info("Dumped {} samples by {}", s.getSamplesRead(), s.getName());
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
