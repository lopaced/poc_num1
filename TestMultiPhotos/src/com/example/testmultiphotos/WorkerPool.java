package com.example.testmultiphotos;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class WorkerPool {

	private static int DEFAULT_SIZE = 2;
	private int size;
	private BlockingQueue<FrameDto> frames;
	private Set<String> results;
	private ExecutorService executor;
	private QRCodeHandler qrCodeHandler;

	public WorkerPool(BlockingQueue<FrameDto> frames, QRCodeHandler qrCodeHandler) {
		this(frames, DEFAULT_SIZE, qrCodeHandler);
	}

	public WorkerPool(BlockingQueue<FrameDto> frames, int size, QRCodeHandler qrCodeHandler) {
		this.size = size;
		this.frames = frames;
		this.qrCodeHandler = qrCodeHandler;
		results = new ConcurrentSkipListSet<String>();
	}

	public void start() {
		Log.d(this.getClass().getName(), "WorkerPool START");
		executor = Executors.newFixedThreadPool(size);
		results.clear();
		for (int i = 0; i < size; i++) {
			Runnable worker = new ExtractWorker(frames, results, qrCodeHandler);
			executor.execute(worker);
		}
	}

	/**
	 * Arrête le pool de workers et l'ensemble des workers. Cette méthode attend
	 * que les workers soient terminés pour rendre la main à l'appelant.
	 */
	public void stop() {
		Log.d(this.getClass().getName(), "WorkerPool STOP");
		// Send kill signal to threads
		for (int i = 0; i < size; i++) {
			frames.add(new FrameDto(null, ExtractStatusEnum.KILL));
		}
		while (!frames.isEmpty()) {
		}
		Log.d(this.getClass().getName(), "WorkerPool STOP : Frames empty");
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		Log.d(this.getClass().getName(), "WorkerPool STOP : executor terminated");
	}

	public Iterable<String> getResults() {
		return results;
	}

}
