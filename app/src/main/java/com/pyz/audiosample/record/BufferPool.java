package com.pyz.audiosample.record;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BufferPool {
	private LinkedBlockingQueue<AudioData> mFreeQueue;
	private LinkedBlockingQueue<AudioData> mDataQueue;

	public class AudioData {
		byte[] data;
		int readSize;

		AudioData(int bufferSize) {
			data = new byte[bufferSize];
		}
	}

	public BufferPool(int capacity, int bufferSize) {
		mFreeQueue = new LinkedBlockingQueue<AudioData>(capacity);
		for (int i = 0; i < capacity; i++) {
			mFreeQueue.add(new AudioData(bufferSize));
		}
		mDataQueue = new LinkedBlockingQueue<AudioData>(capacity);
	}

	public int getFreeSize() {
		return mFreeQueue.size();
	}

	public AudioData takeFreeBuffer() throws InterruptedException {
		return mFreeQueue.take();
	}

	public AudioData pollFreeBuffer() {
		return mFreeQueue.poll();
	}

	public AudioData pollFreeBuffer(long timeout, TimeUnit unit) throws InterruptedException {
		return mFreeQueue.poll(timeout, unit);
	}

	public void enque(AudioData data) throws InterruptedException {
		mDataQueue.put(data);
	}

	public AudioData takeDataBuffer() throws InterruptedException {
		return mDataQueue.take();
	}

	public AudioData pollDataBuffer() {
		return mDataQueue.poll();
	}

	public AudioData pollDataBuffer(long timeout, TimeUnit unit) throws InterruptedException {
		return mDataQueue.poll(timeout, unit);
	}

	public void deque(AudioData data) throws InterruptedException {
		mFreeQueue.put(data);
	}

	public void clear() {
		if (mFreeQueue != null) {
			mFreeQueue.clear();
			mFreeQueue = null;
		}
		if (mDataQueue != null) {
			mDataQueue.clear();
			mDataQueue = null;
		}
	}
}
