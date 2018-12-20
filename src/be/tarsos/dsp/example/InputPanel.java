/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package be.tarsos.dsp.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class InputPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Mixer mixer = null;

	public InputPanel() {
		super(new BorderLayout());
		Border border = (BorderFactory.createLineBorder(Color.decode("#abcdef")));
		this.setBorder(BorderFactory.createTitledBorder(border, "1. Choose a microphone input"));
		this.setMaximumSize(new Dimension(100, 300));
		this.setPreferredSize(new Dimension(100, 300));

		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		ButtonGroup group = new ButtonGroup();
		final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
		final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);

		for (Mixer.Info info : Shared.getMixerInfo(false, true)) {
			JRadioButton button = new JRadioButton();
			button.setText(Shared.toLocalString(info));
			buttonPanel.add(button);
			group.add(button);

			// Se la linea di ingresso è supportata
			if (!AudioSystem.getMixer(info).isLineSupported(dataLineInfo)) {
				button.setEnabled(false);
			}

			button.setActionCommand(info.toString());
			button.addActionListener(setInput);
		}
		this.add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
	}

	private ActionListener setInput = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			for (Mixer.Info info : Shared.getMixerInfo(false, true)) {
				if (arg0.getActionCommand().equals(info.toString())) {
					Mixer newValue = AudioSystem.getMixer(info);
					InputPanel.this.firePropertyChange("mixer", mixer, newValue);
					InputPanel.this.mixer = newValue;
					break;
				}
			}
		}
	};

}
