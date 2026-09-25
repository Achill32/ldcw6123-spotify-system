import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.text.DecimalFormat;

/**
 * Spotify Interactive System - LDCW6123 Group Project
 * Complete Desktop GUI Application (Java Swing)
 * 
 * Features:
 * 1. Music Recommendation Assistant (Genre, Popularity, Search, CSV Parsing)
 * 2. Subscription Fare Calculator (Student, Individual, Duo, Family, Multi-currency, Tax & Addons)
 * 3. Track Dataset Explorer & Custom Song Manager
 */
public class Draft extends JFrame {

    // --- Color Palette (Spotify Theme) ---
    private static final Color SPOTIFY_GREEN = new Color(29, 185, 84);
    private static final Color DARK_BG = new Color(18, 18, 18);
    private static final Color SIDEBAR_BG = new Color(0, 0, 0);
    private static final Color CARD_BG = new Color(24, 24, 24);
    private static final Color CARD_HOVER_BG = new Color(40, 40, 40);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(179, 179, 179);
    private static final Color ACCENT_GRAY = new Color(50, 50, 50);

    // --- Data Model ---
    public static class SpotifyTrack {
        String id;
        String artist;
        String album;
        String title;
        int popularity;
        int durationMs;
        String genre;

        public SpotifyTrack(String id, String artist, String album, String title, int popularity, int durationMs, String genre) {
            this.id = id;
            this.artist = artist;
            this.album = album;
            this.title = title;
            this.popularity = popularity;
            this.durationMs = durationMs;
            this.genre = genre;
        }

        public String getFormattedDuration() {
            int totalSec = durationMs / 1000;
            int mins = totalSec / 60;
            int secs = totalSec % 60;
            return String.format("%d:%02d", mins, secs);
        }
    }

    private List<SpotifyTrack> trackDatabase = new ArrayList<>();
    private Set<String> availableGenres = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    // --- GUI Components ---
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    // Recommendation Controls
    private JComboBox<String> genreComboBox;
    private JSlider popularitySlider;
    private JTextField searchTextField;
    private DefaultTableModel recTableModel;
    private JLabel recStatusLabel;

    // Calculator Controls
    private JRadioButton studentRadio, individualRadio, duoRadio, familyRadio;
    private JComboBox<String> billingCycleCombo, currencyCombo;
    private JCheckBox hifiCheckBox, offlineCheckBox;
    private JTextField promoTextField;
    private JTextArea invoiceTextArea;
    private JLabel totalCostLabel;

    // Dataset View
    private DefaultTableModel datasetTableModel;
    private JLabel totalTracksLabel;

    public Draft() {
        super("Spotify Interactive System - LDCW6123 Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        // Set global UI defaults for dark theme
        setupDarkThemeDefaults();

        // Load CSV Data from data/dataset.csv or dataset.csv
        loadDataset("data/dataset.csv");

        // Build UI Layout
        initUI();
    }

    private void setupDarkThemeDefaults() {
        UIManager.put("Panel.background", DARK_BG);
        UIManager.put("OptionPane.background", DARK_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_WHITE);
        UIManager.put("Label.foreground", TEXT_WHITE);
    }

    private void initUI() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        // Left Sidebar Navigation
        JPanel sidebar = createSidebar();
        container.add(sidebar, BorderLayout.WEST);

        // Center Content Area (CardLayout)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createRecommendationPanel(), "REC");
        mainContentPanel.add(createCalculatorPanel(), "CALC");
        mainContentPanel.add(createDatasetPanel(), "DATA");

        container.add(mainContentPanel, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = createStatusBar();
        container.add(statusBar, BorderLayout.SOUTH);
    }

    // ==========================================
    // SIDEBAR NAVIGATION
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // Brand Logo & Header
        JLabel logoLabel = new JLabel("Spotify System");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(SPOTIFY_GREEN);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("LDCW6123 Group Project");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_MUTED);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logoLabel);
        sidebar.add(subLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // Navigation Buttons
        JButton navRecBtn = createNavButton("🎵  Recommendation Assistant", "REC");
        JButton navCalcBtn = createNavButton("💳  Subscription Calculator", "CALC");
        JButton navDataBtn = createNavButton("📊  Track Dataset Explorer", "DATA");

        sidebar.add(navRecBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(navCalcBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(navDataBtn);

        sidebar.add(Box.createVerticalGlue());

        // Footer info in sidebar
        JLabel footerInfo = new JLabel("<html><body style='width: 170px; color: #888888; font-size: 10px;'>"
                + "<b>Course:</b> LDCW6123<br>"
                + "<b>Tech:</b> Java Swing GUI<br>"
                + "<b>Version:</b> 2.0 (Interactive)"
                + "</body></html>");
        footerInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footerInfo);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GRAY, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        btn.setMaximumSize(new Dimension(210, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(CARD_HOVER_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SPOTIFY_GREEN, 1),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(CARD_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_GRAY, 1),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
        });

        btn.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));
        return btn;
    }

    // ==========================================
    // PANEL 1: MUSIC RECOMMENDATION ASSISTANT
    // ==========================================
    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Header & Controls Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("Music Recommendation Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel("Filter Spotify songs by your preferred genre, minimum popularity score, or artist name.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);

        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(descLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Control Filters Row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRow.setBackground(CARD_BG);
        filterRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Genre Selector
        filterRow.add(new JLabel("Genre:"));
        Vector<String> genreList = new Vector<>();
        genreList.add("All Genres");
        genreList.addAll(availableGenres);
        genreComboBox = new JComboBox<>(genreList);
        genreComboBox.setPreferredSize(new Dimension(140, 32));
        filterRow.add(genreComboBox);

        // Popularity Slider
        filterRow.add(new JLabel("Min Popularity:"));
        popularitySlider = new JSlider(0, 100, 50);
        popularitySlider.setBackground(CARD_BG);
        popularitySlider.setForeground(SPOTIFY_GREEN);
        popularitySlider.setPreferredSize(new Dimension(130, 40));
        popularitySlider.setMajorTickSpacing(50);
        popularitySlider.setPaintTicks(true);
        popularitySlider.setPaintLabels(true);
        filterRow.add(popularitySlider);

        // Search Input
        filterRow.add(new JLabel("Search:"));
        searchTextField = new JTextField(12);
        searchTextField.setPreferredSize(new Dimension(140, 32));
        filterRow.add(searchTextField);

        // Action Buttons
        JButton filterBtn = new JButton("Search Tracks");
        stylePrimaryButton(filterBtn);
        filterBtn.addActionListener(e -> applyRecommendationFilter());
        filterRow.add(filterBtn);

        JButton surpriseBtn = new JButton("🎲 Surprise Me!");
        styleSecondaryButton(surpriseBtn);
        surpriseBtn.addActionListener(e -> pickRandomRecommendation());
        filterRow.add(surpriseBtn);

        topPanel.add(filterRow);
        panel.add(topPanel, BorderLayout.NORTH);

        // Center Table of Results
        String[] columnNames = {"#", "Track Title", "Artist", "Album", "Genre", "Popularity", "Duration"};
        recTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable recTable = new JTable(recTableModel);
        styleTable(recTable);

        JScrollPane scrollPane = new JScrollPane(recTable);
        scrollPane.getViewport().setBackground(DARK_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Status Info
        recStatusLabel = new JLabel("Ready. Select options above to view recommendations.");
        recStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        recStatusLabel.setForeground(TEXT_MUTED);
        panel.add(recStatusLabel, BorderLayout.SOUTH);

        // Populate initial table
        applyRecommendationFilter();

        return panel;
    }

    private void applyRecommendationFilter() {
        recTableModel.setRowCount(0);
        String selectedGenre = (String) genreComboBox.getSelectedItem();
        int minPopularity = popularitySlider.getValue();
        String searchKeyword = searchTextField.getText().trim().toLowerCase();

        int count = 0;
        int totalPop = 0;

        for (SpotifyTrack track : trackDatabase) {
            boolean genreMatch = "All Genres".equals(selectedGenre) || track.genre.equalsIgnoreCase(selectedGenre);
            boolean popMatch = track.popularity >= minPopularity;
            boolean searchMatch = searchKeyword.isEmpty() ||
                    track.title.toLowerCase().contains(searchKeyword) ||
                    track.artist.toLowerCase().contains(searchKeyword) ||
                    track.album.toLowerCase().contains(searchKeyword);

            if (genreMatch && popMatch && searchMatch) {
                count++;
                totalPop += track.popularity;
                recTableModel.addRow(new Object[]{
                        count,
                        track.title,
                        track.artist,
                        track.album,
                        track.genre.toUpperCase(),
                        track.popularity + " / 100",
                        track.getFormattedDuration()
                });
            }
        }

        double avgPop = count > 0 ? (double) totalPop / count : 0.0;
        recStatusLabel.setText(String.format("Found %d recommendations for genre '%s' (Min Pop: %d) | Avg Popularity: %.1f",
                count, selectedGenre, minPopularity, avgPop));
    }

    private void pickRandomRecommendation() {
        if (trackDatabase.isEmpty()) return;
        Random rand = new Random();
        SpotifyTrack randomTrack = trackDatabase.get(rand.nextInt(trackDatabase.size()));

        recTableModel.setRowCount(0);
        recTableModel.addRow(new Object[]{
                1,
                randomTrack.title,
                randomTrack.artist,
                randomTrack.album,
                randomTrack.genre.toUpperCase(),
                randomTrack.popularity + " / 100",
                randomTrack.getFormattedDuration()
        });

        recStatusLabel.setText("🎲 Surprise Pick: \"" + randomTrack.title + "\" by " + randomTrack.artist + " (" + randomTrack.genre + ")");
    }

    // ==========================================
    // PANEL 2: SUBSCRIPTION FARE CALCULATOR
    // ==========================================
    private JPanel createCalculatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("Subscription Fare Calculator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel("Calculate precise Spotify plan pricing based on account tier, duration, regional taxes, and add-ons.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(descLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Split Input & Output Grid
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setBackground(DARK_BG);

        // Left Panel: Form Controls
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel planHeader = new JLabel("1. Select Plan Tier");
        planHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        planHeader.setForeground(SPOTIFY_GREEN);
        formPanel.add(planHeader);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        ButtonGroup planGroup = new ButtonGroup();
        studentRadio = createRadioButton("Student Tier (1 Account - $5.99/mo)");
        individualRadio = createRadioButton("Individual Tier (1 Account - $10.99/mo)");
        duoRadio = createRadioButton("Duo Tier (2 Accounts - $14.99/mo)");
        familyRadio = createRadioButton("Family Tier (6 Accounts - $16.99/mo)");

        individualRadio.setSelected(true);

        planGroup.add(studentRadio);
        planGroup.add(individualRadio);
        planGroup.add(duoRadio);
        planGroup.add(familyRadio);

        formPanel.add(studentRadio);
        formPanel.add(individualRadio);
        formPanel.add(duoRadio);
        formPanel.add(familyRadio);

        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel cycleHeader = new JLabel("2. Billing Cycle & Region");
        cycleHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cycleHeader.setForeground(SPOTIFY_GREEN);
        formPanel.add(cycleHeader);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel cycleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cycleRow.setBackground(CARD_BG);
        cycleRow.add(new JLabel("Cycle:"));
        billingCycleCombo = new JComboBox<>(new String[]{"Monthly", "Annual (Save 16.6% / 2 Months Free)"});
        cycleRow.add(billingCycleCombo);
        formPanel.add(cycleRow);

        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel currencyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        currencyRow.setBackground(CARD_BG);
        currencyRow.add(new JLabel("Currency:"));
        currencyCombo = new JComboBox<>(new String[]{"USD ($)", "EUR (€)", "GBP (£)", "MYR (RM)"});
        currencyRow.add(currencyCombo);
        formPanel.add(currencyRow);

        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel addonHeader = new JLabel("3. Optional Features & Promo");
        addonHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        addonHeader.setForeground(SPOTIFY_GREEN);
        formPanel.add(addonHeader);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        hifiCheckBox = new JCheckBox("Hi-Fi Lossless Audio Upgrade (+15%)");
        hifiCheckBox.setBackground(CARD_BG);
        hifiCheckBox.setForeground(TEXT_WHITE);

        offlineCheckBox = new JCheckBox("Extra Multi-Device Offline Storage (+$1.99/mo)");
        offlineCheckBox.setBackground(CARD_BG);
        offlineCheckBox.setForeground(TEXT_WHITE);

        formPanel.add(hifiCheckBox);
        formPanel.add(offlineCheckBox);

        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel promoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        promoRow.setBackground(CARD_BG);
        promoRow.add(new JLabel("Promo Code:"));
        promoTextField = new JTextField(8);
        promoRow.add(promoTextField);
        JLabel promoHint = new JLabel("(Try: STUDENT10)");
        promoHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        promoHint.setForeground(TEXT_MUTED);
        promoRow.add(promoHint);
        formPanel.add(promoRow);

        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton calcButton = new JButton("Calculate Total Fare");
        stylePrimaryButton(calcButton);
        calcButton.addActionListener(e -> calculateSubscriptionFare());
        formPanel.add(calcButton);

        centerGrid.add(formPanel);

        // Right Panel: Invoice Summary Card
        JPanel invoicePanel = new JPanel(new BorderLayout(10, 10));
        invoicePanel.setBackground(CARD_BG);
        invoicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel summaryTitle = new JLabel("Fare Breakdown & Invoice");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryTitle.setForeground(TEXT_WHITE);
        invoicePanel.add(summaryTitle, BorderLayout.NORTH);

        invoiceTextArea = new JTextArea();
        invoiceTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        invoiceTextArea.setBackground(DARK_BG);
        invoiceTextArea.setForeground(TEXT_WHITE);
        invoiceTextArea.setEditable(false);
        invoiceTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        invoicePanel.add(new JScrollPane(invoiceTextArea), BorderLayout.CENTER);

        totalCostLabel = new JLabel("Total Fare: $0.00");
        totalCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalCostLabel.setForeground(SPOTIFY_GREEN);
        totalCostLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        invoicePanel.add(totalCostLabel, BorderLayout.SOUTH);

        centerGrid.add(invoicePanel);
        panel.add(centerGrid, BorderLayout.CENTER);

        // Initial Calculation
        calculateSubscriptionFare();

        return panel;
    }

    private JRadioButton createRadioButton(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setBackground(CARD_BG);
        rb.setForeground(TEXT_WHITE);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setFocusPainted(false);
        return rb;
    }

    private void calculateSubscriptionFare() {
        double baseMonthlyPrice = 10.99;
        String tierName = "Individual";
        int accountCount = 1;

        if (studentRadio.isSelected()) {
            baseMonthlyPrice = 5.99;
            tierName = "Student";
            accountCount = 1;
        } else if (duoRadio.isSelected()) {
            baseMonthlyPrice = 14.99;
            tierName = "Duo";
            accountCount = 2;
        } else if (familyRadio.isSelected()) {
            baseMonthlyPrice = 16.99;
            tierName = "Family";
            accountCount = 6;
        }

        boolean isAnnual = billingCycleCombo.getSelectedIndex() == 1;
        double monthsToBill = isAnnual ? 10.0 : 1.0; // 2 months free for annual
        double baseFare = baseMonthlyPrice * monthsToBill;

        // Add-ons
        double hifiFee = hifiCheckBox.isSelected() ? (baseFare * 0.15) : 0.0;
        double offlineFee = offlineCheckBox.isSelected() ? (1.99 * (isAnnual ? 12 : 1)) : 0.0;

        double subtotal = baseFare + hifiFee + offlineFee;

        // Promo Discount
        double promoDiscount = 0.0;
        String promoCode = promoTextField.getText().trim();
        if ("STUDENT10".equalsIgnoreCase(promoCode)) {
            promoDiscount = subtotal * 0.10;
        }

        double discountedSubtotal = subtotal - promoDiscount;

        // Tax (6% estimated SST / VAT)
        double tax = discountedSubtotal * 0.06;
        double totalFare = discountedSubtotal + tax;

        // Multi-currency conversion
        String currencySymbol = "$";
        double exchangeRate = 1.0;
        int currIndex = currencyCombo.getSelectedIndex();
        if (currIndex == 1) { currencySymbol = "€"; exchangeRate = 0.92; }
        else if (currIndex == 2) { currencySymbol = "£"; exchangeRate = 0.79; }
        else if (currIndex == 3) { currencySymbol = "RM"; exchangeRate = 4.70; }

        double finalConvertedTotal = totalFare * exchangeRate;
        double perUserCost = finalConvertedTotal / accountCount;

        DecimalFormat df = new DecimalFormat("0.02");

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("      SPOTIFY SUBSCRIPTION INVOICE       \n");
        sb.append("=========================================\n\n");
        sb.append(String.format("Plan Tier          : %s (%d Account%s)\n", tierName, accountCount, accountCount > 1 ? "s" : ""));
        sb.append(String.format("Billing Cycle      : %s\n", isAnnual ? "Annual (12 Mos / Pay 10)" : "Monthly"));
        sb.append(String.format("Base Plan Rate     : %s%.2f\n", currencySymbol, baseMonthlyPrice * exchangeRate));
        sb.append(String.format("Base Duration Rate : %s%.2f\n\n", currencySymbol, baseFare * exchangeRate));

        sb.append("--- Add-ons & Adjustments ---\n");
        if (hifiCheckBox.isSelected()) {
            sb.append(String.format(" + Hi-Fi Audio (15%s) : %s%.2f\n", "%", currencySymbol, hifiFee * exchangeRate));
        }
        if (offlineCheckBox.isSelected()) {
            sb.append(String.format(" + Extra Storage    : %s%.2f\n", currencySymbol, offlineFee * exchangeRate));
        }
        if (promoDiscount > 0) {
            sb.append(String.format(" - Promo (STUDENT10): -%s%.2f\n", currencySymbol, promoDiscount * exchangeRate));
        }

        sb.append(String.format("Est. Tax (6%% SST)  : %s%.2f\n", currencySymbol, tax * exchangeRate));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL FARE         : %s%.2f %s\n", currencySymbol, finalConvertedTotal, isAnnual ? "/ year" : "/ month"));
        if (accountCount > 1) {
            sb.append(String.format("Cost Per User      : %s%.2f / user\n", currencySymbol, perUserCost));
        }
        sb.append("=========================================\n");

        invoiceTextArea.setText(sb.toString());
        totalCostLabel.setText(String.format("Total: %s%.2f %s", currencySymbol, finalConvertedTotal, isAnnual ? "/yr" : "/mo"));
    }

    // ==========================================
    // PANEL 3: DATASET & TRACK MANAGER
    // ==========================================
    private JPanel createDatasetPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("Spotify Track Dataset Explorer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel("View full loaded dataset, search entries, or add custom tracks dynamically.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);

        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(descLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Button Toolbar
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        toolRow.setBackground(DARK_BG);

        JButton addTrackBtn = new JButton("➕ Add New Track");
        stylePrimaryButton(addTrackBtn);
        addTrackBtn.addActionListener(e -> showAddTrackDialog());
        toolRow.add(addTrackBtn);

        JButton reloadBtn = new JButton("🔄 Reload CSV");
        styleSecondaryButton(reloadBtn);
        reloadBtn.addActionListener(e -> {
            loadDataset("data/dataset.csv");
            refreshDatasetTable();
            applyRecommendationFilter();
            JOptionPane.showMessageDialog(this, "Dataset reloaded successfully from data/dataset.csv!", "Reload Success", JOptionPane.INFORMATION_MESSAGE);
        });
        toolRow.add(reloadBtn);

        totalTracksLabel = new JLabel("Total Tracks Loaded: " + trackDatabase.size());
        totalTracksLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalTracksLabel.setForeground(SPOTIFY_GREEN);
        toolRow.add(totalTracksLabel);

        topPanel.add(toolRow);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table View
        String[] columnNames = {"ID", "Track Title", "Artist", "Album", "Genre", "Popularity", "Duration (ms)"};
        datasetTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable datasetTable = new JTable(datasetTableModel);
        styleTable(datasetTable);

        JScrollPane scrollPane = new JScrollPane(datasetTable);
        scrollPane.getViewport().setBackground(DARK_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshDatasetTable();

        return panel;
    }

    private void refreshDatasetTable() {
        datasetTableModel.setRowCount(0);
        for (SpotifyTrack t : trackDatabase) {
            datasetTableModel.addRow(new Object[]{
                    t.id, t.title, t.artist, t.album, t.genre, t.popularity, t.durationMs
            });
        }
        if (totalTracksLabel != null) {
            totalTracksLabel.setText("Total Tracks Loaded: " + trackDatabase.size());
        }
    }

    private void showAddTrackDialog() {
        JDialog dialog = new JDialog(this, "Add New Spotify Track", true);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBackground(CARD_BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField();
        JTextField artistField = new JTextField();
        JTextField albumField = new JTextField();
        JTextField genreField = new JTextField();
        JTextField popField = new JTextField("80");
        JTextField durField = new JTextField("210000");

        form.add(new JLabel("Track Title:")); form.add(titleField);
        form.add(new JLabel("Artist:")); form.add(artistField);
        form.add(new JLabel("Album:")); form.add(albumField);
        form.add(new JLabel("Genre:")); form.add(genreField);
        form.add(new JLabel("Popularity (0-100):")); form.add(popField);
        form.add(new JLabel("Duration (ms):")); form.add(durField);

        dialog.add(form, BorderLayout.CENTER);

        JButton saveBtn = new JButton("Save Track");
        stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String artist = artistField.getText().trim();
                String album = albumField.getText().trim();
                String genre = genreField.getText().trim().toLowerCase();
                int pop = Integer.parseInt(popField.getText().trim());
                int dur = Integer.parseInt(durField.getText().trim());

                if (title.isEmpty() || artist.isEmpty() || genre.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Title, Artist, and Genre cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String id = String.valueOf(trackDatabase.size() + 1);
                SpotifyTrack newTrack = new SpotifyTrack(id, artist, album, title, pop, dur, genre);
                trackDatabase.add(newTrack);
                availableGenres.add(genre);

                refreshDatasetTable();
                applyRecommendationFilter();
                dialog.dispose();

                JOptionPane.showMessageDialog(this, "Track \"" + title + "\" added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Popularity and Duration must be valid numbers!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomBtnPanel = new JPanel();
        bottomBtnPanel.setBackground(CARD_BG);
        bottomBtnPanel.add(saveBtn);
        dialog.add(bottomBtnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ==========================================
    // BOTTOM STATUS BAR
    // ==========================================
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SIDEBAR_BG);
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_GRAY),
                BorderFactory.createEmptyBorder(4, 15, 4, 15)
        ));

        JLabel leftStatus = new JLabel("● System Operational | CSV Dataset: data/dataset.csv");
        leftStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        leftStatus.setForeground(SPOTIFY_GREEN);

        JLabel rightStatus = new JLabel("Spotify-Based System Project | LDCW6123");
        rightStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightStatus.setForeground(TEXT_MUTED);

        bar.add(leftStatus, BorderLayout.WEST);
        bar.add(rightStatus, BorderLayout.EAST);
        return bar;
    }

    // ==========================================
    // STYLING UTILITIES
    // ==========================================
    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(SPOTIFY_GREEN);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(ACCENT_GRAY);
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(ACCENT_GRAY);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTableHeader header = table.getTableHeader();
        header.setBackground(SIDEBAR_BG);
        header.setForeground(SPOTIFY_GREEN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    // ==========================================
    // CSV DATASET LOADER ENGINE
    // ==========================================
    private void loadDataset(String filename) {
        trackDatabase.clear();
        availableGenres.clear();

        File csvFile = new File(filename);
        if (!csvFile.exists()) {
            csvFile = new File("dataset.csv");
        }
        if (!csvFile.exists()) {
            loadFallbackData();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (tokens.length >= 7) {
                    String id = cleanCsvToken(tokens[0]);
                    String artist = cleanCsvToken(tokens[1]);
                    String album = cleanCsvToken(tokens[2]);
                    String title = cleanCsvToken(tokens[3]);
                    int pop = parseSafeInt(tokens[4], 50);
                    int dur = parseSafeInt(tokens[5], 200000);
                    String genre = cleanCsvToken(tokens[6]).toLowerCase();

                    trackDatabase.add(new SpotifyTrack(id, artist, album, title, pop, dur, genre));
                    availableGenres.add(genre);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing CSV: " + e.getMessage());
            loadFallbackData();
        }

        if (trackDatabase.isEmpty()) {
            loadFallbackData();
        }
    }

    private String cleanCsvToken(String token) {
        return token.trim().replaceAll("^\"|\"$", "");
    }

    private int parseSafeInt(String val, int defaultVal) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private void loadFallbackData() {
        trackDatabase.add(new SpotifyTrack("1", "The Weeknd", "After Hours", "Blinding Lights", 95, 200040, "pop"));
        trackDatabase.add(new SpotifyTrack("2", "Dua Lipa", "Future Nostalgia", "Levitating", 88, 203807, "pop"));
        trackDatabase.add(new SpotifyTrack("3", "Queen", "A Night at the Opera", "Bohemian Rhapsody", 89, 354320, "rock"));
        trackDatabase.add(new SpotifyTrack("4", "Nirvana", "Nevermind", "Smells Like Teen Spirit", 87, 301160, "rock"));
        trackDatabase.add(new SpotifyTrack("5", "Eminem", "The Eminem Show", "Without Me", 91, 290320, "hip-hop"));
        trackDatabase.add(new SpotifyTrack("6", "Vance Joy", "Dream Your Life Away", "Riptide", 83, 194240, "acoustic"));
        trackDatabase.add(new SpotifyTrack("7", "Miles Davis", "Kind of Blue", "So What", 75, 562000, "jazz"));
        trackDatabase.add(new SpotifyTrack("8", "Daft Punk", "Random Access Memories", "Get Lucky", 84, 248413, "electronic"));
        trackDatabase.add(new SpotifyTrack("9", "SZA", "SOS", "Kill Bill", 92, 153933, "r-n-b"));
        trackDatabase.add(new SpotifyTrack("10", "BTS", "BE", "Dynamite", 88, 199053, "k-pop"));

        for (SpotifyTrack t : trackDatabase) {
            availableGenres.add(t.genre);
        }
    }

    // ==========================================
    // MAIN ENTRY POINT
    // ==========================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Draft app = new Draft();
            app.setVisible(true);
        });
    }
}