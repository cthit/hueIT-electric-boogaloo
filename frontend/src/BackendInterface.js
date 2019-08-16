import Axios from "axios";
import { TestPreset } from "./Util";

let url = "http://localhost:8080";

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

    Axios.post(url, {
        requestBodyList: { bodyList },
    }).then(function(response) {
        console.log("Response:");
        console.log(response);
    });
}

export function Get() {
    console.warn("Get unimplemented");
    return TestPreset().lamps;
}
