import { useState, useEffect } from 'react';
import { usePlayer } from '../context/PlayerContext';
import { Track } from '../types';
import { Clock, Music, Play, ArrowLeft, ChevronDown } from 'lucide-react';
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
    const [expandedPlaylistId, setExpandedPlaylistId] = useState<string | null>(null);
    const [playlistTracksMap, setPlaylistTracksMap] = useState<Record<string, Track[]>>({});
    const [loadingPlaylistId, setLoadingPlaylistId] = useState<string | null>(null);
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

    const togglePlaylist = async (id: string) => {
        console.log('toggle playlist', id);
        if (expandedPlaylistId === id) {
            setExpandedPlaylistId(null);
            return;
        }
        if (!playlistTracksMap[id]) {
            setLoadingPlaylistId(id);
            const t = await ipcRenderer.invoke('playlist-get-tracks', { playlistId: id });
            setPlaylistTracksMap(prev => ({ ...prev, [id]: t }));
            setLoadingPlaylistId(null);
        }
        setExpandedPlaylistId(id);
    };

    if (!playlistId) {
        return (
            <div className="p-8">
                <h2 className="text-3xl font-bold text-[#FFFFFF] mb-8">All Playlists</h2>
                {loading ? (
                    <div className="text-[#71717A]">Loading...</div>
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {playlists.map(p => (
                            <div key={p.id} className="col-span-1 w-full">
                                <div
                                    onClick={() => togglePlaylist(p.id)}
                                    className={`bg-[#1F1F23] border border-white/[0.06] p-6 rounded-xl cursor-pointer transition-all duration-200 hover:scale-[1.02] group ${expandedPlaylistId === p.id ? 'bg-[#27272A] ring-2 ring-[#6366F1]' : 'hover:bg-[#27272A]'}`}
                                    role="button"
                                    aria-expanded={expandedPlaylistId === p.id}
                                >
                                    <div className="w-full aspect-square bg-gradient-to-br from-indigo-600/20 to-[#27272A] rounded-lg mb-4 flex items-center justify-center">
                                        <Music size={48} className="text-[#71717A] group-hover:text-[#6366F1] transition-colors" />
                                    </div>

                                    <div className="flex items-center justify-between">
                                        <div className="min-w-0">
                                            <h3 className="font-bold text-[#FFFFFF] truncate">{p.name}</h3>
                                            <p className="text-sm text-[#71717A]">Playlist</p>
                                        </div>

                                        <ChevronDown size={20} className={`ml-4 transition-transform duration-200 ${expandedPlaylistId === p.id ? 'rotate-180 text-[#6366F1]' : 'text-[#71717A] group-hover:text-[#6366F1]'}`} />
                                    </div>
                                </div>

                                {/* Expanded dropdown */}
                                {expandedPlaylistId === p.id && (
                                    <div className="col-span-full mt-2 bg-[#18181B] border border-white/[0.06] p-4 rounded-lg">
                                        {loadingPlaylistId === p.id ? (
                                            <div className="text-[#71717A]">Loading...</div>
                                        ) : (
                                            <>
                                                {playlistTracksMap[p.id]?.length ? (
                                                    <div className="space-y-2 max-h-60 overflow-y-auto text-sm text-[#E4E4E7]">
                                                        {playlistTracksMap[p.id].map((t, i) => (
                                                            <div key={`${t.id}-${i}`} className="flex items-center justify-between py-1 border-b border-white/[0.04]">
                                                                <div className="flex items-center gap-3 truncate">
                                                                    <div className="font-medium truncate text-[#FFFFFF]">{t.title}</div>
                                                                    <div className="text-xs text-[#71717A] truncate">{t.artist}</div>
                                                                </div>
                                                                <div className="text-xs text-[#A1A1AA]">{formatTime(t.duration)}</div>
                                                            </div>
                                                        ))}
                                                    </div>
                                                ) : (
                                                    <div className="text-[#71717A]">No tracks in this playlist.</div>
                                                )}
                                                <div className="mt-3 flex gap-2">
                                                    <button
                                                        onClick={() => onNavigate(`playlist:${p.id}`)}
                                                        className="text-sm text-[#6366F1] hover:text-[#818CF8] hover:underline transition-colors"
                                                    >
                                                        Open full view
                                                    </button>
                                                    <button
                                                        onClick={() => { setExpandedPlaylistId(null); }}
                                                        className="text-sm text-[#A1A1AA] hover:text-white hover:underline transition-colors"
                                                    >
                                                        Close
                                                    </button>
                                                </div>
                                            </>
                                        )}
                                    </div>
                                )}
                            </div>
                        ))}
                        {playlists.length === 0 && (
                            <div className="col-span-full text-[#71717A] text-center py-10">
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
                    className="text-[#A1A1AA] hover:text-white flex items-center gap-2 mb-4 hover:underline transition-colors"
                >
                    <ArrowLeft size={16} /> Back to Playlists
                </button>
                <div className="flex items-end gap-6">
                    <div className="w-52 h-52 bg-gradient-to-br from-indigo-600 to-indigo-900 shadow-2xl rounded-xl flex items-center justify-center">
                        <Music size={64} className="text-white/20" />
                    </div>
                    <div>
                        <span className="uppercase text-xs font-bold tracking-wider text-[#A1A1AA]">Playlist</span>
                        <h1 className="text-5xl font-bold text-[#FFFFFF] mt-2 mb-4">{playlistName}</h1>
                        <div className="text-sm text-[#A1A1AA]">
                            {tracks.length} songs
                        </div>
                    </div>
                </div>
            </div>

            {/* Track List */}
            <div className="grid grid-cols-12 gap-4 px-4 py-2 text-xs font-semibold text-[#71717A] border-b border-white/[0.08] uppercase tracking-wider mb-2">
                <div className="col-span-1">#</div>
                <div className="col-span-5">Title</div>
                <div className="col-span-3">Album</div>
                <div className="col-span-2">Date Added</div>
                <div className="col-span-1 text-right"><Clock size={16} className="ml-auto" /></div>
            </div>

            {loading ? (
                <div className="text-[#71717A] mt-10 text-center">Loading Tracks...</div>
            ) : (
                <div className="space-y-1">
                    {tracks.map((track, i) => {
                        const isCurrent = currentTrack?.id === track.id;
                        return (
                            <div
                                key={`${track.id}-${i}`}
                                className={`group grid grid-cols-12 gap-4 px-4 py-3 rounded-lg items-center hover:bg-white/[0.04] transition-colors duration-200 ${isCurrent ? 'bg-indigo-500/10 text-[#6366F1]' : 'text-[#A1A1AA]'
                                    }`}
                                onDoubleClick={() => playTrack(track, tracks)}
                            >
                                <div className="col-span-1 font-medium relative">
                                    <span className="group-hover:hidden">{i + 1}</span>
                                    <button
                                        className="hidden group-hover:block text-white hover:text-[#6366F1] transition-colors"
                                        onClick={() => playTrack(track, tracks)}
                                    >
                                        <Play size={16} fill="currentColor" />
                                    </button>
                                </div>

                                <div className="col-span-5 flex items-center gap-3 overflow-hidden">
                                    <div className="flex flex-col truncate">
                                        <span className={`truncate font-medium ${isCurrent ? 'text-[#6366F1]' : 'text-[#FFFFFF]'}`}>
                                            {track.title}
                                        </span>
                                        <span className="truncate text-xs text-[#71717A]">
                                            {track.artist}
                                        </span>
                                    </div>
                                </div>

                                <div className="col-span-3 truncate text-sm text-[#A1A1AA]">
                                    {track.album}
                                </div>

                                <div className="col-span-2 text-sm text-[#71717A]">
                                    -
                                </div>

                                <div className="col-span-1 text-right text-sm font-variant-numeric tab-nums">
                                    {formatTime(track.duration)}
                                </div>
                            </div>
                        );
                    })}
                    {tracks.length === 0 && (
                        <div className="text-[#71717A] py-10 text-center">This playlist is empty.</div>
                    )}
                </div>
            )}
        </div>
    );
};
