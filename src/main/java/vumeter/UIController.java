package vumeter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class UIController implements ActionListener {

	private static UIController instance;
	private UI ui;
	private boolean serialToggled;
	private InetAddress address;
	private boolean listening;
	private static final int AUDIO_BUFFER_SIZE = 1024;
	private static final int UDP_PORT = 12345;
	private static boolean capturing = false;
	private TargetDataLine line;
	private BlockingQueue<Integer> audioQueue = new LinkedBlockingQueue<>();
	private DatagramSocket socket;
	private SerialPort serialPort = null;

	public UIController(UI ui) {
		this.ui = ui;
		attachListeners();
	}

	public static UIController getInstance(UI ui) {
		if (instance == null) {
			instance = new UIController(ui);
		}
		return instance;
	}

	private void attachListeners() {
		ui.cb_AudioDevice.addActionListener(this);
		ui.btn_Scan.addActionListener(this);
		ui.btn_Start.addActionListener(this);
		ui.btn_ConnectionType.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ui.cb_AudioDevice) {
			System.out.println(ui.cb_AudioDevice.getSelectedIndex());
		} else if (e.getSource() == ui.btn_Scan) {
			handleScanButton();
		} else if (e.getSource() == ui.btn_Start) {
			handleListening();
		} else if (e.getSource() == ui.btn_ConnectionType) {
			handleToggleConnection();
		}
	}

	private void handleListening() {
		String selectedDevice = (String) ui.cb_AudioDevice.getSelectedItem();
		Mixer.Info selectedMixerInfo = getMixerInfoByName(selectedDevice);

		if (!serialToggled) {
			if (!listening) {
				ui.btn_Start.setText("Stop");
				listening = true;

				// String selectedPort = (String) comPortComboBox.getSelectedItem();
				if (selectedDevice != null) {

					startAudioCapture(selectedMixerInfo);
				}
			} else {
				ui.btn_Start.setText("Start");
				listening = false;
				stopAudioCapture();
			}
		}

		else {
			if (!listening) {
				ui.btn_Start.setText("Stop");
				listening = true;
				startSerial();
				// String selectedPort = (String) comPortComboBox.getSelectedItem();
				if (selectedDevice != null) {

					startAudioCapture(selectedMixerInfo);
				}
			} else {
				ui.btn_Start.setText("Start");
				listening = false;
				stopAudioCapture();
				stopSerial();
			}
		}
	}

	private void startSerial() {
		String serialDevice = ui.cb_SerialDevice.getSelectedItem().toString();
		serialPort = new SerialPort(serialDevice);
		try {
			serialPort.openPort();
			System.out.println("Serial Open: " + serialPort);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			serialPort.setParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopSerial() {
		try {
			serialPort.closePort();
			System.out.println("Serial Closed: " + serialPort);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleScanButton() {
		if (!serialToggled) {
			scanForESP32UDP();
		} else {
			populateSerialDevices();
		}
	}

	private void handleToggleConnection() {
		if (!serialToggled) {
			System.out.println("we are using Serial connection");
			ui.btn_ConnectionType.setText("Serial");
			serialToggled = true;
		} else {
			System.out.println("we are using Network connection");
			ui.btn_ConnectionType.setText("Network");
			serialToggled = false;
		}
	}

	private void populateSerialDevices() {
		// Clear the existing items in the combo box
		ui.cb_SerialDevice.removeAllItems();

		// Get the list of available serial ports
		String[] portNames = SerialPortList.getPortNames();

		// Add each port name to the combo box
		for (String portName : portNames) {
			ui.cb_SerialDevice.addItem(portName);
		}
	}

	private Mixer.Info getMixerInfoByName(String name) {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo : mixerInfos) {
			if (mixerInfo.getName().equals(name)) {
				return mixerInfo;
			}
		}
		return null;
	}

	private void startAudioCapture(Mixer.Info selectedMixerInfo) {
		try {
			if (capturing) {
				System.out.println("Already capturing audio.");
				return;
			}

			Mixer mixer = AudioSystem.getMixer(selectedMixerInfo);
			Line.Info[] lineInfos = mixer.getTargetLineInfo();
			line = null;
			for (Line.Info lineInfo : lineInfos) {
				if (lineInfo.getLineClass().equals(TargetDataLine.class)) {
					line = (TargetDataLine) mixer.getLine(lineInfo);
					break;
				}
			}

			if (line == null) {
				System.err.println("Line not supported");
				return;
			}

			AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 41000, 8, 1, 1, 41000, false);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				System.err.println("Line not supported");
				return;
			}

			line.open(format, AUDIO_BUFFER_SIZE);
			line.start();
			capturing = true;

			System.out.println("Listening with Device: " + selectedMixerInfo.getName());

			SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
				@Override
				protected Void doInBackground() throws Exception {
					byte[] buffer = new byte[AUDIO_BUFFER_SIZE];
					int bytesRead;
					while (capturing) {
						bytesRead = line.read(buffer, 0, buffer.length);
						int audioInputValue = processAudioData(buffer, bytesRead);
						audioQueue.put(audioInputValue);
					}
					return null;
				}

				@Override
				protected void done() {
				}
			};

			worker.execute();

			new Thread(() -> {
				while (capturing) {
					try {
						int audioValue = audioQueue.take();

						if (!serialToggled) {
							socket = new DatagramSocket();

							address = InetAddress.getByName(ui.ipField.getText());
							sendAudioLevelOverUDP(socket, (byte) audioValue, address);
						} else {
							sendAudioLevelOverSerial((byte) audioValue);
						}

					} catch (Exception e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}
				}
			}).start();

		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	private static int processAudioData(byte[] audioData, int bytesRead) {
		long sum = 0;

		for (int i = 0; i < bytesRead - 1; i += 2) {
			short sample = (short) ((audioData[i + 1] << 8) | audioData[i]);
			sum += sample * sample;
		}

		double rms = Math.sqrt(sum / (bytesRead / 2));
		double scalingFactor = 768 / (double) Short.MAX_VALUE;

		return (int) (scalingFactor * rms);
	}

	private void sendAudioLevelOverSerial(byte audioLevel) {
		byte[] dataBytes = new byte[2];
		dataBytes[0] = (byte) (audioLevel & 0xFF);
		dataBytes[1] = (byte) ((audioLevel >> 8) & 0xFF);
		try {
			serialPort.writeBytes(dataBytes);
			System.out.println(audioLevel);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

	}

	private static void sendAudioLevelOverUDP(DatagramSocket socket, byte audioLevel, InetAddress receiverAddress)
			throws Exception {
		socket.send(new DatagramPacket(new byte[] { audioLevel }, 1, receiverAddress, UDP_PORT));
	}

	private void scanForESP32UDP() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);

			InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
			int esp32Port = 12345;

			byte[] requestData = "ScanRequest".getBytes();
			DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, broadcastAddress,
					esp32Port);

			socket.send(requestPacket);
			System.out.println("Broadcast scan request sent.");

			byte[] responseData = new byte[1024];
			DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);

			socket.setSoTimeout(1000);
			while (true) {
				socket.receive(responsePacket);
				String esp32IPAddress = responsePacket.getAddress().getHostAddress();
				ui.ipField.setText(esp32IPAddress);
				socket.close();
				break;
			}
		} catch (Exception e) {
			handleError("Error scanning for ESP32 devices: " + e.getMessage());
		} finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}
	}

	private void handleError(String message) {
		JOptionPane.showMessageDialog(ui, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void showInfoMessage(String message, String title) {
		JOptionPane.showMessageDialog(ui, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	private void stopAudioCapture() {
		capturing = false;
		if (line != null) {
			line.stop();
			line.close();
		}
	}
}
