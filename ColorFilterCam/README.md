Messing around with the camera. Tried to simplify the camera code but it is super sluggish and still complicated.

The problem was intercepting each frame to do the face detection. This makes it slow and violates the usual camera workflow where the user presses a button.

This handles rotation.  I tried to do without the preview screen, but some devices require it. I suppose it could be shrunk to 1px.   

It also has some simple color filter code.

