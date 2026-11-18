import test from 'node:test';
import assert from 'node:assert/strict';
import { createOnboardingController } from '../screens/OnboardingScreen';
import { UserProfile } from '../../../types/water';

test('OnboardingController steps through flow and computes target', () => {
  let completedProfile: UserProfile | null = null;
  const controller = createOnboardingController(
    { gender: 'female', weightKg: 65, activityLevel: 'moderate' },
    (profile) => {
      completedProfile = profile;
    }
  );

  let state = controller.getState();
  assert.strictEqual(state.currentStep, 'gender');
  assert.strictEqual(state.gender, 'female');

  // Change gender to male
  controller.setGender('male');
  state = controller.getState();
  assert.strictEqual(state.gender, 'male');

  // Next -> stats step
  controller.nextStep();
  state = controller.getState();
  assert.strictEqual(state.currentStep, 'stats');

  // Set weight and activity
  controller.setWeight(80);
  controller.setActivity('active');
  state = controller.getState();
  assert.strictEqual(state.weightKg, 80);
  assert.strictEqual(state.activityLevel, 'active');

  // Next -> target step
  controller.nextStep();
  state = controller.getState();
  assert.strictEqual(state.currentStep, 'target');
  // Target: 80 * 35 + 700 = 2800 + 700 = 3500
  assert.strictEqual(state.calculatedTargetMl, 3500);

  // Set custom override: 3200
  controller.setCustomDailyGoal(3200);
  state = controller.getState();
  assert.strictEqual(state.customDailyGoalMl, 3200);
  assert.strictEqual(state.calculatedTargetMl, 3200);

  // Finish
  const profile = controller.finish();
  assert.strictEqual(profile.gender, 'male');
  assert.strictEqual(profile.weightKg, 80);
  assert.strictEqual(profile.activityLevel, 'active');
  assert.strictEqual(profile.customDailyGoalMl, 3200);
  assert.notStrictEqual(completedProfile, null);
  assert.strictEqual(completedProfile?.id, profile.id);
});

test('OnboardingController navigation back and forward works as expected', () => {
  const controller = createOnboardingController();
  assert.strictEqual(controller.getState().currentStep, 'gender');

  controller.nextStep();
  assert.strictEqual(controller.getState().currentStep, 'stats');

  controller.nextStep();
  assert.strictEqual(controller.getState().currentStep, 'target');

  controller.prevStep();
  assert.strictEqual(controller.getState().currentStep, 'stats');

  controller.prevStep();
  assert.strictEqual(controller.getState().currentStep, 'gender');
});
