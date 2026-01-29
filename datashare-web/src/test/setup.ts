import 'zone.js';
import 'zone.js/testing';
import { vi } from 'vitest';

vi.stubGlobal('scrollTo', vi.fn());
