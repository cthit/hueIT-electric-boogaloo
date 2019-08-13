import React, {useEffect, useRef, useState} from 'react';
import logo from './logo.svg';
import './App.css';
import Navigation from './Navigation';
import Button from '@material-ui/core/Button';
import {ApplyColor} from './Util.js'
import ApplyToBackend from "./BackendInterface";
import {ChromePicker} from 'react-color'
import BasicColorScreen from "./BasicColorScreen";


const N_OF_LAMPS = 8;

export default function App(props) {

  const [state, setState] = useState(DefaultState(8));

  const didMountRef = useRef(false);

  useEffect(() => {
      if (didMountRef.current) {
        ApplyToBackend(state);
      } else {
        didMountRef.current = true;
      }
    }
  );

  return (
    <div>
      <Navigation
        state={state}
        setState={setState}
      />
    </div>
  );
}

function DefaultState(nLamps) {
  let lamps = [];
  for (let i = 0; i < nLamps; i++) {
    lamps.push(DefaultLampState(i))
  }
  return {
    "lamps": lamps,
    "color": "#09CCDA",
  };
}

function DefaultLampState(index) {
  return {
    'id': index,
    'power': true,
    'h': Math.random() * 360,
    's': Math.random() * 50 + 50,
    'v': Math.random() * 100,
  };
}
