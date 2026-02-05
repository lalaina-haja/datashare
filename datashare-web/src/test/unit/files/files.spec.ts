// src/test/unit/files/files.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach } from "vitest";
// SUT (System Under Test)
import { Files } from "../../../app/features/files/components/files/files";
import { Home } from "../../../app/shared/home/home";
import { provideRouter } from "@angular/router";

/** Files Component Unit tests */
describe("Files (unit)", () => {
  let component: Files;
  let fixture: ComponentFixture<Files>;

  beforeEach(async () => {
    const testRoutes = [
      { path: "home", component: Home },
      { path: "**", redirectTo: "" },
    ];

    await TestBed.configureTestingModule({
      imports: [Files],
      providers: [provideRouter(testRoutes)],
    }).compileComponents();

    fixture = TestBed.createComponent(Files);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
