package be.tarsos.dsp.example;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StopTuneFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6775320089273805381L;

	/** JFRAME TIMER START HEAR **/
	public StopTuneFrame(int perfetto, int ottimo, int distinto, int buono, int discreto, int sufficiente,
			int appenaSufficiente, int nonSufficiente, long time) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(0, 1));
		setTitle("Result");
		setPreferredSize(new Dimension(900, 600));
		setMinimumSize(new Dimension(900, 600));
		setMaximumSize(new Dimension(900, 600));
		setResizable(false);
		setVisible(true);

		JPanel resultPanel = new JPanel(new GridLayout(0, 1));

		JLabel resultLabel0 = new JLabel();
		resultLabel0.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel0.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel1 = new JLabel();
		resultLabel1.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel1.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel2 = new JLabel();
		resultLabel2.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel2.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel3 = new JLabel();
		resultLabel3.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel3.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel4 = new JLabel();
		resultLabel4.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel4.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel5 = new JLabel();
		resultLabel5.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel5.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel6 = new JLabel();
		resultLabel6.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel6.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel7 = new JLabel();
		resultLabel7.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel7.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel8 = new JLabel();
		resultLabel8.setFont(new Font("Serif", Font.PLAIN, 30));
		resultLabel8.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		JLabel resultLabel9 = new JLabel();
		resultLabel9.setFont(new Font("Serif", Font.PLAIN, 36));
		resultLabel9.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		
		int ore = 0, min = 0, sec = 0;
		ore = (int) (time / 3600);
		min = (int) ((time - (3600 * ore))/60);
		sec = (int) (time - (3600 * ore) - (60 * min));

		if (ore < 10 && min < 10 && sec < 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + "0" + ore + ":0" + min + ":0" + sec);
		} else if (ore < 10 && min < 10 && sec >= 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + "0" + ore + ":0" + min + ":" + sec);
		} else if (ore < 10 && min >= 10 && sec < 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + "0" + ore + ":" + min + ":0" + sec);
		} else if (ore >= 10 && min < 10 && sec < 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + ore + ":0" + min + ":0" + sec);
		} else if (ore < 10 && min >= 10 && sec >= 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + "0" + ore + ":" + min + ":" + sec);
		} else if (ore >= 10 && min < 10 && sec >= 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + ore + ":0" + min + ":" + sec);
		} else if (ore >= 10 && min >= 10 && sec < 10) {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + ore + ":" + min + ":0" + sec);
		} else {
			resultLabel0.setText("TEMPO DI ESECUZIONE: " + ore + ":" + min + ":" + sec);
		}
		resultPanel.add(resultLabel0);

		resultLabel1.setText("PERFETTO = " + perfetto);
		resultPanel.add(resultLabel1);
		resultLabel2.setText("OTTIMO = " + ottimo);
		resultPanel.add(resultLabel2);
		resultLabel3.setText("DISTINTO = " + distinto);
		resultPanel.add(resultLabel3);
		resultLabel4.setText("BUONO = " + buono);
		resultPanel.add(resultLabel4);
		resultLabel5.setText("DISCRETO = " + discreto);
		resultPanel.add(resultLabel5);
		resultLabel6.setText("SUFFICIENTE = " + sufficiente);
		resultPanel.add(resultLabel6);
		resultLabel7.setText("APPENA SUFFICIENTE = " + appenaSufficiente);
		resultPanel.add(resultLabel7);
		resultLabel8.setText("NON SUFFICIENTE = " + nonSufficiente);
		resultPanel.add(resultLabel8);

		float averageMark = (float) (((perfetto * 10.05) + (ottimo * 10) + (distinto * 9) + (buono * 8) + (discreto * 7)
				+ (sufficiente * 6) + (appenaSufficiente * 5) + (nonSufficiente * 4))
				/ (perfetto + ottimo + distinto + buono + discreto + sufficiente + appenaSufficiente + nonSufficiente));
		if (Float.isNaN(averageMark)) {
			resultLabel9.setText("Non è stata eseguita alcuna nota");
		} else if (Float.compare(averageMark, 10) < 0) {
			resultLabel9.setText(String.format("VOTO = %.1f", averageMark));
		} else {
			resultLabel9.setText("VOTO = 10L");
		}
		resultPanel.add(resultLabel9);

		add(resultPanel);
	}
}
