package at.tuwien.dse.statustrackingservice.dto;

import java.time.LocalDateTime;

public class Movement {

    private String vin;
    private double speed;
    private LocalDateTime dateTime;
    private Double longitude;
    private Double latitude;
    private Double distance;
    private boolean crash;

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean isCrash() {
        return crash;
    }

    public void setCrash(boolean crash) {
        this.crash = crash;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Movement{" +
                "vin='" + vin + '\'' +
                ", speed=" + speed +
                ", dateTime=" + dateTime +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", distance=" + distance +
                ", crash=" + crash +
                '}';
    }
}
