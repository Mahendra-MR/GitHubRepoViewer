# GitHub Repo Viewer

![App Logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

A Android application built with **Kotlin** and **Jetpack Compose** that allows users to search GitHub profiles and view their repositories, complete with repo details, live search, pull-to-refresh, and README rendering.

---

## Features

- **Search GitHub Users** by username
- **Display Public Repositories**
- **Real-Time Search** with debounce
- **Pull-to-Refresh** functionality
- **User Profile Info** (name, avatar)
- **Repository Detail Screen**:
    - Owner info, stars, forks
    - Full **README** rendering (fallback when no description)
    - Clickable link to open repo on GitHub
- Graceful **Error Handling**
- Responsive layout with **Jetpack Compose**


## Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **ViewModel + StateFlow**
- **Retrofit + Gson**
- **Coil (for image loading)**

---

## API Reference

Uses [GitHub REST API v3](https://docs.github.com/en/rest).

- `GET /users/{username}` → Fetch user profile
- `GET /users/{username}/repos` → Fetch public repos
- `GET /repos/{owner}/{repo}/readme` → Fetch repo README

---

## Setup Instructions

```bash
git clone https://github.com/Mahendra-MR/GitHubRepoViewer.git
cd GitHubRepoViewer
```

## Project Structure

```bash
├── model/              # Data classes (User, Repo, Owner)
├── screens/            # UI screens (MainScreen, RepoDetailScreen)
├── viewmodel/          # GitHubViewModel with state and logic
├── repository/         # GitHubRepository handles API calls
├── data/               # Retrofit client and API interface
├── navigation/         # AppNavGraph with navigation setup
└── MainActivity.kt     # App entry point
```