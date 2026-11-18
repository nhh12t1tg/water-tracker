import React, { useState } from 'react';
import { DailyProgress, StreakState, WaterLogEntry } from '../../../types/water';
import { WeeklyChart } from '../components/WeeklyChart';
import { StreakBadges } from '../components/StreakBadges';

export interface HistoryScreenProps {
  /** Array of 7-day daily hydration progress objects */
  weeklyLogs: DailyProgress[];
  /** Current streak retention state */
  streakState: StreakState;
  /** Callback to trigger Rewarded Video Ad for streak protection token */
  onWatchRewardForStreakFreeze?: () => void;
  /** Alias callback for streak restoration */
  onRestoreStreak?: () => void;
  /** Navigation back action */
  onBack?: () => void;
  /** Dark mode theme toggle */
  isDark?: boolean;
}

/**
 * HistoryScreen - Comprehensive History & Streaks Dashboard
 * Assembles WeeklyChart, StreakBadges, and granular drink log records.
 */
export const HistoryScreen: React.FC<HistoryScreenProps> = ({
  weeklyLogs,
  streakState,
  onWatchRewardForStreakFreeze,
  onRestoreStreak,
  onBack,
  isDark = true,
}) => {
  // Default selected date to today or the most recent log date
  const latestDate =
    weeklyLogs.length > 0 ? weeklyLogs[weeklyLogs.length - 1].date : '';
  const [selectedDate, setSelectedDate] = useState<string>(latestDate);

  const selectedDayProgress = weeklyLogs.find((d) => d.date === selectedDate) || null;

  const handleRewardTrigger = () => {
    if (onWatchRewardForStreakFreeze) {
      onWatchRewardForStreakFreeze();
    } else if (onRestoreStreak) {
      onRestoreStreak();
    }
  };

  // Theme Tokens
  const theme = {
    screenBg: isDark ? '#090E17' : '#F0F9FF',
    textColor: isDark ? '#F0F9FF' : '#0F172A',
    subTextColor: isDark ? '#94A3B8' : '#64748B',
    cardBg: isDark ? 'rgba(19, 30, 49, 0.7)' : 'rgba(255, 255, 255, 0.85)',
    cardBorder: isDark
      ? '1px solid rgba(56, 189, 248, 0.2)'
      : '1px solid rgba(255, 255, 255, 0.4)',
    entryBg: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.03)',
  };

  return (
    <div
      style={{
        backgroundColor: theme.screenBg,
        minHeight: '100vh',
        color: theme.textColor,
        padding: '20px 16px 40px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
        fontFamily: 'system-ui, -apple-system, sans-serif',
        boxSizing: 'border-box',
      }}
    >
      {/* 1. TOP APP BAR */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          paddingBottom: '8px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {onBack && (
            <button
              onClick={onBack}
              style={{
                background: 'rgba(255, 255, 255, 0.1)',
                border: 'none',
                borderRadius: '50%',
                width: '36px',
                height: '36px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: theme.textColor,
                cursor: 'pointer',
                fontSize: '16px',
              }}
            >
              ←
            </button>
          )}
          <h1
            style={{
              margin: 0,
              fontSize: '22px',
              fontWeight: 800,
              letterSpacing: '-0.5px',
            }}
          >
            Lịch Sử & Huy Hiệu
          </h1>
        </div>
      </div>

      {/* 2. 7-DAY GLASSMORPHIC WEEKLY BAR CHART */}
      <WeeklyChart
        data={weeklyLogs}
        selectedDate={selectedDate}
        onSelectDay={(date) => setSelectedDate(date)}
        isDark={isDark}
      />

      {/* 3. STREAK RETENTION BADGES & REWARDED AD TRIGGER */}
      <StreakBadges
        streakState={streakState}
        onWatchRewardForStreakFreeze={handleRewardTrigger}
        isDark={isDark}
      />

      {/* 4. DETAILED LOG RECORDS FOR SELECTED DAY */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '12px',
        }}
      >
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <h3
            style={{
              margin: 0,
              fontSize: '17px',
              fontWeight: 700,
              color: theme.textColor,
            }}
          >
            Chi Tiết Ngày: {selectedDate || 'Hôm nay'}
          </h3>

          {selectedDayProgress && (
            <span
              style={{
                fontSize: '12px',
                fontWeight: 700,
                color: selectedDayProgress.isGoalAchieved ? '#10B981' : '#F59E0B',
                background: selectedDayProgress.isGoalAchieved
                  ? 'rgba(16, 185, 129, 0.15)'
                  : 'rgba(245, 158, 11, 0.15)',
                padding: '4px 10px',
                borderRadius: '12px',
              }}
            >
              {selectedDayProgress.isGoalAchieved
                ? '✓ Đạt mục tiêu'
                : `${selectedDayProgress.percentage}%`}
            </span>
          )}
        </div>

        {/* Selected Day Summary Card */}
        {selectedDayProgress && (
          <div
            style={{
              background: theme.cardBg,
              border: theme.cardBorder,
              borderRadius: '18px',
              padding: '14px 18px',
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              backdropFilter: 'blur(12px)',
              WebkitBackdropFilter: 'blur(12px)',
            }}
          >
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '11px', color: theme.subTextColor }}>Mục tiêu</div>
              <div style={{ fontSize: '15px', fontWeight: 700, color: theme.textColor }}>
                {selectedDayProgress.targetVolumeMl} ml
              </div>
            </div>
            <div style={{ width: '1px', height: '24px', background: 'rgba(255, 255, 255, 0.1)' }} />
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '11px', color: theme.subTextColor }}>Đã ghi nhận</div>
              <div style={{ fontSize: '15px', fontWeight: 700, color: '#38BDF8' }}>
                {selectedDayProgress.totalLoggedMl} ml
              </div>
            </div>
            <div style={{ width: '1px', height: '24px', background: 'rgba(255, 255, 255, 0.1)' }} />
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '11px', color: theme.subTextColor }}>Cấp ẩm thực tế</div>
              <div style={{ fontSize: '15px', fontWeight: 700, color: '#10B981' }}>
                {selectedDayProgress.effectiveHydrationMl} ml
              </div>
            </div>
          </div>
        )}

        {/* List of Drink Entries */}
        {selectedDayProgress && selectedDayProgress.entries.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {selectedDayProgress.entries.map((entry: WaterLogEntry) => {
              const timeStr = (() => {
                try {
                  const d = new Date(entry.timestamp);
                  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                } catch {
                  return entry.timestamp;
                }
              })();

              return (
                <div
                  key={entry.id}
                  style={{
                    background: theme.entryBg,
                    border: '1px solid rgba(255, 255, 255, 0.06)',
                    borderRadius: '14px',
                    padding: '12px 16px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontSize: '18px' }}>💧</span>
                    <div>
                      <div style={{ fontSize: '13px', fontWeight: 600, color: theme.textColor }}>
                        {entry.beverageId || 'Nước lọc'}
                      </div>
                      <div style={{ fontSize: '11px', color: theme.subTextColor }}>{timeStr}</div>
                    </div>
                  </div>

                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '14px', fontWeight: 700, color: '#38BDF8' }}>
                      +{entry.effectiveVolumeMl} ml
                    </div>
                    {entry.rawVolumeMl !== entry.effectiveVolumeMl && (
                      <div style={{ fontSize: '10px', color: theme.subTextColor }}>
                        (thực tế {entry.rawVolumeMl}ml)
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div
            style={{
              padding: '24px',
              textAlign: 'center',
              color: theme.subTextColor,
              fontSize: '13px',
              background: theme.entryBg,
              borderRadius: '16px',
              border: '1px dashed rgba(255, 255, 255, 0.1)',
            }}
          >
            Chưa có dữ liệu uống nước nào cho ngày này.
          </div>
        )}
      </div>
    </div>
  );
};
