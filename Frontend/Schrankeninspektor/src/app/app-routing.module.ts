import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {InspektorComponent} from "./inspektor/inspektor.component";
import {DisclaimerComponent} from "./disclaimer/disclaimer.component";

const routes: Routes = [
  { path: '', component: InspektorComponent },
  { path: 'disclaimer', component: DisclaimerComponent },
  { path: '**', component: InspektorComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule { }
