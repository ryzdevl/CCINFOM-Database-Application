package gui;

import dao.*;
import models.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class BeachResortManagementGUI extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;

    // DAOs
    private GuestDAO guestDAO;
    private RoomDAO roomDAO;
    private AmenityDAO amenityDAO;
    private InventoryDAO inventoryDAO;
    private ReservationDAO reservationDAO;
    private CheckOutDAO checkOutDAO;
    private RestockDAO restockDAO;
    private AmenityRentalDAO amenityRentalDAO;
    private ReportsDAO reportsDAO;

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BG_COLOR = new Color(236, 240, 241);
    private final Color TEXT_COLOR = new Color(0, 0, 0);

    public BeachResortManagementGUI() {
        setTitle("Beach Resort Management System - CCINFOM S27-06");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // Initialize components first
        initComponents();

        // Initialize DAOs after GUI is ready
        initializeDAOs();

        // Show connection status
        SwingUtilities.invokeLater(() -> {
            if (guestDAO != null) {
                updateStatus("Database connected - Ready");
            } else {
                updateStatus("Database connection failed - Check credentials");
                JOptionPane.showMessageDialog(this,
                        "Could not connect to database.\nPlease check:\n" +
                                "1. MySQL is running\n" +
                                "2. Database 'beach_resort' exists\n" +
                                "3. Credentials in DatabaseConnection.java are correct",
                        "Database Connection Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        setVisible(true);
    }

    private void initializeDAOs() {
        try {
            guestDAO = new GuestDAO();
            roomDAO = new RoomDAO();
            amenityDAO = new AmenityDAO();
            inventoryDAO = new InventoryDAO();
            reservationDAO = new ReservationDAO();
            checkOutDAO = new CheckOutDAO();
            restockDAO = new RestockDAO();
            amenityRentalDAO = new AmenityRentalDAO();
            reportsDAO = new ReportsDAO();
            System.out.println("DAOs initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database connection: " + e.getMessage());
            e.printStackTrace();
            // Don't show dialog here - GUI not fully initialized yet
        }
    }

    private void initComponents() {
        // Main container
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        // Top panel - Header
        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);

        // Left panel - Navigation Menu
        JPanel menuPanel = createMenuPanel();
        container.add(menuPanel, BorderLayout.WEST);

        // Center panel - Content Area
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);

        // Add all panels
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createGuestManagementPanel(), "guest");
        mainPanel.add(createRoomManagementPanel(), "room");
        mainPanel.add(createAmenityManagementPanel(), "amenity");
        mainPanel.add(createInventoryManagementPanel(), "inventory");
        mainPanel.add(createReservationBookingPanel(), "reservation");
        mainPanel.add(createCheckInPanel(), "checkin");
        mainPanel.add(createCheckOutPanel(), "checkout");
        mainPanel.add(createInventoryRestockPanel(), "restock");
        mainPanel.add(createAmenityRentalPanel(), "rental");
        mainPanel.add(createReportsPanel(), "reports");

        container.add(mainPanel, BorderLayout.CENTER);

        // Bottom panel - Status bar
        JPanel statusPanel = createStatusPanel();
        container.add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🏖️ Beach Resort Management System");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Group 6 - Bondoc, Malapitan, Mariano, Pamintuan");
        subtitleLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Dashboard
        panel.add(createMenuButton("🏠 Dashboard", "dashboard"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Records Management Section
        panel.add(createMenuSection("RECORDS MANAGEMENT"));
        panel.add(createMenuButton("👥 Guest Management", "guest"));
        panel.add(createMenuButton("🛏️ Room Management", "room"));
        panel.add(createMenuButton("🏊 Amenity Management", "amenity"));
        panel.add(createMenuButton("📦 Inventory Management", "inventory"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Transactions Section
        panel.add(createMenuSection("TRANSACTIONS"));
        panel.add(createMenuButton("📅 Reservation Booking", "reservation"));
        panel.add(createMenuButton("✅ Guest Check-In", "checkin"));
        panel.add(createMenuButton("💳 Guest Check-Out", "checkout"));
        panel.add(createMenuButton("📥 Inventory Restocking", "restock"));
        panel.add(createMenuButton("🎯 Amenity Rental", "rental"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Reports Section
        panel.add(createMenuSection("REPORTS"));
        panel.add(createMenuButton("📊 Generate Reports", "reports"));

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JLabel createMenuSection(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        label.setForeground(new Color(149, 165, 166));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(10, 15, 5, 15));
        return label;
    }

    private JButton createMenuButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(44, 62, 80));
        button.setBorder(new EmptyBorder(12, 15, 12, 15));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(44, 62, 80));
            }
        });

        button.addActionListener(e -> {
            cardLayout.show(mainPanel, panelName);
            updateStatus("Navigated to " + text);

            // Refresh data when navigating to specific panels
            refreshPanelData(panelName);
        });

        return button;
    }

    private void refreshPanelData(String panelName) {
        // This will be called when switching panels to refresh data
        switch(panelName) {
            case "guest":
                // Refresh guest table if needed
                break;
            case "room":
                // Refresh room table if needed
                break;
            // Add more cases as needed
        }
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(8, 20, 8, 20));

        statusLabel = new JLabel("Ready - Database connected");
        statusLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);

        JLabel timeLabel = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(189, 195, 199));

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);

        // Update time every second
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        timer.start();

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCard("Total Guests", "Click Refresh", SUCCESS_COLOR));
        statsPanel.add(createStatCard("Active Reservations", "Click Refresh", PRIMARY_COLOR));
        statsPanel.add(createStatCard("Available Rooms", "Click Refresh", SUCCESS_COLOR));
        statsPanel.add(createStatCard("Revenue Today", "$0.00", WARNING_COLOR));
        statsPanel.add(createStatCard("Occupied Rooms", "Click Refresh", DANGER_COLOR));
        statsPanel.add(createStatCard("Amenities Rented", "Click Refresh", SECONDARY_COLOR));
        statsPanel.add(createStatCard("Inventory Items", "Click Refresh", new Color(155, 89, 182)));
        statsPanel.add(createStatCard("Pending Checkouts", "Click Refresh", WARNING_COLOR));

        panel.add(statsPanel, BorderLayout.CENTER);

        // Refresh button
        JButton refreshBtn = createActionButton("🔄 Refresh Dashboard", SECONDARY_COLOR);
        refreshBtn.addActionListener(e -> loadDashboardStats(statsPanel));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadDashboardStats(JPanel statsPanel) {
        try {
            // This would be implemented with actual queries
            updateStatus("Dashboard statistics refreshed");
            JOptionPane.showMessageDialog(this,
                    "Dashboard refresh functionality ready!\nConnect to database queries for live stats.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(127, 140, 141));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // GUEST MANAGEMENT PANEL
    private JPanel createGuestManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Guest Management");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Charles Andrew Bondoc");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(assignLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Guest ID", "First Name", "Last Name", "Phone", "Email", "Passport No"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addBtn = createActionButton("➕ Add New", SUCCESS_COLOR);
        JButton editBtn = createActionButton("✏️ Edit", PRIMARY_COLOR);
        JButton deleteBtn = createActionButton("🗑️ Delete", DANGER_COLOR);
        JButton refreshBtn = createActionButton("🔄 Refresh", SECONDARY_COLOR);
        JButton viewPrefsBtn = createActionButton("👁️ View Preferences", new Color(155, 89, 182));
        JButton viewFeedbackBtn = createActionButton("⭐ View Feedback", WARNING_COLOR);

        // Add button actions
        addBtn.addActionListener(e -> addGuest(model));
        editBtn.addActionListener(e -> editGuest(table, model));
        deleteBtn.addActionListener(e -> deleteGuest(table, model));
        refreshBtn.addActionListener(e -> loadGuestData(model));
        viewPrefsBtn.addActionListener(e -> viewGuestPreferences(table));
        viewFeedbackBtn.addActionListener(e -> viewGuestFeedback(table));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewPrefsBtn);
        buttonPanel.add(viewFeedbackBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // DON'T load data here - will be loaded after DAOs are initialized

        return panel;
    }

    private void loadGuestData(DefaultTableModel model) {
        try {
            // Check if DAO is initialized
            if (guestDAO == null) {
                JOptionPane.showMessageDialog(this,
                        "Database connection not ready. Please check database setup.",
                        "Connection Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            model.setRowCount(0);
            List<Guest> guests = guestDAO.getAllGuests(null);
            for (Guest guest : guests) {
                model.addRow(new Object[]{
                        guest.getGuestId(),
                        guest.getFirstName(),
                        guest.getLastName(),
                        guest.getPhone(),
                        guest.getEmail(),
                        guest.getPassportNo()
                });
            }
            updateStatus("Loaded " + guests.size() + " guests");
        } catch (SQLException e) {
            showError("Error loading guests: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addGuest(DefaultTableModel model) {
        // Check if DAO is initialized
        if (guestDAO == null) {
            JOptionPane.showMessageDialog(this,
                    "Database connection not ready. Please check database setup.",
                    "Connection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField firstNameField = new JTextField(20);
        JTextField lastNameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField passportField = new JTextField(20);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Passport No:"));
        inputPanel.add(passportField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Add New Guest", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Guest guest = new Guest(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        phoneField.getText().trim(),
                        emailField.getText().trim(),
                        passportField.getText().trim()
                );

                Long id = guestDAO.addGuest(guest);
                JOptionPane.showMessageDialog(this,
                        "Guest added successfully with ID: " + id,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadGuestData(model);
            } catch (SQLException e) {
                showError("Error adding guest: " + e.getMessage());
            }
        }
    }

    private void editGuest(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a guest to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long guestId = (Long) model.getValueAt(selectedRow, 0);

        try {
            Guest guest = guestDAO.getGuestById(guestId);
            if (guest == null) {
                showError("Guest not found!");
                return;
            }

            JTextField firstNameField = new JTextField(guest.getFirstName(), 20);
            JTextField lastNameField = new JTextField(guest.getLastName(), 20);
            JTextField phoneField = new JTextField(guest.getPhone(), 20);
            JTextField emailField = new JTextField(guest.getEmail(), 20);
            JTextField passportField = new JTextField(guest.getPassportNo(), 20);

            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
            inputPanel.add(new JLabel("First Name:"));
            inputPanel.add(firstNameField);
            inputPanel.add(new JLabel("Last Name:"));
            inputPanel.add(lastNameField);
            inputPanel.add(new JLabel("Phone:"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Email:"));
            inputPanel.add(emailField);
            inputPanel.add(new JLabel("Passport No:"));
            inputPanel.add(passportField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel,
                    "Edit Guest", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                guest.setFirstName(firstNameField.getText().trim());
                guest.setLastName(lastNameField.getText().trim());
                guest.setPhone(phoneField.getText().trim());
                guest.setEmail(emailField.getText().trim());
                guest.setPassportNo(passportField.getText().trim());

                guestDAO.updateGuest(guest);
                JOptionPane.showMessageDialog(this, "Guest updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadGuestData(model);
            }
        } catch (SQLException e) {
            showError("Error updating guest: " + e.getMessage());
        }
    }

    private void deleteGuest(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a guest to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long guestId = (Long) model.getValueAt(selectedRow, 0);
        String guestName = model.getValueAt(selectedRow, 1) + " " + model.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete guest: " + guestName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                guestDAO.deleteGuest(guestId);
                JOptionPane.showMessageDialog(this, "Guest deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadGuestData(model);
            } catch (SQLException e) {
                showError("Error deleting guest: " + e.getMessage());
            }
        }
    }

    private void viewGuestPreferences(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a guest",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long guestId = (Long) table.getModel().getValueAt(selectedRow, 0);

        try {
            String prefs = guestDAO.getGuestWithPreferences(guestId);
            JTextArea textArea = new JTextArea(prefs, 20, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane,
                    "Guest Preferences", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Error viewing preferences: " + e.getMessage());
        }
    }

    private void viewGuestFeedback(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a guest",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long guestId = (Long) table.getModel().getValueAt(selectedRow, 0);

        try {
            String feedback = guestDAO.getGuestWithFeedback(guestId);
            JTextArea textArea = new JTextArea(feedback, 20, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane,
                    "Guest Feedback", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Error viewing feedback: " + e.getMessage());
        }
    }

    // ROOM MANAGEMENT - Full Implementation (Ryan James Malapitan)
    private JPanel createRoomManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Room Management");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(assignLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Room ID", "Room Code", "Room Type", "Bed Type", "Max Capacity", "Rate/Night", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addBtn = createActionButton("➕ Add New", SUCCESS_COLOR);
        JButton editBtn = createActionButton("✏️ Edit", PRIMARY_COLOR);
        JButton deleteBtn = createActionButton("🗑️ Delete", DANGER_COLOR);
        JButton refreshBtn = createActionButton("🔄 Refresh", SECONDARY_COLOR);
        JButton viewServicesBtn = createActionButton("👁️ View Services", new Color(155, 89, 182));
        JButton viewGuestCountBtn = createActionButton("📊 Guest Count", WARNING_COLOR);

        // Add button actions
        addBtn.addActionListener(e -> addRoom(model));
        editBtn.addActionListener(e -> editRoom(table, model));
        deleteBtn.addActionListener(e -> deleteRoom(table, model));
        refreshBtn.addActionListener(e -> loadRoomData(model));
        viewServicesBtn.addActionListener(e -> viewRoomServices(table));
        viewGuestCountBtn.addActionListener(e -> viewRoomGuestCount(table));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewServicesBtn);
        buttonPanel.add(viewGuestCountBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadRoomData(DefaultTableModel model) {
        try {
            if (roomDAO == null) {
                JOptionPane.showMessageDialog(this, "Database connection not ready.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            model.setRowCount(0);
            List<Room> rooms = roomDAO.getAllRooms(null);
            for (Room room : rooms) {
                model.addRow(new Object[]{
                        room.getRoomId(),
                        room.getRoomCode(),
                        room.getRoomType(),
                        room.getBedType(),
                        room.getMaxCapacity(),
                        String.format("$%.2f", room.getRatePerNight()),
                        room.getStatus()
                });
            }
            updateStatus("Loaded " + rooms.size() + " rooms");
        } catch (SQLException e) {
            showError("Error loading rooms: " + e.getMessage());
        }
    }

    private void addRoom(DefaultTableModel model) {
        if (roomDAO == null) {
            JOptionPane.showMessageDialog(this, "Database connection not ready.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField roomCodeField = new JTextField(20);
        JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite", "Cottage"});
        JComboBox<String> bedTypeCombo = new JComboBox<>(new String[]{"Single", "Twin", "Queen", "King"});
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JTextField rateField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.add(new JLabel("Room Code:"));
        inputPanel.add(roomCodeField);
        inputPanel.add(new JLabel("Room Type:"));
        inputPanel.add(roomTypeCombo);
        inputPanel.add(new JLabel("Bed Type:"));
        inputPanel.add(bedTypeCombo);
        inputPanel.add(new JLabel("Max Capacity:"));
        inputPanel.add(capacitySpinner);
        inputPanel.add(new JLabel("Rate per Night:"));
        inputPanel.add(rateField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add New Room", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Room room = new Room(
                        roomCodeField.getText().trim(),
                        (String) roomTypeCombo.getSelectedItem(),
                        (String) bedTypeCombo.getSelectedItem(),
                        (Integer) capacitySpinner.getValue(),
                        Double.parseDouble(rateField.getText().trim())
                );
                room.setDescription(descArea.getText().trim());

                Long id = roomDAO.addRoom(room);
                JOptionPane.showMessageDialog(this, "Room added successfully with ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRoomData(model);
            } catch (SQLException e) {
                showError("Error adding room: " + e.getMessage());
            } catch (NumberFormatException e) {
                showError("Invalid rate format. Please enter a valid number.");
            }
        }
    }

    private void editRoom(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long roomId = (Long) model.getValueAt(selectedRow, 0);

        try {
            Room room = roomDAO.getRoomById(roomId);
            if (room == null) {
                showError("Room not found!");
                return;
            }

            JTextField roomCodeField = new JTextField(room.getRoomCode(), 20);
            JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite", "Cottage"});
            roomTypeCombo.setSelectedItem(room.getRoomType());
            JComboBox<String> bedTypeCombo = new JComboBox<>(new String[]{"Single", "Twin", "Queen", "King"});
            bedTypeCombo.setSelectedItem(room.getBedType());
            JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(room.getMaxCapacity(), 1, 10, 1));
            JTextField rateField = new JTextField(String.valueOf(room.getRatePerNight()), 20);
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"available", "reserved", "occupied", "maintenance"});
            statusCombo.setSelectedItem(room.getStatus());

            JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
            inputPanel.add(new JLabel("Room Code:"));
            inputPanel.add(roomCodeField);
            inputPanel.add(new JLabel("Room Type:"));
            inputPanel.add(roomTypeCombo);
            inputPanel.add(new JLabel("Bed Type:"));
            inputPanel.add(bedTypeCombo);
            inputPanel.add(new JLabel("Max Capacity:"));
            inputPanel.add(capacitySpinner);
            inputPanel.add(new JLabel("Rate per Night:"));
            inputPanel.add(rateField);
            inputPanel.add(new JLabel("Status:"));
            inputPanel.add(statusCombo);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Edit Room", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                room.setRoomCode(roomCodeField.getText().trim());
                room.setRoomType((String) roomTypeCombo.getSelectedItem());
                room.setBedType((String) bedTypeCombo.getSelectedItem());
                room.setMaxCapacity((Integer) capacitySpinner.getValue());
                room.setRatePerNight(Double.parseDouble(rateField.getText().trim()));
                room.setStatus((String) statusCombo.getSelectedItem());

                roomDAO.updateRoom(room);
                JOptionPane.showMessageDialog(this, "Room updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRoomData(model);
            }
        } catch (SQLException e) {
            showError("Error updating room: " + e.getMessage());
        }
    }

    private void deleteRoom(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long roomId = (Long) model.getValueAt(selectedRow, 0);
        String roomCode = (String) model.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete room: " + roomCode + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                roomDAO.deleteRoom(roomId);
                JOptionPane.showMessageDialog(this, "Room deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRoomData(model);
            } catch (SQLException e) {
                showError("Error deleting room: " + e.getMessage());
            }
        }
    }

    private void viewRoomServices(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long roomId = (Long) table.getModel().getValueAt(selectedRow, 0);

        try {
            String services = roomDAO.getRoomWithServiceRequests(roomId);
            JTextArea textArea = new JTextArea(services, 20, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane, "Room Service Requests", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Error viewing services: " + e.getMessage());
        }
    }

    private void viewRoomGuestCount(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long roomId = (Long) table.getModel().getValueAt(selectedRow, 0);

        try {
            String guestCount = roomDAO.getRoomWithGuestCount(roomId);
            JTextArea textArea = new JTextArea(guestCount, 15, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scrollPane, "Room Guest Statistics", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Error viewing guest count: " + e.getMessage());
        }
    }

    // AMENITY MANAGEMENT - Full Implementation (Vener Mariano)
    private JPanel createAmenityManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Amenity Management");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Vener Mariano");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(assignLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Amenity ID", "Name", "Description", "Rate", "Availability", "Rating"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addBtn = createActionButton("➕ Add", SUCCESS_COLOR);
        JButton editBtn = createActionButton("✏️ Edit", PRIMARY_COLOR);
        JButton deleteBtn = createActionButton("🗑️ Delete", DANGER_COLOR);
        JButton refreshBtn = createActionButton("🔄 Refresh", SECONDARY_COLOR);
        JButton viewRequestsBtn = createActionButton("📊 View Requests", WARNING_COLOR);

        addBtn.addActionListener(e -> addAmenity(model));
        editBtn.addActionListener(e -> editAmenity(table, model));
        deleteBtn.addActionListener(e -> deleteAmenity(table, model));
        refreshBtn.addActionListener(e -> loadAmenityData(model));
        viewRequestsBtn.addActionListener(e -> viewAmenityRequests(table));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewRequestsBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadAmenityData(DefaultTableModel model) {
        try {
            if (amenityDAO == null) {
                JOptionPane.showMessageDialog(this, "Database connection not ready.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            model.setRowCount(0);
            List<Amenity> amenities = amenityDAO.getAllAmenities(null);
            for (Amenity amenity : amenities) {
                model.addRow(new Object[]{
                        amenity.getAmenityId(),
                        amenity.getName(),
                        amenity.getDescription(),
                        String.format("$%.2f", amenity.getRate()),
                        amenity.getAvailability(),
                        amenity.getOverallRating() != null ? amenity.getOverallRating() : "N/A"
                });
            }
            updateStatus("Loaded " + amenities.size() + " amenities");
        } catch (SQLException e) {
            showError("Error loading amenities: " + e.getMessage());
        }
    }

    private void addAmenity(DefaultTableModel model) {
        if (amenityDAO == null) return;

        JTextField nameField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JTextField rateField = new JTextField(20);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(new JScrollPane(descArea));
        inputPanel.add(new JLabel("Rate:"));
        inputPanel.add(rateField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Amenity", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Amenity amenity = new Amenity(
                        nameField.getText().trim(),
                        descArea.getText().trim(),
                        Double.parseDouble(rateField.getText().trim())
                );

                Long id = amenityDAO.addAmenity(amenity);
                JOptionPane.showMessageDialog(this, "Amenity added with ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAmenityData(model);
            } catch (SQLException e) {
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void editAmenity(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an amenity", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long id = (Long) model.getValueAt(row, 0);
            Amenity amenity = amenityDAO.getAmenityById(id);

            JTextField nameField = new JTextField(amenity.getName(), 20);
            JTextArea descArea = new JTextArea(amenity.getDescription(), 3, 20);
            JTextField rateField = new JTextField(String.valueOf(amenity.getRate()), 20);
            JComboBox<String> availCombo = new JComboBox<>(new String[]{"available", "reserved", "maintenance"});
            availCombo.setSelectedItem(amenity.getAvailability());

            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            inputPanel.add(new JLabel("Name:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Description:"));
            inputPanel.add(new JScrollPane(descArea));
            inputPanel.add(new JLabel("Rate:"));
            inputPanel.add(rateField);
            inputPanel.add(new JLabel("Availability:"));
            inputPanel.add(availCombo);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Edit Amenity", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                amenity.setName(nameField.getText().trim());
                amenity.setDescription(descArea.getText().trim());
                amenity.setRate(Double.parseDouble(rateField.getText().trim()));
                amenity.setAvailability((String) availCombo.getSelectedItem());

                amenityDAO.updateAmenity(amenity);
                JOptionPane.showMessageDialog(this, "Amenity updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAmenityData(model);
            }
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void deleteAmenity(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        Long id = (Long) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                amenityDAO.deleteAmenity(id);
                JOptionPane.showMessageDialog(this, "Deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAmenityData(model);
            } catch (SQLException e) {
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void viewAmenityRequests(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        try {
            Long id = (Long) table.getModel().getValueAt(row, 0);
            String requests = amenityDAO.getAmenityWithGuestRequests(id);

            JTextArea textArea = new JTextArea(requests, 15, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Amenity Requests", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    // INVENTORY MANAGEMENT - Full Implementation (Daniel Pamintuan)
    private JPanel createInventoryManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Daniel Pamintuan");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(assignLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Item ID", "Name", "Quantity", "Supplier", "Last Restocked"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addBtn = createActionButton("➕ Add", SUCCESS_COLOR);
        JButton editBtn = createActionButton("✏️ Edit", PRIMARY_COLOR);
        JButton deleteBtn = createActionButton("🗑️ Delete", DANGER_COLOR);
        JButton refreshBtn = createActionButton("🔄 Refresh", SECONDARY_COLOR);
        JButton viewStatsBtn = createActionButton("📊 View Stats", WARNING_COLOR);

        addBtn.addActionListener(e -> addInventoryItem(model));
        editBtn.addActionListener(e -> editInventoryItem(table, model));
        deleteBtn.addActionListener(e -> deleteInventoryItem(table, model));
        refreshBtn.addActionListener(e -> loadInventoryData(model));
        viewStatsBtn.addActionListener(e -> viewInventoryStats(table));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewStatsBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadInventoryData(DefaultTableModel model) {
        try {
            if (inventoryDAO == null) {
                JOptionPane.showMessageDialog(this, "Database connection not ready.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            model.setRowCount(0);
            List<InventoryItem> items = inventoryDAO.getAllInventoryItems();
            for (InventoryItem item : items) {
                model.addRow(new Object[]{
                        item.getItemId(),
                        item.getName(),
                        item.getQuantityOnHand(),
                        item.getSupplier(),
                        item.getLastRestocked() != null ? item.getLastRestocked().toString() : "Never"
                });
            }
            updateStatus("Loaded " + items.size() + " inventory items");
        } catch (SQLException e) {
            showError("Error loading inventory: " + e.getMessage());
        }
    }

    private void addInventoryItem(DefaultTableModel model) {
        if (inventoryDAO == null) return;

        JTextField nameField = new JTextField(20);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        JTextField supplierField = new JTextField(20);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(qtySpinner);
        inputPanel.add(new JLabel("Supplier:"));
        inputPanel.add(supplierField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Inventory Item", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                InventoryItem item = new InventoryItem(
                        nameField.getText().trim(),
                        (Integer) qtySpinner.getValue(),
                        supplierField.getText().trim()
                );

                Long id = inventoryDAO.addInventoryItem(item);
                JOptionPane.showMessageDialog(this, "Item added with ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData(model);
            } catch (SQLException e) {
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void editInventoryItem(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long id = (Long) model.getValueAt(row, 0);
            InventoryItem item = inventoryDAO.getInventoryItemById(id);

            JTextField nameField = new JTextField(item.getName(), 20);
            JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(item.getQuantityOnHand(), 0, 10000, 1));
            JTextField supplierField = new JTextField(item.getSupplier(), 20);

            JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
            inputPanel.add(new JLabel("Item Name:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Quantity:"));
            inputPanel.add(qtySpinner);
            inputPanel.add(new JLabel("Supplier:"));
            inputPanel.add(supplierField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Edit Inventory Item", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                item.setName(nameField.getText().trim());
                item.setQuantityOnHand((Integer) qtySpinner.getValue());
                item.setSupplier(supplierField.getText().trim());

                inventoryDAO.updateInventoryItem(item);
                JOptionPane.showMessageDialog(this, "Item updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData(model);
            }
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void deleteInventoryItem(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        Long id = (Long) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                inventoryDAO.deleteInventoryItem(id);
                JOptionPane.showMessageDialog(this, "Deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData(model);
            } catch (SQLException e) {
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void viewInventoryStats(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        try {
            Long id = (Long) table.getModel().getValueAt(row, 0);
            String stats = inventoryDAO.getInventoryWithRequestCount(id);

            JTextArea textArea = new JTextArea(stats, 15, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Inventory Statistics", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    private JPanel createReservationBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Reservation Booking");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Charles Andrew Bondoc");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(assignLabel);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Guest ID field with search
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Guest ID:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        JTextField guestIdField = new JTextField(15);
        formPanel.add(guestIdField, gbc);

        gbc.gridx = 2;
        JButton searchGuestBtn = createActionButton("🔍 Search Guest", PRIMARY_COLOR);
        JLabel guestInfoLabel = new JLabel("");
        searchGuestBtn.addActionListener(e -> searchGuest(guestIdField, guestInfoLabel));
        formPanel.add(searchGuestBtn, gbc);

        // Guest info display
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        guestInfoLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        guestInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(guestInfoLabel, gbc);

        // Room ID field with availability check
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Room ID:"), gbc);

        gbc.gridx = 1;
        JTextField roomIdField = new JTextField(15);
        formPanel.add(roomIdField, gbc);

        gbc.gridx = 2;
        JButton checkAvailBtn = createActionButton("Check Availability", SUCCESS_COLOR);
        JLabel roomInfoLabel = new JLabel("");
        formPanel.add(checkAvailBtn, gbc);

        // Room info display
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        roomInfoLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        roomInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(roomInfoLabel, gbc);

        // Check-in Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Check-In Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField checkInField = new JTextField(15);
        checkInField.setText(LocalDate.now().toString());
        formPanel.add(checkInField, gbc);

        // Check-out Date
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Check-Out Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField checkOutField = new JTextField(15);
        checkOutField.setText(LocalDate.now().plusDays(3).toString());
        formPanel.add(checkOutField, gbc);

        // Booking Channel
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Booking Channel:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<String> channelCombo = new JComboBox<>(new String[]{"Walk-In", "Online", "Phone", "Agent"});
        channelCombo.setSelectedIndex(1); // Default to online
        formPanel.add(channelCombo, gbc);

        // Amenities selection
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        JLabel amenityLabel = new JLabel("Select Amenities (Optional):");
        amenityLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        formPanel.add(amenityLabel, gbc);

        gbc.gridy = 8;
        JPanel amenityPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        amenityPanel.setOpaque(false);

        // Load amenities dynamically
        JCheckBox[] amenityCheckboxes = new JCheckBox[8];
        JButton loadAmenitiesBtn = createActionButton("Load Available Amenities", SECONDARY_COLOR);
        loadAmenitiesBtn.addActionListener(e -> loadAmenitiesForBooking(amenityPanel, amenityCheckboxes));
        formPanel.add(loadAmenitiesBtn, gbc);

        gbc.gridy = 9;
        formPanel.add(amenityPanel, gbc);

        // Availability check button action
        checkAvailBtn.addActionListener(e -> checkRoomAvailability(
                roomIdField, checkInField, checkOutField, roomInfoLabel
        ));

        panel.add(formPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);

        JButton bookBtn = createActionButton("✅ Create Reservation", SUCCESS_COLOR);
        bookBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        bookBtn.addActionListener(e -> createReservation(
                guestIdField, roomIdField, checkInField, checkOutField,
                channelCombo, amenityCheckboxes
        ));

        JButton clearBtn = createActionButton("🔄 Clear Form", WARNING_COLOR);
        clearBtn.addActionListener(e -> {
            guestIdField.setText("");
            roomIdField.setText("");
            checkInField.setText(LocalDate.now().toString());
            checkOutField.setText(LocalDate.now().plusDays(3).toString());
            guestInfoLabel.setText("");
            roomInfoLabel.setText("");
            channelCombo.setSelectedIndex(1);
            for (JCheckBox cb : amenityCheckboxes) {
                if (cb != null) cb.setSelected(false);
            }
        });

        actionPanel.add(bookBtn);
        actionPanel.add(clearBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void searchGuest(JTextField guestIdField, JLabel guestInfoLabel) {
        try {
            if (guestDAO == null) {
                showError("Database not connected");
                return;
            }

            String guestIdText = guestIdField.getText().trim();
            if (guestIdText.isEmpty()) {
                showError("Please enter a Guest ID");
                return;
            }

            Long guestId = Long.parseLong(guestIdText);
            Guest guest = guestDAO.getGuestById(guestId);

            if (guest != null) {
                guestInfoLabel.setText("✓ Guest found: " + guest.getFullName() + " (" + guest.getEmail() + ")");
                guestInfoLabel.setForeground(SUCCESS_COLOR);
                updateStatus("Guest verified: " + guest.getFullName());
            } else {
                guestInfoLabel.setText("✗ Guest not found");
                guestInfoLabel.setForeground(DANGER_COLOR);
                showError("Guest with ID " + guestId + " not found!");
            }
        } catch (NumberFormatException e) {
            showError("Invalid Guest ID format. Please enter a number.");
        } catch (SQLException e) {
            showError("Error searching guest: " + e.getMessage());
        }
    }

    private void checkRoomAvailability(JTextField roomIdField, JTextField checkInField,
                                       JTextField checkOutField, JLabel roomInfoLabel) {
        try {
            if (roomDAO == null) {
                showError("Database not connected");
                return;
            }

            String roomIdText = roomIdField.getText().trim();
            if (roomIdText.isEmpty()) {
                showError("Please enter a Room ID");
                return;
            }

            Long roomId = Long.parseLong(roomIdText);
            Room room = roomDAO.getRoomById(roomId);

            if (room == null) {
                roomInfoLabel.setText("✗ Room not found");
                roomInfoLabel.setForeground(DANGER_COLOR);
                showError("Room with ID " + roomId + " not found!");
                return;
            }

            // Parse dates
            LocalDate checkIn = LocalDate.parse(checkInField.getText().trim());
            LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim());

            // Validate dates
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                showError("Check-out date must be after check-in date!");
                return;
            }

            // Check availability
            boolean isAvailable = roomDAO.isRoomAvailable(roomId, checkIn, checkOut);

            if (isAvailable) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                double totalCost = nights * room.getRatePerNight();

                roomInfoLabel.setText(String.format(
                        "✓ %s (%s) - $%.2f/night - Available for %d nights - Total: $%.2f",
                        room.getRoomCode(), room.getRoomType(), room.getRatePerNight(), nights, totalCost
                ));
                roomInfoLabel.setForeground(SUCCESS_COLOR);
                updateStatus("Room available for selected dates");
            } else {
                roomInfoLabel.setText("✗ Room NOT available for selected dates");
                roomInfoLabel.setForeground(DANGER_COLOR);
                showError("Room is not available for the selected dates. Please choose different dates.");
            }

        } catch (NumberFormatException e) {
            showError("Invalid Room ID format. Please enter a number.");
        } catch (Exception e) {
            showError("Error checking availability: " + e.getMessage());
        }
    }

    private void loadAmenitiesForBooking(JPanel amenityPanel, JCheckBox[] amenityCheckboxes) {
        try {
            if (amenityDAO == null) {
                showError("Database not connected");
                return;
            }

            amenityPanel.removeAll();
            List<Amenity> amenities = amenityDAO.getAllAmenities("available");

            for (int i = 0; i < amenities.size() && i < amenityCheckboxes.length; i++) {
                Amenity amenity = amenities.get(i);
                String label = String.format("%s - $%.2f", amenity.getName(), amenity.getRate());
                amenityCheckboxes[i] = new JCheckBox(label);
                amenityCheckboxes[i].putClientProperty("amenityId", amenity.getAmenityId());
                amenityPanel.add(amenityCheckboxes[i]);
            }

            amenityPanel.revalidate();
            amenityPanel.repaint();
            updateStatus("Loaded " + amenities.size() + " available amenities");

        } catch (SQLException e) {
            showError("Error loading amenities: " + e.getMessage());
        }
    }

    // RESERVATION BOOKING - Full Implementation (Charles Andrew Bondoc)
    private void createReservation(JTextField guestIdField, JTextField roomIdField,
                                   JTextField checkInField, JTextField checkOutField,
                                   JComboBox<String> channelCombo, JCheckBox[] amenityCheckboxes) {
        try {
            if (reservationDAO == null) {
                showError("Database not connected");
                return;
            }

            // Validate inputs
            if (guestIdField.getText().trim().isEmpty() || roomIdField.getText().trim().isEmpty()) {
                showError("Please fill in Guest ID and Room ID");
                return;
            }

            // Parse data
            Long guestId = Long.parseLong(guestIdField.getText().trim());
            Long roomId = Long.parseLong(roomIdField.getText().trim());
            LocalDate checkIn = LocalDate.parse(checkInField.getText().trim());
            LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim());
            String bookingChannel = (String) channelCombo.getSelectedItem();

            // Validate dates
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                showError("Check-out date must be after check-in date!");
                return;
            }

            if (checkIn.isBefore(LocalDate.now())) {
                showError("Check-in date cannot be in the past!");
                return;
            }

            // Collect selected amenities
            List<Long> selectedAmenities = new ArrayList<>();
            for (JCheckBox cb : amenityCheckboxes) {
                if (cb != null && cb.isSelected()) {
                    Long amenityId = (Long) cb.getClientProperty("amenityId");
                    if (amenityId != null) {
                        selectedAmenities.add(amenityId);
                    }
                }
            }

            // Create reservation object
            Reservation reservation = new Reservation(guestId, roomId, checkIn, checkOut, bookingChannel);

            // Confirm before creating
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Create reservation:\n\n" +
                                    "Guest ID: %d\n" +
                                    "Room ID: %d\n" +
                                    "Check-In: %s\n" +
                                    "Check-Out: %s\n" +
                                    "Channel: %s\n" +
                                    "Amenities: %d selected\n\n" +
                                    "Proceed?",
                            guestId, roomId, checkIn, checkOut, bookingChannel, selectedAmenities.size()),
                    "Confirm Reservation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Call DAO to create reservation (with transaction)
            Long reservationId = reservationDAO.createReservation(reservation, selectedAmenities);

            // Success message
            JOptionPane.showMessageDialog(this,
                    String.format("Reservation created successfully!\n\n" +
                                    "Reservation ID: %d\n" +
                                    "Status: Confirmed\n\n" +
                                    "The room status has been updated to 'reserved'.\n" +
                                    "Selected amenities have been linked to the reservation.",
                            reservationId),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            updateStatus("Reservation #" + reservationId + " created successfully");

            // Clear form
            guestIdField.setText("");
            roomIdField.setText("");
            checkInField.setText(LocalDate.now().toString());
            checkOutField.setText(LocalDate.now().plusDays(3).toString());
            channelCombo.setSelectedIndex(1);
            for (JCheckBox cb : amenityCheckboxes) {
                if (cb != null) cb.setSelected(false);
            }

        } catch (NumberFormatException e) {
            showError("Invalid ID format. Please enter valid numbers for Guest ID and Room ID.");
        } catch (java.time.format.DateTimeParseException e) {
            showError("Invalid date format. Please use YYYY-MM-DD format (e.g., 2025-12-25)");
        } catch (SQLException e) {
            showError("Error creating reservation: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void getReservationDetails(JTextField reservationIdField, JLabel reservationInfoLabel) {
        try {
            long resId = Long.parseLong(reservationIdField.getText().trim());
            Reservation res = reservationDAO.getReservationById(resId);
            if (res != null) {
                reservationInfoLabel.setText("Reservation: Room " + res.getRoomId() +
                        ", Status: " + res.getStatus() +
                        ", Check-in: " + res.getCheckIn() +
                        ", Check-out: " + res.getCheckOut());
            } else {
                reservationInfoLabel.setText("Reservation not found.");
            }
        } catch (NumberFormatException e) {
            reservationInfoLabel.setText("Invalid Reservation ID.");
        } catch (SQLException e) {
            reservationInfoLabel.setText("Error: " + e.getMessage());
        }
    }

    private void confirmCheckIn(JTextField reservationIdField, JLabel reservationInfoLabel) {
        try {
            long resId = Long.parseLong(reservationIdField.getText().trim());
            Reservation res = reservationDAO.getReservationById(resId);
            if (res == null) {
                JOptionPane.showMessageDialog(null, "Reservation not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Confirm check-in for Guest: " + res.getGuestName() +
                            ", Reservation ID: " + res.getReservationId() + "?",
                    "Confirm Check-In", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = reservationDAO.checkInGuest(resId);
                if (success) {
                    JOptionPane.showMessageDialog(null, "Guest checked in successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh reservation info display
                    res = reservationDAO.getReservationById(resId);
                    reservationInfoLabel.setText("Reservation: Room " + res.getRoomId() +
                            ", Status: " + res.getStatus() +
                            ", Check-in: " + res.getCheckIn() +
                            ", Check-out: " + res.getCheckOut());
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Reservation ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // CHECK-IN - Placeholder
    private JPanel createCheckInPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JLabel titleLabel = new JLabel("Guest Check-in Confirmation");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(assignLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Guest ID field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Guest ID:"), gbc);
        gbc.gridx = 1;
        JTextField guestIdField = new JTextField(15);
        formPanel.add(guestIdField, gbc);

        gbc.gridx = 2;
        JButton searchGuestBtn = createActionButton("🔍 Search Guest", PRIMARY_COLOR);
        formPanel.add(searchGuestBtn, gbc);

        // Guest info display
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        JLabel guestInfoLabel = new JLabel("");
        guestInfoLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        guestInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(guestInfoLabel, gbc);

        // Reservation combo box
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Select Reservation:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<Reservation> reservationCombo = new JComboBox<>();
        reservationCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Reservation res) {
                    setText("ID: " + res.getReservationId() + " | Room: " + res.getRoomId() + " | Status: " + res.getStatus());
                }
                return this;
            }
        });
        formPanel.add(reservationCombo, gbc);

        // Reservation info display
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        JLabel reservationInfoLabel = new JLabel("");
        reservationInfoLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        reservationInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(reservationInfoLabel, gbc);

        // --- Button Actions ---

        searchGuestBtn.addActionListener(e -> {
            try {
                long guestId = Long.parseLong(guestIdField.getText().trim());
                Guest guest = guestDAO.getGuestById(guestId);
                reservationCombo.removeAllItems(); // Clear previous reservations

                if (guest != null) {
                    guestInfoLabel.setText("Guest: " + guest.getFirstName() + " " + guest.getLastName());
                    // Fetch active reservations for this guest
                    List<Reservation> reservations = reservationDAO.getActiveReservationsByGuestId(guestId);
                    if (reservations.isEmpty()) {
                        reservationInfoLabel.setText("No active reservations found for this guest.");
                    } else {
                        for (Reservation res : reservations) {
                            reservationCombo.addItem(res);
                        }
                        reservationCombo.setSelectedIndex(0);
                        Reservation selected = (Reservation) reservationCombo.getSelectedItem();
                        reservationInfoLabel.setText("Selected Reservation: ID " + selected.getReservationId() +
                                ", Room " + selected.getRoomId() + ", Status " + selected.getStatus());
                    }
                } else {
                    guestInfoLabel.setText("Guest not found.");
                    reservationInfoLabel.setText("");
                }
            } catch (NumberFormatException ex) {
                guestInfoLabel.setText("Invalid Guest ID.");
                reservationInfoLabel.setText("");
            } catch (SQLException ex) {
                guestInfoLabel.setText("Error: " + ex.getMessage());
                reservationInfoLabel.setText("");
            }
        });

        reservationCombo.addActionListener(e -> {
            Reservation selected = (Reservation) reservationCombo.getSelectedItem();
            if (selected != null) {
                reservationInfoLabel.setText("Selected Reservation: ID " + selected.getReservationId() +
                        ", Room " + selected.getRoomId() + ", Status " + selected.getStatus());
            }
        });

        panel.add(formPanel, BorderLayout.CENTER);

        // --- Action Panel ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        JButton checkInBtn = createActionButton("✅ Confirm Check-In", SUCCESS_COLOR);
        checkInBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        checkInBtn.addActionListener(e -> {
            Reservation selected = (Reservation) reservationCombo.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(null, "Please select a reservation.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Confirm check-in for Guest: " + selected.getGuestName() +
                            ", Reservation ID: " + selected.getReservationId() + "?",
                    "Confirm Check-In", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = reservationDAO.checkInGuest(selected.getReservationId());
                    if (success) {
                        JOptionPane.showMessageDialog(null, "Guest checked in successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        reservationInfoLabel.setText("Selected Reservation: ID " + selected.getReservationId() +
                                ", Room " + selected.getRoomId() + ", Status checked-in");
                        selected.setStatus("checked-in"); // Update combo display
                        reservationCombo.repaint();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        actionPanel.add(checkInBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateReservationInfo(Reservation r, JLabel label, JTextField chargeField) {
        if (r == null) return;
        try {
            label.setText("Reservation ID " + r.getReservationId() +
                    ", Room " + r.getRoomId() + ", Status " + r.getStatus());

            double total = checkOutDAO.calculateTotalCharges(r.getReservationId());
            chargeField.setText(String.valueOf(total));

        } catch (SQLException ex) {
            label.setText("Error loading reservation details.");
        }
    }

    // CHECK-OUT - Placeholder
    private JPanel createCheckOutPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ---- Header ----
        JLabel titleLabel = new JLabel("Guest Check-Out & Billing Settlement");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(assignLabel);

        panel.add(headerPanel, BorderLayout.NORTH);

        // ---- Form Panel ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Guest ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Guest ID:"), gbc);

        gbc.gridx = 1;
        JTextField guestIdField = new JTextField(15);
        formPanel.add(guestIdField, gbc);

        gbc.gridx = 2;
        JButton searchGuestBtn = createActionButton("🔍 Search Guest", PRIMARY_COLOR);
        formPanel.add(searchGuestBtn, gbc);

        // Guest info
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        JLabel guestInfoLabel = new JLabel("");
        guestInfoLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        guestInfoLabel.setForeground(PRIMARY_COLOR);
        formPanel.add(guestInfoLabel, gbc);

        // Reservation combo
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Select Reservation:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<Reservation> reservationCombo = new JComboBox<>();

        reservationCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Reservation res) {
                    setText("ID: " + res.getReservationId() +
                            " | Room: " + res.getRoomId() +
                            " | Status: " + res.getStatus());
                }
                return this;
            }
        });

        formPanel.add(reservationCombo, gbc);

        // Reservation info
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        JLabel reservationInfoLabel = new JLabel("");
        reservationInfoLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        reservationInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(reservationInfoLabel, gbc);

        // Total charges
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Total Charges:"), gbc);

        gbc.gridx = 1;
        JTextField totalChargeField = new JTextField(15);
        totalChargeField.setEditable(false);
        formPanel.add(totalChargeField, gbc);

        // Amount paid
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Amount Paid:"), gbc);

        gbc.gridx = 1;
        JTextField amountPaidField = new JTextField(15);
        formPanel.add(amountPaidField, gbc);

        // Payment method
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Payment Method:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> paymentMethodBox = new JComboBox<>(
                new String[]{"Cash", "Card", "Online", "Bank_Transfer", "Other_Mode"}
        );
        formPanel.add(paymentMethodBox, gbc);

        // Transaction reference
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Transaction Ref:"), gbc);

        gbc.gridx = 1;
        JTextField transactionRefField = new JTextField(15);
        formPanel.add(transactionRefField, gbc);

        // Add form panel
        panel.add(formPanel, BorderLayout.CENTER);

        // ---- Action Panel ----
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        JButton checkOutBtn = createActionButton("📤 Confirm Check-Out", DANGER_COLOR);
        checkOutBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

        actionPanel.add(checkOutBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        // ---- EVENT HANDLERS ----

        // Search guest → load checked-in reservations
        searchGuestBtn.addActionListener(e -> {
            try {
                long guestId = Long.parseLong(guestIdField.getText().trim());

                Guest guest = guestDAO.getGuestById(guestId);
                reservationCombo.removeAllItems();

                if (guest != null) {
                    guestInfoLabel.setText("Guest: " + guest.getFirstName() + " " + guest.getLastName());

                    List<Reservation> reservations =
                            reservationDAO.getCheckedInReservationsByGuestId(guestId);

                    if (reservations.isEmpty()) {
                        reservationInfoLabel.setText("No CHECKED-IN reservations for this guest.");
                    } else {
                        for (Reservation r : reservations) reservationCombo.addItem(r);

                        Reservation selected = (Reservation) reservationCombo.getSelectedItem();
                        updateReservationInfo(selected, reservationInfoLabel, totalChargeField);

                    }
                } else {
                    guestInfoLabel.setText("Guest not found.");
                    reservationInfoLabel.setText("");
                }

            } catch (Exception ex) {
                guestInfoLabel.setText("Error: " + ex.getMessage());
            }
        });

        // Load details when selecting reservation
        reservationCombo.addActionListener(e -> {
            Reservation selected = (Reservation) reservationCombo.getSelectedItem();
            updateReservationInfo(selected, reservationInfoLabel, totalChargeField);
        });

        // Process Check-Out
        checkOutBtn.addActionListener(e -> {

            Reservation selected = (Reservation) reservationCombo.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(null, "Please select a reservation.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double amountPaid = Double.parseDouble(amountPaidField.getText());
                String method = paymentMethodBox.getSelectedItem().toString();
                String reference = transactionRefField.getText();

                int confirm = JOptionPane.showConfirmDialog(null,
                        "Confirm CHECK-OUT for Reservation ID: " + selected.getReservationId() + " ?",
                        "Confirm Check-Out", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {

                    boolean ok = checkOutDAO.processCheckOut(
                            selected.getReservationId(),
                            amountPaid,
                            method,
                            reference
                    );

                    if (ok) {
                        JOptionPane.showMessageDialog(null, "Guest checked out successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        reservationInfoLabel.setText("Reservation checked out successfully.");
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        });

        return panel;
    }

    // INVENTORY RESTOCK - Placeholder
    private JPanel createInventoryRestockPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Inventory Restocking");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Vener Mariano");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(assignLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JLabel infoLabel = new JLabel("<html><center>Implement inventory restock transaction<br>" +
                "Use RestockDAO.processRestock() method</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(infoLabel, BorderLayout.CENTER);

        return panel;
    }

    // AMENITY RENTAL - Placeholder
    private JPanel createAmenityRentalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Amenity Rental");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        assignLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(assignLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JLabel infoLabel = new JLabel("<html><center>Implement amenity rental transaction<br>" +
                "Use AmenityRentalDAO.processAmenityRental() method</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(infoLabel, BorderLayout.CENTER);

        return panel;
    }

    // REPORTS PANEL
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Reports Generation");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel reportsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        reportsPanel.setOpaque(false);

        // Room Occupancy Report (Charles Andrew Bondoc)
        reportsPanel.add(createReportCard(
                "Room Occupancy Report",
                "Number of days each room was reserved in a month",
                "Assigned to: Charles Andrew Bondoc",
                PRIMARY_COLOR,
                "occupancy"
        ));

        // Revenue Report (Ryan James Malapitan)
        reportsPanel.add(createReportCard(
                "Revenue Report",
                "Total revenue generated by each room per month",
                "Assigned to: Ryan James Malapitan",
                SUCCESS_COLOR,
                "revenue"
        ));

        // Inventory Report (Vener Mariano)
        reportsPanel.add(createReportCard(
                "Inventory Report",
                "Total inventory items used in a month",
                "Assigned to: Vener Mariano",
                WARNING_COLOR,
                "inventory"
        ));

        // Amenities Report (Daniel Pamintuan)
        reportsPanel.add(createReportCard(
                "Amenities Report",
                "Total number of times amenities were availed in a month",
                "Assigned to: Daniel Pamintuan",
                new Color(155, 89, 182),
                "amenities"
        ));

        panel.add(reportsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReportCard(String title, String description, String assignedTo, Color color, String reportType) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        titleLabel.setForeground(color);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        descLabel.setForeground(new Color(127, 140, 141));

        JLabel assignLabel = new JLabel(assignedTo);
        assignLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        assignLabel.setForeground(new Color(149, 165, 166));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(descLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(assignLabel);

        JPanel paramPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        paramPanel.setOpaque(false);
        paramPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        paramPanel.add(new JLabel("Year:"));
        JComboBox<Integer> yearCombo = new JComboBox<>(new Integer[]{2025, 2024, 2023, 2022});
        paramPanel.add(yearCombo);
        paramPanel.add(new JLabel("Month:"));
        JComboBox<Integer> monthCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        monthCombo.setSelectedIndex(10); // November (index 10)
        paramPanel.add(monthCombo);

        JButton generateBtn = createActionButton("📊 Generate Report", color);
        generateBtn.addActionListener(e -> generateReport(reportType, (Integer)yearCombo.getSelectedItem(), (Integer)monthCombo.getSelectedItem()));

        card.add(textPanel, BorderLayout.NORTH);
        card.add(paramPanel, BorderLayout.CENTER);
        card.add(generateBtn, BorderLayout.SOUTH);

        return card;
    }

    private void generateReport(String reportType, int year, int month) {
        try {
            List<String[]> reportData = null;
            String[] columns = null;
            String reportTitle = "";

            switch(reportType) {
                case "occupancy":
                    reportData = reportsDAO.getRoomOccupancyReport(year, month);
                    columns = new String[]{"Room Code", "Room Type", "Days Reserved"};
                    reportTitle = "Room Occupancy Report";
                    break;
                case "revenue":
                    reportData = reportsDAO.getRevenueReport(year, month);
                    columns = new String[]{"Room Code", "Room Type", "Rate/Night", "Total Revenue"};
                    reportTitle = "Revenue Report";
                    break;
                case "inventory":
                    reportData = reportsDAO.getInventoryReport(year, month);
                    columns = new String[]{"Item Name", "Supplier", "Total Restocked", "Current Quantity"};
                    reportTitle = "Inventory Report";
                    break;
                case "amenities":
                    reportData = reportsDAO.getAmenitiesReport(year, month);
                    columns = new String[]{"Amenity Name", "Rate", "Times Rented", "Total Quantity", "Total Revenue"};
                    reportTitle = "Amenities Report";
                    break;
            }

            if (reportData != null && columns != null) {
                showReportDialog(reportTitle, year, month, columns, reportData);
            }

        } catch (SQLException e) {
            showError("Error generating report: " + e.getMessage());
        }
    }

    private void showReportDialog(String title, int year, int month, String[] columns, List<String[]> data) {
        JDialog dialog = new JDialog(this, title + " - " + getMonthName(month) + " " + year, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel(title + " - " + getMonthName(month) + " " + year);
        headerLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        panel.add(headerLabel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (String[] row : data) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = createActionButton("Close", SECONDARY_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return (month >= 1 && month <= 12) ? months[month - 1] : "Invalid";
    }

    // Helper methods
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        button.setForeground(Color.BLACK);
        button.setBackground(color);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        updateStatus("Error: " + message);
    }

    public static void main(String[] args) {
        // Set system look and feel before creating GUI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new BeachResortManagementGUI();
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start application:\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
