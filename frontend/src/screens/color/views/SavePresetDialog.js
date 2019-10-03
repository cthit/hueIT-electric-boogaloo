import React from "react";
import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText";
import DialogTitle from "@material-ui/core/DialogTitle";
import { SavePreset } from "../../../common/Util";

export default function SavePresetDialog(props) {
    const { lamps, open, setOpen } = props;

    const [name, setName] = React.useState("");

    function handleClose() {
        console.log(setOpen);
        setOpen(false);
    }

    return (
        <div>
            <Dialog
                open={open}
                onClose={handleClose}
                aria-labelledby="form-dialog-title"
            >
                <DialogTitle id="form-dialog-title">Save preset</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Enter a name for your preset. Once saved, it can be
                        found in the 'Presets' tab.
                    </DialogContentText>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="preset-name"
                        label="Name"
                        type="name"
                        fullWidth
                        onChange={evt => {
                            setName(evt.target.value);
                        }}
                        error={name.length === 0}
                        helperText={name.length === 0 ? "Cannot be empty" : ""}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color="primary">
                        Cancel
                    </Button>
                    <Button
                        disabled={name.length === 0}
                        onClick={() => {
                            SavePreset(lamps, name);
                            handleClose();
                        }}
                        color="primary"
                    >
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
