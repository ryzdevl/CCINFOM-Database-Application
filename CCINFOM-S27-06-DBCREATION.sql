-- Database
CREATE DATABASE IF NOT EXISTS CCINFOM-S27-06-DBCREATION CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE CCINFOM-S27-06-DBCREATION;

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
  room_code      VARCHAR(50) NOT NULL, -- e.g., COTT-101, RM-201
  room_type      VARCHAR(50) NOT NULL, -- Standard, Deluxe, Suite, Cottage
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
  rating         TINYINT, -- 1..5
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

select * from guest

