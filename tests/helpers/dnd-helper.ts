/**
 * DND & Smart Reminder Calculation Helper
 * Logic for circular midnight boundary checks and smart reminder suppression.
 */

export interface ReminderScheduleOptions {
  now: Date;
  lastDrinkTimestamp?: string | null;
  wakeUpTime: string; // 'HH:mm', e.g. '07:00'
  bedTime: string;    // 'HH:mm', e.g. '22:00'
  intervalMinutes: number; // e.g. 90 or 120
  isDndEnabled: boolean;
}

/**
 * Parse 'HH:mm' time string into minutes since midnight (0 - 1439)
 */
export function parseTimeToMinutes(timeStr: string): number {
  const [h, m] = timeStr.split(':').map((val) => parseInt(val, 10));
  return (h || 0) * 60 + (m || 0);
}

/**
 * Format minutes since midnight into 'HH:mm'
 */
export function formatMinutesToTime(minutes: number): string {
  const normalized = ((minutes % 1440) + 1440) % 1440;
  const h = Math.floor(normalized / 60);
  const m = normalized % 60;
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`;
}

/**
 * Checks whether a given timestamp falls within quiet/sleep hours.
 * Accurately handles circular midnight boundary (e.g. 22:00 -> 07:00).
 */
export function isWithinQuietHours(
  date: Date,
  wakeUpTime: string = '07:00',
  bedTime: string = '22:00'
): boolean {
  const currentMinutes = date.getHours() * 60 + date.getMinutes();
  const wakeMinutes = parseTimeToMinutes(wakeUpTime);
  const bedMinutes = parseTimeToMinutes(bedTime);

  if (bedMinutes > wakeMinutes) {
    // Standard night schedule across midnight (e.g. 22:00 -> 07:00)
    // Quiet hours if current >= 22:00 OR current < 07:00
    return currentMinutes >= bedMinutes || currentMinutes < wakeMinutes;
  } else if (bedMinutes < wakeMinutes) {
    // Day sleeping (e.g. 08:00 -> 16:00)
    return currentMinutes >= bedMinutes && currentMinutes < wakeMinutes;
  } else {
    // bedTime === wakeUpTime -> no quiet hours
    return false;
  }
}

/**
 * Calculate the next reminder notification timestamp.
 * 
 * Rules:
 * 1. If DND is enabled and current time is in quiet hours, schedule for today/tomorrow's wakeUpTime.
 * 2. If user logged a drink recently (within 45 minutes of the nominal next reminder),
 *    reschedule the reminder by 90 minutes from the drink time (Review Focus 4).
 * 3. If rescheduled reminder falls into quiet hours, postpone to next wakeUpTime.
 */
export function calculateNextReminder(options: ReminderScheduleOptions): Date {
  const {
    now,
    lastDrinkTimestamp,
    wakeUpTime,
    bedTime,
    intervalMinutes,
    isDndEnabled,
  } = options;

  const wakeMinutes = parseTimeToMinutes(wakeUpTime);

  // 1. If currently in quiet hours, return next wakeUpTime
  if (isDndEnabled && isWithinQuietHours(now, wakeUpTime, bedTime)) {
    const nextWake = new Date(now);
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    if (currentMinutes >= parseTimeToMinutes(bedTime)) {
      // It's late evening; next wake-up is tomorrow morning
      nextWake.setDate(nextWake.getDate() + 1);
    }
    nextWake.setHours(Math.floor(wakeMinutes / 60), wakeMinutes % 60, 0, 0);
    return nextWake;
  }

  // Nominal next reminder
  let candidateTime = new Date(now.getTime() + intervalMinutes * 60 * 1000);

  // 2. Check for recent drink suppression (Review Focus 4)
  if (lastDrinkTimestamp) {
    const lastDrinkDate = new Date(lastDrinkTimestamp);
    const timeSinceDrinkMs = now.getTime() - lastDrinkDate.getTime();
    const timeUntilReminderMs = candidateTime.getTime() - now.getTime();

    // If drink was logged recently and next reminder is within 45 minutes of the drink,
    // delay reminder by 90 minutes from the drink timestamp
    const msSinceDrinkUntilReminder = candidateTime.getTime() - lastDrinkDate.getTime();
    const threshold45MinMs = 45 * 60 * 1000;

    if (msSinceDrinkUntilReminder <= threshold45MinMs || timeSinceDrinkMs < threshold45MinMs) {
      const delayAfterDrinkMs = 90 * 60 * 1000;
      candidateTime = new Date(lastDrinkDate.getTime() + delayAfterDrinkMs);
    }
  }

  // 3. If candidate time falls inside quiet hours, advance to next wakeUpTime
  if (isDndEnabled && isWithinQuietHours(candidateTime, wakeUpTime, bedTime)) {
    const nextWake = new Date(candidateTime);
    const candidateMinutes = candidateTime.getHours() * 60 + candidateTime.getMinutes();
    if (candidateMinutes >= parseTimeToMinutes(bedTime)) {
      nextWake.setDate(nextWake.getDate() + 1);
    }
    nextWake.setHours(Math.floor(wakeMinutes / 60), wakeMinutes % 60, 0, 0);
    return nextWake;
  }

  return candidateTime;
}
