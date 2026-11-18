import { describe, it } from 'node:test';
import assert from 'node:assert';
import { isQuietHours, parseTimeToMinutes, getNextActiveTime, calculateSleepDurationHours } from '../dnd-helper.ts';
import { NotificationService, QUICK_ADD_250ML_ACTION } from '../notification.service.ts';
import type { UserProfile, WaterLogEntry, UserId, BeverageId, LogEntryId } from '../../../types/water.ts';

describe('DND Helper - Circular Midnight Quiet Hours', () => {
  const wakeUpTime = '07:00';
  const bedTime = '22:00';

  it('should identify times within quiet hours across midnight', () => {
    // Exact bedtime boundary (inclusive)
    assert.strictEqual(isQuietHours('22:00', wakeUpTime, bedTime), true);
    // Late night
    assert.strictEqual(isQuietHours('22:30', wakeUpTime, bedTime), true);
    assert.strictEqual(isQuietHours('23:59', wakeUpTime, bedTime), true);
    // Midnight
    assert.strictEqual(isQuietHours('00:00', wakeUpTime, bedTime), true);
    // Early morning before wake-up
    assert.strictEqual(isQuietHours('03:15', wakeUpTime, bedTime), true);
    assert.strictEqual(isQuietHours('06:59', wakeUpTime, bedTime), true);
  });

  it('should identify times outside quiet hours', () => {
    // Right before bedtime
    assert.strictEqual(isQuietHours('21:59', wakeUpTime, bedTime), false);
    // Exact wake-up time (exclusive / user is now awake)
    assert.strictEqual(isQuietHours('07:00', wakeUpTime, bedTime), false);
    // Midday
    assert.strictEqual(isQuietHours('07:01', wakeUpTime, bedTime), false);
    assert.strictEqual(isQuietHours('12:00', wakeUpTime, bedTime), false);
    assert.strictEqual(isQuietHours('18:45', wakeUpTime, bedTime), false);
  });

  it('should handle ISO 8601 timestamps', () => {
    assert.strictEqual(isQuietHours('2026-11-18T22:30:00', wakeUpTime, bedTime), true);
    assert.strictEqual(isQuietHours('2026-11-18T14:15:00', wakeUpTime, bedTime), false);
  });

  it('should handle custom sleep schedules across midnight (e.g. 23:00 to 08:00)', () => {
    assert.strictEqual(isQuietHours('22:59', '08:00', '23:00'), false);
    assert.strictEqual(isQuietHours('23:00', '08:00', '23:00'), true);
    assert.strictEqual(isQuietHours('07:59', '08:00', '23:00'), true);
    assert.strictEqual(isQuietHours('08:00', '08:00', '23:00'), false);
  });

  it('should handle same-day quiet hours (e.g. 01:00 to 07:00)', () => {
    assert.strictEqual(isQuietHours('03:00', '07:00', '01:00'), true);
    assert.strictEqual(isQuietHours('00:30', '07:00', '01:00'), false);
    assert.strictEqual(isQuietHours('08:00', '07:00', '01:00'), false);
  });

  it('should return false if bedTime equals wakeUpTime', () => {
    assert.strictEqual(isQuietHours('12:00', '07:00', '07:00'), false);
  });

  it('should advance quiet hours to the next wake-up time', () => {
    // Current time is 23:00 on 2026-11-18. Bed is 22:00, Wake is 07:00.
    const lateNight = new Date('2026-11-18T23:00:00');
    const nextActive = getNextActiveTime(lateNight, wakeUpTime, bedTime);
    assert.strictEqual(nextActive.getDate(), 19);
    assert.strictEqual(nextActive.getHours(), 7);
    assert.strictEqual(nextActive.getMinutes(), 0);

    // Current time is 04:30 on 2026-11-19. Wake is 07:00 today.
    const earlyMorning = new Date('2026-11-19T04:30:00');
    const nextActiveSameDay = getNextActiveTime(earlyMorning, wakeUpTime, bedTime);
    assert.strictEqual(nextActiveSameDay.getDate(), 19);
    assert.strictEqual(nextActiveSameDay.getHours(), 7);
    assert.strictEqual(nextActiveSameDay.getMinutes(), 0);
  });

  it('should accurately calculate sleep duration across midnight', () => {
    assert.strictEqual(calculateSleepDurationHours('07:00', '22:00'), 9);
    assert.strictEqual(calculateSleepDurationHours('07:00', '23:30'), 7.5);
    assert.strictEqual(calculateSleepDurationHours('08:00', '01:00'), 7);
  });
});

describe('Notification Service - Smart Scheduler & Delay', () => {
  const service = NotificationService.getInstance();
  const mockProfile: UserProfile = {
    id: 'user-1' as UserId,
    gender: 'male',
    weightKg: 70,
    activityLevel: 'moderate',
    customDailyGoalMl: null,
    wakeUpTime: '07:00',
    bedTime: '22:00',
    notificationIntervalMinutes: 90,
    isDndEnabled: true,
    createdAt: '2026-11-18T00:00:00.000Z',
    updatedAt: '2026-11-18T00:00:00.000Z',
  };

  it('should provide quick action [+250ml Nước]', () => {
    assert.strictEqual(QUICK_ADD_250ML_ACTION.id, 'QUICK_ADD_250ML');
    assert.strictEqual(QUICK_ADD_250ML_ACTION.title, '+250ml Nước');
    assert.strictEqual(QUICK_ADD_250ML_ACTION.payload?.volumeMl, 250);
  });

  it('should delay next reminder by 90 minutes when user drank water in the past 45-60 minutes', () => {
    // Current time is 14:00
    const currentTime = new Date('2026-11-18T14:00:00');
    // Last drink was 50 minutes ago (13:10), which falls in the 45-60 min window
    const lastDrinkTime = new Date('2026-11-18T13:10:00').toISOString();

    const nextSchedule = service.calculateNextScheduleTime(
      lastDrinkTime,
      mockProfile.notificationIntervalMinutes,
      mockProfile.wakeUpTime,
      mockProfile.bedTime,
      currentTime,
      mockProfile.isDndEnabled
    );

    assert.ok(nextSchedule !== null);
    // Delayed by 90 minutes from current time (14:00 + 90m = 15:30)
    const expectedTime = new Date('2026-11-18T15:30:00');
    assert.strictEqual(nextSchedule.getTime(), expectedTime.getTime());
  });

  it('should handle boundary of 45-60 minutes drink suppression', () => {
    const currentTime = new Date('2026-11-18T14:00:00');

    // Exactly 45 minutes ago (13:15)
    const drink45mAgo = new Date('2026-11-18T13:15:00').toISOString();
    const next45 = service.calculateNextScheduleTime(
      drink45mAgo,
      mockProfile.notificationIntervalMinutes,
      mockProfile.wakeUpTime,
      mockProfile.bedTime,
      currentTime,
      mockProfile.isDndEnabled
    );
    assert.strictEqual(next45?.getTime(), new Date('2026-11-18T15:30:00').getTime());

    // Exactly 60 minutes ago (13:00)
    const drink60mAgo = new Date('2026-11-18T13:00:00').toISOString();
    const next60 = service.calculateNextScheduleTime(
      drink60mAgo,
      mockProfile.notificationIntervalMinutes,
      mockProfile.wakeUpTime,
      mockProfile.bedTime,
      currentTime,
      mockProfile.isDndEnabled
    );
    assert.strictEqual(next60?.getTime(), new Date('2026-11-18T15:30:00').getTime());
  });

  it('should schedule at regular interval if drink was beyond 60 minutes ago', () => {
    // Current time is 14:00, last drink was 65 minutes ago (12:55)
    const currentTime = new Date('2026-11-18T14:00:00');
    const lastDrinkTime = new Date('2026-11-18T12:55:00').toISOString();

    const nextSchedule = service.calculateNextScheduleTime(
      lastDrinkTime,
      mockProfile.notificationIntervalMinutes, // 90 min
      mockProfile.wakeUpTime,
      mockProfile.bedTime,
      currentTime,
      mockProfile.isDndEnabled
    );

    assert.ok(nextSchedule !== null);
    // Regular 90-minute interval from now (14:00 + 90m = 15:30)
    const expectedTime = new Date('2026-11-18T15:30:00');
    assert.strictEqual(nextSchedule.getTime(), expectedTime.getTime());
  });

  it('should suppress reminders during quiet hours and postpone to wakeUpTime', () => {
    // Current time is 21:30, interval is 90 min -> next schedule would normally be 23:00 (Quiet Hours)
    const currentTime = new Date('2026-11-18T21:30:00');

    const nextSchedule = service.calculateNextScheduleTime(
      null,
      mockProfile.notificationIntervalMinutes,
      mockProfile.wakeUpTime,
      mockProfile.bedTime,
      currentTime,
      true // DND enabled
    );

    assert.ok(nextSchedule !== null);
    // Must be pushed to tomorrow morning at 07:00
    assert.strictEqual(nextSchedule.getDate(), 19);
    assert.strictEqual(nextSchedule.getHours(), 7);
    assert.strictEqual(nextSchedule.getMinutes(), 0);
  });

  it('should return wakeUpTime if current time is already within quiet hours', () => {
    // Current time is 02:00 on 2026-11-19 (in quiet hours 22:00-07:00)
    const currentTime = new Date('2026-11-19T02:00:00');

    const nextSchedule = service.calculateNextScheduleTime(
      null,
      mockProfile.notificationIntervalMinutes,
      mockProfile.wakeUpTime,
      mockProfile.bedTime,
      currentTime,
      true
    );

    assert.ok(nextSchedule !== null);
    assert.strictEqual(nextSchedule.getDate(), 19);
    assert.strictEqual(nextSchedule.getHours(), 7);
    assert.strictEqual(nextSchedule.getMinutes(), 0);
  });

  it('should create notification payload with quick action [+250ml Nước]', async () => {
    const currentTime = new Date('2026-11-18T10:00:00');
    const mockEntry: WaterLogEntry = {
      id: 'log-1' as LogEntryId,
      timestamp: '2026-11-18T09:10:00', // 50 mins ago
      beverageId: 'bev-1' as BeverageId,
      rawVolumeMl: 250,
      effectiveVolumeMl: 250,
    };

    const payload = await service.scheduleNextReminder(mockProfile, mockEntry, currentTime);
    assert.ok(payload !== null);
    assert.strictEqual(payload.actions.length, 1);
    assert.strictEqual(payload.actions[0].title, '+250ml Nước');
    assert.strictEqual(payload.actions[0].id, 'QUICK_ADD_250ML');
    assert.strictEqual(payload.scheduledTime.getTime(), new Date('2026-11-18T11:30:00').getTime());
  });

  it('should track pending notifications and allow cancelling them', async () => {
    service.cancelAllReminders();
    assert.strictEqual(service.getPendingNotifications().length, 0);

    await service.scheduleNextReminder(mockProfile);
    assert.strictEqual(service.getPendingNotifications().length, 1);

    service.cancelAllReminders();
    assert.strictEqual(service.getPendingNotifications().length, 0);
  });
});
