# GOTronome
Lightweight metronome app in Jetpack Compose with Oboe for real-time audio


This Android app is a simple metronome espressly designed for Band rehearsals: beats are shown in big bright visuals, so that all band members can see them and play in sync. This is especially useful when the sound of the band makes it difficult to hear the metronome's beats.

GOTronome is implemented in Jetpack Compose and uses Oboe for real-time audio. It is lightweight and simple.
Oboe is accessed through JNI

![image](https://github.com/user-attachments/assets/4a3270e4-ee43-4fcf-87d3-035e6cb420a2) ![image](https://github.com/user-attachments/assets/7a8e5bc4-634d-4de8-a6e8-a3517dea2c7e) ![image](https://github.com/user-attachments/assets/43e677e4-7c3b-4765-b554-6c5ecb04c4a5)


To compile the app, first get Oboe (https://github.com/google/oboe), then update CMakeLists.txt to point to your local Oboe directory
