import React from 'react'
import Grid from "@material-ui/core/Grid";
import Fab from "@material-ui/core/Fab";
import {Container} from "@material-ui/core";
import {LampToHex, ApplyColor} from "./Util";

export default function LampLayoutComponent(props) {
  const {state, setState, columns, rows, ...children} = props;

  let rowList = [];

  for (let i = 0; i < state.lamps.length; i += columns) {
    rowList.push(state.lamps.slice(i, i + columns))
  }

  return (
    <Container>
      {rowList.map(row =>
        MakeRow(row, state, setState)
      )}
    </Container>
  )
}


function MakeRow(lamps, state, setState) {
  let list = lamps.map(lamp => {
    return <Fab
      onClick={() => {
        setState(ApplyColor(state, [lamp.id]))
      }}
      style={{backgroundColor: `#${LampToHex(lamp)}`}
      }
    />
  });

  return (<Grid container> {list}</Grid>)
}
