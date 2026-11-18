import React, { useState } from 'react';
import type { UserProfile } from '../../../types/water.ts';
import { QuietHoursPicker } from '../components/QuietHoursPicker.tsx';

export interface SettingsScreenProps {
  profile: UserProfile;
  onUpdateProfile: (updated: UserProfile) => void;
  soundEnabled?: boolean;
  onSoundToggle?: (enabled: boolean) => void;
  onBack?: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  profile,
  onUpdateProfile,
  soundEnabled = true,
  onSoundToggle,
  onBack,
}) => {
  const [wakeUpTime, setWakeUpTime] = useState<string>(profile.wakeUpTime || '07:00');
  const [bedTime, setBedTime] = useState<string>(profile.bedTime || '22:00');
  const [intervalMinutes, setIntervalMinutes] = useState<number>(profile.notificationIntervalMinutes || 90);
  const [isDndEnabled, setIsDndEnabled] = useState<boolean>(profile.isDndEnabled ?? true);
  const [isSoundOn, setIsSoundOn] = useState<boolean>(soundEnabled);
  const [savedSuccess, setSavedSuccess] = useState<boolean>(false);

  const handleFrequencySelect = (mins: number) => {
    setIntervalMinutes(mins);
  };

  const handleSoundToggleChange = () => {
    const nextVal = !isSoundOn;
    setIsSoundOn(nextVal);
    if (onSoundToggle) {
      onSoundToggle(nextVal);
    }
  };

  const handleSave = () => {
    const updated: UserProfile = {
      ...profile,
      wakeUpTime,
      bedTime,
      notificationIntervalMinutes: intervalMinutes,
      isDndEnabled,
      updatedAt: new Date().toISOString(),
    };

    onUpdateProfile(updated);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 2500);
  };

  return (
    <div
      className="settings-screen"
      style={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100%',
        backgroundColor: '#FFFFFF',
        color: '#0F172A',
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
        padding: '20px',
        boxSizing: 'border-box',
      }}
    >
      {/* Header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          paddingBottom: '16px',
          borderBottom: '1px solid #E2E8F0',
          marginBottom: '20px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {onBack && (
            <button
              type="button"
              onClick={onBack}
              aria-label="Back"
              style={{
                background: 'none',
                border: 'none',
                fontSize: '20px',
                cursor: 'pointer',
                color: '#0284C7',
                padding: '4px',
              }}
            >
              ←
            </button>
          )}
          <h2 style={{ margin: 0, fontSize: '20px', fontWeight: 700, color: '#0F172A' }}>
            Cài đặt nhắc nhở & Giấc ngủ
          </h2>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', flex: 1 }}>
        {/* Section 1: Reminder Frequency */}
        <section>
          <div style={{ marginBottom: '8px' }}>
            <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 600, color: '#334155' }}>
              Tần suất nhắc nhở
            </h3>
            <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#64748B' }}>
              Khoảng thời gian giữa các lần gửi thông báo nhắc uống nước.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
            {[90, 120].map((mins) => {
              const isSelected = intervalMinutes === mins;
              return (
                <button
                  key={mins}
                  type="button"
                  onClick={() => handleFrequencySelect(mins)}
                  style={{
                    flex: 1,
                    padding: '12px 16px',
                    borderRadius: '10px',
                    border: isSelected ? '2px solid #0284C7' : '1px solid #E2E8F0',
                    backgroundColor: isSelected ? '#E0F2FE' : '#FFFFFF',
                    color: isSelected ? '#0369A1' : '#334155',
                    fontWeight: 600,
                    fontSize: '15px',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease-in-out',
                  }}
                >
                  {mins === 90 ? '90 phút (Khuyên dùng)' : '120 phút (2 giờ)'}
                </button>
              );
            })}
          </div>
        </section>

        {/* Section 2: Do Not Disturb (DND) */}
        <section>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '12px',
            }}
          >
            <div>
              <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 600, color: '#334155' }}>
                Không làm phiền khi ngủ (DND)
              </h3>
              <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#64748B' }}>
                Tắt toàn bộ thông báo trong khoảng thời gian đi ngủ.
              </p>
            </div>
            <label
              style={{
                position: 'relative',
                display: 'inline-block',
                width: '48px',
                height: '26px',
                cursor: 'pointer',
              }}
            >
              <input
                type="checkbox"
                checked={isDndEnabled}
                onChange={(e) => setIsDndEnabled(e.target.checked)}
                style={{ opacity: 0, width: 0, height: 0 }}
              />
              <span
                style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  backgroundColor: isDndEnabled ? '#0284C7' : '#CBD5E1',
                  borderRadius: '26px',
                  transition: '0.2s',
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    content: '""',
                    height: '20px',
                    width: '20px',
                    left: isDndEnabled ? '24px' : '3px',
                    bottom: '3px',
                    backgroundColor: 'white',
                    borderRadius: '50%',
                    transition: '0.2s',
                  }}
                />
              </span>
            </label>
          </div>

          <QuietHoursPicker
            wakeUpTime={wakeUpTime}
            bedTime={bedTime}
            isDndEnabled={isDndEnabled}
            onWakeUpTimeChange={setWakeUpTime}
            onBedTimeChange={setBedTime}
          />
        </section>

        {/* Section 3: Sound & Vibration */}
        <section>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '14px 16px',
              borderRadius: '12px',
              backgroundColor: '#F8FAFC',
            }}
          >
            <div>
              <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 600, color: '#1E293B' }}>
                Âm thanh thông báo
              </h4>
              <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#64748B' }}>
                Phát âm thanh giọt nước khi đến giờ nhắc nhở
              </p>
            </div>
            <label
              style={{
                position: 'relative',
                display: 'inline-block',
                width: '44px',
                height: '24px',
                cursor: 'pointer',
              }}
            >
              <input
                type="checkbox"
                checked={isSoundOn}
                onChange={handleSoundToggleChange}
                style={{ opacity: 0, width: 0, height: 0 }}
              />
              <span
                style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  backgroundColor: isSoundOn ? '#0284C7' : '#CBD5E1',
                  borderRadius: '24px',
                  transition: '0.2s',
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    content: '""',
                    height: '18px',
                    width: '18px',
                    left: isSoundOn ? '22px' : '3px',
                    bottom: '3px',
                    backgroundColor: 'white',
                    borderRadius: '50%',
                    transition: '0.2s',
                  }}
                />
              </span>
            </label>
          </div>
        </section>
      </div>

      {/* Save Button & Feedback */}
      <div style={{ marginTop: '32px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {savedSuccess && (
          <div
            style={{
              padding: '10px 14px',
              borderRadius: '8px',
              backgroundColor: '#DCFCE7',
              color: '#166534',
              fontSize: '14px',
              fontWeight: 500,
              textAlign: 'center',
            }}
          >
            ✓ Đã lưu cài đặt nhắc nhở thành công!
          </div>
        )}

        <button
          type="button"
          onClick={handleSave}
          style={{
            width: '100%',
            padding: '14px',
            borderRadius: '12px',
            border: 'none',
            backgroundColor: '#0284C7',
            color: '#FFFFFF',
            fontSize: '16px',
            fontWeight: 700,
            cursor: 'pointer',
            boxShadow: '0 4px 6px -1px rgba(2, 132, 199, 0.2)',
          }}
        >
          Lưu cài đặt
        </button>
      </div>
    </div>
  );
};
