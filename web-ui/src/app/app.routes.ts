import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { authGuard } from './guards/auth.guard';
import { ChatLayout } from './components/chat-layout/chat-layout';
import { LandingComponent } from './components/landing/landing';

export const routes: Routes = [
    { path: '', component: LandingComponent },
    { path: 'login', component: LoginComponent },
    { path: 'chat', component: ChatLayout, canActivate: [authGuard] },
    { path: '**', redirectTo: '/', pathMatch: 'full' }
];
