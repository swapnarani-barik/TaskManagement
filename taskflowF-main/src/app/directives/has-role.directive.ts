import { Directive, Input, TemplateRef, ViewContainerRef, inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import type { UserRole } from '../models/role.model';

@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective {
  #tpl = inject(TemplateRef<unknown>);
  #vcr = inject(ViewContainerRef);
  #auth = inject(AuthService);

  @Input('appHasRole') set roles(allowed: UserRole[]) {
    const role = this.#auth.getCurrentUser()?.role;
    const canShow = role ? allowed.includes(role) : false;
    this.#vcr.clear();
    if (canShow) this.#vcr.createEmbeddedView(this.#tpl);
  }
}

