import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { MeshWobbleMaterial, Float, Sparkles, PerspectiveCamera } from '@react-three/drei';
import * as THREE from 'three';
import useStore from '../store/useStore';

const Block = () => {
  const meshRef = useRef<THREE.Mesh>(null);
  const sparklesRef1 = useRef<any>(null);
  const sparklesRef2 = useRef<any>(null);
  
  useFrame((state, delta) => {
    // Read state directly from store to avoid React re-renders of the Block component
    const progress = useStore.getState().compressionProgress;
    const scaleX = 1 - (progress / 100) * 0.85;

    if (meshRef.current) {
      meshRef.current.rotation.y += delta * 0.5;
      meshRef.current.rotation.z += delta * 0.2;
      meshRef.current.scale.x = scaleX;
      
      // Dynamic emissive intensity
      if (meshRef.current.material instanceof THREE.MeshStandardMaterial) {
        meshRef.current.material.emissiveIntensity = progress / 200;
      }
    }

    if (sparklesRef1.current) {
      sparklesRef1.current.count = 50 + Math.floor(progress);
    }
    if (sparklesRef2.current) {
      sparklesRef2.current.visible = progress > 60;
      if (sparklesRef2.current.visible) {
        sparklesRef2.current.count = Math.floor(progress * 1.5);
      }
    }
  });

  return (
    <>
      <PerspectiveCamera makeDefault position={[0, 0, 8]} />
      <ambientLight intensity={0.5} />
      <pointLight position={[10, 10, 10]} intensity={1.5} color="#3b82f6" />
      <pointLight position={[-10, -10, -10]} intensity={0.8} color="#ef4444" />
      
      <Float speed={2} rotationIntensity={1} floatIntensity={1}>
        <mesh ref={meshRef}>
          <boxGeometry args={[3, 3, 3]} />
          <MeshWobbleMaterial 
            color="#3b82f6" 
            factor={0.5} // We can modulate this in useFrame too if needed
            speed={2} 
            metalness={0.9}
            roughness={0.1}
            emissive="#3b82f6"
            emissiveIntensity={0}
          />
        </mesh>
      </Float>

      <Sparkles 
        ref={sparklesRef1}
        count={50} 
        scale={6} 
        size={2} 
        speed={0.5} 
        color="#3b82f6" 
        opacity={0.8}
      />
      
      <Sparkles 
        ref={sparklesRef2}
        count={0} 
        scale={4} 
        size={4} 
        speed={2} 
        color="#ef4444" 
        visible={false}
      />
    </>
  );
};

export default Block;
