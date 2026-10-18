# PromptVault 🌌 — AI Image & Prompt Gallery

A production-ready Android application (Jetpack Compose, Clean MVVM, 16 KB Page-Size Compliant) powered by remote GitHub JSON and a static GitHub Pages Admin Panel.

---

## 📱 Features

- **Stunning UI/UX**: Ultra-modern deep space theme with violet-to-cyan gradients, smooth spring animations, glassmorphic headers and cards.
- **Hidden Signature Prompts**: Prompts remain hidden until tapping **View Prompt**, revealing a typewriter animation popup with 1-tap clipboard copying.
- **Rewarded Ad Unlocks**: Premium prompts can be unlocked for 48 hours by watching a short Rewarded Video Ad (anti-tamper clock rollback verification included).
- **100% Remote AdMob Control**: Banner, Interstitial, Native, and Rewarded ads are configured and remotely toggled directly via `app_config.json`.
- **Offline-First & Fast**: DataStore caching + ETag support + pull-to-refresh with query cache-busters.
- **Dual Language**: English & বাংলা (Bangla) strings.
- **Static Web Admin Panel**: Hosted on GitHub Pages with direct GitHub Contents REST API sync and automatic client-side WebP image compression.

---

## 🚀 Setup Guide (English)

### 1. Create Your GitHub Repository
1. Create a **public** repository on GitHub named `PromptVault`.
2. Push this project code to your repository:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of PromptVault"
   git remote add origin https://github.com/<YOUR_USERNAME>/PromptVault.git
   git branch -M main
   git push -u origin main
   ```
3. Enable **GitHub Pages**:
   - Go to **Settings** → **Pages**.
   - Under **Build and deployment**, select **Deploy from a branch**.
   - Branch: `main` / Folder: `/admin` (or root).
   - Click **Save**. Your Admin Panel will be live at `https://<YOUR_USERNAME>.github.io/PromptVault/admin/`.

### 2. Generate a Fine-Grained GitHub Personal Access Token (PAT)
1. Go to **GitHub Settings** → **Developer Settings** → **Personal Access Tokens** → **Fine-grained tokens**.
2. Click **Generate new token**.
3. Under **Repository access**, choose **Only select repositories** and pick `PromptVault`.
4. Under **Permissions**, select:
   - **Repository permissions** → **Contents** → **Read and write**.
5. Generate and copy the token.
6. Open your live Admin Panel URL, enter your Username, Repo Name, and Token to connect!

### 3. Configure the Android App
1. Open `app/build.gradle.kts`:
   - Replace the `BASE_URL` with your repository URL:
     ```kotlin
     buildConfigField("String", "BASE_URL", "\"https://raw.githubusercontent.com/<YOUR_USERNAME>/PromptVault/main/\"")
     ```
2. Open `app/src/main/AndroidManifest.xml`:
   - Replace the sample AdMob Application ID with your real AdMob ID before releasing:
     ```xml
     <meta-data
         android:name="com.google.android.gms.ads.APPLICATION_ID"
         android:value="ca-app-pub-YOUR_PUBLISHER_ID~YOUR_APP_ID" />
     ```
3. Update `app_config.json`:
   - In production, set `"use_test_ads": false` and add your real AdMob ad unit IDs.

---

## 🛡️ 16 KB Page-Size Compliance Verification

Google Play requires all Android apps with native libraries to support 16 KB memory page sizes.

### How to Verify:
1. Build your release APK:
   ```bash
   ./gradlew assembleRelease
   ```
2. Run `zipalign` verification (located in `Android/sdk/build-tools/<version>/`):
   ```bash
   zipalign -c -P 16 -v 4 app/build/outputs/apk/release/app-release-unsigned.apk
   ```
   *Result: `Verification successful` indicates 16 KB page-size compliance.*
3. Open **Android Studio** → **Build** → **Analyze APK...** → select your APK and inspect any `.so` libraries to verify page alignment.

---

## 🇧🇩 সেটআপ গাইড (বাংলা সারসংক্ষেপ)

১. **গিটহাব রিপো তৈরি**:
   - GitHub এ একটি পাবলিক রিপো তৈরি করুন এবং কোড পুশ করুন।
   - রিপো সেটিংস থেকে GitHub Pages চালু করুন (ফোল্ডার: `/admin`)।
   - এডমিন প্যানেল সরাসরি আপনার ব্রাউজার থেকে চলবে: `https://<USERNAME>.github.io/PromptVault/admin/`

২. **টোকেন তৈরি**:
   - GitHub Settings -> Developer settings -> Fine-grained tokens এ গিয়ে `PromptVault` রিপোর জন্য **Contents: Read and write** পারমিশন দিয়ে টোকেন তৈরি করুন।
   - এডমিন প্যানেলে ইউজারনেম, রিপো নাম ও টোকেন দিয়ে কানেক্ট করুন।

৩. **Android অ্যাপ কনফিগারেশন**:
   - `app/build.gradle.kts` ফাইলে `BASE_URL` এ আপনার গিটহাব ইউজারনেম দিন।
   - `AndroidManifest.xml` ফাইলে আপনার AdMob App ID বসান।
   - `app_config.json` দিয়ে রিমোটলি যেকোনো সময় বিজ্ঞাপন চালু/বন্ধ এবং প্রম্পট আনলক সময় নিয়ন্ত্রণ করতে পারবেন।
