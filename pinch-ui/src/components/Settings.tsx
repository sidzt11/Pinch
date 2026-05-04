import React from 'react';
import useStore from '../store/useStore';
import Tooltip from './ui/tooltip';

const Settings = ({ settings, setSettings }) => {
  const file = useStore((state) => state.file);
  const isVideo = file.type.startsWith('video/');

  const handleSettingChange = (e) => {
    setSettings({ ...settings, [e.target.name]: e.target.value });
  };

  const resetSettings = () => {
    setSettings({
      crf: 28,
      bitrate: 128,
    });
  };

  return (
    <div className="mt-4">
      {isVideo ? (
        <div className="flex items-center justify-center space-x-2">
          <Tooltip text="Constant Rate Factor: Lower values mean higher quality and larger file size.">
            <label>CRF</label>
          </Tooltip>
          <input type="range" name="crf" min="18" max="28" value={settings.crf} onChange={handleSettingChange} />
          <span>{settings.crf}</span>
        </div>
      ) : (
        <div className="flex items-center justify-center space-x-2">
          <Tooltip text="Bitrate: Higher values mean better audio quality and larger file size.">
            <label>Bitrate</label>
          </Tooltip>
          <input type="range" name="bitrate" min="64" max="192" step="64" value={settings.bitrate} onChange={handleSettingChange} />
          <span>{settings.bitrate}k</span>
        </div>
      )}
      <button onClick={resetSettings} className="text-xs text-gray-400 hover:text-white mt-2">
        Reset to Default
      </button>
    </div>
  );
};

export default Settings;
