package be.tarsos.dsp.example;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class AccurateTunerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 695816192920926375L;
	private Color defaultColor;
	private JLabel frequencyLabel;
	private JLabel tolleranceLabel;
	
	public AccurateTunerPanel() {
		super(new GridLayout(0, 1));
		Border border = (BorderFactory.createLineBorder(Color.decode("#abcdef")));
		this.setBorder(BorderFactory.createTitledBorder(border, "13. Accurate Tuner"));
		this.defaultColor = this.getBackground();
		this.frequencyLabel = new JLabel();
		this.frequencyLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		this.frequencyLabel.setFont(new Font("Serif", Font.BOLD, 72));
		this.tolleranceLabel = new JLabel();
		this.tolleranceLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		this.tolleranceLabel.setFont(new Font("Serif", Font.PLAIN, 36));
	}

	public void setTunerInPanel(double frequency, int tollerance)
			throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					// ignore failure to set default look en feel;
				}
				frequencyLabel.setText(String.format("%.1f", frequency));
				// VIOLIN CHORDS
				if (frequency < MusicNoteRecorder.sol3Frequency - tollerance) {
					// NOT A VIOLIN NOTE
					setBackground(defaultColor);
					tolleranceLabel.setText("-");
				}
				// Prima ottava
				else if (frequency <= MusicNoteRecorder.sol3Frequency + tollerance) {
					// SOL 4a corda
					setBackground(Color.decode("#00ff00"));
					tolleranceLabel.setText("SOL3 (" + String.format("%.1f", (frequency - MusicNoteRecorder.sol3Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.solD3Frequency + tollerance) {
					// SOL# 4a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL#3 (" + String.format("%.1f", (frequency - MusicNoteRecorder.solD3Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.la3Frequency + tollerance) {
					// LA 4a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA3 (" + String.format("%.1f", (frequency - MusicNoteRecorder.la3Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.laD3Frequency + tollerance) {
					// LA# 4a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA#3 (" + String.format("%.1f", (frequency - MusicNoteRecorder.laD3Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.si3Frequency + (tollerance + 1)) {
					// SI 4a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SI3 (" + String.format("%.1f", (frequency - MusicNoteRecorder.si3Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.do4Frequency + (tollerance + 1)) {
					// DO 4a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.do4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.doD4Frequency + (tollerance + 2)) {
					// DO# 4a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO#4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.doD4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.re4Frequency + (tollerance + 2)) {
					// RE 3a corda
					setBackground(Color.decode("#00ff00"));
					tolleranceLabel.setText("RE4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.re4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.reD4Frequency + (tollerance + 3)) {
					// RE# 3a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE#4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.reD4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.mi4Frequency + (tollerance + 3)) {
					// MI 3a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("MI4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.mi4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.fa4Frequency + (tollerance + 4)) {
					// FA 3a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.fa4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.faD4Frequency + (tollerance + 5)) {
					// FA# 3a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA#4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.faD4Frequency)) + ")");
				}

				// Seconda ottava
				else if (frequency <= MusicNoteRecorder.sol4Frequency + (tollerance + 5)) {
					// SOL 3a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.sol4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.solD4Frequency + (tollerance + 6)) {
					// SOL# 3a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL#4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.solD4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.la4Frequency + (tollerance + 7)) {
					// LA 2a corda
					setBackground(Color.decode("#00ff00"));
					tolleranceLabel.setText("LA4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.la4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.laD4Frequency + (tollerance + 8)) {
					// LA# 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA#4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.laD4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.si4Frequency + (tollerance + 8)) {
					// SI 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SI4 (" + String.format("%.1f", (frequency - MusicNoteRecorder.si4Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.do5Frequency + (tollerance + 9)) {
					// DO 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.do5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.doD5Frequency + (tollerance + 10)) {
					// DO# 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO#5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.doD5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.re5Frequency + (tollerance + 11)) {
					// RE 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.re5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.reD5Frequency + (tollerance + 12)) {
					// RE# 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE#5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.reD5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.mi5Frequency + (tollerance + 13)) {
					// MI 1a corda
					setBackground(Color.decode("#00ff00"));
					tolleranceLabel.setText("MI5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.mi5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.fa5Frequency + (tollerance + 15)) {
					// FA 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.fa5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.faD5Frequency + (tollerance + 16)) {
					// FA# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA#5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.faD5Frequency)) + ")");
				}

				// Terza ottava
				else if (frequency <= MusicNoteRecorder.sol5Frequency + (tollerance + 17)) {
					// SOL 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.sol5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.solD5Frequency + (tollerance + 18)) {
					// SOL# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL#5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.solD5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.la5Frequency + (tollerance + 20)) {
					// LA 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.la5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.laD5Frequency + (tollerance + 22)) {
					// LA# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA#5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.laD5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.si5Frequency + (tollerance + 28)) {
					// SI 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SI5 (" + String.format("%.1f", (frequency - MusicNoteRecorder.si5Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.do6Frequency + (tollerance + 30)) {
					// DO 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.do6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.doD6Frequency + (tollerance + 32)) {
					// DO# 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO#6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.doD6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.re6Frequency + (tollerance + 34)) {
					// RE 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.re6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.reD6Frequency + (tollerance + 36)) {
					// RE# 2a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE#6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.reD6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.mi6Frequency + (tollerance + 38)) {
					// MI 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("MI6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.mi6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.fa6Frequency + (tollerance + 40)) {
					// FA 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.fa6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.faD6Frequency + (tollerance + 43)) {
					// FA# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA#6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.faD6Frequency)) + ")");
				}

				// Quarta ottava
				else if (frequency <= MusicNoteRecorder.sol6Frequency + (tollerance + 45)) {
					// SOL 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.sol6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.solD6Frequency + (tollerance + 48)) {
					// SOL# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL#6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.solD6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.la6Frequency + (tollerance + 51)) {
					// LA 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.la6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.laD6Frequency + (tollerance + 54)) {
					// LA# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA#6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.laD6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.si6Frequency + (tollerance + 57)) {
					// SI 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SI6 (" + String.format("%.1f", (frequency - MusicNoteRecorder.si6Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.do7Frequency + (tollerance + 61)) {
					// DO 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.do7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.doD7Frequency + (tollerance + 65)) {
					// DO# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO#7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.doD7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.re7Frequency + (tollerance + 69)) {
					// RE 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.re7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.reD7Frequency + (tollerance + 73)) {
					// RE# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("RE#7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.reD7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.mi7Frequency + (tollerance + 77)) {
					// MI 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("MI7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.mi7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.fa7Frequency + (tollerance + 82)) {
					// FA 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.fa7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.faD7Frequency + (tollerance + 87)) {
					// FA# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("FA#7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.faD7Frequency)) + ")");
				}

				// Forse
				else if (frequency <= MusicNoteRecorder.sol7Frequency + (tollerance + 92)) {
					// SOL 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.sol7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.solD7Frequency + (tollerance + 98)) {
					// SOL# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SOL# 7(" + String.format("%.1f", (frequency - MusicNoteRecorder.solD7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.la7Frequency + (tollerance + 103)) {
					// LA 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.la7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.laD7Frequency + (tollerance + 110)) {
					// LA# 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("LA#7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.laD7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.si7Frequency + (tollerance + 116)) {
					// SI 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("SI7 (" + String.format("%.1f", (frequency - MusicNoteRecorder.si7Frequency)) + ")");
				} else if (frequency <= MusicNoteRecorder.do8Frequency + (tollerance + 122)) {
					// DO 1a corda
					setBackground(defaultColor);
					tolleranceLabel.setText("DO8 (" + String.format("%.1f", (frequency - MusicNoteRecorder.do8Frequency)) + ")");
				} else {
					// NOT A VIOLIN NOTE
					setBackground(defaultColor);
					tolleranceLabel.setText("-");
				}

				add(frequencyLabel);
				add(tolleranceLabel);
			}
		});
	}
}
