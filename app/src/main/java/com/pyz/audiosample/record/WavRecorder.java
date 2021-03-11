package com.pyz.audiosample.record;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WavRecorder implements RecorderInterface.Recorder {

	private final static String TAG = "WavRecord";
	private final static int WAR_HEADER_LENGTH = 44;
	private final static int RECORDER_BPP = 16; //bits per sample
	private final static int SEC_INTERNAL = 1000;
	private final static int BUFFER_COUNT = 5;
	private final static int TIME_OUT = 20;

	private AudioRecord mRecord;
	private RecorderInterface.StateListener stateListener;

	private int bufferSize = 0;
	private int sampleRate = 44100;
	private int channelCount = 1;
	private boolean isRecording;
	private boolean isPaused;

	private Thread recordingThread;
	private File audioFile;

	private Timer timerProgress;
	private long progress = 0;

	private AudioTrack mTrack;
	private BufferPool mBufferPool;
	private Thread mPlayThread;

	private static class WavRecorderSingletonHolder {
		private static WavRecorder singleton = new WavRecorder();

		public static WavRecorder getSingleton() {
			return WavRecorderSingletonHolder.singleton;
		}
	}

	public static WavRecorder getInstance() {
		return WavRecorderSingletonHolder.getSingleton();
	}

	private WavRecorder() {
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
			audioFile = new File(outputFile);
			int channelConfig = channelCount == 1 ? AudioFormat.CHANNEL_IN_MONO :
					AudioFormat.CHANNEL_IN_STEREO;
			bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
					AudioFormat.ENCODING_PCM_16BIT);
			mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize);

			int outChannelConfig = channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO :
					AudioFormat.CHANNEL_OUT_STEREO;
			int minSize = AudioTrack.getMinBufferSize(sampleRate,
					outChannelConfig, AudioFormat.ENCODING_PCM_16BIT);
			mTrack = new AudioTrack.Builder()
					.setAudioAttributes(new AudioAttributes.Builder()
							.setUsage(AudioAttributes.USAGE_MEDIA)
							.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
							.build())
					.setAudioFormat(new AudioFormat.Builder()
							.setEncoding(AudioFormat.ENCODING_PCM_16BIT)
							.setSampleRate(sampleRate)
							.setChannelMask(outChannelConfig)
							.build())
					.setBufferSizeInBytes(minSize)
					.build();
			mBufferPool = new BufferPool(BUFFER_COUNT, bufferSize);
			Log.i(TAG, "minBufferSize:" + bufferSize + ",minSize:" + minSize);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			if (mRecord != null) {
				mRecord.release();
				mRecord = null;
			}
			if (mTrack != null) {
				mTrack.release();
				mTrack = null;
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

	@Override
	public void startRecording() {
		Log.i(TAG, "startRecording");
		if (mRecord != null && mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			try {
				mRecord.startRecording();
				isRecording = true;
				if (mTrack != null) {
					mTrack.play();
				}
				if (!isPaused && recordingThread == null) {
					recordingThread = new Thread(new Runnable() {
						@Override
						public void run() {
							writeAudioDataToFile();
						}
					});
					recordingThread.start();
					startPlayThraed();
				} else if (isPaused) {
					synchronized (mPlayThread) {
						isPaused = false;
						mPlayThread.notify();
						Log.i(TAG, "notify");
					}
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
				if (mTrack != null) {
					mTrack.pause();
				}
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
			} finally {
				mRecord.release();
				mRecord = null;
				synchronized (mPlayThread) {
					mPlayThread.notify();
				}
			}
			mPlayThread = null;
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

	private void writeAudioDataToFile() {
		FileOutputStream out;
		try {
			out = new FileOutputStream(audioFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			out = null;
		}
		if (out != null) {
			try {
				//先写入wav文件头44字节，最后修改这44字节数据
				byte[] header = new byte[WAR_HEADER_LENGTH];
				out.write(header);
				int readSize = 0;
				while (isRecording) {
					if (isPaused) {
						synchronized (mPlayThread) {
							mPlayThread.wait();
							Log.i(TAG, "wait end");
						}
						continue;
					}
					BufferPool.AudioData audioData = mBufferPool.takeFreeBuffer();
					if (audioData != null) {
						readSize = mRecord.read(audioData.data, 0, bufferSize);
						Log.i(TAG, "readSize:" + readSize);
						if (readSize <= 0) {
							audioData.readSize = 0;
						} else {
							audioData.readSize = readSize;
							out.write(audioData.data, 0, readSize);
						}
						mBufferPool.enque(audioData);
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

			setWaveFileHeader(audioFile, channelCount);
		}
	}

	private void setWaveFileHeader(File file, int channels) {
		long fileSize = file.length();
		//头中的 总长度 忽略8个字节（RIFF 4个字节,和自身所占4个字节）。
		long totalSize = fileSize - 8;
		long totalAudioLen = fileSize - WAR_HEADER_LENGTH;
		long byteRate = sampleRate * channels * (RECORDER_BPP / 8); //2 byte per 1 sample for 1
		// channel.

		try {
			final RandomAccessFile wavFile = randomAccessFile(file);
			wavFile.seek(0); // to the beginning
			wavFile.write(generateHeader(totalAudioLen, totalSize, sampleRate, channels,
					byteRate));
			wavFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private RandomAccessFile randomAccessFile(File file) {
		RandomAccessFile randomAccessFile;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return randomAccessFile;
	}

	private byte[] generateHeader(
			long totalAudioLen, long totalDataLen, long longSampleRate, int channels,
			long byteRate) {
		byte[] header = new byte[WAR_HEADER_LENGTH];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; //16 for PCM. 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (channels * (RECORDER_BPP / 8)); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		return header;
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

	private void startPlayThraed() {
		//创建HandlerThread实例
		mPlayThread = new Thread(new Runnable() {
			@Override
			public void run() {
				playAudioData();
			}
		});
		//开始运行线程
		mPlayThread.start();
	}

	private void playAudioData() {
		Log.i(TAG, "playAudioData start");
		try {
			while (isRecording) {
				BufferPool.AudioData audioData = mBufferPool.pollDataBuffer(TIME_OUT,
						TimeUnit.MILLISECONDS);
				if (audioData != null) {
					int size = audioData.readSize;
					if (mTrack != null && mTrack.getState() == AudioTrack.STATE_INITIALIZED) {
						if (size > 0) {
							Log.i(TAG, "write size:" + size);
							mTrack.write(audioData.data, 0, size);
						}
					}
					mBufferPool.deque(audioData);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (mTrack != null) {
				mTrack.stop();
				mTrack.release();
				mTrack = null;
			}
			if (mBufferPool == null) {
				mBufferPool.clear();
				mBufferPool = null;
			}
		}
		Log.i(TAG, "playAudioData end ");
	}

}
