import { Routes } from '@angular/router';
import { ChatLayout } from './components/chat-layout/chat-layout';
import { LandingComponent } from './components/landing/landing';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', component: LandingComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'chat', component: ChatLayout, canActivate: [authGuard] },
    { path: '**', redirectTo: '/', pathMatch: 'full' }
];
