package vumeter;

import java.awt.Font;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class UI extends JPanel {

	JComboBox<String> cb_AudioDevice;
	JComboBox<String> cb_SerialDevice;
	JTextField ipField;
	JButton btn_Scan;
	JButton btn_Start;
	JButton btn_ConnectionType;

	public UI() {

		init();
		populateAudioInputDevices();
	}

	private void init() {
		setLayout(new GridLayout(3, 0, 0, 0));

		JPanel audioDevicePanel = new JPanel();
		add(audioDevicePanel);

		JLabel lb_AudioDevice = new JLabel("Audio Device:");
		lb_AudioDevice.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lb_AudioDevice.setHorizontalAlignment(SwingConstants.CENTER);
		audioDevicePanel.add(lb_AudioDevice);

		cb_AudioDevice = new JComboBox<String>();
		cb_AudioDevice.setFont(new Font("Tahoma", Font.PLAIN, 12));
		audioDevicePanel.add(cb_AudioDevice);

		JPanel ConnectionsPanel = new JPanel();
		add(ConnectionsPanel);

		JLabel lb_IP = new JLabel("IP:");
		lb_IP.setHorizontalAlignment(SwingConstants.CENTER);
		lb_IP.setFont(new Font("Tahoma", Font.PLAIN, 12));
		ConnectionsPanel.add(lb_IP);

		ipField = new JTextField();
		ipField.setHorizontalAlignment(SwingConstants.CENTER);
		ipField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		ConnectionsPanel.add(ipField);
		ipField.setColumns(10);

		JLabel lb_IP_1 = new JLabel("Serial:");
		lb_IP_1.setHorizontalAlignment(SwingConstants.CENTER);
		lb_IP_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		ConnectionsPanel.add(lb_IP_1);

		cb_SerialDevice = new JComboBox<String>();
		cb_SerialDevice.setFont(new Font("Tahoma", Font.PLAIN, 12));
		ConnectionsPanel.add(cb_SerialDevice);

		JPanel buttonsPanel = new JPanel();
		add(buttonsPanel);

		btn_Scan = new JButton("Scan");
		buttonsPanel.add(btn_Scan);

		btn_Start = new JButton("Start");
		buttonsPanel.add(btn_Start);

		btn_ConnectionType = new JButton("Network");
		buttonsPanel.add(btn_ConnectionType);
	}

	private void populateAudioInputDevices() {

		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo : mixerInfos) {
			Mixer mixer = AudioSystem.getMixer(mixerInfo);
			Line.Info[] lineInfos = mixer.getTargetLineInfo();
			for (Line.Info lineInfo : lineInfos) {
				if (lineInfo.getLineClass().equals(TargetDataLine.class)) {
					cb_AudioDevice.addItem(mixerInfo.getName());
					break;
				}
			}
		}

	}
}