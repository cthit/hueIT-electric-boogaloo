import React from 'react';
import './App.css';
import Axios from 'axios';

import {ChromePicker} from 'react-color';

const url = "http://localhost:8080/";

class App extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      currentColor: "#09CCDA",
    }
  }

  handleChangeComplete = (color) => {
    let obj = {
      "isGroup": true,
      "id": 1,
      "props": [
        {
          "key": "hue",
          "value": color.hsv.h,
        },
        {
          "key": "sat",
          "value": color.hsv.s * 100,
        },
        {
          "key": "bri",
          "value": color.hsv.v * 100,
        },
      ]
    };
    console.log(obj);
    Axios({
        method: "post",
        url: url,
        data: obj
      }
    ).then(function (response) {
      //console.log(response)
    })
  };

  render() {
    return (
      <div className="flex-container">
        <div>
          <ChromePicker
            color={this.state.currentColor}
            onChangeComplete={this.handleChangeComplete}
          />
        </div>
      </div>
    );
  }
}

export default App;
