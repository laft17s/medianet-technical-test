import { TestBed } from '@angular/core/testing';

import { MovHttpService } from './mov-http.service';

describe('MovHttpService', () => {
  let service: MovHttpService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MovHttpService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
