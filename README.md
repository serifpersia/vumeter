# VuMeter
Basic VuMeter Over UDP or Serial

## Usage

1. **Setup**
   - Install Java JRE 18 (OpenJRE or Temurin).
   - Install Arduino IDE and upload the appropriate sketch (network or serial) based on whether you want to send audio data over network or serial.
     - Network sketch supports ESP32; modify it for other network-capable boards.
   - Ensure correct network credentials.
   - Install FastLED library for compilation and upload.

2. **Running the Application**
   - Launch the `.jar` file.
   - By default, it's in network mode.
   - Press the button on the right to switch to Serial mode; the button text will change to reflect the connection type.
   - Press "Scan":
     - For network mode, it fills the IP field with the discovered IP.
     - For Serial mode, it populates the Serial dropdown (manually select the correct COM port).
   - Ensure the correct audio device is selected, along with the device you'll use to send audio data.
   - Press "Start".
   - The strip will reflect the audio level with a red light from the center, mirrored on both sides.

3. **Additional Notes**
   - For Windows users:
     - Enable StereoMix or use VoiceMeeter Banana to route audio to a virtual B2 channel, then use that device as the audio source in the Java application.
   - Other systems need alternative methods to use desktop sound as the audio source.

## LICENCE
This project is licensed under the [MIT License](LICENSE).
