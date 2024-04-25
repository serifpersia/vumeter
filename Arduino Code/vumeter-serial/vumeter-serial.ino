#include <FastLED.h>

int audioLevel = 0;

// Define LED settings
#define LED_PIN 18
#define NUM_LEDS 176
CRGB leds[NUM_LEDS];

void setup() {
  Serial.begin(115200);

  FastLED.addLeds<WS2812B, LED_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setMaxPowerInVoltsAndMilliamps(5, 400);

  // CLEAR LEDS
  for (int i = 0; i < NUM_LEDS; i++)
    leds[i] = CRGB(0, 0, 0);
  FastLED.show();

}

void loop() {
  receiveSerialData();
  vuMeter();
}

void receiveSerialData() {
  int bufferSize = Serial.available();
  if (bufferSize >= 2) {
    byte buffer[bufferSize];
    Serial.readBytes(buffer, bufferSize);

    audioLevel = buffer[0] | (buffer[1] << 8);
  }
}

void vuMeter() {
  int numLEDsToLight = map(audioLevel, 0, 255, 0, NUM_LEDS);
  int centerIndex = NUM_LEDS / 2;

  static int prevNumLEDsToLight = -1;
  if (numLEDsToLight != prevNumLEDsToLight) {
    prevNumLEDsToLight = numLEDsToLight;

    // Update only the changed LEDs
    for (int i = 0; i < NUM_LEDS; i++) {
      int distanceFromCenter = abs(i - centerIndex);

      if (audioLevel > 0 && distanceFromCenter <= numLEDsToLight / 2) {
        int brightness = map(distanceFromCenter, 0, numLEDsToLight / 2, 255, 0);
        leds[i] = CRGB(brightness, 0, 0);
      } else {
        leds[i] = CRGB::Black;
      }
    }
    FastLED.show();
  }
}
