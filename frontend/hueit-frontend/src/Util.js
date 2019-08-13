let convert = require('color-convert');

// given a state and a list of affected indices,
// returns a new state where the color is applied
// to those lamps
export function ApplyColor(state, indices) {
  let newState = {}

  newState.lamps = state.lamps.map(lamp => {
    if (indices.includes(lamp.id)) {
      let [h, s, v] = convert.hex.hsv(state.color);
      return {
        "id": lamp.id,
        "power": lamp.power,
        "h": h,
        "s": s,
        "v": v,
      }
    } else return lamp
  });

  newState.color = state.color;

  return newState;
}

export function LampToHex(lamp) {
  return convert.hsv.hex(
    lamp.h,
    lamp.s,
    lamp.v)
}

export function TestPreset() {
  return testPreset
}

const testPreset = {
  "name": "testPreset",
  "description": "A preset showcasing the format of preset objects. If a description is very long, it does something I assume!",
  "value": [
    {
      "id": 0,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 1,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 2,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 3,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 4,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 5,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 6,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
    {
      "id": 7,
      "power": true,
      "h": 25,
      "s": 128,
      "v": 128,
    },
  ]
}