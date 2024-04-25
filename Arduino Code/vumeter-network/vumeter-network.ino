#include <WiFi.h>
#include <WiFiUdp.h>
#include <FastLED.h>

const char *ssid = "your-wifi";
const char *password = "your-wifi-password";
const int udpPort = 12345;

WiFiUDP udp;
int audioLevel = 0;

// Define LED settings
#define LED_PIN 18
#define NUM_LEDS 176
CRGB leds[NUM_LEDS];

void setup() {
  Serial.begin(115200);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("WiFi connected!");

  FastLED.addLeds<WS2812B, LED_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setMaxPowerInVoltsAndMilliamps(5, 200);

  // CLEAR LEDS
  for (int i = 0; i < NUM_LEDS; i++)
    leds[i] = CRGB(0, 0, 0);
  FastLED.show();

  udp.begin(udpPort);
}

void loop() {
  receiveUDPData();
  vuMeter();
}

void receiveUDPData() {
  int packetSize = udp.parsePacket();
  if (packetSize) {
    byte packetBuffer[packetSize];
    udp.read(packetBuffer, packetSize);

    switch (packetSize) {
      case 11:
        sendESP32IPInfo();
        break;
      default:
        processPacket(packetBuffer);
        break;
    }
  }
}

void processPacket(byte* data) {
  audioLevel = data[0];
}

void sendESP32IPInfo() {
  IPAddress localIP = WiFi.localIP();

  byte responseBuffer[4]; // Assuming IPv4 address
  responseBuffer[0] = localIP[0];
  responseBuffer[1] = localIP[1];
  responseBuffer[2] = localIP[2];
  responseBuffer[3] = localIP[3];

  udp.beginPacket(udp.remoteIP(), udp.remotePort());
  udp.write(responseBuffer, 4); // Assuming IPv4 address
  udp.endPacket();
}

void vuMeter() {
  int numLEDsToLight = map(audioLevel, 0, 255, 0, NUM_LEDS);
  int centerIndex = NUM_LEDS / 2;

  static int prevNumLEDsToLight = -1;
  if (numLEDsToLight != prevNumLEDsToLight) {
    prevNumLEDsToLight = numLEDsToLight;

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
