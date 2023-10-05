public class Starter {
    public static void main (String[] args) {
        int port = 8080;
        WSCore server = new WSCore(port);
        server.start();

//        String string = "123/234/?id=4&token=user.4.mYB6QbgnGGu2xavkfTHAUkF7mJ8jxxf5s42Fxph2Oo9Nw4RL6fyVfnNhfnr7IKmzV";
//
//        Helper.ConnectionPath connectParams = Helper.parseURI(string);
//
//        if (PermissionOperator.validateToken(Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token"))) {
//            System.out.println("Токен верный");
//        } else System.out.println("Токен неверный");
    }
}
