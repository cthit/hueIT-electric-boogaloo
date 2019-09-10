import React from "react";
import Grid from "@material-ui/core/Grid";
import Fab from "@material-ui/core/Fab";
import { Container } from "@material-ui/core";
import Box from "@material-ui/core/Box";
import {ApplyColor, LampToHex} from "../../../common/Util";

export default function LampLayoutComponent(props) {
    const { lamps, setLamps, color, columns } = props;

    let rowList = [];

    for (let i = 0; i < lamps.length; i += columns) {
        rowList.push(lamps.slice(i, i + columns));
    }

    return (
        <Container>
            {rowList.map((row, index) =>
                MakeRow(row, lamps, setLamps, color, index)
            )}
        </Container>
    );
}

function MakeRow(lampsInRow, lamps, setLamps, color, index = 0) {
    let list = lampsInRow.map((lamp, index) => {
        return (
            <Box key={lamp.id} style={{ padding: "10px" }}>
                <Fab
                    onClick={() => {
                        setLamps(ApplyColor(lamps, color, [lamp.id]));
                    }}
                    style={{ backgroundColor: `#${LampToHex(lamp)}` }}
                    children=""
                />
            </Box>
        );
    });

    return (
        <Grid container key={index}>
            {list}
        </Grid>
    );
}
