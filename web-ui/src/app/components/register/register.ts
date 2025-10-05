import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { User } from '../../models/user';
import { UserService } from '../../services/user.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './register.html',
    styleUrls: ['./register.scss']
})
export class RegisterComponent {
    registerUser: User = {};
    password: string = '';
    confirmPassword: string = '';
    errorMessage: string = '';
    successMessage: string = '';

    constructor(
        private userService: UserService,
        private router: Router
    ) { }

    onRegister() {
        this.errorMessage = '';
        this.successMessage = '';

        if (!this.registerUser.username || !this.registerUser.email || !this.password) {
            this.errorMessage = 'All fields are required';
            return;
        }

        if (this.password !== this.confirmPassword) {
            this.errorMessage = 'Passwords do not match';
            return;
        }

        if (this.password.length < 8) {
            this.errorMessage = 'Password must be at least 8 characters long';
            return;
        }

        const userToRegister: User = {
            ...this.registerUser,
            passwordHash: this.password
        };

        this.userService.register(userToRegister).subscribe(
            (response: any) => {
                console.log('Registration successful', response);
                this.successMessage = 'Registration successful! Redirecting to login...';
                setTimeout(() => {
                    this.router.navigate(['/login']);
                }, 2000);
            },
            (error: any) => {
                console.error('Registration failed', error);
                this.errorMessage = error.error?.message || 'Registration failed. Please try again.';
            }
        );
    }

    navigateToLogin() {
        this.router.navigate(['/login']);
    }
}
