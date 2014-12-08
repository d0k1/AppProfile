package com.focusit.utils.metrics.store;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.utils.metrics.store.file.MethodsMapDiskDumper;
import com.focusit.utils.metrics.store.file.StatisticDiskDumper;

import java.io.FileNotFoundException;

/**
 * Facade storage implementation.
 * <p/>
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class StorageManager implements Storage {

	private Storage storages[] = new Storage[2];

	public StorageManager() throws FileNotFoundException {
		storages[0] = new StatisticDiskDumper(AgentConfiguration.getStatisticsFile());
		storages[1] = new MethodsMapDiskDumper(AgentConfiguration.getMethodsMapFile());
	}

	@Override
	public void dumpRest() {
		for (Storage s : storages) {
			s.dumpRest();
		}
	}

	@Override
	public void exit() throws InterruptedException {
		for (Storage s : storages) {
			s.exit();
		}
	}

	@Override
	public void start() {
		for (Storage s : storages) {
			s.start();
		}
	}
}
