'use strict';

/**
 * Jest resolver that retries `.js` specifiers against `.ts` sources.
 *
 * Prisma 7 emits the generated client as TypeScript with NodeNext-style
 * `./foo.js` import specifiers (see src/generated/prisma). Jest resolves the
 * `.js` form against source files that only exist as `.ts`, which fails for
 * everything EXCEPT real packages whose names end in `.js` (e.g. ipaddr.js),
 * so a global moduleNameMapper is unsafe. We only retry `.ts` when the
 * `.js` resolution genuinely fails.
 */
module.exports = function resolver(request, options) {
  const { defaultResolver } = options;
  try {
    return defaultResolver(request, options);
  } catch (err) {
    if (request.endsWith('.js')) {
      try {
        return defaultResolver(request.slice(0, -3) + '.ts', options);
      } catch {
        // fall through to the original error
      }
    }
    throw err;
  }
};
