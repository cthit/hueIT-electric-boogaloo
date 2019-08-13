import React from 'react';

import Container from '@material-ui/core/Container';
import ChromePicker from 'react-color';
import Typography from '@material-ui/core/Typography';
import {ApplyColor} from './Util.js';


export default function BasicColorScreen(props) {

  const {children, color, onColorChangeComplete, ...other} = props;

  return (
    <React.Fragment>
      <Container>
        <ChromePicker
          disableAlpha
          color={color}
          onChangeComplete={onColorChangeComplete}
        />
      </Container>
    </React.Fragment>
  )
}