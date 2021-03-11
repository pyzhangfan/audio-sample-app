package com.pyz.audiosample.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.pyz.audiosample.util.AacUtil;
import com.pyz.audiosample.util.MediaUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AacRecorder implements RecorderInterface.Recorder {
	private final static String TAG = AacRecorder.class.getSimpleName();

	private final static String AAC_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
	private final static int AAC_HEADER_SIZE = 7;
	private final static int TIME_OUT = 20;
	private final static int BUFFER_COUNT = 10;

	private final static int SEC_INTERNAL = 1000;
	private AudioRecord mRecord;
	private RecorderInterface.StateListener stateListener;

	private int bufferSize = 0;
	private int sampleRate = 44100;
	private int channelCount = 1;
	private int bitRate = 128000;
	private int aacLevel = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

	private boolean isRecording;
	private boolean isPaused;
	private boolean isEncodeEnd;

	private Thread recordingThread;
	private File audioFile;

	private Timer timerProgress;
	private long progress = 0;

	private MediaCodec mCodec;
	private MediaFormat mediaFormat;
	private Thread encodeThread;
	private BufferPool mBufferPool;

	private static class AacRecorderSingletonHolder {
		private static AacRecorder singleton = new AacRecorder();

		public static AacRecorder getSingleton() {
			return AacRecorderSingletonHolder.singleton;
		}
	}

	public static AacRecorder getInstance() {
		return AacRecorderSingletonHolder.getSingleton();
	}

	private AacRecorder() {
	}

	@Override
	public void setStateListener(RecorderInterface.StateListener stateListener) {
		this.stateListener = stateListener;
	}

	@Override
	public void prepare(String outputFile, int channelCount, int sampleRate, int bitrate) {
		try {
			this.sampleRate = sampleRate;
			this.channelCount = channelCount;
			bitRate = bitrate;
			audioFile = new File(outputFile);
			int channelConfig = channelCount == 1 ? AudioFormat.CHANNEL_IN_MONO :
					AudioFormat.CHANNEL_IN_STEREO;
			bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
					AudioFormat.ENCODING_PCM_16BIT) * 2;
			mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			initMediaCodec(sampleRate, channelCount);
			mBufferPool = new BufferPool(BUFFER_COUNT, bufferSize);
			isEncodeEnd = false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			if (mRecord != null) {
				mRecord.release();
				mRecord = null;
			}
		}

		if (mRecord != null && mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			if (stateListener != null)
				stateListener.onPrepareRecord();
		} else {
			if (stateListener != null)
				stateListener.onError("new AudioRecord error");
		}
	}

	private void initMediaCodec(int sampleRate,
								int channelCount) {
		MediaCodecInfo info = MediaUtil.getEncoderCodecInfo(AAC_TYPE);
		if (info != null) {
			try {
				mCodec = MediaCodec.createByCodecName(info.getName());
				mediaFormat = MediaFormat.createAudioFormat(AAC_TYPE
						, sampleRate,
						channelCount);
				mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
						aacLevel);
				mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
				mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
				mediaFormat.setInteger(MediaFormat.KEY_PCM_ENCODING,
						AudioFormat.ENCODING_PCM_16BIT);
				mCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			} catch (IOException e) {
				e.printStackTrace();
				if (mCodec == null) {
					mCodec.release();
					mCodec = null;
				}
			}
		}
	}

	@Override
	public void startRecording() {
		Log.i(TAG, "startRecording");
		if (mRecord != null && mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			try {
				mRecord.startRecording();
				isRecording = true;
				if (!isPaused && recordingThread == null) {
					mCodec.start();
					recordingThread = new Thread(new Runnable() {
						@Override
						public void run() {
							readAudioData();
						}
					});
					recordingThread.start();
					encodeThread = new Thread(new Runnable() {
						@Override
						public void run() {
							encodeAudioDataToFile();
						}
					});
					encodeThread.start();
				}
				isPaused = false;
				startRecordingTimer();
				if (stateListener != null) {
					stateListener.onStartRecord(null);
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void pauseRecording() {
		Log.i(TAG, "pauseRecording");
		if (mRecord != null && isRecording) {
			try {
				pauseRecordingTimer();
				mRecord.stop();
				isPaused = true;
				if (stateListener != null) {
					stateListener.onPauseRecord();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stopRecording() {
		Log.i(TAG, "stopRecording");
		if (mRecord != null) {
			try {
				stopRecordingTimer();
				isRecording = false;
				isPaused = false;
				mRecord.stop();
				recordingThread = null;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

			if (stateListener != null) {
				stateListener.onStopRecord(null);
			}
		}
	}

	@Override
	public boolean isRecording() {
		return isRecording;
	}

	@Override
	public boolean isPaused() {
		return isPaused;
	}

	private void readAudioData() {
		Log.i(TAG, "readAudioData start");
		long totalSize = 0;
		try {
			int readSize = 0;
			while (isRecording) {
				if (!isPaused) {
//					Log.i(TAG, "mBufferPool free size:" + mBufferPool.getFreeSize());
					BufferPool.AudioData audioData = mBufferPool.takeFreeBuffer();
					readSize = mRecord.read(audioData.data, 0, bufferSize);
					if (readSize < 0) {
						audioData.readSize = 0;
					} else {
						totalSize += readSize;
						audioData.readSize = readSize;
					}
					mBufferPool.enque(audioData);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "readAudioData end " + totalSize);
	}

	private void encodeAudioDataToFile() {
		Log.i(TAG, "encodeAudioDataToFile start");
		FileOutputStream out;
		try {
			out = new FileOutputStream(audioFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			out = null;
		}
		long totalSize = 0;
		if (out != null) {
			try {
				//应该够保存编码后的aac数据
				byte[] aacData = new byte[bufferSize / 5];
				byte[] aacHeader = new byte[AAC_HEADER_SIZE];
				//首次主要设置header的1~3、7字节，后续只需要修改4~6字节即可。
				AacUtil.addADTStoPacket(aacLevel, sampleRate, channelCount, aacHeader, 0);
				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				while (!isEncodeEnd) {
					int inputBufferId = mCodec.dequeueInputBuffer(TIME_OUT);
					if (inputBufferId >= 0) {
						BufferPool.AudioData audioData = mBufferPool.pollDataBuffer(TIME_OUT,
								TimeUnit.MILLISECONDS);

						if (audioData != null) {
							ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferId);
//							Log.i(TAG, "put len:" + audioData.readSize);
							byte[] data = audioData.data;
//							totalSize += audioData.readSize;
							inputBuffer.put(data, 0, audioData.readSize);
							mCodec.queueInputBuffer(inputBufferId, 0, audioData.readSize, 0,
									0);
							mBufferPool.deque(audioData);
						} else if (audioData == null && isRecording) {
							mCodec.queueInputBuffer(inputBufferId, 0, 0, 0,
									0);
						} else {
							mCodec.queueInputBuffer(inputBufferId, 0, 0, 0,
									MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						}
					}

					int outputBufferId = mCodec.dequeueOutputBuffer(bufferInfo, TIME_OUT);
					if (outputBufferId >= 0) {
						ByteBuffer outputBuffer = mCodec.getOutputBuffer(outputBufferId);
//							MediaFormat bufferFormat = mCodec.getOutputFormat(outputBufferId);
						// option A
//						Log.i(TAG,
//								"bufferInfo offset:" + bufferInfo.offset + ",size:" + bufferInfo.size
//										+ ",presentationTimeUs:" + bufferInfo.presentationTimeUs + ",flags:" + bufferInfo.flags);
						if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
							Log.i(TAG, "BUFFER_FLAG_END_OF_STREAM");
							isEncodeEnd = true;
						} else {
							if (outputBuffer != null && bufferInfo.size > 0) {
								int len = bufferInfo.size;
								outputBuffer.get(aacData, 0, bufferInfo.size);
								//设置header
								AacUtil.setADTSPacketLen(channelCount, aacHeader,
										len + AAC_HEADER_SIZE);
								totalSize += len;
								out.write(aacHeader);
								out.write(aacData, 0, len);
							}
						}
						// bufferFormat is identical to outputFormat
						// outputBuffer is ready to be processed or rendered.
						mCodec.releaseOutputBuffer(outputBufferId, false);
					} else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
						// Subsequent data will conform to new format.
						// Can ignore if using getOutputFormat(outputBufferId)
						mediaFormat = mCodec.getOutputFormat(); // option B
					}
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mBufferPool.clear();
			mBufferPool = null;
			mCodec.stop();
			mCodec.release();
			mCodec = null;
		}
		Log.i(TAG, "encodeAudioDataToFile end " + totalSize);
	}

	private void startRecordingTimer() {
		timerProgress = new Timer();
		timerProgress.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stateListener != null && mRecord != null) {
					stateListener.onRecordProgress(progress, 0);
					progress += SEC_INTERNAL;
				}
			}
		}, 0, SEC_INTERNAL);
	}

	private void stopRecordingTimer() {
		timerProgress.cancel();
		timerProgress.purge();
		progress = 0;
	}

	private void pauseRecordingTimer() {
		timerProgress.cancel();
		timerProgress.purge();
	}

}
