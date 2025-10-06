import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { User } from '../../models/user';
import { UserService } from '../../services/user.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './login.html',
    styleUrls: ['./login.scss']
})
export class LoginComponent implements OnInit {
    loginUser: User = {};
    password: string = '';
    errorMessage: string = '';
    tokenExpired: boolean = false;
    returnUrl: string = '/chat';

    constructor(
        private userService: UserService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            if (params['expired'] === 'true') {
                this.tokenExpired = true;
                this.errorMessage = 'Your session has expired. Please log in again.';
            }
            if (params['returnUrl']) {
                this.returnUrl = params['returnUrl'];
            }
        });
    }

    onLogin() {
        this.errorMessage = '';
        this.tokenExpired = false;

        const loginRequest = {
            username: this.loginUser.username,
            password: this.password
        };

        this.userService.login(loginRequest).subscribe(
            (response: any) => {
                console.log('Login successful', response);
                if (response.token) {
                    localStorage.setItem('authToken', response.token);
                }
                this.router.navigate([this.returnUrl]);
            },
            (error: any) => {
                console.error('Login failed', error);
                this.errorMessage = error.error?.message || 'Login failed. Please check your credentials.';
            }
        );
    }
}
