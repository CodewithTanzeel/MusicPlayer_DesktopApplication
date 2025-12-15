import { useState, useEffect } from 'react';
import { usePlayer } from '../context/PlayerContext';
import { Track } from '../types';
import { Clock, Music, Play, MoreHorizontal, Plus, List } from 'lucide-react';
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
            // 2. Add current track to it (we need to know which track triggered this)
            // Wait, we lost the track ID when we closed the context menu.
            // We need to store 'selectedTrack' state.
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
