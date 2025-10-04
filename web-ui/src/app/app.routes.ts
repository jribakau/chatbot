import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { authGuard } from './guards/auth.guard';
import { ChatLayout } from './components/chat-layout/chat-layout';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'chat', component: ChatLayout, canActivate: [authGuard] },
    { path: '', redirectTo: '/chat', pathMatch: 'full' }
];
