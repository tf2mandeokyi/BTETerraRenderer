package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.util.IOUtil;
import com.mndk.bteterrarenderer.dep.terraplusplus.http.Http;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@UtilityClass
public class HttpResourceManager {

    private final Transformer TRANSFORMER;
    private final DocumentBuilder DOCUMENT_BUILDER;
    private final PNGTranscoder PNG_TRANSCODER = new PNGTranscoder();

    private final NullPointerException NPE = new NullPointerException();

    public BufferedImage downloadAsImage(String url) throws ExecutionException, InterruptedException, IOException {
        ByteBuf buf = download(url);
        byte[] bytes = IOUtil.readAllBytes(buf);
        Exception exception;

        // Try bitmap type image
        try {
            InputStream stream = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(stream);
            stream.close();

            if (image == null) throw NPE;
            return image;
        } catch (IOException | NullPointerException ignored) {}

        // Try svg type image
        try {
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
        } catch (TranscoderException | NullPointerException | SAXException | TransformerException e) {
            exception = e;
        }

        throw new IOException("Cannot specify image type for " + url, exception);
    }

    private InputStream fixBrokenSvgFile(InputStream brokenSvgStream) throws IOException, SAXException, TransformerException {
        // "The attribute "offset" of the element <stop> is required" error handler
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

    public ByteBuf download(String url) throws ExecutionException, InterruptedException {
        return Http.get(url).get();
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
