# CollegeConnect ERP 📱🎓

![React Native](https://img.shields.io/badge/React%20Native-0.72-blue)
![Firebase](https://img.shields.io/badge/Firebase-9.0-orange)
![Status](https://img.shields.io/badge/Status-Beta-yellow)

A mobile-first ERP solution for colleges with attendance tracking, timetable management, collaborative note sharing, and gamified learning.

## Key Features ✨

### 📊 Attendance System
- Facial recognition attendance
- Real-time status updates
- Faculty approval workflows

### 🕒 Smart Timetable
- Dynamic schedule views
- Class reminders
- Room change notifications

### 📚 Note Sharing Network
- PDF/Image uploads
- Peer ratings (1-5 stars)
- Topic-based search (#math #physics)

### 🎮 Quiz & Badges
- Daily challenge quizzes
- Achievement badges
- Leaderboard rankings

## Tech Stack 💻

*Frontend*:
- React Native (Expo)
- Redux Toolkit
- NativeBase UI

*Backend*:
- Firebase (Auth, Firestore, Storage)
- Cloud Functions
- Redis caching

## Installation ⚙

### Prerequisites
- Node.js 18+
- Expo CLI (npm install -g expo-cli)
- Firebase account
- Android/iOS simulator or device

### Setup
1. Clone repo:
   ```bash
   git clone https://github.com/yourrepo/college-connect.git
   cd college-connect
2. Install dependencies:
    ```bash
    npm install
3. Firebase setup:

Create firebase-config.js in root:
```bash
    export default {
  apiKey: "YOUR_KEY",
  authDomain: "YOUR_APP.firebaseapp.com",
  projectId: "YOUR_APP",
  storageBucket: "YOUR_APP.appspot.com",
  messagingSenderId: "SENDER_ID",
  appId: "APP_ID"
};

4. Run app:
```bash
    expo start