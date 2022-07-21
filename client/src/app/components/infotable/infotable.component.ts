import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {Movement} from '../../models/movement';
import {TrafficLight} from '../../models/traffic-light';
import {MatPaginator} from '@angular/material/paginator';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import {RestService} from 'src/app/services/rest-service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-infotable',
  templateUrl: './infotable.component.html',
  styleUrls: ['./infotable.component.css']
})
export class InfotableComponent implements OnInit {

  @ViewChild('paginatorOne', {static: true}) movementPaginator: MatPaginator;
  @ViewChild('paginatorTwo', {static: true}) trafficLightPaginator: MatPaginator;

  ws: any;

  movementDisplayedColumns: string[] = ['vin', 'speed', 'longitude', 'latitude', 'crash', 'statusChange'];
  movementDataSource = new MatTableDataSource<Movement>();
  trafficLightDisplayedColumns: string[] = ['id', 'longitude', 'latitude', 'status', 'statusChange'];
  trafficLightDataSource = new MatTableDataSource<TrafficLight>();
  trafficLights: TrafficLight[] = [];


  constructor(private restService: RestService) {
  }

  ngOnInit(): void {
    this.trafficLightDataSource.paginator = this.trafficLightPaginator;
    this.movementDataSource.paginator = this.movementPaginator;
    this.getCoreData();
    this.initSocketConnections();
  }

  getAllTrafficLights() {
    this.restService.getAllTrafficLights().subscribe(response => {
      response.forEach(element => {
        const trafficLight = this.createTrafficLight(element);
        this.trafficLights.push(trafficLight);
      })
    });
  }

  createMovement(element: any): Movement {
    return new Movement(element.vin, element.speed, element.longitude, element.latitude, element.crash, new Date(element.dateTime));
  }

  createTrafficLight(element: any) {
    return new TrafficLight(element.id, element.longitude, element.latitude);
  }

  initSocketConnections() {
    this.trafficLightDataSource.data = []
    let ws = new SockJS(environment.apiEndpoint + "/ws");
    this.ws = Stomp.over(ws);
    let that = this;
    this.ws.connect({}, function (frame) {
      that.ws.subscribe("/trafficLights", function (element) {
        let tl = JSON.parse(element.body);
        let trafficLight = that.trafficLights.find(light => tl['trafficLightId'] === light.id);
        trafficLight.statusGreen = tl['green'];
        trafficLight.statusChange = new Date(tl['dateTime']);
        const data = that.trafficLightDataSource.data;
        data.unshift(JSON.parse(JSON.stringify(trafficLight)));
        that.trafficLightDataSource.data = data;
      });
      that.ws.subscribe("/movements", function (element) {
        let mvm = JSON.parse(element.body);
        const movement = that.createMovement(mvm);
        const data = that.movementDataSource.data;
        data.unshift(movement);
        that.movementDataSource.data = data;
      });
    })
  }

  formatNumber(number: number) {
    return Math.round(number * 100) / 100;
  }

  private getCoreData() {
    let intervalId = setInterval(() => {
      if (this.trafficLights.length !== 0) {
        clearInterval(intervalId);
      }
      this.getAllTrafficLights();
    }, 1000);
  }

}
