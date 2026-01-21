import { ArrowRight, LayoutList, Library, Search } from 'lucide-react';

interface SidebarProps {
  currentView: string;
  onChangeView: (view: string) => void;
  onTogglePlaylists: () => void;
}

export const Sidebar = ({ currentView, onChangeView }: SidebarProps) => {

  const navItems = [
    { id: 'library', label: 'Library', icon: Library },
    { id: 'playlists', label: 'Playlists', icon: LayoutList },
    { id: 'search', label: 'Search', icon: Search },
    { id: 'queue', label: 'Queue', icon: ArrowRight },
  ];

  return (
    <div className="w-64 h-full bg-[#121212] flex flex-col border-r border-white/[0.08] p-4">
      <div className="flex items-center gap-3 px-2 mb-8 mt-4">
        <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-indigo-500 to-indigo-700 flex items-center justify-center shadow-lg shadow-indigo-500/20">
          <span className="font-bold text-white text-lg">V</span>
        </div>
        <h1 className="text-xl font-bold text-[#A1A1AA]">
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
              className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${isActive
                ? 'bg-indigo-500/12 text-[#6366F1] border-l-[3px] border-indigo-500'
                : 'text-[#A1A1AA] hover:text-[#E4E4E7] hover:bg-white/[0.04]'
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


