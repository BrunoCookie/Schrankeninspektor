import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {InspektorComponent} from "./inspektor/inspektor.component";

const routes: Routes = [
  { path: '**', component: InspektorComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule { }
