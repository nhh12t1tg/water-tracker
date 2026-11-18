import React from 'react';

export const DEFAULT_QUICK_DRINK_PRESETS = [150, 250, 500];

export interface QuickDrinkBarProps {
  presets?: number[];
  onQuickAdd?: (ml: number) => void;
  onAddWater?: (ml: number) => void;
  onOpenThemeShop?: () => void;
  onWatchRewardForTheme?: () => void;
  isDark?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

/**
 * QuickDrinkBar Component
 * One-touch hydration log pills with pulsing Gift button for Rewarded Ad theme unlocks.
 */
export const QuickDrinkBar: React.FC<QuickDrinkBarProps> = ({
  presets = DEFAULT_QUICK_DRINK_PRESETS,
  onQuickAdd,
  onAddWater,
  onOpenThemeShop,
  onWatchRewardForTheme,
  isDark = false,
  className = '',
  style = {},
}) => {
  const handleAdd = (volume: number) => {
    if (onQuickAdd) onQuickAdd(volume);
    if (onAddWater) onAddWater(volume);
  };

  const handleOpenTheme = () => {
    if (onOpenThemeShop) onOpenThemeShop();
    if (onWatchRewardForTheme) onWatchRewardForTheme();
  };

  const pillBg = isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(255, 255, 255, 0.75)';
  const pillBorder = isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(6, 182, 212, 0.25)';
  const pillTextColor = isDark ? '#38BDF8' : '#0284C7';

  return (
    <div
      className={`quick-drink-bar ${className}`}
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '10px',
        padding: '8px 16px',
        width: '100%',
        maxWidth: '480px',
        margin: '0 auto',
        boxSizing: 'border-box',
        ...style,
      }}
    >
      <style>{`
        @keyframes giftPulse {
          0%, 100% {
            transform: scale(1);
            box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35);
          }
          50% {
            transform: scale(1.06);
            box-shadow: 0 6px 18px rgba(245, 158, 11, 0.55);
          }
        }
        .quick-drink-pill {
          transition: transform 0.15s ease, background 0.15s ease;
        }
        .quick-drink-pill:hover {
          transform: translateY(-2px);
        }
        .quick-drink-pill:active {
          transform: scale(0.96);
        }
        .gift-reward-button {
          animation: giftPulse 1.8s infinite ease-in-out;
          transition: transform 0.15s ease;
        }
        .gift-reward-button:active {
          transform: scale(0.96) !important;
        }
      `}</style>

      {/* Preset Drink Volume Pills */}
      {presets.map((volume) => (
        <button
          key={volume}
          type="button"
          onClick={() => handleAdd(volume)}
          className="quick-drink-pill"
          aria-label={`Log ${volume}ml water`}
          style={{
            flex: '1 1 auto',
            height: '48px',
            borderRadius: '24px',
            background: pillBg,
            border: `1px solid ${pillBorder}`,
            color: pillTextColor,
            fontSize: '14px',
            fontWeight: 700,
            cursor: 'pointer',
            backdropFilter: 'blur(12px)',
            WebkitBackdropFilter: 'blur(12px)',
            boxShadow: isDark
              ? '0 4px 12px rgba(0, 0, 0, 0.3)'
              : '0 4px 12px rgba(6, 182, 212, 0.15)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            outline: 'none',
            userSelect: 'none',
          }}
        >
          +{volume}ml
        </button>
      ))}

      {/* Rewarded Video Theme Unlock Gift Button */}
      <button
        type="button"
        onClick={handleOpenTheme}
        className="gift-reward-button"
        aria-label="Unlock wave theme colors via rewarded video"
        title="Xem video nhận màu nước mới"
        style={{
          flex: '1 1 auto',
          height: '48px',
          borderRadius: '24px',
          background: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
          border: 'none',
          color: '#FFFFFF',
          fontSize: '12px',
          fontWeight: 700,
          cursor: 'pointer',
          padding: '0 14px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '6px',
          outline: 'none',
          userSelect: 'none',
          whiteSpace: 'nowrap',
        }}
      >
        <span style={{ fontSize: '16px', lineHeight: 1 }}>🎁</span>
        <span>Màu Nước</span>
      </button>
    </div>
  );
};
