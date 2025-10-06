import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { Router } from '@angular/router';
import { JwtService } from '../../services/jwt.service';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './landing.html',
    styleUrls: ['./landing.scss']
})
export class LandingComponent implements OnInit {
    private platformId = inject(PLATFORM_ID);
    private jwtService = inject(JwtService);

    constructor(private router: Router) { }

    ngOnInit() {
        if (isPlatformBrowser(this.platformId)) {
            const token = this.jwtService.getToken();
            if (token && !this.jwtService.isTokenExpired()) {
                this.router.navigate(['/chat']);
            }
        }
    }

    navigateToLogin() {
        this.router.navigate(['/login']);
    }

    navigateToRegister() {
        this.router.navigate(['/register']);
    }
}
