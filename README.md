# GitHub Repo Viewer

![App Logo](assets/logo.png)

An Android application built with **Kotlin** and **Jetpack Compose**, designed to provide a sleek and responsive UI for searching GitHub users, viewing their repositories, and accessing key details like README content, stars, forks, and profile info — with full backend integration via the GitHub API and support for GitHub OAuth login.

---

## Features

-  **Search GitHub Users** by username
-  **List Public Repositories** of any user
-  **Real-Time Search** with debounce
-  **Pull-to-Refresh** on user result screen
- ️ **User Profile Info**: name, avatar, bio
-  **Repository Detail Screen**:
  - Owner info, stars, forks, language
  - Full **README** rendering (Markdown supported)
  - Open repo directly in browser
-  **GitHub OAuth Login** using a secure backend
-  **Global Stats** (total repos & users)
-  **Trending Topics** and **Top Languages**
-  **Developer Fun Facts** displayed on dashboard
-  Graceful **Error Handling** and fallback UIs
-  Built entirely with **Jetpack Compose UI**

---

##  Tech Stack

- **Kotlin** + **Jetpack Compose**
- **MVVM Architecture**: ViewModel + StateFlow
- **Retrofit** + **Gson** (API integration)
- **Coil** (image loading)
- **Navigation Component** (NavGraph)
- **Material3** (polished UI with themes)
- **Accompanist FlowRow** (for dynamic layouts)

---

##  Backend OAuth Integration

The app uses a secure OAuth proxy for GitHub login:

 **OAuth Proxy Backend:**  
[https://github-oauth-proxy-uuuo.onrender.com](https://github-oauth-proxy-uuuo.onrender.com)

- GitHub login handled via a hosted FastAPI proxy
- Returns access token after redirect (secure exchange)
- Used to fetch authenticated user profile securely

---

##  GitHub API Endpoints Used

Uses [GitHub REST API v3](https://docs.github.com/en/rest):

- `GET /users/{username}` → Get user profile
- `GET /users/{username}/repos` → List user's public repos
- `GET /repos/{owner}/{repo}/readme` → Fetch README file
- `GET /rate_limit` → Detect API rate limit status

---

##  Screens Implemented

- **LoginScreen**: OAuth GitHub login
- **DashboardScreen**:
  - Search Bar, Stats, Top Languages, Fun Facts, Trending Topics
- **UserResultScreen**: User info + repo list
- **RepoDetailScreen**: Full metadata + README
- **UserProfileScreen**: Displays logged-in user

---

##  Project Structure

```bash
├── model/              # Data classes (User, Repo, Owner)
├── screens/            # UI screens (Login, Dashboard, UserResult, RepoDetail)
├── viewmodel/          # GitHubViewModel for state handling
├── repository/         # GitHubRepository handles API logic
├── data/               # Retrofit setup and API interface
├── navigation/         # AppNavGraph and screen routes
├── util/               # TokenStore and helpers
├── MainActivity.kt     # App entry point
└── assets/logo.png     # App logo for branding

```

##  Setup Instruction

```bash
git clone https://github.com/Mahendra-MR/GitHubRepoViewer.git
cd GitHubRepoViewer
```
Open the project in Android Studio, sync Gradle, and run the app on emulator or device.