package vumeter;

import com.formdev.flatlaf.FlatDarkLaf;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

@SuppressWarnings("serial")
public class VuMeter extends JFrame {

	private UI contentPane = new UI();

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				VuMeter frame = new VuMeter();
				frame.setVisible(true);
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		});
	}

	public VuMeter() {
		setTitle("VuMeter");
		setSize(345, 175);
		// setIconImage(new ImageIcon(getClass().getResource("/logo.png")).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		getContentPane().add(contentPane);

		new UIController(contentPane);
	}
}