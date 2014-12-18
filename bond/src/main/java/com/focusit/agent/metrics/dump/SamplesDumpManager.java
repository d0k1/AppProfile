package com.focusit.agent.metrics.dump;

import com.focusit.agent.metrics.dump.file.JvmMonitoringDiskDumper;
import com.focusit.agent.metrics.dump.file.MethodsMapDiskDumper;
import com.focusit.agent.metrics.dump.file.StatisticDiskDumper;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Facade storage implementation.
 * <p/>
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class SamplesDumpManager implements SamplesDataDumper {

	private final static Logger LOG = Logger.getLogger(SamplesDumpManager.class.getName());

	private SamplesDataDumper storages[] = new SamplesDataDumper[3];

	public SamplesDumpManager() throws IOException {
		storages[0] = new StatisticDiskDumper();
		storages[1] = new MethodsMapDiskDumper();
		storages[2] = new JvmMonitoringDiskDumper();
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
