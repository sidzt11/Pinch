import React, { useState } from 'react';
import useStore from '../store/useStore';
import Card from './ui/card';
import Settings from './Settings';
import { Video, Music } from 'lucide-react';

const FileView = () => {
  const file = useStore((state) => state.file);
  const compressFile = useStore((state) => state.compressFile);
  const [settings, setSettings] = useState({
    crf: 28,
    bitrate: 128,
  });

  if (!file) {
    return null;
  }

  const handleCompress = () => {
    compressFile(settings);
  };

  const isVideo = file.type.startsWith('video/');

  return (
    <Card className="text-center flex flex-col items-center justify-center">
      {isVideo ? <Video className="w-16 h-16 mb-4" /> : <Music className="w-16 h-16 mb-4" />}
      <p className="font-bold">{file.name}</p>
      <p className="text-sm text-gray-400">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
      <Settings settings={settings} setSettings={setSettings} />
      <button onClick={handleCompress} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mt-4">
        Compress
      </button>
    </Card>
  );
};

export default FileView;
