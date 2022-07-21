package registryservice.dto;

public class Vehicle {

    private String vin;
    private String model;
    private String producer;

    public Vehicle(){

    }
    public Vehicle(String vin, String model, String producer){
        this.vin = vin;
        this.model = model;
        this.producer = producer;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }
}
