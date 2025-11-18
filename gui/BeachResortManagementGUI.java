package gui;

import dao.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.*;
import javax.swing.JSpinner.DateEditor;
import javax.swing.SpinnerDateModel;

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
    private DashboardDAO dashboardDAO;

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BG_COLOR = new Color(236, 240, 241);
    private final Color TEXT_COLOR = new Color(0, 0, 0);

    //dashboard labels
    private JLabel totalGuestsLabel;
    private JLabel activeReservationsLabel;
    private JLabel availableRoomsLabel;
    private JLabel revenueTodayLabel;
    private JLabel occupiedRoomsLabel;
    private JLabel amenitiesRentedLabel;
    private JLabel inventoryItemsLabel;
    private JLabel pendingCheckoutsLabel;

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
            dashboardDAO = new DashboardDAO();
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
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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

        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setOpaque(false);

        // Initialize JLabels
        totalGuestsLabel = new JLabel("0");
        activeReservationsLabel = new JLabel("0");
        availableRoomsLabel = new JLabel("0");
        revenueTodayLabel = new JLabel("₱0.00");
        occupiedRoomsLabel = new JLabel("0");
        amenitiesRentedLabel = new JLabel("0");
        inventoryItemsLabel = new JLabel("0");
        pendingCheckoutsLabel = new JLabel("0");

        // Add stat cards
        statsPanel.add(createStatCard("Total Guests", totalGuestsLabel, SUCCESS_COLOR));
        statsPanel.add(createStatCard("Active Reservations", activeReservationsLabel, PRIMARY_COLOR));
        statsPanel.add(createStatCard("Available Rooms", availableRoomsLabel, SUCCESS_COLOR));
        statsPanel.add(createStatCard("Revenue Today", revenueTodayLabel, WARNING_COLOR));
        statsPanel.add(createStatCard("Occupied Rooms", occupiedRoomsLabel, DANGER_COLOR));
        statsPanel.add(createStatCard("Amenities Rented", amenitiesRentedLabel, SECONDARY_COLOR));
        statsPanel.add(createStatCard("Inventory Items", inventoryItemsLabel, new Color(155, 89, 182)));
        statsPanel.add(createStatCard("Pending Checkouts", pendingCheckoutsLabel, WARNING_COLOR));

        panel.add(statsPanel, BorderLayout.CENTER);

        JButton refreshBtn = createActionButton("🔄 Refresh Dashboard", SECONDARY_COLOR);
        refreshBtn.addActionListener(e -> loadDashboardStats());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateCard(Component card, int index, String value) {
        if (card instanceof JPanel panel) {
            Component[] comps = panel.getComponents();
            JLabel valueLabel = (JLabel) comps[1]; // title = [0], value = [1]
            valueLabel.setText(value);
        }
    }

    private void loadDashboardStats() {
        try {
            DashboardDAO dao = new DashboardDAO();

            totalGuestsLabel.setText(String.valueOf(dao.getTotalGuests()));
            activeReservationsLabel.setText(String.valueOf(dao.getActiveReservations()));
            availableRoomsLabel.setText(String.valueOf(dao.getAvailableRooms()));
            revenueTodayLabel.setText("₱" + String.format("%.2f", dao.getRevenueToday()));
            occupiedRoomsLabel.setText(String.valueOf(dao.getOccupiedRooms()));
            amenitiesRentedLabel.setText(String.valueOf(dao.getAmenitiesRented()));
            inventoryItemsLabel.setText(String.valueOf(dao.getInventoryItems()));
            pendingCheckoutsLabel.setText(String.valueOf(dao.getPendingCheckouts()));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading dashboard: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(127, 140, 141));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Charles Andrew Bondoc");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        inputPanel.add(new JLabel("Phone (Optiona):"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("Email (Optional):"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Passport No (Optional):"));
        inputPanel.add(passportField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Add New Guest", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String passport = passportField.getText().trim();

            if(firstName.isEmpty() || lastName.isEmpty()) {
                showError("First Name and Last Name are both required.");
                return;
            }

            try {
                Guest guest = new Guest(firstName, lastName, phone, email, passport);
                Long id = guestDAO.addGuest(guest);
                JOptionPane.showMessageDialog(this, "Guest Added Successfully with ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
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
            inputPanel.add(new JLabel("Phone (Optional):"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Email (Optional):"));
            inputPanel.add(emailField);
            inputPanel.add(new JLabel("Passport No (Optional):"));
            inputPanel.add(passportField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel,
                    "Edit Guest", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String passport = passportField.getText().trim();

                if(firstName.isEmpty() || lastName.isEmpty()) {
                    showError("First Name and Last Name are required.");
                    return;
                }

                guest.setFirstName(firstName);
                guest.setLastName(lastName);
                guest.setPhone(phone);
                guest.setEmail(email);
                guest.setPassportNo(passport);

                guestDAO.updateGuest(guest);
                JOptionPane.showMessageDialog(this, "Guest updated Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
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
                        String.format("₱%.2f", room.getRatePerNight()),
                        room.getStatus()
                });
            }
            updateStatus("Loaded " + rooms.size() + " rooms");
        } catch (SQLException e) {
            showError("Error loading rooms: " + e.getMessage());
        }
    }

    //helper to generate room code based on the type and the timestamp
    //time stamp is used at the end of the code to make the code unique.
    private String generateRoomCode(String roomType) {
        String prefix;
        if (roomType == null) {
            prefix = "RM";
        }else {
            switch (roomType.toLowerCase()) {
                case "standard" -> prefix = "STD";
                case "deluxe"   -> prefix = "DLX";
                case "suite"    -> prefix = "STE";
                case "cottage"  -> prefix = "COT";
                default         -> prefix = "RM";
            }
        }
        // Use last digits of current time to keep it "unique enough"
        long suffix = System.currentTimeMillis() % 1_000_000;
        return prefix + "-" + suffix;
    }

    private void addRoom(DefaultTableModel model) {
        if (roomDAO == null) {
            JOptionPane.showMessageDialog(this, "Database connection not ready.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //removed room code field *it was here before*
        JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite", "Cottage"});
        JComboBox<String> bedTypeCombo = new JComboBox<>(new String[]{"Single", "Twin", "Queen", "King"});
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JTextField rateField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10)); //changed rows from 6 to 5 because we removed roomcode input
        //removed roomcode field it was here before
        //and here
        inputPanel.add(new JLabel("Room Type:"));
        inputPanel.add(roomTypeCombo);
        inputPanel.add(new JLabel("Bed Type (Optional):"));
        inputPanel.add(bedTypeCombo);
        inputPanel.add(new JLabel("Max Capacity:"));
        inputPanel.add(capacitySpinner);
        inputPanel.add(new JLabel("Rate per Night:"));
        inputPanel.add(rateField);
        inputPanel.add(new JLabel("Description (Optional):"));
        inputPanel.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add New Room", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            //removed string roomcode
            String roomType = (String) roomTypeCombo.getSelectedItem();
            String bedType  = (String) bedTypeCombo.getSelectedItem();
            int maxCap      = (Integer) capacitySpinner.getValue();
            String rateText = rateField.getText().trim();
            String desc = descArea.getText().trim();

            //removed if room code is empty (it was here before)
            if(roomType == null || roomType.trim().isEmpty()) {
                showError("Room Type is Required!");
                return;
            }
            if(maxCap <= 0) {
                showError("Max Capacity must be atleast 1.");
                return;
            }
            double rate;
            try {
                rate = Double.parseDouble(rateText);
            } catch (NumberFormatException ex) {
                showError("Invalid rate format. Please enter a valid number.");
                return;
            }
            if(rate <= 0) {
                showError("Rate per night must be greater than 0.");
                return;
            }
            //auto generate the room code
            String roomCode = generateRoomCode(roomType);
            try {
                Room room = new Room(roomCode, roomType, bedType, maxCap, rate);
                room.setDescription(desc);

                long id = roomDAO.addRoom(room);
                JOptionPane.showMessageDialog(this, "Room added successfully with ID: " + id + "\n Generated Room Code: " +roomCode, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRoomData(model);
            } catch (SQLException e) {
                showError("Error adding room: " + e.getMessage());
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
            //show room code as read only
            JLabel roomCodeLabel = new JLabel(room.getRoomCode());
            //removed roomcodefield
            JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite", "Cottage"});
            roomTypeCombo.setSelectedItem(room.getRoomType());
            JComboBox<String> bedTypeCombo = new JComboBox<>(new String[]{"Single", "Twin", "Queen", "King"});
            bedTypeCombo.setSelectedItem(room.getBedType());
            JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(room.getMaxCapacity(), 1, 10, 1));
            JTextField rateField = new JTextField(String.valueOf(room.getRatePerNight()), 20);
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"available", "reserved", "occupied", "maintenance"});
            statusCombo.setSelectedItem(room.getStatus());

            JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
            inputPanel.add(new JLabel("Room Code:")); //read-only display
            inputPanel.add(roomCodeLabel); //changed from field to label.
            inputPanel.add(new JLabel("Room Type:"));
            inputPanel.add(roomTypeCombo);
            inputPanel.add(new JLabel("Bed Type (Optional):"));
            inputPanel.add(bedTypeCombo);
            inputPanel.add(new JLabel("Max Capacity:"));
            inputPanel.add(capacitySpinner);
            inputPanel.add(new JLabel("Rate per Night:"));
            inputPanel.add(rateField);
            inputPanel.add(new JLabel("Status:"));
            inputPanel.add(statusCombo);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Edit Room", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                //removed roomcode
                String roomType = (String) roomTypeCombo.getSelectedItem();
                String bedType  = (String) bedTypeCombo.getSelectedItem();
                int maxCap      = (Integer) capacitySpinner.getValue();
                String rateText = rateField.getText().trim();
                String status   = (String) statusCombo.getSelectedItem();

                //removed if room code
                if (roomType == null || roomType.trim().isEmpty()) {
                    showError("Room Type is required.");
                    return;
                }
                if (status == null || status.trim().isEmpty()) {
                    showError("Status is required.");
                    return;
                }
                if (maxCap <= 0) {
                    showError("Max Capacity must be at least 1.");
                    return;
                }

                double rate;
                try {
                    rate = Double.parseDouble(rateText);
                } catch (NumberFormatException ex) {
                    showError("Invalid rate format. Please enter a valid number.");
                    return;
                }
                if (rate <= 0) {
                    showError("Rate per night must be greater than 0.");
                    return;
                }

                //removed set roomcode
                room.setRoomType(roomType);
                room.setBedType(bedType);
                room.setMaxCapacity(maxCap);
                room.setRatePerNight(rate);
                room.setStatus(status);

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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Vener Mariano");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
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
                        String.format("₱%.2f", amenity.getRate()),
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

        // no row selected
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an amenity to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Daniel Pamintuan");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
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

        // no row selected
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an inventory item to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Charles Andrew Bondoc");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        guestInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        roomInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        roomInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(roomInfoLabel, gbc);

        // Check-in Date with Calendar Picker
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Check-In Date:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JPanel checkInPanel = new JPanel(new BorderLayout(5, 0));
        checkInPanel.setOpaque(false);

        // Create date picker for check-in
        DatePickerPanel checkInDatePicker = new DatePickerPanel(LocalDate.now());
        checkInPanel.add(checkInDatePicker, BorderLayout.CENTER);

        formPanel.add(checkInPanel, gbc);

        // Check-out Date with Calendar Picker
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Check-Out Date:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JPanel checkOutPanel = new JPanel(new BorderLayout(5, 0));
        checkOutPanel.setOpaque(false);

        // Create date picker for check-out (3 days from now)
        DatePickerPanel checkOutDatePicker = new DatePickerPanel(LocalDate.now().plusDays(3));
        checkOutPanel.add(checkOutDatePicker, BorderLayout.CENTER);

        formPanel.add(checkOutPanel, gbc);

        // Booking Channel
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Booking Channel:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<String> channelCombo = new JComboBox<>(new String[]{"Walk-In", "Online", "Phone", "Agent"});
        channelCombo.setSelectedIndex(1);
        formPanel.add(channelCombo, gbc);

        // Amenities selection
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        JLabel amenityLabel = new JLabel("Select Amenities (Optional):");
        amenityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(amenityLabel, gbc);

        gbc.gridy = 8;
        JPanel amenityPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        amenityPanel.setOpaque(false);

        JCheckBox[] amenityCheckboxes = new JCheckBox[8];
        JButton loadAmenitiesBtn = createActionButton("Load Available Amenities", SECONDARY_COLOR);
        loadAmenitiesBtn.addActionListener(e -> loadAmenitiesForBooking(amenityPanel, amenityCheckboxes));
        formPanel.add(loadAmenitiesBtn, gbc);

        gbc.gridy = 9;
        formPanel.add(amenityPanel, gbc);

        // Availability check button action
        checkAvailBtn.addActionListener(e -> checkRoomAvailabilityWithDatePicker(
                roomIdField, checkInDatePicker, checkOutDatePicker, roomInfoLabel
        ));

        panel.add(formPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);

        JButton bookBtn = createActionButton("✅ Create Reservation", SUCCESS_COLOR);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookBtn.addActionListener(e -> createReservationWithDatePicker(
                guestIdField, roomIdField, checkInDatePicker, checkOutDatePicker,
                channelCombo, amenityCheckboxes
        ));

        JButton clearBtn = createActionButton("🔄 Clear Form", WARNING_COLOR);
        clearBtn.addActionListener(e -> {
            guestIdField.setText("");
            roomIdField.setText("");
            guestInfoLabel.setText("");
            roomInfoLabel.setText("");
            channelCombo.setSelectedIndex(1);
            checkInDatePicker.setDate(LocalDate.now());
            checkOutDatePicker.setDate(LocalDate.now().plusDays(3));
            for (JCheckBox cb : amenityCheckboxes) {
                if (cb != null) cb.setSelected(false);
            }
        });

        actionPanel.add(bookBtn);
        actionPanel.add(clearBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    class DatePickerPanel extends JPanel {
        private LocalDate selectedDate;
        private JTextField dateField;
        private JButton calendarButton;
        private JDialog calendarDialog;
        private JPanel calendarPanel;
        private JLabel monthYearLabel;
        private int displayMonth;
        private int displayYear;

        public DatePickerPanel(LocalDate initialDate) {
            this.selectedDate = initialDate;
            this.displayMonth = initialDate.getMonthValue();
            this.displayYear = initialDate.getYear();

            setLayout(new BorderLayout(5, 0));
            setOpaque(false);

            // Date display field
            dateField = new JTextField(12);
            dateField.setEditable(false);
            dateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dateField.setText(formatDate(selectedDate));

            // Calendar button
            calendarButton = new JButton("📅");
            calendarButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            calendarButton.setPreferredSize(new Dimension(40, 25));
            calendarButton.setFocusPainted(false);

            calendarButton.addActionListener(e -> showCalendar());

            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
        }

        private void showCalendar() {
            if (calendarDialog != null && calendarDialog.isVisible()) {
                calendarDialog.dispose();
                return;
            }

            calendarDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
            calendarDialog.setLayout(new BorderLayout());
            calendarDialog.setUndecorated(false);

            // Header with month/year navigation
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton prevButton = new JButton("◀");
            prevButton.setFocusPainted(false);
            prevButton.addActionListener(e -> {
                displayMonth--;
                if (displayMonth < 1) {
                    displayMonth = 12;
                    displayYear--;
                }
                updateCalendar();
            });

            JButton nextButton = new JButton("▶");
            nextButton.setFocusPainted(false);
            nextButton.addActionListener(e -> {
                displayMonth++;
                if (displayMonth > 12) {
                    displayMonth = 1;
                    displayYear++;
                }
                updateCalendar();
            });

            monthYearLabel = new JLabel(getMonthName(displayMonth) + " " + displayYear);
            monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            monthYearLabel.setForeground(Color.WHITE);
            monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);

            headerPanel.add(prevButton, BorderLayout.WEST);
            headerPanel.add(monthYearLabel, BorderLayout.CENTER);
            headerPanel.add(nextButton, BorderLayout.EAST);

            calendarDialog.add(headerPanel, BorderLayout.NORTH);

            // Calendar grid
            calendarPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            calendarPanel.setBackground(Color.WHITE);

            updateCalendar();

            calendarDialog.add(calendarPanel, BorderLayout.CENTER);

            // Footer with Clear and Today buttons
            JPanel footerPanel = new JPanel(new FlowLayout());
            footerPanel.setBackground(Color.WHITE);

            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(e -> {
                calendarDialog.dispose();
            });

            JButton todayButton = new JButton("Today");
            todayButton.addActionListener(e -> {
                LocalDate today = LocalDate.now();
                setDate(today);
                displayMonth = today.getMonthValue();
                displayYear = today.getYear();
                calendarDialog.dispose();
            });

            footerPanel.add(clearButton);
            footerPanel.add(todayButton);

            calendarDialog.add(footerPanel, BorderLayout.SOUTH);

            calendarDialog.pack();
            calendarDialog.setLocationRelativeTo(this);
            calendarDialog.setVisible(true);
        }

        private void updateCalendar() {
            calendarPanel.removeAll();
            monthYearLabel.setText(getMonthName(displayMonth) + " " + displayYear);

            // Day headers
            String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
            for (String day : days) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setForeground(new Color(127, 140, 141));
                calendarPanel.add(label);
            }

            // Get first day of month and number of days
            LocalDate firstDay = LocalDate.of(displayYear, displayMonth, 1);
            int daysInMonth = firstDay.lengthOfMonth();
            int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0

            // Add empty cells for days before month starts
            for (int i = 0; i < startDayOfWeek; i++) {
                calendarPanel.add(new JLabel(""));
            }

            // Add day buttons
            LocalDate today = LocalDate.now();
            for (int day = 1; day <= daysInMonth; day++) {
                final int dayNum = day;
                LocalDate date = LocalDate.of(displayYear, displayMonth, day);

                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dayButton.setFocusPainted(false);
                dayButton.setBorderPainted(true);
                dayButton.setContentAreaFilled(true);

                // Highlight today
                if (date.equals(today)) {
                    dayButton.setBackground(SECONDARY_COLOR);
                    dayButton.setForeground(Color.WHITE);
                    dayButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    dayButton.setBackground(Color.WHITE);
                    dayButton.setForeground(Color.BLACK);
                }

                // Highlight selected date
                if (date.equals(selectedDate)) {
                    dayButton.setBackground(PRIMARY_COLOR);
                    dayButton.setForeground(Color.WHITE);
                    dayButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }

                dayButton.addActionListener(e -> {
                    LocalDate newDate = LocalDate.of(displayYear, displayMonth, dayNum);
                    setDate(newDate);
                    calendarDialog.dispose();
                });

                calendarPanel.add(dayButton);
            }

            calendarPanel.revalidate();
            calendarPanel.repaint();
        }

        private String formatDate(LocalDate date) {
            return String.format("%02d/%02d/%04d", date.getMonthValue(), date.getDayOfMonth(), date.getYear());
        }

        private String getMonthName(int month) {
            String[] months = {"", "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            return months[month];
        }

        public LocalDate getDate() {
            return selectedDate;
        }

        public void setDate(LocalDate date) {
            this.selectedDate = date;
            this.displayMonth = date.getMonthValue();
            this.displayYear = date.getYear();
            dateField.setText(formatDate(date));
        }
    }


    // Helper method to update day combo based on selected year and month
    private void updateDayCombo(JComboBox<Integer> dayCombo, int year, int month) {
        int currentSelection = dayCombo.getSelectedItem() != null ? (Integer)dayCombo.getSelectedItem() : 1;
        dayCombo.removeAllItems();

        // Get number of days in the month
        int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();

        for (int i = 1; i <= daysInMonth; i++) {
            dayCombo.addItem(i);
        }

        // Restore previous selection if valid
        if (currentSelection <= daysInMonth) {
            dayCombo.setSelectedItem(currentSelection);
        } else {
            dayCombo.setSelectedItem(daysInMonth);
        }
    }

    // Helper method to get LocalDate from dropdowns
    private LocalDate getDateFromDropdowns(JComboBox<Integer> yearCombo,
                                           JComboBox<String> monthCombo,
                                           JComboBox<Integer> dayCombo) {
        int year = (Integer) yearCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        int day = (Integer) dayCombo.getSelectedItem();
        return LocalDate.of(year, month, day);
    }

// Updated helper methods for date picker

    private void checkRoomAvailabilityWithDatePicker(JTextField roomIdField,
                                                     DatePickerPanel checkInPicker,
                                                     DatePickerPanel checkOutPicker,
                                                     JLabel roomInfoLabel) {
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

            LocalDate checkIn = checkInPicker.getDate();
            LocalDate checkOut = checkOutPicker.getDate();

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
                        "✓ %s (%s) - ₱%.2f/night - Available for %d nights - Total: ₱%.2f",
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

    private void createReservationWithDatePicker(JTextField guestIdField, JTextField roomIdField,
                                                 DatePickerPanel checkInPicker,
                                                 DatePickerPanel checkOutPicker,
                                                 JComboBox<String> channelCombo,
                                                 JCheckBox[] amenityCheckboxes) {
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
            LocalDate checkIn = checkInPicker.getDate();
            LocalDate checkOut = checkOutPicker.getDate();
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

            // Call DAO to create reservation
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
            channelCombo.setSelectedIndex(1);
            checkInPicker.setDate(LocalDate.now());
            checkOutPicker.setDate(LocalDate.now().plusDays(3));
            for (JCheckBox cb : amenityCheckboxes) {
                if (cb != null) cb.setSelected(false);
            }

        } catch (NumberFormatException e) {
            showError("Invalid ID format. Please enter valid numbers for Guest ID and Room ID.");
        } catch (SQLException e) {
            showError("Error creating reservation: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
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
                roomInfoLabel.setText("Room not found");
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
                        "%s (%s) - ₱%.2f/night - Available for %d nights - Total: ₱%.2f",
                        room.getRoomCode(), room.getRoomType(), room.getRatePerNight(), nights, totalCost
                ));
                roomInfoLabel.setForeground(SUCCESS_COLOR);
                updateStatus("Room available for selected dates");
            } else {
                roomInfoLabel.setText("Room NOT available for selected dates");
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
                String label = String.format("%s - ₱%.2f", amenity.getName(), amenity.getRate());
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Ryan James Malapitan");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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

    // INVENTORY RESTOCKING - Full Implementation (Vener Mariano)
    private JPanel createInventoryRestockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        RestockDAO restockDAO = new RestockDAO();
        InventoryDAO inventoryDAO = new InventoryDAO();

        // ---------- HEADER ----------
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


        // ---------- FORM ----------

        //the big one
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ITEM SELECTOR (ID + name)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Select Item:"), gbc);

        JComboBox<ItemWrapper> itemCombo = new JComboBox<>();
        try {
            List<InventoryItem> items = inventoryDAO.getAllInventoryItems();

            for (InventoryItem itm : items) {
                itemCombo.addItem(new ItemWrapper(itm.getItemId(), itm.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gbc.gridx = 1;
        formPanel.add(itemCombo, gbc);


        // SUPPLIER
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Supplier:"), gbc);

        JTextField supplierField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(supplierField, gbc);


        // QUANTITY
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Quantity:"), gbc);

        JTextField qtyField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(qtyField, gbc);


        // NOTES
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Notes:"), gbc);

        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(notesArea), gbc);

        // DATE
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Restock Date (YYYY-MM-DD):"), gbc);

        JTextField dateField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        // ---------- INVENTORY TABLE ----------
        JTable inventoryTable = new JTable();
        JScrollPane tableScroll = new JScrollPane(inventoryTable);
        tableScroll.setPreferredSize(new Dimension(450, 170));
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(tableScroll, gbc);

        refreshInventoryTable(inventoryTable, inventoryDAO);

        // ---------- BUTTON ----------
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);

        JButton processBtn = createActionButton("Process Restock", SUCCESS_COLOR);
        processBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

        actionPanel.add(processBtn);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        // ---------- BUTTON ACTION ----------
        processBtn.addActionListener(e -> {
            ItemWrapper wrapper = (ItemWrapper) itemCombo.getSelectedItem();

            if (wrapper == null) {
                JOptionPane.showMessageDialog(panel, "No item selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Long itemId = wrapper.id;
            String supplier = supplierField.getText().trim();
            String notes = notesArea.getText().trim();

            String dateStr = dateField.getText().trim();
            java.sql.Date restockDate;

            try {
                // Parse user input to LocalDate, then convert to java.sql.Date
                LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                restockDate = java.sql.Date.valueOf(localDate);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid date format. Please use YYYY-MM-DD format (e.g., 2025-12-25)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity;

            try {
                quantity = Integer.parseInt(qtyField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Quantity must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Long restockId = restockDAO.processRestock(itemId, supplier, quantity, notes, restockDate);

                JOptionPane.showMessageDialog(panel,
                        "Restock successful! (ID: " + restockId + ")",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshInventoryTable(inventoryTable, inventoryDAO);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Restock failed:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // Helper class to store ID+name while showing name in JComboBox
    private static class ItemWrapper {
        Long id;
        String name;

        public ItemWrapper(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;  // display name only
        }
    }

    // Refresh table method
    private void refreshInventoryTable(JTable table, InventoryDAO dao) {
        try {
            List<InventoryItem> items = dao.getAllInventoryItems();

            items.sort(Comparator.comparingLong(InventoryItem::getItemId)); // sort items before populating table

            String[] cols = {"ID", "Item Name", "Quantity"};
            Object[][] data = new Object[items.size()][3];

            for (int i = 0; i < items.size(); i++) {
                data[i][0] = items.get(i).getItemId();
                data[i][1] = items.get(i).getName();
                data[i][2] = items.get(i).getQuantityOnHand();
            }

            // make table uneditable
            DefaultTableModel model = new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            table.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // AMENITY RENTAL - Assigned to Daniel Pamintuan
    private JPanel createAmenityRentalPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Amenity Rental");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel assignLabel = new JLabel("Assigned to: Daniel Pamintuan");
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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

        gbc.gridx = 1;
        JTextField guestIdField = new JTextField(15);
        formPanel.add(guestIdField, gbc);

        gbc.gridx = 2;
        JButton searchGuestBtn = createActionButton("🔍 Search Guest", PRIMARY_COLOR);
        formPanel.add(searchGuestBtn, gbc);

        // Guest info display
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        JLabel guestInfoLabel = new JLabel("");
        guestInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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
        reservationInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        reservationInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(reservationInfoLabel, gbc);

        // Amenity selection
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Select Amenity:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<Amenity> amenityCombo = new JComboBox<>();
        amenityCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Amenity amenity) {
                    setText(String.format("%s - ₱%.2f (Status: %s)",
                            amenity.getName(), amenity.getRate(), amenity.getAvailability()));
                }
                return this;
            }
        });
        formPanel.add(amenityCombo, gbc);

        // Load amenities button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        JButton loadAmenitiesBtn = createActionButton("🔄 Load Available Amenities", SECONDARY_COLOR);
        formPanel.add(loadAmenitiesBtn, gbc);

        // Amenity info display
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        JLabel amenityInfoLabel = new JLabel("");
        amenityInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        amenityInfoLabel.setForeground(SUCCESS_COLOR);
        formPanel.add(amenityInfoLabel, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        formPanel.add(quantitySpinner, gbc);

        // Rental Start DateTime
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Rental Start (YYYY-MM-DD HH:MM):"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField rentStartField = new JTextField(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        formPanel.add(rentStartField, gbc);

        // Rental End DateTime
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Rental End (YYYY-MM-DD HH:MM):"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField rentEndField = new JTextField(LocalDateTime.now().plusHours(4).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        formPanel.add(rentEndField, gbc);

        // Active rentals display area
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 3;
        JLabel activeRentalsLabel = new JLabel("Active Rentals:");
        activeRentalsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(activeRentalsLabel, gbc);

        gbc.gridy = 11;
        JTextArea activeRentalsArea = new JTextArea(5, 40);
        activeRentalsArea.setEditable(false);
        activeRentalsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane rentalsScrollPane = new JScrollPane(activeRentalsArea);
        formPanel.add(rentalsScrollPane, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Button Actions

        // Search Guest
        searchGuestBtn.addActionListener(e -> {
            try {
                if (guestDAO == null || reservationDAO == null) {
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
                reservationCombo.removeAllItems();
                activeRentalsArea.setText("");

                if (guest != null) {
                    guestInfoLabel.setText("✓ Guest: " + guest.getFirstName() + " " + guest.getLastName() + " (" + guest.getEmail() + ")");
                    guestInfoLabel.setForeground(SUCCESS_COLOR);

                    // Load checked-in reservations
                    List<Reservation> reservations = reservationDAO.getCheckedInReservationsByGuestId(guestId);
                    if (reservations.isEmpty()) {
                        reservationInfoLabel.setText("⚠ No checked-in reservations found. Guest must be checked-in to rent amenities.");
                        reservationInfoLabel.setForeground(WARNING_COLOR);
                    } else {
                        for (Reservation res : reservations) {
                            reservationCombo.addItem(res);
                        }
                        Reservation selected = (Reservation) reservationCombo.getSelectedItem();
                        if (selected != null) {
                            reservationInfoLabel.setText("✓ Reservation ID: " + selected.getReservationId() +
                                    " | Room: " + selected.getRoomId() + " | Status: " + selected.getStatus());
                            reservationInfoLabel.setForeground(SUCCESS_COLOR);
                        }
                    }

                    // Load active rentals
                    loadActiveRentals(guestId, activeRentalsArea);

                } else {
                    guestInfoLabel.setText("✗ Guest not found");
                    guestInfoLabel.setForeground(DANGER_COLOR);
                    reservationInfoLabel.setText("");
                }

            } catch (NumberFormatException ex) {
                showError("Invalid Guest ID format. Please enter a number.");
            } catch (Exception ex) {
                showError("Error searching guest: " + ex.getMessage());
            }
        });

        // Update reservation info when selection changes
        reservationCombo.addActionListener(e -> {
            Reservation selected = (Reservation) reservationCombo.getSelectedItem();
            if (selected != null) {
                reservationInfoLabel.setText("✓ Reservation ID: " + selected.getReservationId() +
                        " | Room: " + selected.getRoomId() + " | Status: " + selected.getStatus());
                reservationInfoLabel.setForeground(SUCCESS_COLOR);
            }
        });

        // Load available amenities
        loadAmenitiesBtn.addActionListener(e -> {
            try {
                if (amenityDAO == null) {
                    showError("Database not connected");
                    return;
                }

                amenityCombo.removeAllItems();
                List<Amenity> amenities = amenityDAO.getAllAmenities("available");

                if (amenities.isEmpty()) {
                    amenityInfoLabel.setText("⚠ No available amenities found");
                    amenityInfoLabel.setForeground(WARNING_COLOR);
                } else {
                    for (Amenity amenity : amenities) {
                        amenityCombo.addItem(amenity);
                    }
                    Amenity selected = (Amenity) amenityCombo.getSelectedItem();
                    if (selected != null) {
                        amenityInfoLabel.setText(String.format("✓ %s - ₱%.2f per unit",
                                selected.getName(), selected.getRate()));
                        amenityInfoLabel.setForeground(SUCCESS_COLOR);
                    }
                    updateStatus("Loaded " + amenities.size() + " available amenities");
                }

            } catch (Exception ex) {
                showError("Error loading amenities: " + ex.getMessage());
            }
        });

        // Update amenity info when selection changes
        amenityCombo.addActionListener(e -> {
            Amenity selected = (Amenity) amenityCombo.getSelectedItem();
            if (selected != null) {
                amenityInfoLabel.setText(String.format("✓ %s - ₱%.2f per unit - %s",
                        selected.getName(), selected.getRate(), selected.getDescription()));
                amenityInfoLabel.setForeground(SUCCESS_COLOR);
            }
        });

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);

        JButton processRentalBtn = createActionButton("✅ Process Rental", SUCCESS_COLOR);
        processRentalBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processRentalBtn.addActionListener(e -> processAmenityRental(
                guestIdField, reservationCombo, amenityCombo, quantitySpinner,
                rentStartField, rentEndField, activeRentalsArea
        ));

        JButton refreshRentalsBtn = createActionButton("🔄 Refresh Active Rentals", SECONDARY_COLOR);
        refreshRentalsBtn.addActionListener(e -> {
            try {
                String guestIdText = guestIdField.getText().trim();
                if (!guestIdText.isEmpty()) {
                    Long guestId = Long.parseLong(guestIdText);
                    loadActiveRentals(guestId, activeRentalsArea);
                }
            } catch (Exception ex) {
                showError("Error refreshing rentals: " + ex.getMessage());
            }
        });

        JButton clearBtn = createActionButton("🗑️ Clear Form", WARNING_COLOR);
        clearBtn.addActionListener(e -> {
            guestIdField.setText("");
            guestInfoLabel.setText("");
            reservationInfoLabel.setText("");
            amenityInfoLabel.setText("");
            reservationCombo.removeAllItems();
            amenityCombo.removeAllItems();
            quantitySpinner.setValue(1);
            rentStartField.setText(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            rentEndField.setText(LocalDateTime.now().plusHours(4).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            activeRentalsArea.setText("");
        });

        actionPanel.add(processRentalBtn);
        actionPanel.add(refreshRentalsBtn);
        actionPanel.add(clearBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Helper method to load active rentals
    private void loadActiveRentals(Long guestId, JTextArea activeRentalsArea) {
        try {
            if (amenityRentalDAO == null) {
                activeRentalsArea.setText("Database not connected");
                return;
            }

            List<String> rentals = amenityRentalDAO.getActiveRentals(guestId);

            if (rentals.isEmpty()) {
                activeRentalsArea.setText("No active rentals for this guest.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("=== ACTIVE RENTALS ===\n\n");
                for (String rental : rentals) {
                    sb.append(rental).append("\n");
                }
                activeRentalsArea.setText(sb.toString());
            }

            updateStatus("Loaded " + rentals.size() + " active rentals");

        } catch (SQLException e) {
            activeRentalsArea.setText("Error loading active rentals: " + e.getMessage());
        }
    }

    // Main method to process amenity rental
    private void processAmenityRental(JTextField guestIdField, JComboBox<Reservation> reservationCombo,
                                      JComboBox<Amenity> amenityCombo, JSpinner quantitySpinner,
                                      JTextField rentStartField, JTextField rentEndField,
                                      JTextArea activeRentalsArea) {
        try {
            if (amenityRentalDAO == null) {
                showError("Database not connected");
                return;
            }

            // Validate inputs
            String guestIdText = guestIdField.getText().trim();
            if (guestIdText.isEmpty()) {
                showError("Please enter a Guest ID");
                return;
            }

            Reservation selectedReservation = (Reservation) reservationCombo.getSelectedItem();
            if (selectedReservation == null) {
                showError("Please select a reservation. Guest must be checked-in.");
                return;
            }

            Amenity selectedAmenity = (Amenity) amenityCombo.getSelectedItem();
            if (selectedAmenity == null) {
                showError("Please select an amenity");
                return;
            }

            // Parse data
            Long guestId = Long.parseLong(guestIdText);
            Long reservationId = selectedReservation.getReservationId();
            Long amenityId = selectedAmenity.getAmenityId();
            int quantity = (Integer) quantitySpinner.getValue();

            // Parse datetime with flexible format
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime rentStart = LocalDateTime.parse(rentStartField.getText().trim(), formatter);
            LocalDateTime rentEnd = LocalDateTime.parse(rentEndField.getText().trim(), formatter);

            // Validate dates
            if (rentEnd.isBefore(rentStart) || rentEnd.isEqual(rentStart)) {
                showError("Rental end time must be after start time!");
                return;
            }

            if (rentStart.isBefore(LocalDateTime.now())) {
                showError("Rental start time cannot be in the past!");
                return;
            }

            // Calculate total cost
            double totalCost = selectedAmenity.getRate() * quantity;

            // Confirm rental
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Process Amenity Rental:\n\n" +
                                    "Guest ID: %d\n" +
                                    "Reservation ID: %d\n" +
                                    "Amenity: %s\n" +
                                    "Quantity: %d\n" +
                                    "Rate per unit: ₱%.2f\n" +
                                    "Total Cost: ₱%.2f\n" +
                                    "Start: %s\n" +
                                    "End: %s\n\n" +
                                    "This charge will be added to the reservation billing.\n" +
                                    "Proceed?",
                            guestId, reservationId, selectedAmenity.getName(), quantity,
                            selectedAmenity.getRate(), totalCost, rentStart, rentEnd),
                    "Confirm Amenity Rental",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Process rental
            Long rentalId = amenityRentalDAO.processAmenityRental(
                    guestId, amenityId, reservationId,
                    rentStart, rentEnd, quantity
            );

            // Success message
            JOptionPane.showMessageDialog(this,
                    String.format("Amenity rental processed successfully!\n\n" +
                                    "Rental ID: %d\n" +
                                    "Amenity: %s\n" +
                                    "Quantity: %d\n" +
                                    "Total Charge: ₱%.2f\n\n" +
                                    "The charge has been added to Reservation #%d billing.\n" +
                                    "Rental Status: Active",
                            rentalId, selectedAmenity.getName(), quantity, totalCost, reservationId),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            updateStatus("Rental #" + rentalId + " processed successfully - Charge: ₱" + String.format("%.2f", totalCost));

            // Refresh active rentals
            loadActiveRentals(guestId, activeRentalsArea);

            // Reset form fields
            quantitySpinner.setValue(1);
            rentStartField.setText(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            rentEndField.setText(LocalDateTime.now().plusHours(4).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        } catch (NumberFormatException e) {
            showError("Invalid Guest ID format. Please enter a valid number.");
        } catch (java.time.format.DateTimeParseException e) {
            showError("Invalid date/time format. Please use YYYY-MM-DD HH:MM format (e.g., 2025-11-17 14:30)");
        } catch (SQLException e) {
            showError("Error processing rental: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // REPORTS PANEL
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Reports Generation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(color);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(127, 140, 141));

        JLabel assignLabel = new JLabel(assignedTo);
        assignLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
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
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(headerLabel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // all cells are non-editable
            }
        };

        for (String[] row : data) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
