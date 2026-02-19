import { ActivatedRoute } from '@angular/router';
import { ExpensesPageComponent } from './expenses-page.component';

describe('ExpensesPageComponent', () => {
  it('returns unicode arrows for active sort column', () => {
    const component = new ExpensesPageComponent({} as never, {
      snapshot: { data: {} }
    } as ActivatedRoute);

    component.sortBy = 'amount';
    component.sortDir = 'asc';
    expect(component.sortIndicator('amount')).toBe('▲');

    component.sortDir = 'desc';
    expect(component.sortIndicator('amount')).toBe('▼');
  });

  it('returns empty indicator for inactive sort columns', () => {
    const component = new ExpensesPageComponent({} as never, {
      snapshot: { data: {} }
    } as ActivatedRoute);

    component.sortBy = 'amount';
    expect(component.sortIndicator('category')).toBe('');
  });
});