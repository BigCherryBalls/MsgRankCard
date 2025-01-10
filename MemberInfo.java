package RankCard;

import java.awt.image.BufferedImage;

public class MemberInfo
{
    public String name;
    public int msg_count;
    public int time_seconds;
    public BufferedImage headPic;

    public MemberInfo()
    {

    }
    public MemberInfo(String name, int msg_count, int time_seconds, BufferedImage headPic)
    {
        this.name = name;
        this.msg_count = msg_count;
        this.time_seconds = time_seconds;
        this.headPic = headPic;
    }



}
