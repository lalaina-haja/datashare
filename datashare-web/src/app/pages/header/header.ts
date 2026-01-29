import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

// Angular Material modules
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-header',
  imports: [RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, MatCardModule],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  readonly authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
  }
}
