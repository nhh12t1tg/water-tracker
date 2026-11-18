/**
 * SQLite Driver abstraction for offline-first local storage.
 * Works seamlessly with Node.js built-in `node:sqlite` or an in-memory fallback.
 */

export interface ISqliteDriver {
  initialize(): Promise<void>;
  execute(sql: string, params?: unknown[]): Promise<void>;
  query<T = unknown>(sql: string, params?: unknown[]): Promise<T[]>;
  transaction<T>(fn: () => Promise<T>): Promise<T>;
  close(): Promise<void>;
}

/**
 * Concrete driver utilizing Node.js built-in `node:sqlite` (DatabaseSync)
 * with automatic fallback to an in-memory SQL mock engine if unavailable.
 */
export class SqliteDriver implements ISqliteDriver {
  private db: unknown = null;
  private isNodeSqlite: boolean = false;
  private memoryTables: Map<string, Array<Record<string, unknown>>> = new Map();
  private dbPath: string;

  constructor(dbPath: string = ':memory:') {
    this.dbPath = dbPath;
  }

  public async initialize(): Promise<void> {
    if (this.db) return;

    try {
      // Dynamic import to support node:sqlite without requiring compile-time @types/node
      const sqliteModule = 'node:sqlite';
      const sqlite = await (new Function('m', 'return import(m)')(sqliteModule) as Promise<{
        DatabaseSync?: new (path: string) => unknown;
      }>);
      if (sqlite && sqlite.DatabaseSync) {
        this.db = new sqlite.DatabaseSync(this.dbPath);
        this.isNodeSqlite = true;
      }
    } catch {
      // Fallback to in-memory store
      this.isNodeSqlite = false;
    }
  }

  public async execute(sql: string, params: unknown[] = []): Promise<void> {
    if (!this.db && !this.isNodeSqlite) {
      await this.initialize();
    }

    if (this.isNodeSqlite && this.db) {
      const db = this.db as {
        exec: (s: string) => void;
        prepare: (s: string) => { run: (...args: unknown[]) => void };
      };

      if (params.length === 0) {
        db.exec(sql);
      } else {
        const stmt = db.prepare(sql);
        stmt.run(...params);
      }
      return;
    }

    // In-memory fallback execution
    this.executeInMemory(sql, params);
  }

  public async query<T = unknown>(sql: string, params: unknown[] = []): Promise<T[]> {
    if (!this.db && !this.isNodeSqlite) {
      await this.initialize();
    }

    if (this.isNodeSqlite && this.db) {
      const db = this.db as {
        prepare: (s: string) => { all: (...args: unknown[]) => T[] };
      };
      const stmt = db.prepare(sql);
      return stmt.all(...params);
    }

    return this.queryInMemory<T>(sql, params);
  }

  public async transaction<T>(fn: () => Promise<T>): Promise<T> {
    if (this.isNodeSqlite && this.db) {
      await this.execute('BEGIN TRANSACTION');
      try {
        const result = await fn();
        await this.execute('COMMIT');
        return result;
      } catch (err) {
        await this.execute('ROLLBACK');
        throw err;
      }
    }

    return await fn();
  }

  public async close(): Promise<void> {
    if (this.isNodeSqlite && this.db) {
      const db = this.db as { close?: () => void };
      if (typeof db.close === 'function') {
        db.close();
      }
    }
    this.db = null;
    this.memoryTables.clear();
  }

  // Simple in-memory fallback for environments without native node:sqlite
  private executeInMemory(sql: string, params: unknown[]): void {
    const trimmed = sql.trim();
    if (trimmed.startsWith('CREATE TABLE')) {
      const match = trimmed.match(/CREATE TABLE\s+(?:IF NOT EXISTS\s+)?([a-zA-Z0-9_]+)/i);
      if (match && match[1]) {
        if (!this.memoryTables.has(match[1])) {
          this.memoryTables.set(match[1], []);
        }
      }
    } else if (trimmed.startsWith('INSERT')) {
      const match = trimmed.match(/INSERT (?:OR REPLACE )?INTO\s+([a-zA-Z0-9_]+)/i);
      if (match && match[1]) {
        const tableName = match[1];
        let table = this.memoryTables.get(tableName);
        if (!table) {
          table = [];
          this.memoryTables.set(tableName, table);
        }

        if (tableName === 'user_profile') {
          // Replace if exists
          const existingIdx = table.findIndex(r => r.id === params[0]);
          const record: Record<string, unknown> = {
            id: params[0],
            gender: params[1],
            weight_kg: params[2],
            activity_level: params[3],
            custom_daily_goal_ml: params[4],
            wake_up_time: params[5],
            bed_time: params[6],
            notification_interval_minutes: params[7],
            is_dnd_enabled: params[8],
            created_at: params[9],
            updated_at: params[10],
          };
          if (existingIdx >= 0) {
            table[existingIdx] = record;
          } else {
            table.push(record);
          }
        } else if (tableName === 'water_logs') {
          const record: Record<string, unknown> = {
            id: params[0],
            date_str: params[1],
            timestamp: params[2],
            beverage_id: params[3],
            raw_volume_ml: params[4],
            effective_volume_ml: params[5],
          };
          table.push(record);
        }
      }
    } else if (trimmed.startsWith('DELETE')) {
      const match = trimmed.match(/DELETE FROM\s+([a-zA-Z0-9_]+)/i);
      if (match && match[1]) {
        this.memoryTables.set(match[1], []);
      }
    }
  }

  private queryInMemory<T>(sql: string, params: unknown[]): T[] {
    const trimmed = sql.trim();
    if (trimmed.includes('FROM user_profile')) {
      const table = this.memoryTables.get('user_profile') || [];
      return [...table] as unknown as T[];
    } else if (trimmed.includes('FROM water_logs')) {
      const table = this.memoryTables.get('water_logs') || [];
      if (params.length > 0 && typeof params[0] === 'string') {
        const dateStr = params[0];
        const filtered = table.filter(r => r.date_str === dateStr);
        return [...filtered] as unknown as T[];
      }
      return [...table] as unknown as T[];
    }
    return [];
  }
}
