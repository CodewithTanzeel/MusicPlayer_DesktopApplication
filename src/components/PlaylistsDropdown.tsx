import { useEffect, useState, useRef } from 'react';
import { Music, X, Play, ChevronDown } from 'lucide-react';
import { Track } from '../types';
import { usePlayer } from '../context/PlayerContext';
import { formatTime } from '../utils/time';

const { ipcRenderer } = window.require('electron');

interface Props {
  onClose: () => void;
  onSelect: (playlistId: string) => void;
}

const PlaylistsDropdown = ({ onClose, onSelect }: Props) => {
  const [playlists, setPlaylists] = useState<{ id: string; name: string }[]>([]);
  const [loading, setLoading] = useState(false);
  const [expandedPlaylistId, setExpandedPlaylistId] = useState<string | null>(null);
  const [playlistTracksMap, setPlaylistTracksMap] = useState<Record<string, Track[]>>({});
  const [loadingPlaylistId, setLoadingPlaylistId] = useState<string | null>(null);
  const [visible, setVisible] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const { playTrack } = usePlayer();

  useEffect(() => {
    loadPlaylists();
    // animate in
    const t = setTimeout(() => setVisible(true), 10);
    return () => clearTimeout(t);
  }, []);

  const loadPlaylists = async () => {
    setLoading(true);
    const res = await ipcRenderer.invoke('playlist-get-all');
    setPlaylists(res);
    setLoading(false);
  };

  const togglePlaylist = async (id: string) => {
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

  useEffect(() => {
    const onMouseDown = (e: MouseEvent) => {
      if (!containerRef.current) return;
      if (!(e.target instanceof Node)) return;
      if (!containerRef.current.contains(e.target)) {
        closeAnimated();
      }
    };
    window.addEventListener('mousedown', onMouseDown);
    return () => window.removeEventListener('mousedown', onMouseDown);
  }, []);

  const closeAnimated = () => {
    setVisible(false);
    setTimeout(() => onClose(), 220);
  };

  return (
    <div className="absolute top-0 left-0 w-full z-50 flex justify-center pointer-events-auto">
      <div
        ref={containerRef}
        className={`max-w-5xl w-full mx-4 mt-4 bg-zinc-900/90 backdrop-blur rounded-b-lg shadow-2xl overflow-hidden transform transition-transform duration-300 ease-out ${visible ? 'translate-y-0 opacity-100' : '-translate-y-full opacity-0'}`}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b border-white/5">
          <div className="flex items-center gap-3">
            <Music size={18} />
            <div>
              <div className="text-sm font-bold">Playlists</div>
              <div className="text-xs text-zinc-400">Select a playlist to open it</div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={closeAnimated} className="text-zinc-400 hover:text-white p-1 rounded">
              <X size={16} />
            </button>
          </div>
        </div>

        <div className="p-4">
          {loading ? (
            <div className="text-zinc-500">Loading...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {playlists.map(p => (
                <div key={p.id} className="col-span-1 w-full">
                  <div
                    onClick={() => togglePlaylist(p.id)}
                    className={`bg-zinc-800/50 p-6 rounded-lg cursor-pointer transition-all hover:scale-105 group ${expandedPlaylistId === p.id ? 'bg-zinc-800 ring-2 ring-primary' : 'hover:bg-zinc-800'}`}
                    role="button"
                    aria-expanded={expandedPlaylistId === p.id}
                  >
                    <div className="w-full aspect-square bg-zinc-700/50 rounded-md mb-4 flex items-center justify-center">
                      <Music size={48} className="text-zinc-600 group-hover:text-primary transition-colors" />
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="min-w-0">
                        <h3 className="font-bold text-white truncate">{p.name}</h3>
                        <p className="text-sm text-zinc-500">Playlist</p>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => { e.stopPropagation(); onSelect(p.id); }}
                          className="text-sm text-primary hover:underline"
                        >
                          Open
                        </button>
                        <ChevronDown size={20} className={`ml-4 transition-transform duration-200 ${expandedPlaylistId === p.id ? 'rotate-180 text-primary' : 'text-zinc-500 group-hover:text-primary'}`} />
                      </div>
                    </div>
                  </div>

                  {expandedPlaylistId === p.id && (
                    <div className="col-span-full mt-2 bg-zinc-900/60 p-4 rounded-md">
                      {loadingPlaylistId === p.id ? (
                        <div className="text-zinc-500">Loading...</div>
                      ) : (
                        <>
                          {playlistTracksMap[p.id]?.length ? (
                            <div className="space-y-2 max-h-48 overflow-y-auto text-sm text-zinc-300">
                              {playlistTracksMap[p.id].map((t, i) => (
                                <div key={`${t.id}-${i}`} className="flex items-center justify-between py-1 border-b border-white/5">
                                  <div className="flex items-center gap-3 truncate">
                                    <div className="truncate font-medium">{t.title}</div>
                                    <div className="text-xs text-zinc-500 truncate">{t.artist}</div>
                                  </div>
                                  <div className="flex items-center gap-3">
                                    <div className="text-xs text-zinc-400">{formatTime(t.duration)}</div>
                                    <button
                                      onClick={() => playTrack(t, playlistTracksMap[p.id] ?? [])}
                                      className="text-sm text-zinc-300 hover:text-white"
                                    >
                                      <Play size={14} />
                                    </button>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <div className="text-zinc-500">No tracks</div>
                          )}

                          <div className="mt-3 flex gap-2">
                            <button
                              onClick={(e) => { e.stopPropagation(); onSelect(p.id); }}
                              className="text-sm text-primary hover:underline"
                            >
                              Open full view
                            </button>
                            <button
                              onClick={(e) => { e.stopPropagation(); setExpandedPlaylistId(null); }}
                              className="text-sm text-zinc-400 hover:text-white hover:underline"
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
                <div className="col-span-full text-zinc-500 text-center py-10">No playlists created yet.</div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PlaylistsDropdown;
