import React, { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Activity } from 'lucide-react';
import Card from './ui/card';
import useStore from '../store/useStore';

const Uploader = () => {
  const setFile = useStore((state) => state.setFile);

  const onDrop = useCallback((acceptedFiles) => {
    setFile(acceptedFiles[0]);
  }, [setFile]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  return (
    <div {...getRootProps()} className="w-full h-full flex items-center justify-center">
      <input {...getInputProps()} />
      <Card className={`relative group w-full max-w-xl aspect-video flex flex-col items-center justify-center text-center transition-all duration-500 glass-panel border ${isDragActive ? 'border-blue-500 scale-[1.02] shadow-[0_0_50px_rgba(59,130,246,0.2)]' : 'border-white/10 hover:border-white/20'}`}>
        <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
        
        <div className="relative z-10 p-8">
          <div className="w-16 h-16 mx-auto mb-6 rounded-2xl bg-blue-500/10 flex items-center justify-center border border-blue-500/20 group-hover:scale-110 transition-transform">
            <Activity className={`w-8 h-8 ${isDragActive ? 'text-blue-400 animate-pulse' : 'text-blue-500'}`} />
          </div>
          
          <h1 className="text-2xl font-bold tracking-tight text-white mb-2">Pinch Compression</h1>
          <p className="text-white/40 font-mono text-xs uppercase tracking-widest mb-8">Ready for input</p>
          
          {
            isDragActive ?
              <p className="text-blue-400 font-medium animate-bounce">Release to feel the Pinch</p> :
              <p className="text-white/60">Drop your media here or <span className="text-blue-500 cursor-pointer hover:underline">browse</span></p>
          }
        </div>
      </Card>
    </div>
  );
};

export default Uploader;
