import Axios from "axios";

let url = 'http://localhost:8080';

export default function ApplyToBackend(state) {

  let bodyList = [];

  state.lamps.forEach(function (lamp, idx) {
    bodyList.push({
      "isGroup": false,
      "id": lamp.id,
      "props": {
        "hue": lamp.h,
        "sat": lamp.s,
        "bri": lamp.v,
        "pwr": lamp.power,
      }
    })
  });

  Axios.post(
    url,
    {
      "requestBodyList": {bodyList}
    }
  ).then(function (response) {
    console.log("Response:")
    console.log(response)
  })
}