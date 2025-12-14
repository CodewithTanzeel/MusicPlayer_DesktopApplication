import { useState } from 'react';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  login: (u: string, p: string) => Promise<{success: boolean; error?: string}>;
  register: (u: string, p: string) => Promise<{success: boolean; error?: string}>;
  logout: () => void;
}

import React, { createContext, useContext, ReactNode } from 'react';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const { ipcRenderer } = window.require('electron');

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);

  const login = async (u: string, p: string) => {
    try {
        const res = await ipcRenderer.invoke('auth-login', { username: u, password: p });
        if (res.success) {
            setUser(res.user);
        }
        return res;
    } catch (e: any) {
        return { success: false, error: e.message };
    }
  };

  const register = async (u: string, p: string) => {
      try {
        const res = await ipcRenderer.invoke('auth-register', { username: u, password: p });
        if (res.success) {
            setUser(res.user);
        }
        return res;
      } catch (e: any) {
          return { success: false, error: e.message };
      }
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
