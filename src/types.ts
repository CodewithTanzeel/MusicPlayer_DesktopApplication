export interface User {
  id: string;
  username: string;
}

export interface Track {
  id: string;
  filepath: string;
  title: string;
  artist: string;
  album: string;
  duration: number; // in seconds
  codec?: string;
  coverArt?: string; // Data URL or path
}

export interface Playlist {
  id: string;
  name: string;
  trackIds: string[];
}

export interface PlayerState {
  currentTrack: Track | null;
  isPlaying: boolean;
  volume: number; // 0-1
  currentTime: number;
  duration: number;
}
