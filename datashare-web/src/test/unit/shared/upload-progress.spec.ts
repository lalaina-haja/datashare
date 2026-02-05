// src/test/unit/shared/upload-progress.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach } from "vitest";
// SUT (System Under Test)
import { UploadProgress } from "../../../app/shared/upload-progress/upload-progress";

/** UploadProgress unit tests */
describe("UploadProgress (unit)", () => {
  let component: UploadProgress;
  let fixture: ComponentFixture<UploadProgress>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UploadProgress],
    }).compileComponents();

    fixture = TestBed.createComponent(UploadProgress);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
