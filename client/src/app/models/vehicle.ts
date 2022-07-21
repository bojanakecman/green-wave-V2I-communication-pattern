export class Vehicle {
    vin: string;
    model: string;
    producer: string;
    constructor(vin: string, model: string, producer: string) {
        this.vin = vin;
        this.model = model;
        this.producer = producer;
    }
}