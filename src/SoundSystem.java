import javax.sound.sampled.*;

public class SoundSystem {

    private static final float SAMPLE_RATE = 8000f;
    private static final int[] INVADER_STEP_TONES = { 110, 98, 87, 82 };
    private static int invaderStepIndex = 0;
    private static volatile boolean ufoLooping = false;
    private static Thread ufoThread;

    public static void playPlayerShoot() {
        playTone(880, 60);
    }

    public static void playEnemyShoot() {
        playTone(520, 70);
    }

    public static void playStep(boolean high) {
        int hz = INVADER_STEP_TONES[invaderStepIndex];
        invaderStepIndex = (invaderStepIndex + 1) % INVADER_STEP_TONES.length;
        playBassPluck(hz, 230, 115);
    }

    public static void playUfo() {
        playTone(300, 120);
    }

    public static void startUfoLoop() {
        if (ufoLooping) return;
        ufoLooping = true;
        ufoThread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    byte[] buf = new byte[512];
                    long sampleIndex = 0;
                    while (ufoLooping) {
                        for (int i = 0; i < buf.length; i++) {
                            double t = sampleIndex / SAMPLE_RATE;
                            double mod = Math.sin(2.0 * Math.PI * 2.0 * t);
                            double hz = 220.0 + (mod * 10.0);
                            double angle = 2.0 * Math.PI * hz * t;
                            int sample = (int) Math.round(Math.sin(angle) * 60.0);
                            buf[i] = (byte) sample;
                            sampleIndex++;
                        }
                        line.write(buf, 0, buf.length);
                    }
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        }, "ufo-sound");
        ufoThread.start();
    }

    public static void stopUfoLoop() {
        ufoLooping = false;
    }

    public static void playPlayerHit() {
        playTone(180, 180);
    }

    public static void playEnemyHit() {
        playTone(260, 90);
    }

    public static void playPlayerDeath() {
        playTone(120, 260);
    }

    private static void playTone(int hz, int ms) {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    byte[] buf = new byte[(int) (SAMPLE_RATE * ms / 1000)];
                    for (int i = 0; i < buf.length; i++) {
                        double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
                        buf[i] = (byte) (Math.sin(angle) * 64);
                    }
                    line.write(buf, 0, buf.length);
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        }, "sound").start();
    }

    private static void playBassPluck(int hz, int ms, int amplitude) {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    byte[] buf = new byte[(int) (SAMPLE_RATE * ms / 1000)];
                    int len = buf.length;
                    double base = 2.0 * Math.PI * hz / SAMPLE_RATE;
                    for (int i = 0; i < len; i++) {
                        float t = i / (float) len;
                        // Pluck-like envelope: fast attack, slower decay
                        float env = (t < 0.06f) ? (t / 0.06f) : (float) Math.pow(1.0f - t, 1.2f);
                        double s1 = Math.sin(base * i);
                        double s2 = 0.35 * Math.sin(base * 2.0 * i);
                        double s3 = 0.18 * Math.sin(base * 3.0 * i);
                        double sample = (s1 + s2 + s3) * env;
                        int sampleVal = (int) Math.round(amplitude * sample);
                        if (sampleVal > 127) sampleVal = 127;
                        if (sampleVal < -127) sampleVal = -127;
                        buf[i] = (byte) sampleVal;
                    }
                    line.write(buf, 0, buf.length);
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        }, "sound").start();
    }
}
