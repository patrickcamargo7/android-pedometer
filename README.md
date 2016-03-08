# android-pedometer

### Requirements:

1. Count steps in 2 orthogonal directions
2. Button to reset steps
3. Calibration
4. Low error rate

### Components/Checklist:

- [x] Pedometer: steps taken - *Pierre*
- [x] Orientation: direction of steps
  - [x] Get acc + mag sensor combo working together to produce an azimuth - *Tian*
  - [x] Calculate the North and East components of each step taken - *Tian*
  - [x] Smooth the sensors - *Pierre*
- [x] Map - *Tian*
- [x] Reset button - *Nelson*
- [x] Calibration button/popup - disables steps - *Nelson*
- [ ] (Optional) Pause step counting when a high angular velocity is detected to reduce false postives.

### Implementation:
- Step detection code will be carried over from previous lab
- Each time step is updated a method will be called to update the North and East components of the step, from sin(theta) and cos(theta), theta value obtained from orientation sensor

## Buttons:
  - reset: changes all steps to 0
  - calibration: disables step registering, show popup dialog to rotate phone
