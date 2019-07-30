import React from 'react';
import './App.css';

import {ChromePicker} from 'react-color';
import {LampView} from './components/LampView.js'

function App() {
  return (
    <div className="flex-container">
      <div>
        <ChromePicker/>
      </div>
      <div>
        <LampView color="#00FFFF"/>
        <LampView color="#00AAAA" selected="true"/>
        <LampView color="#004400"/>
      </div>
    </div>
  );
}

export default App;
