package com.example.demo.qrcode;

import com.example.demo.util.ImgUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;

public class Img {

    private final BufferedImage srcImage;
    private Image targetImage;
    /**
     * 目标图片文件格式，用于写出
     */
    private String targetImageType;
    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     */
    private boolean positionBaseCentre = true;
    /**
     * 图片输出质量，用于压缩
     */
    private float quality = -1;


    /**
     * 构造，目标图片类型取决于来源图片类型
     *
     * @param srcImage 来源图片
     */
    public Img(BufferedImage srcImage) {
        this(srcImage, null);
    }

    /**
     * 构造
     *
     * @param srcImage        来源图片
     * @param targetImageType 目标图片类型，null则读取来源图片类型
     * @since 5.0.7
     */
    public Img(BufferedImage srcImage, String targetImageType) {
        this.srcImage = srcImage;
        if (null == targetImageType) {
            if (srcImage.getType() == BufferedImage.TYPE_INT_ARGB
                    || srcImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE
            ) {
                targetImageType = ImgUtil.IMAGE_TYPE_PNG;
            } else {
                targetImageType = ImgUtil.IMAGE_TYPE_JPG;
            }
        }
        this.targetImageType = targetImageType;
    }

    /**
     * 从Image取图片并开始处理
     *
     * @param image 图片
     * @return Img
     */
    public static Img from(Image image) {
        return new Img(ImgUtil.toBufferedImage(image));
    }

    /**
     * 图片圆角处理
     *
     * @param arc 圆角弧度，0~1，为长宽占比
     * @return this
     * @since 4.5.3
     */
    public Img round(double arc) {
        final Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

        // 通过弧度占比计算弧度
        arc = mul(arc, Math.min(width, height));

        final BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = targetImage.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        // 抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(srcImage, 0, 0, null);
        g2.dispose();
        this.targetImage = targetImage;
        return this;
    }

    private double mul(double d, int i) {
        BigDecimal decimal = new BigDecimal(d);
        return decimal.multiply(new BigDecimal(i)).doubleValue();
    }


    /**
     * 获取有效的源图片，首先检查上一次处理的结果图片，如无则使用用户传入的源图片
     *
     * @return 有效的源图片
     */
    private Image getValidSrcImg() {
        return (null != this.targetImage) ? this.targetImage : this.srcImage;
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImg  水印图片，可以使用{@link ImageIO#read(File)}方法读取文件
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height，x,y从背景图片中心计算
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return this
     * @since 4.1.14
     */
    public Img pressImage(Image pressImg, Rectangle rectangle, float alpha) {
        final Image targetImg = getValidSrcImg();

        this.targetImage = draw(ImgUtil.toBufferedImage(targetImg, this.targetImageType), pressImg, rectangle, alpha);
        return this;
    }

    /**
     * 获取处理过的图片
     *
     * @return 处理过的图片
     */
    public Image getImg() {
        return getValidSrcImg();
    }
    /**
     * 将图片绘制在背景上
     *
     * @param backgroundImg 背景图片
     * @param img           要绘制的图片
     * @param rectangle     矩形对象，表示矩形区域的x，y，width，height，x,y从背景图片中心计算（如果positionBaseCentre为true）
     * @param alpha         透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 绘制后的背景
     */
    private BufferedImage draw(BufferedImage backgroundImg, Image img, Rectangle rectangle, float alpha) {
        final Graphics2D g = backgroundImg.createGraphics();
//        设置画笔透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        fixRectangle(rectangle, backgroundImg.getWidth(), backgroundImg.getHeight());
//        绘制图片
        g.drawImage(img, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null); // 绘制切割后的图

        g.dispose();
        return backgroundImg;
    }

    /**
     * 修正矩形框位置，如果{@link Img#(boolean)} 设为{@code true}，<br>
     * 则坐标修正为基于图形中心，否则基于左上角
     *
     * @param rectangle  矩形
     * @param baseWidth  参考宽
     * @param baseHeight 参考高
     * @return 修正后的{@link Rectangle}
     * @since 4.1.15
     */
    private Rectangle fixRectangle(Rectangle rectangle, int baseWidth, int baseHeight) {
        if (this.positionBaseCentre) {
            final Point pointBaseCentre = ImgUtil.getPointBaseCentre(rectangle, baseWidth, baseHeight);
            // 修正图片位置从背景的中心计算
            rectangle.setLocation(pointBaseCentre.x, pointBaseCentre.y);
        }
        return rectangle;
    }
}
