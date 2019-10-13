import React, { useCallback } from "react";
import Grid from "@material-ui/core/Grid";
import PresetCardComponent from "./views/PresetCardComponent";
import { LoadPresets } from "../../common/Util";
import { Container } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";

export default function PresetsScreen(props) {
    const { ...other } = props;

    const [, updateState] = React.useState();
    const forceUpdate = useCallback(() => updateState({}), []);

    let presets = DefaultPresets().concat(LoadPresets());

    return presets.length > 0 ? (
        <Grid container spacing={3} style={{margin:"0px"}}>
            {presets.map((p, index) => (
                <Grid item key={index}>
                    <PresetCardComponent
                        {...other}
                        parentForceUpdate={forceUpdate}
                        preset={p}
                    />
                </Grid>
            ))}
        </Grid>
    ) : (
        <Container>
            <Typography variant={"h4"}>
                No presets here! You can make and save them in the customize
                tab.
            </Typography>
        </Container>
    );
}

function DefaultPresets() {
    return [];
}
