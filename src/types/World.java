package types;

import java.util.Arrays;

public class World {

    private static final Integer[] FREE_WORLDS = {1, 8, 16, 26, 35, 81, 82, 83, 84, 85, 93, 94};
    private final Integer[] DEADMAN_WORLDS = {45, 52, 57};

    private int world;
    private int players;
    private String location;
    private Type type;
    private String activity;

    public World(int world, int players, String location, Type type, String activity) {
        this.world = world;
        this.players = players;
        this.location = location;
        this.type = type;
        this.activity = activity;
    }

    public static World valueOf(String str) {
        String[] split = str.split(" ");
        int world = Integer.valueOf(split[2]);
        int players;
        try {
            players = Integer.parseInt(split[3]);
        } catch (NumberFormatException e) {
            players = 2000;
        }
        String location = "null";
        Type type = Type.MEMBER;
        String activity = "null";
        if (Arrays.asList(FREE_WORLDS).contains(world)) {
            type = Type.FREE;
        }
        return new World(world, players, location, type, activity);
    }

    @Override
    public String toString() {
        return "types.World: " + world + " Players: " + players + " Location: " + location + " Type: " + type + " Activity: " + activity;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getWorldId() {
        return world;
    }

    public void setWorldId(int world) {
        this.world = world;
    }

    public boolean isDeadman() {
        return Arrays.asList(DEADMAN_WORLDS).contains(getWorldId());
    }

    public enum Type {
        FREE, MEMBER
    }

}
