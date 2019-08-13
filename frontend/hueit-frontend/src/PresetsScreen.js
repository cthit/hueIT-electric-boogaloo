import React from 'react'
import Grid from "@material-ui/core/Grid";
import PresetCardComponent from "./PresetCardComponent";

export default function PresetsScreen(props) {

  const {children, presets} = props;

  return (
    <Grid container spacing={3}>
      {presets.map(p =>
        <Grid item xs={3}>
          <PresetCardComponent preset={p}/>
        </Grid>
      )}
    </Grid>)
}