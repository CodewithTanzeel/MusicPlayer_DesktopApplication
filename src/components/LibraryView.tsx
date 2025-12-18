import { useState, useEffect } from 'react';
import { usePlayer } from '../context/PlayerContext';
import { Track } from '../types';
import { Clock, Music, Play, MoreHorizontal, Plus, List, ChevronDown, ChevronRight, Search } from 'lucide-react';
import { formatTime } from '../utils/time';

const { ipcRenderer } = window.require('electron');

export const LibraryView = () => {
    const [tracks, setTracks] = useState<Track[]>([]);
    const [loading, setLoading] = useState(false);
    const { playTrack, currentTrack, addToQueue } = usePlayer();

    useEffect(() => {
        loadLibrary();
    }, []);

    // --- Playlist Logic ---
    const [contextMenuTrackId, setContextMenuTrackId] = useState<string | null>(null);
    const [showCreatePlaylist, setShowCreatePlaylist] = useState(false);
    const [showAddToPlaylist, setShowAddToPlaylist] = useState(false);
    const [playlists, setPlaylists] = useState<{ id: string, name: string }[]>([]);
    const [newPlaylistName, setNewPlaylistName] = useState('');

    // Playlist dropdown UI (moved back to Library)
    const [playlistOpen, setPlaylistOpen] = useState(false);
    const [playlistSearch, setPlaylistSearch] = useState('');
    const [expandedPlaylistId, setExpandedPlaylistId] = useState<string | null>(null);
    const [playlistTracksMap, setPlaylistTracksMap] = useState<Record<string, Track[]>>({});

    const loadPlaylistsList = async () => {
        const pl = await ipcRenderer.invoke('playlist-get-all');
        setPlaylists(pl);
    };

    const loadPlaylistTracks = async (id: string) => {
        if (playlistTracksMap[id]) return; // already loaded
        const t: Track[] = await ipcRenderer.invoke('playlist-get-tracks', { playlistId: id });
        setPlaylistTracksMap(prev => ({ ...prev, [id]: t }));
    };


    const toggleContextMenu = (id: string, e: React.MouseEvent) => {
        e.stopPropagation();
        setContextMenuTrackId(contextMenuTrackId === id ? null : id);
    };

    const handleCreatePlaylistOption = () => {
        setContextMenuTrackId(null);
        setShowCreatePlaylist(true);
    };

    const handleAddToPlaylistOption = async () => {
        setContextMenuTrackId(null);
        // Fetch playlists
        const pl = await ipcRenderer.invoke('playlist-get-all');
        setPlaylists(pl);
        setShowAddToPlaylist(true);
    };

    const confirmCreatePlaylist = async () => {
        if (!newPlaylistName.trim()) return;

        // 1. Create Playlist
        const res = await ipcRenderer.invoke('playlist-create', { name: newPlaylistName });
        if (res.success) {
            // If this modal was opened from a track context menu, add that track
            if (contextMenuTrackId) {
                await ipcRenderer.invoke('playlist-add-track', {
                    playlistId: res.playlist.id || res.id || null,
                    trackId: contextMenuTrackId
                });
            }

            // Refresh playlists shown in the dropdown
            await loadPlaylistsList();
            setNewPlaylistName('');
            setShowCreatePlaylist(false);
        }
    };


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


    const confirmAddToPlaylist = async (playlistId: string) => {
        if (!contextMenuTrackId) return;

        await ipcRenderer.invoke('playlist-add-track', {
            playlistId,
            trackId: contextMenuTrackId
        });

        setShowAddToPlaylist(false);
        setContextMenuTrackId(null);
        // Show success toast?
    };

    return (
        <div className="flex-1 w-full h-full p-8 overflow-y-auto relative">

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
                                className={`group grid grid-cols-12 gap-4 px-4 py-3 rounded-md items-center hover:bg-white/5 transition-colors ${isCurrent ? 'bg-white/10 text-primary' : 'text-zinc-400'
                                    }`}
                                onDoubleClick={() => playTrack(track, tracks)}
                                onContextMenu={(e) => {
                                    e.preventDefault();
                                    addToQueue(track);
                                    // Could show a toast here "Added to Queue"
                                }}
                            >
                                <div className="col-span-1 font-medium relative flex items-center gap-2">
                                    <div className="w-6 text-center group-hover:hidden">{i + 1}</div>
                                    <button
                                        className="hidden group-hover:block text-white hover:text-primary"
                                        onClick={() => playTrack(track, tracks)}
                                    >
                                        <Play size={16} fill="currentColor" />
                                    </button>
                                    <button
                                        className="text-zinc-400 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity"
                                        onClick={(e) => toggleContextMenu(track.id, e)}
                                    >
                                        <MoreHorizontal size={16} />
                                    </button>

                                    {/* Context Menu */}
                                    {contextMenuTrackId === track.id && (
                                        <div className="absolute left-8 top-8 z-50 bg-zinc-900 border border-white/10 rounded-md shadow-xl py-1 w-48 font-normal text-sm text-white">
                                            <button
                                                className="w-full text-left px-4 py-2 hover:bg-zinc-800 flex items-center gap-2"
                                                onClick={handleCreatePlaylistOption}
                                            >
                                                <Plus size={14} /> Create New Playlist
                                            </button>
                                            <button
                                                className="w-full text-left px-4 py-2 hover:bg-zinc-800 flex items-center gap-2"
                                                onClick={handleAddToPlaylistOption}
                                            >
                                                <List size={14} /> Add to Existing Playlist
                                            </button>
                                        </div>
                                    )}
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

            {/* Playlists Dropdown (moved into Library) */}
            <div className="mt-8 px-4">
                <div className="w-full flex items-center justify-between bg-zinc-900/40 px-4 py-3 rounded hover:bg-zinc-900 transition-colors">
                    <button
                        className="flex items-center gap-3 text-left"
                        onClick={async () => {
                            const next = !playlistOpen;
                            setPlaylistOpen(next);
                            if (next) await loadPlaylistsList();
                        }}
                    >
                        <List size={16} />
                        <span className="font-medium text-white ml-2">Playlists</span>
                    </button>

                    <div className="flex items-center gap-2">
                        <button
                            className="text-zinc-400 hover:text-white p-1 rounded"
                            onClick={(e) => {
                                e.stopPropagation();
                                setShowCreatePlaylist(true);
                            }}
                            title="Create Playlist"
                        >
                            <Plus size={16} />
                        </button>
                        <button
                            className={`transform transition-transform duration-150 ${playlistOpen ? 'rotate-180' : ''}`}
                            onClick={async (e) => {
                                e.stopPropagation();
                                const next = !playlistOpen;
                                setPlaylistOpen(next);
                                if (next) await loadPlaylistsList();
                            }}
                        >
                            <ChevronDown size={18} />
                        </button>
                    </div>
                </div> 

                {playlistOpen && (
                    <div className="mt-3 bg-zinc-900/30 border border-white/5 rounded p-3">
                        <div className="flex items-center gap-2 mb-3">
                            <div className="relative flex-1">
                                <input
                                    type="text"
                                    placeholder="Search Playlists..."
                                    className="w-full pl-9 pr-3"
                                    value={playlistSearch}
                                    onChange={(e) => setPlaylistSearch(e.target.value)}
                                />
                                <div className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500 pointer-events-none">
                                    <Search size={14} />
                                </div>
                            </div>
                        </div>

                        <div className="playlist-dropdown max-h-80 overflow-y-auto custom-scrollbar">
                            {playlists.filter(p => p.name.toLowerCase().includes(playlistSearch.toLowerCase())).map(p => (
                                <div key={p.id} className="mb-2">
                                    <div className="flex items-center justify-between px-3 py-2 rounded hover:bg-zinc-800/60 cursor-pointer" onClick={async () => {
                                        const expanding = expandedPlaylistId === p.id ? null : p.id;
                                        setExpandedPlaylistId(expanding);
                                        if (expanding) await loadPlaylistTracks(p.id);
                                    }}>
                                        <div className="flex items-center gap-3">
                                            <Music size={16} />
                                            <span className="font-medium text-white truncate">{p.name}</span>
                                        </div>
                                        <div className="flex items-center gap-2 text-zinc-400">
                                            <span className="text-sm">{playlistTracksMap[p.id]?.length ?? '-'}</span>
                                            <ChevronRight size={16} className={`transform ${expandedPlaylistId === p.id ? 'rotate-90' : ''} transition-transform duration-150`} />
                                        </div>
                                    </div>

                                    {expandedPlaylistId === p.id && (
                                        <div className="mt-2 ml-6">
                                            {(!playlistTracksMap[p.id] || playlistTracksMap[p.id].length === 0) ? (
                                                <div className="text-zinc-500 text-sm py-2">No songs in this playlist.</div>
                                            ) : (
                                                playlistTracksMap[p.id].map((t, i) => (
                                                    <div key={`${p.id}-${t.id}-${i}`} className="flex items-center justify-between px-3 py-2 rounded hover:bg-white/5 transition-colors">
                                                        <div className="flex items-center gap-3 truncate">
                                                            <div className="w-8 h-8 bg-zinc-800 rounded flex items-center justify-center text-zinc-500">
                                                                <Music size={14} />
                                                            </div>
                                                            <div className="truncate text-sm">
                                                                <div className="truncate font-medium">{t.title}</div>
                                                                <div className="text-xs text-zinc-500 truncate">{t.artist}</div>
                                                            </div>
                                                        </div>
                                                        <div className="text-sm text-zinc-500 flex items-center gap-2">
                                                            <div className="text-xs text-zinc-400">{formatTime(t.duration)}</div>
                                                            <button className="text-zinc-400 hover:text-white" onClick={() => playTrack(t, playlistTracksMap[p.id] ?? [])}>
                                                                <Play size={14} />
                                                            </button>
                                                        </div>
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    )}
                                </div>
                            ))}

                            {playlists.length === 0 && (
                                <div className="text-zinc-500 px-3 py-2">No playlists found.</div>
                            )}
                        </div>
                    </div>
                )}
            </div>



            {/* Modals */}
            {
                showCreatePlaylist && (
                    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-[100]">
                        <div className="bg-zinc-900 border border-white/10 p-6 rounded-lg w-96 shadow-2xl">
                            <h3 className="text-xl font-bold text-white mb-4">Create Playlist</h3>
                            <input
                                type="text"
                                placeholder="Playlist Name"
                                className="w-full bg-zinc-800 border-none rounded p-2 text-white mb-4 focus:ring-1 focus:ring-primary outline-none"
                                value={newPlaylistName}
                                onChange={(e) => setNewPlaylistName(e.target.value)}
                                autoFocus
                            />
                            <div className="flex justify-end gap-2">
                                <button
                                    className="px-4 py-2 text-zinc-400 hover:text-white"
                                    onClick={() => setShowCreatePlaylist(false)}
                                >
                                    Cancel
                                </button>
                                <button
                                    className="px-4 py-2 bg-primary text-white rounded font-medium hover:brightness-110"
                                    onClick={() => {
                                        confirmCreatePlaylist();
                                        setShowCreatePlaylist(false);
                                    }}
                                >
                                    Create & Add Song
                                </button>
                            </div>
                        </div>
                    </div>
                )
            }

            {
                showAddToPlaylist && (
                    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-[100]">
                        <div className="bg-zinc-900 border border-white/10 p-6 rounded-lg w-96 max-h-[80vh] flex flex-col shadow-2xl">
                            <h3 className="text-xl font-bold text-white mb-4">Add to Playlist</h3>
                            <div className="flex-1 overflow-y-auto space-y-1 mb-4 custom-scrollbar">
                                {playlists.length === 0 ? (
                                    <div className="text-zinc-500 text-center py-4">No playlists found.</div>
                                ) : (
                                    playlists.map(p => (
                                        <button
                                            key={p.id}
                                            className="w-full text-left px-4 py-3 bg-zinc-800/50 hover:bg-zinc-800 rounded text-white transition-colors"
                                            onClick={() => confirmAddToPlaylist(p.id)}
                                        >
                                            {p.name}
                                        </button>
                                    ))
                                )}
                            </div>
                            <div className="flex justify-end">
                                <button
                                    className="px-4 py-2 text-zinc-400 hover:text-white"
                                    onClick={() => setShowAddToPlaylist(false)}
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                )
            }
        </div>
    );
};
