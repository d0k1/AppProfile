package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.agent.metrics.samples.OSInfo;
import com.focusit.agent.metrics.samples.Sample;
import com.focusit.agent.utils.common.FixedSamplesArray;
import com.focusit.agent.utils.jmm.FinalBoolean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OS specific metrics. Currently Linux only
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public class OSMonitoring {
	private static final long appId = AgentConfiguration.getAppId();

	public static FinalBoolean enabled = new FinalBoolean(AgentConfiguration.isOsMonitoringEnabled());
	private final static int LIMIT = AgentConfiguration.getOsBufferLength();

	private final static String[] faces = AgentConfiguration.getNetworkInterfaces();
	private final static String[] drives = AgentConfiguration.getHdDrives();

	private final static FixedSamplesArray<OSInfo> data = new FixedSamplesArray<>(LIMIT, new FixedSamplesArray.ItemInitializer() {
		@Override
		public Sample[] initData(int limit) {
			return new OSInfo[limit];
		}

		@Override
		public Sample createItem() {
			return new OSInfo();
		}
	}, "OsStat", AgentConfiguration.getOsDumpBatch());

	private final static OSMonitoring instance = new OSMonitoring();

	private final Thread monitoringThread;

	private RandomAccessFile facesReader = null;
	private RandomAccessFile hddsReader = null;

	private String facesData[] = null;
	private String hddsData[] = null;

	private String facesPatternString = "\\s+(\\w+)\\:\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)";
	private Pattern facesPattern = Pattern.compile(facesPatternString);

	private String hddPatternString = "\\s+(\\d+)\\s+(\\d+)\\s+(\\w+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)";
	private Pattern hddPattern = Pattern.compile(hddPatternString);

	OSInfo sample = new OSInfo();


	public OSMonitoring() {

		try {
			facesReader = new RandomAccessFile("/proc/net/dev", "r");
			hddsReader = new RandomAccessFile("/proc/diskstats", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		facesData = new String[faces.length];
		hddsData = new String[drives.length];

		monitoringThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int interval = AgentConfiguration.getOsMonitoringInterval();

				while (!Thread.interrupted()) {
					try {
						writeMeasure();
						Thread.sleep(interval);
					} catch (InterruptedException | IOException e) {
						break;
					}
				}
			}
		}, "OSMonitoring thread");

		monitoringThread.setDaemon(true);

	}

	private void writeMeasure() throws InterruptedException, IOException {
		FinalBoolean working = enabled;

		if (!working.value)
			return;

		if (data.isFull()) {
			System.err.println("No memory to store sample in " + data.getName());
		}

		String line = null;
		if(facesReader!=null){
			while((line = facesReader.readLine())!=null){
				for(int i=0;i<faces.length;i++){
					if(line.contains(faces[i]) && facesData[i]==null){
						facesData[i] = line;
					}
				}
			}
			facesReader.seek(0);
		}

		if(hddsReader!=null){
			while((line=hddsReader.readLine())!=null){
				for(int i=0;i<drives.length;i++){
					if(line.contains(drives[i]) && hddsData[i]==null){
						hddsData[i] = line;
					}
				}
			}
			hddsReader.seek(0);
		}

		for(int i=0;i<facesData.length;i++) {
			Matcher m = facesPattern.matcher(facesData[i]);
			if(m.matches()) {
				Long received = Long.parseLong(m.group(2));
				Long transmitted = Long.parseLong(m.group(10));

				if (i < OSInfo.DEVICES) {
					sample.ifIn[i] = received;
					sample.ifOut[i] = transmitted;
				}
			}
			facesData[i] = null;
		}

		for(int i=0;i<hddsData.length;i++) {
			Matcher m = hddPattern.matcher(hddsData[i]);
			if(m.matches()) {
				Long read = Long.parseLong(m.group(6));
				Long write = Long.parseLong(m.group(10));
				if (i < OSInfo.DEVICES) {
					sample.reads[i] = read;
					sample.writes[i] = write;
				}
			}
			hddsData[i] = null;
		}

		sample.time = GlobalTime.getCurrentTime();
		sample.timestamp = GlobalTime.getCurrentTimeInMillis();
		sample.appId = appId;

		data.writeItemFrom(sample);
	}
	public static final OSMonitoring getInstance() {
		return instance;
	}

	public void start() {
		monitoringThread.start();
	}

	public void stop() throws InterruptedException {
		monitoringThread.interrupt();
		monitoringThread.join(10000);
	}

	public void doMeasureAtExit() throws InterruptedException, IOException {
		writeMeasure();
		facesReader.close();
		hddsReader.close();
	}

	public static boolean hasMore() throws InterruptedException {
		return data.hasMore();
	}

	public static OSInfo readData(OSInfo info) throws InterruptedException {
		return data.readItemTo(info);
	}
}
