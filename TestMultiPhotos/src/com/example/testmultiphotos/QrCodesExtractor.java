package com.example.testmultiphotos;

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

public class QrCodesExtractor {

	public static ArrayList<String> extract(File dir) throws Exception {

		ArrayList<String> arrayList = new ArrayList<String>();

		for (File file : dir.listFiles()) {
			extractQRCodes(file, arrayList);
		}

		return arrayList;
	}

	private static void extractQRCodes(File file, ArrayList<String> arrayList) throws IOException, NotFoundException {
		int height = 480;
		int width = 800;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileUtils.copyFile(file, output);

		LuminanceSource source = new PlanarYUVLuminanceSource(output.toByteArray(), width, height, 0, 0, width, height, false);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultipleBarcodeReader reader = new QRCodeMultiReader();

		try {
			Result[] retours = reader.decodeMultiple(bitmap);
			for (Result retour : retours) {
				arrayList.add(retour.getText());
			}
		} catch (NotFoundException e) {
			// rien
		} finally {
			IOUtils.closeQuietly(output);
		}
	}
}
