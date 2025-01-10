package RankCard;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SendMsgCard
{
    private static SendMsgCard instance;

    private static final int bg_width = 672;
    private static final int bg_height = 1080;
    private static final int card_width = 640;
    private static final int card_height = 80;
    private static final int head_pic_width = 640;
    private static final int head_pic_height = 640;
    private static final int head_width = 60;
    private static final int head_height = 60;
    private static final int head_x = 20;
    private static final int head_y = 10;

    private static final String title = "今日水群排行榜";
    
    private static final int card_frame_count = 10;

    private final BufferedImage head_mask;
    private final GradientPaint gradient;



    private File[] bg_pic_file;
    private File font_file;
    private final File[] card_frames;

    public static SendMsgCard getInstance(String img_dir)
    {
        if(SendMsgCard.instance == null)
        {
            SendMsgCard.instance = new SendMsgCard(img_dir);
        }
        return SendMsgCard.instance;
    }

    private SendMsgCard(String img_dir)
    {
        String img_dir_temp;
        Graphics2D head_mask_g2d;
        int idx;

        if(img_dir.endsWith(File.separator))
        {
            img_dir_temp = img_dir;
        }
        else
        {
            img_dir_temp = img_dir + File.separator;
        }

        this.card_frames = new File[SendMsgCard.card_frame_count];
        for(idx = 0; idx < SendMsgCard.card_frame_count; idx++)
        {
            this.card_frames[idx] = new File(String.format("%sframe%s%d.png", img_dir_temp, File.separator, idx + 1));
        }

        this.initPic(img_dir);

        /* 头像遮罩 */
        this.head_mask = new BufferedImage(head_width, head_height, BufferedImage.TYPE_INT_ARGB);
        head_mask_g2d = this.head_mask.createGraphics();
        head_mask_g2d.setColor(Color.WHITE);
        head_mask_g2d.fillRect(0, 0, head_width, head_height);
        head_mask_g2d.setComposite(AlphaComposite.Clear);
        head_mask_g2d.fillOval(0, 0, head_width, head_height);
        head_mask_g2d.dispose();

        this.gradient = new GradientPaint(0, 0, new Color(0, 0, 0, 255), card_width, 0, new Color(0, 0, 0, 0));
    }

    public void initPic(String img_dir)
    {
        File[] ttfs;

        this.bg_pic_file = SendMsgCard.getAllFile(img_dir + "bg" + File.separator, "bg");
        ttfs = SendMsgCard.getAllFile(img_dir + "ttf" + File.separator, ".ttf");
        System.out.println(ttfs.length);
        if(ttfs.length == 0)
        {
            this.font_file = null;
        }
        else
        {
            this.font_file = SendMsgCard.getAllFile(img_dir + "ttf" + File.separator, ".ttf")[0];
        }
        
    }

    public BufferedImage getCard(List<MemberInfo> meList)
    {
        BufferedImage background;
        Graphics2D bg_g2d;
        Font current_font;
        FontMetrics metrics;
        int size = Math.min(SendMsgCard.card_frame_count, meList.size());
        int idx;
        boolean is_custom_bg = false;


        if(this.bg_pic_file.length == 0)
        {
            background = new BufferedImage(bg_width, bg_height, BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            try 
            {
                background = SendMsgCard.formatImage(bg_width, bg_height, this.bg_pic_file[ThreadLocalRandom.current().nextInt(0, this.bg_pic_file.length)]);
                is_custom_bg = true;
            } 
            catch (IOException e) 
            {
                background = new BufferedImage(bg_width, bg_height, BufferedImage.TYPE_INT_ARGB);
            }

        }

        bg_g2d = background.createGraphics();
        /* 如果加载自定义背景图失败，就用白色背景 */
        if(!is_custom_bg)
        {
            bg_g2d.setColor(Color.WHITE);
            bg_g2d.fillRect(0, 0, bg_width, bg_height);
        }
        
        if(this.font_file == null)
        {   
            current_font = new Font("楷体", Font.PLAIN, 60);
        }
        else
        {
            try
            {
                current_font = Font.createFont(Font.TRUETYPE_FONT, this.font_file).deriveFont(60f);
            }
            catch(IOException | FontFormatException e)
            {
                current_font = new Font("楷体", Font.PLAIN, 60);
            }
        }

        bg_g2d.setFont(current_font);
        bg_g2d.setColor(Color.MAGENTA);
        metrics = bg_g2d.getFontMetrics(current_font);
        bg_g2d.drawString(SendMsgCard.title, (SendMsgCard.bg_width - metrics.stringWidth(SendMsgCard.title)) / 2, metrics.getHeight() / 2 + 30);


        for(idx = 0; idx < size; idx++)
        {
            BufferedImage current = this.getMemberCard(meList.get(idx), this.card_frames[idx], current_font);
            /* 这里就先写成固定间距95像素了 */
            bg_g2d.drawImage(current, (bg_width - card_width) / 2, 100 + 95 * idx, null);
        }

        bg_g2d.dispose();

        return background;
    }


    private BufferedImage getMemberCard(MemberInfo member, File card_frame, Font current_font)
    {
        BufferedImage back = new BufferedImage(card_width, card_height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D back_g2d = back.createGraphics();
        BufferedImage front = new BufferedImage(card_width, card_height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D front_g2d = front.createGraphics();
        BufferedImage frame;
        try
        {
            frame = ImageIO.read(card_frame);

        } 
        catch (IOException e) 
        {
            frame = null;
        }

        /* 把头像已渐进透明的方式绘制 */
        front_g2d.drawImage(member.headPic, 0, -head_pic_height / 2, head_pic_width, head_pic_height, null);
        front_g2d.setComposite(AlphaComposite.DstIn);
        front_g2d.setPaint(this.gradient);
        front_g2d.fillRect(0, 0, head_pic_width, card_height);

        /* 绘制边框 */
        front_g2d.setComposite(AlphaComposite.SrcOver); 
        front_g2d.drawImage(frame, 0, 0, null);
        front_g2d.setComposite(AlphaComposite.Clear);
        front_g2d.fillOval(head_x, head_y, head_width, head_height);
        front_g2d.dispose();


        /*---------- 制作背景图 ----------*/
        back_g2d.setColor(Color.WHITE);
        back_g2d.fillRect(0, 0, card_width, card_height);
        back_g2d.drawImage(member.headPic, head_x, head_y, head_width, head_height, null);
        back_g2d.drawImage(this.head_mask, head_x, head_y, null);
        back_g2d.drawImage(front, 0, 0, null);

        /* 绘制文字 */
        back_g2d.setFont(current_font.deriveFont(24f));
        back_g2d.setColor(Color.BLACK);
        if(member.name.length() > 12)
        {
            member.name = member.name.substring(0, 12) + "...";
        }
        back_g2d.drawString(member.name, 100, 35);
        back_g2d.drawString(formatTime(member.time_seconds), 440, 52);
        back_g2d.setFont(current_font.deriveFont(16f));
        back_g2d.drawString(String.format("消息数: %d条", member.msg_count), 102, 65);

        back_g2d.dispose();
        

        return back;
    }


    private static String formatTime(int seconds)
    {
        seconds = Math.max(0, seconds);

        int hours = seconds / 3600; 
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (hours > 0) {
            formattedTime.append(hours).append("时");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append("分");
        }
        if (remainingSeconds > 0 || formattedTime.isEmpty()) {
            formattedTime.append(remainingSeconds).append("秒");
        }

        return formattedTime.toString();
    }

    private static File[] getAllFile(String dir, String name) 
    {
        if (dir == null || dir.isEmpty()) 
        {
            return new File[0];
        }

        File directory = new File(dir);
        // 检查路径是否为有效的目录
        if (!directory.exists() || !directory.isDirectory()) 
        {
            return new File[0];
        }

        // 使用文件过滤器只保留文件
        return directory.listFiles(file -> file.isFile() && file.getName().contains(name));
    }

    private static BufferedImage formatImage(int width, int height, File picture) throws IOException 
    {
        // 读取图片
        BufferedImage originalImage = ImageIO.read(picture);
        if (originalImage == null) {
            throw new IOException("无法读取输入的图片文件！");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 计算缩放比例
        double scale = Math.max((double) width / originalWidth, (double) height / originalHeight);

        // 计算缩放后的尺寸
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        // 创建缩放后的图像
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();

        // 开启高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.setColor(new Color(255, 255, 255, 128));
        g2d.fillRect(0, 0, scaledWidth, scaledHeight);
        g2d.dispose();

        // 计算裁剪的起始位置
        int cropX = (scaledWidth - width) / 2;
        int cropY = (scaledHeight - height) / 2;

        // 裁剪指定区域
        return scaledImage.getSubimage(cropX, cropY, width, height);
    }

}
