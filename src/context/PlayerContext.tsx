import React, { createContext, useContext, useState, useRef, useEffect, ReactNode } from 'react';
import { Track } from '../types';
import { DoublyLinkedList, Node } from '../utils/structures/DoublyLinkedList';
import { Queue } from '../utils/structures/Queue';
import { Stack } from '../utils/structures/Stack';

interface PlayerContextType {
  currentTrack: Track | null;
  isPlaying: boolean;
  volume: number;
  currentTime: number;
  duration: number;
  playTrack: (track: Track, contextList?: Track[]) => void;
  togglePlay: () => void;
  playNext: () => void;
  playPrevious: () => void;
  addToQueue: (track: Track) => void;
  seek: (time: number) => void;
  setVolume: (vol: number) => void;
  queue: Track[]; // For UI visualization
  history: Track[]; // For UI visualization (optional)
}

const PlayerContext = createContext<PlayerContextType | undefined>(undefined);

export const PlayerProvider = ({ children }: { children: ReactNode }) => {
  // --- State ---
  const [currentTrack, setCurrentTrack] = useState<Track | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [volume, setVolumeState] = useState(0.5);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [queueArr, setQueueArr] = useState<Track[]>([]); // For UI only
  
  // --- Data Structures ---
  // The 'Main Playlist' context (e.g. invalidating when user clicks a new album)
  const mainList = useRef(new DoublyLinkedList<Track>());
  const currentNode = useRef<Node<Track> | null>(null);
  
  // FR-4: Priority Queue (FIFO) for manual 'Play Next'
  const priorityQueue = useRef(new Queue<Track>());
  
  // FR-5: History Stack
  const historyStack = useRef(new Stack<Track>());
  
  // --- Audio Engine ---
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    audioRef.current = new Audio();
    audioRef.current.volume = volume;
    
    // Event Listeners
    const audio = audioRef.current;
    
    const timeUpdate = () => setCurrentTime(audio.currentTime);
    const loadedMetadata = () => setDuration(audio.duration);
    const onEnded = () => playNext(); // Auto-advance
    
    audio.addEventListener('timeupdate', timeUpdate);
    audio.addEventListener('loadedmetadata', loadedMetadata);
    audio.addEventListener('ended', onEnded);
    
    return () => {
      audio.removeEventListener('timeupdate', timeUpdate);
      audio.removeEventListener('loadedmetadata', loadedMetadata);
      audio.removeEventListener('ended', onEnded);
    };
  }, []); // Run once

  // --- Actions ---

  const playTrack = (track: Track, contextList?: Track[]) => {
    // 1. If context provided, rebuild the DoublyLinkedList
    if (contextList) {
      mainList.current.clear();
      contextList.forEach(t => {
          const node = mainList.current.append(t);
          if (t.id === track.id) currentNode.current = node;
      });
    }

    // 2. Add current track to history before switching (if it exists)
    if (currentTrack) {
        historyStack.current.push(currentTrack);
    }

    // 3. Set and Play
    loadAndPlay(track);
  };

  const loadAndPlay = (track: Track) => {
    if (!audioRef.current) return;
    
    setCurrentTrack(track);
    audioRef.current.src = track.filepath.startsWith('file:') 
        ? track.filepath 
        : `file://${track.filepath}`; // Handle Electron file paths
    
    audioRef.current.play().catch(e => console.error("Play error:", e));
    setIsPlaying(true);
  };

  const togglePlay = () => {
    if (!audioRef.current || !currentTrack) return;
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const playNext = () => {
    // Priority 1: Check Queue (FR-4)
    if (!priorityQueue.current.isEmpty()) {
       // Since the Queue is "Play Next", it interrupts the main list flow but 
       // usually we want to return to the main list after? 
       // For simply strict requirements: "Incoming songs from queue preempt main playlist"
       const nextTrack = priorityQueue.current.dequeue();
       if (nextTrack) {
           setQueueArr(priorityQueue.current.toArray()); // Update UI
           
           // We push current to history
           if(currentTrack) historyStack.current.push(currentTrack);
           
           loadAndPlay(nextTrack);
           return;
       }
    }

    // Priority 2: Next in Linked List (FR-2)
    if (currentNode.current?.next) {
        currentNode.current = currentNode.current.next;
        const nextTrack = currentNode.current.value;
        if (currentTrack) historyStack.current.push(currentTrack);
        loadAndPlay(nextTrack);
    } else {
        // End of playlist
        setIsPlaying(false);
    }
  };

  const playPrevious = () => {
      // FR-5: "Pressing Back retrieves the last track" (History Stack)
      // Usually "Previous" button checks history first if we want strict Backtracking,
      // OR it checks the DoublyLinkedList 'prev'.
      // Requirement says: "Pressing Back/Home retrieves the last track..." (Stack)
      
      const prevFromHistory = historyStack.current.pop();
      if (prevFromHistory) {
          // If we are navigating back, we probably need to sync our currentNode 
          // to this track if it exists in our current list to maintain consistency
          // or just play it as an isolated event.
          // Let's try to find it in the current list to keep 'next' working.
          
          let iter = mainList.current.head;
          let found = false;
          while(iter) {
              if (iter.value.id === prevFromHistory.id) {
                  currentNode.current = iter;
                  found = true;
                  break;
              }
              iter = iter.next;
          }
          
          loadAndPlay(prevFromHistory);
      } else if (currentNode.current?.prev) {
          // Fallback to list previous if history empty
          currentNode.current = currentNode.current.prev;
          loadAndPlay(currentNode.current.value);
      } else {
          // Start of list, maybe replay
          if (audioRef.current) audioRef.current.currentTime = 0;
      }
  };

  const addToQueue = (track: Track) => {
      priorityQueue.current.enqueue(track);
      setQueueArr(priorityQueue.current.toArray());
  };

  const seek = (time: number) => {
      if (audioRef.current) {
          audioRef.current.currentTime = time;
          setCurrentTime(time);
      }
  };

  const setVolume = (vol: number) => {
      if (audioRef.current) {
          audioRef.current.volume = vol;
          setVolumeState(vol);
      }
  };

  return (
    <PlayerContext.Provider value={{
      currentTrack,
      isPlaying,
      volume,
      currentTime,
      duration,
      playTrack,
      togglePlay,
      playNext,
      playPrevious,
      addToQueue,
      seek,
      setVolume,
      queue: queueArr,
      history: [] // Not exposing full history to UI yet to save perf, but could
    }}>
      {children}
    </PlayerContext.Provider>
  );
};

export const usePlayer = () => {
  const context = useContext(PlayerContext);
  if (!context) throw new Error("usePlayer must be used within PlayerProvider");
  return context;
};
