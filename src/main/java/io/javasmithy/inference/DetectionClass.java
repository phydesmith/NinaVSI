package io.javasmithy.inference;

public enum DetectionClass {
    LOG("Log", 1),
    JUNK("Junk", 2),
    STONE("Stone", 3),
    STUMP("Stump", 4),
    TREE("Tree", 5);

    private final String description;
    private final int classId;

    DetectionClass(String description, int classId){
        this.description = description;
        this.classId = classId;
    }

    public static String getEnumDescriptionById(int id){
        String desc = "";
        switch(id){
            case 1:
                desc = LOG.name();
                break;
            case 2:
                desc = JUNK.name();
                break;
            case 3:
                desc = STONE.name();
                break;
            case 4:
                desc = STUMP.name();
                break;
            case 5:
                desc = TREE.name();
                break;
            default:
                desc = "None";
                break;
        }
        return desc;
    }
}
