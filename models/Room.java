package models;

public class Room {
    private Long roomId;
    private String roomCode;
    private String roomType;
    private String bedType;
    private int maxCapacity;
    private double ratePerNight;
    private String status; // available, reserved, occupied, maintenance
    private String description;

    // Constructors
    public Room() {}

    public Room(String roomCode, String roomType, String bedType, int maxCapacity, double ratePerNight) {
        this.roomCode = roomCode;
        this.roomType = roomType;
        this.bedType = bedType;
        this.maxCapacity = maxCapacity;
        this.ratePerNight = ratePerNight;
        this.status = "available";
    }

    // Getters and Setters
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public double getRatePerNight() { return ratePerNight; }
    public void setRatePerNight(double ratePerNight) { this.ratePerNight = ratePerNight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

