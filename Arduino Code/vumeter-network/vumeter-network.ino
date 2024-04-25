#include <WiFi.h>
#include <WiFiUdp.h>
#include <FastLED.h>

const char *ssid = "your-wifi";
const char *password = "your-wifi-password";
const int udpPort = 12345;

WiFiUDP udp;

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

  FastLED.show();

  decay_check++;
  if (decay_check > decay)
  {
    decay_check = 0;
    if (audio > 0)
      audio -= 5;
  }
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
