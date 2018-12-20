package be.tarsos.dsp.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

public class StringsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4031063011944228258L;

	enum Strings {
		Synthetic, Steel, Gut
	};

	public StringsPanel(ActionListener algoChangedListener) {
		super(new GridLayout(0, 1));
		this.setPreferredSize(new Dimension(100, 200));
		this.setMaximumSize(new Dimension(100, 200));
		Border border = (BorderFactory.createLineBorder(Color.decode("#abcdef")));
		this.setBorder(BorderFactory.createTitledBorder(border, "3. Choose your string material"));
		ButtonGroup group = new ButtonGroup();
		for (Strings value : Strings.values()) {
			JRadioButton button = new JRadioButton();
			button.setText(value.toString());
			add(button);
			group.add(button);
			if (value == Strings.Gut) {
				button.setEnabled(false);
			}
			button.setActionCommand(value.name());
			button.addActionListener(algoChangedListener);
		}
	}
}
