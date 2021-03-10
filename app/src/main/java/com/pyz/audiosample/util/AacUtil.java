package com.pyz.audiosample.util;

public class AacUtil {
	//采样率 对应下标。
	private final static int[] sampleData = new int[]{96000, 88200, 64000, 48000, 44100, 32000,
			24000, 22050, 16000, 12000, 11025, 8000, 7350};

	private AacUtil() {
	}

	public static int getProfile(int aacLevel) {
		return aacLevel - 1;
	}

	public static int getSamplingIndex(int sampleRate) {
		for (int i = 0; i < sampleData.length; i++) {
			if (sampleData[i] == sampleRate)
				return i;
		}
		return 0;
	}

	public static void addADTStoPacket(int aacLevel, int sampleRate, int channelCou, byte[] packet,
								 int packetLen) {
		int profile = getProfile(aacLevel); // AAC LC
		int sampling_frequency_index = getSamplingIndex(sampleRate); // 采样率
		int chanCfg = channelCou; // channel_configuration

		// fill in ADTS data
		packet[0] = (byte) 0xFF;
		packet[1] = (byte) 0xF1;
		packet[2] = (byte) ((profile << 6) + (sampling_frequency_index << 2) + (chanCfg >> 2));
		packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
		packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
		packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		packet[6] = (byte) 0xFC;
	}

	/*
		adts header 第4~6个字节会由于包长度边度
		所以这里可以只修改这三个字节
	 */
	public static void setADTSPacketLen(int channelCou, byte[] packet,
										int packetLen){
		int chanCfg = channelCou;
		packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
		packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
		packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
	}
}
