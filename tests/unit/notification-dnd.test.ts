import { describe, it } from 'node:test';
import assert from 'node:assert';
import {
  isWithinQuietHours,
  calculateNextReminder,
  parseTimeToMinutes,
  formatMinutesToTime,
} from '../helpers/dnd-helper.ts';

describe('Notification DND & Smart Reminder Unit Tests', () => {
  describe('DND Quiet Hours Circular Midnight Boundary', () => {
    it('suppresses notifications during default quiet hours (22:00 to 07:00)', () => {
      // 22:00 exact boundary
      const d2200 = new Date('2026-11-18T22:00:00');
      assert.strictEqual(isWithinQuietHours(d2200, '07:00', '22:00'), true);

      // Late night: 23:45
      const d2345 = new Date('2026-11-18T23:45:00');
      assert.strictEqual(isWithinQuietHours(d2345, '07:00', '22:00'), true);

      // Midnight: 00:00
      const d0000 = new Date('2026-11-18T00:00:00');
      assert.strictEqual(isWithinQuietHours(d0000, '07:00', '22:00'), true);

      // Early morning: 04:30
      const d0430 = new Date('2026-11-18T04:30:00');
      assert.strictEqual(isWithinQuietHours(d0430, '07:00', '22:00'), true);

      // 06:59 (1 minute before wake-up)
      const d0659 = new Date('2026-11-18T06:59:00');
      assert.strictEqual(isWithinQuietHours(d0659, '07:00', '22:00'), true);
    });

    it('allows notifications during active daytime hours', () => {
      // 07:00 wake-up boundary
      const d0700 = new Date('2026-11-18T07:00:00');
      assert.strictEqual(isWithinQuietHours(d0700, '07:00', '22:00'), false);

      // Mid-day: 12:30
      const d1230 = new Date('2026-11-18T12:30:00');
      assert.strictEqual(isWithinQuietHours(d1230, '07:00', '22:00'), false);

      // Evening before bedtime: 21:59
      const d2159 = new Date('2026-11-18T21:59:00');
      assert.strictEqual(isWithinQuietHours(d2159, '07:00', '22:00'), false);
    });

    it('supports custom user quiet hours (e.g. 23:30 to 08:15)', () => {
      // 23:25 -> still awake
      const d2325 = new Date('2026-11-18T23:25:00');
      assert.strictEqual(isWithinQuietHours(d2325, '08:15', '23:30'), false);

      // 23:35 -> quiet hours
      const d2335 = new Date('2026-11-18T23:35:00');
      assert.strictEqual(isWithinQuietHours(d2335, '08:15', '23:30'), true);

      // 08:10 -> quiet hours
      const d0810 = new Date('2026-11-18T08:10:00');
      assert.strictEqual(isWithinQuietHours(d0810, '08:15', '23:30'), true);

      // 08:20 -> awake
      const d0820 = new Date('2026-11-18T08:20:00');
      assert.strictEqual(isWithinQuietHours(d0820, '08:15', '23:30'), false);
    });

    it('handles daytime sleeping schedules for shift workers (09:00 to 17:00)', () => {
      const d1200 = new Date('2026-11-18T12:00:00');
      assert.strictEqual(isWithinQuietHours(d1200, '17:00', '09:00'), true);

      const d2000 = new Date('2026-11-18T20:00:00');
      assert.strictEqual(isWithinQuietHours(d2000, '17:00', '09:00'), false);
    });
  });

  describe('Review Focus 4: Smart Notification Delay After Drink', () => {
    it('reschedules reminder by 90 minutes when drink logged within 45 minutes of scheduled reminder', () => {
      const now = new Date('2026-11-18T10:00:00');
      // User just logged a drink 5 minutes ago at 09:55
      const lastDrink = '2026-11-18T09:55:00';

      const nextReminder = calculateNextReminder({
        now,
        lastDrinkTimestamp: lastDrink,
        wakeUpTime: '07:00',
        bedTime: '22:00',
        intervalMinutes: 90,
        isDndEnabled: true,
      });

      // 09:55 + 90 minutes = 11:25:00
      const expectedTime = new Date('2026-11-18T11:25:00').getTime();
      assert.strictEqual(
        nextReminder.getTime(),
        expectedTime,
        `Expected next reminder at 11:25, got ${nextReminder.toTimeString()}`
      );
    });

    it('uses standard reminder interval when no recent drink logged', () => {
      const now = new Date('2026-11-18T14:00:00');
      // Last drink was 3 hours ago
      const lastDrink = '2026-11-18T11:00:00';

      const nextReminder = calculateNextReminder({
        now,
        lastDrinkTimestamp: lastDrink,
        wakeUpTime: '07:00',
        bedTime: '22:00',
        intervalMinutes: 90,
        isDndEnabled: true,
      });

      // 14:00 + 90 min = 15:30:00
      const expectedTime = new Date('2026-11-18T15:30:00').getTime();
      assert.strictEqual(nextReminder.getTime(), expectedTime);
    });

    it('postpones to morning wakeUpTime when next reminder falls inside quiet hours', () => {
      const now = new Date('2026-11-18T21:15:00');
      // Drink logged at 21:10 -> nominal +90m delay puts it at 22:40 (inside 22:00-07:00 quiet hours)
      const lastDrink = '2026-11-18T21:10:00';

      const nextReminder = calculateNextReminder({
        now,
        lastDrinkTimestamp: lastDrink,
        wakeUpTime: '07:00',
        bedTime: '22:00',
        intervalMinutes: 90,
        isDndEnabled: true,
      });

      // Must be postponed to tomorrow morning at 07:00
      assert.strictEqual(nextReminder.getHours(), 7);
      assert.strictEqual(nextReminder.getMinutes(), 0);
      assert.strictEqual(nextReminder.getDate(), 19);
    });

    it('schedules for next wakeUpTime when currently in quiet hours', () => {
      const now = new Date('2026-11-18T23:30:00');

      const nextReminder = calculateNextReminder({
        now,
        lastDrinkTimestamp: null,
        wakeUpTime: '07:00',
        bedTime: '22:00',
        intervalMinutes: 90,
        isDndEnabled: true,
      });

      assert.strictEqual(nextReminder.getHours(), 7);
      assert.strictEqual(nextReminder.getMinutes(), 0);
      assert.strictEqual(nextReminder.getDate(), 19);
    });
  });

  describe('Time Parsing & Formatting Utilities', () => {
    it('parses HH:mm strings correctly', () => {
      assert.strictEqual(parseTimeToMinutes('00:00'), 0);
      assert.strictEqual(parseTimeToMinutes('07:30'), 450);
      assert.strictEqual(parseTimeToMinutes('22:00'), 1320);
      assert.strictEqual(parseTimeToMinutes('23:59'), 1439);
    });

    it('formats minutes since midnight into HH:mm correctly', () => {
      assert.strictEqual(formatMinutesToTime(0), '00:00');
      assert.strictEqual(formatMinutesToTime(450), '07:30');
      assert.strictEqual(formatMinutesToTime(1320), '22:00');
      assert.strictEqual(formatMinutesToTime(1439), '23:59');
    });
  });
});
