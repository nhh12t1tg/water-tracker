import { isBuiltin, createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';
import fs from 'node:fs/promises';

const require = createRequire(import.meta.url);
const ts = require('typescript');

export async function resolve(specifier, context, nextResolve) {
  if (isBuiltin(specifier)) {
    return nextResolve(specifier, context);
  }

  try {
    return await nextResolve(specifier, context);
  } catch (err) {
    if (specifier.startsWith('.') || specifier.startsWith('/')) {
      for (const ext of ['.ts', '.tsx', '.js', '/index.ts', '/index.js']) {
        try {
          return await nextResolve(specifier + ext, context);
        } catch {}
      }
    }
    throw err;
  }
}

export async function load(url, context, nextLoad) {
  if (url.endsWith('.tsx')) {
    const filePath = fileURLToPath(url);
    const rawContent = await fs.readFile(filePath, 'utf-8');
    const transpiled = ts.transpileModule(rawContent, {
      compilerOptions: {
        module: ts.ModuleKind.ESNext,
        target: ts.ScriptTarget.ESNext,
        jsx: ts.JsxEmit.ReactJSX,
      },
    });
    return {
      format: 'module',
      source: transpiled.outputText,
      shortCircuit: true,
    };
  }

  const result = await nextLoad(url, context);

  if (url.endsWith('.ts')) {
    let source = '';
    if (result.source) {
      if (typeof result.source === 'string') {
        source = result.source;
      } else if (Buffer.isBuffer(result.source) || result.source instanceof Uint8Array || result.source.buffer) {
        source = Buffer.from(result.source).toString('utf-8');
      }
    }

    if (url.includes('types/water.ts')) {
      const typeMatches = [...source.matchAll(/export\s+(?:type|interface)\s+([A-Za-z0-9_]+)/g)];
      const dummyExports = typeMatches.map(m => `export const ${m[1]} = undefined;`);
      return {
        format: 'module',
        source: dummyExports.join('\n') + '\n',
        shortCircuit: true,
      };
    }
  }

  return result;
}
