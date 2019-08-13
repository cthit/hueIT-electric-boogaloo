import React from 'react'
import Grid from "@material-ui/core/Grid";
import PresetCardComponent from "./PresetCardComponent";

export default function PresetsScreen(props) {

  const {presets,} = props;

  return (
    <Grid container spacing={3}>
      {presets.map((p, index) =>
        <Grid item xs={3} key={index}>
          <PresetCardComponent preset={p}/>
        </Grid>
      )}
    </Grid>)
}