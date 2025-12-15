import { useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { ControlsBar } from './components/ControlsBar';
import { LibraryView } from './components/LibraryView';
import { PlaylistView } from './components/PlaylistView';
import { useAuth } from './context/AuthContext';
import { Navigate } from 'react-router-dom';

export default function PlayerLayout() {
  const [view, setView] = useState('library');
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" />;
  }

  return (
    <div className="flex h-screen w-screen bg-black text-white overflow-hidden font-sans">
      <Sidebar currentView={view} onChangeView={setView} />

      <div className="flex-1 flex flex-col min-w-0 bg-[#0f0f13]">
        {/* Main Content Area */}
        <div className="flex-1 relative overflow-hidden">
          {/* Gradient Mesh Background */}
          <div className="absolute top-0 left-0 w-full h-[300px] bg-gradient-to-b from-violet-900/10 to-transparent pointer-events-none" />

          {view === 'library' && <LibraryView />}
          {(view === 'playlists' || view.startsWith('playlist:')) && (
            <PlaylistView
              playlistId={view.startsWith('playlist:') ? view.split(':')[1] : null}
              onNavigate={setView}
            />
          )}
          {view === 'search' && <div className="p-10 text-zinc-500">Search Coming Soon...</div>}
          {view === 'queue' && <div className="p-10 text-zinc-500">Queue Visualization Coming Soon...</div>}
        </div>

        {/* Controls */}
        <ControlsBar />
      </div>
    </div>
  );
}
