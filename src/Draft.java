import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.text.DecimalFormat;

/**
 * Spotify Desktop System - LDCW6123 Group Project
 * Refined Modern Desktop Application (Java Swing)
 * 
 * Features:
 * - Spotify Dark Theme (#121212, #181818, #1DB954)
 * - Custom Sleek Dark Scrollbars (No default Windows scrollbars)
 * - Borderless Modern Table Header & Styled Rows (No harsh white grid lines)
 * - Graphical Anti-Aliased Popularity Pill Bar Renderer
 * - Auto-detects & loads 'Spotify Dataset/train.csv' or 'data/dataset.csv'
 */
public class Draft extends JFrame {

    // --- Spotify Brand Color Palette ---
    public static final Color SPOTIFY_GREEN = new Color(29, 185, 84);
    public static final Color SPOTIFY_GREEN_HOVER = new Color(30, 215, 96);
    public static final Color DARK_BG = new Color(18, 18, 18);
    public static final Color SIDEBAR_BG = new Color(0, 0, 0);
    public static final Color CARD_BG = new Color(24, 24, 24);
    public static final Color CARD_HOVER_BG = new Color(38, 38, 38);
    public static final Color CARD_SELECTED_BG = new Color(45, 45, 45);
    public static final Color INPUT_BG = new Color(32, 32, 32);
    public static final Color TEXT_WHITE = new Color(255, 255, 255);
    public static final Color TEXT_MUTED = new Color(167, 167, 167);
    public static final Color BORDER_COLOR = new Color(38, 38, 38);

    // --- Custom Anti-Aliased Rounded Panel ---
    public static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;
        private Color borderColor;

        public RoundedPanel(int radius, Color bg) {
            this(radius, bg, null);
        }

        public RoundedPanel(int radius, Color bg, Color border) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            this.borderColor = border;
            setOpaque(false);
        }

        public void setBackgroundColor(Color bg) {
            this.backgroundColor = bg;
            repaint();
        }

        public void setBorderColor(Color border) {
            this.borderColor = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            }
            g2.dispose();
        }
    }

    // --- Custom Dark ScrollBar UI ---
    public static class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(60, 60, 60);
            this.trackColor = DARK_BG;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(100, 100, 100) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(DARK_BG);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

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
    private String loadedDatasetPath = "";

    // --- GUI Components ---
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    // Sidebar Navigation Buttons
    private JButton navRecBtn, navCalcBtn, navDataBtn;

    // Recommendation Controls
    private JComboBox<String> genreComboBox;
    private JSlider popularitySlider;
    private JTextField searchTextField;
    private DefaultTableModel recTableModel;
    private JLabel recStatusLabel;

    // Calculator Controls
    private RoundedPanel studentCard, individualCard, duoCard, familyCard;
    private String selectedTier = "Individual";
    private double selectedBasePrice = 10.99;
    private int selectedAccounts = 1;

    private JComboBox<String> billingCycleCombo, currencyCombo;
    private JCheckBox hifiCheckBox, offlineCheckBox;
    private JTextField promoTextField;
    private JTextArea invoiceTextArea;
    private JLabel totalCostLabel;

    // Dataset View
    private DefaultTableModel datasetTableModel;
    private JLabel totalTracksLabel;

    public Draft() {
        super("Spotify System - LDCW6123 Desktop App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

        // Anti-aliased font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        setupDarkThemeDefaults();
        loadDatasetAuto();
        initUI();
    }

    private void setupDarkThemeDefaults() {
        UIManager.put("Panel.background", DARK_BG);
        UIManager.put("OptionPane.background", DARK_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_WHITE);
        UIManager.put("Label.foreground", TEXT_WHITE);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", TEXT_WHITE);
        UIManager.put("ComboBox.selectionBackground", CARD_HOVER_BG);
        UIManager.put("ComboBox.selectionForeground", SPOTIFY_GREEN);
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", TEXT_WHITE);
        UIManager.put("TextField.caretForeground", SPOTIFY_GREEN);
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

        switchNavTab("REC");
    }

    // ==========================================
    // SIDEBAR NAVIGATION (No System Info Box)
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        // Brand Header (Standard text, no hollow emoji boxes)
        JLabel logoLabel = new JLabel("Spotify");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(TEXT_WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("LDCW6123 Group Project");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_MUTED);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logoLabel);
        sidebar.add(subLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 35)));

        // Navigation Buttons
        navRecBtn = createNavButton("Recommendation Assistant", "REC");
        navCalcBtn = createNavButton("Subscription Calculator", "CALC");
        navDataBtn = createNavButton("Track Dataset Explorer", "DATA");

        sidebar.add(navRecBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(navCalcBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(navDataBtn);

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        btn.setMaximumSize(new Dimension(210, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.getForeground().equals(SPOTIFY_GREEN)) {
                    btn.setForeground(TEXT_WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getForeground().equals(SPOTIFY_GREEN)) {
                    btn.setForeground(TEXT_MUTED);
                }
            }
        });

        btn.addActionListener(e -> {
            switchNavTab(cardName);
            cardLayout.show(mainContentPanel, cardName);
        });
        return btn;
    }

    private void switchNavTab(String activeCard) {
        navRecBtn.setForeground("REC".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
        navCalcBtn.setForeground("CALC".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
        navDataBtn.setForeground("DATA".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
    }

    // ==========================================
    // PANEL 1: MUSIC RECOMMENDATION ASSISTANT
    // ==========================================
    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("Music Recommendation Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel("Personalized track recommendations based on audio genre preferences and popularity rating filters.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);

        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        topPanel.add(descLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Filter Controls Card
        RoundedPanel filterCard = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 12));

        // Genre Selector
        JLabel genreLbl = new JLabel("Genre:");
        genreLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        genreLbl.setForeground(TEXT_MUTED);
        filterCard.add(genreLbl);

        Vector<String> genreList = new Vector<>();
        genreList.add("All Genres");
        genreList.addAll(availableGenres);
        genreComboBox = new JComboBox<>(genreList);
        genreComboBox.setPreferredSize(new Dimension(150, 34));
        filterCard.add(genreComboBox);

        // Popularity Slider
        JLabel popLbl = new JLabel("  Min Popularity:");
        popLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        popLbl.setForeground(TEXT_MUTED);
        filterCard.add(popLbl);

        popularitySlider = new JSlider(0, 100, 40);
        popularitySlider.setBackground(CARD_BG);
        popularitySlider.setForeground(SPOTIFY_GREEN);
        popularitySlider.setPreferredSize(new Dimension(130, 38));
        filterCard.add(popularitySlider);

        // Search Input
        JLabel searchLbl = new JLabel("  Search:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLbl.setForeground(TEXT_MUTED);
        filterCard.add(searchLbl);

        searchTextField = new JTextField(12);
        searchTextField.setPreferredSize(new Dimension(140, 34));
        searchTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        filterCard.add(searchTextField);

        // Action Buttons
        JButton filterBtn = new JButton("Search Tracks");
        stylePillButton(filterBtn, SPOTIFY_GREEN, Color.BLACK);
        filterBtn.addActionListener(e -> applyRecommendationFilter());
        filterCard.add(filterBtn);

        JButton surpriseBtn = new JButton("Surprise Me");
        stylePillButton(surpriseBtn, CARD_HOVER_BG, TEXT_WHITE);
        surpriseBtn.addActionListener(e -> pickRandomRecommendation());
        filterCard.add(surpriseBtn);

        topPanel.add(filterCard);
        panel.add(topPanel, BorderLayout.NORTH);

        // Center Table Panel
        String[] columnNames = {"#", "Track Title", "Artist", "Album", "Genre", "Popularity Rating", "Duration"};
        recTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable recTable = new JTable(recTableModel);
        styleTable(recTable);

        // Custom Popularity Progress Bar Renderer
        recTable.getColumnModel().getColumn(5).setCellRenderer(new PopularityBarRenderer());

        JScrollPane scrollPane = createDarkScrollPane(recTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Status Info
        recStatusLabel = new JLabel("Ready. Select options above to view recommendations.");
        recStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        recStatusLabel.setForeground(TEXT_MUTED);
        panel.add(recStatusLabel, BorderLayout.SOUTH);

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
        int limit = 500; // Limit displayed rows for high performance

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
                        track.popularity,
                        track.getFormattedDuration()
                });
                if (count >= limit) break;
            }
        }

        double avgPop = count > 0 ? (double) totalPop / count : 0.0;
        recStatusLabel.setText(String.format("Displaying %d recommendations for genre '%s' (Min Pop: %d) | Avg Score: %.1f/100",
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
                randomTrack.popularity,
                randomTrack.getFormattedDuration()
        });

        recStatusLabel.setText("Surprise Pick: \"" + randomTrack.title + "\" by " + randomTrack.artist + " [" + randomTrack.genre.toUpperCase() + "]");
    }

    // ==========================================
    // CUSTOM TABLE POPULARITY RENDERER (Clean Pill)
    // ==========================================
    public static class PopularityBarRenderer extends JPanel implements TableCellRenderer {
        private int popValue = 0;

        public PopularityBarRenderer() {
            setOpaque(true);
            setBackground(CARD_BG);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            popValue = (value instanceof Integer) ? (Integer) value : 50;
            if (isSelected) {
                setBackground(CARD_SELECTED_BG);
            } else {
                setBackground(row % 2 == 0 ? CARD_BG : DARK_BG);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 55;
            int h = 10;
            int x = 10;
            int y = (getHeight() - h) / 2;

            // Background Track
            g2.setColor(INPUT_BG);
            g2.fillRoundRect(x, y, w, h, 6, 6);

            // Fill Bar
            int fillW = (int) (w * (popValue / 100.0));
            g2.setColor(SPOTIFY_GREEN);
            g2.fillRoundRect(x, y, fillW, h, 6, 6);

            // Text Label
            g2.setColor(TEXT_WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(String.valueOf(popValue), x + w + 12, y + 9);

            g2.dispose();
        }
    }

    // ==========================================
    // PANEL 2: SUBSCRIPTION FARE CALCULATOR
    // ==========================================
    private JPanel createCalculatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Top Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("Subscription Fare Calculator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel("Select an account tier card and configure regional tax, billing cycles, and optional audio add-ons.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(descLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center Split Layout
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        centerGrid.setBackground(DARK_BG);

        // Left Config Panel
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(DARK_BG);

        JLabel tierHeader = new JLabel("1. Select Plan Tier");
        tierHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tierHeader.setForeground(SPOTIFY_GREEN);
        leftPanel.add(tierHeader);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Interactive 2x2 Plan Tier Cards
        JPanel cardsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        cardsGrid.setBackground(DARK_BG);
        cardsGrid.setMaximumSize(new Dimension(600, 170));

        studentCard = createPlanCard("Student", "$5.99 / mo", "1 Verified Account", 5.99, 1);
        individualCard = createPlanCard("Individual", "$10.99 / mo", "1 Premium Account", 10.99, 1);
        duoCard = createPlanCard("Duo", "$14.99 / mo", "2 Premium Accounts", 14.99, 2);
        familyCard = createPlanCard("Family", "$16.99 / mo", "Up to 6 Accounts", 16.99, 6);

        cardsGrid.add(studentCard);
        cardsGrid.add(individualCard);
        cardsGrid.add(duoCard);
        cardsGrid.add(familyCard);

        leftPanel.add(cardsGrid);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Config Options Panel
        RoundedPanel optCard = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        optCard.setLayout(new BoxLayout(optCard, BoxLayout.Y_AXIS));
        optCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel configHeader = new JLabel("2. Billing Frequency & Currency");
        configHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        configHeader.setForeground(TEXT_WHITE);
        optCard.add(configHeader);
        optCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel cycleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cycleRow.setBackground(CARD_BG);
        cycleRow.add(new JLabel("Billing Cycle:"));
        billingCycleCombo = new JComboBox<>(new String[]{"Monthly Billing", "Annual Billing (Save 16.6% / 2 Mos Free)"});
        billingCycleCombo.addActionListener(e -> calculateSubscriptionFare());
        cycleRow.add(billingCycleCombo);
        optCard.add(cycleRow);

        optCard.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel currRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        currRow.setBackground(CARD_BG);
        currRow.add(new JLabel("Region Currency:"));
        currencyCombo = new JComboBox<>(new String[]{"USD ($)", "EUR (€)", "GBP (£)", "MYR (RM)"});
        currencyCombo.addActionListener(e -> calculateSubscriptionFare());
        currRow.add(currencyCombo);
        optCard.add(currRow);

        optCard.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel addonHeader = new JLabel("3. Add-ons & Promo Code");
        addonHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addonHeader.setForeground(TEXT_WHITE);
        optCard.add(addonHeader);
        optCard.add(Box.createRigidArea(new Dimension(0, 8)));

        hifiCheckBox = new JCheckBox("Hi-Fi Lossless Audio Upgrade (+15%)");
        hifiCheckBox.setBackground(CARD_BG);
        hifiCheckBox.setForeground(TEXT_WHITE);
        hifiCheckBox.addActionListener(e -> calculateSubscriptionFare());

        offlineCheckBox = new JCheckBox("Extra Storage & Device Sync (+$1.99/mo)");
        offlineCheckBox.setBackground(CARD_BG);
        offlineCheckBox.setForeground(TEXT_WHITE);
        offlineCheckBox.addActionListener(e -> calculateSubscriptionFare());

        optCard.add(hifiCheckBox);
        optCard.add(offlineCheckBox);

        optCard.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel promoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        promoRow.setBackground(CARD_BG);
        promoRow.add(new JLabel("Promo Code:"));
        promoTextField = new JTextField(8);
        promoRow.add(promoTextField);

        JButton applyPromoBtn = new JButton("Apply");
        stylePillButton(applyPromoBtn, CARD_HOVER_BG, SPOTIFY_GREEN);
        applyPromoBtn.addActionListener(e -> calculateSubscriptionFare());
        promoRow.add(applyPromoBtn);

        optCard.add(promoRow);

        leftPanel.add(optCard);
        centerGrid.add(leftPanel);

        // Right Invoice Summary Card
        RoundedPanel invoiceCard = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        invoiceCard.setLayout(new BorderLayout(15, 15));
        invoiceCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel summaryTitle = new JLabel("Fare Breakdown & Invoice");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryTitle.setForeground(TEXT_WHITE);
        invoiceCard.add(summaryTitle, BorderLayout.NORTH);

        invoiceTextArea = new JTextArea();
        invoiceTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        invoiceTextArea.setBackground(DARK_BG);
        invoiceTextArea.setForeground(TEXT_WHITE);
        invoiceTextArea.setEditable(false);
        invoiceTextArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        invoiceCard.add(createDarkScrollPane(invoiceTextArea), BorderLayout.CENTER);

        totalCostLabel = new JLabel("Total Fare: $10.99");
        totalCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalCostLabel.setForeground(SPOTIFY_GREEN);
        totalCostLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        invoiceCard.add(totalCostLabel, BorderLayout.SOUTH);

        centerGrid.add(invoiceCard);
        panel.add(centerGrid, BorderLayout.CENTER);

        updateCardHighlights();
        calculateSubscriptionFare();

        return panel;
    }

    private RoundedPanel createPlanCard(String name, String price, String sub, double basePrice, int accounts) {
        RoundedPanel card = new RoundedPanel(14, CARD_BG, BORDER_COLOR);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(TEXT_WHITE);

        JLabel priceLbl = new JLabel(price);
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        priceLbl.setForeground(SPOTIFY_GREEN);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);

        card.add(nameLbl);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(priceLbl);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(subLbl);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedTier = name;
                selectedBasePrice = basePrice;
                selectedAccounts = accounts;
                updateCardHighlights();
                calculateSubscriptionFare();
            }
        });

        return card;
    }

    private void updateCardHighlights() {
        studentCard.setBorderColor("Student".equals(selectedTier) ? SPOTIFY_GREEN : BORDER_COLOR);
        studentCard.setBackgroundColor("Student".equals(selectedTier) ? CARD_SELECTED_BG : CARD_BG);

        individualCard.setBorderColor("Individual".equals(selectedTier) ? SPOTIFY_GREEN : BORDER_COLOR);
        individualCard.setBackgroundColor("Individual".equals(selectedTier) ? CARD_SELECTED_BG : CARD_BG);

        duoCard.setBorderColor("Duo".equals(selectedTier) ? SPOTIFY_GREEN : BORDER_COLOR);
        duoCard.setBackgroundColor("Duo".equals(selectedTier) ? CARD_SELECTED_BG : CARD_BG);

        familyCard.setBorderColor("Family".equals(selectedTier) ? SPOTIFY_GREEN : BORDER_COLOR);
        familyCard.setBackgroundColor("Family".equals(selectedTier) ? CARD_SELECTED_BG : CARD_BG);
    }

    private void calculateSubscriptionFare() {
        boolean isAnnual = billingCycleCombo.getSelectedIndex() == 1;
        double monthsToBill = isAnnual ? 10.0 : 1.0;
        double baseFare = selectedBasePrice * monthsToBill;

        double hifiFee = hifiCheckBox.isSelected() ? (baseFare * 0.15) : 0.0;
        double offlineFee = offlineCheckBox.isSelected() ? (1.99 * (isAnnual ? 12 : 1)) : 0.0;

        double subtotal = baseFare + hifiFee + offlineFee;

        double promoDiscount = 0.0;
        String promoCode = promoTextField.getText().trim();
        if ("STUDENT10".equalsIgnoreCase(promoCode)) {
            promoDiscount = subtotal * 0.10;
        }

        double discountedSubtotal = subtotal - promoDiscount;
        double tax = discountedSubtotal * 0.06;
        double totalFare = discountedSubtotal + tax;

        String currencySymbol = "$";
        double exchangeRate = 1.0;
        int currIndex = currencyCombo.getSelectedIndex();
        if (currIndex == 1) { currencySymbol = "€"; exchangeRate = 0.92; }
        else if (currIndex == 2) { currencySymbol = "£"; exchangeRate = 0.79; }
        else if (currIndex == 3) { currencySymbol = "RM"; exchangeRate = 4.70; }

        double finalConvertedTotal = totalFare * exchangeRate;
        double perUserCost = finalConvertedTotal / selectedAccounts;

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("      SPOTIFY SUBSCRIPTION INVOICE       \n");
        sb.append("=========================================\n\n");
        sb.append(String.format("Plan Tier          : %s (%d Account%s)\n", selectedTier, selectedAccounts, selectedAccounts > 1 ? "s" : ""));
        sb.append(String.format("Billing Cycle      : %s\n", isAnnual ? "Annual (12 Mos / Pay 10)" : "Monthly"));
        sb.append(String.format("Base Rate / Month  : %s%.2f\n", currencySymbol, selectedBasePrice * exchangeRate));
        sb.append(String.format("Base Duration Rate : %s%.2f\n\n", currencySymbol, baseFare * exchangeRate));

        sb.append("--- Add-ons & Adjustments ---\n");
        if (hifiCheckBox.isSelected()) {
            sb.append(String.format(" + Hi-Fi Audio (15%%) : %s%.2f\n", currencySymbol, hifiFee * exchangeRate));
        }
        if (offlineCheckBox.isSelected()) {
            sb.append(String.format(" + Extra Device Sync : %s%.2f\n", currencySymbol, offlineFee * exchangeRate));
        }
        if (promoDiscount > 0) {
            sb.append(String.format(" - Promo (STUDENT10): -%s%.2f\n", currencySymbol, promoDiscount * exchangeRate));
        }

        sb.append(String.format("Est. Tax (6%% SST)  : %s%.2f\n", currencySymbol, tax * exchangeRate));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL FARE         : %s%.2f %s\n", currencySymbol, finalConvertedTotal, isAnnual ? "/ year" : "/ month"));
        if (selectedAccounts > 1) {
            sb.append(String.format("Cost Per Account   : %s%.2f / user\n", currencySymbol, perUserCost));
        }
        sb.append("=========================================\n");

        invoiceTextArea.setText(sb.toString());
        totalCostLabel.setText(String.format("Total: %s%.2f %s", currencySymbol, finalConvertedTotal, isAnnual ? "/yr" : "/mo"));
    }

    // ==========================================
    // PANEL 3: DATASET EXPLORER
    // ==========================================
    private JPanel createDatasetPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("Spotify Track Dataset Explorer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel("Browse loaded dataset from " + loadedDatasetPath + ", add new entries, or reload dataset.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);

        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        topPanel.add(descLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // Button Toolbar Card
        RoundedPanel toolCard = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        toolCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JButton addTrackBtn = new JButton("Add New Track");
        stylePillButton(addTrackBtn, SPOTIFY_GREEN, Color.BLACK);
        addTrackBtn.addActionListener(e -> showAddTrackDialog());
        toolCard.add(addTrackBtn);

        JButton reloadBtn = new JButton("Reload Dataset");
        stylePillButton(reloadBtn, CARD_HOVER_BG, TEXT_WHITE);
        reloadBtn.addActionListener(e -> {
            loadDatasetAuto();
            refreshDatasetTable();
            applyRecommendationFilter();
            JOptionPane.showMessageDialog(this, "Dataset reloaded from " + loadedDatasetPath + "!", "Reload Success", JOptionPane.INFORMATION_MESSAGE);
        });
        toolCard.add(reloadBtn);

        totalTracksLabel = new JLabel("  Total Tracks Loaded: " + trackDatabase.size());
        totalTracksLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalTracksLabel.setForeground(SPOTIFY_GREEN);
        toolCard.add(totalTracksLabel);

        topPanel.add(toolCard);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Track Title", "Artist", "Album", "Genre", "Popularity", "Duration (ms)"};
        datasetTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable datasetTable = new JTable(datasetTableModel);
        styleTable(datasetTable);

        JScrollPane scrollPane = createDarkScrollPane(datasetTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshDatasetTable();
        return panel;
    }

    private void refreshDatasetTable() {
        datasetTableModel.setRowCount(0);
        int limit = 1000;
        int count = 0;
        for (SpotifyTrack t : trackDatabase) {
            count++;
            datasetTableModel.addRow(new Object[]{
                    t.id, t.title, t.artist, t.album, t.genre.toUpperCase(), t.popularity, t.durationMs
            });
            if (count >= limit) break;
        }
        if (totalTracksLabel != null) {
            totalTracksLabel.setText("  Total Tracks Loaded: " + trackDatabase.size());
        }
    }

    private void showAddTrackDialog() {
        JDialog dialog = new JDialog(this, "Add New Spotify Track", true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 12, 12));
        form.setBackground(CARD_BG);
        form.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

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
        stylePillButton(saveBtn, SPOTIFY_GREEN, Color.BLACK);
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
                trackDatabase.add(0, newTrack);
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
        bar.setPreferredSize(new Dimension(0, 30));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)
        ));

        JLabel leftStatus = new JLabel("System Operational | Dataset: " + loadedDatasetPath);
        leftStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        leftStatus.setForeground(SPOTIFY_GREEN);

        JLabel rightStatus = new JLabel("Spotify Desktop App | LDCW6123");
        rightStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightStatus.setForeground(TEXT_MUTED);

        bar.add(leftStatus, BorderLayout.WEST);
        bar.add(rightStatus, BorderLayout.EAST);
        return bar;
    }

    // ==========================================
    // STYLING UTILITIES (Clean ScrollBars & Tables)
    // ==========================================
    private JScrollPane createDarkScrollPane(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.getViewport().setBackground(DARK_BG);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return sp;
    }

    private void stylePillButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(false); // Clean gridless look
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(CARD_SELECTED_BG);
        table.setSelectionForeground(SPOTIFY_GREEN);

        JTableHeader header = table.getTableHeader();
        header.setBackground(DARK_BG);
        header.setForeground(SPOTIFY_GREEN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }

    // ==========================================
    // DATASET LOADER ENGINE (Priority Order)
    // ==========================================
    private void loadDatasetAuto() {
        trackDatabase.clear();
        availableGenres.clear();

        File f1 = new File("Spotify Dataset/train.csv");
        File f2 = new File("data/dataset.csv");
        File f3 = new File("dataset.csv");

        if (f1.exists()) {
            loadedDatasetPath = "Spotify Dataset/train.csv";
            parseCsvFile(f1);
        } else if (f2.exists()) {
            loadedDatasetPath = "data/dataset.csv";
            parseCsvFile(f2);
        } else if (f3.exists()) {
            loadedDatasetPath = "dataset.csv";
            parseCsvFile(f3);
        } else {
            loadedDatasetPath = "Built-in Fallback Data";
            loadFallbackData();
        }

        if (trackDatabase.isEmpty()) {
            loadFallbackData();
        }
    }

    private void parseCsvFile(File csvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isHeader = true;
            int count = 0;
            int maxTracksToLoad = 10000; // Efficient loading limit

            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // Detection: train.csv has 21 columns (track_genre is index 20)
                if (tokens.length >= 21) {
                    String id = cleanCsvToken(tokens[1]);
                    String artist = cleanCsvToken(tokens[2]);
                    String album = cleanCsvToken(tokens[3]);
                    String title = cleanCsvToken(tokens[4]);
                    int pop = parseSafeInt(tokens[5], 50);
                    int dur = parseSafeInt(tokens[6], 200000);
                    String genre = cleanCsvToken(tokens[20]).toLowerCase();

                    if (!title.isEmpty() && !artist.isEmpty()) {
                        trackDatabase.add(new SpotifyTrack(id, artist, album, title, pop, dur, genre));
                        availableGenres.add(genre);
                        count++;
                    }
                } 
                // Fallback for 7 column schema
                else if (tokens.length >= 7) {
                    String id = cleanCsvToken(tokens[0]);
                    String artist = cleanCsvToken(tokens[1]);
                    String album = cleanCsvToken(tokens[2]);
                    String title = cleanCsvToken(tokens[3]);
                    int pop = parseSafeInt(tokens[4], 50);
                    int dur = parseSafeInt(tokens[5], 200000);
                    String genre = cleanCsvToken(tokens[6]).toLowerCase();

                    if (!title.isEmpty() && !artist.isEmpty()) {
                        trackDatabase.add(new SpotifyTrack(id, artist, album, title, pop, dur, genre));
                        availableGenres.add(genre);
                        count++;
                    }
                }

                if (count >= maxTracksToLoad) break;
            }
        } catch (Exception e) {
            System.err.println("Error parsing CSV: " + e.getMessage());
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