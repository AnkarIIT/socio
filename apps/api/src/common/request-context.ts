import { AsyncLocalStorage } from 'node:async_hooks';

export interface RequestContext {
  requestId: string;
}

/** Per-request context carried via async_hooks (correlates logs + errors). */
export const requestContext = new AsyncLocalStorage<RequestContext>();
