package io.github.derkottersberg.seamlessdogs.gameplay;

public enum PetAction {
    DOG_PET(40,1), CAT_PET(40,2), DIG(80,3), STRETCH(80,4), KNEAD(120,6), GROOM(120,7), HEAD_TILT(60,8);
    public final int duration;
    public final int wireId;
    PetAction(int duration,int wireId) { this.duration = duration; this.wireId=wireId; }
    public static PetAction fromWire(int id) { for(var action:values())if(action.wireId==id)return action;return null; }
    public boolean expressive() { return wireId>=6; }
    public boolean petting() { return this == DOG_PET || this == CAT_PET; }
}
