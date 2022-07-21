package at.tuwien.dse.actorsimulator.dto;

public class TrafficLight {

    private Long id;
    private Double longitude;
    private Double latitude;

    public TrafficLight(){

    }
    public TrafficLight(Double longitude, Double latitude,Long id){
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

}
