import {
  UserProfile,
  Gender,
  ActivityLevel,
  UserId,
} from '../../../types/water';
import { calculateIntakeGoal, clampWeight } from '../utils/calculator';

// Ensure JSX compatibility across browser/node/native environments without ambient React bundle
declare global {
  namespace JSX {
    interface IntrinsicElements {
      [elemName: string]: any;
    }
  }
}

export interface OnboardingScreenProps {
  onComplete?: (profile: UserProfile) => void;
  initialValues?: Partial<UserProfile>;
}

export type OnboardingStep = 'gender' | 'stats' | 'target';

export interface OnboardingState {
  currentStep: OnboardingStep;
  gender: Gender;
  weightKg: number;
  activityLevel: ActivityLevel;
  customDailyGoalMl: number | null;
  wakeUpTime: string;
  bedTime: string;
  calculatedTargetMl: number;
}

/**
 * Controller hook / logic for Onboarding Flow.
 * Can be used headless or inside React / React Native components.
 */
export function createOnboardingController(
  initialValues?: Partial<UserProfile>,
  onComplete?: (profile: UserProfile) => void
) {
  let currentStep: OnboardingStep = 'gender';
  let gender: Gender = initialValues?.gender || 'male';
  let weightKg: number = clampWeight(initialValues?.weightKg ?? 70);
  let activityLevel: ActivityLevel = initialValues?.activityLevel || 'moderate';
  let customDailyGoalMl: number | null = initialValues?.customDailyGoalMl ?? null;
  let wakeUpTime: string = initialValues?.wakeUpTime || '07:00';
  let bedTime: string = initialValues?.bedTime || '22:00';

  const getState = (): OnboardingState => ({
    currentStep,
    gender,
    weightKg,
    activityLevel,
    customDailyGoalMl,
    wakeUpTime,
    bedTime,
    calculatedTargetMl: calculateIntakeGoal(weightKg, gender, activityLevel, customDailyGoalMl),
  });

  const setGender = (g: Gender) => {
    gender = g;
  };

  const setWeight = (w: number) => {
    weightKg = clampWeight(w);
  };

  const setActivity = (a: ActivityLevel) => {
    activityLevel = a;
  };

  const setCustomDailyGoal = (ml: number | null) => {
    customDailyGoalMl = ml !== null && ml > 0 ? Math.round(ml) : null;
  };

  const nextStep = () => {
    if (currentStep === 'gender') {
      currentStep = 'stats';
    } else if (currentStep === 'stats') {
      weightKg = clampWeight(weightKg);
      currentStep = 'target';
    }
  };

  const prevStep = () => {
    if (currentStep === 'stats') {
      currentStep = 'gender';
    } else if (currentStep === 'target') {
      currentStep = 'stats';
    }
  };

  const finish = (): UserProfile => {
    const finalWeight = clampWeight(weightKg);
    const now = new Date().toISOString();
    const profile: UserProfile = {
      id: (initialValues?.id || `user_${Date.now()}`) as UserId,
      gender,
      weightKg: finalWeight,
      activityLevel,
      customDailyGoalMl: customDailyGoalMl && customDailyGoalMl > 0 ? Math.round(customDailyGoalMl) : null,
      wakeUpTime,
      bedTime,
      notificationIntervalMinutes: initialValues?.notificationIntervalMinutes ?? 90,
      isDndEnabled: initialValues?.isDndEnabled ?? true,
      createdAt: initialValues?.createdAt || now,
      updatedAt: now,
    };

    if (onComplete) {
      onComplete(profile);
    }
    return profile;
  };

  return {
    getState,
    setGender,
    setWeight,
    setActivity,
    setCustomDailyGoal,
    nextStep,
    prevStep,
    finish,
  };
}

/**
 * OnboardingScreen - Interactive 3-step intake form.
 *
 * Step 1: Gender Selection ('male' | 'female' | 'other')
 * Step 2: Weight (kg) & Activity Level ('sedentary' | 'moderate' | 'active')
 * Step 3: Target Confirmation & Daily Hydration Goal display with custom override
 */
export function OnboardingScreen({ onComplete, initialValues }: OnboardingScreenProps) {
  const controller = createOnboardingController(initialValues, onComplete);
  const state = controller.getState();

  return (
    <div className="onboarding-container" data-testid="onboarding-screen">
      {/* Header & Step Indicator */}
      <header className="onboarding-header">
        <h1 className="onboarding-title">Personalize Your Hydration Plan</h1>
        <div className="step-indicator" data-step={state.currentStep}>
          <span className={`step-dot ${state.currentStep === 'gender' ? 'active' : ''}`}>1</span>
          <span className={`step-dot ${state.currentStep === 'stats' ? 'active' : ''}`}>2</span>
          <span className={`step-dot ${state.currentStep === 'target' ? 'active' : ''}`}>3</span>
        </div>
      </header>

      {/* Step 1: Gender */}
      {state.currentStep === 'gender' && (
        <section className="step-section" data-testid="step-gender">
          <h2>Select Your Biological Profile</h2>
          <p className="step-description">Helps determine base metabolic hydration rate.</p>
          <div className="gender-options">
            <button
              type="button"
              className={`option-btn ${state.gender === 'male' ? 'selected' : ''}`}
              onClick={() => controller.setGender('male')}
            >
              Male (35ml/kg)
            </button>
            <button
              type="button"
              className={`option-btn ${state.gender === 'female' ? 'selected' : ''}`}
              onClick={() => controller.setGender('female')}
            >
              Female (31ml/kg)
            </button>
            <button
              type="button"
              className={`option-btn ${state.gender === 'other' ? 'selected' : ''}`}
              onClick={() => controller.setGender('other')}
            >
              Other (31ml/kg)
            </button>
          </div>
          <button type="button" className="action-btn next-btn" onClick={controller.nextStep}>
            Continue
          </button>
        </section>
      )}

      {/* Step 2: Weight & Activity */}
      {state.currentStep === 'stats' && (
        <section className="step-section" data-testid="step-stats">
          <h2>Body Weight & Daily Activity</h2>
          <div className="input-group">
            <label htmlFor="weight-input">Weight (kg):</label>
            <input
              id="weight-input"
              type="number"
              min="30"
              max="250"
              defaultValue={state.weightKg}
              onChange={(e: any) => controller.setWeight(Number(e.target.value))}
            />
          </div>
          <div className="activity-options">
            <label>Activity Level:</label>
            <button
              type="button"
              className={`option-btn ${state.activityLevel === 'sedentary' ? 'selected' : ''}`}
              onClick={() => controller.setActivity('sedentary')}
            >
              Sedentary (+0ml)
            </button>
            <button
              type="button"
              className={`option-btn ${state.activityLevel === 'moderate' ? 'selected' : ''}`}
              onClick={() => controller.setActivity('moderate')}
            >
              Moderate (+350ml)
            </button>
            <button
              type="button"
              className={`option-btn ${state.activityLevel === 'active' ? 'selected' : ''}`}
              onClick={() => controller.setActivity('active')}
            >
              Active (+700ml)
            </button>
          </div>
          <div className="button-row">
            <button type="button" className="action-btn back-btn" onClick={controller.prevStep}>
              Back
            </button>
            <button type="button" className="action-btn next-btn" onClick={controller.nextStep}>
              Calculate Target
            </button>
          </div>
        </section>
      )}

      {/* Step 3: Target Confirmation & Custom Override */}
      {state.currentStep === 'target' && (
        <section className="step-section" data-testid="step-target">
          <h2>Your Recommended Daily Goal</h2>
          <div className="target-display-card">
            <span className="target-volume" data-testid="target-volume-display">
              {state.calculatedTargetMl} ml
            </span>
            <span className="target-subtext">per day</span>
          </div>

          <div className="custom-override-group">
            <label htmlFor="custom-goal-input">Custom Override (Optional):</label>
            <input
              id="custom-goal-input"
              type="number"
              placeholder="e.g. 2500"
              defaultValue={state.customDailyGoalMl ?? ''}
              onChange={(e: any) => {
                const val = Number(e.target.value);
                controller.setCustomDailyGoal(val > 0 ? val : null);
              }}
            />
          </div>

          <div className="button-row">
            <button type="button" className="action-btn back-btn" onClick={controller.prevStep}>
              Back
            </button>
            <button
              type="button"
              className="action-btn finish-btn"
              onClick={() => controller.finish()}
            >
              Start Tracking
            </button>
          </div>
        </section>
      )}
    </div>
  );
}

export default OnboardingScreen;
