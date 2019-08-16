import React from "react";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import Card from "@material-ui/core/Card";
import CardActionArea from "@material-ui/core/CardActionArea";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import Container from "@material-ui/core/Container";
import Fab from "@material-ui/core/Fab";
import { LampToHex } from "./Util";

export default function PresetCardComponent(props) {
    const { preset, state, setState } = props;

    function handleClick() {
        let newState = {};
        newState.lamps = preset.lamps;
        newState.color = state.color;
        setState(newState);
    }

    return (
        <Container maxWidth="md">
            <Card>
                <CardActionArea onClick={handleClick}>
                    <CardContent>
                        <Typography gutterBottom variant="h5" component="h2">
                            {preset.name}
                        </Typography>
                        <GhettoPreview {...props} />
                        {/*<Typo  graphy variant="body2" color="textSecondary" component="p"*/}
                        {/*            style={{maxHeight: "100px"}}>*/}
                        {/*  {preset.description}*/}
                        {/*</Typography>*/}
                    </CardContent>
                </CardActionArea>
                <CardActions>
                    <Button size="medium" color="primary" onClick={handleClick}>
                        Apply
                    </Button>
                </CardActions>
            </Card>
        </Container>
    );
}

function GhettoPreview(props) {
    const { preset } = props;

    return (
        <Container>
            {preset.lamps.map((lamp, index) => {
                return (
                    <Fab
                        style={{
                            backgroundColor: `#${LampToHex(lamp)}`,
                        }}
                        key={index}
                        disabled={true}
                        children=""
                    />
                );
            })}
        </Container>
    );
}
