import React from 'react';

const Card = ({ children, className }) => {
  return (
    <div className={`bg-gray-800/30 border border-gray-700/50 rounded-lg shadow-lg p-8 ${className}`}>
      {children}
    </div>
  );
};

export default Card;
