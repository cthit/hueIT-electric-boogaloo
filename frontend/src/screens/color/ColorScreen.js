import React from "react";
import Grid from "@material-ui/core/Grid";
import { Container } from "@material-ui/core";
import ControlPanelComponent from "./views/ControlPanelComponent";
import LampLayoutComponent from "./views/LampLayoutComponent";

export default function ColorScreen(props) {
    return (
        <Container>
            <Grid container>
                <Grid item>
                    <ControlPanelComponent {...props} />
                </Grid>
                <Grid item>
                    <Container>
                        <LampLayoutComponent {...props} columns={2} rows={4} />
                    </Container>
                </Grid>
            </Grid>
        </Container>
    );
}
