package se.campusmolndal.easyweather.models;

public class Aliases {
    private String name;
    private String[] aliases;

    public Aliases(String name, String[] aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }
}
