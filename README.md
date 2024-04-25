<div align="center">
   
![image](https://github.com/serifpersia/vumeter/assets/62844718/aec608e7-3cd4-451a-91a8-4958d8f56ad0)

<h1><span class="piano-text" style="color: white;">VuMeter</span>

[![Release](https://img.shields.io/github/release/serifpersia/pianolux-esp32.svg?style=flat-square)](https://github.com/serifpersia/vumeter/releases)
[![License](https://img.shields.io/github/license/serifpersia/pianolux-esp32?color=blue&style=flat-square)](https://raw.githubusercontent.com/serifpersia/pianolux-esp32/master/LICENSE)
[![Discord](https://img.shields.io/discord/1077195120950120458.svg?colorB=blue&label=discord&style=flat-square)](https://discord.gg/MAypyD7k86)
</div>

## Demo
<div align="center">
   TODO:
</div>



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
