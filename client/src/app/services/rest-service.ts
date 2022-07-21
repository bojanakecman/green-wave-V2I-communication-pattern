import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse} from '@angular/common/http';
import { TrafficLight } from '../models/traffic-light';
import { Observable } from 'rxjs';
import { Vehicle } from '../models/vehicle';
import { environment } from 'src/environments/environment';






const endpoint = environment.apiEndpoint;


@Injectable({
    providedIn: 'root'
})
export class RestService {

    constructor(private http: HttpClient) {}

    getAllTrafficLights() : Observable<TrafficLight[]> {
        return this.http.get<TrafficLight[]>(endpoint + '/getAllTrafficLights');
    }

    getAllVehicles() : Observable<Vehicle[]> {
        return this.http.get<Vehicle[]>(endpoint + '/getAllVehicles');
    }
}