import React, { useEffect, useRef } from 'react';
import type { DailyProgress, StreakState } from '../../../types/water';
import { WaveProgress } from '../components/WaveProgress';
import { QuickDrinkBar } from '../components/QuickDrinkBar';

export const ADMOB_BANNER_RESERVED_HEIGHT_DP = 60;
export const ADMOB_BANNER_TOUCH_PADDING_DP = 16;

/**
 * Returns encouragement message based on hydration progress
 */
export function getMotivationSubtitle(currentMl: number, targetMl: number): string {
  if (targetMl > 0 && currentMl >= targetMl) {
    return 'Tuyệt vời! Bạn đã đủ nước hôm nay';
  }
  return 'Hãy uống từng ngụm nhỏ để cơ thể hấp thụ tốt nhất';
}

export interface TrackerScreenProps {
  progress: DailyProgress;
  streakState?: StreakState;
  currentStreakDays?: number;
  onAddDrink: (ml: number) => void;
  onOpenThemeReward: () => void;
  onOpenHistory?: () => void;
  onOpenSettings?: () => void;
  onGoalReached?: () => void;
  activeWaveColor?: string;
  isDark?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

/**
 * TrackerScreen Component
 * Main dashboard screen coordinating:
 * - Top bar with streak flame counter and navigation
 * - Center dual-sine wave progress visualizer
 * - Dynamic motivation subtitle
 * - Quick-log drink pills & rewarded video theme unlock button
 * - 60dp reserved safe slot for AdMob Adaptive Banner with >=16dp accidental click buffer
 */
export const TrackerScreen: React.FC<TrackerScreenProps> = ({
  progress,
  streakState,
  currentStreakDays,
  onAddDrink,
  onOpenThemeReward,
  onOpenHistory,
  onOpenSettings,
  onGoalReached,
  activeWaveColor = '#06B6D4',
  isDark = false,
  className = '',
  style = {},
}) => {
  const currentMl = progress.effectiveHydrationMl ?? progress.totalLoggedMl ?? 0;
  const targetMl = progress.targetVolumeMl;
  const streak = currentStreakDays ?? streakState?.currentStreakDays ?? 0;

  // Track goal reached callback to fire once per transition
  const hasTriggeredGoal = useRef(false);
  useEffect(() => {
    if (targetMl > 0 && currentMl >= targetMl && !hasTriggeredGoal.current) {
      hasTriggeredGoal.current = true;
      if (onGoalReached) {
        onGoalReached();
      }
    } else if (currentMl < targetMl) {
      hasTriggeredGoal.current = false;
    }
  }, [currentMl, targetMl, onGoalReached]);

  const bgGradient = isDark
    ? 'linear-gradient(180deg, #0F172A 0%, #090D16 100%)'
    : 'linear-gradient(180deg, #F0F9FF 0%, #E0F2FE 50%, #BAE6FD 100%)';

  const streakPillBg = isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(255, 255, 255, 0.7)';
  const textColor = isDark ? '#F8FAFC' : '#0F172A';
  const subtitleColor = isDark ? '#94A3B8' : '#475569';

  return (
    <div
      className={`tracker-screen ${className}`}
      style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        alignItems: 'center',
        minHeight: '100vh',
        width: '100%',
        maxWidth: '520px',
        margin: '0 auto',
        padding: '16px 20px',
        boxSizing: 'border-box',
        background: bgGradient,
        fontFamily: 'system-ui, -apple-system, sans-serif',
        ...style,
      }}
    >
      {/* 1. TOP BAR: Streak Flame Badge & Navigation Icons */}
      <header
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          width: '100%',
          paddingTop: '8px',
        }}
      >
        {/* Streak Flame Pill */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            background: streakPillBg,
            backdropFilter: 'blur(10px)',
            WebkitBackdropFilter: 'blur(10px)',
            border: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(255,255,255,0.6)',
            borderRadius: '20px',
            padding: '6px 14px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          }}
        >
          <span style={{ fontSize: '18px', lineHeight: 1 }} role="img" aria-label="streak flame">
            🔥
          </span>
          <span style={{ fontSize: '14px', fontWeight: 700, color: textColor }}>
            {streak} ngày
          </span>
        </div>

        {/* Action Buttons: History & Settings */}
        <div style={{ display: 'flex', gap: '8px' }}>
          {onOpenHistory && (
            <button
              type="button"
              onClick={onOpenHistory}
              aria-label="Lịch sử uống nước"
              title="Lịch sử"
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                border: 'none',
                background: streakPillBg,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '18px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
              }}
            >
              📊
            </button>
          )}
          {onOpenSettings && (
            <button
              type="button"
              onClick={onOpenSettings}
              aria-label="Cài đặt"
              title="Cài đặt"
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                border: 'none',
                background: streakPillBg,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '18px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
              }}
            >
              ⚙️
            </button>
          )}
        </div>
      </header>

      {/* 2. CENTER SECTION: Liquid Wave Circle & Motivation */}
      <main
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          flex: '1 1 auto',
          padding: '24px 0',
          width: '100%',
        }}
      >
        <WaveProgress
          currentMl={currentMl}
          targetMl={targetMl}
          progressPercentage={progress.percentage}
          waveColor={activeWaveColor}
          isDark={isDark}
          size={270}
        />

        <p
          style={{
            marginTop: '20px',
            fontSize: '13px',
            fontWeight: 500,
            color: subtitleColor,
            textAlign: 'center',
            maxWidth: '300px',
            lineHeight: 1.5,
          }}
        >
          {getMotivationSubtitle(currentMl, targetMl)}
        </p>
      </main>

      {/* 3. CONTROLS: Quick Drink Presets & Gift Button */}
      <section style={{ width: '100%', marginBottom: '12px' }}>
        <QuickDrinkBar
          onQuickAdd={onAddDrink}
          onOpenThemeShop={onOpenThemeReward}
          isDark={isDark}
        />
      </section>

      {/* 4. BOTTOM ADMOB ADAPTIVE BANNER RESERVED SLOT */}
      {/* Google Better Ads policy requires 60dp height reservation with >=16dp touch padding */}
      <footer
        style={{
          width: '100%',
          paddingTop: `${ADMOB_BANNER_TOUCH_PADDING_DP}px`,
          paddingBottom: '4px',
          boxSizing: 'border-box',
        }}
      >
        <div
          data-testid="admob-banner-slot"
          style={{
            width: '100%',
            height: `${ADMOB_BANNER_RESERVED_HEIGHT_DP}px`,
            minHeight: `${ADMOB_BANNER_RESERVED_HEIGHT_DP}px`,
            borderRadius: '12px',
            background: isDark ? 'rgba(30, 41, 59, 0.5)' : 'rgba(255, 255, 255, 0.4)',
            border: isDark ? '1px dashed rgba(255,255,255,0.1)' : '1px dashed rgba(6,182,212,0.2)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: subtitleColor,
            fontSize: '11px',
            letterSpacing: '0.5px',
            textTransform: 'uppercase',
            userSelect: 'none',
          }}
        >
          AdMob Adaptive Banner (60dp)
        </div>
      </footer>
    </div>
  );
};
