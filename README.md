# Violin Pitch Detector

### This detector allows you to write a score based on what you play, allowing you to understand how accurately a note was played, with the vote at the end of the performance.
<br/>

The code you can download is the result of my thesis in musinformatics presented by me at the Bachelor's degree of the "_University of Salerno_". This is a **_classical violin tone detector_**.

I am a _violinist_ and during the years of studies I could understand that when you play a sheet music you make notes whose sound is not always perfectly precise and this is more evident when you are accompanied with the piano, or you play octaves.
This led to the design of a detector, whose function is to minimize these small inaccuracies.
The detector is a program that runs on a computer and uses the microphone to capture the note played by the violin and translate its frequency and its execution time into the note on the score with the relative precision.
<br/><br/>

The "**Violin Pitch Detector**" program was essentially created for two reasons: 
- Recognize the notes, so both the duration and the height, to rewrite a score to Run Time based on the instrument, in this case the classical violin; 
- Improve the accuracy of the notes for a violinist;

This program turns out to be interesting given its double utility, both for those who approach the world of the violin and who begin to learn the correspondence between the notes on the stave and the correct positions of the fingers on the strings for the right pitch, both for those who they are more experienced to perfect the pitch (especially for those who do not have the absolute pitch). Obviously, this program does not want to replace the classic exercise of listening to the note and other similar exercises for the memorization of the correct intonation.

Moreover, another reason that led to its creation is to improve and reduce any existing problems such as the recognition of notes based on the musical instrument: each has its own stamp and this characterizes it and the difference compared to any other instrument and that is why a "universal" operation on any instrument cannot be precise enough either for normal operation, but especially in case there may be general noise.
<br/><br/>

## Manual
The user must set simple parameters in the following order:

- in the **PANEL 1** must _select the microphone_: the first selectable is the default one that is chosen on your operating system;
- in the **PANEL 2** _must not select anything_ (currently): since the most appropriate algorithm for the violin is the one already pre-selected, the others have been disabled.
- in the **PANEL 3** must _select the material of the strings of the violin_;
- in the **PANEL 4** must set the minimum listening threshold that corresponds to a filter for the disturbances; can do it in two ways:
  - **_Manually_** moving the slider according to your needs. You can help by observing the **PANEL 11**: the yellow line represents the minimum threshold and everything that will be found below this line will be considered disturbed and ignored by the listener; what will be found above this line, on the other hand, will be accepted;
  - **_Automatically_** by pressing the appropriate button inside the PANEL and then follow the instructions on the screen;
- in the **PANEL 5** must _set the quality of listening_ which corresponds to a ratio between the probability of recognition and disturbances; can do it in two ways:
  - **_Manually_** moving the slider according to your needs;
  - **_Automatically_** in turn subdivided into two other ways:
    - **_Automatically 1_** leaving the default value relative to the selected string;
    - **_Automatically 2_** by pressing the appropriate button inside the PANEL and then follow the instructions on the screen;
_If you want to return to the default values_ ??of the selected string **you must** necessarily press the return button to the default values.
- in the **PANEL 6** must _set the execution speed_ manually; this parameter serves the program to correctly identify the real duration of a performed note.
- in the **PANEL 7** must select the time of the song to be executed in manual mode; this parameter serves the program to correctly identify the maximum duration of a note per bar.
  - _it is good practice to tune the instrument through **the tuner** which can be started from **PANEL 9**._
- After executing the following procedure, the user _to understand if the program works correctly on your device_, must observe the **PANEL 12**: if when it sounds, the graph marks the _**continuous** red dots_, then the program it works correctly, otherwise it will not work properly.
- At this point the user _can start the listener through the button_ in the **PANEL 8** and _to stop it, there is a button on the listening window for the **"STOP"**_.
<br/><br/>

## Credits
https://github.com/JorenSix/TarsosDSP
<br/><br/>

## Source Code Organization
The source code organization is the same of the project TarsosDSP, metioned previously in the credits. 
In addition in src/examples I add the main program with some panels.
<br/><br/>

## Donation
If you like this project support me: <br/><br/>
[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.me/ClaudioAmato1)