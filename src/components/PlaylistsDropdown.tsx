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
        className={`max-w-5xl w-full mx-4 mt-4 bg-[#18181B]/95 backdrop-blur-lg border border-white/[0.08] rounded-xl shadow-2xl overflow-hidden transform transition-all duration-300 ease-out ${visible ? 'translate-y-0 opacity-100' : '-translate-y-full opacity-0'}`}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b border-white/[0.08]">
          <div className="flex items-center gap-3">
            <Music size={18} className="text-[#A1A1AA]" />
            <div>
              <div className="text-sm font-bold text-[#FFFFFF]">Playlists</div>
              <div className="text-xs text-[#A1A1AA]">Select a playlist to open it</div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={closeAnimated} className="text-[#A1A1AA] hover:text-white p-1.5 rounded-md hover:bg-white/[0.06] transition-colors">
              <X size={16} />
            </button>
          </div>
        </div>

        <div className="p-4">
          {loading ? (
            <div className="text-[#71717A]">Loading...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
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

                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => { e.stopPropagation(); onSelect(p.id); }}
                          className="text-sm text-[#6366F1] hover:text-[#818CF8] hover:underline transition-colors"
                        >
                          Open
                        </button>
                        <ChevronDown size={20} className={`ml-4 transition-transform duration-200 ${expandedPlaylistId === p.id ? 'rotate-180 text-[#6366F1]' : 'text-[#71717A] group-hover:text-[#6366F1]'}`} />
                      </div>
                    </div>
                  </div>

                  {expandedPlaylistId === p.id && (
                    <div className="col-span-full mt-2 bg-[#121212] border border-white/[0.06] p-4 rounded-lg">
                      {loadingPlaylistId === p.id ? (
                        <div className="text-[#71717A]">Loading...</div>
                      ) : (
                        <>
                          {playlistTracksMap[p.id]?.length ? (
                            <div className="space-y-2 max-h-48 overflow-y-auto text-sm text-[#E4E4E7]">
                              {playlistTracksMap[p.id].map((t, i) => (
                                <div key={`${t.id}-${i}`} className="flex items-center justify-between py-1 border-b border-white/[0.04]">
                                  <div className="flex items-center gap-3 truncate">
                                    <div className="truncate font-medium text-[#FFFFFF]">{t.title}</div>
                                    <div className="text-xs text-[#71717A] truncate">{t.artist}</div>
                                  </div>
                                  <div className="flex items-center gap-3">
                                    <div className="text-xs text-[#A1A1AA]">{formatTime(t.duration)}</div>
                                    <button
                                      onClick={() => playTrack(t, playlistTracksMap[p.id] ?? [])}
                                      className="text-sm text-[#A1A1AA] hover:text-[#6366F1] transition-colors"
                                    >
                                      <Play size={14} />
                                    </button>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <div className="text-[#71717A]">No tracks</div>
                          )}

                          <div className="mt-3 flex gap-2">
                            <button
                              onClick={(e) => { e.stopPropagation(); onSelect(p.id); }}
                              className="text-sm text-[#6366F1] hover:text-[#818CF8] hover:underline transition-colors"
                            >
                              Open full view
                            </button>
                            <button
                              onClick={(e) => { e.stopPropagation(); setExpandedPlaylistId(null); }}
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
                <div className="col-span-full text-[#71717A] text-center py-10">No playlists created yet.</div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PlaylistsDropdown;
