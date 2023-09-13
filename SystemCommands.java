public class SystemCommands {
    private static abstract class Listens {
        public String type = "SYSTEM-CHANNELS-LISTEN";
        public int user;
    }
    public static class ListenChannel extends Listens {
        public int channel;
        public Boolean status;
    }
}
