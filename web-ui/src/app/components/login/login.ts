import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './login.html',
    styleUrls: ['./login.scss']
})
export class LoginComponent {
    loginUser: User = {};

    constructor(
        private userService: UserService,
        private router: Router
    ) { }

    onLogin() {
        this.userService.login(this.loginUser).subscribe(
            (response: any) => {
                console.log('Login successful', response);
                if (response.token) {
                    localStorage.setItem('authToken', response.token);
                }
                this.router.navigate(['/chat']);
            },
            (error: any) => {
                console.error('Login failed', error);
            }
        );
    }
}
