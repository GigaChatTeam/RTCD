public class DataCommands {
    private abstract static class Message {
        public String type;
        public int id = 0;
        public int channel;
        public int author;

        public void setId (int id) {
            this.id = id;
        }
    }
    public static class MessagePost extends Message {
        public String type = "MESSAGE-POST";
        public String text;
    }
    public static class MessageDelete extends Message {
        public String type = "MESSAGE-DELETE";
    }

    private abstract static class ChannelControl {
        public int id = 0;

        public void setId (int id) {
            this.id = id;
        }
    }
    public static class ChannelCreate extends ChannelControl {
        public String type = "CHANNEL-CONTROL-CREATE";
        public String title;
    }
    private abstract static class ChannelUserControl extends ChannelControl {
        public int user;
    }
    public static class ChannelUserControlAccess extends ChannelUserControl {
        public String type = "CHANNEL-USERCONTROL-ADD";
        public Boolean access;
    }
    public static class ChannelUserPermissions extends ChannelUserControl {
        public String type = "CHANNEL-USERCONTROL-UPDATEPERMISSIONS";
        public int permission;
        public Boolean status;
    }
}
