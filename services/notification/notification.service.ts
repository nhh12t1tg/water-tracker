import type { UserProfile, WaterLogEntry } from '../../types/water.ts';
import { isQuietHours, getNextActiveTime } from './dnd-helper.ts';

/**
 * Action attached to interactive notifications.
 */
export interface NotificationAction {
  id: string;
  title: string;
  payload?: Record<string, unknown>;
}

/**
 * Notification payload scheduled for presentation.
 */
export interface NotificationPayload {
  id: string;
  title: string;
  body: string;
  scheduledTime: Date;
  actions: NotificationAction[];
}

/**
 * Quick action payload to log water directly from the notification bar.
 */
export const QUICK_ADD_250ML_ACTION: NotificationAction = {
  id: 'QUICK_ADD_250ML',
  title: '+250ml Nước',
  payload: {
    volumeMl: 250,
  },
};

/**
 * Smart Notification Service
 * File: services/notification/notification.service.ts
 *
 * Implements intelligent hydration reminders:
 * - Smart delay: Suppresses immediate reminders if user logged a drink recently (past 45-60 min)
 *   and extends next reminder by 90 minutes.
 * - Strict Quiet Hours (DND): Suppresses notifications during sleep (bedTime -> wakeUpTime).
 * - Interactive action: Appends [+250ml Nước] quick action button.
 */
export class NotificationService {
  private static instance: NotificationService | null = null;
  private pendingNotifications: NotificationPayload[] = [];

  private constructor() {}

  public static getInstance(): NotificationService {
    if (!NotificationService.instance) {
      NotificationService.instance = new NotificationService();
    }
    return NotificationService.instance;
  }

  /**
   * Helper delegate to check quiet hours.
   */
  public isQuietHours(timeStr: string, wakeUp: string, bed: string): boolean {
    return isQuietHours(timeStr, wakeUp, bed);
  }

  /**
   * Calculates the next safe, smart reminder time.
   *
   * @param lastDrinkTime Timestamp of user's last logged drink, or null
   * @param intervalMinutes Configured interval between reminders (default 90m)
   * @param wakeUp Wake-up time 'HH:mm'
   * @param bed Bedtime 'HH:mm'
   * @param currentTime Reference current time (defaults to Date.now())
   * @param isDndEnabled Whether Do Not Disturb is respected
   * @returns Date of next scheduled reminder, or null if cannot schedule
   */
  public calculateNextScheduleTime(
    lastDrinkTime: string | Date | null,
    intervalMinutes: number = 90,
    wakeUp: string = '07:00',
    bed: string = '22:00',
    currentTime: Date = new Date(),
    isDndEnabled: boolean = true
  ): Date | null {
    const now = new Date(currentTime);

    // Format current time as HH:mm
    const currentHH = String(now.getHours()).padStart(2, '0');
    const currentMM = String(now.getMinutes()).padStart(2, '0');
    const currentTimeStr = `${currentHH}:${currentMM}`;

    // If currently already in quiet hours, schedule for wake-up time
    if (isDndEnabled && isQuietHours(currentTimeStr, wakeUp, bed)) {
      return getNextActiveTime(now, wakeUp, bed);
    }

    let targetTime: Date;

    if (lastDrinkTime) {
      const lastDrinkDate = typeof lastDrinkTime === 'string' ? new Date(lastDrinkTime) : lastDrinkTime;
      const elapsedMinutes = (now.getTime() - lastDrinkDate.getTime()) / (60 * 1000);

      // Smart delay: if user logged a drink within the past 45-60 minutes (or under 60m)
      if (elapsedMinutes >= 0 && elapsedMinutes <= 60) {
        // Delay next reminder by 90 minutes
        targetTime = new Date(now.getTime() + 90 * 60 * 1000);
      } else {
        targetTime = new Date(now.getTime() + intervalMinutes * 60 * 1000);
      }
    } else {
      targetTime = new Date(now.getTime() + intervalMinutes * 60 * 1000);
    }

    // Check if scheduled targetTime falls into Quiet Hours
    if (isDndEnabled) {
      const targetHH = String(targetTime.getHours()).padStart(2, '0');
      const targetMM = String(targetTime.getMinutes()).padStart(2, '0');
      const targetTimeStr = `${targetHH}:${targetMM}`;

      if (isQuietHours(targetTimeStr, wakeUp, bed)) {
        targetTime = getNextActiveTime(targetTime, wakeUp, bed);
      }
    }

    return targetTime;
  }

  /**
   * Schedules the next hydration reminder for the given user profile.
   */
  public async scheduleNextReminder(
    profile: UserProfile,
    lastDrink?: WaterLogEntry,
    currentTime: Date = new Date()
  ): Promise<NotificationPayload | null> {
    const nextTime = this.calculateNextScheduleTime(
      lastDrink ? lastDrink.timestamp : null,
      profile.notificationIntervalMinutes || 90,
      profile.wakeUpTime || '07:00',
      profile.bedTime || '22:00',
      currentTime,
      profile.isDndEnabled
    );

    if (!nextTime) {
      return null;
    }

    const payload: NotificationPayload = {
      id: `reminder-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      title: 'Đã đến giờ uống nước! 💧',
      body: 'Nạp thêm nước để cơ thể luôn khỏe mạnh và tràn đầy năng lượng.',
      scheduledTime: nextTime,
      actions: [QUICK_ADD_250ML_ACTION],
    };

    this.pendingNotifications.push(payload);
    return payload;
  }

  /**
   * Retrieves currently pending notification schedules.
   */
  public getPendingNotifications(): NotificationPayload[] {
    return [...this.pendingNotifications];
  }

  /**
   * Cancels all pending notifications.
   */
  public cancelAllReminders(): void {
    this.pendingNotifications = [];
  }
}
