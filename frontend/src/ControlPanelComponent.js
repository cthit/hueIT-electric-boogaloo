import Grid from "@material-ui/core/Grid";
import ChromePicker from "react-color";
import Box from "@material-ui/core/Box";
import Button from "@material-ui/core/Button";
import { ApplyColorToAll, SavePreset } from "./Util";
import React from "react";
import UndoIcon from "@material-ui/icons/Undo";
import SaveIcon from "@material-ui/icons/Save";
import Paper from "@material-ui/core/Paper";
import IconButton from "@material-ui/core/IconButton";
import { Container } from "@material-ui/core";

export default function ControlPanelComponent(props) {
    const { state, setState, handleUndo, disableUndo } = props;

    return (
        <Paper style={{ padding: "20px", justifyContent: "center" }}>
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
                alignItems={"center"}
                spacing={1}
                style={{ padding: "10px" }}
            >
                <Grid item>
                    <IconButton
                        color="primary"
                        onClick={handleUndo}
                        disabled={disableUndo}
                    >
                        <UndoIcon />
                    </IconButton>
                </Grid>
                <Grid item>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={() => {
                            setState(ApplyColorToAll(state)); // TODO consider lifting this and save functions
                        }}
                    >
                        Apply to All
                    </Button>
                </Grid>
                <Grid item>
                    <IconButton
                        color="primary"
                        onClick={() => {
                            SavePreset(state.lamps);
                        }}
                    >
                        <SaveIcon />
                    </IconButton>
                </Grid>
            </Grid>
        </Paper>
    );
}
