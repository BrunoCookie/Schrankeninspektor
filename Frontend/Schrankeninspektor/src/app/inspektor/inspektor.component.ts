import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-inspektor',
  templateUrl: './inspektor.component.html',
  styleUrls: ['./inspektor.component.css']
})
export class InspektorComponent {
  isOpen: boolean = false;
  statusChangeTime: string = '';
}
