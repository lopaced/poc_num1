package test.lecture.qr.code;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

public class MainTestEntryPoint {

	public static void main(String[] args) throws Exception {

		// File file = new File("./img/simpleqrcode.bmp");
		// File file = new File("./img/qrcodes.bmp");
		// File file = new File("./img/multiqr.jpeg");

		// BufferedImage img = ImageIO.read(file);

		// int height = img.getHeight();
		// int width = img.getWidth();

		// int[] data = new int[width * height];

		// img.getRGB(0, 0, width, height, data, 0, width);

		File dir = new File("./img/yum");
		ArrayList<String> arrayList = new ArrayList<String>();

		for (File file : dir.listFiles()) {
			extractQRCodes(file, arrayList);
		}

		for (String qr : arrayList) {
			System.out.println(qr);
		}
	}

	private static void extractQRCodes(File file, ArrayList<String> arrayList) throws IOException, NotFoundException {
		int height = 480;
		int width = 800;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileUtils.copyFile(file, output);

		LuminanceSource source = new PlanarYUVLuminanceSource(output.toByteArray(), width, height, 0, 0, width, height, false);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultipleBarcodeReader reader = new QRCodeMultiReader();

		arrayList.add("----------" + file.getName() + "----------");
		try {
			Result[] retours = reader.decodeMultiple(bitmap);
			for (Result retour : retours) {
				arrayList.add(retour.getText());
			}
		} catch (NotFoundException e) {
			arrayList.add("Pas de QR code");
		} finally {
			IOUtils.closeQuietly(output);
		}
	}
}
