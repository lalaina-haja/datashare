import { Component, OnInit } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { CommonModule } from "@angular/common";
import { Header } from "./shared/header/header";
import { environment } from "../../config/env/environment";

@Component({
  selector: "app-root",
  imports: [CommonModule, RouterOutlet, Header],
  templateUrl: "./app.html",
  styleUrl: "./app.scss",
})
export class App implements OnInit {
  ngOnInit() {
    // Expose for Cypress
    (window as any).environment = environment;
  }
}
