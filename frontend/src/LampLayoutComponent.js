import React from 'react'
import Grid from "@material-ui/core/Grid";
import Fab from "@material-ui/core/Fab";
import {Container} from "@material-ui/core";
import {LampToHex, ApplyColor} from "./Util";

export default function LampLayoutComponent(props) {
  const {state, setState, columns,} = props;

  let rowList = [];

  for (let i = 0; i < state.lamps.length; i += columns) {
    rowList.push(state.lamps.slice(i, i + columns))
  }

  return (
    <Container>
      {rowList.map((row, index) =>
        MakeRow(row, state, setState, index)
      )}
    </Container>
  )
}


function MakeRow(lamps, state, setState, index = 0) {
  let list = lamps.map(lamp => {
    return <Fab
      key={lamp.id}
      onClick={() => {
        setState(ApplyColor(state, [lamp.id]))
      }}
      style={{
        backgroundColor: `#${LampToHex(lamp)}`,
      }}
      children=""     // fabs really want children
    />
  });

  return (<Grid container key={index}> {list}</Grid>)
}
