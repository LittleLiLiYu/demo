package com.example.demo.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Base64;
import java.util.Iterator;

public class ImgUtil {

    public static final String IMAGE_TYPE_GIF = "gif";// 图形交换格式
    public static final String IMAGE_TYPE_JPG = "jpg";// 联合照片专家组
    public static final String IMAGE_TYPE_JPEG = "jpeg";// 联合照片专家组
    public static final String IMAGE_TYPE_BMP = "bmp";// 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
    public static final String IMAGE_TYPE_PNG = "png";// 可移植网络图形
    public static final String IMAGE_TYPE_PSD = "psd";// Photoshop的专用格式Photoshop

    /**
     * 将的图像bytes转为 {@link BufferedImage}
     *
     * @param imageBytes 图像bytes
     * @return {@link BufferedImage}
     * @throws RuntimeException IO异常
     */
    public static BufferedImage toImage(byte[] imageBytes) throws RuntimeException {
        return read(new ByteArrayInputStream(imageBytes));
    }


    /**
     * 从流中读取图片
     *
     * @param imageStream 图片文件
     * @return 图片
     * @since 3.2.2
     */
    public static BufferedImage read(InputStream imageStream) {
        BufferedImage result;
        try {
            result = ImageIO.read(imageStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * 从文件中读取图片
     *
     * @param imageFile 图片文件
     * @return 图片
     * @since 3.2.2
     */
    public static BufferedImage read(File imageFile) {
        BufferedImage result;
        try {
            result = ImageIO.read(imageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type of file [" + imageFile.getName() + "] is not supported!");
        }

        return result;
    }

    /**
     * 将图片对象转换为Base64的Data URI形式，格式为：data:image/[imageType];base64,[data]
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     * @since 5.3.6
     */
    public static String toBase64DataUri(Image image, String imageType) {
        return URLUtil.getDataUri(
                "image/" + imageType, "base64",
                toBase64(image, imageType));
    }

    /**
     * 将图片对象转换为Base64形式
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     * @since 4.1.8
     */
    public static String toBase64(Image image, String imageType) {
        return new String(Base64.getEncoder().encode(toBytes(image, imageType)));
    }

    /**
     * 将图片对象转换为bytes形式
     *
     * @param image     图片对象
     * @param imageType 图片类型
     * @return Base64的字符串表现形式
     * @since 5.2.4
     */
    public static byte[] toBytes(Image image, String imageType) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(image, imageType, out);
        return out.toByteArray();
    }

    /**
     * 写出图像：GIF=》JPG、GIF=》PNG、PNG=》JPG、PNG=》GIF(X)、BMP=》PNG<br>
     * 此方法并不关闭流
     *
     * @param image     {@link Image}
     * @param imageType 图片类型（图片扩展名）
     * @param out       写出到的目标流
     * @throws RuntimeException IO异常
     * @since 3.1.2
     */
    public static void write(Image image, String imageType, OutputStream out) throws RuntimeException {
        write(image, imageType, getImageOutputStream(out));
    }

    /**
     * 获取{@link ImageOutputStream}
     *
     * @param out {@link OutputStream}
     * @return {@link ImageOutputStream}
     * @throws RuntimeException IO异常
     * @since 3.1.2
     */
    public static ImageOutputStream getImageOutputStream(OutputStream out) throws RuntimeException {
        ImageOutputStream result;
        try {
            result = ImageIO.createImageOutputStream(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type is not supported!");
        }

        return result;
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param image      {@link Image}
     * @param targetFile 目标文件
     * @throws RuntimeException IO异常
     * @since 3.1.0
     */
    public static void write(Image image, File targetFile) throws RuntimeException {
        FileUtil.touch(targetFile);
        ImageOutputStream out = null;
        try {
            out = getImageOutputStream(targetFile);
            write(image, FileUtil.extName(targetFile), out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 获取{@link ImageOutputStream}
     *
     * @param outFile {@link File}
     * @return {@link ImageOutputStream}
     * @throws RuntimeException IO异常
     * @since 3.2.2
     */
    public static ImageOutputStream getImageOutputStream(File outFile) throws RuntimeException {
        ImageOutputStream result;
        try {
            result = ImageIO.createImageOutputStream(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (null == result) {
            throw new IllegalArgumentException("Image type of file [" + outFile.getName() + "] is not supported!");
        }

        return result;
    }

    /**
     * 写出图像为指定格式：GIF=》JPG、GIF=》PNG、PNG=》JPG、PNG=》GIF(X)、BMP=》PNG<br>
     * 此方法并不关闭流
     *
     * @param image           {@link Image}
     * @param imageType       图片类型（图片扩展名）
     * @param destImageStream 写出到的目标流
     * @return 是否成功写出，如果返回false表示未找到合适的Writer
     * @throws RuntimeException IO异常
     * @since 3.1.2
     */
    public static boolean write(Image image, String imageType, ImageOutputStream destImageStream) throws RuntimeException {
        return write(image, imageType, destImageStream, 1);
    }

    /**
     * 写出图像为指定格式
     *
     * @param image           {@link Image}
     * @param imageType       图片类型（图片扩展名）
     * @param destImageStream 写出到的目标流
     * @param quality         质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     * @return 是否成功写出，如果返回false表示未找到合适的Writer
     * @throws RuntimeException IO异常
     * @since 4.3.2
     */
    public static boolean write(Image image, String imageType, ImageOutputStream destImageStream, float quality) throws RuntimeException {
        if (StringUtils.isBlank(imageType)) {
            imageType = IMAGE_TYPE_JPG;
        }

        final BufferedImage bufferedImage = toBufferedImage(image, imageType);
        final ImageWriter writer = getWriter(bufferedImage, imageType);
        return write(bufferedImage, writer, destImageStream, quality);
    }

    /**
     * 通过{@link ImageWriter}写出图片到输出流
     *
     * @param image   图片
     * @param writer  {@link ImageWriter}
     * @param output  输出的Image流{@link ImageOutputStream}
     * @param quality 质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     * @return 是否成功写出
     * @since 4.3.2
     */
    public static boolean write(Image image, ImageWriter writer, ImageOutputStream output, float quality) {
        if (writer == null) {
            return false;
        }

        writer.setOutput(output);
        final RenderedImage renderedImage = toRenderedImage(image);
        // 设置质量
        ImageWriteParam imgWriteParams = null;
        if (quality > 0 && quality < 1) {
            imgWriteParams = writer.getDefaultWriteParam();
            if (imgWriteParams.canWriteCompressed()) {
                imgWriteParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imgWriteParams.setCompressionQuality(quality);
                final ColorModel colorModel = renderedImage.getColorModel();// ColorModel.getRGBdefault();
                imgWriteParams.setDestinationType(new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));
            }
        }

        try {
            if (null != imgWriteParams) {
                writer.write(null, new IIOImage(renderedImage, null, null), imgWriteParams);
            } else {
                writer.write(renderedImage);
            }
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            writer.dispose();
        }
        return true;
    }

    /**
     * {@link Image} 转 {@link RenderedImage}<br>
     * 首先尝试强转，否则新建一个{@link BufferedImage}后重新绘制，使用 {@link BufferedImage#TYPE_INT_RGB} 模式。
     *
     * @param img {@link Image}
     * @return {@link BufferedImage}
     * @since 4.3.2
     */
    public static RenderedImage toRenderedImage(Image img) {
        if (img instanceof RenderedImage) {
            return (RenderedImage) img;
        }

        return copyImage(img, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * {@link Image} 转 {@link BufferedImage}<br>
     * 首先尝试强转，否则新建一个{@link BufferedImage}后重新绘制，使用 {@link BufferedImage#TYPE_INT_RGB} 模式
     *
     * @param img {@link Image}
     * @return {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        return copyImage(img, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * 根据给定的Image对象和格式获取对应的{@link ImageWriter}，如果未找到合适的Writer，返回null
     *
     * @param img        {@link Image}
     * @param formatName 图片格式，例如"jpg"、"png"
     * @return {@link ImageWriter}
     * @since 4.3.2
     */
    public static ImageWriter getWriter(Image img, String formatName) {
        final ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(toBufferedImage(img, formatName));
        final Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, formatName);
        return iter.hasNext() ? iter.next() : null;
    }

    /**
     * {@link Image} 转 {@link BufferedImage}<br>
     * 如果源图片的RGB模式与目标模式一致，则直接转换，否则重新绘制<br>
     * 默认的，png图片使用 {@link BufferedImage#TYPE_INT_ARGB}模式，其它使用 {@link BufferedImage#TYPE_INT_RGB} 模式
     *
     * @param image     {@link Image}
     * @param imageType 目标图片类型，例如jpg或png等
     * @return {@link BufferedImage}
     * @since 4.3.2
     */
    public static BufferedImage toBufferedImage(Image image, String imageType) {
        final int type = IMAGE_TYPE_PNG.equalsIgnoreCase(imageType)
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        return toBufferedImage(image, type);
    }

    /**
     * {@link Image} 转 {@link BufferedImage}<br>
     * 如果源图片的RGB模式与目标模式一致，则直接转换，否则重新绘制
     *
     * @param image     {@link Image}
     * @param imageType 目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @return {@link BufferedImage}
     * @since 5.4.7
     */
    public static BufferedImage toBufferedImage(Image image, int imageType) {
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
            if (imageType != bufferedImage.getType()) {
                bufferedImage = copyImage(image, imageType);
            }
        } else {
            bufferedImage = copyImage(image, imageType);
        }
        return bufferedImage;
    }

    /**
     * 将已有Image复制新的一份出来
     *
     * @param img       {@link Image}
     * @param imageType 目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @return {@link BufferedImage}
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_BYTE_GRAY
     * @see BufferedImage#TYPE_USHORT_GRAY
     * @see BufferedImage#TYPE_BYTE_BINARY
     * @see BufferedImage#TYPE_BYTE_INDEXED
     * @see BufferedImage#TYPE_USHORT_565_RGB
     * @see BufferedImage#TYPE_USHORT_555_RGB
     */
    public static BufferedImage copyImage(Image img, int imageType) {
        return copyImage(img, imageType, null);
    }

    /**
     * 将已有Image复制新的一份出来
     *
     * @param img             {@link Image}
     * @param imageType       目标图片类型，{@link BufferedImage}中的常量，例如黑白等
     * @param backgroundColor 背景色，{@code null} 表示默认背景色（黑色或者透明）
     * @return {@link BufferedImage}
     * @see BufferedImage#TYPE_INT_RGB
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_ARGB_PRE
     * @see BufferedImage#TYPE_INT_BGR
     * @see BufferedImage#TYPE_3BYTE_BGR
     * @see BufferedImage#TYPE_4BYTE_ABGR
     * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see BufferedImage#TYPE_BYTE_GRAY
     * @see BufferedImage#TYPE_USHORT_GRAY
     * @see BufferedImage#TYPE_BYTE_BINARY
     * @see BufferedImage#TYPE_BYTE_INDEXED
     * @see BufferedImage#TYPE_USHORT_565_RGB
     * @see BufferedImage#TYPE_USHORT_555_RGB
     * @since 4.5.17
     */
    public static BufferedImage copyImage(Image img, int imageType, Color backgroundColor) {
        final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), imageType);
        final Graphics2D bGr = createGraphics(bimage, backgroundColor);
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    /**
     * 创建{@link Graphics2D}
     *
     * @param image {@link BufferedImage}
     * @param color {@link Color}背景颜色以及当前画笔颜色，{@code null}表示不设置背景色
     * @return {@link Graphics2D}
     * @since 4.5.2
     */
    public static Graphics2D createGraphics(BufferedImage image, Color color) {
        final Graphics2D g = image.createGraphics();

        if (null != color) {
            // 填充背景
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        return g;
    }

    /**
     * 获得修正后的矩形坐标位置，变为以背景中心为基准坐标（即x,y == 0,0时，处于背景正中）
     *
     * @param rectangle        矩形
     * @param backgroundWidth  参考宽（背景宽）
     * @param backgroundHeight 参考高（背景高）
     * @return 修正后的{@link Point}
     * @since 5.3.6
     */
    public static Point getPointBaseCentre(Rectangle rectangle, int backgroundWidth, int backgroundHeight) {
        return new Point(
                rectangle.x + (Math.abs(backgroundWidth - rectangle.width) / 2), //
                rectangle.y + (Math.abs(backgroundHeight - rectangle.height) / 2)//
        );
    }
}
