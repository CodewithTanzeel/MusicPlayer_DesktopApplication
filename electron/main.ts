import { app, BrowserWindow, ipcMain, dialog } from 'electron';
import path from 'path';
import Database from 'better-sqlite3';
import { v4 as uuidv4 } from 'uuid';

// Handle creating/removing shortcuts on Windows when installing/uninstalling.
if (require('electron-squirrel-startup')) {
  app.quit();
}

let db: Database.Database;

function setupDatabase() {
  const userDataPath = app.getPath('userData');
  const dbPath = path.join(userDataPath, 'vibe_music.db');
  console.log('Database path:', dbPath);
  
  db = new Database(dbPath); // verbose: console.log

  // FR-1: User Authentication tables
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      username TEXT UNIQUE,
      password_hash TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );
  `);

  // FR-7, FR-8: Library tables
  db.exec(`
    CREATE TABLE IF NOT EXISTS tracks (
      id TEXT PRIMARY KEY,
      filepath TEXT UNIQUE,
      title TEXT,
      artist TEXT,
      album TEXT,
      duration REAL,
      codec TEXT
    );
  `);

  // FR-6: Playlists
  db.exec(`
    CREATE TABLE IF NOT EXISTS playlists (
      id TEXT PRIMARY KEY,
      name TEXT,
      user_id TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY(user_id) REFERENCES users(id)
    );
  `);
  
  db.exec(`
    CREATE TABLE IF NOT EXISTS playlist_tracks (
      playlist_id TEXT,
      track_id TEXT,
      order_index INTEGER,
      PRIMARY KEY (playlist_id, track_id),
      FOREIGN KEY(playlist_id) REFERENCES playlists(id),
      FOREIGN KEY(track_id) REFERENCES tracks(id)
    );
  `);
}

function createWindow() {
  const mainWindow = new BrowserWindow({
    width: 800,
    height: 800,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false, // Disabling context isolation for simpler IPC in this prototype. In production, use preload scripts.
      webSecurity: false // Allow loading local files
    },
    autoHideMenuBar: true,
    titleBarStyle: 'hiddenInset', // Mac style, on windows it might look different but cleaner.
  });

  // Load the app
  if (process.env.VITE_DEV_SERVER_URL) {
    mainWindow.loadURL(process.env.VITE_DEV_SERVER_URL);
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'));
  }
  
  // Open DevTools in dev mode
  // mainWindow.webContents.openDevTools();
}

app.whenReady().then(() => {
  setupDatabase();
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

// --- IPC Handlers ---

// FR-1: Login
ipcMain.handle('auth-login', (event, { username, password }) => {
  // In a real app, hash the password (e.g. bcrypt).
  // Here we do simple simulation or plain text for prototype if bcrypt not available.
  // Warning: Production must use hashing.
  const stmt = db.prepare('SELECT * FROM users WHERE username = ?');
  const user = stmt.get(username) as any;
  
  if (user && user.password_hash === password) { // Simple check
    return { success: true, user: { id: user.id, username: user.username } };
  }
  return { success: false, error: 'Invalid credentials' };
});

ipcMain.handle('auth-register', (event, { username, password }) => {
  const stmt = db.prepare('SELECT id FROM users WHERE username = ?');
  if (stmt.get(username)) {
      return { success: false, error: 'User already exists' };
  }
  
  const id = uuidv4();
  const insert = db.prepare('INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)');
  try {
    insert.run(id, username, password);
    return { success: true, user: { id, username } };
  } catch (e: any) {
    return { success: false, error: e.message };
  }
});

// FR-10: File Management
ipcMain.handle('library-scan', async (event) => {
    // Open dialog to select folder
    const result = await dialog.showOpenDialog({
        properties: ['openDirectory']
    });
    
    if (result.canceled) return { success: false };
    
    const folderPath = result.filePaths[0];
    // Traverse and find files (simplified: just list dir).
    // In real app: recursive scan.
    // We will use 'fs' to read dir.
    const fs = require('fs');
    const parseFile = require('music-metadata').parseFile;
    
    // Recursive get files
    async function getFiles(dir: string): Promise<string[]> {
      const dirents = await fs.promises.readdir(dir, { withFileTypes: true });
      const files = await Promise.all(dirents.map((dirent: any) => {
        const res = path.resolve(dir, dirent.name);
        return dirent.isDirectory() ? getFiles(res) : res;
      }));
      return Array.prototype.concat(...files);
    }
    
    const allFiles = await getFiles(folderPath);
    const audioFiles = allFiles.filter((f: string) => /\.(mp3|flac|wav|ogg)$/i.test(f));
    
    let addedCount = 0;
    const insertTrack = db.prepare('INSERT OR IGNORE INTO tracks (id, filepath, title, artist, album, duration, codec) VALUES (?, ?, ?, ?, ?, ?, ?)');

    for (const file of audioFiles) {
        try {
            const metadata = await parseFile(file);
            const id = uuidv4();
            insertTrack.run(
                id,
                file,
                metadata.common.title || path.basename(file),
                metadata.common.artist || 'Unknown Artist',
                metadata.common.album || 'Unknown Album',
                metadata.format.duration || 0,
                metadata.format.container
            );
            addedCount++;
        } catch (e) {
            console.error('Failed to parse', file, e);
        }
    }
    
    return { success: true, added: addedCount };
});

ipcMain.handle('library-get-all', () => {
    const stmt = db.prepare('SELECT * FROM tracks ORDER BY artist, album, title');
    return stmt.all();
});
