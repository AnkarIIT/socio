import { Injectable, Logger } from '@nestjs/common';

export const OTP_PROVIDER = Symbol('OTP_PROVIDER');

/** Contract for delivering one-time codes to Indian phone numbers. */
export interface OtpProvider {
  send(phone: string, code: string): Promise<void>;
}

/** Dev provider: logs the code to the server console (M1 default). */
@Injectable()
export class DevOtpProvider implements OtpProvider {
  private readonly logger = new Logger('OtpProvider');
  send(phone: string, code: string): Promise<void> {
    this.logger.log(`[OTP:dev] code ${code} -> +91${phone}`);
    return Promise.resolve();
  }
}

/** Msg91 provider (India SMS/WhatsApp OTP). Wiring stub — enable in M2+ with MSG91_AUTH_KEY. */
@Injectable()
export class Msg91OtpProvider implements OtpProvider {
  private readonly logger = new Logger('OtpProvider');
  send(phone: string, code: string): Promise<void> {
    return Promise.reject(
      new Error(
        `Msg91 provider not configured yet (attempted OTP ${code} -> +91${phone}). Set OTP_PROVIDER=dev for local development.`,
      ),
    );
  }
}
