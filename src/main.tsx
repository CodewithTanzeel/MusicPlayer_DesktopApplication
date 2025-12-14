import React from 'react';
import ReactDOM from 'react-dom/client';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { PlayerProvider } from './context/PlayerContext';
import PlayerLayout from './PlayerLayout';
import { LoginPage } from './components/LoginPage';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
        <HashRouter>
            <PlayerProvider>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/" element={<PlayerLayout />} />
                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </PlayerProvider>
        </HashRouter>
    </AuthProvider>
  </React.StrictMode>,
)
