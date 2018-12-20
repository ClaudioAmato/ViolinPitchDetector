package be.tarsos.dsp.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.Oscilloscope;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.example.StringsPanel.Strings;
import be.tarsos.dsp.Oscilloscope.OscilloscopeEventHandler;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.util.fft.FFT;

public class MusicNoteRecorder extends JFrame
		implements PitchDetectionHandler, OscilloscopeEventHandler, AudioProcessor {

	private static final long serialVersionUID = 385370780791348150L;
	private static int i = 1, j = 1, k = 1;
	private String oldNote = "", messageNote = "", noteOctave = "", olddB = "";
	private float averageRange = 0, previusOctaveRange = 0, precision, counterPause = 0, defaultRMS, autoRMS,
			autoPrecision, averagedB = 0, previusOctavedB = 0;
	private int sameNote = 0, preSetCaret = 0, minValidNote = 4, n_precision = 0, n_rms = 0;
	private long startTimeNextNote = 0, startTimePause = 0, silenceTime = 0, startExecution = 0, stopExecution = 0;
	private double tempo = 1.0, speed = 60.0, bpm = 60.0;
	private boolean firstPause = true, startHear = false, messageShow = false, precisionAlert = false,
			startAccurateTuner = false, startAutomaticProbability = false;
	private int perfetto = 0, ottimo = 0, distinto = 0, buono = 0, discreto = 0, sufficiente = 0, appenaSufficiente = 0,
			nonSufficiente = 0;

	static final double sol3Frequency = 196, solD3Frequency = 207.7, la3Frequency = 220, laD3Frequency = 233.1,
			si3Frequency = 246.9, do4Frequency = 261.6, doD4Frequency = 277.2, re4Frequency = 293.7,
			reD4Frequency = 311.1, mi4Frequency = 329.6, fa4Frequency = 349.2, faD4Frequency = 370, sol4Frequency = 392,
			solD4Frequency = 415.3, la4Frequency = 440, laD4Frequency = 466.2, si4Frequency = 493.9,
			do5Frequency = 523.3, doD5Frequency = 554.4, re5Frequency = 587.3, reD5Frequency = 622.3,
			mi5Frequency = 659.3, fa5Frequency = 698.5, faD5Frequency = 740, sol5Frequency = 784,
			solD5Frequency = 830.6, la5Frequency = 880, laD5Frequency = 932.3, si5Frequency = 987.8,
			do6Frequency = 1047, doD6Frequency = 1109, re6Frequency = 1175, reD6Frequency = 1245, mi6Frequency = 1319,
			fa6Frequency = 1397, faD6Frequency = 1480, sol6Frequency = 1568, solD6Frequency = 1661, la6Frequency = 1760,
			laD6Frequency = 1865, si6Frequency = 1976, do7Frequency = 2093, doD7Frequency = 2217, re7Frequency = 2349,
			reD7Frequency = 2489, mi7Frequency = 2637, fa7Frequency = 2794, faD7Frequency = 2960, sol7Frequency = 3136,
			solD7Frequency = 3322, la7Frequency = 3520, laD7Frequency = 3729, si7Frequency = 3951, do8Frequency = 4186;

	private static JFrame frame;
	private JFrame textAreaFrame;
	private JTextArea textAreaNote;
	private JLabel speedLabel, thresholdLabel, precisionLabel, rmsLabel;
	private JSlider thresholdSlider, precisionSlider;
	private JPanel containerAllPanel;
	private JButton startTune, stopTune, accurateTuner, buttonDefaultProbability;

	private float sampleRate = 44100;
	private int bufferSize = 1024 * 4;
	private int overlap = 768 * 4;
	private AudioDispatcher dispatcher;
	private Mixer currentMixer;
	private double threshold;
	private final SoundDetectorPanel soundPanel;
	private SilenceDetector silenceDetector;

	private double pitch;
	private final SpectrogramPanel spectogramPanel;
	private String fileName;
	private final GraphPanel graphPanel;
	private AccurateTunerPanel tunerPanel;

	/** ALGORITMI DI PITCH **/
	private PitchEstimationAlgorithm algo;
	private ActionListener algoChangeListener = new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent e) {
			String name = e.getActionCommand();
			PitchEstimationAlgorithm newAlgo = PitchEstimationAlgorithm.valueOf(name);
			algo = newAlgo;
			try {
				setNewMixer(currentMixer);
			} catch (LineUnavailableException e1) {
				e1.printStackTrace();
			} catch (UnsupportedAudioFileException e1) {
				e1.printStackTrace();
			}
		}
	};

	/** SET MIXER **/
	private void setNewMixer(Mixer mixer) throws LineUnavailableException, UnsupportedAudioFileException {
		if (dispatcher != null) {
			dispatcher.stop();
		}
		if (fileName == null) {
			final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
			final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
			TargetDataLine line;
			line = (TargetDataLine) mixer.getLine(dataLineInfo);
			final int numberOfSamples = bufferSize;
			line.open(format, numberOfSamples);
			line.start();
			final AudioInputStream stream = new AudioInputStream(line);
			JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
			// create a new dispatcher
			dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);
		} else {
			try {
				File audioFile = new File(fileName);
				dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
				AudioFormat format = AudioSystem.getAudioFileFormat(audioFile).getFormat();
				dispatcher.addAudioProcessor(new AudioPlayer(format));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		currentMixer = mixer;

		// add a processor, handle pitch event.
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
		// add a processor, handle percussion event.
		dispatcher.addAudioProcessor(new Oscilloscope(this));
		// add a processor, handle percussion event.
		silenceDetector = new SilenceDetector(threshold, false);
		dispatcher.addAudioProcessor(silenceDetector);
		// add a processor, handle spectogram event.
		dispatcher.addAudioProcessor(fftProcessor);

		dispatcher.addAudioProcessor(this);

		// run the dispatcher (on a new thread).
		new Thread(dispatcher, "Audio dispatching").start();
	}

	/** SET PARAMETRI DEFAULT PER IL MATERIALE DELLE CORDE **/
	private Strings chord;
	private ActionListener StringChangeListener = new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent e) {
			String name = e.getActionCommand();
			Strings newStrings = Strings.valueOf(name);
			chord = newStrings;
			switch (chord) {
			case Steel:
				defaultRMS = 2f;
				precisionSlider.setValue(95);
				break;
			case Gut:
				defaultRMS = 4f;
				precisionSlider.setValue(98);
				break;
			default:
				defaultRMS = 3f;
				precisionSlider.setValue(97);
				break;
			}
			buttonDefaultProbability.setEnabled(true);
			rmsLabel.setText("RMS: " + String.format("%.2f", defaultRMS));
			precisionSlider.setEnabled(true);
		}
	};

	/** JSLIDER PER IL RILEVATORE DI SUONO (SOUND DETECTOR) **/
	private JSlider initialzeThresholdSlider() {
		JSlider thresholdSlider = new JSlider(-120, 0);
		thresholdSlider.setValue((int) threshold);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setMajorTickSpacing(10);
		thresholdSlider.setMinorTickSpacing(5);
		thresholdSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				threshold = source.getValue();
				soundPanel.setThresholdLevel(threshold);
				thresholdLabel.setText("Threshold: " + String.valueOf(threshold));
				if (!source.getValueIsAdjusting()) {
					try {
						setNewMixer(currentMixer);
					} catch (LineUnavailableException e1) {
						e1.printStackTrace();
					} catch (UnsupportedAudioFileException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		return thresholdSlider;
	}

	/** JSLIDER PER IL "METRONOMO" **/
	private JSlider initialzeSpeedSlider() {
		JSlider speedSlider = new JSlider(30, 200);
		speedSlider.setValue((int) speed);
		speedSlider.setPaintLabels(true);
		speedSlider.setPaintTicks(true);
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setMinorTickSpacing(5);
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				speed = source.getValue();
				speedLabel.setText("Velocità: " + String.valueOf(speed) + " bpm");
				if ((int) speed < 45) {
					minValidNote = 5;
				} else if ((int) speed < 60) {
					minValidNote = 4;
				} else if ((int) speed < 80) {
					minValidNote = 3;
				} else if ((int) speed < 100) {
					minValidNote = 2;
				} else if ((int) speed < 120) {
					minValidNote = 1;
				} else {
					minValidNote = 0;
				}
			}
		});
		return speedSlider;
	}

	/** JSLIDER PER LA PROBABILITA' DELLA PRECISIONE **/
	private JSlider initialzePrecisionSlider() {
		precisionSlider = new JSlider(84, 99);
		precisionSlider.setValue((int) (precision * 100));
		precisionSlider.setPaintLabels(true);
		precisionSlider.setPaintTicks(true);
		precisionSlider.setMajorTickSpacing(5);
		precisionSlider.setMinorTickSpacing(1);
		precisionSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!precisionAlert && precisionSlider.isEnabled()) {
					precisionAlert = true;
					JOptionPane.showMessageDialog(null,
							"Aumentare la probabilità di riconoscimento delle note comporta il dover suonare note sempre più \"pulite\".\n"
									+ "Diminuire la probabilità invece comporta la riduzione della precisione, "
									+ "quindi può capitare che qualche disturbo venga catturato come una nota.\n"
									+ "Si consiglia dunque di lasciare il valore di default.",
							"ATTENZIONE!", JOptionPane.WARNING_MESSAGE);
				}
				JSlider source = (JSlider) e.getSource();
				precision = (float) source.getValue() / 100f;
				precisionLabel.setText("Precisione: " + String.valueOf(source.getValue()) + "%");
			}
		});
		return precisionSlider;
	}

	/** JFRAME TIMER THRESHOLD **/
	public void jTimerThreshold(int timeSec) {
		Timer timer;
		JLabel timeLabel;
		JFrame timeFrame;

		timeFrame = new JFrame();
		timeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		timeFrame.setLayout(new BorderLayout());
		timeFrame.setTitle("Timer Threshold");
		timeFrame.setMinimumSize(new Dimension(900, 600));
		timeFrame.setResizable(false);
		timeFrame.setVisible(true);

		JPanel timePanel = new JPanel(new BorderLayout());
		timePanel.setPreferredSize(new Dimension(900, 600));
		timePanel.setMinimumSize(new Dimension(900, 600));
		timePanel.setMaximumSize(new Dimension(900, 600));

		timeLabel = new JLabel();
		timeLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		timeLabel.setFont(new Font("Serif", Font.BOLD, 72));

		class App extends TimerTask {
			int countdown = timeSec + 4;
			double maxSilence = 0, minSilence = -120, averageSilence = 0;
			double maxSound = -120, minSound = 0, averageSound = 0;
			double detectedSilence = 0;
			boolean secondControl = false, firstControl = false;

			public void run() {
				frame.setEnabled(false);
				if (countdown == -1) {
					firstControl = true;
					if (secondControl) {
						cancel();
						frame.setEnabled(true);
						if (averageSilence >= averageSound) {
							JOptionPane.showConfirmDialog(frame, "C'è troppo rumore", "OOPS!",
									JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
							threshold = 0;
							thresholdSlider.setValue((int) threshold);
						} else {
							if ((averageSound / averageSilence) >= 0.75) {
								JOptionPane.showConfirmDialog(frame, "Potrebbe esserci del rumore di sottofondo",
										"ATTENZIONE!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
								threshold = ((((maxSound + minSilence) / 2) + (averageSound / timeSec)) / 2);
							} else {
								JOptionPane.showConfirmDialog(frame, "Threshold automatico riuscito!", "COMPLETATO!",
										JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
								threshold = ((((minSound + maxSilence) / 2) + (averageSound / timeSec)) / 2);
							}
							if (threshold < -120) {
								threshold = -120;
							}
							thresholdSlider.setValue((int) threshold);
						}
						timeFrame.dispose();
					} else {
						countdown = timeSec + 4;
					}
				} else if (countdown <= timeSec) {
					detectedSilence = silenceDetector.currentSPL();
					if (!secondControl) {
						timeLabel.setText("Restare in silenzio per " + countdown);
						if (detectedSilence < maxSilence) {
							maxSilence = detectedSilence;
						}
						if (detectedSilence > minSilence) {
							minSilence = detectedSilence;
						}
						averageSilence += detectedSilence;
					} else {
						timeLabel.setText("Suonare il brano per " + countdown);
						if (detectedSilence < minSound) {
							minSound = detectedSilence;
						}
						if (detectedSilence > maxSound) {
							maxSound = detectedSilence;
						}
						averageSound += detectedSilence;
					}
					--countdown;
				} else {
					if (!firstControl) {
						timeLabel.setText("Restare in silenzio fra: " + (countdown - (timeSec + 1)));
					} else {
						secondControl = true;
						timeLabel.setText("Suonare un brano tra: " + (countdown - (timeSec + 1)));
					}
					--countdown;
				}
			}
		}

		timer = new Timer();
		timer.schedule(new App(), 0, 1000);

		timePanel.add(timeLabel);
		timeFrame.add(timePanel);

		timeFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Sei sicuro di voler chiudere la finestra?", "Vuoi chiudere?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					if (timeFrame.isVisible()) {
						frame.setEnabled(true);
						timer.cancel();
						timeFrame.dispose();
					}
				}
			}
		});
	}

	/** JFRAME TIMER PRECISION **/
	public void jTimerProbability(int timeSec) {
		Timer timer;
		JLabel timeLabel;
		JFrame timeFrame;

		timeFrame = new JFrame();
		timeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		timeFrame.setLayout(new BorderLayout());
		timeFrame.setTitle("Timer Probability");
		timeFrame.setMinimumSize(new Dimension(900, 600));
		timeFrame.setResizable(false);
		timeFrame.setVisible(true);

		JPanel timePanel = new JPanel(new BorderLayout());
		timePanel.setPreferredSize(new Dimension(900, 600));
		timePanel.setMinimumSize(new Dimension(900, 600));
		timePanel.setMaximumSize(new Dimension(900, 600));

		timeLabel = new JLabel();
		timeLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		timeLabel.setFont(new Font("Serif", Font.BOLD, 72));

		class App extends TimerTask {
			int countdown = timeSec + 4;

			public void run() {
				frame.setEnabled(false);
				if (countdown == -1) {
					startAutomaticProbability = false;
					cancel();
					frame.setEnabled(true);
					if (n_precision != 0 && n_rms != 0) {
						autoPrecision = ((autoPrecision / n_precision) + precision) / 2;
						autoRMS = ((autoRMS / n_rms) + defaultRMS) / 2;
						if (autoPrecision < 0.84f || autoPrecision >= 1) {
							autoPrecision = precision;
						}
						if (autoRMS < 1) {
							autoRMS = defaultRMS;
						}
						precision = autoPrecision;
						defaultRMS = autoRMS;
						precisionSlider.setValue((int) (precision * 100));
					} else if (n_precision == 0 || n_rms == 0) {
						JOptionPane.showMessageDialog(null, "Non è stato rilevato alcun suono accettabile",
								"ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
					}
					timeFrame.dispose();
					JOptionPane.showConfirmDialog(frame,
							"E' importante cliccare sull'apposito pulsante per riprsistinare tutti i valori di default",
							"ATTENZIONE!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
					precisionLabel.setText("Precisione: " + String.valueOf((int) (precision * 100)) + "%");
					rmsLabel.setText("RMS: " + String.format("%.2f", defaultRMS));
				} else if (countdown <= timeSec) {
					startAutomaticProbability = true;
					timeLabel.setText("Suonare il brano per " + countdown);
					--countdown;
				} else {
					timeLabel.setText("Suonare un brano tra: " + (countdown - (timeSec + 1)));
					--countdown;
				}
			}
		}

		timer = new Timer();
		timer.schedule(new App(), 0, 1000);

		timePanel.add(timeLabel);
		timeFrame.add(timePanel);

		timeFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Sei sicuro di voler chiudere la finestra?", "Vuoi chiudere?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					if (timeFrame.isVisible()) {
						frame.setEnabled(true);
						timer.cancel();
						timeFrame.dispose();
						startAutomaticProbability = false;
					}
				}
			}
		});
	}

	/** JFRAME CONTO ALLA ROVESCIA INIZIO ASCOLTO **/
	public void jTimerStartTune(int timeSec) {
		Timer timer;
		JLabel timeLabel;
		JFrame timeFrame;

		timeFrame = new JFrame();
		timeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		timeFrame.setLayout(new BorderLayout());
		timeFrame.setTitle("Ready?");
		timeFrame.setMinimumSize(new Dimension(900, 600));
		timeFrame.setResizable(false);
		timeFrame.setLocationRelativeTo(null);
		timeFrame.setVisible(true);

		JPanel timePanel = new JPanel(new BorderLayout());
		timePanel.setPreferredSize(new Dimension(900, 600));
		timePanel.setMinimumSize(new Dimension(900, 600));
		timePanel.setMaximumSize(new Dimension(900, 600));

		timeLabel = new JLabel();
		timeLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		timeLabel.setFont(new Font("Serif", Font.BOLD, 72));

		class App extends TimerTask {
			int countdown = timeSec;

			public void run() {
				frame.setEnabled(false);
				textAreaFrame = new JFrame("NOTE");

				if (countdown <= -1) {
					cancel();
					perfetto = ottimo = distinto = buono = discreto = sufficiente = appenaSufficiente = nonSufficiente = 0;
					startTimeNextNote = startTimePause = silenceTime = stopExecution = 0;
					startExecution = System.currentTimeMillis();
					oldNote = messageNote = noteOctave = olddB = "";
					averageRange = counterPause = previusOctaveRange = averagedB = previusOctavedB = 0;
					firstPause = true;
					i = j = k = 1;
					sameNote = preSetCaret = 0;
					messageShow = false;
					validate();
					startHear = true;
					textAreaFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
					textAreaFrame.setMinimumSize(new Dimension(600, 500));
					textAreaFrame.setSize(new Dimension(974, 1047));
					textAreaNote = new JTextArea();
					textAreaNote.setEditable(false);
					try {
						Font MECHANI = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("matryoshka.ttf"))
								.deriveFont(40f);
						textAreaNote.setFont(MECHANI);
					} catch (FontFormatException | IOException ex) {
						System.err.println("Exception loading fonts " + ex);
					}
					textAreaFrame.add(new JScrollPane(textAreaNote));
					textAreaFrame.add(stopTune, BorderLayout.SOUTH);
					textAreaFrame.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(java.awt.event.WindowEvent windowEvent) {
							if (JOptionPane.showConfirmDialog(textAreaFrame,
									"Sei sicuro di voler chiudere la finestra?", "Vuoi chiudere?",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
								if (stopTune.isEnabled()) {
									stopTune.doClick();
								}
								startTune.setEnabled(true);
								textAreaFrame.dispose();
							}
						}
					});
					textAreaFrame.setVisible(true);
					timeFrame.dispose();
				} else {
					timeLabel.setText("Si comincia fra: " + countdown);
					--countdown;
				}
			}
		}

		timer = new Timer();
		timer.schedule(new App(), 0, 1000);

		timePanel.add(timeLabel);
		timeFrame.add(timePanel);

		timeFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Sei sicuro di voler chiudere la finestra?", "Vuoi chiudere?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					if (timeFrame.isVisible()) {
						startHear = false;
						frame.setEnabled(true);
						timer.cancel();
						timeFrame.dispose();
						stopTune.setEnabled(false);
						startTune.setEnabled(true);
						accurateTuner.setEnabled(true);
					}
				}
			}
		});
	}

	/** COSTRUTTORE **/
	public MusicNoteRecorder(String fileName) {
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Pitch Detector");

		this.threshold = -90;
		this.precision = 0.96f;
		this.defaultRMS = 5f;
		this.speed = (double) 60;
		this.algo = PitchEstimationAlgorithm.MPM;

		Border border = (BorderFactory.createLineBorder(Color.decode("#abcdef")));
		this.fileName = fileName;

		JPanel containerPanel1 = new JPanel(new GridLayout(0, 1));
		containerPanel1.setPreferredSize(new Dimension(200, 900));
		JPanel containerPanel2 = new JPanel(new GridLayout(0, 1));
		containerPanel2.setPreferredSize(new Dimension(200, 900));
		JPanel containerPanel3 = new JPanel(new GridLayout(0, 1));
		containerPanel3.setPreferredSize(new Dimension(500, 900));
		containerAllPanel = new JPanel(new GridLayout(1, 0));

		JPanel pitchDetectionPanel = new PitchDetectionPanel(algoChangeListener);
		JPanel stringsPanel = new StringsPanel(StringChangeListener);

		JPanel sliderPanelThreshold = new JPanel(new BorderLayout());
		JPanel sliderPanelSpeed = new JPanel(new BorderLayout());
		JPanel sliderPanelPrecision1 = new JPanel(new GridLayout(1, 2));
		JPanel sliderPanelPrecision2 = new JPanel(new BorderLayout());
		JPanel sliderPanelPrecision3 = new JPanel(new BorderLayout());
		JPanel oscilloscopeContainer = new JPanel(new BorderLayout());
		JPanel spectogramContainer = new JPanel(new BorderLayout());
		JPanel decibelContainer = new JPanel(new BorderLayout());
		JPanel startContainer = new JPanel(new GridLayout(0, 1));
		JPanel accurateContainer = new JPanel(new GridLayout(0, 1));

		JButton buttonAutoThreshold = new JButton("Auto set threshold");
		JButton buttonAutoProbability = new JButton("Auto set probability");
		buttonDefaultProbability = new JButton("Set default values probability");
		buttonDefaultProbability.setEnabled(false);

		JPanel inputPanel = new InputPanel();
		inputPanel.addPropertyChangeListener("mixer", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				if (pitchDetectionPanel.getComponents().length > 0
						&& !pitchDetectionPanel.getComponents()[0].isEnabled()) {
					pitchDetectionPanel.setEnabled(true);
					Component[] c = pitchDetectionPanel.getComponents();
					for (int i = 0; i < c.length; i++) {
						JRadioButton jrb = (JRadioButton) c[i];
						if (!jrb.getText().equals(PitchEstimationAlgorithm.MPM.toString())) {
							jrb.setEnabled(false);
						} else {
							jrb.setEnabled(true);
						}
					}
				}
				thresholdSlider.setEnabled(true);
				try {
					setNewMixer((Mixer) arg0.getNewValue());
				} catch (LineUnavailableException e) {
					System.out.println("Line is already in use by another application");
				} catch (UnsupportedAudioFileException e) {
					System.out.println("File did not contain valid data of a recognized file type and format");
				}
			}
		});

		JPanel timePanel = new TimePanel();
		timePanel.addPropertyChangeListener("tempo", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg1) {
				tempo = (double) arg1.getNewValue();

			}
		});

		startTune = new JButton("Start tune");
		stopTune = new JButton("Stop tune");
		stopTune.setEnabled(false);

		accurateTuner = new JButton("Open accurate tuner");
		accurateTuner.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentMixer == null) {
					JOptionPane.showMessageDialog(null, "Selezionare un microfono in input", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else if (threshold == 0) {
					JOptionPane.showMessageDialog(null, "Threshold è 0 ... avvio threshold automatico", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
					buttonAutoThreshold.doClick();
				} else if (chord == null) {
					JOptionPane.showMessageDialog(null, "Selezionare il tipo di corda che stai usando", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else {
					JFrame accurateFrame = new JFrame("Accurate tuner");
					tunerPanel = new AccurateTunerPanel();
					accurateFrame.add(tunerPanel);
					frame.dispose();
					startAccurateTuner = true;
					accurateFrame.setMinimumSize(new Dimension(800, 1000));
					accurateFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					accurateFrame.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(java.awt.event.WindowEvent windowEvent) {
							accurateFrame.dispose();
							frame.setVisible(true);
							startAccurateTuner = false;
						}
					});
					accurateFrame.setLocationRelativeTo(null);
					accurateFrame.setVisible(true);
				}
			}
		});

		buttonAutoThreshold.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentMixer == null) {
					JOptionPane.showMessageDialog(null, "Selezionare un microfono in input", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else {
					jTimerThreshold(5);
				}
			}
		});

		buttonAutoProbability.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentMixer == null) {
					JOptionPane.showMessageDialog(null, "Selezionare un microfono in input", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else if (threshold == 0) {
					JOptionPane.showMessageDialog(null, "Threshold è 0 ... avvio threshold automatico", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
					buttonAutoThreshold.doClick();
				} else if (chord == null) {
					JOptionPane.showMessageDialog(null, "Selezionare il tipo di corda che stai usando", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else {
					n_precision = n_rms = 0;
					autoPrecision = autoRMS = 0;
					buttonDefaultProbability.doClick();
					jTimerProbability(10);
				}
			}
		});

		buttonDefaultProbability.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (chord) {
				case Steel:
					defaultRMS = 2f;
					precisionSlider.setValue(95);
					break;
				case Gut:
					defaultRMS = 4f;
					precisionSlider.setValue(98);
					break;
				default:
					defaultRMS = 3f;
					precisionSlider.setValue(97);
					break;
				}
				rmsLabel.setText("RMS: " + String.format("%.2f", defaultRMS));
			}
		});

		startTune.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentMixer == null) {
					JOptionPane.showMessageDialog(null, "Selezionare un microfono in input", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else if (threshold == 0) {
					JOptionPane.showMessageDialog(null, "Threshold è 0 ... avvio threshold automatico", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
					buttonAutoThreshold.doClick();
				} else if (chord == null) {
					JOptionPane.showMessageDialog(null, "Selezionare il tipo di corda che stai usando", "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
				} else {
					startTune.setEnabled(false);
					stopTune.setEnabled(true);
					accurateTuner.setEnabled(false);
					jTimerStartTune(3);
				}
			}
		});

		stopTune.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopTune.setEnabled(false);
				accurateTuner.setEnabled(true);
				stopExecution = System.currentTimeMillis();
				new StopTuneFrame(perfetto, ottimo, distinto, buono, discreto, sufficiente, appenaSufficiente,
						nonSufficiente, (stopExecution - startExecution) / 1000);
				startHear = false;
				frame.setEnabled(true);
			}
		});

		thresholdSlider = initialzeThresholdSlider();
		thresholdSlider.setEnabled(false);
		JSlider speedSlider = initialzeSpeedSlider();
		precisionSlider = initialzePrecisionSlider();
		precisionSlider.setEnabled(false);
		precisionLabel = new JLabel("Precisione: " + String.valueOf((int) (precision * 100)) + "%");
		precisionLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		rmsLabel = new JLabel("RMS: " + String.format("%.2f", defaultRMS));
		rmsLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		speedLabel = new JLabel("Velocità: " + String.valueOf(speed) + " bpm");
		speedLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		thresholdLabel = new JLabel("Threshold: " + String.valueOf(threshold));
		thresholdLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);

		sliderPanelThreshold.setBorder(BorderFactory.createTitledBorder(border, "4. Set threshold"));
		sliderPanelPrecision2
				.setBorder(BorderFactory.createTitledBorder(border, "5. Set probability of pitch precision detection"));
		sliderPanelSpeed.setBorder(BorderFactory.createTitledBorder(border, "6. Set speed metronome"));
		startContainer.setBorder(BorderFactory.createTitledBorder(border, "8. Start/Stop tune"));
		accurateContainer.setBorder(BorderFactory.createTitledBorder(border, "9. Accurate tuner"));
		oscilloscopeContainer.setBorder(BorderFactory.createTitledBorder(border, "10. Oscilloscope"));
		decibelContainer.setBorder(BorderFactory.createTitledBorder(border, "11. Sound detector"));
		spectogramContainer
				.setBorder(BorderFactory.createTitledBorder(border, "12. Utter a sound (whistling works best)"));

		graphPanel = new GraphPanel();
		spectogramPanel = new SpectrogramPanel();
		soundPanel = new SoundDetectorPanel(threshold);

		sliderPanelThreshold.add(buttonAutoThreshold, BorderLayout.NORTH);
		sliderPanelThreshold.add(thresholdSlider, BorderLayout.CENTER);
		sliderPanelThreshold.add(thresholdLabel, BorderLayout.SOUTH);
		sliderPanelPrecision1.add(buttonAutoProbability);
		sliderPanelPrecision1.add(buttonDefaultProbability);
		sliderPanelPrecision3.add(precisionLabel, BorderLayout.CENTER);
		sliderPanelPrecision3.add(rmsLabel, BorderLayout.EAST);
		sliderPanelPrecision2
				.setToolTipText("the higher the accuracy, the higher the probability of recognizing the notes, "
						+ "but at the expense of playing \"clean\" notes.");
		sliderPanelPrecision2.add(sliderPanelPrecision1, BorderLayout.NORTH);
		sliderPanelPrecision2.add(precisionSlider, BorderLayout.CENTER);
		sliderPanelPrecision2.add(sliderPanelPrecision3, BorderLayout.SOUTH);
		sliderPanelSpeed.add(speedSlider, BorderLayout.CENTER);
		sliderPanelSpeed.add(speedLabel, BorderLayout.SOUTH);
		startContainer.add(startTune);
		accurateContainer.add(accurateTuner);
		oscilloscopeContainer.setToolTipText("Sound wave shape");
		oscilloscopeContainer.add(graphPanel);
		decibelContainer.setToolTipText("Energy level when sound is counted (dB SPL).");
		decibelContainer.add(soundPanel);
		spectogramContainer.setToolTipText("Spectogram of the sound wave");
		spectogramContainer.add(spectogramPanel);

		containerPanel1.add(inputPanel, BorderLayout.NORTH);
		containerPanel1.add(pitchDetectionPanel, BorderLayout.CENTER);
		containerPanel1.add(stringsPanel, BorderLayout.SOUTH);
		containerPanel2.add(sliderPanelThreshold);
		containerPanel2.add(sliderPanelPrecision2);
		containerPanel2.add(sliderPanelSpeed);
		containerPanel2.add(timePanel);
		containerPanel2.add(startContainer);
		containerPanel2.add(accurateContainer);
		containerPanel3.add(oscilloscopeContainer);
		containerPanel3.add(decibelContainer);
		containerPanel3.add(spectogramContainer);

		containerAllPanel.add(containerPanel1);
		containerAllPanel.add(containerPanel2);
		containerAllPanel.add(containerPanel3);

		add(containerAllPanel);
	}

	/** FUNZIONE MAIN **/
	public static void main(final String... strings) throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					// ignore failure to set default look en feel;
				}
				frame = strings.length == 0 ? new MusicNoteRecorder(null) : new MusicNoteRecorder(strings[0]);
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
				frame.setMinimumSize(new Dimension(1600, 900));
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	/** PITCH **/
	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
		long currentTime = 0;
		boolean isAccepted = false, avoidOctave = false;
		float tollerancePause = 6f;
		String newNote = null, valueNote, tempdB, newString;
		float probability = pitchDetectionResult.getProbability(), range = 0;
		double rms = audioEvent.getRMS() * 100;
		pitch = pitchDetectionResult.getPitch();
		float dB = (float) silenceDetector.currentSPL();

		// Se è stato avviato l'accordatore
		if (!frame.isVisible() && !startHear && !startAutomaticProbability && startAccurateTuner) {
			try {
				if (pitch != -1 && handleSound() && rms >= defaultRMS && probability >= precision - 0.6) {
					tunerPanel.setTunerInPanel(pitch, 5);
				}
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Se invece è stato avviato il settaggio automatico della precisione (e
		// dell'RMS)
		else if (frame.isVisible() && !startHear && startAutomaticProbability && !startAccurateTuner) {
			newString = getNameMusicNote(pitchDetectionResult.getPitch(), 0.0);
			if (newString != null) {
				autoRMS += audioEvent.getRMS() * 100;
				n_rms++;
				autoPrecision += pitchDetectionResult.getProbability();
				n_precision++;
			}
		}
		// Se invece è stato avviato l'ascoltatore
		else if (frame.isVisible() && startHear && !startAccurateTuner && !startAutomaticProbability) {
			newString = getNameMusicNote(pitch, 0.0);
			if (newString != null) {
				newNote = newString.substring(0, newString.indexOf("(") - 1);
				range = Float.parseFloat(newString.substring(newString.indexOf("(") + 1, newString.indexOf(")")));
				if (newNote.equals(oldNote)) {
					isAccepted = true;
				}
			}

			// Se l'algoritmo riesce a catturare un suono
			// con potenza (dB) minima segnata da handleSound()
			if ((pitch != -1 && handleSound() && probability >= (precision - 0.06)) || isAccepted) {
				long startTimeNote = 0;
				boolean isPause, octave = false;

				// Se non ci sono disturbi (rms >= rootMeanSquare(3) e
				// probability >= precision (0.97)) e
				// Se la nota rientra nel range delle frequenze del violino
				if ((rms >= defaultRMS && probability >= precision && newString != null) || isAccepted) {

					// Deve essere stata eseguita almeno una nota corretta
					if (i > 2) {

						// stessa nota intervallo diverso
						if (!oldNote.equals("") && oldNote.equals(newNote) && counterPause >= tollerancePause - 2
								&& counterPause <= tollerancePause) {
							oldNote = "intervallo";
						}
						// pausa
						else if (!oldNote.equals("") && counterPause > tollerancePause) {
							firstPause = true;
							///////////////////////////////////////// oldNote = "pause";
						}
					}
					// tempo di silenzio ignorato
					if (!oldNote.equals("") && counterPause > 0) {
						silenceTime += System.currentTimeMillis() - startTimePause;
					}

					counterPause = 0;

					System.out.println(i - 1 + "] #IN -- OLD NOTE = " + oldNote + ", newNote = " + newNote
							+ ", probabilità = " + probability + ", rms = " + rms + ", DB = "
							+ (int) (silenceDetector.currentSPL()) + " range = " + range);

					// Controlla probabilità di errore ottava
					// Controlla sulla nota precedente (semitono)
					if (!oldNote.equals("pause") && !oldNote.equals("invervallo") && !oldNote.equals("")) {
						octave = checkOctave(oldNote, newNote);
						if (octave) {
							if (!noteOctave.equals(newNote)) {
								noteOctave = newNote;
								newNote = oldNote;
								System.out.println("OTTAVA TROVATA!");
							} else {
								System.out.println("OTTAVA EVITATA!");
								avoidOctave = true;
							}
						}
						if (sameNote != 0 && !octave) {
							float prevRange = averageRange / sameNote;
							if ((newString = checkPrevius(oldNote, newNote, pitch, prevRange)) != null) {
								newNote = newString.substring(0, newString.indexOf("(") - 1);
								range = Float.parseFloat(
										newString.substring(newString.indexOf("(") + 1, newString.indexOf(")")));
								System.out.println(i - 1 + "] ### REVISIONE IN -- OLD NOTE = " + oldNote
										+ ", newNote = " + newNote + ", probabilità = " + probability + ", rms = " + rms
										+ ", DB = " + dB + " range = " + range);
							}
						}
					}

					// Se sto eseguendo una nuova nota
					if (!oldNote.equals(newNote)) {
						startTimeNote = startTimeNextNote;
						startTimeNextNote = System.currentTimeMillis();

						// Precisione media della nota alla fine della sua
						// esecuzione (quando si passa da una nota
						// all'altra).
						// Se è la primissima nota, allora oldNote sarà una
						// stringa vuota e bisogna ignorare
						if (!oldNote.equals("")) {
							isPause = chechPause();

							// se c'è stata una pausa o un break ignoro questo
							// passaggio ed eseguo quello nella funzione
							// isPause()
							if (!isPause) {

								// se la durata della nota non è null stampo il
								// valore e la precisione della nota prima della
								// nuova
								if (((valueNote = getValueMusicNote(startTimeNote, startTimeNextNote, silenceTime,
										(bpm / speed))) != null) && (sameNote > minValidNote)) {
									showMessage();
									j = 1;
									if (!(tempdB = getStringDynamics()).equals(olddB)) {
										olddB = tempdB;
										textAreaNote.append(j++ + ") VALORE NOTA = " + valueNote + "\n" + "Intensita' = "
												+ olddB + " -> " + (int) (silenceDetector.currentSPL()) + "dB\n");
									} else {
										textAreaNote.append(j++ + ") VALORE NOTA = " + valueNote + "\n");
									}
									textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
									textAreaNote.append(getStringAccuracy());
									textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
									System.out.println("NOTA ACCETTATA");

									// Azzera i valori per la nuova nota
									// in esecuzione
									if (avoidOctave) {
										averageRange = previusOctaveRange;
										averagedB = previusOctavedB;
										sameNote++;
									} else {
										averageRange = 0;
										averagedB = 0;
										sameNote = 0;
									}
								}
								// se la durata della nota è null la ignoro e
								// cancello la stringa dalla jtextarea
								else {
									replaceMessageVoid();
									System.out.println("NOTA RIFIUTATA");

									// Azzera i valori per la nuova nota
									// in esecuzione
									averageRange = 0;
									averagedB = 0;
									sameNote = 0;
									noteOctave = "";

									if (!oldNote.equals("")) {
										oldNote = newNote;
										averageRange += range;
										averagedB += dB;
										sameNote++;
										messageNote = String.format(
												"NOTA: /*** " + newNote + " ***/ -> Frequenza = %.2f Hz" + "\n"
														+ "Probabilita': %d%%, RMS: %.2f",
												pitch, (int) (probability * 100), rms, range);

										if (i > 1) {
											textAreaNote.append("\n" + i + "] " + messageNote + "\n");
										} else {
											textAreaNote.append(i + "] " + messageNote + "\n");
										}
										i++;
										preSetCaret = textAreaNote.getCaretPosition();
										textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
										messageShow = true;
									}
									return;
								}
							}
						}

						averageRange += range;
						averagedB += dB;
						sameNote++;
						oldNote = newNote;
						noteOctave = "";
						messageNote = String.format(
								"NOTA: /*** " + newNote + " ***/ -> Frequenza = %.2f Hz" + "\n"
										+ "Probabilita': %d%%, RMS: %.2f",
								pitch, (int) (probability * 100), rms, range);

						if (i > 1) {
							textAreaNote.append("\n" + i + "] " + messageNote + "\n");
						} else {
							textAreaNote.append(i + "] " + messageNote + "\n");
						}
						i++;

						preSetCaret = textAreaNote.getCaretPosition();
						textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
						messageShow = true;
					}
					// Se sto eseguendo la stessa nota
					else {
						currentTime = System.currentTimeMillis();
						double diffTime = (double) (currentTime - startTimeNextNote) / 1000;
						if (!octave) {
							averageRange += range;
							averagedB += dB;
							sameNote++;
						} else {
							previusOctaveRange = range;
							previusOctavedB = dB;
						}
						if (diffTime >= ((bpm / speed) * 4 * tempo)) {

							// se la durata della nota è null in questo caso
							// significa che è troppo grande (la ignoro)
							if (((valueNote = getValueMusicNote(startTimeNextNote, currentTime, silenceTime,
									(bpm / speed))) != null) && (sameNote > minValidNote)) {
								showMessage();
								if (!(tempdB = getStringDynamics()).equals(olddB)) {
									olddB = tempdB;
									textAreaNote.append(j++ + ") VALORE NOTA = " + valueNote + "\n" + "Intensita' = "
											+ olddB + " -> " + (int) (silenceDetector.currentSPL()) + "dB\n");
								} else {
									textAreaNote.append(j++ + ") VALORE NOTA = " + valueNote + "\n");
								}
								textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
								textAreaNote.append(getStringAccuracy());
								textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
								System.out.println("NOTA ACCETTATA");

								// Azzera i valori per la nuova nota in
								// esecuzione
								startTimeNextNote = currentTime;
								averageRange = 0;
								averagedB = 0;
								sameNote = 0;
							} else {
								replaceMessageVoid();
								System.out.println("NOTA RIFIUTATA");

								// Azzera i valori per la nuova nota in
								// esecuzione
								startTimeNextNote = currentTime;
								averageRange = 0;
								averagedB = 0;
								sameNote = 0;
								return;
							}
						}
					}
				}
				// POSSIBILE NOTA NON VALIDA
				else {
					// IGNORA
					System.out.println(
							i - 1 + "] #OUT - OLD NOTE = " + oldNote + ", newNote = " + newNote + ", probabilità = "
									+ probability + ", rms = " + rms + ", DB = " + dB + ", pitch = " + pitch);
				}
			}
			// Se non rileva pitch o la probabilità è < precision-0.06
			else {
				if (!oldNote.equals("")) {

					// potrebbe portare ad una pausa
					if (counterPause == 0) {
						startTimePause = System.currentTimeMillis();
					}
					counterPause += (speed / bpm);
					if (counterPause > tollerancePause) {
						currentTime = System.currentTimeMillis();
						if (firstPause) {
							k = 1;

							// se la durata della nota è null la ignoro
							if (((valueNote = getValueMusicNote(startTimeNextNote, startTimePause, silenceTime,
									(bpm / speed))) != null) && (sameNote > minValidNote)) {
								showMessage();
								if (!(tempdB = getStringDynamics()).equals(olddB)) {
									olddB = tempdB;
									textAreaNote.append(--j + ") VALORE NOTA = " + valueNote + "\n" + "Intensita' = "
											+ olddB + " -> " + (int) (silenceDetector.currentSPL()) + "dB\n");
								} else {
									textAreaNote.append(--j + ") VALORE NOTA = " + valueNote + "\n");
								}
								textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
								textAreaNote.append(getStringAccuracy());
								textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
								System.out.println("NOTA ACCETTATA");

								// Azzera i valori per la nuova nota in
								// esecuzione
								j = 1;
								startTimeNextNote = currentTime;
								averageRange = 0;
								averagedB = 0;
								sameNote = 0;
								oldNote = "pause";
								firstPause = false;
							} else {
								replaceMessageVoid();
								startTimeNextNote = currentTime;
								System.out.println("NOTA RIFIUTATA");

								// Azzera i valori per la nuova nota in
								// esecuzione
								averageRange = 0;
								averagedB = 0;
								sameNote = 0;
								j = 1;
								if (i <= 2) {
									oldNote = "";
								} else {
									oldNote = "pause";
									firstPause = false;
								}
								return;
							}
						}
						double diffTime = (double) (currentTime - startTimePause) / 1000;
						if (diffTime >= ((60 / speed) * 4 * tempo)) {

							// se la durata della pausa non è null allora stampo
							// il suo valore (che è massimo in questo caso)
							if ((valueNote = getValueMusicNote(startTimePause, currentTime, 0, (60 / speed))) != null) {
								if (k <= 1) {
									textAreaNote.append("\n" + k++ + ") VALORE PAUSA = " + valueNote + "\n");
								} else {
									textAreaNote.append(k++ + ") VALORE PAUSA = " + valueNote + "\n");
								}
								textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
								System.out.println("PAUSA ACCETTATA");
							}
							// se la durata della pausa è null in questo caso
							// significa che è troppo grande (la dovrei
							// ignorare, ma in realtà comunque la stampo col suo
							// valore massimo)
							else {
								System.out.println("i = " + i + ", " + k + "] Valore della pausa troppo grande");
								textAreaNote.replaceRange("", preSetCaret, textAreaNote.getCaretPosition());
								System.out.println("PAUSA RIFIUTATA");
							}
							startTimePause = currentTime;
						}
					}
				}
			}
		}
	}

	/** DALLA FREQUENZA AL NOME DELLA NOTA MUSICALE (VIOLINO) **/
	public String getNameMusicNote(double pitch2, double doubleTollerance) {
		int tollerance = 5;
		String musicNote = null;

		if (pitch2 < sol3Frequency - tollerance) {
			// NON E' UNA NOTA DEL VIOLINO
			return null;
		}

		// Prima ottava
		else if (pitch2 <= solD3Frequency - tollerance) {
			// SOL 4a corda
			musicNote = "SOL3 (" + (pitch2 - sol3Frequency) + ")";
		} else if (pitch2 <= la3Frequency - tollerance) {
			// SOL# 4a corda
			musicNote = "SOL#3 (" + (pitch2 - solD3Frequency) + ")";
		} else if (pitch2 <= laD3Frequency - tollerance) {
			// LA 4a corda
			musicNote = "LA3 (" + (pitch2 - la3Frequency) + ")";
		} else if (pitch2 <= si3Frequency - tollerance) {
			// LA# 4a corda
			musicNote = "LA#3 (" + (pitch2 - laD3Frequency) + ")";
		} else if (pitch2 <= do4Frequency - tollerance - doubleTollerance) {
			// SI 4a corda
			musicNote = "SI3 (" + (pitch2 - si3Frequency) + ")";
		} else if (pitch2 <= doD4Frequency - tollerance - doubleTollerance) {
			// DO 4a corda
			musicNote = "DO4 (" + (pitch2 - do4Frequency) + ")";
		} else if (pitch2 <= re4Frequency - tollerance - doubleTollerance) {
			// DO# 4a corda
			musicNote = "DO#4 (" + (pitch2 - doD4Frequency) + ")";
		} else if (pitch2 <= reD4Frequency - tollerance - doubleTollerance) {
			// RE 3a corda
			musicNote = "RE4 (" + (pitch2 - re4Frequency) + ")";
		} else if (pitch2 <= mi4Frequency - tollerance - doubleTollerance) {
			// RE# 3a corda
			musicNote = "RE#4 (" + (pitch2 - reD4Frequency) + ")";
		} else if (pitch2 <= fa4Frequency - tollerance - doubleTollerance) {
			// MI 3a corda
			musicNote = "MI4 (" + (pitch2 - mi4Frequency) + ")";
		} else if (pitch2 <= 370 - tollerance - doubleTollerance) {
			// FA 3a corda
			musicNote = "FA4 (" + (pitch2 - fa4Frequency) + ")";
		} else if (pitch2 <= sol4Frequency - tollerance - doubleTollerance) {
			// FA# 3a corda
			musicNote = "FA#4 (" + (pitch2 - faD4Frequency) + ")";
		}

		// Seconda ottava
		else if (pitch2 <= solD4Frequency - tollerance - doubleTollerance) {
			// SOL 3a corda
			musicNote = "SOL4 (" + (pitch2 - sol4Frequency) + ")";
		} else if (pitch2 <= la4Frequency - tollerance - doubleTollerance) {
			// SOL# 3a corda
			musicNote = "SOL#4 (" + (pitch2 - solD4Frequency) + ")";
		} else if (pitch2 <= laD4Frequency - tollerance - doubleTollerance) {
			// LA 2a corda
			musicNote = "LA4 (" + (pitch2 - la4Frequency) + ")";
		} else if (pitch2 <= si4Frequency - tollerance - doubleTollerance) {
			// LA# 2a corda
			musicNote = "LA#4 (" + (pitch2 - laD4Frequency) + ")";
		} else if (pitch2 <= do5Frequency - tollerance - doubleTollerance) {
			// SI 2a corda
			musicNote = "SI4 (" + (pitch2 - si4Frequency) + ")";
		} else if (pitch2 <= doD5Frequency - tollerance - doubleTollerance) {
			// DO 2a corda
			musicNote = "DO5 (" + (pitch2 - do5Frequency) + ")";
		} else if (pitch2 <= re5Frequency - tollerance - doubleTollerance) {
			// DO# 2a corda
			musicNote = "DO#5 (" + (pitch2 - doD5Frequency) + ")";
		} else if (pitch2 <= reD5Frequency - tollerance - doubleTollerance) {
			// RE 2a corda
			musicNote = "RE5 (" + (pitch2 - re5Frequency) + ")";
		} else if (pitch2 <= mi5Frequency - tollerance - doubleTollerance) {
			// RE# 2a corda
			musicNote = "RE#5 (" + (pitch2 - reD5Frequency) + ")";
		} else if (pitch2 <= fa5Frequency - tollerance - doubleTollerance) {
			// MI 1a corda
			musicNote = "MI5 (" + (pitch2 - mi5Frequency) + ")";
		} else if (pitch2 <= faD5Frequency - tollerance - doubleTollerance) {
			// FA 1a corda
			musicNote = "FA5 (" + (pitch2 - fa5Frequency) + ")";
		} else if (pitch2 <= sol5Frequency - tollerance - doubleTollerance) {
			// FA# 1a corda
			musicNote = "FA#5 (" + (pitch2 - faD5Frequency) + ")";
		}

		// Terza ottava
		else if (pitch2 <= solD5Frequency - tollerance - doubleTollerance) {
			// SOL 1a corda
			musicNote = "SOL5 (" + (pitch2 - sol5Frequency) + ")";
		} else if (pitch2 <= la5Frequency - tollerance - doubleTollerance) {
			// SOL# 1a corda
			musicNote = "SOL#5 (" + (pitch2 - solD5Frequency) + ")";
		} else if (pitch2 <= laD5Frequency - tollerance - doubleTollerance) {
			// LA 1a corda
			musicNote = "LA5 (" + (pitch2 - la5Frequency) + ")";
		} else if (pitch2 <= si5Frequency - tollerance - doubleTollerance) {
			// LA# 1a corda
			musicNote = "LA#5 (" + (pitch2 - laD5Frequency) + ")";
		} else if (pitch2 <= do6Frequency - tollerance - doubleTollerance) {
			// SI 1a corda
			musicNote = "SI5 (" + (pitch2 - si5Frequency) + ")";
		} else if (pitch2 <= doD6Frequency - tollerance - doubleTollerance) {
			// DO 2a corda
			musicNote = "DO6 (" + (pitch2 - do6Frequency) + ")";
		} else if (pitch2 <= re6Frequency - tollerance - doubleTollerance) {
			// DO# 2a corda
			musicNote = "DO#6 (" + (pitch2 - doD6Frequency) + ")";
		} else if (pitch2 <= reD6Frequency - tollerance - doubleTollerance) {
			// RE 2a corda
			musicNote = "RE6 (" + (pitch2 - re6Frequency) + ")";
		} else if (pitch2 <= mi6Frequency - tollerance - doubleTollerance) {
			// RE# 2a corda
			musicNote = "RE#6 (" + (pitch2 - reD6Frequency) + ")";
		} else if (pitch2 <= fa6Frequency - tollerance - doubleTollerance) {
			// MI 1a corda
			musicNote = "MI6 (" + (pitch2 - mi6Frequency) + ")";
		} else if (pitch2 <= faD6Frequency - tollerance - doubleTollerance) {
			// FA 1a corda
			musicNote = "FA6 (" + (pitch2 - fa6Frequency) + ")";
		} else if (pitch2 <= sol6Frequency - tollerance - doubleTollerance) {
			// FA# 1a corda
			musicNote = "FA#6 (" + (pitch2 - faD6Frequency) + ")";
		}

		// Quarta ottava
		else if (pitch2 <= solD6Frequency - tollerance - doubleTollerance) {
			// SOL 1a corda
			musicNote = "SOL6 (" + (pitch2 - sol6Frequency) + ")";
		} else if (pitch2 <= la6Frequency - tollerance - doubleTollerance) {
			// SOL# 1a corda
			musicNote = "SOL#6 (" + (pitch2 - solD6Frequency) + ")";
		} else if (pitch2 <= laD6Frequency - tollerance - doubleTollerance) {
			// LA 1a corda
			musicNote = "LA6 (" + (pitch2 - la6Frequency) + ")";
		} else if (pitch2 <= si6Frequency - tollerance - doubleTollerance) {
			// LA# 1a corda
			musicNote = "LA#6 (" + (pitch2 - laD6Frequency) + ")";
		} else if (pitch2 <= do7Frequency - tollerance - doubleTollerance) {
			// SI 1a corda
			musicNote = "SI6 (" + (pitch2 - si6Frequency) + ")";
		} else if (pitch2 <= doD7Frequency - tollerance - doubleTollerance) {
			// DO 2a corda
			musicNote = "DO7 (" + (pitch2 - do7Frequency) + ")";
		} else if (pitch2 <= re7Frequency - tollerance - doubleTollerance) {
			// DO# 2a corda
			musicNote = "DO#7 (" + (pitch2 - doD7Frequency) + ")";
		} else if (pitch2 <= reD7Frequency - tollerance - doubleTollerance) {
			// RE 2a corda
			musicNote = "RE7 (" + (pitch2 - re7Frequency) + ")";
		} else if (pitch2 <= mi7Frequency - tollerance - doubleTollerance) {
			// RE# 2a corda
			musicNote = "RE#7 (" + (pitch2 - reD7Frequency) + ")";
		} else if (pitch2 <= fa7Frequency - tollerance - doubleTollerance) {
			// MI 1a corda
			musicNote = "MI7 (" + (pitch2 - mi7Frequency) + ")";
		} else if (pitch2 <= faD7Frequency - tollerance - doubleTollerance) {
			// FA 1a corda
			musicNote = "FA7 (" + (pitch2 - fa7Frequency) + ")";
		} else if (pitch2 <= sol7Frequency - tollerance - doubleTollerance) {
			// FA# 1a corda
			musicNote = "FA#7 (" + (pitch2 - faD7Frequency) + ")";
		}

		// Forse
		else if (pitch2 <= solD7Frequency - tollerance - doubleTollerance) {
			// SOL 1a corda
			musicNote = "SOL7 (" + (pitch2 - sol7Frequency) + ")";
		} else if (pitch2 <= la7Frequency - tollerance - doubleTollerance) {
			// SOL# 1a corda
			musicNote = "SOL#7 (" + (pitch2 - solD7Frequency) + ")";
		} else if (pitch2 <= laD7Frequency - tollerance - doubleTollerance) {
			// LA 1a corda
			musicNote = "LA7 (" + (pitch2 - solD7Frequency) + ")";
		} else if (pitch2 <= si7Frequency - tollerance - doubleTollerance) {
			// LA# 1a corda
			musicNote = "LA#7 (" + (pitch2 - laD7Frequency) + ")";
		} else if (pitch2 <= do8Frequency - tollerance - doubleTollerance) {
			// SI 1a corda
			musicNote = "SI7 (" + (pitch2 - si7Frequency) + ")";
		} else if (pitch2 <= do8Frequency + 250 - tollerance - doubleTollerance) {
			// DO 2a corda
			musicNote = "DO8 (" + (pitch2 - do8Frequency) + ")";
		} else {
			// NON E' UNA NOTA DEL VIOLINO
			return null;
		}

		return musicNote;
	}

	/** DURATA DELLA NOTA MUSICALE **/
	public String getValueMusicNote(long startTime, long endTime, long silence, double speed) {
		String value = null;
		double differenceTime;
		if (startTime >= silence) {
			differenceTime = ((double) (endTime - startTime)) / 1000;
		} else {
			differenceTime = ((double) ((endTime - startTime) - silence)) / 1000;
		}

		silenceTime = 0;

		if (Double.compare(differenceTime, 0.035 * speed) < 0) {
			return null;
		} else if (Double.compare(differenceTime, (0.07 * speed)) < 0) {
			value = "SEMI-BISCROMA";
		} else if (Double.compare(differenceTime, (0.14 * speed)) < 0) {
			value = "BISCROMA";
		} else if (Double.compare(differenceTime, (0.28 * speed)) < 0) {
			value = "SEMI-CROMA";
		} else if (Double.compare(differenceTime, (0.55 * speed)) < 0) {
			value = "CROMA";
		} else if (Double.compare(differenceTime, (1.1 * speed)) < 0) {
			value = "SEMI-MINIMA";
		} else if (Double.compare(differenceTime, (1.65 * speed)) < 0) {
			value = "SEMI-MINIMA COL PUNTO";
		} else if (Double.compare(differenceTime, (1.93 * speed)) < 0) {
			value = "SEMI-MINIMA CON DUE PUNTI";
		} else if (Double.compare(differenceTime, (2.2 * speed)) < 0) {
			value = "MINIMA";
		} else if (Double.compare(differenceTime, (2.75 * speed)) < 0) {
			value = "MINIMA PIU' CROMA";
		} else if (Double.compare(differenceTime, (3.3 * speed)) < 0) {
			value = "MINIMA COL PUNTO";
		} else if (Double.compare(differenceTime, (3.85 * speed)) < 0) {
			value = "MINIMA CON DUE PUNTI";
		} else if (Double.compare(differenceTime, (4.4 * speed)) < 0) {
			value = "SEMI-BREVE";
		}

		return value;
	}

	/** PRECISIONE DELLA NOTA **/
	public String getStringAccuracy() {
		String message = null;
		String plus = "";

		// Valori di Default se è "la prima e la seconda ottava"
		float perfetto1 = 1.65f, ottimo1 = perfetto1 * 2, distinto1 = perfetto1 * 3, buono1 = perfetto1 * 4,
				discreto1 = perfetto1 * 5, sufficiente1 = perfetto1 * 6, appenaSufficiente1 = perfetto1 * 7;

		// se è la seconda ottava incremento di un fattore 1.7 rispetto al default
		if (oldNote.contains("4")) {
			perfetto1 = 2.82f;
			ottimo1 = perfetto1 * 2;
			distinto1 = perfetto1 * 3;
			buono1 = perfetto1 * 4;
			discreto1 = perfetto1 * 5;
			sufficiente1 = perfetto1 * 6;
			appenaSufficiente1 = perfetto1 * 7;
		}
		// se è la terza ottava incremento di un fattore 3.43 rispetto al default
		if (oldNote.contains("5")) {
			perfetto1 = 5.65f;
			ottimo1 = perfetto1 * 2;
			distinto1 = perfetto1 * 3;
			buono1 = perfetto1 * 4;
			discreto1 = perfetto1 * 5;
			sufficiente1 = perfetto1 * 6;
			appenaSufficiente1 = perfetto1 * 7;
		}
		// se è la quarta ottava incremento di un fattore 6.78 rispetto al default
		else if (oldNote.contains("6")) {
			perfetto1 = 11.19f;
			ottimo1 = perfetto1 * 2;
			distinto1 = perfetto1 * 3;
			buono1 = perfetto1 * 4;
			discreto1 = perfetto1 * 5;
			sufficiente1 = perfetto1 * 6;
			appenaSufficiente1 = perfetto1 * 7;
		}
		// se è la quinta ottava incremento di un fattore 13.6 rispetto al default
		else if (oldNote.contains("7")) {
			perfetto1 = 22.44f;
			ottimo1 = perfetto1 * 2;
			distinto1 = perfetto1 * 3;
			buono1 = perfetto1 * 4;
			discreto1 = perfetto1 * 5;
			sufficiente1 = perfetto1 * 6;
			appenaSufficiente1 = perfetto1 * 7;
		}
		// se è la sesta ottava incremento di un fattore 27.4 rispetto al default
		else if (oldNote.contains("8")) {
			perfetto1 = 45.21f;
			ottimo1 = perfetto1 * 2;
			distinto1 = perfetto1 * 3;
			buono1 = perfetto1 * 4;
			discreto1 = perfetto1 * 5;
			sufficiente1 = perfetto1 * 6;
			appenaSufficiente1 = perfetto1 * 7;
		}

		if (sameNote > 0) {
			averageRange /= sameNote;
			if (averageRange > 0) {
				plus = "+";
			}
			if (averageRange <= perfetto1 && averageRange >= -perfetto1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## PERFETTO ##\n", averageRange);
				perfetto++;
			} else if (averageRange <= ottimo1 && averageRange >= -ottimo1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## OTTIMO ##\n", averageRange);
				ottimo++;
			} else if (averageRange <= distinto1 && averageRange >= -distinto1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## DISTINTO ##\n", averageRange);
				distinto++;
			} else if (averageRange <= buono1 && averageRange >= -buono1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## BUONO ##\n", averageRange);
				buono++;
			} else if (averageRange <= discreto1 && averageRange >= -discreto1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## DISCRETO ##\n", averageRange);
				discreto++;
			} else if (averageRange <= sufficiente1 && averageRange >= -sufficiente1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## SUFFICIENTE ##\n", averageRange);
				sufficiente++;
			} else if (averageRange <= appenaSufficiente1 && averageRange >= -appenaSufficiente1) {
				message = String.format("Precisione: " + plus + "%.2f --> ## APPENA SUFFICIENTE ##\n", averageRange);
				appenaSufficiente++;
			} else {
				message = String.format("Precisione: " + plus + "%.2f --> ## INSUFFICIENTE ##\n", averageRange);
				nonSufficiente++;
			}
		}
		return message;
	}

	/** DINAMICA DELLA NOTA **/
	public String getStringDynamics() {
		String intensità = "pppp";

		float dB = (averagedB / sameNote);

		if (dB < -110) {
			intensità = "pianississimo";
		} else if (dB < -98) {
			intensità = "pianissimo";
		} else if (dB < -86) {
			intensità = "piano";
		} else if (dB < -74) {
			intensità = "mezzo piano";
		} else if (dB < -62) {
			intensità = "mezzo forte";
		} else if (dB < -50) {
			intensità = "forte";
		} else if (dB < -38) {
			intensità = "fortissimo";
		} else {
			intensità = "fortississimo";
		}

		return intensità;
	}

	/** CANCELLA MESSAGGIO **/
	public void replaceMessageVoid() {
		if (messageShow) {
			textAreaNote.replaceRange("", preSetCaret, textAreaNote.getCaretPosition());
			messageShow = false;
		}
		if (i == 2) {
			i--;
			oldNote = "";
		} else if (i > 2) {
			i--;
		}
	}

	/** MOSTRA MESSAGGIO **/
	public void showMessage() {
		if (!messageShow) {
			messageShow = true;
			if (i > 1) {
				textAreaNote.append("\n" + i++ + "] " + messageNote + "\n");
			} else {
				textAreaNote.append(i++ + "] " + messageNote + "\n");
			}
			preSetCaret = textAreaNote.getCaretPosition();
			textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
		}
	}

	/** C'E' STATA UNA PAUSA ? **/
	public boolean chechPause() {
		String valueNote = null;

		// Precisione media della nota alla fine della sua
		// esecuzione (quando si passa da una nota
		// all'altra).
		// Se è la primissima nota, allora oldNote sarà una
		// stringa vuota e bisogna ignorare
		if (!oldNote.equals("")) {

			// se è una pausa
			// stampo la durata della pausa prima
			// della nuova nota
			if (oldNote.equals("pause")) {
				if ((valueNote = getValueMusicNote(startTimePause, startTimeNextNote, 0, (bpm / speed))) != null) {
					if (k <= 1) {
						textAreaNote.append("\n" + k + ") VALORE PAUSA = " + valueNote + "\n");
					} else {
						textAreaNote.append(k + ") VALORE PAUSA = " + valueNote + "\n");
					}
					textAreaNote.setCaretPosition(textAreaNote.getDocument().getLength());
					System.out.println("PAUSA ACCETTATA");
				} else {
					// DISTURBO
					textAreaNote.replaceRange("", preSetCaret, textAreaNote.getCaretPosition()); // ----
					System.out.println("PAUSA RIFIUTATA");
				}
				return true;
			}
		}
		return false;
	}

	/** CONTROLLA OTTAVA CON LA NOTA PRECEDENTE **/
	public boolean checkOctave(String previusNote, String actualNote) {
		// PROBLEMI CON LE OTTAVE
		// PRIMA I # PER MEZZO DI CONTAINS PER EFFICIENZA

		// DO#
		if (previusNote.contains("DO#") && actualNote.contains("DO#")) {
			if (previusNote.equals("DO#4") && actualNote.equals("DO#5")
					|| previusNote.equals("DO#4") && actualNote.equals("DO#6")
					|| previusNote.equals("DO#4") && actualNote.equals("DO#7")
					|| previusNote.equals("DO#5") && actualNote.equals("DO#4")
					|| previusNote.equals("DO#5") && actualNote.equals("DO#6")
					|| previusNote.equals("DO#5") && actualNote.equals("DO#7")
					|| previusNote.equals("DO#6") && actualNote.equals("DO#4")
					|| previusNote.equals("DO#6") && actualNote.equals("DO#5")
					|| previusNote.equals("DO#6") && actualNote.equals("DO#7")
					|| previusNote.equals("DO#7") && actualNote.equals("DO#4")
					|| previusNote.equals("DO#7") && actualNote.equals("DO#5")
					|| previusNote.equals("DO#7") && actualNote.equals("DO#6")) {
				return true;
			}
		}
		// DO
		else if (previusNote.contains("DO") && actualNote.contains("DO")) {
			if (previusNote.equals("DO4") && actualNote.equals("DO8")
					|| previusNote.equals("DO4") && actualNote.equals("DO5")
					|| previusNote.equals("DO4") && actualNote.equals("DO6")
					|| previusNote.equals("DO4") && actualNote.equals("DO7")
					|| previusNote.equals("DO5") && actualNote.equals("DO8")
					|| previusNote.equals("DO5") && actualNote.equals("DO4")
					|| previusNote.equals("DO5") && actualNote.equals("DO6")
					|| previusNote.equals("DO5") && actualNote.equals("DO7")
					|| previusNote.equals("DO6") && actualNote.equals("DO8")
					|| previusNote.equals("DO6") && actualNote.equals("DO4")
					|| previusNote.equals("DO6") && actualNote.equals("DO5")
					|| previusNote.equals("DO6") && actualNote.equals("DO7")
					|| previusNote.equals("DO7") && actualNote.equals("DO4")
					|| previusNote.equals("DO7") && actualNote.equals("DO5")
					|| previusNote.equals("DO7") && actualNote.equals("DO6")
					|| previusNote.equals("DO7") && actualNote.equals("DO8")
					|| previusNote.equals("DO8") && actualNote.equals("DO4")
					|| previusNote.equals("DO8") && actualNote.equals("DO5")
					|| previusNote.equals("DO8") && actualNote.equals("DO6")
					|| previusNote.equals("DO8") && actualNote.equals("DO7")) {
				return true;
			}
		}
		// RE#
		else if (previusNote.contains("RE#") && actualNote.contains("RE#")) {
			if (previusNote.equals("RE#4") && actualNote.equals("RE#5")
					|| previusNote.equals("RE#4") && actualNote.equals("RE#6")
					|| previusNote.equals("RE#4") && actualNote.equals("RE#7")
					|| previusNote.equals("RE#5") && actualNote.equals("RE#4")
					|| previusNote.equals("RE#5") && actualNote.equals("RE#6")
					|| previusNote.equals("RE#5") && actualNote.equals("RE#7")
					|| previusNote.equals("RE#6") && actualNote.equals("RE#4")
					|| previusNote.equals("RE#6") && actualNote.equals("RE#5")
					|| previusNote.equals("RE#6") && actualNote.equals("RE#7")
					|| previusNote.equals("RE#7") && actualNote.equals("RE#4")
					|| previusNote.equals("RE#7") && actualNote.equals("RE#5")
					|| previusNote.equals("RE#7") && actualNote.equals("RE#6")) {
				return true;
			}
		}
		// RE
		else if (previusNote.contains("RE") && actualNote.contains("RE")) {
			if (previusNote.equals("RE4") && actualNote.equals("RE3")
					|| previusNote.equals("RE4") && actualNote.equals("RE5")
					|| previusNote.equals("RE4") && actualNote.equals("RE6")
					|| previusNote.equals("RE4") && actualNote.equals("RE7")
					|| previusNote.equals("RE5") && actualNote.equals("RE4")
					|| previusNote.equals("RE5") && actualNote.equals("RE6")
					|| previusNote.equals("RE5") && actualNote.equals("RE7")
					|| previusNote.equals("RE6") && actualNote.equals("RE4")
					|| previusNote.equals("RE6") && actualNote.equals("RE5")
					|| previusNote.equals("RE6") && actualNote.equals("RE7")
					|| previusNote.equals("RE7") && actualNote.equals("RE4")
					|| previusNote.equals("RE7") && actualNote.equals("RE5")
					|| previusNote.equals("RE7") && actualNote.equals("RE6")) {
				return true;
			}
		}
		// MI
		else if (previusNote.contains("MI") && actualNote.contains("MI")) {
			if (previusNote.equals("MI4") && actualNote.equals("MI5")
					|| previusNote.equals("MI4") && actualNote.equals("MI6")
					|| previusNote.equals("MI4") && actualNote.equals("MI7")
					|| previusNote.equals("MI5") && actualNote.equals("MI4")
					|| previusNote.equals("MI5") && actualNote.equals("MI6")
					|| previusNote.equals("MI5") && actualNote.equals("MI7")
					|| previusNote.equals("MI6") && actualNote.equals("MI4")
					|| previusNote.equals("MI6") && actualNote.equals("MI5")
					|| previusNote.equals("MI6") && actualNote.equals("MI7")
					|| previusNote.equals("MI7") && actualNote.equals("MI4")
					|| previusNote.equals("MI7") && actualNote.equals("MI5")
					|| previusNote.equals("MI7") && actualNote.equals("MI6")) {
				return true;
			}
		}
		// FA#
		else if (previusNote.contains("FA#") && actualNote.contains("FA#")) {
			if (previusNote.equals("FA#4") && actualNote.equals("FA#5")
					|| previusNote.equals("FA#4") && actualNote.equals("FA#6")
					|| previusNote.equals("FA#4") && actualNote.equals("FA#7")
					|| previusNote.equals("FA#5") && actualNote.equals("FA#4")
					|| previusNote.equals("FA#5") && actualNote.equals("FA#6")
					|| previusNote.equals("FA#5") && actualNote.equals("FA#7")
					|| previusNote.equals("FA#6") && actualNote.equals("FA#4")
					|| previusNote.equals("FA#6") && actualNote.equals("FA#5")
					|| previusNote.equals("FA#6") && actualNote.equals("FA#7")
					|| previusNote.equals("FA#7") && actualNote.equals("FA#4")
					|| previusNote.equals("FA#7") && actualNote.equals("FA#5")
					|| previusNote.equals("FA#7") && actualNote.equals("FA#6")) {
				return true;
			}
		}
		// FA
		else if (previusNote.contains("FA") && actualNote.contains("FA")) {
			if (previusNote.equals("FA4") && actualNote.equals("FA5")
					|| previusNote.equals("FA4") && actualNote.equals("FA6")
					|| previusNote.equals("FA4") && actualNote.equals("FA7")
					|| previusNote.equals("FA5") && actualNote.equals("FA4")
					|| previusNote.equals("FA5") && actualNote.equals("FA6")
					|| previusNote.equals("FA5") && actualNote.equals("FA7")
					|| previusNote.equals("FA6") && actualNote.equals("FA4")
					|| previusNote.equals("FA6") && actualNote.equals("FA5")
					|| previusNote.equals("FA6") && actualNote.equals("FA7")
					|| previusNote.equals("FA7") && actualNote.equals("FA4")
					|| previusNote.equals("FA7") && actualNote.equals("FA5")
					|| previusNote.equals("FA7") && actualNote.equals("FA6")) {
				return true;
			}
		}
		// SOL#
		else if (previusNote.contains("SOL#") && actualNote.contains("SOL#")) {
			if (previusNote.equals("SOL#3") && actualNote.equals("SOL#4")
					|| previusNote.equals("SOL#3") && actualNote.equals("SOL#5")
					|| previusNote.equals("SOL#3") && actualNote.equals("SOL#6")
					|| previusNote.equals("SOL#3") && actualNote.equals("SOL#7")
					|| previusNote.equals("SOL#4") && actualNote.equals("SOL#3")
					|| previusNote.equals("SOL#4") && actualNote.equals("SOL#5")
					|| previusNote.equals("SOL#4") && actualNote.equals("SOL#6")
					|| previusNote.equals("SOL#4") && actualNote.equals("SOL#7")
					|| previusNote.equals("SOL#5") && actualNote.equals("SOL#3")
					|| previusNote.equals("SOL#5") && actualNote.equals("SOL#4")
					|| previusNote.equals("SOL#5") && actualNote.equals("SOL#6")
					|| previusNote.equals("SOL#5") && actualNote.equals("SOL#7")
					|| previusNote.equals("SOL#6") && actualNote.equals("SOL#3")
					|| previusNote.equals("SOL#6") && actualNote.equals("SOL#4")
					|| previusNote.equals("SOL#6") && actualNote.equals("SOL#5")
					|| previusNote.equals("SOL#6") && actualNote.equals("SOL#7")
					|| previusNote.equals("SOL#7") && actualNote.equals("SOL#3")
					|| previusNote.equals("SOL#7") && actualNote.equals("SOL#4")
					|| previusNote.equals("SOL#7") && actualNote.equals("SOL#5")
					|| previusNote.equals("SOL#7") && actualNote.equals("SOL#6")) {
				return true;
			}
		}
		// SOL
		else if (previusNote.contains("SOL") && actualNote.contains("SOL")) {
			if (previusNote.equals("SOL3") && actualNote.equals("SOL4")
					|| previusNote.equals("SOL3") && actualNote.equals("SOL5")
					|| previusNote.equals("SOL3") && actualNote.equals("SOL6")
					|| previusNote.equals("SOL3") && actualNote.equals("SOL7")
					|| previusNote.equals("SOL4") && actualNote.equals("SOL3")
					|| previusNote.equals("SOL4") && actualNote.equals("SOL5")
					|| previusNote.equals("SOL4") && actualNote.equals("SOL6")
					|| previusNote.equals("SOL4") && actualNote.equals("SOL7")
					|| previusNote.equals("SOL5") && actualNote.equals("SOL3")
					|| previusNote.equals("SOL5") && actualNote.equals("SOL4")
					|| previusNote.equals("SOL5") && actualNote.equals("SOL6")
					|| previusNote.equals("SOL5") && actualNote.equals("SOL7")
					|| previusNote.equals("SOL6") && actualNote.equals("SOL3")
					|| previusNote.equals("SOL6") && actualNote.equals("SOL4")
					|| previusNote.equals("SOL6") && actualNote.equals("SOL5")
					|| previusNote.equals("SOL6") && actualNote.equals("SOL7")
					|| previusNote.equals("SOL7") && actualNote.equals("SOL3")
					|| previusNote.equals("SOL7") && actualNote.equals("SOL4")
					|| previusNote.equals("SOL7") && actualNote.equals("SOL5")
					|| previusNote.equals("SOL7") && actualNote.equals("SOL6")) {
				return true;
			}
		}
		// LA#
		else if (previusNote.contains("LA#") && actualNote.contains("LA#")) {
			if (previusNote.equals("LA#3") && actualNote.equals("LA#4")
					|| previusNote.equals("LA#3") && actualNote.equals("LA#5")
					|| previusNote.equals("LA#3") && actualNote.equals("LA#6")
					|| previusNote.equals("LA#3") && actualNote.equals("LA#7")
					|| previusNote.equals("LA#4") && actualNote.equals("LA#3")
					|| previusNote.equals("LA#4") && actualNote.equals("LA#5")
					|| previusNote.equals("LA#4") && actualNote.equals("LA#6")
					|| previusNote.equals("LA#4") && actualNote.equals("LA#7")
					|| previusNote.equals("LA#5") && actualNote.equals("LA#3")
					|| previusNote.equals("LA#5") && actualNote.equals("LA#4")
					|| previusNote.equals("LA#5") && actualNote.equals("LA#6")
					|| previusNote.equals("LA#5") && actualNote.equals("LA#7")
					|| previusNote.equals("LA#6") && actualNote.equals("LA#3")
					|| previusNote.equals("LA#6") && actualNote.equals("LA#4")
					|| previusNote.equals("LA#6") && actualNote.equals("LA#5")
					|| previusNote.equals("LA#6") && actualNote.equals("LA#7")
					|| previusNote.equals("LA#7") && actualNote.equals("LA#3")
					|| previusNote.equals("LA#7") && actualNote.equals("LA#4")
					|| previusNote.equals("LA#7") && actualNote.equals("LA#5")
					|| previusNote.equals("LA#7") && actualNote.equals("LA#6")) {
				return true;
			}
		}
		// LA
		else if (previusNote.contains("LA") && actualNote.contains("LA")) {
			if (previusNote.equals("LA3") && actualNote.equals("LA4")
					|| previusNote.equals("LA3") && actualNote.equals("LA5")
					|| previusNote.equals("LA3") && actualNote.equals("LA6")
					|| previusNote.equals("LA3") && actualNote.equals("LA7")
					|| previusNote.equals("LA4") && actualNote.equals("LA3")
					|| previusNote.equals("LA4") && actualNote.equals("LA5")
					|| previusNote.equals("LA4") && actualNote.equals("LA6")
					|| previusNote.equals("LA4") && actualNote.equals("LA7")
					|| previusNote.equals("LA5") && actualNote.equals("LA3")
					|| previusNote.equals("LA5") && actualNote.equals("LA4")
					|| previusNote.equals("LA5") && actualNote.equals("LA6")
					|| previusNote.equals("LA5") && actualNote.equals("LA7")
					|| previusNote.equals("LA6") && actualNote.equals("LA3")
					|| previusNote.equals("LA6") && actualNote.equals("LA4")
					|| previusNote.equals("LA6") && actualNote.equals("LA5")
					|| previusNote.equals("LA6") && actualNote.equals("LA7")
					|| previusNote.equals("LA7") && actualNote.equals("LA3")
					|| previusNote.equals("LA7") && actualNote.equals("LA4")
					|| previusNote.equals("LA7") && actualNote.equals("LA5")
					|| previusNote.equals("LA7") && actualNote.equals("LA6")) {
				return true;
			}
		}
		// SI
		else if (previusNote.contains("SI") && actualNote.contains("SI")) {
			if (previusNote.equals("SI3") && actualNote.equals("SI4")
					|| previusNote.equals("SI3") && actualNote.equals("SI5")
					|| previusNote.equals("SI3") && actualNote.equals("SI6")
					|| previusNote.equals("SI3") && actualNote.equals("SI7")
					|| previusNote.equals("SI4") && actualNote.equals("SI3")
					|| previusNote.equals("SI4") && actualNote.equals("SI5")
					|| previusNote.equals("SI4") && actualNote.equals("SI6")
					|| previusNote.equals("SI4") && actualNote.equals("SI7")
					|| previusNote.equals("SI5") && actualNote.equals("SI3")
					|| previusNote.equals("SI5") && actualNote.equals("SI4")
					|| previusNote.equals("SI5") && actualNote.equals("SI6")
					|| previusNote.equals("SI5") && actualNote.equals("SI7")
					|| previusNote.equals("SI6") && actualNote.equals("SI3")
					|| previusNote.equals("SI6") && actualNote.equals("SI4")
					|| previusNote.equals("SI6") && actualNote.equals("SI5")
					|| previusNote.equals("SI6") && actualNote.equals("SI7")
					|| previusNote.equals("SI7") && actualNote.equals("SI3")
					|| previusNote.equals("SI7") && actualNote.equals("SI4")
					|| previusNote.equals("SI7") && actualNote.equals("SI5")
					|| previusNote.equals("SI7") && actualNote.equals("SI6")) {
				return true;
			}
		}
		return false;
	}

	/** CONTROLLA PRECISIONE CON NOTA PRECEDENTE **/
	public String checkPrevius(String previusNote, String actualNote, double pitch, float prevRange) {
		int tollerance = 10;
		double x = 0;
		int n = 2;

		// PROBLEMA FREQUENZA LIMITE TRA DUE SEMITONI
		// IL PRECEDENTE è > DEL SUCCESSIVO

		// DO - SI
		if (previusNote.equals("DO4") && actualNote.equals("SI3")) {
			x = (do4Frequency - si3Frequency) / n;
			if ((do4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO5") && actualNote.equals("SI4")) {
			x = (do5Frequency - si4Frequency) / n;
			if ((do5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO6") && actualNote.equals("SI5")) {
			x = (do6Frequency - si5Frequency) / n;
			if ((do6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO7") && actualNote.equals("SI6")) {
			x = (do7Frequency - si6Frequency) / n;
			if ((do7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO8") && actualNote.equals("SI7")) {
			x = (do8Frequency - si7Frequency) / n;
			if ((do8Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// DO# - DO
		else if (previusNote.equals("DO#4") && actualNote.equals("DO4")) {
			x = (doD4Frequency - do4Frequency) / n;
			if ((doD4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO#5") && actualNote.equals("DO5")) {
			x = (doD5Frequency - do5Frequency) / n;
			if ((doD5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO#6") && actualNote.equals("DO6")) {
			x = (doD6Frequency - do6Frequency) / n;
			if ((doD6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("DO#7") && actualNote.equals("DO7")) {
			x = (doD7Frequency - do7Frequency) / n;
			if ((doD7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// RE - DO#
		else if (previusNote.equals("RE4") && actualNote.equals("DO#4")) {
			x = (re4Frequency - doD4Frequency) / n;
			if ((re4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("RE5") && actualNote.equals("DO#5")) {
			x = (re5Frequency - doD5Frequency) / n;
			if ((re5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("RE6") && actualNote.equals("DO#6")) {
			x = (re6Frequency - doD6Frequency) / n;
			if ((re6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("RE7") && actualNote.equals("DO#7")) {
			x = (re7Frequency - doD7Frequency) / n;
			if ((re7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// RE# - RE
		else if (previusNote.equals("RE#4") && actualNote.equals("RE4")) {
			x = (reD4Frequency - re4Frequency) / n;
			if ((reD4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("RE#5") && actualNote.equals("RE5")) {
			x = (reD5Frequency - re5Frequency) / n;
			if ((reD5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("RE#6") && actualNote.equals("RE6")) {
			x = (reD6Frequency - re6Frequency) / n;
			if ((reD6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("RE#7") && actualNote.equals("RE7")) {
			x = (reD7Frequency - re7Frequency) / n;
			if ((reD7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// MI - RE#
		else if (previusNote.equals("MI4") && actualNote.equals("RE#4")) {
			x = (mi4Frequency - reD4Frequency) / n;
			if ((mi4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("MI5") && actualNote.equals("RE#5")) {
			x = (mi5Frequency - reD5Frequency) / n;
			if ((mi5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("MI6") && actualNote.equals("RE#6")) {
			x = (mi6Frequency - reD6Frequency) / n;
			if ((mi6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("MI7") && actualNote.equals("RE#7")) {
			x = (mi7Frequency - reD7Frequency) / n;
			if ((mi7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// FA - MI
		else if (previusNote.equals("FA4") && actualNote.equals("MI4")) {
			x = (fa4Frequency - mi4Frequency) / n;
			if ((fa4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("FA5") && actualNote.equals("MI5")) {
			x = (fa5Frequency - mi5Frequency) / n;
			if ((fa5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("FA6") && actualNote.equals("MI6")) {
			x = (fa6Frequency - mi6Frequency) / n;
			if ((fa6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("FA7") && actualNote.equals("MI7")) {
			x = (fa7Frequency - mi7Frequency) / n;
			if ((fa7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// FA# - FA
		else if (previusNote.equals("FA#4") && actualNote.equals("FA4")) {
			x = (faD4Frequency - fa4Frequency) / n;
			if ((faD4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("FA#5") && actualNote.equals("FA5")) {
			x = (faD5Frequency - fa5Frequency) / n;
			if ((faD5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("FA#6") && actualNote.equals("FA6")) {
			x = (faD6Frequency - fa6Frequency) / n;
			if ((faD6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("FA#7") && actualNote.equals("FA7")) {
			x = (faD7Frequency - fa7Frequency) / n;
			if ((faD7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// SOL - FA#
		else if (previusNote.equals("SOL4") && actualNote.equals("FA#4")) {
			x = (sol4Frequency - faD4Frequency) / n;
			if ((sol4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL5") && actualNote.equals("FA#5")) {
			x = (sol5Frequency - faD5Frequency) / n;
			if ((sol5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL6") && actualNote.equals("FA#6")) {
			x = (sol6Frequency - faD6Frequency) / n;
			if ((sol6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL7") && actualNote.equals("FA#7")) {
			x = (sol7Frequency - faD7Frequency) / n;
			if ((sol7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// SOL# - SOL
		else if (previusNote.equals("SOL#3") && actualNote.equals("SOL3")) {
			x = (solD3Frequency - sol3Frequency) / n;
			if ((solD3Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL#4") && actualNote.equals("SOL4")) {
			x = (solD4Frequency - sol4Frequency) / n;
			if ((solD4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL#5") && actualNote.equals("SOL5")) {
			x = (solD5Frequency - sol5Frequency) / n;
			if ((solD5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL#6") && actualNote.equals("SOL6")) {
			x = (solD6Frequency - sol6Frequency) / n;
			if ((solD6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SOL#7") && actualNote.equals("SOL7")) {
			x = (solD7Frequency - sol7Frequency) / n;
			if ((solD7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// LA - SOL#
		else if (previusNote.equals("LA3") && actualNote.equals("SOL#3")) {
			x = (la3Frequency - solD3Frequency) / n;
			if ((la3Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA4") && actualNote.equals("SOL#4")) {
			x = (la4Frequency - solD4Frequency) / n;
			if ((la4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA5") && actualNote.equals("SOL#5")) {
			x = (la5Frequency - solD5Frequency) / n;
			if ((la5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA6") && actualNote.equals("SOL#6")) {
			x = (la6Frequency - solD6Frequency) / n;
			if ((la6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA7") && actualNote.equals("SOL#7")) {
			x = (la7Frequency - solD7Frequency) / n;
			if ((la7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// LA# - LA
		else if (previusNote.equals("LA#3") && actualNote.equals("LA3")) {
			x = (laD3Frequency - la3Frequency) / n;
			if ((laD3Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA#4") && actualNote.equals("LA4")) {
			x = (laD4Frequency - la4Frequency) / n;
			if ((laD4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA#5") && actualNote.equals("LA5")) {
			x = (laD5Frequency - la5Frequency) / n;
			if ((laD5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA#6") && actualNote.equals("LA6")) {
			x = (laD6Frequency - la6Frequency) / n;
			if ((laD6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("LA#7") && actualNote.equals("LA7")) {
			x = (laD7Frequency - la7Frequency) / n;
			if ((laD7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}

		// SI - LA#
		else if (previusNote.equals("SI3") && actualNote.equals("LA#3")) {
			x = (si3Frequency - laD3Frequency) / n;
			if ((si3Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SI4") && actualNote.equals("LA#4")) {
			x = (si4Frequency - laD4Frequency) / n;
			if ((si4Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SI5") && actualNote.equals("LA#5")) {
			x = (si5Frequency - laD5Frequency) / n;
			if ((si5Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SI6") && actualNote.equals("LA#6")) {
			x = (si6Frequency - laD6Frequency) / n;
			if ((si6Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		} else if (previusNote.equals("SI7") && actualNote.equals("LA#7")) {
			x = (si7Frequency - laD7Frequency) / n;
			if ((si7Frequency - pitch <= x) && (prevRange <= tollerance)) {
				return getNameMusicNote(pitch, x);
			}
		}
		return null;
	}

	/** RILEVATORE SUONO (SOUND DETECTOR) **/
	@Override
	public boolean process(AudioEvent audioEvent) {
		handleSound();
		return true;
	}

	/** OSCILLOSCOPIO **/
	@Override
	public void handleEvent(float[] data, AudioEvent event) {
		graphPanel.paint(data, event);
		graphPanel.repaint();
	}

	/** GESTIONE POTENZA SUONO (dB) **/
	private boolean handleSound() {
		boolean isGreater = true;
		if (silenceDetector.currentSPL() > threshold) {
			isGreater = true;
		} else {
			isGreater = false;
		}
		soundPanel.addDataPoint(silenceDetector.currentSPL(), System.currentTimeMillis());
		return isGreater;
	}

	@Override
	public void processingFinished() {
		silenceDetector.processingFinished();
	}

	/** PROCESSO DELL'AUDIO DELLO SPETTOGRAMMA **/
	AudioProcessor fftProcessor = new AudioProcessor() {
		FFT fft = new FFT(bufferSize);
		float[] amplitudes = new float[bufferSize / 2];

		@Override
		public void processingFinished() {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean process(AudioEvent audioEvent) {
			float[] audioFloatBuffer = audioEvent.getFloatBuffer();
			float[] transformbuffer = new float[bufferSize * 2];
			System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
			fft.forwardTransform(transformbuffer);
			fft.modulus(transformbuffer, amplitudes);
			spectogramPanel.drawFFT(pitch, amplitudes, fft);
			spectogramPanel.repaint();
			return true;
		}
	};
}
