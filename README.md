# ScreenSense

<p align="center">
  <img src="https://i.postimg.cc/LswZkpTS/111445-6a82248358825.png" alt="ScreenSense Cover Banner" width="100%" />
</p>

<p align="center">
  <a href="https://screensense-ljw7fuw2.manus.space/">
    <img src="https://img.shields.io/badge/Download%20APK-ScreenSense-FF4081?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
  </a>
</p>

> **Built for CS Girlies Annual Hackathon — Technology For Wellness**  
> *Smart, friendly digital wellness for Android*

[![CS Girlies Hackathon](https://img.shields.io/badge/CS%20Girlies%20Annual%20Hackathon-Technology%20For%20Wellness-FF69B4?style=flat-square&logo=sparkles&logoColor=white)](https://github.com/)
[![Built in 48 Hours](https://img.shields.io/badge/Sprint-48%20Hours-FF4081?style=flat-square)](https://github.com/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)


## Inspiration

Most screen time and wellness apps either force you to log everything manually or guilt-trip you with alarming red charts. Staring at a high screen time number feels discouraging, even when most of those hours were spent coding, studying, working, or staying connected. Screen time is not inherently bad, and seeing a larger number shouldn't damage your mindset.

We built ScreenSense to automate digital wellness through an easy-to-use Android APK. It automatically categorizes your usage based on what you are actually doing and suggests quick 1- to 3-minute physical breaks without any judgment or guilt.


## What ScreenSense Does

* **Automatic Tracking**: Safely reads daily usage statistics directly on your device without requiring manual logging.
* **Smart App Categorization**: Organizes apps into clear buckets such as Development & Coding, Work & Study, Tools, and Relaxation & Entertainment.
* **Gemini AI Suggestions**: Analyzes usage patterns to suggest quick 1- to 3-minute habits, such as the 20-20-20 eye rest rule, posture stretches, or brief breathing breaks.
* **Habits & Small Wins**: Encourages healthy daily routines (e.g., No Screen 1H Before Bed, Morning Water Hydration, Daily 10-Min Sunlight, Cold Shower) with built-in timers and streak protection that does not punish you for missing a day.
* **Daily Motivation Hub**: Features swipeable affirmations and reflections focused on circadian balance, eye health, and mindful focus.


---

## How We Used AI

"In Manus 1.6 by Meta, we called multiple agents (UI Designer, Senior Android Developer, and Wellness Expert) to generate a detailed master prompt covering Android screen time tracking, categorization, healthy habits (No Screen 1H Before Bed, morning water, 10-min sunlight, cold shower), motivation quotes, and streak tracking. We then used that generated prompt in Google AI Studio to build the native Android app. Finally, we used live screenshare with Gemini to walk through all app screens and get real-time UI/UX improvements and design suggestions."

---

## Challenges We Ran Into

* **Launcher Noise Filtering**: Android's `UsageStatsManager` frequently logs system home launchers as active foreground screen time. We built custom package filtering logic to strip out idle launcher time while keeping genuine tool usage intact.
* **Offline Architecture**: Built local rule-based fallback engines ensuring habit reminders and scoring remain functional without internet access or API connectivity.


## Accomplishments

* Shipped a functional, feature-complete Android APK within a 48-hour hackathon 

## How to Try It

### Download & Install the APK

<p align="left">
  <a href="https://screensense-ljw7fuw2.manus.space/">
    <img src="https://img.shields.io/badge/Download%20APK-Direct%20Link-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Download APK Link" />
  </a>
</p>

2. Open the downloaded `.apk` file on your Android device and install it.

