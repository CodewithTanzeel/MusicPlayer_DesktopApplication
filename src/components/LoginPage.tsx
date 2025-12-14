import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export const LoginPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    // Simple validation
    if (!username || !password) {
        setError("Please fill in all fields");
        return;
    }

    const action = isLogin ? login : register;
    const res = await action(username, password);
    
    if (res.success) {
        navigate('/'); // Go to main app
    } else {
        setError(res.error || 'Operation failed');
    }
  };

  return (
    <div className="w-full h-screen bg-black flex items-center justify-center relative overflow-hidden">
      {/* Background Blobs */}
      <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-purple-900/30 rounded-full blur-3xl" />
      <div className="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] bg-pink-900/20 rounded-full blur-3xl" />

      <div className="w-[400px] p-8 glass-panel z-10 animate-fade-in">
         <div className="text-center mb-8">
            <h1 className="text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-violet-400 to-pink-400 mb-2">
                Vibe
            </h1>
            <p className="text-zinc-400">
                {isLogin ? "Welcome back to the flow." : "Start your journey."}
            </p>
         </div>
         
         {error && (
             <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded text-red-400 text-sm text-center">
                 {error}
             </div>
         )}
         
         <form onSubmit={handleSubmit} className="space-y-4">
            <div>
               <label className="block text-xs font-medium text-zinc-500 mb-1 uppercase">Username</label>
               <input 
                  type="text" 
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  className="w-full bg-[#18181b] border-zinc-800 text-white focus:border-violet-500 transition-colors"
                  placeholder="Enter username"
               />
            </div>
            
             <div>
               <label className="block text-xs font-medium text-zinc-500 mb-1 uppercase">Password</label>
               <input 
                  type="password" 
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full bg-[#18181b] border-zinc-800 text-white focus:border-violet-500 transition-colors"
                  placeholder="Enter password"
               />
            </div>
            
            <button 
                type="submit"
                className="w-full py-3 bg-white text-black font-bold rounded hover:bg-zinc-200 transition-colors mt-4"
            >
                {isLogin ? "Log In" : "Create Account"}
            </button>
         </form>
         
         <div className="mt-6 text-center text-sm text-zinc-500">
             {isLogin ? "No account yet?" : "Already have an account?"}
             <button 
                onClick={() => setIsLogin(!isLogin)}
                className="ml-2 text-violet-400 hover:text-violet-300 font-medium"
             >
                 {isLogin ? "Sign up" : "Log in"}
             </button>
         </div>
      </div>
    </div>
  );
};
