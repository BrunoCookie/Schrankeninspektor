import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {interval, Subscription} from "rxjs";
import {booleanReturn} from "../../interfaces/MyInterfaces";

const refreshInterval = interval(60000);

@Component({
  selector: 'app-inspektor',
  templateUrl: './inspektor.component.html',
  styleUrls: ['./inspektor.component.css']
})
export class InspektorComponent implements OnInit, OnDestroy {
  subscription: Subscription | undefined;
  isOpen: booleanReturn | any;
  statusChangeTime: string = '';

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.isOpen = {retData: false};
    this.refreshStatus();
    this.subscription = refreshInterval.subscribe(x => {
      this.refreshStatus()
    })
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  refreshStatus(){
    this.getIsOpen();
    this.getStatusChangeTime();
  }

  getIsOpen() {
    this.http.get<booleanReturn>("/api/inspektor/isCurrentlyOpen").subscribe(x => {
      this.isOpen = x;
      console.log(this.isOpen);
    });
  }

  getStatusChangeTime() {
    this.http.get("/api/inspektor/StatusChange", {responseType: 'text'}).subscribe(x => {
      this.statusChangeTime = x;
      console.log(this.statusChangeTime);
    });
  }

  statusToString() : string{
    if (this.isOpen) return "Offen";
    else return "Geschlossen";
  }

  formattedTime() : string{
    return this.statusChangeTime.substring(11,19)
  }
}
