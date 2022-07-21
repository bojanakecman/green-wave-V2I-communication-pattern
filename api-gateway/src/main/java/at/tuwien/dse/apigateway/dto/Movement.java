package at.tuwien.dse.apigateway.dto;

import java.time.LocalDateTime;

public class Movement {

    private String vin;
    private double speed;
    private LocalDateTime dateTime;
    private Double longitude;
    private Double latitude;
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

    @Override
    public String toString() {
        return "Movement{" +
                "dateTime=" + dateTime +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
