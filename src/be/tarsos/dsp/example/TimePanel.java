package be.tarsos.dsp.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class TimePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5991216404665204325L;
	private String[] timeStrings = { "1/4", "2/4", "3/4", "4/4"};
	private JComboBox<String> comboBox = new JComboBox<String>(timeStrings);
	private double tempo = 1;
	
	public TimePanel() {
		super(new BorderLayout());
		Border border = (BorderFactory.createLineBorder(Color.decode("#abcdef")));
		this.setBorder(BorderFactory.createTitledBorder(border,"7. Choose a time"));
		super.add(comboBox);
		comboBox.setSelectedIndex(3);
		comboBox.addActionListener(cbActionListener);
		
		this.setMaximumSize(new Dimension(100, 200));
		this.setPreferredSize(new Dimension(100, 200));
	}

	private ActionListener cbActionListener = new ActionListener() {//add actionlistner to listen for change
         @Override
         public void actionPerformed(ActionEvent e) {

             String s = (String) comboBox.getSelectedItem();//get the selected item
             double newTempo = 1;
             switch (s) {//check for a match
                 case "1/4":
                     comboBox.setSelectedIndex(0);
                     newTempo = (double) 1 / (double) 4;
                     break;
                 case "2/4":
                     comboBox.setSelectedIndex(1);
                     newTempo = (double) 2 / (double) 4;
                     break;
                 case "3/4":
                     comboBox.setSelectedIndex(2);
                     newTempo = (double) 3 / (double) 4;
                     break;
                 case "4/4":
                     comboBox.setSelectedIndex(3);
                     newTempo = (double) 4 / (double) 4;
                     break;
                 default:
                     comboBox.setSelectedIndex(3);
                     newTempo = (double) 4 / (double) 4;
                     break;
             }
             TimePanel.this.firePropertyChange("tempo", tempo, newTempo);
             tempo = newTempo;
         }
     };

	public double getTempo() {
		return tempo;
	} 
}
