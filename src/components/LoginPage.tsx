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
        <div className="w-full h-screen bg-[#121212] flex items-center justify-center relative overflow-hidden">
            {/* Background Blobs */}
            <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-indigo-900/20 rounded-full blur-3xl" />
            <div className="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] bg-indigo-800/15 rounded-full blur-3xl" />

            <div className="w-[400px] p-8 bg-[#18181B] border border-white/[0.08] rounded-xl z-10 animate-fade-in shadow-2xl">
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold text-[#6366F1] mb-2">
                        Vibe
                    </h1>
                    <p className="text-[#A1A1AA]">
                        {isLogin ? "Welcome back to the flow." : "Start your journey."}
                    </p>
                </div>

                {error && (
                    <div className="mb-4 p-3 bg-[#F43F5E]/10 border border-[#F43F5E]/20 rounded-lg text-[#F43F5E] text-sm text-center">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-xs font-semibold text-[#71717A] mb-2 uppercase tracking-wide">Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            className="w-full bg-[#1F1F23] border border-white/[0.08] text-white rounded-lg px-4 py-3 focus:border-[#6366F1] focus:ring-2 focus:ring-[#6366F1]/20 transition-all outline-none"
                            placeholder="Enter username"
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-[#71717A] mb-2 uppercase tracking-wide">Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            className="w-full bg-[#1F1F23] border border-white/[0.08] text-white rounded-lg px-4 py-3 focus:border-[#6366F1] focus:ring-2 focus:ring-[#6366F1]/20 transition-all outline-none"
                            placeholder="Enter password"
                        />
                    </div>

                    <button
                        type="submit"
                        className="w-full py-3 bg-[#6366F1] text-white font-semibold rounded-lg hover:bg-[#818CF8] hover:shadow-lg hover:shadow-indigo-500/25 transition-all duration-200 mt-4"
                    >
                        {isLogin ? "Log In" : "Create Account"}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-[#71717A]">
                    {isLogin ? "No account yet?" : "Already have an account?"}
                    <button
                        onClick={() => setIsLogin(!isLogin)}
                        className="ml-2 text-[#6366F1] hover:text-[#818CF8] font-medium transition-colors"
                    >
                        {isLogin ? "Sign up" : "Log in"}
                    </button>
                </div>
            </div>
        </div>
    );
};
