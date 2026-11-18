import React from 'react';
import { DailyProgress } from '../../../types/water';
import {
  DayChartData,
  mapDailyProgressToDayChart,
  calculateWeeklySummary,
} from '../utils/hydration-math';

export interface WeeklyChartProps {
  /** Array of DailyProgress objects or pre-mapped DayChartData */
  data: DailyProgress[] | DayChartData[];
  /** Callback when user taps a specific day bar */
  onSelectDay?: (date: string) => void;
  /** Currently selected date string (YYYY-MM-DD) */
  selectedDate?: string;
  /** Explicit dark mode override (defaults to true for Deep Ocean theme) */
  isDark?: boolean;
}

/**
 * 7-Day Glassmorphic Hydration Bar Chart Component
 * Visualizes daily water intake vs daily target goal across the week.
 */
export const WeeklyChart: React.FC<WeeklyChartProps> = ({
  data,
  onSelectDay,
  selectedDate,
  isDark = true,
}) => {
  // Normalize data items to DayChartData
  const chartItems: DayChartData[] = data.map((item) => {
    if ('effectiveHydrationMl' in item) {
      return mapDailyProgressToDayChart(item as DailyProgress);
    }
    return item as DayChartData;
  });

  // Calculate high-level summary
  const summary = (() => {
    if (data.length === 0) return { avg: 0, completionRate: 0 };
    if ('effectiveHydrationMl' in data[0]) {
      const s = calculateWeeklySummary(data as DailyProgress[]);
      return { avg: s.averageDailyVolumeMl, completionRate: s.completionRatePercent };
    }
    const total = chartItems.reduce((acc, curr) => acc + curr.intakeMl, 0);
    const achieved = chartItems.filter((i) => i.isGoalAchieved).length;
    return {
      avg: Math.round(total / chartItems.length),
      completionRate: Math.round((achieved / chartItems.length) * 100),
    };
  })();

  // Design Tokens
  const theme = {
    cardBg: isDark ? 'rgba(19, 30, 49, 0.75)' : 'rgba(255, 255, 255, 0.85)',
    cardBorder: isDark ? '1px solid rgba(56, 189, 248, 0.25)' : '1px solid rgba(255, 255, 255, 0.5)',
    textColor: isDark ? '#F0F9FF' : '#0F172A',
    subTextColor: isDark ? '#94A3B8' : '#64748B',
    barBg: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.05)',
    barNormalGradient: 'linear-gradient(180deg, #22D3EE 0%, #0284C7 100%)',
    barAchievedGradient: 'linear-gradient(180deg, #34D399 0%, #059669 100%)',
    todayGlow: isDark ? 'rgba(34, 211, 238, 0.4)' : 'rgba(6, 182, 212, 0.3)',
    activeBorder: '#06B6D4',
  };

  return (
    <div
      style={{
        background: theme.cardBg,
        border: theme.cardBorder,
        borderRadius: '24px',
        padding: '20px',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        boxShadow: isDark
          ? '0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 8px 10px -6px rgba(0, 0, 0, 0.3)'
          : '0 10px 25px -5px rgba(2, 132, 199, 0.1), 0 8px 10px -6px rgba(2, 132, 199, 0.05)',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        fontFamily: 'system-ui, -apple-system, sans-serif',
      }}
    >
      {/* Header & Weekly Stats */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
        }}
      >
        <div>
          <h3
            style={{
              margin: '0 0 4px 0',
              fontSize: '18px',
              fontWeight: 700,
              color: theme.textColor,
            }}
          >
            Biểu Đồ 7 Ngày Qua
          </h3>
          <span style={{ fontSize: '13px', color: theme.subTextColor }}>
            Mục tiêu: Đạt chuẩn 100% mỗi ngày
          </span>
        </div>

        <div
          style={{
            display: 'flex',
            gap: '12px',
            textAlign: 'right',
          }}
        >
          <div>
            <div style={{ fontSize: '11px', color: theme.subTextColor }}>Trung bình</div>
            <div style={{ fontSize: '14px', fontWeight: 700, color: '#38BDF8' }}>
              {summary.avg} ml
            </div>
          </div>
          <div>
            <div style={{ fontSize: '11px', color: theme.subTextColor }}>Đạt chuẩn</div>
            <div style={{ fontSize: '14px', fontWeight: 700, color: '#10B981' }}>
              {summary.completionRate}%
            </div>
          </div>
        </div>
      </div>

      {/* Bar Chart Visualization */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-end',
          height: '180px',
          paddingTop: '16px',
          paddingBottom: '8px',
          position: 'relative',
        }}
      >
        {/* 100% Target Goal Reference Line */}
        <div
          style={{
            position: 'absolute',
            top: '28px',
            left: 0,
            right: 0,
            borderTop: '1px dashed rgba(56, 189, 248, 0.35)',
            zIndex: 1,
            pointerEvents: 'none',
          }}
        >
          <span
            style={{
              position: 'absolute',
              right: '2px',
              top: '-16px',
              fontSize: '10px',
              color: isDark ? 'rgba(56, 189, 248, 0.8)' : '#0284C7',
              fontWeight: 600,
            }}
          >
            Mục tiêu 100%
          </span>
        </div>

        {/* 7 Daily Bars */}
        {chartItems.map((day) => {
          const isSelected = selectedDate === day.date;
          // Fractional height clamped between 8% and 100%
          const ratio = day.targetMl > 0 ? day.intakeMl / day.targetMl : 0;
          const heightPercent = Math.min(100, Math.max(8, Math.round(ratio * 100)));
          const isAchieved = day.isGoalAchieved || ratio >= 1.0;

          return (
            <div
              key={day.date}
              onClick={() => onSelectDay?.(day.date)}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                flex: 1,
                cursor: 'pointer',
                zIndex: 2,
                padding: '4px 2px',
                borderRadius: '12px',
                background: isSelected ? 'rgba(56, 189, 248, 0.12)' : 'transparent',
                outline: isSelected ? `1.5px solid ${theme.activeBorder}` : 'none',
                transition: 'all 0.2s ease',
              }}
            >
              {/* Daily Volume Tooltip Label */}
              <span
                style={{
                  fontSize: '9px',
                  fontWeight: 600,
                  color: isAchieved ? '#34D399' : theme.subTextColor,
                  marginBottom: '6px',
                }}
              >
                {day.intakeMl >= 1000
                  ? `${(day.intakeMl / 1000).toFixed(1)}L`
                  : `${day.intakeMl}m`}
              </span>

              {/* Bar Track & Fill */}
              <div
                style={{
                  width: '18px',
                  height: '110px',
                  backgroundColor: theme.barBg,
                  borderRadius: '10px',
                  display: 'flex',
                  alignItems: 'flex-end',
                  overflow: 'hidden',
                  position: 'relative',
                }}
              >
                <div
                  style={{
                    width: '100%',
                    height: `${heightPercent}%`,
                    background: isAchieved
                      ? theme.barAchievedGradient
                      : theme.barNormalGradient,
                    borderRadius: '10px',
                    transition: 'height 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
                    boxShadow: day.isToday
                      ? `0 0 12px ${theme.todayGlow}`
                      : 'none',
                  }}
                />
              </div>

              {/* Day Name Label */}
              <span
                style={{
                  marginTop: '8px',
                  fontSize: '12px',
                  fontWeight: day.isToday || isSelected ? 700 : 500,
                  color: day.isToday
                    ? '#38BDF8'
                    : isSelected
                    ? theme.textColor
                    : theme.subTextColor,
                }}
              >
                {day.dayName}
              </span>

              {/* Today Dot Indicator */}
              {day.isToday && (
                <div
                  style={{
                    width: '4px',
                    height: '4px',
                    borderRadius: '50%',
                    backgroundColor: '#38BDF8',
                    marginTop: '2px',
                  }}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
