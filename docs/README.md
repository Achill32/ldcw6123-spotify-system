# Spotify Interactive Desktop System (LDCW6123)

A Java Swing Desktop GUI Application developed for the LDCW6123 group project. The system simulates core Spotify mechanics including a **Music Recommendation Assistant** and a **Subscription Fare Calculator**.

---

## Features

1. **Music Recommendation Assistant**:
   - Filter tracks by music genre (Pop, Rock, Hip-Hop, Acoustic, Jazz, Electronic, R&B, K-Pop, etc.).
   - Filter by minimum popularity rating slider.
   - Live search bar for track title, artist, or album.
   - "Surprise Me!" random track selector.
   - Formatted track table displaying title, artist, album, genre, popularity rating, and duration.

2. **Subscription Fare Calculator**:
   - Account Tiers: **Student** ($5.99), **Individual** ($10.99), **Duo** ($14.99 - 2 accounts), and **Family** ($16.99 - 6 accounts).
   - Billing Cycles: Monthly vs. Annual (includes 16.6% annual savings discount / 2 months free).
   - Multi-Currency Conversion: USD ($), EUR (€), GBP (£), and MYR (RM).
   - Add-on Features: Hi-Fi Lossless Audio Upgrade (+15%), Extra Multi-Device Storage ($1.99).
   - Promo Code support (`STUDENT10` for 10% off).
   - Live Invoice Summary displaying base rate, discounts, SST/VAT tax, total fare, and per-user cost breakdown.

3. **Track Dataset Explorer & CSV Loader**:
   - Reads `dataset.csv` matching Kaggle Spotify track dataset formatting.
   - Dynamic track insertion modal ("Add New Track").
   - Reload CSV dataset feature.

---

## Requirements & Running Instructions

### System Requirements
- Java Development Kit (JDK 8 or higher)
- Operating System: Windows, macOS, or Linux

### How to Compile & Run in Command Prompt / Terminal

1. Open **Command Prompt** or **PowerShell** and navigate to this folder:
   ```cmd
   cd "C:\Users\ASUS\Documents\Digital Comp"
   ```

2. Compile the Java source file:
   ```cmd
   javac Draft.java
   ```

3. Run the compiled application:
   ```cmd
   java Draft
   ```

---

## Git Version Control & Commit Trace (Rubric Requirement)

To generate the required development commit trace for your final PDF report submission:

1. Initialize Git in this folder (if not already initialized):
   ```bash
   git init
   ```

2. Perform regular commits with clear messages:
   ```bash
   git add dataset.csv
   git commit -m "docs: add Kaggle Spotify dataset CSV"

   git add Draft.java
   git commit -m "feat: implement Spotify dark GUI interface in Java Swing"

   git add README.md
   git commit -m "docs: add project execution guide and rubric documentation"
   ```

3. Run the command required by the rubric to view/export your development trace:
   ```bash
   git log --oneline --graph
   ```

---

## Report Screenshots Checklist

For your final PDF report compilation (`Ldcw6123_project_[section]_[leadername] ([student id]).pdf`):
- [x] Java source code screenshot (`Draft.java`)
- [x] Music Recommendation Assistant GUI screenshot
- [x] Subscription Fare Calculator GUI screenshot (with calculated invoice)
- [x] Dataset Explorer GUI screenshot
- [x] Git log terminal output screenshot (`git log --oneline --graph`)
