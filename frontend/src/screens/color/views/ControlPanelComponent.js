import Grid from "@material-ui/core/Grid";
import ChromePicker from "react-color";
import Box from "@material-ui/core/Box";
import Button from "@material-ui/core/Button";
import { ApplyColorToAll } from "../../../common/Util";
import React from "react";
import UndoIcon from "@material-ui/icons/Undo";
import SaveIcon from "@material-ui/icons/Save";
import Paper from "@material-ui/core/Paper";
import IconButton from "@material-ui/core/IconButton";
import SavePresetDialog from "./SavePresetDialog";
import Container from "@material-ui/core/Container";

export default function ControlPanelComponent(props) {
    const { lamps, setLamps, color, setColor, handleUndo, disableUndo } = props;

    const [openDialog, setOpenDialog] = React.useState(false);

    return (
        <Paper style={{ padding: "20px", justifyContent: "center" }}>
            <SavePresetDialog
                lamps={lamps}
                open={openDialog}
                setOpen={setOpenDialog}
            />
            <Grid container>
                <Container>
                    <ChromePicker
                        disableAlpha
                        color={color}
                        onChangeComplete={color => {
                            setColor(color.hex);
                        }}
                    />
                </Container>
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
                            setLamps(ApplyColorToAll(lamps, color)); // TODO consider lifting this and save functions
                        }}
                    >
                        Apply to All
                    </Button>
                </Grid>
                <Grid item>
                    <IconButton
                        color="primary"
                        onClick={() => {
                            setOpenDialog(true);
                        }}
                    >
                        <SaveIcon />
                    </IconButton>
                </Grid>
            </Grid>
        </Paper>
    );
}
