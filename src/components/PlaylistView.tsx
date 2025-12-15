import { useState, useEffect } from 'react';
import { usePlayer } from '../context/PlayerContext';
import { Track } from '../types';
import { Clock, Music, Play, ArrowLeft } from 'lucide-react';
import { formatTime } from '../utils/time';

const { ipcRenderer } = window.require('electron');

interface PlaylistViewProps {
    playlistId?: string | null;
    onNavigate: (view: string) => void;
}

export const PlaylistView = ({ playlistId, onNavigate }: PlaylistViewProps) => {
    const [playlists, setPlaylists] = useState<{ id: string, name: string }[]>([]);
    const [tracks, setTracks] = useState<Track[]>([]);
    const [loading, setLoading] = useState(false);
    const [playlistName, setPlaylistName] = useState('');
    const { playTrack, currentTrack } = usePlayer();

    useEffect(() => {
        if (playlistId) {
            loadPlaylistTracks(playlistId);
        } else {
            loadAllPlaylists();
        }
    }, [playlistId]);

    const loadAllPlaylists = async () => {
        setLoading(true);
        const res = await ipcRenderer.invoke('playlist-get-all');
        setPlaylists(res);
        setLoading(false);
    };

    const loadPlaylistTracks = async (id: string) => {
        setLoading(true);
        // Get name
        const all = await ipcRenderer.invoke('playlist-get-all');
        const pl = all.find((p: any) => p.id === id);
        if (pl) setPlaylistName(pl.name);

        // Get tracks
        const t = await ipcRenderer.invoke('playlist-get-tracks', { playlistId: id });
        setTracks(t);
        setLoading(false);
    };

    if (!playlistId) {
        return (
            <div className="p-8">
                <h2 className="text-3xl font-bold text-white mb-8">All Playlists</h2>
                {loading ? (
                    <div className="text-zinc-500">Loading...</div>
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {playlists.map(p => (
                            <div
                                key={p.id}
                                onClick={() => onNavigate(`playlist:${p.id}`)}
                                className="bg-zinc-800/50 hover:bg-zinc-800 p-6 rounded-lg cursor-pointer transition-all hover:scale-105 group"
                            >
                                <div className="w-full aspect-square bg-zinc-700/50 rounded-md mb-4 flex items-center justify-center">
                                    <Music size={48} className="text-zinc-600 group-hover:text-primary transition-colors" />
                                </div>
                                <h3 className="font-bold text-white truncate">{p.name}</h3>
                                <p className="text-sm text-zinc-500">Playlist</p>
                            </div>
                        ))}
                        {playlists.length === 0 && (
                            <div className="col-span-full text-zinc-500 text-center py-10">
                                No playlists created yet. Go to Library to create one!
                            </div>
                        )}
                    </div>
                )}
            </div>
        );
    }

    // Detail View
    return (
        <div className="flex-1 w-full h-full p-8 overflow-y-auto">
            <div className="mb-8">
                <button
                    onClick={() => onNavigate('playlists')}
                    className="text-zinc-400 hover:text-white flex items-center gap-2 mb-4 hover:underline"
                >
                    <ArrowLeft size={16} /> Back to Playlists
                </button>
                <div className="flex items-end gap-6">
                    <div className="w-52 h-52 bg-gradient-to-br from-violet-800 to-indigo-900 shadow-2xl rounded-lg flex items-center justify-center">
                        <Music size={64} className="text-white/20" />
                    </div>
                    <div>
                        <span className="uppercase text-xs font-bold tracking-wider text-white">Playlist</span>
                        <h1 className="text-5xl font-bold text-white mt-2 mb-4">{playlistName}</h1>
                        <div className="text-sm text-zinc-400">
                            {tracks.length} songs
                        </div>
                    </div>
                </div>
            </div>

            {/* Track List */}
            <div className="grid grid-cols-12 gap-4 px-4 py-2 text-sm font-medium text-zinc-400 border-b border-white/5 uppercase tracking-wider mb-2">
                <div className="col-span-1">#</div>
                <div className="col-span-5">Title</div>
                <div className="col-span-3">Album</div>
                <div className="col-span-2">Date Added</div>
                <div className="col-span-1 text-right"><Clock size={16} className="ml-auto" /></div>
            </div>

            {loading ? (
                <div className="text-zinc-500 mt-10 text-center">Loading Tracks...</div>
            ) : (
                <div className="space-y-1">
                    {tracks.map((track, i) => {
                        const isCurrent = currentTrack?.id === track.id;
                        return (
                            <div
                                key={`${track.id}-${i}`} // Composite key as tracks can theoretically be dupes in playlists?
                                className={`group grid grid-cols-12 gap-4 px-4 py-3 rounded-md items-center hover:bg-white/5 transition-colors ${isCurrent ? 'bg-white/10 text-primary' : 'text-zinc-400'
                                    }`}
                                onDoubleClick={() => playTrack(track, tracks)}
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
                                    -
                                </div>

                                <div className="col-span-1 text-right text-sm font-variant-numeric tab-nums">
                                    {formatTime(track.duration)}
                                </div>
                            </div>
                        );
                    })}
                    {tracks.length === 0 && (
                        <div className="text-zinc-500 py-10 text-center">This playlist is empty.</div>
                    )}
                </div>
            )}
        </div>
    );
};
