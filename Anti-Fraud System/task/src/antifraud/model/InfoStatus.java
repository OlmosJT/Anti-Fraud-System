package antifraud.model;

public enum InfoStatus {
    NONE("none"),
    AMOUNT("amount"),
    CARD_NUMBER("card-number"),
    IP("ip"),
    Ip_CORRELATION("ip-correlation"),
    REGION_CORRELATION("region-correlation");

    private final String value;


    InfoStatus(String value) {
        this.value = value;
    }

    public String getValue() {return value; }


}
