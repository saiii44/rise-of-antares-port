package javax.microedition.lcdui;

public class Command {
    public static final int SCREEN = 1, BACK = 2, CANCEL = 3, OK = 4, HELP = 5, STOP = 6, EXIT = 7, ITEM = 8;
    private String label;
    private int commandType;
    private int priority;

    public Command(String label, int commandType, int priority) {
        this.label = label;
        this.commandType = commandType;
        this.priority = priority;
    }

    public String getLabel() { return label; }
    public int getCommandType() { return commandType; }
    public int getPriority() { return priority; }
}
