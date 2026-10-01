import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class SpotifySystem extends JFrame {

    public static final Color SPOTIFY_GREEN = new Color(29, 185, 84);
    public static final Color SPOTIFY_GREEN_HOVER = new Color(30, 215, 96);
    public static final Color DARK_BG = new Color(18, 18, 18); // #121212
    public static final Color SIDEBAR_BG = new Color(0, 0, 0);
    public static final Color CARD_BG = new Color(24, 24, 24); // #181818
    public static final Color CARD_HOVER_BG = new Color(38, 38, 38);
    public static final Color CARD_SELECTED_BG = new Color(42, 42, 42); // #2a2a2a hover/select
    public static final Color INPUT_BG = new Color(32, 32, 32);
    public static final Color TEXT_WHITE = new Color(255, 255, 255);
    public static final Color TEXT_MUTED = new Color(167, 167, 167);
    public static final Color BORDER_COLOR = new Color(38, 38, 38);
    public static final Color TOP_NAV_BG = new Color(7, 7, 7);
    public static final Color BOTTOM_BAR_BG = new Color(24, 24, 24);

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

    public static class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(83, 83, 83);
            this.trackColor = new Color(0,0,0,0);
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
            g2.setColor(isThumbRollover() ? new Color(179, 179, 179) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(c.getBackground());
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

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
    private JPanel navRecRow, navCalcRow, navDataRow, navTimelineRow;
    private JLabel navRecLbl, navCalcLbl, navDataLbl, navTimelineLbl;

    // Recommendation Controls
    private JComboBox<String> genreComboBox;
    private String selectedPopularityRange = "All"; // Popularity phase button state
    private List<JButton> popButtons = new ArrayList<>();
    private JTextField searchTextField;
    private DefaultTableModel recTableModel;
    private JLabel recStatusLabel;
    private JTable recTable;
    private int hoveredRecRow = -1;

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
    private int hoveredDatasetRow = -1;

    public SpotifySystem() {
        super("Spotify");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

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

        // Top Navigation Bar
        container.add(createTopNavBar(), BorderLayout.NORTH);

        // Main Layout (Sidebar + Content)
        JPanel centerWrapper = new JPanel(new BorderLayout());
        
        JPanel sidebar = createSidebar();
        centerWrapper.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.add(createRecommendationPanel(), "REC");
        mainContentPanel.add(createCalculatorPanel(), "CALC");
        mainContentPanel.add(createDatasetPanel(), "DATA");
        mainContentPanel.add(createTimelinePanel(), "TIME");

        centerWrapper.add(mainContentPanel, BorderLayout.CENTER);
        container.add(centerWrapper, BorderLayout.CENTER);

        // Bottom Player Bar
        container.add(createPlayerBar(), BorderLayout.SOUTH);

        switchNavTab("REC");
    }

    private JPanel createTopNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(TOP_NAV_BG);
        navBar.setPreferredSize(new Dimension(0, 50));
        navBar.setBorder(BorderFactory.createEmptyBorder(7, 20, 7, 20));

        // Left: Back/Forward
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(createCircleButton("◀"));
        leftPanel.add(createCircleButton("▶"));
        navBar.add(leftPanel, BorderLayout.WEST);

        // Center: Search
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.setOpaque(false);
        
        RoundedPanel searchContainer = new RoundedPanel(20, new Color(36,36,36));
        searchContainer.setPreferredSize(new Dimension(400, 36));
        searchContainer.setLayout(new BorderLayout());
        
        JTextField searchField = new JTextField("What do you want to play?");
        searchField.setForeground(TEXT_WHITE);
        searchField.setBackground(new Color(36,36,36));
        searchField.setCaretColor(TEXT_WHITE);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        searchField.setOpaque(false);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("What do you want to play?")) {
                    searchField.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("What do you want to play?");
                }
            }
        });
        // Wire to recommendation search
        searchField.addActionListener(e -> {
            String text = searchField.getText();
            if (!text.equals("What do you want to play?")) {
                searchTextField.setText(text);
                applyRecommendationFilter();
                switchNavTab("REC");
                cardLayout.show(mainContentPanel, "REC");
            }
        });
        
        searchContainer.add(searchField, BorderLayout.CENTER);
        centerPanel.add(searchContainer);
        navBar.add(centerPanel, BorderLayout.CENTER);

        // Right: Profile and subtle status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JLabel sysLabel = new JLabel("System Operational");
        sysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sysLabel.setForeground(TEXT_MUTED);
        rightPanel.add(sysLabel);

        JPanel profileCircle = new RoundedPanel(28, SPOTIFY_GREEN);
        profileCircle.setPreferredSize(new Dimension(32, 32));
        profileCircle.setLayout(new GridBagLayout());
        JLabel pLabel = new JLabel("U");
        pLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pLabel.setForeground(Color.BLACK);
        profileCircle.add(pLabel);
        rightPanel.add(profileCircle);

        navBar.add(rightPanel, BorderLayout.EAST);
        return navBar;
    }

    private JButton createCircleButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(50,50,50) : new Color(42, 42, 42));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(TEXT_WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createPlayerBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BOTTOM_BAR_BG);
        bar.setPreferredSize(new Dimension(0, 72));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40,40,40)));

        // Left: Track Info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(300, 72));

        RoundedPanel artPlaceholder = new RoundedPanel(4, new Color(51,51,51));
        artPlaceholder.setPreferredSize(new Dimension(48, 48));
        leftPanel.add(artPlaceholder);

        JPanel textInfo = new JPanel();
        textInfo.setLayout(new BoxLayout(textInfo, BoxLayout.Y_AXIS));
        textInfo.setOpaque(false);
        JLabel trackLbl = new JLabel("Blinding Lights");
        trackLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        trackLbl.setForeground(TEXT_WHITE);
        JLabel artistLbl = new JLabel("The Weeknd");
        artistLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        artistLbl.setForeground(TEXT_MUTED);
        textInfo.add(Box.createRigidArea(new Dimension(0, 4)));
        textInfo.add(trackLbl);
        textInfo.add(artistLbl);
        leftPanel.add(textInfo);
        bar.add(leftPanel, BorderLayout.WEST);

        // Center: Controls
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnRow.setOpaque(false);
        btnRow.add(createIconButton("⇆", 20, TEXT_MUTED));
        btnRow.add(createIconButton("⏮", 24, TEXT_WHITE));
        btnRow.add(createPlayButton());
        btnRow.add(createIconButton("⏭", 24, TEXT_WHITE));
        btnRow.add(createIconButton("🔁", 20, TEXT_MUTED));
        
        JPanel progressRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        progressRow.setOpaque(false);
        JLabel tStart = new JLabel("1:23");
        tStart.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tStart.setForeground(TEXT_MUTED);
        
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60,60,60));
                g2.fillRoundRect(0, getHeight()/2 - 2, getWidth(), 4, 4, 4);
                g2.setColor(SPOTIFY_GREEN);
                g2.fillRoundRect(0, getHeight()/2 - 2, (int)(getWidth()*0.4), 4, 4, 4);
            }
        };
        progressBar.setPreferredSize(new Dimension(300, 10));
        progressBar.setOpaque(false);

        JLabel tEnd = new JLabel("3:20");
        tEnd.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tEnd.setForeground(TEXT_MUTED);
        
        progressRow.add(tStart);
        progressRow.add(progressBar);
        progressRow.add(tEnd);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(btnRow);
        centerPanel.add(progressRow);
        bar.add(centerPanel, BorderLayout.CENTER);

        // Right: Volume
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 26));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(300, 72));
        
        JLabel volIcon = new JLabel("🔊");
        volIcon.setForeground(TEXT_MUTED);
        
        JPanel volSlider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60,60,60));
                g2.fillRoundRect(0, getHeight()/2 - 2, getWidth(), 4, 4, 4);
                g2.setColor(TEXT_WHITE);
                g2.fillRoundRect(0, getHeight()/2 - 2, (int)(getWidth()*0.7), 4, 4, 4);
            }
        };
        volSlider.setPreferredSize(new Dimension(100, 10));
        volSlider.setOpaque(false);
        
        rightPanel.add(volIcon);
        rightPanel.add(volSlider);
        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }

    private JLabel createIconButton(String symbol, int size, Color c) {
        JLabel l = new JLabel(symbol);
        l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        l.setForeground(c);
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return l;
    }

    private JButton createPlayButton() {
        JButton btn = new JButton("▶") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEXT_WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("▶")) / 2 + 1;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("▶", x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 25, 15));

        // Brand Header
        JLabel logoLabel = new JLabel("Spotify");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(TEXT_WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // "Your Library" Header
        JPanel libraryRow = new JPanel(new BorderLayout());
        libraryRow.setOpaque(false);
        libraryRow.setMaximumSize(new Dimension(260, 30));
        libraryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel libLbl = new JLabel("Your Library");
        libLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        libLbl.setForeground(TEXT_MUTED);
        libraryRow.add(libLbl, BorderLayout.WEST);
        
        JLabel createLbl = new JLabel("+ Create");
        createLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createLbl.setForeground(TEXT_MUTED);
        libraryRow.add(createLbl, BorderLayout.EAST);
        
        sidebar.add(libraryRow);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // Filter Pills Row
        JPanel pillsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillsRow.setOpaque(false);
        pillsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pillsRow.add(createSidebarPill("All", true));
        pillsRow.add(createSidebarPill("Music", false));
        pillsRow.add(createSidebarPill("Podcasts", false));
        sidebar.add(pillsRow);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nav Rows
        navRecRow = createNavRow("Recommendation Assistant", new Color(29, 185, 84), "REC");
        navRecLbl = (JLabel) navRecRow.getComponent(2);
        
        navCalcRow = createNavRow("Subscription Calculator", new Color(45, 136, 255), "CALC");
        navCalcLbl = (JLabel) navCalcRow.getComponent(2);
        
        navDataRow = createNavRow("Dataset Explorer", new Color(175, 40, 150), "DATA");
        navDataLbl = (JLabel) navDataRow.getComponent(2);
        
        navTimelineRow = createNavRow("Innovation Timeline", new Color(255, 100, 55), "TIME");
        navTimelineLbl = (JLabel) navTimelineRow.getComponent(2);

        sidebar.add(navRecRow);
        sidebar.add(navCalcRow);
        sidebar.add(navDataRow);
        sidebar.add(navTimelineRow);
        sidebar.add(Box.createVerticalGlue());

        // About Section
        JLabel aboutLbl = new JLabel("About");
        aboutLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aboutLbl.setForeground(TEXT_MUTED);
        aboutLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(aboutLbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextArea aboutText = new JTextArea("Winston's Innovation Model applied to Spotify's evolution from idea to global platform.");
        aboutText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        aboutText.setForeground(TEXT_MUTED);
        aboutText.setBackground(SIDEBAR_BG);
        aboutText.setLineWrap(true);
        aboutText.setWrapStyleWord(true);
        aboutText.setEditable(false);
        aboutText.setMaximumSize(new Dimension(230, 60));
        aboutText.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(aboutText);
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        JLabel groupLbl = new JLabel("Group 1 • FCI7");
        groupLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        groupLbl.setForeground(SPOTIFY_GREEN);
        groupLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(groupLbl);

        return sidebar;
    }

    private JButton createSidebarPill(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(active ? TEXT_WHITE : new Color(35,35,35));
        btn.setForeground(active ? Color.BLACK : TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? TEXT_WHITE : new Color(35,35,35), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createNavRow(String text, Color iconColor, String cardName) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(SIDEBAR_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(260, 48));
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel iconSquare = new JPanel();
        iconSquare.setBackground(iconColor);
        iconSquare.setMinimumSize(new Dimension(32, 32));
        iconSquare.setPreferredSize(new Dimension(32, 32));
        iconSquare.setMaximumSize(new Dimension(32, 32));
        
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_MUTED);

        row.add(iconSquare);
        row.add(Box.createRigidArea(new Dimension(12, 0)));
        row.add(lbl);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(26,26,26)); // hover #1a1a1a
                if (!lbl.getForeground().equals(TEXT_WHITE)) {
                    lbl.setForeground(TEXT_WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(SIDEBAR_BG);
                if (!lbl.getForeground().equals(SPOTIFY_GREEN)) {
                    lbl.setForeground(TEXT_MUTED);
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                switchNavTab(cardName);
                cardLayout.show(mainContentPanel, cardName);
            }
        });

        return row;
    }

    private void switchNavTab(String activeCard) {
        navRecLbl.setForeground("REC".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
        navCalcLbl.setForeground("CALC".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
        navDataLbl.setForeground("DATA".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
        navTimelineLbl.setForeground("TIME".equals(activeCard) ? SPOTIFY_GREEN : TEXT_MUTED);
    }

    // ==========================================
    // PANEL 1: MUSIC RECOMMENDATION ASSISTANT
    // ==========================================
    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Top Filter Area
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(DARK_BG);

        // Line 1: Genre
        JPanel genreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genreRow.setOpaque(false);
        JLabel genreLbl = new JLabel("Genre:");
        genreLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        genreLbl.setForeground(TEXT_WHITE);
        Vector<String> genreList = new Vector<>();
        genreList.add("All Genres");
        genreList.addAll(availableGenres);
        genreComboBox = new JComboBox<>(genreList);
        genreComboBox.setPreferredSize(new Dimension(200, 32));
        genreRow.add(genreLbl);
        genreRow.add(genreComboBox);

        // Line 2: Popularity
        JPanel popRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        popRow.setOpaque(false);
        JLabel popLbl = new JLabel("Popularity:");
        popLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        popLbl.setForeground(TEXT_WHITE);
        popRow.add(popLbl);

        String[] popRanges = {"All", "0-20", "20-40", "40-60", "60-80", "80-100"};
        popButtons.clear();
        for (String r : popRanges) {
            JButton pb = new JButton(r);
            pb.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pb.setFocusPainted(false);
            pb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            updatePopButtonStyle(pb, r.equals(selectedPopularityRange));
            
            pb.addActionListener(e -> {
                selectedPopularityRange = r;
                for (JButton b : popButtons) {
                    updatePopButtonStyle(b, b.getText().equals(selectedPopularityRange));
                }
            });
            popButtons.add(pb);
            popRow.add(pb);
        }

        // Line 3: Search & Actions
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        searchTextField = new JTextField(20);
        searchTextField.setPreferredSize(new Dimension(200, 32));
        searchTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        
        JButton filterBtn = new JButton("Search Tracks");
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBtn.setBackground(SPOTIFY_GREEN);
        filterBtn.setForeground(Color.BLACK);
        filterBtn.setFocusPainted(false);
        filterBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        filterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBtn.addActionListener(e -> applyRecommendationFilter());

        JButton surpriseBtn = new JButton("Surprise Me");
        surpriseBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        surpriseBtn.setBackground(DARK_BG);
        surpriseBtn.setForeground(TEXT_WHITE);
        surpriseBtn.setFocusPainted(false);
        surpriseBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_WHITE, 1),
            BorderFactory.createEmptyBorder(7, 19, 7, 19)
        ));
        surpriseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        surpriseBtn.addActionListener(e -> pickRandomRecommendation());

        actionRow.add(searchTextField);
        actionRow.add(filterBtn);
        actionRow.add(surpriseBtn);

        topPanel.add(genreRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        topPanel.add(popRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        topPanel.add(actionRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(topPanel, BorderLayout.NORTH);

        // Center Table Panel
        String[] columnNames = {"#", "Track Title", "Artist", "Album", "Genre", "Popularity", "Duration"};
        recTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        recTable = new JTable(recTableModel);
        styleTable(recTable, true);
        
        recTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        recTable.getColumnModel().getColumn(0).setMaxWidth(40);
        
        JScrollPane scrollPane = createDarkScrollPane(recTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        recStatusLabel = new JLabel("Ready. Select options above to view recommendations.");
        recStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        recStatusLabel.setForeground(TEXT_MUTED);
        panel.add(recStatusLabel, BorderLayout.SOUTH);

        applyRecommendationFilter();
        return panel;
    }
    
    private void updatePopButtonStyle(JButton btn, boolean active) {
        btn.setBackground(active ? SPOTIFY_GREEN : new Color(51,51,51));
        btn.setForeground(active ? Color.BLACK : TEXT_WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    }

    private void applyRecommendationFilter() {
        recTableModel.setRowCount(0);
        String selectedGenre = (String) genreComboBox.getSelectedItem();
        String searchKeyword = searchTextField.getText().trim().toLowerCase();

        int popMin = 0, popMax = 100;
        if (!selectedPopularityRange.equals("All")) {
            String[] parts = selectedPopularityRange.split("-");
            popMin = Integer.parseInt(parts[0]);
            popMax = Integer.parseInt(parts[1]);
        }

        int count = 0;
        int totalPop = 0;
        int limit = 500; 

        for (SpotifyTrack track : trackDatabase) {
            boolean genreMatch = "All Genres".equals(selectedGenre) || track.genre.equalsIgnoreCase(selectedGenre);
            boolean popMatch = track.popularity >= popMin && track.popularity <= popMax;
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
        recStatusLabel.setText(String.format("Displaying %d recommendations for genre '%s' (Pop: %s) | Avg Score: %.1f/100",
                count, selectedGenre, selectedPopularityRange, avgPop));
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
        applyPromoBtn.setBackground(CARD_HOVER_BG);
        applyPromoBtn.setForeground(SPOTIFY_GREEN);
        applyPromoBtn.setFocusPainted(false);
        applyPromoBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
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
        boolean invalidPromo = false;
        if (!promoCode.isEmpty()) {
            if ("STUDENT10".equalsIgnoreCase(promoCode)) {
                promoDiscount = subtotal * 0.10;
            } else {
                invalidPromo = true;
            }
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
        } else if (invalidPromo) {
            sb.append(String.format(" - Promo (%s)   : [Invalid code]\n", promoCode));
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

        JPanel toolCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolCard.setBackground(DARK_BG);
        
        JButton addTrackBtn = new JButton("Add New Track");
        addTrackBtn.setBackground(SPOTIFY_GREEN);
        addTrackBtn.setForeground(Color.BLACK);
        addTrackBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addTrackBtn.setFocusPainted(false);
        addTrackBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        addTrackBtn.addActionListener(e -> showAddTrackDialog());
        toolCard.add(addTrackBtn);

        JButton reloadBtn = new JButton("Reload Dataset");
        reloadBtn.setBackground(DARK_BG);
        reloadBtn.setForeground(TEXT_WHITE);
        reloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        reloadBtn.setFocusPainted(false);
        reloadBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_WHITE, 1),
            BorderFactory.createEmptyBorder(7, 17, 7, 17)
        ));
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

        String[] columnNames = {"ID", "Track Title", "Artist", "Album", "Genre", "Popularity", "Duration (ms)"};
        datasetTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable datasetTable = new JTable(datasetTableModel);
        styleTable(datasetTable, false);

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
        saveBtn.setBackground(SPOTIFY_GREEN);
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
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
                
                if (pop < 0 || pop > 100) {
                    JOptionPane.showMessageDialog(dialog, "Popularity must be between 0 and 100!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (dur <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Duration must be positive!", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
    // PANEL 4: INNOVATION TIMELINE
    // ==========================================
    private JPanel createTimelinePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Spotify Innovation Timeline");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_WHITE);
        
        JLabel descLabel = new JLabel("Explore Spotify's journey through Brian Winston's Innovation Lifecycle Model");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);
        
        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        topPanel.add(descLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        centerGrid.setBackground(DARK_BG);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(DARK_BG);

        String[] stageNames = {
            "Scientific Competence", "Ideation", "Prototype", 
            "Supervening Social Necessity", "Invention", "Diffusion"
        };
        String[] stageYears = {
            "1990s", "2006", "2006-2007", 
            "Mid-2000s", "2008", "2008-Present"
        };
        String[] stageSummaries = {
            "MP3 compression & internet streaming technologies emerged",
            "Daniel Ek envisioned combining Napster's choice with iTunes' ease",
            "Built desktop client focused on instant playback speed",
            "Rising piracy created demand for legal, convenient music access",
            "Launched in Europe with major label licensing deals secured",
            "Expanded to 180+ markets with 200M+ subscribers globally"
        };
        
        String[] stageDetails = {
            "The technological foundations for music streaming existed before Spotify. During the 1990s, Fraunhofer IIS developed MP3 audio compression, allowing digital music to be represented using much less data than uncompressed audio. In 1995, RealNetworks introduced RealAudio, enabling audio to be played over the internet while data was still being delivered. Apple's iTunes Music Store (2003) demonstrated that people would pay for legal digital music downloads. These existing technologies — audio compression, internet delivery, and digital music distribution — formed the scientific competence base that made Spotify possible.",
            "In April 2006, Daniel Ek and Martin Lorentzon founded Spotify with a clear vision: combine the vast music choice that file-sharing services like Napster and Kazaa offered with the polished, legal experience of Apple's iTunes. The key insight was that listeners wanted to explore lots of music and start listening straight away — without buying individual tracks or downloading potentially incomplete files. The music industry also needed a solution: rights holders wanted control over how their recordings were used and a way to earn revenue from digital distribution. Spotify's founders proposed a licensed service where someone could type a song name, press play, and hear it almost instantly.",
            "Spotify's engineering team had to transform the concept into working technology. The team built an early desktop application where the primary design goal was making playback feel extremely fast — a listener could begin hearing music almost immediately after selecting a track. This required developing a custom streaming architecture that prioritized speed over traditional download-then-play approaches. The company used existing technologies like MP3 compression and internet streaming protocols but assembled them into a system specifically designed around quick access and instant playback. The prototype had to work alongside legal and business requirements, since Spotify needed licensing agreements with music rights holders before it could offer their recordings.",
            "Before Spotify, the way people listened to music was fundamentally changing. Internet adoption was growing, and music piracy through file-sharing was increasingly common. Listeners wanted quick, easy access to large catalogues of music, but legal options were limited to purchasing individual tracks or albums. Meanwhile, music industry revenues were declining as physical sales dropped and unauthorized file-sharing provided free (but often poor-quality) alternatives. This created a social demand for a service that could provide convenient, legal music access. The need wasn't just technological — it was a societal shift in how people expected to consume media.",
            "Spotify launched in October 2008 in six European countries: Finland, France, Norway, Spain, Sweden, and the United Kingdom. Crucially, the company had secured major global music licensing deals in 2007 — before launch — ensuring the service was fully legal. The initial service was free to listeners and supported by advertising, with a premium ad-free tier also available. Invention in Winston's model isn't simply about creating something new; society, markets, and institutions help decide which technologies are supported and accepted. In Spotify's case, user demand, music licensing agreements, and market support all helped transform the prototype into a real, functioning service.",
            "After its European launch, Spotify expanded rapidly. Mobile access arrived in 2009, allowing users to listen while travelling or exercising. The U.S. launch came in 2011, followed by Southeast Asia in 2013. By 2023, Spotify had over 200 million subscribers across 180+ markets. The service also expanded beyond music: in 2019, Spotify acquired podcast companies Gimlet and Anchor, creating spin-off services. Meanwhile, traditional music downloads became increasingly redundant as streaming replaced individual file purchases — though physical formats like vinyl continued to grow. However, Spotify also faced the 'law of suppression of radical potential': licensing agreements with labels sometimes restricted content availability, such as Universal Music Group's 2017 deal limiting new album access for free-tier users."
        };
        
        String[] stageInsights = {
            "Winston argues that technologies don't appear from nothing — they build on existing scientific knowledge and capabilities.",
            "Ideation in Winston's model is about imagining a possible use for knowledge and technology that already exists — not inventing new technology.",
            "The prototype stage is about developing an idea into a working technological form, not necessarily inventing entirely new technology.",
            "Winston's supervening social necessity explains how changes in society create the demand that drives adoption of new technologies.",
            "Invention requires more than technology — institutional support, licensing, and market acceptance are essential for a technology to become real.",
            "Diffusion shows how technology spreads through society, while suppression forces (like institutional agreements) can constrain its radical potential."
        };

        RoundedPanel detailCard = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        detailCard.setLayout(new BorderLayout(15, 15));
        detailCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel detailHeader = new JPanel();
        detailHeader.setLayout(new BoxLayout(detailHeader, BoxLayout.Y_AXIS));
        detailHeader.setBackground(CARD_BG);
        
        JLabel detailTitleLbl = new JLabel(stageNames[0]);
        detailTitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        detailTitleLbl.setForeground(TEXT_WHITE);
        
        JLabel detailYearLbl = new JLabel(stageYears[0]);
        detailYearLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailYearLbl.setForeground(SPOTIFY_GREEN);
        
        detailHeader.add(detailTitleLbl);
        detailHeader.add(Box.createRigidArea(new Dimension(0, 5)));
        detailHeader.add(detailYearLbl);
        detailCard.add(detailHeader, BorderLayout.NORTH);

        JTextArea detailTextArea = new JTextArea(stageDetails[0]);
        detailTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailTextArea.setForeground(TEXT_WHITE);
        detailTextArea.setBackground(CARD_BG);
        detailTextArea.setLineWrap(true);
        detailTextArea.setWrapStyleWord(true);
        detailTextArea.setEditable(false);
        detailTextArea.setMargin(new Insets(10, 0, 10, 0));
        
        JScrollPane detailScroll = new JScrollPane(detailTextArea);
        detailScroll.setBorder(null);
        detailScroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        detailScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        detailCard.add(detailScroll, BorderLayout.CENTER);

        RoundedPanel insightCard = new RoundedPanel(10, INPUT_BG);
        insightCard.setLayout(new BorderLayout());
        insightCard.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        JLabel insightLbl = new JLabel("<html><b>Key Insight:</b> " + stageInsights[0] + "</html>");
        insightLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        insightLbl.setForeground(SPOTIFY_GREEN);
        insightCard.add(insightLbl, BorderLayout.CENTER);
        detailCard.add(insightCard, BorderLayout.SOUTH);

        List<RoundedPanel> cards = new ArrayList<>();
        
        for (int i = 0; i < 6; i++) {
            int index = i;
            RoundedPanel c = new RoundedPanel(12, CARD_BG, BORDER_COLOR);
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            c.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            JLabel nLbl = new JLabel(stageNames[i]);
            nLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            nLbl.setForeground(TEXT_WHITE);
            
            JLabel yLbl = new JLabel(stageYears[i]);
            yLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            yLbl.setForeground(SPOTIFY_GREEN);
            
            JLabel sLbl = new JLabel(stageSummaries[i]);
            sLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            sLbl.setForeground(TEXT_MUTED);
            
            c.add(nLbl);
            c.add(Box.createRigidArea(new Dimension(0, 3)));
            c.add(yLbl);
            c.add(Box.createRigidArea(new Dimension(0, 5)));
            c.add(sLbl);
            
            c.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (RoundedPanel p : cards) {
                        p.setBorderColor(BORDER_COLOR);
                        p.setBackgroundColor(CARD_BG);
                    }
                    c.setBorderColor(SPOTIFY_GREEN);
                    c.setBackgroundColor(CARD_SELECTED_BG);
                    
                    detailTitleLbl.setText(stageNames[index]);
                    detailYearLbl.setText(stageYears[index]);
                    detailTextArea.setText(stageDetails[index]);
                    detailTextArea.setCaretPosition(0);
                    insightLbl.setText("<html><b>Key Insight:</b> " + stageInsights[index] + "</html>");
                }
            });
            
            cards.add(c);
            listPanel.add(c);
            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        cards.get(0).setBorderColor(SPOTIFY_GREEN);
        cards.get(0).setBackgroundColor(CARD_SELECTED_BG);

        JScrollPane listScroll = createDarkScrollPane(listPanel);
        listScroll.setBorder(null);
        centerGrid.add(listScroll);
        centerGrid.add(detailCard);
        
        panel.add(centerGrid, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // STYLING UTILITIES
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

    private void styleTable(JTable table, boolean isRecTable) {
        table.setBackground(DARK_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(CARD_SELECTED_BG);
        table.setSelectionForeground(SPOTIFY_GREEN);
        
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (isRecTable) {
                    if (row != hoveredRecRow) {
                        hoveredRecRow = row;
                        table.repaint();
                    }
                } else {
                    if (row != hoveredDatasetRow) {
                        hoveredDatasetRow = row;
                        table.repaint();
                    }
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (isRecTable) hoveredRecRow = -1;
                else hoveredDatasetRow = -1;
                table.repaint();
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int hoverRow = isRecTable ? hoveredRecRow : hoveredDatasetRow;
                
                if (isSelected) {
                    c.setBackground(CARD_SELECTED_BG);
                } else if (row == hoverRow) {
                    c.setBackground(CARD_SELECTED_BG);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(24,24,24) : new Color(18,18,18));
                }
                
                if (column == table.getColumnCount() - 1) { // Duration
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                }
                return c;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(DARK_BG);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(51,51,51)));
        
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
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
            int maxTracksToLoad = 10000;

            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SpotifySystem app = new SpotifySystem();
            app.setVisible(true);
        });
    }
}
