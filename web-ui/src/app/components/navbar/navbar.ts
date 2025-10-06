import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [],
    templateUrl: './navbar.html',
    styleUrls: ['./navbar.scss']
})
export class NavbarComponent {
    @Input() userName?: string;
    @Input() userAvatar?: string;
    @Output() logout = new EventEmitter<void>();
    @Output() settings = new EventEmitter<void>();

    onLogout(): void {
        this.logout.emit();
    }

    onSettings(): void {
        this.settings.emit();
    }
}
