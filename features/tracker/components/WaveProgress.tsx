import React, { useEffect, useRef, useState } from 'react';

export interface WaveProgressProps {
  currentMl: number;
  targetMl: number;
  progressPercentage?: number;
  waveColor?: string;
  secondaryWaveColor?: string;
  size?: number;
  isDark?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

/**
 * Pure calculation helper: Hydration progress ratio and percentage
 */
export function calculateHydrationProgress(
  currentMl: number,
  targetMl: number
): { rawRatio: number; clampedProgress: number; percentage: number } {
  if (!Number.isFinite(targetMl) || targetMl <= 0) {
    return { rawRatio: 0, clampedProgress: 0, percentage: 0 };
  }
  const rawRatio = Math.max(0, currentMl) / targetMl;
  const clampedProgress = Math.min(Math.max(rawRatio, 0), 1.25);
  const percentage = Math.round(rawRatio * 100);
  return { rawRatio, clampedProgress, percentage };
}

/**
 * Pure calculation helper: Sine wave point at x coordinate following dual-sine wave formula
 * y = waterLevelY + amplitude * sin(angle + phase)
 */
export function calculateSineWavePoint(
  x: number,
  width: number,
  waterLevelY: number,
  amplitude: number,
  phase: number
): number {
  if (width <= 0) return waterLevelY;
  const angle = (x / width) * 2 * Math.PI;
  return waterLevelY + amplitude * Math.sin(angle + phase);
}

/**
 * Pure calculation helper: Spring physics step for smooth fluid level transitions
 * Simulates a damped harmonic oscillator:
 * F = -k * (x - target) - c * v
 */
export function calculateSpringPhysicsStep(
  current: number,
  target: number,
  velocity: number,
  stiffness: number = 0.08,
  damping: number = 0.82
): { position: number; velocity: number } {
  const displacement = target - current;
  const acceleration = displacement * stiffness;
  const nextVelocity = (velocity + acceleration) * damping;
  const nextPosition = current + nextVelocity;
  return { position: nextPosition, velocity: nextVelocity };
}

/**
 * WaveProgress Component
 * Renders an interactive circular glassmorphic hydration visualizer with dual-layer sine waves
 * and spring-based level transitions.
 */
export const WaveProgress: React.FC<WaveProgressProps> = ({
  currentMl,
  targetMl,
  progressPercentage,
  waveColor = '#06B6D4', // Cyan primary
  secondaryWaveColor = '#22D3EE', // Cyan light
  size = 270,
  isDark = false,
  className = '',
  style = {},
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const { clampedProgress, percentage: computedPercentage } = calculateHydrationProgress(
    currentMl,
    targetMl
  );
  const displayPercentage = progressPercentage !== undefined ? progressPercentage : computedPercentage;

  // Spring animation state for water level
  const [animatedLevel, setAnimatedLevel] = useState<number>(clampedProgress);
  const springRef = useRef<{ position: number; velocity: number }>({
    position: clampedProgress,
    velocity: 0,
  });

  // Keep target updated
  useEffect(() => {
    springRef.current.position = animatedLevel;
  }, []);

  // Wave phase animation loop
  useEffect(() => {
    let animationFrameId: number;
    let phaseFront = 0;
    let phaseBack = Math.PI / 3;

    const render = () => {
      // 1. Advance spring physics towards target level
      const targetRatio = clampedProgress;
      const springResult = calculateSpringPhysicsStep(
        springRef.current.position,
        targetRatio,
        springRef.current.velocity,
        0.06,
        0.84
      );
      springRef.current = springResult;
      setAnimatedLevel(springResult.position);

      // 2. Render dual-sine waves on Canvas if available
      const canvas = canvasRef.current;
      if (canvas) {
        const ctx = canvas.getContext('2d');
        if (ctx) {
          const w = canvas.width;
          const h = canvas.height;
          ctx.clearRect(0, 0, w, h);

          // Save context for circular clipping
          ctx.save();
          ctx.beginPath();
          ctx.arc(w / 2, h / 2, w / 2 - 2, 0, Math.PI * 2);
          ctx.clip();

          // Water level Y (inverted: 0 is top, h is bottom)
          const levelRatio = Math.min(Math.max(springResult.position, 0), 1.0);
          const waterLevelY = h * (1 - levelRatio);

          // Amplitude dampens at extreme edges
          const amplitudeDampener = 1 - Math.abs(levelRatio - 0.5) * 0.5;
          const amplitude = Math.max(4, 14 * amplitudeDampener);

          // Phase advancement (front faster than back)
          phaseFront = (phaseFront + 0.045) % (Math.PI * 2);
          phaseBack = (phaseBack + 0.028) % (Math.PI * 2);

          // --- Draw Back Wave (Secondary, translucent) ---
          ctx.fillStyle = secondaryWaveColor;
          ctx.globalAlpha = 0.45;
          ctx.beginPath();
          ctx.moveTo(0, h);
          ctx.lineTo(0, waterLevelY);
          for (let x = 0; x <= w; x += 4) {
            const y = calculateSineWavePoint(x, w, waterLevelY, amplitude * 0.75, phaseBack);
            ctx.lineTo(x, y);
          }
          ctx.lineTo(w, h);
          ctx.closePath();
          ctx.fill();

          // --- Draw Front Wave (Primary, vibrant) ---
          ctx.fillStyle = waveColor;
          ctx.globalAlpha = 0.85;
          ctx.beginPath();
          ctx.moveTo(0, h);
          ctx.lineTo(0, waterLevelY);
          for (let x = 0; x <= w; x += 4) {
            const y = calculateSineWavePoint(x, w, waterLevelY, amplitude, phaseFront);
            ctx.lineTo(x, y);
          }
          ctx.lineTo(w, h);
          ctx.closePath();
          ctx.fill();

          ctx.restore();
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    animationFrameId = requestAnimationFrame(render);
    return () => cancelAnimationFrame(animationFrameId);
  }, [clampedProgress, waveColor, secondaryWaveColor]);

  // Glassmorphism theme styling
  const glassBg = isDark ? 'rgba(30, 41, 59, 0.65)' : 'rgba(255, 255, 255, 0.7)';
  const glassBorder = isDark
    ? 'linear-gradient(135deg, rgba(255,255,255,0.25) 0%, rgba(6,182,212,0.3) 100%)'
    : 'linear-gradient(135deg, rgba(255,255,255,0.8) 0%, rgba(6,182,212,0.4) 100%)';
  const textColor = animatedLevel > 0.55 ? '#FFFFFF' : isDark ? '#F8FAFC' : '#0F172A';
  const subtitleColor = animatedLevel > 0.55 ? 'rgba(255,255,255,0.9)' : isDark ? '#94A3B8' : '#64748B';

  return (
    <div
      className={`wave-progress-container ${className}`}
      style={{
        position: 'relative',
        width: size,
        height: size,
        borderRadius: '50%',
        background: glassBg,
        boxShadow: isDark
          ? '0 20px 40px rgba(0, 0, 0, 0.4), inset 0 2px 4px rgba(255, 255, 255, 0.15)'
          : '0 20px 40px rgba(6, 182, 212, 0.25), inset 0 2px 4px rgba(255, 255, 255, 0.6)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        padding: '3px',
        backgroundClip: 'padding-box',
        border: '2px solid transparent',
        backgroundImage: `${glassBorder}`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
        ...style,
      }}
    >
      {/* Canvas for Wave Animation */}
      <canvas
        ref={canvasRef}
        width={size}
        height={size}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          borderRadius: '50%',
          pointerEvents: 'none',
        }}
      />

      {/* Central Hydration Status Overlay */}
      <div
        style={{
          position: 'relative',
          zIndex: 10,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          userSelect: 'none',
        }}
      >
        <span
          style={{
            fontSize: `${Math.round(size * 0.18)}px`,
            fontWeight: 800,
            lineHeight: 1,
            color: textColor,
            textShadow: '0 2px 10px rgba(0,0,0,0.25)',
            fontFamily: 'system-ui, -apple-system, sans-serif',
          }}
        >
          {displayPercentage}%
        </span>
        <span
          style={{
            marginTop: '6px',
            fontSize: `${Math.round(size * 0.055)}px`,
            fontWeight: 600,
            color: subtitleColor,
            fontFamily: 'system-ui, -apple-system, sans-serif',
          }}
        >
          {currentMl} / {targetMl} ml
        </span>
      </div>
    </div>
  );
};
