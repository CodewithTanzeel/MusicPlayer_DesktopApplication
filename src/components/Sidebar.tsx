import { useState, useEffect } from 'react';
import { ArrowRight, LayoutList, Library, Search, ChevronDown, ChevronRight, Music } from 'lucide-react';
import { usePlayer } from '../context/PlayerContext';
import { Track } from '../types';

const { ipcRenderer } = window.require('electron');

interface SidebarProps {
  currentView: string;
  onChangeView: (view: string) => void;
  onTogglePlaylists: () => void;
}

export const Sidebar = ({ currentView, onChangeView }: SidebarProps) => {

  const [playlists, setPlaylists] = useState<{ id: string, name: string }[]>([]);

  useEffect(() => {
    fetchPlaylists();
  }, [currentView]); // Re-fetch when view changes (e.g. after creation)

  const fetchPlaylists = async () => {
    const pl = await ipcRenderer.invoke('playlist-get-all');
    setPlaylists(pl);
  };

  const navItems = [
    { id: 'library', label: 'Library', icon: Library },
    { id: 'playlists', label: 'Playlists', icon: LayoutList },
    { id: 'search', label: 'Search', icon: Search },
    { id: 'queue', label: 'Queue', icon: ArrowRight },
  ];

  return (
    <div className="w-64 h-full bg-[#18181b] flex flex-col border-r border-white/5 p-4">
      <div className="flex items-center gap-3 px-2 mb-8 mt-4">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-pink-500 flex items-center justify-center">
          <span className="font-bold text-white">V</span>
        </div>
        <h1 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
          Vibe
        </h1>
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentView === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onChangeView(item.id)}
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${isActive
                ? 'bg-violet-500/10 text-violet-400'
                : 'text-zinc-400 hover:text-zinc-100 hover:bg-white/5'
                }`}
            >
              <Icon size={20} />
              {item.label}
            </button>
          );
        })}
      </nav>




    </div>
  );
};


