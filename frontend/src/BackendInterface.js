import Axios from "axios";
import { testPreset } from "./Util";

let url = "http://localhost:8080/list";

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

export function Get() {
    console.warn("Get unimplemented");
    return testPreset.lamps;
}
