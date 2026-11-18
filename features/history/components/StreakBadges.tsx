import React from 'react';
import { StreakState } from '../../../types/water';
import { StreakMilestone, getStreakMilestones } from '../utils/hydration-math';

export interface StreakBadgesProps {
  streakState: StreakState;
  onWatchRewardForStreakFreeze?: () => void;
  isDark?: boolean;
}

/**
 * StreakBadges Component
 * Displays streak milestone achievements (3, 7, 14, 30 days) and
 * the "Bảo vệ chuỗi (+1 Khiên)" Rewarded Video trigger.
 */
export const StreakBadges: React.FC<StreakBadgesProps> = ({
  streakState,
  onWatchRewardForStreakFreeze,
  isDark = true,
}) => {
  const currentStreak = streakState.currentStreakDays || 0;
  const longestStreak = streakState.longestStreakDays || 0;
  const availableFreezes = streakState.availableFreezes || 0;

  const milestones = getStreakMilestones(currentStreak);

  const theme = {
    cardBg: isDark ? 'rgba(19, 30, 49, 0.75)' : 'rgba(255, 255, 255, 0.85)',
    cardBorder: isDark ? '1px solid rgba(56, 189, 248, 0.25)' : '1px solid rgba(255, 255, 255, 0.5)',
    textColor: isDark ? '#F0F9FF' : '#0F172A',
    subTextColor: isDark ? '#94A3B8' : '#64748B',
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        fontFamily: 'system-ui, -apple-system, sans-serif',
      }}
    >
      {/* 1. REWARDED VIDEO STREAK FREEZE PROMO CARD */}
      <div
        style={{
          background: 'linear-gradient(135deg, #0369A1 0%, #0284C7 50%, #0EA5E9 100%)',
          borderRadius: '20px',
          padding: '16px 20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 8px 20px -4px rgba(2, 132, 199, 0.4)',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          color: '#FFFFFF',
        }}
      >
        <div style={{ flex: 1, paddingRight: '12px' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              marginBottom: '4px',
            }}
          >
            <span style={{ fontSize: '16px' }}>🛡️</span>
            <span style={{ fontWeight: 700, fontSize: '15px' }}>
              Bảo Vệ Chuỗi Streak ({availableFreezes} sẵn có)
            </span>
          </div>
          <div style={{ fontSize: '12px', opacity: 0.9, lineHeight: 1.4 }}>
            Xem 1 video nhận 1 Khiên Đóng Băng khi lỡ quên uống nước trong ngày.
          </div>
        </div>

        <button
          onClick={onWatchRewardForStreakFreeze}
          style={{
            background: 'linear-gradient(180deg, #FBBF24 0%, #F59E0B 100%)',
            color: '#78350F',
            border: 'none',
            borderRadius: '24px',
            padding: '10px 18px',
            fontWeight: 700,
            fontSize: '13px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            boxShadow: '0 4px 12px rgba(245, 158, 11, 0.4)',
            whiteSpace: 'nowrap',
            transition: 'transform 0.1s ease',
          }}
          onMouseDown={(e) => (e.currentTarget.style.transform = 'scale(0.96)')}
          onMouseUp={(e) => (e.currentTarget.style.transform = 'scale(1)')}
        >
          <span>▶️</span>
          <span>+1 Khiên</span>
        </button>
      </div>

      {/* 2. STREAK MILESTONES HEADER & SUMMARY */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div>
          <h3
            style={{
              margin: '0 0 2px 0',
              fontSize: '17px',
              fontWeight: 700,
              color: theme.textColor,
            }}
          >
            Huy Hiệu Chuỗi ({currentStreak} Ngày)
          </h3>
          <span style={{ fontSize: '12px', color: theme.subTextColor }}>
            Kỷ lục cao nhất: {longestStreak} ngày liên tiếp
          </span>
        </div>
      </div>

      {/* 3. HORIZONTAL MILESTONE BADGES */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '10px',
        }}
      >
        {milestones.map((badge: StreakMilestone) => {
          return (
            <div
              key={badge.targetDays}
              style={{
                background: theme.cardBg,
                border: badge.isUnlocked
                  ? '1.5px solid rgba(245, 158, 11, 0.6)'
                  : theme.cardBorder,
                borderRadius: '18px',
                padding: '12px 6px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                textAlign: 'center',
                gap: '8px',
                backdropFilter: 'blur(12px)',
                WebkitBackdropFilter: 'blur(12px)',
                boxShadow: badge.isUnlocked
                  ? '0 6px 16px -2px rgba(245, 158, 11, 0.25)'
                  : 'none',
                opacity: badge.isUnlocked ? 1.0 : 0.65,
                transition: 'all 0.2s ease',
              }}
            >
              {/* Badge Icon Emblem */}
              <div
                style={{
                  width: '44px',
                  height: '44px',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: badge.isUnlocked
                    ? 'radial-gradient(circle, #FF7043 0%, #EA580C 100%)'
                    : isDark
                    ? 'linear-gradient(180deg, #334155 0%, #1E293B 100%)'
                    : 'linear-gradient(180deg, #E2E8F0 0%, #CBD5E1 100%)',
                  boxShadow: badge.isUnlocked
                    ? '0 4px 10px rgba(234, 88, 12, 0.4)'
                    : 'none',
                  fontSize: '20px',
                }}
              >
                {badge.isUnlocked ? '🔥' : '🔒'}
              </div>

              {/* Title & Target */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 700,
                    color: badge.isUnlocked ? theme.textColor : theme.subTextColor,
                    lineHeight: 1.2,
                  }}
                >
                  {badge.title}
                </span>
                <span
                  style={{
                    fontSize: '10px',
                    fontWeight: 500,
                    color: badge.isUnlocked ? '#F59E0B' : theme.subTextColor,
                  }}
                >
                  {badge.targetDays} ngày
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
