import { useState, useEffect } from 'react';
import { usePlayer } from '../context/PlayerContext';
import { Track } from '../types';
import { Clock, Music, Play } from 'lucide-react';
import { formatTime } from '../utils/time';

const { ipcRenderer } = window.require('electron');

export const LibraryView = () => {
    const [tracks, setTracks] = useState<Track[]>([]);
    const [loading, setLoading] = useState(false);
    const { playTrack, currentTrack, addToQueue } = usePlayer();

    useEffect(() => {
        loadLibrary();
    }, []);

    const loadLibrary = async () => {
        setLoading(true);
        const allTracks = await ipcRenderer.invoke('library-get-all');
        // Map DB result to Track interface
        const mapped: Track[] = allTracks.map((t: any) => ({
            id: t.id,
            filepath: t.filepath,
            title: t.title,
            artist: t.artist,
            album: t.album,
            duration: t.duration
        }));
        setTracks(mapped);
        setLoading(false);
    };

    const handleImport = async () => {
        setLoading(true);
        await ipcRenderer.invoke('library-scan');
        loadLibrary(); // Reload
    };

    return (
        <div className="flex-1 w-full h-full p-8 overflow-y-auto">
             <div className="flex items-center justify-between mb-8">
                 <h2 className="text-3xl font-bold text-white">Library</h2>
                 <button 
                    onClick={handleImport}
                    className="px-4 py-2 bg-white text-black font-semibold rounded-full hover:scale-105 transition-transform"
                 >
                     Import Folder
                 </button>
             </div>

             {/* Header */}
             <div className="grid grid-cols-12 gap-4 px-4 py-2 text-sm font-medium text-zinc-400 border-b border-white/5 uppercase tracking-wider mb-2">
                 <div className="col-span-1">#</div>
                 <div className="col-span-5">Title</div>
                 <div className="col-span-3">Album</div>
                 <div className="col-span-2">Date Added</div>
                 <div className="col-span-1 text-right flex justify-end"><Clock size={16} /></div>
             </div>
        
             {loading ? (
                 <div className="text-zinc-500 mt-10 text-center">Loading Library...</div>
             ) : (
                <div className="space-y-1">
                    {tracks.map((track, i) => {
                        const isCurrent = currentTrack?.id === track.id;
                        return (
                            <div 
                                key={track.id}
                                className={`group grid grid-cols-12 gap-4 px-4 py-3 rounded-md items-center hover:bg-white/5 transition-colors ${
                                    isCurrent ? 'bg-white/10 text-primary' : 'text-zinc-400'
                                }`}
                                onDoubleClick={() => playTrack(track, tracks)}
                                onContextMenu={(e) => {
                                    e.preventDefault();
                                    addToQueue(track);
                                    // Could show a toast here "Added to Queue"
                                }}
                            >
                                <div className="col-span-1 font-medium relative">
                                    <span className="group-hover:hidden">{i + 1}</span>
                                    <button 
                                        className="hidden group-hover:block text-white"
                                        onClick={() => playTrack(track, tracks)}
                                    >
                                        <Play size={16} fill="currentColor" />
                                    </button>
                                </div>
                                
                                <div className="col-span-5 flex items-center gap-3 overflow-hidden">
                                     <div className="w-10 h-10 bg-zinc-800 rounded flex items-center justify-center flex-shrink-0">
                                         <Music size={16} className="text-zinc-600" />
                                     </div>
                                     <div className="flex flex-col truncate">
                                         <span className={`truncate font-medium ${isCurrent ? 'text-violet-400' : 'text-white'}`}>
                                             {track.title}
                                         </span>
                                         <span className="truncate text-xs text-zinc-500">
                                             {track.artist}
                                         </span>
                                     </div>
                                </div>
                                
                                <div className="col-span-3 truncate text-sm">
                                    {track.album}
                                </div>
                                
                                <div className="col-span-2 text-sm text-zinc-600">
                                    Just now
                                </div>
                                
                                <div className="col-span-1 text-right text-sm font-variant-numeric tab-nums">
                                    {formatTime(track.duration)}
                                </div>
                            </div>
                        );
                    })}
                </div>
             )}
             
             {!loading && tracks.length === 0 && (
                 <div className="mt-20 text-center">
                     <div className="text-zinc-400 mb-4">Your library is empty.</div>
                 </div>
             )}
        </div>
    );
};
