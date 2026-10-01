# Spotify Interactive Desktop System (LDCW6123)

A Java Swing Desktop GUI Application developed for the **LDCW6123 - Fundamentals of Digital Competence for Programmer** group project (Multimedia University). The system connects to the group's research on **Spotify** modeled through **Brian Winston's Innovation Lifecycle Model**.

---

## 📂 Folder Structure

```
Digital Comp/
│
├── Launch_Spotify_App.bat        # 1-Click build & launch launcher
├── run.bat                       # Quick runner
├── view_git_log.bat              # Displays and exports git log trace
│
├── src/
│   └── SpotifySystem.java        # Main Java Swing application source code
│
├── bin/                          # Compiled Java class files (.class)
│
├── Spotify Dataset/
│   └── train.csv                 # Primary Spotify Kaggle dataset (10,000+ tracks)
│
├── data/
│   ├── dataset.csv               # Fallback / sample track dataset
│   └── git_log_output.txt        # Exported git development history trace
│
├── docs/
│   ├── README.md                 # Complete project documentation & guide
│   └── LDCW6123_Group Project_T2620.docx  # Assignment guidelines & marking rubric
│
└── Group_Reports/                # Part 1 Team research reports & poster slides
    ├── Spotify_Ideation_Report_.docx
    ├── Prototype.docx
    ├── Supervening Social Necessity and Invention.docx
    ├── law of suppression + difussion.docx
    └── LCDW6123_PROJECT_GROUP_FCI7_GROUP1_253UC2568R.pptx
```

---

##  How to Run the Application

### Option 1: 1-Click Launch (Recommended)
Double-click `Launch_Spotify_App.bat` in the project root. It will automatically compile `src/SpotifySystem.java` and start the GUI application.

### Option 2: Command Prompt / PowerShell
Navigate to this folder and execute:
```cmd
javac -d bin src\SpotifySystem.java
java -cp bin SpotifySystem
```

---

##  Key Application Features

1. **Music Recommendation Assistant**:
   - Filter tracks by audio genre (Pop, Rock, Hip-Hop, Acoustic, Jazz, Electronic, R&B, K-Pop, etc.).
   - **Numbered Popularity Phase Buttons**: `All`, `0-20`, `20-40`, `40-60`, `60-80`, and `80-100+`.
   - Real-time search by track title, artist name, or album.
   - "Surprise Me" instant recommendation generator.
   - Graphical Popularity Bar Pill renderer with live average score calculations.

2. **Subscription Fare Calculator**:
   - 4 Interactive Plan Tiers: **Student** ($5.99), **Individual** ($10.99), **Duo** ($14.99), and **Family** ($16.99).
   - Billing Frequencies: Monthly vs. Annual (includes 16.6% annual savings / 2 months free).
   - Multi-Currency Conversion: USD ($), EUR (€), GBP (£), and MYR (RM).
   - Optional Add-ons: Hi-Fi Lossless Audio Upgrade (+15%), Extra Offline Storage (+$1.99/mo).
   - Voucher / Promo Support: `STUDENT10` provides an additional 10% discount.
   - Modern HTML Itemized Invoice breakdown with live SST/tax calculations and per-user cost callouts.
   - Interactive Checkout Simulation.

3. **Track Dataset Explorer**:
   - Browse thousands of loaded tracks with metadata (ID, Title, Artist, Album, Genre, Popularity, Duration).
   - "Add New Track" dialog with input validation (0-100 popularity constraints and positive duration).
   - Live reload feature to re-sync dataset from disk.

4. **Innovation Timeline (Brian Winston's Model)**:
   - Interactive visual mapping of Spotify's evolution through all 6 stages:
     1. *Scientific Competence* (1990s: MP3 & streaming foundations)
     2. *Ideation* (2006: Daniel Ek's legal streaming vision)
     3. *Prototype* (2006-2007: Sub-second desktop client playback)
     4. *Supervening Social Necessity* (Mid-2000s: Piracy crisis & consumer shift)
     5. *Invention* (2008: European launch with major label licensing)
     6. *Diffusion & Suppression* (2008-Present: 180+ markets, podcasts, label caps)
   - Synchronized detail card showing comprehensive history and theoretical key insights.

---

##  Git Version Control & Rubric Trace

To regenerate the `git_log_output.txt` required for the submission PDF:
```cmd
view_git_log.bat
```
Or run directly:
```bash
git log --oneline --graph
```

---
