let convert = require("color-convert");

// given a state and a list of affected indices,
// returns a new state where the color is applied
// to those lamps
export function ApplyColor(state, indices) {
    let newState = Object.assign({}, state);

    newState.lamps = state.lamps.map(lamp => {
        if (indices.includes(lamp.id)) {
            let [h, s, v] = convert.hex.hsv(state.color);
            return {
                id: lamp.id,
                power: lamp.power,
                h: h,
                s: s,
                v: v,
            };
        } else return lamp;
    });

    return newState;
}

export function ApplyColorToAll(state) {
    let newState = Object.assign({}, state);

    newState.lamps = newState.lamps.map(lamp => {
        let [h, s, v] = convert.hex.hsv(state.color);
        return {
            id: lamp.id,
            power: lamp.power,
            h: h,
            s: s,
            v: v,
        };
    });

    return newState;
}

export function LampToHex(lamp) {
    return convert.hsv.hex(lamp.h, lamp.s, lamp.v);
}

export function SavePreset(lamps) {
    // TODO give ability to name it and provide description
    let id = Math.round(Math.random() * 1000);

    let newPreset = {
        name: `Preset ${id}`,
        description: `A randomly generated description! ${id}`,
        lamps: lamps,
    };

    document.cookie = `preset.${id}=${JSON.stringify(newPreset)}`; // TODO expiration date
}

export function LoadPresets() {
    let cookies = document.cookie.split(";").filter(str => {
        return str.trim().slice(0, 6) === "preset"; // TODO just use a standard library instead of this garbage
    });

    let presets = [];

    cookies.forEach(cookie => {
        try {
            let p = JSON.parse(cookie.slice(cookie.indexOf("=") + 1));
            presets.push(p); // TODO validate these
        } catch (e) {
            console.log(`Malformed preset: ${e}`);
        }
    });

    console.log(presets);

    return presets;
}

export function TestPreset() {
    return testPreset;
}

const testPreset = {
    name: "testPreset",
    description:
        "A preset showcasing the format of preset objects. If a description is very long, it does something I assume! How long does it have to be before it gets really strange, that is an important question. Maybe a lorem ipsum would make more sense honestly",
    lamps: [
        {
            id: 0,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 1,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 2,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 3,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 4,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 5,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 6,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
        {
            id: 7,
            power: true,
            h: 184,
            s: 96,
            v: 85,
        },
    ],
};
