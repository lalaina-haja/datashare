import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach } from 'vitest';
import { Files } from '../../../app/pages/files/files';

describe('Files', () => {
  let component: Files;
  let fixture: ComponentFixture<Files>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Files],
    }).compileComponents();

    fixture = TestBed.createComponent(Files);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
