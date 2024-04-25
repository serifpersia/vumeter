#include <FastLED.h>

int audioLevel = 0;
int decay = 0;
int decay_check = 0;
long pre_audio = 0;
long audio = 0;

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

  FastLED.show();

  decay_check++;
  if (decay_check > decay)
  {
    decay_check = 0;
    if (audio > 0)
      audio -= 5;
  }
}

void receiveSerialData() {
  int bufferSize = Serial.available();
  if (bufferSize >= 2) {
    byte buffer[bufferSize];
    Serial.readBytes(buffer, bufferSize);

    audioLevel = buffer[0] | (buffer[1] << 8);
  }
}

void vuMeter()
{
  pre_audio = ((long)NUM_LEDS * (long)audioLevel) / 256L;

  if (pre_audio > audio)
    audio = pre_audio;

  int start = (NUM_LEDS - audio) / 2;

  for (int i = 0; i < NUM_LEDS; i++) {
    if (i >= start && i < start + audio)
      leds[i] = CHSV(0, 255, 255);
    else
      leds[i] = CHSV(0, 0, 0);
  }
}
