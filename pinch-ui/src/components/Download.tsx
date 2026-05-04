import React from 'react';
import { motion } from 'framer-motion';
import { Download as DownloadIcon, RefreshCw, CheckCircle2 } from 'lucide-react';
import useStore from '../store/useStore';
import Card from './ui/card';

const Download = () => {
  const { downloadUrl, compressedSize, file, setFile } = useStore();

  const handleDownload = () => {
    if (!downloadUrl) return;
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', `pinched-${file?.name || 'file'}`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const handleCompressAnother = () => {
    setFile(null);
  };

  const originalSize = file?.size || 0;
  const reductionPercentage = originalSize ? ((originalSize - compressedSize) / originalSize) * 100 : 0;

  const formatFileSize = (bytes: number) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };

  return (
    <div className="w-full max-w-lg mx-auto p-4">
      <Card className="glass-panel border border-white/10 p-8 text-center relative overflow-hidden">
        {/* Background Glow */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-blue-500/10 blur-[100px] pointer-events-none" />

        <motion.div
          initial={{ scale: 0.5, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: "spring", damping: 15 }}
          className="mb-6 inline-flex p-4 rounded-full bg-blue-500/10 border border-blue-500/20"
        >
          <CheckCircle2 size={48} className="text-blue-400" />
        </motion.div>

        <h2 className="text-3xl font-black tracking-tight text-white mb-2">Pinch Complete</h2>
        <p className="text-white/40 font-mono text-xs uppercase tracking-widest mb-8">Optimization successful</p>

        <div className="grid grid-cols-2 gap-4 mb-8">
          <div className="p-4 rounded-xl bg-white/5 border border-white/5">
            <p className="text-[10px] font-bold uppercase tracking-widest text-white/30 mb-1">Final Size</p>
            <p className="text-xl font-mono text-white">{formatFileSize(compressedSize)}</p>
          </div>
          <div className="p-4 rounded-xl bg-blue-500/5 border border-blue-500/10">
            <p className="text-[10px] font-bold uppercase tracking-widest text-blue-400/50 mb-1">Reduction</p>
            <p className="text-xl font-mono text-blue-400">-{reductionPercentage.toFixed(1)}%</p>
          </div>
        </div>

        <div className="flex flex-col gap-3">
          <button
            onClick={handleDownload}
            className="w-full group flex items-center justify-center gap-3 bg-blue-500 hover:bg-blue-600 text-white font-bold py-4 px-6 rounded-xl transition-all shadow-[0_0_20px_rgba(59,130,246,0.3)] hover:shadow-[0_0_30px_rgba(59,130,246,0.5)]"
          >
            <DownloadIcon size={20} className="group-hover:translate-y-0.5 transition-transform" />
            <span>Download Compressed File</span>
          </button>
          
          <button
            onClick={handleCompressAnother}
            className="w-full flex items-center justify-center gap-2 text-white/40 hover:text-white/80 py-3 transition-colors text-sm font-medium"
          >
            <RefreshCw size={14} />
            <span>Compress Another File</span>
          </button>
        </div>
      </Card>
    </div>
  );
};

export default Download;
