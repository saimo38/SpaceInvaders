import java.io.*;

public class SaveSystem {

    private static final String FILE = "save.dat";

    public static int credits = 0;
    public static int highScore = 0;
    public static ShipType ownedShip = ShipType.DEFAULT;
    public static int ownedMask = 1 << ShipType.DEFAULT.ordinal();

    public static void load() {

        try (DataInputStream in = new DataInputStream(new FileInputStream(FILE))) {

            credits = in.readInt();
            highScore = in.readInt();
            int rawShip = in.readInt();
            if (in.available() >= 4) {
                if (rawShip >= 0 && rawShip < ShipType.values().length) {
                    ownedShip = ShipType.values()[rawShip];
                } else {
                    ownedShip = ShipType.DEFAULT;
                }
                ownedMask = in.readInt();
            } else {
                ShipType legacy;
                switch (rawShip) {
                    case 0:
                        legacy = ShipType.SCOUT;
                        break;
                    case 1:
                        legacy = ShipType.FIGHTER;
                        break;
                    case 2:
                        legacy = ShipType.DESTROYER;
                        break;
                    default:
                        legacy = ShipType.DEFAULT;
                        break;
                }
                ownedShip = legacy;
                ownedMask = 1 << ShipType.DEFAULT.ordinal();
            }
            ownedMask |= 1 << ShipType.DEFAULT.ordinal();
            ownedMask |= 1 << ownedShip.ordinal();

        } catch (Exception e) {
            credits = 0;
            highScore = 0;
            ownedShip = ShipType.DEFAULT;
            ownedMask = 1 << ShipType.DEFAULT.ordinal();
        }
    }

    public static void save() {

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(FILE))) {

            out.writeInt(credits);
            out.writeInt(highScore);
            out.writeInt(ownedShip.ordinal());
            out.writeInt(ownedMask);

        } catch (Exception ignored) {}
    }

    public static void reset() {
        credits = 0;
        highScore = 0;
        ownedShip = ShipType.DEFAULT;
        ownedMask = 1 << ShipType.DEFAULT.ordinal();
        save();
    }
}
