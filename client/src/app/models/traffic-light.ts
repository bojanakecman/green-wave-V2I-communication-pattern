export class TrafficLight {
    id: number;
    longitude: number;
    latitude: number;
    statusGreen: boolean;
    statusChange: Date;

    constructor(id: number, longitude: number, latitude: number){
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    fullInfo(){
        const status = this.statusGreen ? 'GREEN' : 'RED';
        const statusChange = this.statusChange ? this.statusChange.toUTCString() : "Not activated yet"
        return 'ID: ' + this.id + '\n'
                + 'Longitude: ' + this.longitude + '\n'
                + 'Latitude: ' + this.latitude + '\n'
                + 'Status: ' + status + '\n'
                + 'Last status change: ' + statusChange;
    }
}
