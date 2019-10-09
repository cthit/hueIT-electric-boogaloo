import React, { useState } from "react";
import Paper from "@material-ui/core/Paper";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import Box from "@material-ui/core/Box";
import ColorScreen from "../../screens/color/ColorScreen";
import PresetsScreen from "../../screens/presets/PresetsScreen";

export default function Navigation(props) {
    const [activeTab, setActiveTab] = useState(0); // TODO retain state between sessions

    function handleChange(event, newValue) {
        setActiveTab(newValue);
    }

    return (
        <Box>
            <Paper square elevation={4}>
                <Tabs
                    value={activeTab}
                    centered
                    indicatorColor="primary"
                    textColor="primary"
                    onChange={handleChange}
                >
                    <Tab label="Customize" />
                    <Tab label="Presets" />
                </Tabs>
            </Paper>
            <TabPane value={activeTab} index={0}>
                <ColorScreen {...props} />
            </TabPane>
            <TabPane value={activeTab} index={1}>
                <PresetsScreen {...props} />
            </TabPane>
        </Box>
    );
}

function TabPane(props) {
    const { children, value, index } = props;

    return (
        <Box p={3} hidden={value !== index}>
            {children}
        </Box>
    );
}
