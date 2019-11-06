public class GroupMessage{
    private String username;
    private String accountname;
    private String message;

    public GroupMessage() {
    }

    public GroupMessage(String username,String accountname, String message) {
        this.username = username;
        this.message = message;
        this.accountname=accountname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}