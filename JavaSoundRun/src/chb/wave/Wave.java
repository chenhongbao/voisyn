package chb.wave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Wave class represents the wave data. It provides interfaces to concatenate
 * wave files, record voice from input device, write wave data to files and get
 * short integer array (float, integer) from bytes.
 * 
 * @author Hongbao Chen
 * 
 */
public class Wave {

	public ByteArrayOutputStream ByteArrayOut = null;
	public AudioFormat Format = null;
	public boolean StopRecording = false;
	public int BufferSize = 0;

	public static AudioFileFormat.Type DefaultFileType = AudioFileFormat.Type.WAVE;
	public static AudioFormat.Encoding DefaultEncoding = AudioFormat.Encoding.PCM_SIGNED;
	public static AudioFormat DefaultFormat = new AudioFormat(Wave.DefaultEncoding, 44100.0f, 16, 1, 2, 44100.0f, false);
	
	private Thread PlayThread = null;

	/**
	 * Wrapper class for AudioFileFormat.Type.
	 * 
	 * @author Hongbao Chen
	 * 
	 */
	public static class Type {
		public static AudioFileFormat.Type WAVE = AudioFileFormat.Type.WAVE;
		public static AudioFileFormat.Type AIFC = AudioFileFormat.Type.AIFC;
		public static AudioFileFormat.Type AIFF = AudioFileFormat.Type.AIFF;
		public static AudioFileFormat.Type AU = AudioFileFormat.Type.AU;
		public static AudioFileFormat.Type SND = AudioFileFormat.Type.SND;
	}

	/**
	 * Wrapper class for AudioFormat.Encoding.
	 * 
	 * @author Administrator
	 * 
	 */
	public static class Encoding {
		public static AudioFormat.Encoding PCM_SIGNED = AudioFormat.Encoding.PCM_SIGNED;
		public static AudioFormat.Encoding PCM_UNSIGNED = AudioFormat.Encoding.PCM_UNSIGNED;
		public static AudioFormat.Encoding PCM_FLOAT = AudioFormat.Encoding.PCM_FLOAT;
		public static AudioFormat.Encoding ALAW = AudioFormat.Encoding.ALAW;
		public static AudioFormat.Encoding ULAW = AudioFormat.Encoding.ULAW;
	}

	private Wave() {
	}

	/**
	 * Create a new instance of Wave class.
	 * 
	 * @param format
	 *            AudioFormat of the wave file.
	 * @return New instance of Wave.
	 */
	public static Wave CreateWave(AudioFormat format) {
		Wave w = new Wave();
		w.Format = new AudioFormat(format.getEncoding(),
				format.getSampleRate(), format.getSampleSizeInBits(),
				format.getChannels(), format.getFrameSize(),
				format.getFrameRate(), false);

		return w;
	}

	/**
	 * Record() will record the sound and save it to the file.
	 * 
	 * @param seconds
	 *            The number of seconds of recording.
	 * @return The number of bytes recorded.
	 * @throws Exception
	 *             the string in Exception will indicate the cause of error.
	 */
	public int Record(float seconds) throws Exception {

		if (seconds < 0) {
			return 0;
		}

		if (this.BufferSize <= 0) {
			throw new Exception("Wave: BufferSize is equal ot below zero.");
		}

		TargetDataLine line = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				this.Format);
		if (!AudioSystem.isLineSupported(info)) {
			throw new Exception("Wave: The current DataLine.Info ("
					+ this.Format.toString() + ") is not supported.");
		}

		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
		} catch (LineUnavailableException ex) {
			throw new Exception("Wave: Cannot get DataLine with Info.");
		}

		this.ByteArrayOut = new ByteArrayOutputStream();

		if (line.isOpen() == false) {
			line.open(this.Format, this.BufferSize);
		}

		line.flush();

		byte[] buffer = new byte[this.BufferSize * 4];
		int numRead = 0;

		try {
			line.start();
			while (!this.StopRecording) {

				numRead = line.read(buffer, 0, this.BufferSize);

				if (numRead > 0) {
					this.ByteArrayOut.write(buffer, 0, numRead);
				} else {
					this.StopRecording = true;
				}

				int recSeconds = this.ByteArrayOut.size()
						/ this.Format.getFrameSize()
						/ (int) this.Format.getSampleRate();

				if (recSeconds >= seconds) {
					this.StopRecording = true;
				}
			}

			line.stop();
			line.flush();

		} catch (Exception ex) {
			throw new Exception("Wave: IO read error.");
		} finally {

			line.drain();
			line.close();
		}

		return this.ByteArrayOut.size();
	}

	/**
	 * Get byte array from the wave.
	 * 
	 * @return byte[]
	 */
	public byte[] GetBytes() {
		byte[] samples = this.ByteArrayOut.toByteArray();

		return samples;
	}

	/**
	 * Get short array from wave when it is 16-bit, PCM_SIGNED.
	 * 
	 * @return short[]
	 */
	public short[] Get16Bits() {

		byte[] bytearray = this.GetBytes();
		ByteBuffer bbuffer = ByteBuffer.wrap(bytearray);

		/**
		 * It is very important to care about the endianness of the bytes
		 * because it will make data wield when endianness is wrong.
		 */
		if (this.Format.isBigEndian() == false) {
			bbuffer.order(ByteOrder.LITTLE_ENDIAN);
		} else {
			bbuffer.order(ByteOrder.BIG_ENDIAN);
		}

		ShortBuffer buffer = bbuffer.asShortBuffer();

		short[] samples = new short[buffer.capacity()];
		buffer.get(samples);

		return samples;
	}

	/**
	 * Get integer array from wave when it is 32-bit, PCM_SIGNED.
	 * 
	 * @return int[]
	 */
	public int[] Get32Bits() {

		byte[] bytearray = this.GetBytes();
		ByteBuffer bbuffer = ByteBuffer.wrap(bytearray);

		if (this.Format.isBigEndian() == false) {
			bbuffer.order(ByteOrder.LITTLE_ENDIAN);
		} else {
			bbuffer.order(ByteOrder.BIG_ENDIAN);
		}

		IntBuffer buffer = bbuffer.asIntBuffer();
		buffer.array();

		int[] samples = new int[buffer.capacity()];
		buffer.get(samples);

		return samples;
	}

	/**
	 * Get float array from the wave when it is 32-bit, PCM_FLOAT.
	 * 
	 * @return
	 */
	public float[] GetFloats() {

		byte[] bytearray = this.GetBytes();
		ByteBuffer bbuffer = ByteBuffer.wrap(bytearray);

		if (this.Format.isBigEndian() == false) {
			bbuffer.order(ByteOrder.LITTLE_ENDIAN);
		} else {
			bbuffer.order(ByteOrder.BIG_ENDIAN);
		}

		FloatBuffer buffer = bbuffer.asFloatBuffer();

		float[] samples = new float[buffer.capacity()];
		buffer.get(samples);

		return samples;
	}

	/**
	 * Append wave to the end of the calling Wave instance.
	 * 
	 * @param wave
	 *            Wave instance to be appended.
	 * @return The reference to the calling Wave class.
	 * @throws Exception
	 *             When the calling Wave instance cannot be written to, it will
	 *             throw the exception.
	 */
	public Wave Append(Wave wave) throws Exception {
		if (wave == null) {
			return this;
		}

		if (wave.ByteArrayOut == null) {
			return this;
		}
		try {
			this.ByteArrayOut.write(wave.ByteArrayOut.toByteArray());
		} catch (IOException e) {
			throw new Exception("Wave: ByteArrayOutputStream IO error.");
		}
		return this;
	}

	/**
	 * Write wave data to the file with the AudioFileFormat.Type given.
	 * 
	 * @param path
	 *            Path to the file.
	 * @param type
	 *            The type of the audio file.
	 * @return true if sucessful and false otherwise.
	 * @throws Exception
	 *             It will throw exceptions at the IO error.
	 */
	public boolean WriteTo(String path, AudioFileFormat.Type type)
			throws Exception {
		if (path == null || path.length() == 0) {
			return false;
		}
		if (type == null) {
			return false;
		}
		File file = new File(path);
		if (file.exists() == false) {
			try {
				if (file.createNewFile() == false) {
					throw new Exception("Wave: Cannot create new file \'"
							+ path + "\' because file exists.");
				}
			} catch (IOException e) {
				throw new Exception("Wave: Cannot create new file \'" + path
						+ "\'");
			}
		}

		if (file.canWrite() == false)
			file.setWritable(true);
		byte[] buffer = this.ByteArrayOut.toByteArray();
		ByteArrayInputStream instream = new ByteArrayInputStream(buffer);
		AudioInputStream audioinput = new AudioInputStream(instream,
				this.Format, buffer.length / this.Format.getFrameSize());

		try {
			AudioSystem.write(audioinput, type, file);
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

	/**
	 * Read wave data from a file specified by a path.
	 * @param path File path.
	 * @return true for successful reading and false otherwise.
	 * @throws Exception Error when reading.
	 */
	public boolean ReadFrom(String path) throws Exception {

		if (path == null || path.length() == 0)
			return false;

		File file = new File(path);
		if (file.exists() == false) {
			return false;
		} else {
			return ReadFrom(file);
		}
	}

	/**
	 * Read wave data from the File file.
	 * @param file File instance of the wave file.
	 * @return true for success and false for failure.
	 * @throws Exception Error on reading.
	 */
	public boolean ReadFrom(File file) throws Exception {

		if (file.exists() == false)
			return false;
		
		if(file.canRead() == false) {
			file.setReadable(true);
		}
		
		AudioInputStream instream = null;
		try {
			instream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException e) {
			throw new Exception("Wave: Audio file not supported.");
		} catch (IOException e) {
			throw new Exception("Wave: Audio file IO error.");
		}

		this.Format = instream.getFormat();

		try {
			return this.ReadFrom(instream);
		} catch (Exception e) {
			throw new Exception("Wave: Error when reading from audio stream \'"
					+ file.getAbsolutePath() + "\'.\n"+e.getMessage());
		}

	}

	/**
	 * Read wave data from the InputStream.  
	 * <b>This method will not set the AudioFormat for the Wave instance.</b>
	 * @param instream InputStream of the wave file.
	 * @return true for success and false for failure.
	 * @throws Exception Error on reading.
	 */
	public boolean ReadFrom(InputStream instream) throws Exception {
		if (instream == null)
			return false;

		this.ByteArrayOut = new ByteArrayOutputStream();

		byte[] buffer = new byte[this.BufferSize];
		int numRead = 0;
		try {
			while (numRead != -1) {
				numRead = instream.read(buffer, 0, buffer.length);
				if (numRead > 0) {
					this.ByteArrayOut.write(buffer, 0, numRead);
				}
			}
		} catch (IOException e) {
			throw new Exception("Wave: Error when reading from audio stream or " +
					"writting to byte array.");
		} finally {
			this.ByteArrayOut.flush();
			instream.close();
		}

		return true;
	}

	/**
	 * Read wave data from the given URL
	 * @param url URL to the wave file.
	 * @return true for success and false for failure.
	 * @throws Exception Error on reading.
	 */
	public boolean ReadFrom(URL url) throws Exception {
		if (url == null)
			return false;

		try {
			AudioInputStream instream = AudioSystem.getAudioInputStream(url);
			this.Format = instream.getFormat();
			return this.ReadFrom(instream);
		} catch (Exception e) {
			throw new Exception("Wave: URL error.");
		}
	}

	/**
	 * Play the wave file.
	 * @throws Exception IO error on reading from DataLine and writting to 
	 * byte buffer.
	 */
	public void Play() throws Exception {

		if (this.ByteArrayOut == null || this.ByteArrayOut.size() == 0) {
			return;
		}
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				this.Format);

		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(this.Format);
		} catch (LineUnavailableException e) {
			throw new Exception("Wave: Line not available.");
		} catch (Exception e) {
			throw e;
		}

		auline.start();

		byte[] abData = this.ByteArrayOut.toByteArray();

		try {
			if (abData.length > 0) {
				auline.write(abData, 0, abData.length);
			}
		} catch (Exception e) {
			throw new Exception("Wave: Error on writting to SourceDataLine.");
		} finally {
			auline.drain();
			auline.close();
		}
	}

	/**
	 * Play the wave file in another thread.
	 */
	public void AsynPlay() {
		AsynTask task = new AsynTask();
		task.wave = this;

		Thread th = new Thread(task);
		this.PlayThread = th;
		th.start();
	}
	
	/**
	 * Wait until it finishes playing the wave file.
	 */
	public void AsynJoin() {
		if (this.PlayThread == null)
			return;
		
		try {
			this.PlayThread.join();
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Set the byte buffer inside tha Wave instance to the byte array
	 * given by bytes.
	 * @param bytes byte[]
	 * @throws Exception Cannot write to the inner byte buffer.
	 */
	public void SetBytes(byte[] bytes) throws Exception {
		if (bytes == null || bytes.length == 0)
			return;

		if (this.ByteArrayOut == null) {
			this.ByteArrayOut = new ByteArrayOutputStream();
		}

		this.ByteArrayOut.reset();
		try {
			this.ByteArrayOut.write(bytes);
		} catch (IOException e) {
			throw new Exception("Wave: Error when writting to ByteArray.");
		}
	}
	
	/**
	 * Set the short buffer inside tha Wave instance to the byte array
	 * given by bytes.
	 * @param shorts short[]
	 * @throws Exception Cannot write to the inner byte buffer.
	 */
	public void SetBytes(short[] shorts) throws Exception {
		if (shorts == null || shorts.length == 0)
			return;

		ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
		if (this.Format.isBigEndian()) {
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		for (int i = 0; i < shorts.length; ++i) {
			buffer.putShort(shorts[i]);
		}
		if (buffer.hasArray()) {
			try {
				this.SetBytes(buffer.array());
			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	 * Set the int buffer inside tha Wave instance to the byte array
	 * given by bytes.
	 * @param ints int[]
	 * @throws Exception Cannot write to the inner byte buffer.
	 */
	public void SetBytes(int[] ints) throws Exception {

		if (ints == null || ints.length == 0)
			return;

		ByteBuffer buffer = ByteBuffer.allocate(ints.length * 4);
		if (this.Format.isBigEndian()) {
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		for (int i = 0; i < ints.length; ++i) {
			buffer.putInt(ints[i]);
		}
		if (buffer.hasArray()) {
			try {
				this.SetBytes(buffer.array());
			} catch (Exception e) {
				throw e;
			}
		}
	}
	/**
	 * Set the float buffer inside tha Wave instance to the byte array
	 * given by bytes.
	 * @param floats float[]
	 * @throws Exception Cannot write to the inner byte buffer.
	 */
	public void SetBytes(float[] floats) throws Exception {
		if (floats == null || floats.length == 0)
			return;

		ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
		if (this.Format.isBigEndian()) {
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		for (int i = 0; i < floats.length; ++i) {
			buffer.putFloat(floats[i]);
		}
		if (buffer.hasArray()) {
			try {
				this.SetBytes(buffer.array());
			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	 * Task wrapper for Wave.AsynPlay().
	 * @author Hongbao Chen
	 *
	 */
	class AsynTask implements Runnable {

		public Wave wave = null;

		@Override
		public void run() {
			if (wave == null)
				return;

			try {
				wave.Play();
			} catch (Exception e) {
				return;
			} finally {
				this.wave = null;
			}

		}
	}

}
