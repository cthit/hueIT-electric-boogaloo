import Axios from "axios";
import { testPreset } from "./Util";

let url = "http://localhost:8080/list";

// general methods for interacting with the backend

// Send a request to the backend to set the state of the lamps
export function Post(lamps) {
    let bodyList = [];

    lamps.forEach(function(lamp, idx) {
        bodyList.push({
            isGroup: false,
            id: lamp.id,
            props: {
                hue: lamp.h,
                sat: lamp.s,
                bri: lamp.v,
                pwr: lamp.power,
            },
        });
    });

    let body = {
        requestBodyList: bodyList,
    };

    Axios.post(url, body)
        .then(function(response) {
            console.log(response);
        })
        .catch(function(error) {
            console.log(error);
        });
}

// Send a get request and return the current state of the lamps
export function Get() {
    console.warn("Get unimplemented");
    return testPreset.lamps;
}
