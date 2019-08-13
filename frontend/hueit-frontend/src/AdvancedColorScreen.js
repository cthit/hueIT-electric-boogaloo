import React from 'react'
import ChromePicker from 'react-color'
import Grid from "@material-ui/core/Grid";
import {Container} from "@material-ui/core";
import LampLayoutComponent from "./LampLayoutComponent";
import Button from "@material-ui/core/Button";
import Box from "@material-ui/core/Box";
import {ApplyColor, ApplyColorToAll} from "./Util";


export default function AdvancedColorScreen(props) {

  const {state, setState, ...children} = props;

  return (
    <Container>
      <Grid container>
        <Grid item>
          <Grid container>
            <ChromePicker
              disableAlpha
              color={state.color}
              onChangeComplete={
                (color) => {
                  let newState = Object.assign({}, state);
                  newState.color = color.hex;
                  setState(newState);
                }
              }/>
          </Grid>
          <Box component="span"/>
          <Grid container>
            <Container>
              <Button
                variant="contained"
                color="primary"
                onClick={() => {
                  setState(ApplyColorToAll(state))
                }}
              >
                Apply to all
              </Button>
            </Container>
          </Grid>
        </Grid>
        <Grid item>
          <LampLayoutComponent {...props} columns={2} rows={4}/>
        </Grid>
      </Grid>
    </Container>
  )
}

