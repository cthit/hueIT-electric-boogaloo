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

  console.log(`sending:`)
  console.log(bodyList)

  Axios({
    method: "post",
    url: url,
    body: {
      "requestBodyList": {bodyList}
    }
  }).then(function (response) {
    console.log(response)
  })
}