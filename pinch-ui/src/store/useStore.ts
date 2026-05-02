import create from 'zustand';
import axios from 'axios';

const SIMULATION_DURATION = 8000; // 8 seconds for the animation
const SIMULATION_INTERVAL = 50; // Higher frequency for smoother UI, but with optimized rendering

let simulationInterval: number | null = null;

const useStore = create((set, get) => ({
  file: null,
  setFile: (file) => {
    if (simulationInterval) {
      clearInterval(simulationInterval);
      simulationInterval = null;
    }
    set({ 
      file, 
      isCompressing: false, 
      compressionComplete: false, 
      compressionProgress: 0,
      compressedSize: 0,
      downloadUrl: null 
    });
  },
  compressionProgress: 0,
  isCompressing: false,
  compressionComplete: false,
  compressedSize: 0,
  downloadUrl: null,
  compressFile: async (settings) => {
    const { file } = get();
    if (!file) return;

    // Clear any existing interval just in case
    if (simulationInterval) clearInterval(simulationInterval);

    set({ 
      isCompressing: true, 
      compressionComplete: false, 
      compressionProgress: 0, 
      compressedSize: file.size 
    });

    let networkRequestComplete = false;
    let finalBlob: Blob | null = null;

    // --- Start the real network request in the background ---
    const formData = new FormData();
    formData.append('file', file);
    const isVideo = file.type.startsWith('video/');
    const url = isVideo ? 'http://localhost:8080/api/v1/compress/video' : 'http://localhost:8080/api/v1/compress/audio';
    
    if (isVideo) {
      formData.append('crf', settings.crf || '28');
    } else {
      formData.append('bitrate', (settings.bitrate || '128') + 'k');
    }

    axios.post(url, formData, { responseType: 'blob' })
      .then(response => {
        finalBlob = response.data;
      })
      .catch(error => {
        console.error('Compression error:', error);
        // We don't immediately reset file to null to let the user see the error or retry
        set({ isCompressing: false });
      })
      .finally(() => {
        networkRequestComplete = true;
      });

    // --- Start the UI simulation ---
    let elapsedTime = 0;
    simulationInterval = window.setInterval(() => {
      elapsedTime += SIMULATION_INTERVAL;
      const progress = Math.min(elapsedTime / SIMULATION_DURATION, 1);

      // Simulate a plausible final size (e.g., 30% of original)
      const estimatedFinalSize = file.size * 0.3;
      const progressFactor = progress;
      
      // Smoother size reduction simulation
      const currentSize = file.size - (file.size - estimatedFinalSize) * progressFactor;
      // Add slight jitter for realism
      const jitter = (Math.random() - 0.5) * (file.size * 0.01) * (1 - progress);

      set({
        compressionProgress: progress * 100,
        compressedSize: Math.max(currentSize + jitter, estimatedFinalSize),
      });

      if (progress >= 1) {
        clearInterval(simulationInterval!);
        simulationInterval = null;

        // Wait for the real network request to finish before showing the download screen
        const waitForNetwork = () => {
          if (networkRequestComplete) {
            if (finalBlob) {
              const downloadUrl = window.URL.createObjectURL(finalBlob);
              // Add a small delay for a smooth transition feel
              setTimeout(() => {
                set({
                  isCompressing: false,
                  compressionComplete: true,
                  downloadUrl,
                  compressedSize: finalBlob!.size,
                });
              }, 600);
            }
          } else {
            setTimeout(waitForNetwork, 200);
          }
        };
        waitForNetwork();
      }
    }, SIMULATION_INTERVAL);
  },
}));

export default useStore;
