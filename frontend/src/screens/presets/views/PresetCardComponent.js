import React from "react";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import Card from "@material-ui/core/Card";
import CardActionArea from "@material-ui/core/CardActionArea";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import Container from "@material-ui/core/Container";
import Fab from "@material-ui/core/Fab";
import DeleteButton from "@material-ui/icons/Delete";
import {DeletePreset, LampToHex} from "../../../common/Util";
import IconButton from "@material-ui/core/IconButton";

export default function PresetCardComponent(props) {
    const {preset, setLamps, parentForceUpdate} = props;

    function handleClickApply() {
        setLamps(preset.lamps);
    }

    function handleClickDelete() {
        DeletePreset(preset); // TODO force a render somehow to delete
        console.log("noja print");
        parentForceUpdate();
    }

    return (
      <Container maxWidth="md">
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
                      <GhettoPreview {...props} />
                  </CardContent>
              </CardActionArea>
              <CardActions
                style={{display: "flex", justifyContent: "space-between"}}
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
                      <DeleteButton/>
                  </IconButton>
              </CardActions>
          </Card>
      </Container>
    );
}

function GhettoPreview(props) {
    const {preset} = props;

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
