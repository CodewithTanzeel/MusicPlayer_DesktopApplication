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
    seek,
    volume,
    setVolume
  } = usePlayer();

  if (!currentTrack) {
    // Empty State
    return (
      <div className="h-24 bg-[#121212] border-t border-white/[0.08] flex items-center justify-center text-[#71717A]">
        Select a track to start vibing
      </div>
    );
  }

  return (
    <div className="h-24 bg-[#121212] border-t border-white/[0.08] flex items-center justify-between px-6 z-50">
      {/* Track Info */}
      <div className="flex items-center gap-4 w-[30%]">
        <div className="w-14 h-14 rounded-lg bg-[#1F1F23] flex items-center justify-center overflow-hidden shadow-lg">
          {/* Placeholder Art */}
          <div className="w-full h-full bg-gradient-to-br from-indigo-600 to-[#18181B]" />
        </div>
        <div className="flex flex-col overflow-hidden">
          <span className="text-sm font-semibold text-[#FFFFFF] truncate">{currentTrack.title}</span>
          <span className="text-xs text-[#A1A1AA] truncate">{currentTrack.artist}</span>
        </div>
      </div>

      {/* Controls */}
      <div className="flex flex-col items-center gap-2 w-[40%]">
        <div className="flex items-center gap-6">
          <button className="text-[#A1A1AA] hover:text-[#E4E4E7] transition-colors duration-200" title="Shuffle">
            <Shuffle size={18} />
          </button>

          <button onClick={playPrevious} className="text-[#E4E4E7] hover:text-white transition-colors duration-200">
            <SkipBack size={24} fill="currentColor" />
          </button>

          <button
            onClick={togglePlay}
            className="w-11 h-11 rounded-full bg-[#6366F1] text-white flex items-center justify-center hover:bg-[#818CF8] hover:scale-105 hover:shadow-[0_0_20px_rgba(99,102,241,0.4)] transition-all duration-200"
          >
            {isPlaying ? <Pause size={20} fill="currentColor" /> : <Play size={20} fill="currentColor" className="ml-0.5" />}
          </button>

          <button onClick={playNext} className="text-[#E4E4E7] hover:text-white transition-colors duration-200">
            <SkipForward size={24} fill="currentColor" />
          </button>

          <button className="text-[#A1A1AA] hover:text-[#E4E4E7] transition-colors duration-200" title="Repeat">
            <Repeat size={18} />
          </button>
        </div>

        {/* Progress Bar */}
        <div className="w-full flex items-center gap-3">
          <span className="text-xs text-[#71717A] w-10 text-right font-medium">
            {formatTime(currentTime)}
          </span>
          <input
            type="range"
            min="0"
            max={duration || 100}
            value={currentTime}
            onChange={(e) => seek(Number(e.target.value))}
            className="flex-1 h-1 rounded-lg appearance-none cursor-pointer"
            style={{ background: `linear-gradient(to right, #6366F1 ${(currentTime / (duration || 100)) * 100}%, rgba(255,255,255,0.08) 0%)` }}
          />
          <span className="text-xs text-[#71717A] w-10 font-medium">
            {formatTime(duration)}
          </span>
        </div>
      </div>

      {/* Volume / Extra */}
      <div className="flex items-center justify-end gap-3 w-[30%]">
        <Volume2 size={20} className="text-[#A1A1AA]" />
        <input
          type="range"
          min="0"
          max="1"
          step="0.01"
          value={volume}
          onChange={(e) => setVolume(Number(e.target.value))}
          className="w-24 h-1 rounded-lg appearance-none cursor-pointer"
          style={{ background: `linear-gradient(to right, #6366F1 ${volume * 100}%, rgba(255,255,255,0.08) 0%)` }}
        />
      </div>
    </div>
  );
};

// Helper for formatTime if we don't have utils yet

