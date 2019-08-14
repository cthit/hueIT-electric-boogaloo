import React from 'react'
import Grid from "@material-ui/core/Grid";
import PresetCardComponent from "./PresetCardComponent";

export default function PresetsScreen(props) {

  const {presets, ...other} = props;

  return (
    <Grid container spacing={3}>
      {presets.map((p, index) =>
        <Grid item xs={3} key={index}>
          <PresetCardComponent {...other} preset={p}/>
        </Grid>
      )}
    </Grid>)
}