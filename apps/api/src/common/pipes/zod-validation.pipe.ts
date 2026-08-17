import { Injectable, PipeTransform } from '@nestjs/common';
import { z } from 'zod';
import { validationError } from '../errors/domain-exceptions';

/** Validates request bodies/queries against a Zod schema from @bharat/contracts. */
@Injectable()
export class ZodValidationPipe<
  T extends z.ZodTypeAny,
> implements PipeTransform {
  constructor(private readonly schema: T) {}

  transform(value: unknown): z.infer<T> {
    const result = this.schema.safeParse(value);
    if (!result.success) {
      const details = result.error.issues.reduce<Record<string, unknown>>(
        (acc, issue) => {
          const key = issue.path.join('.') || '_';
          acc[key] = issue.message;
          return acc;
        },
        {},
      );
      throw validationError('Invalid request payload', details);
    }
    return result.data as z.infer<T>;
  }
}
