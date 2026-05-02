import React from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import Uploader from './components/Uploader';
import FileView from './components/FileView';
import Vise from './components/Vise';
import Download from './components/Download';
import useStore from './store/useStore';

function App() {
  const file = useStore((state) => state.file);
  const isCompressing = useStore((state) => state.isCompressing);
  const compressionComplete = useStore((state) => state.compressionComplete);

  return (
    <div className="w-screen h-screen flex items-center justify-center bg-black overflow-hidden font-sans">
      <AnimatePresence mode="wait">
        {isCompressing ? (
          <motion.div
            key="vise"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="w-full h-full"
          >
            <Vise />
          </motion.div>
        ) : compressionComplete ? (
          <motion.div
            key="download"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="w-full h-full flex items-center justify-center"
          >
            <Download />
          </motion.div>
        ) : (
          <motion.div
            key="setup"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 1.05 }}
            className="w-full h-full flex items-center justify-center"
          >
            <div className="w-full max-w-2xl px-4">
              {file ? <FileView /> : <Uploader />}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

export default App;
