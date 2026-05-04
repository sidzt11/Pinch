import React, { useEffect, useState, memo } from 'react';
import { motion } from 'framer-motion';
import { Canvas } from '@react-three/fiber';
import { Activity, Cpu, X } from 'lucide-react';
import Block from './Block';
import useStore from '../store/useStore';

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
};

const ViseHeader = memo(() => {
  const fileName = useStore(state => state.file?.name);
  const [elapsedTime, setElapsedTime] = useState(0);

  useEffect(() => {
    const start = Date.now();
    const interval = setInterval(() => {
      setElapsedTime(Math.floor((Date.now() - start) / 1000));
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="relative z-20 flex items-center justify-between p-6 glass-panel border-b border-white/5">
      <div className="flex items-center gap-4">
        <div className="p-2 rounded bg-blue-500/10 border border-blue-500/20">
          <Cpu size={20} className="text-blue-400" />
        </div>
        <div>
          <h2 className="text-[10px] font-bold uppercase tracking-[0.2em] text-white/40 mb-1">Process Node</h2>
          <p className="text-xs font-mono text-white/80 max-w-[200px] truncate">{fileName || 'UNKNOWN_STREAM'}</p>
        </div>
      </div>

      <div className="flex gap-8 md:gap-12">
        <div className="text-right">
          <h2 className="text-[10px] font-bold uppercase tracking-[0.2em] text-white/40 mb-1">Time Elapsed</h2>
          <p className="text-lg md:text-xl font-mono text-blue-400">{elapsedTime}s <span className="text-[10px] text-white/20">/ 08s</span></p>
        </div>
        <div className="text-right hidden sm:block">
          <h2 className="text-[10px] font-bold uppercase tracking-[0.2em] text-white/40 mb-1">Target Reduction</h2>
          <p className="text-lg md:text-xl font-mono text-red-400">~70%</p>
        </div>
      </div>
    </header>
  );
});

const ViseWalls = memo(() => {
  const progress = useStore(state => state.compressionProgress);
  return (
    <>
      <motion.div 
        className="absolute left-0 h-full w-12 md:w-24 bg-[#111] border-r-4 border-blue-500/30 flex items-center justify-end pr-2 pointer-events-none z-10"
        animate={{ x: `${(progress / 100) * 35}%` }}
        transition={{ type: 'spring', damping: 25, stiffness: 120 }}
      >
        <div className="h-1/2 w-1 bg-blue-500/20 rounded-full" />
      </motion.div>

      <motion.div 
        className="absolute right-0 h-full w-12 md:w-24 bg-[#111] border-l-4 border-blue-500/30 flex items-center justify-start pl-2 pointer-events-none z-10"
        animate={{ x: `-${(progress / 100) * 35}%` }}
        transition={{ type: 'spring', damping: 25, stiffness: 120 }}
      >
        <div className="h-1/2 w-1 bg-blue-500/20 rounded-full" />
      </motion.div>
    </>
  );
});

const ViseOverlay = memo(() => {
  const compressedSize = useStore(state => state.compressedSize);
  const fileType = useStore(state => state.file?.type);
  const isVideo = fileType?.startsWith('video/');

  return (
    <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-0">
      <div className="relative text-center">
        <motion.div 
          className="text-4xl md:text-6xl font-mono font-black tracking-tighter text-white/90"
          animate={{ scale: [1, 1.01, 1] }}
          transition={{ repeat: Infinity, duration: 0.5 }}
        >
          {formatFileSize(compressedSize)}
        </motion.div>
        <div className="text-[10px] font-mono uppercase tracking-widest text-blue-400 mt-2 opacity-50">
          {isVideo ? 'Pinching Pixels...' : 'Squeezing Sound...'}
        </div>
      </div>
    </div>
  );
});

const ViseFooter = memo(() => {
  const progress = useStore(state => state.compressionProgress);
  const setFile = useStore(state => state.setFile);

  return (
    <footer className="relative z-20 p-6 md:p-8 bg-black/40 backdrop-blur-md border-t border-white/5">
      <div className="max-w-3xl mx-auto">
        <div className="flex justify-between items-end mb-4">
          <div className="flex items-center gap-3">
            <Activity className="text-blue-500 animate-pulse" size={14} />
            <span className="text-[10px] font-mono uppercase tracking-widest text-white/60">
              {progress < 30 ? 'Initializing Clamps...' : 
               progress < 70 ? 'Structural Squeeze...' : 
               'Finalizing Bit-Pinch...'}
            </span>
          </div>
          <span className="text-xs font-mono text-blue-400">{progress.toFixed(2)}%</span>
        </div>

        <div className="relative h-1 w-full bg-white/5 rounded-full overflow-hidden">
          <motion.div 
            className="absolute top-0 left-0 h-full bg-blue-500 shadow-[0_0_15px_rgba(59,130,246,0.5)]"
            animate={{ width: `${progress}%` }}
            transition={{ ease: "linear", duration: 0.1 }}
          />
        </div>

        <div className="mt-6 md:mt-8 flex justify-center">
          <button 
            onClick={() => setFile(null)}
            className="group flex items-center gap-2 px-6 py-2 rounded-full border border-white/10 hover:border-red-500/50 hover:bg-red-500/5 transition-all duration-300"
          >
            <X size={12} className="text-white/40 group-hover:text-red-500" />
            <span className="text-[10px] uppercase font-bold tracking-widest text-white/40 group-hover:text-red-400">Abort Compression</span>
          </button>
        </div>
      </div>
    </footer>
  );
});

const Vise = () => {
  return (
    <div className="relative w-full h-full bg-[#050505] overflow-hidden flex flex-col font-sans">
      <div className="absolute inset-0 opacity-[0.03] pointer-events-none" 
           style={{ backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)', backgroundSize: '24px 24px' }} />

      <ViseHeader />

      <main className="relative flex-grow flex items-center justify-center">
        <ViseWalls />
        
        <div className="w-full h-full">
          <Canvas dpr={[1, 2]}>
            <Block />
          </Canvas>
        </div>

        <ViseOverlay />
      </main>

      <ViseFooter />
      
      <div className="scanline absolute inset-0 pointer-events-none" />
    </div>
  );
};

export default Vise;
