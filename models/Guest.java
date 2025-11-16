package models;

import java.time.LocalDateTime;


public class Guest {
    private Long guestId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String passportNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Guest() {}

    public Guest(String firstName, String lastName, String phone, String email, String passportNo) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.passportNo = passportNo;
    }

    // Getters and Setters
    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassportNo() { return passportNo; }
    public void setPassportNo(String passportNo) { this.passportNo = passportNo; }

    public String getFullName() { return firstName + " " + lastName; }
}
