# GOTronome - Metronome and Rhythm trainer

(iOS version here: https://github.com/depasca/iGOTronome)

A precision metronome designed for real musicians, rehearsals, and skill training — available on Android and iOS.

GOTronome is an ultra-responsive visual metronome built for band practice, silent rhythm training, and phrase looping.
It uses native audio engines (Oboe on Android, AudioUnit on iOS) for sample-accurate timing and a custom rendering loop for perfectly synchronized visuals.

<img width="512" height="250" alt="feature" src="https://github.com/user-attachments/assets/eeee577c-9c7a-422c-a454-978fd5474275" /> <img width="250" height="512" alt="mobile05" src="https://github.com/user-attachments/assets/d673ec28-7853-4569-b7d5-414a5f7b197f" />

🚀 Features
🎚️ 1. Designed for Band Practice

In loud rehearsals, traditional click tracks get lost.
GOTronome gives you big, bright, color-changing beats that the whole group can see at a glance.

Every beat flashes with a clear visual pulse

First beat is highlighted

Works from across the room — perfect for live band sessions

Also great for drummers who want visual timing cues

🤫 2. Silent Bar Mode — Train Your Internal Clock

Can you keep the tempo when the metronome drops out?

Silent Bar Mode lets you mute a chosen number of bars so you can test and improve your internal timing:

Choose how many bars go completely silent

Dramatically improves timing accuracy and feel

Perfect for:

Rhythm training

Practicing steadiness without depending on clicks

🔁 3. Bar Loop Mode — Practice Phrases & Solos

Need to clean up a tricky lick or a short solo section?

Bar Loop Mode shows both the beat number and the current bar so you always know where you are in a phrase.

Just set:

BPM

Beats per bar

Number of bars in the phrase

And the metronome loops that phrase over and over — no mental counting required.

Great for:

Jazz & fusion licks

Drum fills

Polyrhythm practice

Learning complex musical phrases

🛠️ Technical Highlights

Perfect audiovisual sync using a real-time rendering loop

Native audio engines

Android → Oboe

iOS → AudioUnit with fast C backend

Ultra-low latency

No jitter between sound and visuals

Lightweight codebase with clear separation between UI and audio engine

100% open source

📲 Download

Android: Google Play Store https://play.google.com/store/apps/details?id=com.pdp.gotronome&pcampaignid=web_share

iOS: App Store https://apps.apple.com/us/app/gotronome/id6755876341

(Links coming soon)

🤝 Contributing

Bug reports, feature requests, and pull requests are welcome!
Feel free to open a discussion if you want to propose larger architectural changes.

📄 License

MIT License — see LICENSE
 for details.
 
GOTronome is implemented in Jetpack Compose and uses Oboe for real-time audio. It is lightweight and simple.
Oboe is accessed through JNI. Here's a simple diagram that illustrates the architecture:

![image](https://github.com/user-attachments/assets/7551b739-2e54-4a11-a286-216cc80a3be1)

To compile the app, first get Oboe (https://github.com/google/oboe), then update CMakeLists.txt to point to your local Oboe directory

