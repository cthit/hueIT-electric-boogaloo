import React, { useEffect, useRef, useState } from "react";
import "./App.css";
import Navigation from "./Navigation";
import * as Backend from "./BackendInterface";

const N_OF_LAMPS = 8;

export default function App(props) {
    const [state, setState] = useState(DefaultState(N_OF_LAMPS));

    const didUndo = useRef(false);
    const didMountRef = useRef(false);
    const prevStates = useRef([]);
    // const changedState = useRef(false);

    useEffect(() => {
        if (didUndo.current) {
            didUndo.current = false;
        } else {
            prevStates.current.push(state);
        }
        // changedState.current = true;
        if (didMountRef.current) {
            Backend.Post(state);
        } else {
            // first load, dont send to backend
            didMountRef.current = true;
        }
    });

    function undo() {
        if (prevStates.current.length === 0) return;
        if (state === prevStates.current[prevStates.current.length - 1])
            prevStates.current.pop();
        setState(prevStates.current.pop());
        didUndo.current = true;
    }

    return (
        <div>
            <Navigation
                state={state}
                setState={setState}
                handleUndo={undo}
                disableUndo={prevStates.current.length < 2}
            />
        </div>
    );
}

function DefaultState(nLamps) {
    let lamps = [];
    for (let i = 0; i < nLamps; i++) {
        lamps.push(DefaultLampState(i));
    }
    return {
        lamps: lamps,
        color: "#09CCDA",
    };
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
