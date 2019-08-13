import React, {useState} from 'react';
import Paper from '@material-ui/core/Paper';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import BasicColorScreen from "./BasicColorScreen";
import {LampToHex, TestPreset} from "./Util";
import AdvancedColorScreen from "./AdvancedColorScreen";
import PresetsScreen from "./PresetsScreen";

export default function Navigation(props) {

  const {children, state, setState, onColorChangeComplete} = props;

  const [value, setValue] = React.useState(0);

  function handleChange(event, newValue) {
    setValue(newValue);
  }

  return (
    <div>
      <Paper square elevation={4}>
        <Tabs
          value={value}
          centered
          indicatorColor="primary"
          textColor="primary"
          onChange={handleChange}
        >
          <Tab label="Customize"/>
          <Tab label="Presets"/>
        </Tabs>
      </Paper>
      <TabPane value={value} index={0}>
        <AdvancedColorScreen {...props}/>
      </TabPane>
      <TabPane value={value} index={1}>
        <PresetsScreen {...props} presets={[
          TestPreset(),
          TestPreset(),
          TestPreset(),
          TestPreset(),
          TestPreset(),
          TestPreset(),
          TestPreset(),
        ]}/>
      </TabPane>

    </div>
  );
}

function TabPane(props) {

  const {children, value, index, ...other} = props;

  return (
    <Box p={3}
         hidden={value !== index}
    >
      {children}
    </Box>
  )
}