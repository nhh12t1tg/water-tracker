/**
 * DND (Do Not Disturb) Helper
 * File: services/notification/dnd-helper.ts
 * 
 * Provides utilities for determining quiet hours (sleep intervals) and
 * calculating safe reminder windows around circular midnight boundaries.
 */

/**
 * Converts "HH:mm" or ISO 8601 string to total minutes since midnight [0..1439].
 */
export function parseTimeToMinutes(timeStr: string): number {
  let timePart = timeStr;
  if (timeStr.includes('T')) {
    const date = new Date(timeStr);
    if (!isNaN(date.getTime())) {
      return date.getHours() * 60 + date.getMinutes();
    }
    const parts = timeStr.split('T');
    if (parts.length > 1) {
      timePart = parts[1].slice(0, 5);
    }
  }

  const match = timePart.match(/(\d{1,2}):(\d{2})/);
  if (!match) {
    throw new Error(`Invalid time format: "${timeStr}". Expected 'HH:mm' or ISO 8601.`);
  }

  const hours = parseInt(match[1], 10);
  const minutes = parseInt(match[2], 10);
  return hours * 60 + minutes;
}

/**
 * Determines whether the given time falls within Quiet Hours (Do Not Disturb).
 * Handles circular midnight quiet hours (e.g., 22:00 to 07:00 next day).
 *
 * Rules:
 * - Bedtime is inclusive (quiet hours start at bedTime).
 * - Wake-up time is exclusive (user is awake at wakeUpTime).
 *
 * @param timeStr Time to evaluate ('HH:mm' or ISO 8601)
 * @param wakeUpTime Wake-up time ('HH:mm')
 * @param bedTime Bedtime ('HH:mm')
 * @returns true if in quiet hours, false otherwise
 */
export function isQuietHours(timeStr: string, wakeUpTime: string, bedTime: string): boolean {
  const currentMins = parseTimeToMinutes(timeStr);
  const wakeMins = parseTimeToMinutes(wakeUpTime);
  const bedMins = parseTimeToMinutes(bedTime);

  if (bedMins === wakeMins) {
    return false;
  }

  if (bedMins > wakeMins) {
    // Crosses midnight (e.g. 22:00 -> 07:00)
    // Quiet if current >= bedMins (22:00..23:59) OR current < wakeMins (00:00..06:59)
    return currentMins >= bedMins || currentMins < wakeMins;
  } else {
    // Same-day quiet hours (e.g. 01:00 -> 07:00)
    // Quiet if current >= bedMins AND current < wakeMins
    return currentMins >= bedMins && currentMins < wakeMins;
  }
}

/**
 * If fromTime falls within quiet hours, advances it to the next wake-up time.
 * If fromTime is already during waking hours, returns fromTime unchanged.
 *
 * @param fromTime The reference date/time
 * @param wakeUpTime Wake-up time in 'HH:mm'
 * @param bedTime Bedtime in 'HH:mm'
 * @returns Date when the user will be awake
 */
export function getNextActiveTime(fromTime: Date, wakeUpTime: string, bedTime: string): Date {
  const currentMins = fromTime.getHours() * 60 + fromTime.getMinutes();
  const wakeMins = parseTimeToMinutes(wakeUpTime);
  const bedMins = parseTimeToMinutes(bedTime);

  const hours = Math.floor(wakeMins / 60);
  const minutes = wakeMins % 60;

  if (bedMins > wakeMins) {
    // Circular overnight quiet hours: e.g. 22:00 to 07:00
    if (currentMins >= bedMins) {
      // It is bedtime tonight (e.g. 22:30) -> Wake-up is tomorrow morning
      const nextWake = new Date(fromTime);
      nextWake.setDate(nextWake.getDate() + 1);
      nextWake.setHours(hours, minutes, 0, 0);
      return nextWake;
    } else if (currentMins < wakeMins) {
      // It is early morning before wake-up (e.g. 04:00) -> Wake-up is today morning
      const nextWake = new Date(fromTime);
      nextWake.setHours(hours, minutes, 0, 0);
      return nextWake;
    }
  } else {
    // Same-day quiet hours: e.g. 01:00 to 07:00
    if (currentMins >= bedMins && currentMins < wakeMins) {
      const nextWake = new Date(fromTime);
      nextWake.setHours(hours, minutes, 0, 0);
      return nextWake;
    }
  }

  // Not in quiet hours
  return new Date(fromTime);
}

/**
 * Calculates sleep duration in hours from bedTime to wakeUpTime.
 * Handles circular midnight boundaries (e.g. 22:00 to 07:00 = 9 hours).
 */
export function calculateSleepDurationHours(wakeUpTime: string, bedTime: string): number {
  const wakeMins = parseTimeToMinutes(wakeUpTime);
  const bedMins = parseTimeToMinutes(bedTime);

  let diff = wakeMins - bedMins;
  if (diff <= 0) {
    diff += 24 * 60;
  }

  return Math.round((diff / 60) * 10) / 10;
}

