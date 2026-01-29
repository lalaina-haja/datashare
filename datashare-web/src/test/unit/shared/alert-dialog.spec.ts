import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach } from 'vitest';

import { AlertDialog } from '../../../app/shared/alert-dialog/alert-dialog';

describe('AlertDialog - unit', () => {
  let component: AlertDialog;
  let fixture: ComponentFixture<AlertDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AlertDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
