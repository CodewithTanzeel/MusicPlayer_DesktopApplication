import { Play, Pause, SkipBack, SkipForward, Volume2, Repeat, Shuffle } from 'lucide-react';
import { usePlayer } from '../context/PlayerContext';
import { formatTime } from '../utils/time';

export const ControlsBar = () => {
  const { 
    currentTrack, 
    isPlaying, 
    togglePlay, 
    playNext, 
    playPrevious, 
    currentTime, 
    duration,
    checkSeek,
    seek, 
    volume, 
    setVolume 
  } = usePlayer();

  if (!currentTrack) {
      // Empty State
      return (
          <div className="h-24 bg-[#18181b] border-t border-white/5 flex items-center justify-center text-zinc-500">
              Select short track to vibe
          </div>
      );
  }

  return (
    <div className="h-24 bg-[#18181b] border-t border-white/5 flex items-center justify-between px-6 z-50">
      {/* Track Info */}
      <div className="flex items-center gap-4 w-[30%]">
        <div className="w-14 h-14 rounded-md bg-zinc-800 flex items-center justify-center overflow-hidden">
             {/* Placeholder Art */}
             <div className="w-full h-full bg-gradient-to-br from-violet-900 to-zinc-900" />
        </div>
        <div className="flex flex-col overflow-hidden">
          <span className="text-sm font-medium text-white truncate">{currentTrack.title}</span>
          <span className="text-xs text-zinc-400 truncate">{currentTrack.artist}</span>
        </div>
      </div>

      {/* Controls */}
      <div className="flex flex-col items-center gap-2 w-[40%]">
        <div className="flex items-center gap-6">
          <button className="text-zinc-400 hover:text-white transition-colors" title="Shuffle">
            <Shuffle size={18} />
          </button>
          
          <button onClick={playPrevious} className="text-zinc-200 hover:text-white transition-colors">
            <SkipBack size={24} fill="currentColor" />
          </button>
          
          <button 
            onClick={togglePlay}
            className="w-10 h-10 rounded-full bg-white text-black flex items-center justify-center hover:scale-105 transition-transform"
          >
            {isPlaying ? <Pause size={20} fill="currentColor" /> : <Play size={20} fill="currentColor" className="ml-1" />}
          </button>
          
          <button onClick={playNext} className="text-zinc-200 hover:text-white transition-colors">
            <SkipForward size={24} fill="currentColor" />
          </button>
          
          <button className="text-zinc-400 hover:text-white transition-colors" title="Repeat">
            <Repeat size={18} />
          </button>
        </div>

        {/* Progress Bar */}
        <div className="w-full flex items-center gap-3">
          <span className="text-xs text-zinc-500 w-10 text-right">
              {formatTime(currentTime)}
          </span>
          <input 
            type="range" 
            min="0" 
            max={duration || 100}
            value={currentTime}
            onChange={(e) => seek(Number(e.target.value))}
            className="flex-1 h-1 bg-zinc-600 rounded-lg appearance-none cursor-pointer [&::-webkit-slider-thumb]:opacity-0 hover:[&::-webkit-slider-thumb]:opacity-100"
          />
           <span className="text-xs text-zinc-500 w-10">
              {formatTime(duration)}
          </span>
        </div>
      </div>

      {/* Volume / Extra */}
      <div className="flex items-center justify-end gap-3 w-[30%]">
         <Volume2 size={20} className="text-zinc-400" />
         <input 
            type="range"
            min="0"
            max="1"
            step="0.01"
            value={volume}
            onChange={(e) => setVolume(Number(e.target.value))}
            className="w-24 h-1 bg-zinc-600 rounded-lg"
         />
      </div>
    </div>
  );
};

// Helper for formatTime if we don't have utils yet
function formatTimeStub(s: number) {
    if(!s) return "0:00";
    const m = Math.floor(s / 60);
    const sec = Math.floor(s % 60);
    return `${m}:${sec < 10 ? '0' : ''}${sec}`;
}
