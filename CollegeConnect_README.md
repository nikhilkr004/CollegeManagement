
# CollegeConnect ERP 📱🎓

![React Native](https://img.shields.io/badge/React%20Native-0.72-blue)
![Firebase](https://img.shields.io/badge/Firebase-9.0-orange)
![Status](https://img.shields.io/badge/Status-Beta-yellow)

**CollegeConnect ERP** is a mobile-first ERP solution designed for colleges. It offers seamless attendance tracking, intelligent timetable management, collaborative note sharing, and gamified learning features.

---

## ✨ Key Features

### 📊 Attendance System

- Real-time updates on student presence
- Faculty approval workflows for manual entries



### 📚 Note Sharing Network
- Upload and share notes (PDFs/images)
- Peer-rated notes (1 to 5 stars)
- Search by topic tags (e.g., `#math`, `#physics`)

### 🎮 Quiz & Badges
- Daily quizzes across difficulty levels
- Achievement badges to motivate learning
- Public leaderboard for competitive edge

---

## 💻 Tech Stack

**Frontend**:
- XML
- Kotlin


**Backend**:
- Firebase (Authentication, Firestore, Storage)
- Firebase Cloud Functions


---

## ⚙ Installation

### Prerequisites
- A Firebase project with Firestore & Auth enabled
- Android/iOS simulator or a physical device

### Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/nikhilkr004/CollegeManagement
   cd college-connect
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Configure Firebase**:  
   Create a file named `firebase-config.js` in the root directory:

   ```javascript
   export default {
     apiKey: "YOUR_KEY",
     authDomain: "YOUR_APP.firebaseapp.com",
     projectId: "YOUR_APP",
     storageBucket: "YOUR_APP.appspot.com",
     messagingSenderId: "SENDER_ID",
     appId: "APP_ID"
   };
   ```

4. **Run the app**:
   ```bash
   expo start
   ```

---

## ⚡ Configuration

You can customize app behavior via `app/config.js`:

```javascript
export default {
  ATTENDANCE_TIME_WINDOW: 15, // minutes
  MAX_NOTE_SIZE: 10, // MB
  QUIZ_REWARD_POINTS: {
    EASY: 10,
    MEDIUM: 20,
    HARD: 50
  }
};
```

---

## 📱 Build Instructions

### Android
```bash
expo build:android -t apk
```

### iOS
```bash
expo build:ios
```

---

## 🖼 App Screenshots

_Coming soon..._

---

## 🤝 Contributing

Feel free to fork the repository and submit a pull request. Open issues if you spot bugs or have feature suggestions!

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).