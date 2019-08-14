import Grid from "@material-ui/core/Grid";
import ChromePicker from "react-color";
import Box from "@material-ui/core/Box";
import { Container } from "@material-ui/core";
import Button from "@material-ui/core/Button";
import { ApplyColorToAll, SavePreset } from "./Util";
import React from "react";

export default function ControlPanelComponent(props) {
    const { state, setState, ...other } = props;

    return (
        <React.Fragment>
            <Grid container>
                <ChromePicker
                    disableAlpha
                    color={state.color}
                    onChangeComplete={color => {
                        let newState = Object.assign({}, state);
                        newState.color = color.hex;
                        setState(newState);
                    }}
                />
            </Grid>
            <Box component="span" />
            <Grid
                container
                direction={"column"}
                alignItems={"center"}
                spacing={1}
                style={{ padding: "10px" }}
            >
                <Grid item>
                    <Button
                        variant="contained"
                        color="primary"
                        style={{ width: "160px" }}
                        onClick={() => {
                            setState(ApplyColorToAll(state));
                        }}
                    >
                        Apply to All
                    </Button>
                </Grid>
                <Grid item>
                    <Button
                        variant="contained"
                        color="primary"
                        style={{ width: "160px" }}
                        onClick={() => {
                            SavePreset(state.lamps);
                        }}
                    >
                        Save as Preset
                    </Button>
                </Grid>
            </Grid>
        </React.Fragment>
    );
}
