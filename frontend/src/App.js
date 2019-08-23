import React, { useEffect, useRef, useState } from "react";
import "./App.css";
import Navigation from "./Navigation";
import * as Backend from "./BackendInterface";
import SavePresetDialog from "./SavePresetDialog";

const N_OF_LAMPS = 8;
const DEFAULT_COLOR = "#09CCDA";

// Load the core build.
let _ = require("lodash/core");

export default function App(props) {
    const [lamps, setLamps] = useState(DefaultState(N_OF_LAMPS));
    const [color, setColor] = useState(DEFAULT_COLOR);

    const didUndo = useRef(false);
    const didMountRef = useRef(false);
    const prevLamps = useRef([]);

    useEffect(() => {
        if (didMountRef.current) {
            let previousState = prevLamps.current[prevLamps.current.length - 1];
            if (!_.isEqual(lamps, previousState)) {
                prevLamps.current.push(lamps);
                Backend.Post(lamps);
            }
        } else {
            // first load, dont send to backend
            didMountRef.current = true;
            prevLamps.current.push(lamps);
        }
    });

    function undo() {
        if (prevLamps.current.length === 0) return;
        if (lamps === prevLamps.current[prevLamps.current.length - 1])
            prevLamps.current.pop();
        setLamps(prevLamps.current.pop());
        didUndo.current = true;
    }

    return (
        <div>
            <Navigation
                lamps={lamps}
                setLamps={setLamps}
                color={color}
                setColor={setColor}
                handleUndo={undo}
                disableUndo={prevLamps.current.length < 1}
            />
        </div>
    );
}

function DefaultState(nLamps) {
    let lamps = [];
    for (let i = 0; i < nLamps; i++) {
        lamps.push(DefaultLampState(i));
    }

    return lamps;
}

function DefaultLampState(index) {
    return {
        id: index,
        power: true,
        h: Math.random() * 360,
        s: Math.random() * 50 + 50,
        v: Math.random() * 100,
    };
}
