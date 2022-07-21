package at.tuwien.dse.statustrackingservice.dto;

import java.time.LocalDateTime;

public class TrafficLightStatus {

    private boolean green;
    private Long trafficLightId;
    private LocalDateTime dateTime;

    public TrafficLightStatus() {

    }

    public TrafficLightStatus(boolean green, Long trafficLightId, LocalDateTime dateTime) {
        this.green = green;
        this.trafficLightId = trafficLightId;
        this.dateTime = dateTime;
    }


    public boolean isGreen() {
        return green;
    }

    public void setGreen(boolean green) {
        this.green = green;
    }

    public Long getTrafficLightId() {
        return trafficLightId;
    }

    public void setTrafficLightId(Long trafficLightId) {
        this.trafficLightId = trafficLightId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "TrafficLightStatus{" +
                "green=" + green +
                ", trafficLightId=" + trafficLightId +
                ", dateTime=" + dateTime +
                '}';
    }
}
