import React from 'react';
import './LampView.css';

export class LampView extends React.Component {

  circleStyle = function (color, size) {
    return {
      height: size,
      width: size,
      backgroundColor: color,
      borderRadius: "50%",
      display: "inline-block",
      margin: "5px",
    }
  };

  render() {
    console.log(this.circleStyle(this.props.color));
    const border = (this.props.selected) ? "solid" : "hidden";
    return (
      <div className="lamp-view">
        <div style={{
          borderStyle: border,
          borderRadius: "8px",
          borderColor: "#aaa",
          margin: "3px",
        }}>
          <div className="lamp-circle" style={this.circleStyle(
            this.props.color,
            "100px")}/>
        </div>
      </div>
    )
  }
}


