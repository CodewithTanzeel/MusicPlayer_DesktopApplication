import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Home, LayoutList, Library, PlusCircle, Search } from 'lucide-react';

interface SidebarProps {
  currentView: string;
  onChangeView: (view: string) => void;
}

export const Sidebar = ({ currentView, onChangeView }: SidebarProps) => {
  const navigate = useNavigate();

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
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive 
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

      {/* Mini Helper for Playlists */}
      <div className="mt-8">
        <div className="flex items-center justify-between px-2 mb-2">
          <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">
            Your Playlists
          </h3>
          <button className="text-zinc-400 hover:text-white">
            <PlusCircle size={16} />
          </button>
        </div>
        <div className="space-y-1">
            {/* Placeholder Playlists */}
            <div className="px-3 py-2 text-sm text-zinc-400 hover:text-zinc-100 cursor-pointer rounded hover:bg-white/5">
                Favorites
            </div>
            <div className="px-3 py-2 text-sm text-zinc-400 hover:text-zinc-100 cursor-pointer rounded hover:bg-white/5">
                Late Night Vibe
            </div>
        </div>
      </div>
    </div>
  );
};
