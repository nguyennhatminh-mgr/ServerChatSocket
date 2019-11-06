import java.util.ArrayList;

public class GroupUser{
    String groupName;
    ArrayList<UserAccount> listUser;
    ArrayList<GroupMessage> listMessage;
    public GroupUser(String groupName,ArrayList<UserAccount> listUser,ArrayList<GroupMessage> listMessage){
        this.groupName=groupName;
        this.listUser=listUser;
        this.listMessage=listMessage;
    }

    
}