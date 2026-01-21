import { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { ControlsBar } from './components/ControlsBar';
import { LibraryView } from './components/LibraryView';
import { PlaylistView } from './components/PlaylistView';
import PlaylistsDropdown from './components/PlaylistsDropdown';
import { useAuth } from './context/AuthContext';
import { Navigate } from 'react-router-dom';

export default function PlayerLayout() {
  const [view, setView] = useState('library');
  const [showPlaylistsDropdown, setShowPlaylistsDropdown] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    if (view.startsWith('playlist:')) {
      setShowPlaylistsDropdown(false);
    }
  }, [view]);

  if (!user) {
    return <Navigate to="/login" />;
  }

  return (
    <div className="flex h-screen w-screen bg-[#121212] text-white overflow-hidden font-sans">
      <Sidebar currentView={view} onChangeView={setView} onTogglePlaylists={() => setShowPlaylistsDropdown(s => !s)} />

      <div className="flex-1 flex flex-col min-w-0 bg-[#18181B]">
        {/* Main Content Area */}
        <div className="flex-1 relative overflow-hidden">
          {/* Gradient Mesh Background */}
          <div className="absolute top-0 left-0 w-full h-[300px] bg-gradient-to-b from-indigo-900/8 to-transparent pointer-events-none" />

          {showPlaylistsDropdown && (
            <PlaylistsDropdown
              onClose={() => setShowPlaylistsDropdown(false)}
              onSelect={(id: string) => { setView(`playlist:${id}`); setShowPlaylistsDropdown(false); }}
            />
          )}

          {view === 'library' && <LibraryView />}
          {(view === 'playlists' || view.startsWith('playlist:')) && (
            <PlaylistView
              playlistId={view.startsWith('playlist:') ? view.split(':')[1] : null}
              onNavigate={setView}
            />
          )}
          {view === 'search' && <div className="p-10 text-[#71717A]">Search Coming Soon...</div>}
          {view === 'queue' && <div className="p-10 text-[#71717A]">Queue Visualization Coming Soon...</div>}
        </div>

        {/* Controls */}
        <ControlsBar />
      </div>
    </div>
  );
}
