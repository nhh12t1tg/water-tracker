#!/usr/bin/env node
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const registerLoaderPath = path.join(__dirname, 'register-loader.js');

const testFiles = [
  path.join(__dirname, 'unit', 'hydration-core.test.ts'),
  path.join(__dirname, 'unit', 'admob-policy.test.ts'),
  path.join(__dirname, 'unit', 'notification-dnd.test.ts'),
  path.join(__dirname, 'integration', 'end-to-end-flow.test.ts'),
];

console.log('🚀 Running Water Tracker Test Suite across all modules...\n');

const child = spawn(
  process.execPath,
  [
    '--experimental-strip-types',
    '--import',
    registerLoaderPath,
    '--test',
    ...testFiles,
  ],
  {
    stdio: 'inherit',
    cwd: path.resolve(__dirname, '..'),
  }
);

child.on('exit', (code) => {
  if (code === 0) {
    console.log('\n✅ 100% All tests passed successfully!');
  } else {
    console.error(`\n❌ Tests failed with exit code ${code}`);
  }
  process.exit(code || 0);
});
