package com.focusit.agent.metrics.samples;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Class to hold host OS metric sample
 * Created by Denis V. Kirpichenkov on 08.12.14.
 */
public class OSInfo implements Sample<OSInfo> {

	public final static int DEVICES=4;

	public long ifIn[] = new long[DEVICES];
	public long ifOut[] = new long[DEVICES];

	public long reads[] = new long[DEVICES];
	public long writes[] = new long[DEVICES];

	public long time;
	public long timestamp;
	public long appId;

	@Override
	public Sample<OSInfo> copyDataFrom(Sample<OSInfo> sample) {
		for(int i=0;i<DEVICES;i++){
			this.ifIn[i] = ((OSInfo)sample).ifIn[i];
			this.ifOut[i] = ((OSInfo)sample).ifOut[i];

			this.reads[i] = ((OSInfo)sample).reads[i];
			this.writes[i] = ((OSInfo)sample).writes[i];
		}

		this.time = ((OSInfo)sample).time;
		this.timestamp = ((OSInfo)sample).timestamp;

		return this;
	}

	@Override
	public void writeToBuffer(ByteBuffer buffer) {
		for(int i=0;i<DEVICES;i++) {
			buffer.putLong(ifIn[i]);
			buffer.putLong(ifOut[i]);

			buffer.putLong(reads[i]);
			buffer.putLong(writes[i]);
		}
		buffer.putLong(time);
		buffer.putLong(timestamp);
	}

	@Override
	public void readFromBuffer(ByteBuffer buffer) {
		for(int i=0;i<DEVICES;i++) {
			ifIn[i] = buffer.getLong();
			ifOut[i] = buffer.getLong();

			reads[i] = buffer.getLong();
			writes[i] = buffer.getLong();
		}

		time = buffer.getLong();
		timestamp = buffer.getLong();
	}

	@Override
	public void writeToBuffer(ByteBuf buffer) {
		for(int i=0;i<DEVICES;i++) {
			buffer.writeLong(ifIn[i]);
			buffer.writeLong(ifOut[i]);

			buffer.writeLong(reads[i]);
			buffer.writeLong(writes[i]);
		}
		buffer.writeLong(time);
		buffer.writeLong(timestamp);
	}

	@Override
	public void readFromBuffer(ByteBuf buffer) {
		for(int i=0;i<DEVICES;i++) {
			ifIn[i] = buffer.readLong();
			ifOut[i] = buffer.readLong();

			reads[i] = buffer.readLong();
			writes[i] = buffer.readLong();
		}

		time = buffer.readLong();
		timestamp = buffer.readLong();
	}

	@Override
	public void readFromBuffer(long[] buffer) {
		int index = 0;
		for(int i=0;i<DEVICES;i++) {
			ifIn[i] = buffer[index++];
			ifOut[i] = buffer[index++];

			reads[i] = buffer[index++];
			writes[i] = buffer[index++];
		}

		time = buffer[index++];
		timestamp = buffer[index++];

	}

	@Override
	public int sizeOfSample() {
		return sizeOf();
	}

	@Override
	public long getAppId() {
		return appId;
	}

	public static int sizeOf() {
		return 4*8 + 4*8 + 4*8 + 4*8 + 8 + 8 + 8; // 152 bytes
	}

}
