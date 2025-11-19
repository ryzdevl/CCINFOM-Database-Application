-- ============================================================================
-- Beach Resort Management System - Complete Database Setup
-- CCINFOM S27-06
-- ============================================================================

-- Drop database if exists (for clean setup)
DROP DATABASE IF EXISTS beach_resort;

-- Create Database
CREATE DATABASE beach_resort CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE beach_resort;

-- ============================================================================
-- TABLE CREATION
-- ============================================================================

-- Guests
CREATE TABLE guest (
  guest_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  phone           VARCHAR(30),
  email           VARCHAR(255),
  passport_no     VARCHAR(50),
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY ux_guest_email (email)
) ENGINE=InnoDB;

-- Guest preferences (optional)
CREATE TABLE guest_preference (
  pref_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  guest_id    BIGINT NOT NULL,
  pref_key    VARCHAR(100) NOT NULL,
  pref_value  VARCHAR(255),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (guest_id) REFERENCES guest(guest_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Rooms
CREATE TABLE room (
  room_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_code      VARCHAR(50) NOT NULL,
  room_type      VARCHAR(50) NOT NULL,
  bed_type       VARCHAR(50),
  max_capacity   INT DEFAULT 1,
  rate_per_night DECIMAL(10,2) NOT NULL,
  status         ENUM('available','reserved','occupied','maintenance') DEFAULT 'available',
  description    TEXT,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY ux_room_code (room_code)
) ENGINE=InnoDB;

-- Room maintenance history
CREATE TABLE room_maintenance (
  maintenance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_id        BIGINT NOT NULL,
  start_time     DATETIME,
  end_time       DATETIME,
  notes          TEXT,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Amenities / Services
CREATE TABLE amenity (
  amenity_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(150) NOT NULL,
  description    TEXT,
  rate           DECIMAL(10,2) DEFAULT 0.00,
  availability   ENUM('available','reserved','maintenance') DEFAULT 'available',
  overall_rating DECIMAL(3,2) DEFAULT NULL,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY ux_amenity_name (name)
) ENGINE=InnoDB;

-- Inventory items
CREATE TABLE inventory_item (
  item_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(150) NOT NULL,
  quantity_on_hand INT DEFAULT 0,
  supplier       VARCHAR(255),
  last_restocked DATE,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY ux_inventory_name (name)
) ENGINE=InnoDB;

-- Inventory restock log
CREATE TABLE restock (
  restock_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_id        BIGINT NOT NULL,
  supplier       VARCHAR(255),
  quantity       INT NOT NULL,
  restock_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
  notes          TEXT,
  FOREIGN KEY (item_id) REFERENCES inventory_item(item_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Reservations
CREATE TABLE reservation (
  reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  guest_id       BIGINT NOT NULL,
  room_id        BIGINT NOT NULL,
  check_in       DATE NOT NULL,
  check_out      DATE NOT NULL,
  booking_channel ENUM('walk-in','online','phone','agent') DEFAULT 'online',
  status         ENUM('confirmed','checked-in','checked-out','cancelled') DEFAULT 'confirmed',
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_res_guest FOREIGN KEY (guest_id) REFERENCES guest(guest_id) ON DELETE CASCADE,
  CONSTRAINT fk_res_room  FOREIGN KEY (room_id)  REFERENCES room(room_id)  ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Link reservations to amenities/services used or pre-booked
CREATE TABLE reservation_amenity (
  res_am_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NOT NULL,
  amenity_id     BIGINT NOT NULL,
  qty            INT DEFAULT 1,
  unit_rate      DECIMAL(10,2) DEFAULT NULL,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ra_res FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE,
  CONSTRAINT fk_ra_amn FOREIGN KEY (amenity_id)     REFERENCES amenity(amenity_id)     ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Amenity rentals/equipment transactions (standalone rentals)
CREATE TABLE amenity_rental (
  rental_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  guest_id       BIGINT NOT NULL,
  amenity_id     BIGINT NOT NULL,
  reservation_id BIGINT DEFAULT NULL,
  rent_start     DATETIME,
  rent_end       DATETIME,
  qty            INT DEFAULT 1,
  rate_per_unit  DECIMAL(10,2),
  status         ENUM('active','returned','overdue','cancelled') DEFAULT 'active',
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ar_guest FOREIGN KEY (guest_id)       REFERENCES guest(guest_id) ON DELETE CASCADE,
  CONSTRAINT fk_ar_amen  FOREIGN KEY (amenity_id)     REFERENCES amenity(amenity_id) ON DELETE RESTRICT,
  CONSTRAINT fk_ar_res   FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Payments / billing
CREATE TABLE payment (
  payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NOT NULL,
  amount         DECIMAL(12,2) NOT NULL,
  method         ENUM('cash','card','online','bank_transfer','other') DEFAULT 'cash',
  payment_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  status         ENUM('paid','pending','refunded') DEFAULT 'paid',
  transaction_reference VARCHAR(255),
  FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Charges for line items (for detailed billing)
CREATE TABLE charge_item (
  charge_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NOT NULL,
  description    VARCHAR(255) NOT NULL,
  qty            INT DEFAULT 1,
  unit_price     DECIMAL(12,2) NOT NULL,
  total_price    DECIMAL(12,2) AS (qty * unit_price) STORED,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Guest feedback / ratings
CREATE TABLE feedback (
  feedback_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  guest_id       BIGINT NOT NULL,
  reservation_id BIGINT DEFAULT NULL,
  rating         TINYINT,
  comments       TEXT,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (guest_id)       REFERENCES guest(guest_id) ON DELETE CASCADE,
  FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Simple audit / check-in log
CREATE TABLE checkin_checkout_log (
  log_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NOT NULL,
  event_type     ENUM('check-in','check-out') NOT NULL,
  event_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  notes          TEXT,
  FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Useful indexes
CREATE INDEX idx_reservation_guest ON reservation(guest_id);
CREATE INDEX idx_reservation_dates ON reservation(check_in, check_out);
CREATE INDEX idx_amenity_name ON amenity(name);

-- ============================================================================
-- SAMPLE DATA INSERTION
-- ============================================================================

-- Insert Guests
INSERT INTO guest (first_name, last_name, phone, email, passport_no) VALUES
('John', 'Doe', '+63-912-345-6789', 'john.doe@email.com', 'P123456'),
('Jane', 'Smith', '+63-917-654-3210', 'jane.smith@email.com', 'P789012'),
('Maria', 'Santos', '+63-918-111-2222', 'maria.santos@email.com', 'P345678'),
('Carlos', 'Reyes', '+63-919-333-4444', 'carlos.reyes@email.com', 'P901234'),
('Ana', 'Cruz', '+63-920-555-6666', 'ana.cruz@email.com', 'P567890');

-- Insert Guest Preferences
INSERT INTO guest_preference (guest_id, pref_key, pref_value) VALUES
(1, 'Room Floor', 'High Floor'),
(1, 'Pillow Type', 'Soft'),
(2, 'Room View', 'Ocean View'),
(3, 'Dietary', 'Vegetarian');

-- Insert Rooms
INSERT INTO room (room_code, room_type, bed_type, max_capacity, rate_per_night, status, description) VALUES
('R101', 'Standard', 'Single', 1, 80.00, 'reserved', 'Cozy single room with garden view'),
('R102', 'Deluxe', 'Queen', 2, 150.00, 'reserved', 'Spacious room with queen bed and balcony'),
('R103', 'Suite', 'King', 4, 250.00, 'reserved', 'Luxury suite with ocean view and living area'),
('R104', 'Standard', 'Twin', 2, 90.00, 'reserved', 'Twin bed room perfect for friends'),
('R105', 'Deluxe', 'King', 2, 180.00, 'available', 'Premium room with king bed'),
('COTT-01', 'Cottage', 'King', 4, 300.00, 'reserved', 'Private beach cottage with kitchenette'),
('COTT-02', 'Cottage', 'Queen', 3, 280.00, 'available', 'Beachfront cottage with patio');

-- Insert Amenities
INSERT INTO amenity (name, description, rate, availability, overall_rating) VALUES
('Kayak Rental', 'Single kayak rental per hour', 15.00, 'available', 4.5),
('Snorkel Gear', 'Snorkeling equipment set', 10.00, 'available', 4.3),
('Surfboard', 'Surfboard rental per hour', 20.00, 'available', 4.7),
('Jet Ski', 'Jet ski rental per 30 minutes', 50.00, 'available', 4.8),
('Spa Treatment', '60-minute massage therapy', 80.00, 'available', 4.9),
('Island Tour', 'Half-day island hopping tour', 120.00, 'available', 4.6),
('Scuba Diving', 'Guided scuba diving session', 150.00, 'available', 4.8),
('Bicycle Rental', 'Mountain bike rental per day', 25.00, 'available', 4.2);

-- Insert Inventory Items
INSERT INTO inventory_item (name, quantity_on_hand, supplier, last_restocked) VALUES
('Towels', 150, 'Linen Suppliers Inc.', '2025-11-01'),
('Toiletries Set', 200, 'HotelCare Co.', '2025-11-05'),
('Snorkel Gear', 45, 'AquaSports Ltd.', '2025-11-10'),
('Life Jackets', 80, 'Safety Equipment Co.', '2025-11-08'),
('Bed Sheets', 120, 'Linen Suppliers Inc.', '2025-11-01'),
('Pillows', 200, 'Comfort Bedding Inc.', '2025-10-28'),
('Beach Umbrellas', 30, 'Outdoor Supplies', '2025-10-15'),
('Food Supplies', 500, 'Fresh Foods Distributor', '2025-11-15');

-- Insert Sample Reservations (November 2025)
INSERT INTO reservation (guest_id, room_id, check_in, check_out, booking_channel, status) VALUES
(1, 1, '2025-11-10', '2025-11-13', 'online', 'checked-out'),
(2, 3, '2025-11-15', '2025-11-20', 'walk-in', 'checked-out'),
(3, 2, '2025-11-18', '2025-11-22', 'online', 'checked-in'),
(4, 6, '2025-11-20', '2025-11-25', 'phone', 'confirmed'),
(5, 4, '2025-11-22', '2025-11-24', 'online', 'confirmed');

-- Insert Reservation Amenities
INSERT INTO reservation_amenity (reservation_id, amenity_id, qty, unit_rate) VALUES
(1, 1, 2, 15.00),
(1, 5, 1, 80.00),
(2, 6, 4, 120.00),
(2, 5, 2, 80.00),
(3, 2, 3, 10.00);

-- -- Insert Amenity Rentals (November 2025)
INSERT INTO amenity_rental (guest_id, amenity_id, reservation_id, rent_start, rent_end, qty, rate_per_unit, status) VALUES
(1, 1, 1, '2025-11-11 10:00:00', '2025-11-11 12:00:00', 2, 15.00, 'returned'),
(2, 6, 2, '2025-11-16 09:00:00', '2025-11-16 14:00:00', 4, 120.00, 'returned'),
(3, 2, 3, '2025-11-19 08:00:00', '2025-11-19 11:00:00', 3, 10.00, 'active');

-- Insert Payments
INSERT INTO payment (reservation_id, amount, method, status, transaction_reference) VALUES
(1, 270.00, 'card', 'paid', 'TXN001-2025-11'),
(2, 1910.00, 'cash', 'paid', 'TXN002-2025-11');

-- Insert Charge Items
INSERT INTO charge_item (reservation_id, description, qty, unit_price) VALUES
(1, 'Room Charge (3 nights)', 3, 80.00),
(1, 'Kayak Rental', 2, 15.00),
(1, 'Spa Treatment', 1, 80.00),
(2, 'Room Charge (5 nights)', 5, 250.00),
(2, 'Island Tour', 4, 120.00),
(2, 'Spa Treatment', 2, 80.00);

-- Insert Feedback
INSERT INTO feedback (guest_id, reservation_id, rating, comments) VALUES
(1, 1, 5, 'Excellent stay! The room was clean and the staff was very friendly.'),
(2, 2, 4, 'Great experience overall. The island tour was amazing!');

-- Insert Check-in/Check-out Logs
INSERT INTO checkin_checkout_log (reservation_id, event_type, notes) VALUES
(1, 'check-in', 'Guest checked in on time'),
(1, 'check-out', 'Guest checked out, payment settled'),
(2, 'check-in', 'Walk-in guest, processed immediately'),
(2, 'check-out', 'Guest checked out, excellent stay'),
(3, 'check-in', 'Online booking confirmed');

-- Insert Restock History (November 2025)
INSERT INTO restock (item_id, supplier, quantity, restock_date, notes) VALUES
(1, 'Linen Suppliers Inc.', 50, '2025-11-01 09:00:00', 'Monthly restock'),
(2, 'HotelCare Co.', 100, '2025-11-05 10:30:00', 'Bulk order'),
(3, 'AquaSports Ltd.', 15, '2025-11-10 14:00:00', 'Seasonal stock'),
(5, 'Linen Suppliers Inc.', 40, '2025-11-01 09:00:00', 'Monthly restock'),
(8, 'Fresh Foods Distributor', 200, '2025-11-15 08:00:00', 'Weekly food supplies');

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check record counts
SELECT 'Guests' as Table_Name, COUNT(*) as Record_Count FROM guest
UNION ALL
SELECT 'Rooms', COUNT(*) FROM room
UNION ALL
SELECT 'Amenities', COUNT(*) FROM amenity
UNION ALL
SELECT 'Inventory Items', COUNT(*) FROM inventory_item
UNION ALL
SELECT 'Reservations', COUNT(*) FROM reservation
UNION ALL
SELECT 'Amenity Rentals', COUNT(*) FROM amenity_rental
UNION ALL
SELECT 'Payments', COUNT(*) FROM payment;

-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

SELECT '✓ Database setup complete!' as Status,
       'beach_resort' as Database_Name,
       '15 tables created' as Tables,
       'Sample data inserted' as Data_Status;
       
 select first_name, last_name
 FROM guest
