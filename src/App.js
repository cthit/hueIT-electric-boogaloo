import React from 'react';
import logo from './logo.svg';
import './App.css';

import {ChromePicker} from 'react-color';

class RootView extends React.Component {

  handleChangeComplete(color, event) {
    alert("color set to : " + color.hex)
  }

  render() {
    return (
      <ChromePicker
        color='#09CCDA'
        onChangeComplete={this.handleChangeComplete}/>
    )
  }
}


function App() {
  return (
    <div className="App">
      <RootView/>
    </div>
  );
}

export default App;
