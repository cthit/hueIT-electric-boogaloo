import React from "react";
import Grid from "@material-ui/core/Grid";
import { Container } from "@material-ui/core";
import LampLayoutComponent from "./LampLayoutComponent";
import ControlPanelComponent from "./ControlPanelComponent";

export default function AdvancedColorScreen(props) {
    return (
        <Container>
            <Grid container>
                <Grid item>
                    <ControlPanelComponent {...props} />
                </Grid>
                <Grid item>
                    <LampLayoutComponent {...props} columns={2} rows={4} />
                </Grid>
            </Grid>
        </Container>
    );
}
