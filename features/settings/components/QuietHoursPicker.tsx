import React from 'react';
import { calculateSleepDurationHours } from '../../../services/notification/dnd-helper.ts';

export { calculateSleepDurationHours };

export interface QuietHoursPickerProps {
  wakeUpTime: string;
  bedTime: string;
  isDndEnabled: boolean;
  onWakeUpTimeChange: (time: string) => void;
  onBedTimeChange: (time: string) => void;
}

export const QuietHoursPicker: React.FC<QuietHoursPickerProps> = ({
  wakeUpTime,
  bedTime,
  isDndEnabled,
  onWakeUpTimeChange,
  onBedTimeChange,
}) => {
  const duration = calculateSleepDurationHours(wakeUpTime, bedTime);

  return (
    <div className="quiet-hours-picker" style={{ padding: '16px', borderRadius: '12px', background: '#F8FAFC' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <h4 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#1E293B' }}>
          Khung giờ yên tĩnh (DND)
        </h4>
        <span style={{ fontSize: '13px', color: '#64748B', fontWeight: 500 }}>
          {duration} giờ ngủ
        </span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
        {/* Bedtime field */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <label htmlFor="bed-time-input" style={{ fontSize: '12px', color: '#64748B', fontWeight: 500 }}>
            Giờ đi ngủ (Bắt đầu)
          </label>
          <input
            id="bed-time-input"
            type="time"
            value={bedTime}
            disabled={!isDndEnabled}
            onChange={(e) => onBedTimeChange(e.target.value)}
            style={{
              padding: '8px 12px',
              borderRadius: '8px',
              border: '1px solid #CBD5E1',
              fontSize: '15px',
              fontWeight: 600,
              color: isDndEnabled ? '#0F172A' : '#94A3B8',
              backgroundColor: isDndEnabled ? '#FFFFFF' : '#F1F5F9',
              cursor: isDndEnabled ? 'pointer' : 'not-allowed',
            }}
          />
        </div>

        {/* Wake up field */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <label htmlFor="wake-time-input" style={{ fontSize: '12px', color: '#64748B', fontWeight: 500 }}>
            Giờ thức dậy (Kết thúc)
          </label>
          <input
            id="wake-time-input"
            type="time"
            value={wakeUpTime}
            disabled={!isDndEnabled}
            onChange={(e) => onWakeUpTimeChange(e.target.value)}
            style={{
              padding: '8px 12px',
              borderRadius: '8px',
              border: '1px solid #CBD5E1',
              fontSize: '15px',
              fontWeight: 600,
              color: isDndEnabled ? '#0F172A' : '#94A3B8',
              backgroundColor: isDndEnabled ? '#FFFFFF' : '#F1F5F9',
              cursor: isDndEnabled ? 'pointer' : 'not-allowed',
            }}
          />
        </div>
      </div>

      <p style={{ marginTop: '10px', marginBottom: 0, fontSize: '12px', color: '#64748B' }}>
        {isDndEnabled
          ? `Thông báo sẽ tắt từ ${bedTime} đến ${wakeUpTime} sáng hôm sau để không làm gián đoạn giấc ngủ.`
          : 'Chế độ không làm phiền đang tắt. Bạn có thể nhận thông báo vào ban đêm.'}
      </p>
    </div>
  );
};
