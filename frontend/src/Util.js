let convert = require("color-convert");
let cookie = require("cookie");

// given a state and a list of affected indices,
// returns a new state where the color is applied
// to those lamps
export function ApplyColor(lamps, color, indices) {
    return lamps.map(lamp => {
        if (indices.includes(lamp.id)) {
            let [h, s, v] = convert.hex.hsv(color);
            return {
                id: lamp.id,
                power: lamp.power,
                h: h,
                s: s,
                v: v,
            };
        } else return lamp;
    });
}

export function ApplyColorToAll(lamps, color) {
    return lamps.map(lamp => {
        let [h, s, v] = convert.hex.hsv(color);
        return {
            id: lamp.id,
            power: lamp.power,
            h: h,
            s: s,
            v: v,
        };
    });
}

export function LampToHex(lamp) {
    return convert.hsv.hex(lamp.h, lamp.s, lamp.v);
}

export function SavePreset(lamps) {
    console.log("lamps");
    console.log(lamps);
    // TODO give ability to name it and provide description
    let id = Math.round(Math.random() * 1000);

    let newPreset = {
        name: `Preset ${id}`,
        description: `A randomly generated description! ${id}`,
        lamps: lamps,
    };

    console.log("newPreset");
    console.log(newPreset);

    document.cookie = `preset.${id}=${JSON.stringify(newPreset)}`; // TODO expiration date?
}

export function LoadPresets() {
    let cookies = cookie.parse(document.cookie);

    let presets = [];

    function isPreset(p) {
        return (
            p.hasOwnProperty("name") &&
            p.hasOwnProperty("description") &&
            p.hasOwnProperty("lamps")
        );
    }

    for (const c in cookies) {
        try {
            let p = JSON.parse(cookies[c]);
            if (isPreset(p)) presets.push(p);
        } catch (e) {
            console.log(`Malformed preset: ${e}`);
        }
    }
    return presets;
}

export const testPreset = {
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
