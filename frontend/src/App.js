import React, { useEffect, useRef, useState } from "react";
import "./App.css";
import Navigation from "./Navigation";
import { Post as BackendPost, Get as BackendGet } from "./BackendInterface";

const N_OF_LAMPS = 8;

export default function App(props) {
    const [state, setState] = useState(DefaultState(N_OF_LAMPS));

    const didMountRef = useRef(false);
    const changedState = useRef(false);

    useEffect(() => {
        changedState.current = true;
        if (didMountRef.current) {
            BackendPost(state);
        } else {
            // first load, dont send to backend
            didMountRef.current = true;
        }
    });

    return (
        <div>
            <Navigation state={state} setState={setState} />
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
        color: "#09CCDA"
    };
}

function DefaultLampState(index) {
    return {
        id: index,
        power: true,
        h: Math.random() * 360,
        s: Math.random() * 50 + 50,
        v: Math.random() * 100
    };
}
