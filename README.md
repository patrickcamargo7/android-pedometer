# android-pedometer

### Requirements:

1. Count steps in 2 orthogonal directions
2. Button to reset steps
3. Calibration
4. Low error rate

### Components/Checklist:

- [ ] Pedometer: steps taken - *Pierre*
- [ ] Orientation: direction of steps
  - [x] Get acc + mag sensor combo working together to produce an azimuth - *Tian*
  - [ ] Calculate the North and East components of each step taken *Tian*
  - [ ] Smooth the sensors - *Pierre*
- [ ] Map - *Tian*
- [x] Reset button - *Nelson*
- [x] Calibration button/popup - disables steps - *Nelson*

### Implementation:
- Step detection code will be carried over from previous lab
- Each time step is updated a method will be called to update the North and East components of the step, from sin(theta) and cos(theta), theta value obtained from orientation sensor

## Buttons:
  - reset: changes all steps to 0
  - calibration: disables step registering, show popup dialog to rotate phone
