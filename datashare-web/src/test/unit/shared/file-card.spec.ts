// src/test/unit/shared/file-card.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach } from "vitest";
// SUT (System Under Test)
import { FileCard } from "../../../app/shared/file-card/file-card";

/** FileCard unit tests */
describe("FileCard (unit)", () => {
  let component: FileCard;
  let fixture: ComponentFixture<FileCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileCard],
    }).compileComponents();

    fixture = TestBed.createComponent(FileCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
