import Axios from "axios";
import { testPreset } from "./Util";

//let base_url = "http://localhost:8080";
const base_url = process.env.REACT_APP_BACKEND_URL;

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

    console.log("request @:" + base_url + "/list");
    Axios.post(base_url + "/list", body)
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
