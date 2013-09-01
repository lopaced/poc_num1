package com.example.testmultiphotos;

import static com.example.testmultiphotos.Constantes.HEIGHT;
import static com.example.testmultiphotos.Constantes.WIDTH;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

public class ExtractWorker implements Runnable {

	private BlockingQueue<FrameDto> framesQueue;	
	private List<String> results;
	
	public ExtractWorker(BlockingQueue<FrameDto> framesQueue, List<String> results) {
		super();
		this.framesQueue = framesQueue;
		this.results = results;
	}

	@Override
	public void run() {
		while(true) {
	        Log.d(this.getClass().getName(),"Consumer "+Thread.currentThread().getName()+" START");
	        try {
	            FrameDto frame = framesQueue.take();
	            Thread.sleep(new Random().nextInt(100));
	            if(frame.getStatus()==ExtractStatusEnum.KILL) {
	            	Log.d(this.getClass().getName(),"Consumer "+Thread.currentThread().getName()+" KILL");
	                break;
	            }
	            //process queueElement
	            extractQRCodes(frame.getDatas());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        Log.d(this.getClass().getName(),"Consumer "+Thread.currentThread().getName()+" END");
	    }		
	}
	
	private void extractQRCodes(byte[] frame){	
		Log.d(this.getClass().getName(),"extractQRCodes START");
		LuminanceSource source = new PlanarYUVLuminanceSource(frame, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultipleBarcodeReader reader = new QRCodeMultiReader();

		try {
			Result[] retours = reader.decodeMultiple(bitmap);
			for (Result retour : retours) {
				results.add(retour.getText());
			}
			Log.d(this.getClass().getName(),"extractQRCodes STOP");
		} catch (NotFoundException e) {
			// rien
		} finally {			
		}
	}	

}