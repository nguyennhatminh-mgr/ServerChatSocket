import java.io.Serializable;

public class UserInfo implements Serializable {
    String accountname;
    String ip;

    public UserInfo(String accountname, String ip) {
        this.accountname = accountname;
        this.ip = ip;
    }

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
