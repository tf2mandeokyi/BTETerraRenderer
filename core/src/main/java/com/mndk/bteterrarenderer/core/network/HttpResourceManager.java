package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.dep.terraplusplus.http.Http;
import com.mndk.bteterrarenderer.util.IOUtil;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class HttpResourceManager {

    private final Transformer TRANSFORMER;
    private final DocumentBuilder DOCUMENT_BUILDER;
    private final PNGTranscoder PNG_TRANSCODER = new PNGTranscoder();

    private final NullPointerException NPE = new NullPointerException();

    public CompletableFuture<BufferedImage> downloadAsImage(String url) {
        return download(url).thenApplyAsync(buf -> {
            try { return bufferToImage(buf); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    private BufferedImage bufferToImage(ByteBuf buf) throws Exception {
        byte[] bytes = IOUtil.readAllBytes(buf);

        // Try bitmap type image
        try {
            InputStream stream = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(stream);
            stream.close();

            if (image == null) throw NPE;
            return image;
        } catch (IOException | NullPointerException ignored) {}

        // Try svg type image
        InputStream svgStream = fixBrokenSvgFile(new ByteArrayInputStream(bytes));
        TranscoderInput svgInput = new TranscoderInput(svgStream);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TranscoderOutput pngOutput = new TranscoderOutput(baos);

        PNG_TRANSCODER.transcode(svgInput, pngOutput);
        byte[] pngBytes = baos.toByteArray();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
        svgStream.close();

        if (image == null) throw NPE;
        return image;
    }

    private InputStream fixBrokenSvgFile(InputStream brokenSvgStream) throws IOException, SAXException, TransformerException {
        // "The attribute 'offset' of the element <stop> is required" error handler
        Document svgDocument = DOCUMENT_BUILDER.parse(brokenSvgStream);
        NodeList stopTags = svgDocument.getElementsByTagName("stop");
        for (int i = 0; i < stopTags.getLength(); i++) {
            NamedNodeMap stopNodeAttributes = stopTags.item(i).getAttributes();
            Node offsetNode = stopNodeAttributes.getNamedItem("offset");
            if (offsetNode != null) continue;

            Attr newOffsetNode = svgDocument.createAttribute("offset");
            newOffsetNode.setValue("0");
            stopNodeAttributes.setNamedItem(newOffsetNode);
        }

        StringWriter writer = new StringWriter();
        TRANSFORMER.transform(new DOMSource(svgDocument), new StreamResult(writer));
        return new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
    }

    public BufferedImage resizeImage(@Nonnull BufferedImage image, int paletteWidth, int paletteHeight) {
        if (paletteWidth <= 0 || paletteHeight <= 0) return image;
        double paletteRatio = (double) paletteHeight / paletteWidth;

        BufferedImage palette = new BufferedImage(paletteWidth, paletteHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = palette.createGraphics();
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, paletteWidth, paletteHeight);

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        double imageRatio = (double) imageHeight / imageWidth;
        if (paletteRatio > imageRatio) {
            int centerY = paletteHeight / 2, height = (int) (paletteWidth * imageRatio);
            g2d.drawImage(image, 0, centerY - height / 2, paletteWidth, height, null);
        } else {
            int centerX = paletteWidth / 2, width = (int) (paletteHeight / imageRatio);
            g2d.drawImage(image, centerX - width / 2, 0, width, paletteHeight, null);
        }

        g2d.dispose();
        return palette;
    }

    public CompletableFuture<ByteBuf> download(String url) {
        return Http.get(url);
    }

    static {
        try {
            TRANSFORMER = TransformerFactory.newInstance().newTransformer();
            DOCUMENT_BUILDER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
