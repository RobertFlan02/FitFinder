package vinigarstudios.fitfinder.enums;

public enum ListOrder
{
    time("timestamp"),
    likes("likes");

    public final String value;

    private ListOrder(String value) {
        this.value = value;
    }
    public String toString() {
        return this.value;
    }

}
