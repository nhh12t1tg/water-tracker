import { register } from 'node:module';
register(new URL('../test-loader.mjs', import.meta.url));

import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import {
  UMPConsentService,
  ConsentStatus,
  UMPDebugGeography,
} from '../../services/admob/umpConsent.service';

describe('Google UMP (User Messaging Platform) & GDPR Compliance Unit Tests', () => {
  let consentService: UMPConsentService;

  beforeEach(() => {
    consentService = UMPConsentService.getInstance();
    consentService.reset();
  });

  describe('GDPR & EEA Consent State Machine', () => {
    it('initializes with UNKNOWN consent status', () => {
      assert.strictEqual(consentService.getConsentStatus(), ConsentStatus.UNKNOWN);
      assert.strictEqual(consentService.canRequestAds(), false);
    });

    it('determines consent NOT_REQUIRED for non-EEA geographic regions', async () => {
      consentService.configure({ debugGeography: UMPDebugGeography.NOT_EEA });
      const result = await consentService.requestConsentInfoUpdate();

      assert.strictEqual(result.status, ConsentStatus.NOT_REQUIRED);
      assert.strictEqual(result.canRequestAds, true);
      assert.strictEqual(consentService.canRequestAds(), true);
    });

    it('requires consent for EEA users before any ads may be served', async () => {
      consentService.configure({
        debugGeography: UMPDebugGeography.EEA,
        testDeviceHashedIds: ['EMULATOR'],
      });

      const updateResult = await consentService.requestConsentInfoUpdate();
      assert.strictEqual(updateResult.status, ConsentStatus.REQUIRED);
      assert.strictEqual(updateResult.canRequestAds, false);
      assert.strictEqual(consentService.canRequestAds(), false);
      assert.strictEqual(consentService.isPrivacyOptionsRequired(), true);

      // Present consent form to user
      const formResult = await consentService.loadAndShowConsentFormIfRequired();
      assert.strictEqual(formResult.status, ConsentStatus.OBTAINED);
      assert.strictEqual(formResult.canRequestAds, true);
      assert.strictEqual(consentService.canRequestAds(), true);
    });

    it('supports GDPR Privacy Options form from Settings screen', async () => {
      consentService.configure({ debugGeography: UMPDebugGeography.EEA });
      await consentService.requestConsentInfoUpdate();
      await consentService.loadAndShowConsentFormIfRequired();

      // User reopens consent from Settings
      const privacyResult = await consentService.showPrivacyOptionsForm();
      assert.strictEqual(privacyResult.status, ConsentStatus.OBTAINED);
      assert.strictEqual(privacyResult.canRequestAds, true);
    });

    it('correctly resets state for debug and testing cycles', async () => {
      consentService.configure({ debugGeography: UMPDebugGeography.EEA });
      await consentService.requestConsentInfoUpdate();
      assert.strictEqual(consentService.getConsentStatus(), ConsentStatus.REQUIRED);

      consentService.reset();
      assert.strictEqual(consentService.getConsentStatus(), ConsentStatus.UNKNOWN);
      assert.strictEqual(consentService.canRequestAds(), false);
    });
  });
});
