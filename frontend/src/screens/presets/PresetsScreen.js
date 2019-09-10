import React, { useCallback } from "react";
import Grid from "@material-ui/core/Grid";
import PresetCardComponent from "./PresetCardComponent";
import { LoadPresets } from "./Util";

export default function PresetsScreen(props) {
    const { ...other } = props;

    const [, updateState] = React.useState();
    const forceUpdate = useCallback(() => updateState({}), []);

    let presets = DefaultPresets().concat(LoadPresets());

    return (
        <Grid container spacing={3}>
            {presets.map((p, index) => (
                <Grid item xs={3} key={index}>
                    <PresetCardComponent
                        {...other}
                        parentForceUpdate={forceUpdate}
                        preset={p}
                    />
                </Grid>
            ))}
        </Grid>
    );
}

function DefaultPresets() {
    return [];
}
