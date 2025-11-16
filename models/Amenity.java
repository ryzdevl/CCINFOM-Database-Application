package models;

public class Amenity {
    private Long amenityId;
    private String name;
    private String description;
    private double rate;
    private String availability;
    private Double overallRating;

    // Constructors
    public Amenity() {}

    public Amenity(String name, String description, double rate) {
        this.name = name;
        this.description = description;
        this.rate = rate;
        this.availability = "available";
    }

    // Getters and Setters
    public Long getAmenityId() { return amenityId; }
    public void setAmenityId(Long amenityId) { this.amenityId = amenityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }
}
