import test from 'node:test';
import assert from 'node:assert/strict';
import { WaterStorageService } from '../water-storage.service';
import { UserProfile, WaterLogEntry, UserId, BeverageId, LogEntryId } from '../../../types/water';

test('WaterStorageService - UserProfile CRUD', async () => {
  const service = new WaterStorageService();
  await service.initialize();

  // Initially null
  const initial = await service.getUserProfile();
  assert.strictEqual(initial, null);

  const profile: UserProfile = {
    id: 'user_123' as UserId,
    gender: 'male',
    weightKg: 75,
    activityLevel: 'moderate',
    customDailyGoalMl: null,
    wakeUpTime: '07:00',
    bedTime: '22:30',
    notificationIntervalMinutes: 90,
    isDndEnabled: true,
    createdAt: '2026-11-18T00:00:00.000Z',
    updatedAt: '2026-11-18T00:00:00.000Z',
  };

  await service.saveUserProfile(profile);
  const fetched = await service.getUserProfile();
  assert.notStrictEqual(fetched, null);
  assert.strictEqual(fetched?.id, 'user_123');
  assert.strictEqual(fetched?.weightKg, 75);
  assert.strictEqual(fetched?.activityLevel, 'moderate');

  // Update profile with customDailyGoalMl
  const updated: UserProfile = {
    ...profile,
    customDailyGoalMl: 3000,
    updatedAt: '2026-11-18T12:00:00.000Z',
  };
  await service.saveUserProfile(updated);

  const fetchedUpdated = await service.getUserProfile();
  assert.strictEqual(fetchedUpdated?.customDailyGoalMl, 3000);
});

test('WaterStorageService - DailyProgress empty state', async () => {
  const service = new WaterStorageService();
  await service.initialize();

  const progress = await service.getDailyProgress('2026-11-18');
  assert.strictEqual(progress.date, '2026-11-18');
  assert.strictEqual(progress.totalLoggedMl, 0);
  assert.strictEqual(progress.effectiveHydrationMl, 0);
  assert.strictEqual(progress.percentage, 0);
  assert.strictEqual(progress.isGoalAchieved, false);
  assert.strictEqual(progress.entries.length, 0);
  assert.ok(progress.targetVolumeMl > 0);
});

test('WaterStorageService - logDrink and aggregation with floating-point precision', async () => {
  const service = new WaterStorageService();
  await service.initialize();

  const profile: UserProfile = {
    id: 'user_1' as UserId,
    gender: 'female',
    weightKg: 60,
    activityLevel: 'sedentary',
    customDailyGoalMl: 2000,
    wakeUpTime: '07:00',
    bedTime: '22:00',
    notificationIntervalMinutes: 90,
    isDndEnabled: true,
    createdAt: '2026-11-18T00:00:00.000Z',
    updatedAt: '2026-11-18T00:00:00.000Z',
  };
  await service.saveUserProfile(profile);

  // Log water: 250ml raw, factor 1.0 -> 250ml effective
  const entry1: WaterLogEntry = {
    id: 'log_1' as LogEntryId,
    timestamp: '2026-11-18T08:30:00.000Z',
    beverageId: 'water' as BeverageId,
    rawVolumeMl: 250,
    effectiveVolumeMl: 250,
  };
  const prog1 = await service.logDrink(entry1);
  assert.strictEqual(prog1.totalLoggedMl, 250);
  assert.strictEqual(prog1.effectiveHydrationMl, 250);
  assert.strictEqual(prog1.percentage, 13); // 250 / 2000 = 12.5% -> round to 13%
  assert.strictEqual(prog1.isGoalAchieved, false);
  assert.strictEqual(prog1.entries.length, 1);

  // Log coffee: 250ml raw, factor 0.8 -> 200ml effective (floating point check)
  const entry2: WaterLogEntry = {
    id: 'log_2' as LogEntryId,
    timestamp: '2026-11-18T10:00:00.000Z',
    beverageId: 'coffee' as BeverageId,
    rawVolumeMl: 250,
    effectiveVolumeMl: Math.round(250 * 0.8), // 200
  };
  const prog2 = await service.logDrink(entry2);
  assert.strictEqual(prog2.totalLoggedMl, 500);
  assert.strictEqual(prog2.effectiveHydrationMl, 450);
  assert.strictEqual(prog2.percentage, 23); // 450 / 2000 = 22.5% -> round to 23%
  assert.strictEqual(prog2.entries.length, 2);

  // Log large drink reaching target
  const entry3: WaterLogEntry = {
    id: 'log_3' as LogEntryId,
    timestamp: '2026-11-18T15:00:00.000Z',
    beverageId: 'water' as BeverageId,
    rawVolumeMl: 1600,
    effectiveVolumeMl: 1600,
  };
  const prog3 = await service.logDrink(entry3);
  assert.strictEqual(prog3.totalLoggedMl, 2100);
  assert.strictEqual(prog3.effectiveHydrationMl, 2050);
  assert.strictEqual(prog3.percentage, 103);
  assert.strictEqual(prog3.isGoalAchieved, true);
});

test('WaterStorageService - multi-date isolation across midnight boundary', async () => {
  const service = new WaterStorageService();
  await service.initialize();

  // Log at 23:59 on Nov 18
  await service.logDrink({
    id: 'log_night' as LogEntryId,
    timestamp: '2026-11-18T23:59:00.000Z',
    beverageId: 'water' as BeverageId,
    rawVolumeMl: 300,
    effectiveVolumeMl: 300,
  });

  // Log at 00:01 on Nov 19
  await service.logDrink({
    id: 'log_morning' as LogEntryId,
    timestamp: '2026-11-19T00:01:00.000Z',
    beverageId: 'water' as BeverageId,
    rawVolumeMl: 250,
    effectiveVolumeMl: 250,
  });

  const prog18 = await service.getDailyProgress('2026-11-18');
  assert.strictEqual(prog18.entries.length, 1);
  assert.strictEqual(prog18.totalLoggedMl, 300);

  const prog19 = await service.getDailyProgress('2026-11-19');
  assert.strictEqual(prog19.entries.length, 1);
  assert.strictEqual(prog19.totalLoggedMl, 250);
});

test('WaterStorageService - getWeeklyProgress returns 7 consecutive days', async () => {
  const service = new WaterStorageService();
  await service.initialize();

  await service.logDrink({
    id: 'log_mid' as LogEntryId,
    timestamp: '2026-11-15T12:00:00.000Z',
    beverageId: 'water' as BeverageId,
    rawVolumeMl: 500,
    effectiveVolumeMl: 500,
  });

  const weekly = await service.getWeeklyProgress('2026-11-18');
  assert.strictEqual(weekly.length, 7);

  const expectedDates = [
    '2026-11-12',
    '2026-11-13',
    '2026-11-14',
    '2026-11-15',
    '2026-11-16',
    '2026-11-17',
    '2026-11-18',
  ];

  assert.deepStrictEqual(weekly.map(d => d.date), expectedDates);

  const day15 = weekly.find(d => d.date === '2026-11-15');
  assert.strictEqual(day15?.totalLoggedMl, 500);

  const day18 = weekly.find(d => d.date === '2026-11-18');
  assert.strictEqual(day18?.totalLoggedMl, 0);
});
