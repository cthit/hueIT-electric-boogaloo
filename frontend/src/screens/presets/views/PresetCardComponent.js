import React from "react";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import Card from "@material-ui/core/Card";
import CardActionArea from "@material-ui/core/CardActionArea";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import Container from "@material-ui/core/Container";
import DeleteButton from "@material-ui/icons/Delete";
import { DeletePreset, LampToHex } from "../../../common/Util";
import IconButton from "@material-ui/core/IconButton";
import Grid from "@material-ui/core/Grid";
import Box from "@material-ui/core/Box";

export default function PresetCardComponent(props) {
    const { preset, setLamps, parentForceUpdate } = props;

    function handleClickApply() {
        setLamps(preset.lamps);
    }

    function handleClickDelete() {
        // This is a funky solution that forces a rerender when the cookie for the preset is deleted
        // (as react does not consider cookies a part of the state)
        DeletePreset(preset);
        parentForceUpdate();
    }

    return (
        <Container>
            <Card>
                <CardActionArea onClick={handleClickApply}>
                    <CardContent>
                        <Typography
                            gutterBottom
                            variant="h5"
                            component="h2"
                            onClick={() => handleClickApply()}
                        >
                            {preset.name}
                        </Typography>
                        <LampComponent col={4} row={2} {...props} />
                    </CardContent>
                </CardActionArea>
                <CardActions
                    style={{ display: "flex", justifyContent: "space-between" }}
                >
                    <Button
                        size="medium"
                        color="primary"
                        onClick={handleClickApply}
                    >
                        Apply
                    </Button>
                    <IconButton
                        size="medium"
                        color="secondary"
                        onClick={handleClickDelete}
                    >
                        <DeleteButton />
                    </IconButton>
                </CardActions>
            </Card>
        </Container>
    );
}

function LampComponent(props) {
    const { preset, col, row } = props;

    const lamps = preset.lamps;

    const indexList = [];
    for (let i = 0; i < row; i++) indexList.push([]);

    lamps.forEach((lamp, index) => indexList[index % row].push(index));

    let lampWidth = "60px";
    if (window.screen.availWidth < 800) {
        lampWidth = window.screen.availWidth * 0.1;
    }

    return indexList.map((row, i) => {
        return (
            <Container>
                <Grid container key={i} spacing={2}>
                    {row.map(idx => {
                        return (
                            <Grid item key={idx}>
                                <Box
                                    style={{
                                        backgroundColor: `#${LampToHex(
                                            lamps[idx]
                                        )}`,
                                        width: lampWidth,
                                        paddingTop: "100%",
                                        borderRadius: "100%",
                                    }}
                                    key={idx}
                                    disabled={true}
                                    children=""
                                />
                            </Grid>
                        );
                    })}
                </Grid>
            </Container>
        );
    });
}
