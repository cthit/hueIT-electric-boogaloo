import React, {useState} from 'react';
import Paper from '@material-ui/core/Paper';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';
import {TestPreset} from "./Util";
import AdvancedColorScreen from "./AdvancedColorScreen";
import PresetsScreen from "./PresetsScreen";

export default function Navigation(props) {

  const {children, ...other} = props;

  const [value, setValue] = useState(1);

  function handleChange(event, newValue) {
    setValue(newValue);
  }

  return (
    <React.Fragment>
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
        <PresetsScreen {...props}/>
      </TabPane>
    </React.Fragment>
  );
}

function TabPane(props) {

  const {children, value, index} = props;

  return (
    <Box p={3}
         hidden={value !== index}
    >
      {children}
    </Box>
  )
}