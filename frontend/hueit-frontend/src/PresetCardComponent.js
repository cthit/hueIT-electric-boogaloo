import React from 'react'
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import Card from "@material-ui/core/Card";
import CardActionArea from "@material-ui/core/CardActionArea";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import Container from "@material-ui/core/Container";

export default function PresetCardComponent(props) {

  const {preset} = props

  return (
    <Container maxWidth="md">
      <Card>
        <CardActionArea>
          <CardContent>
            <Typography gutterBottom variant="h5" component="h2">
              {preset.name}
            </Typography>
            <Typography variant="body2" color="textSecondary" component="p">
              {preset.description}
            </Typography>
          </CardContent>
        </CardActionArea>
        <CardActions>
          <Button size="medium" color="primary">
            Apply
          </Button>
        </CardActions>
      </Card>
    </Container>
  )
}