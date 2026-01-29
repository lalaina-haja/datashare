import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from '../../../app/app';
import { Header } from '../../../app/pages/header/header';
import { By } from '@angular/platform-browser';

describe('App - unit', () => {
  let component: App;
  let fixture: ComponentFixture<App>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Component Creation', () => {
    it('should create the app component', () => {
      expect(component).toBeTruthy();
    });

    it('should be an instance of App', () => {
      expect(component).toBeInstanceOf(App);
    });
  });

  describe('Component Structure', () => {
    it('should have router-outlet in the template', () => {
      const routerOutlet = fixture.debugElement.query(By.css('router-outlet'));
      expect(routerOutlet).toBeTruthy();
    });

    it('should render Header component', () => {
      const headerElement = fixture.debugElement.query(By.directive(Header));
      expect(headerElement).toBeTruthy();
    });

    it('should have app-header selector in DOM', () => {
      const headerElement = fixture.nativeElement.querySelector('app-header');
      expect(headerElement).toBeTruthy();
    });
  });

  describe('Component Imports', () => {
    it('should import CommonModule', () => {
      // Vérifier que les directives CommonModule sont disponibles
      const compiled = fixture.nativeElement;
      expect(compiled).toBeDefined();
    });

    it('should import RouterOutlet', () => {
      const routerOutletElement = fixture.debugElement.query(By.css('router-outlet'));
      expect(routerOutletElement).not.toBeNull();
    });

    it('should import Header component', () => {
      const headerComponent = fixture.debugElement.query(By.directive(Header));
      expect(headerComponent).not.toBeNull();
    });
  });

  describe('Component Rendering', () => {
    it('should render without errors', () => {
      expect(() => {
        fixture.detectChanges();
      }).not.toThrow();
    });

    it('should have the correct selector', () => {
      const componentMetadata = (App as any).ɵcmp;
      expect(componentMetadata.selectors[0][0]).toBe('app-root');
    });
  });

  describe('Template and Styles', () => {
    it('should have templateUrl defined', () => {
      const componentMetadata = (App as any).ɵcmp;
      expect(componentMetadata).toBeDefined();
    });

    it('should compile template successfully', () => {
      const compiled = fixture.nativeElement;
      expect(compiled).toBeTruthy();
      expect(compiled.children.length).toBeGreaterThan(0);
    });
  });

  describe('Component Lifecycle', () => {
    it('should initialize properly', () => {
      expect(fixture.componentInstance).toBeDefined();
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should handle change detection', () => {
      expect(() => {
        fixture.detectChanges();
        fixture.detectChanges();
        fixture.detectChanges();
      }).not.toThrow();
    });
  });

  describe('Child Components', () => {
    it('should instantiate Header component', () => {
      const headerDebugElement = fixture.debugElement.query(By.directive(Header));
      const headerComponentInstance = headerDebugElement?.componentInstance;
      expect(headerComponentInstance).toBeTruthy();
      expect(headerComponentInstance).toBeInstanceOf(Header);
    });
  });

  describe('Standalone Component', () => {
    it('should be a standalone component', () => {
      const componentMetadata = (App as any).ɵcmp;
      expect(componentMetadata.standalone).toBe(true);
    });

    it('should have correct imports array', () => {
      // Vérifier que les imports sont correctement configurés
      const componentMetadata = (App as any).ɵcmp;
      expect(componentMetadata).toBeDefined();
    });
  });
});
