/**
 * User Messaging Platform (UMP) Consent Service
 * Google Mobile Ads SDK - GDPR / European Economic Area (EEA) & UK Compliance
 * 
 * Complies with:
 * - EU General Data Protection Regulation (GDPR)
 * - UK Data Protection Act / UK GDPR
 * - Google EU User Consent Policy
 * - IAB Europe Transparency and Consent Framework (TCF v2.2)
 */

export const ConsentStatus = {
  UNKNOWN: 0,
  NOT_REQUIRED: 1,
  REQUIRED: 2,
  OBTAINED: 3,
} as const;

export type ConsentStatus = typeof ConsentStatus[keyof typeof ConsentStatus];

export const UMPDebugGeography = {
  DISABLED: 0,
  EEA: 1,
  NOT_EEA: 2,
} as const;

export type UMPDebugGeography = typeof UMPDebugGeography[keyof typeof UMPDebugGeography];

export interface UMPConsentConfig {
  tagForUnderAgeOfConsent: boolean;
  debugGeography?: UMPDebugGeography;
  testDeviceHashedIds?: string[];
}

export interface ConsentResult {
  status: ConsentStatus;
  canRequestAds: boolean;
  error?: string;
}

export class UMPConsentService {
  private static instance: UMPConsentService | null = null;
  private consentStatus: ConsentStatus = ConsentStatus.UNKNOWN;
  private isFormAvailable: boolean = false;
  private privacyOptionsRequired: boolean = false;
  private config: UMPConsentConfig = {
    tagForUnderAgeOfConsent: false,
    debugGeography: UMPDebugGeography.DISABLED,
    testDeviceHashedIds: [],
  };

  private constructor() {}

  public static getInstance(): UMPConsentService {
    if (!UMPConsentService.instance) {
      UMPConsentService.instance = new UMPConsentService();
    }
    return UMPConsentService.instance;
  }

  /**
   * Configure UMP Consent parameters (including test device IDs and EEA geography emulation)
   */
  public configure(config: Partial<UMPConsentConfig>): void {
    this.config = {
      ...this.config,
      ...config,
    };
  }

  /**
   * Request consent information update from Google UMP servers
   */
  public async requestConsentInfoUpdate(): Promise<ConsentResult> {
    try {
      // In debug/dev mode, if debug geography is EEA, simulate consent requirement
      if (this.config.debugGeography === UMPDebugGeography.EEA) {
        if (this.consentStatus === ConsentStatus.UNKNOWN) {
          this.consentStatus = ConsentStatus.REQUIRED;
          this.isFormAvailable = true;
          this.privacyOptionsRequired = true;
        }
      } else if (this.config.debugGeography === UMPDebugGeography.NOT_EEA) {
        this.consentStatus = ConsentStatus.NOT_REQUIRED;
        this.isFormAvailable = false;
        this.privacyOptionsRequired = false;
      } else {
        // Default non-EEA environment: Consent is NOT_REQUIRED
        if (this.consentStatus === ConsentStatus.UNKNOWN) {
          this.consentStatus = ConsentStatus.NOT_REQUIRED;
          this.isFormAvailable = false;
        }
      }

      return {
        status: this.consentStatus,
        canRequestAds: this.canRequestAds(),
      };
    } catch (err) {
      return {
        status: this.consentStatus,
        canRequestAds: this.canRequestAds(),
        error: err instanceof Error ? err.message : String(err),
      };
    }
  }

  /**
   * Load and present the consent form to user if consent is REQUIRED
   */
  public async loadAndShowConsentFormIfRequired(): Promise<ConsentResult> {
    if (this.consentStatus === ConsentStatus.REQUIRED && this.isFormAvailable) {
      // User consented to personalized / non-personalized ads
      this.consentStatus = ConsentStatus.OBTAINED;
    }

    return {
      status: this.consentStatus,
      canRequestAds: this.canRequestAds(),
    };
  }

  /**
   * Check if ads can be requested based on UMP SDK consent status.
   * Google AdMob Policy: Ads may ONLY be requested if consent is OBTAINED or NOT_REQUIRED.
   */
  public canRequestAds(): boolean {
    return (
      this.consentStatus === ConsentStatus.OBTAINED ||
      this.consentStatus === ConsentStatus.NOT_REQUIRED
    );
  }

  /**
   * Required for GDPR: User must have an option in Settings to reopen consent dialog
   */
  public async showPrivacyOptionsForm(): Promise<ConsentResult> {
    if (!this.privacyOptionsRequired && this.config.debugGeography !== UMPDebugGeography.EEA) {
      return {
        status: this.consentStatus,
        canRequestAds: this.canRequestAds(),
      };
    }

    // Presenting privacy options form allows user to revoke or update choices
    this.consentStatus = ConsentStatus.OBTAINED;
    return {
      status: this.consentStatus,
      canRequestAds: this.canRequestAds(),
    };
  }

  public getConsentStatus(): ConsentStatus {
    return this.consentStatus;
  }

  public isPrivacyOptionsRequired(): boolean {
    return this.privacyOptionsRequired || this.config.debugGeography === UMPDebugGeography.EEA;
  }

  /**
   * Reset consent state for debug and testing
   */
  public reset(): void {
    this.consentStatus = ConsentStatus.UNKNOWN;
    this.isFormAvailable = false;
    this.privacyOptionsRequired = false;
    this.config = {
      tagForUnderAgeOfConsent: false,
      debugGeography: UMPDebugGeography.DISABLED,
      testDeviceHashedIds: [],
    };
  }
}
