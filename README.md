# GOTronome
Lightweight metronome app in Jetpack Compose with Oboe for real-time audio

The app is on Play Store: https://play.google.com/store/apps/details?id=com.pdp.gotronome&pcampaignid=web_share

This Android app is a simple metronome espressly designed for Band rehearsals: beats are shown in big bright visuals, so that all band members can see them and play in sync. This is especially useful when the sound of the band makes it difficult to hear the metronome's beats.

<img width="1024" height="500" alt="feature" src="https://github.com/user-attachments/assets/eeee577c-9c7a-422c-a454-978fd5474275" />
<img width="250" height="512" alt="mobile05" src="https://github.com/user-attachments/assets/d673ec28-7853-4569-b7d5-414a5f7b197f" />


GOTronome is implemented in Jetpack Compose and uses Oboe for real-time audio. It is lightweight and simple.
Oboe is accessed through JNI. Here's a simple diagram that illustrates the architevture:

![image](https://github.com/user-attachments/assets/7551b739-2e54-4a11-a286-216cc80a3be1)


To compile the app, first get Oboe (https://github.com/google/oboe), then update CMakeLists.txt to point to your local Oboe directory

