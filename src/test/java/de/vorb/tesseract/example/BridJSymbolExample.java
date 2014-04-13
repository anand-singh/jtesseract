package de.vorb.tesseract.example;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.bridj.BridJ;
import org.bridj.FlagSet;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

import de.vorb.tesseract.bridj.Tesseract;
import de.vorb.tesseract.bridj.Tesseract.TessBaseAPI;
import de.vorb.tesseract.bridj.Tesseract.TessOcrEngineMode;
import de.vorb.tesseract.bridj.Tesseract.TessPageIterator;
import de.vorb.tesseract.bridj.Tesseract.TessPageIteratorLevel;
import de.vorb.tesseract.bridj.Tesseract.TessResultIterator;

public class BridJSymbolExample {
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {
    // provide the native library file
    BridJ.setNativeLibraryFile("tesseract", new File("libtesseract303.dll"));

    long start = System.currentTimeMillis();
    // create a reference to an execution handle
    final Pointer<TessBaseAPI> handle = Tesseract.TessBaseAPICreate();

    // init Tesseract with data path, language and OCR engine mode
    Tesseract.TessBaseAPIInit2(handle,
        Pointer.pointerToCString("E:\\Masterarbeit\\Ressourcen\\tessdata"),
        Pointer.pointerToCString("deu-frak"), TessOcrEngineMode.OEM_DEFAULT);

    // set page segmentation mode
    Tesseract.TessBaseAPISetPageSegMode(handle,
        Tesseract.TessPageSegMode.PSM_AUTO);

    // read the image into memory
    final BufferedImage inputImage = ImageIO.read(new File("input.png"));

    // get the image data
    final DataBuffer imageBuffer = inputImage.getRaster().getDataBuffer();
    final byte[] imageData = ((DataBufferByte) imageBuffer).getData();

    // image properties
    final int width = inputImage.getWidth();
    final int height = inputImage.getHeight();
    final int bitsPerPixel = inputImage.getColorModel().getPixelSize();
    final int bytesPerPixel = bitsPerPixel / 8;
    final int bytesPerLine = (int) Math.ceil(width * bitsPerPixel / 8.0);

    // set the image
    Tesseract.TessBaseAPISetImage(handle,
        Pointer.pointerToBytes(ByteBuffer.wrap(imageData)), width, height,
        bytesPerPixel, bytesPerLine);

    // text recognition
    Tesseract.TessBaseAPIRecognize(handle, Pointer.NULL);

    // get the result iterator
    final Pointer<TessResultIterator> resultIt =
        Tesseract.TessBaseAPIGetIterator(handle);

    // get the page iterator
    final Pointer<TessPageIterator> pageIt =
        Tesseract.TessResultIteratorGetPageIterator(resultIt);

    // iterating over symbols
    final TessPageIteratorLevel level =
        Tesseract.TessPageIteratorLevel.RIL_SYMBOL;

    final Pointer<Integer> left = Pointer.allocateInt();
    final Pointer<Integer> top = Pointer.allocateInt();
    final Pointer<Integer> right = Pointer.allocateInt();
    final Pointer<Integer> bottom = Pointer.allocateInt();

    do {
      if (Tesseract.TessPageIteratorIsAtBeginningOf(pageIt,
          Tesseract.TessPageIteratorLevel.RIL_BLOCK) > 0) {
      } else if (Tesseract.TessPageIteratorIsAtBeginningOf(pageIt,
          Tesseract.TessPageIteratorLevel.RIL_TEXTLINE) > 0) {
        System.out.print("\n\n");
      } else if (Tesseract.TessPageIteratorIsAtBeginningOf(pageIt,
          Tesseract.TessPageIteratorLevel.RIL_WORD) > 0) {
        System.out.print('\n');
      }

      final String symbol = Tesseract.TessResultIteratorGetUTF8Text(resultIt,
          level).getCString();

      Tesseract.TessPageIteratorBoundingBox(pageIt, level, left, top, right,
          bottom);

      System.out.println("'" + symbol + "': (" + left.getInt() + ", "
          + top.getInt() + ", " + right.getInt() + ", " + bottom.getInt()
          + ")");
    } while (Tesseract.TessPageIteratorNext(pageIt, level) > 0); // next symbol

    System.out.print("\n\n");

    // calculate the time
    System.out.println("time: " + (System.currentTimeMillis() - start) + "ms");

    // delete handle
    Tesseract.TessBaseAPIDelete(handle);
  }
}
