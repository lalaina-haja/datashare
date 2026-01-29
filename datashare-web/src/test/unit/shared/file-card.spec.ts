import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach } from 'vitest';

import { FileCard } from '../../../app/shared/file-card/file-card';

describe('FileCard', () => {
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

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
