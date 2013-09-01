package com.example.testmultiphotos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class WorkerPool {

	private static int DEFAULT_SIZE = 2;
	private int size;
	private BlockingQueue<FrameDto> frames;
	private List<String> results;
	private ExecutorService executor;
	private List<Runnable> threads = new ArrayList<Runnable>();

	public WorkerPool(BlockingQueue<FrameDto> frames) {
		this(frames, DEFAULT_SIZE);
	}

	public WorkerPool(BlockingQueue<FrameDto> frames, int size) {
		this.size = size;
		this.frames = frames;
		results = new ArrayList<String>();
	}

	public void start() {
		Log.d(this.getClass().getName(),"WorkerPool START");
		executor = Executors.newFixedThreadPool(size);
		for (int i = 0; i < size; i++) {
			Runnable worker = new ExtractWorker(frames, results);
			executor.execute(worker);
			threads.add(worker);
		}
	}

	public void stop() {
		Log.d(this.getClass().getName(),"WorkerPool STOP");
		//Send kill signal to threads
		for (int i = 0; i < size; i++) {
			frames.add(new FrameDto(null, ExtractStatusEnum.KILL));
		}
		while(!frames.isEmpty()){			
		}
		Log.d(this.getClass().getName(),"WorkerPool STOP : Frames empty");
		executor.shutdown();
		while (!executor.isTerminated()) {			
		}
		Log.d(this.getClass().getName(),"WorkerPool STOP : executor terminated");
	}
	
	public List<String> getResults(){
		return results;
	}

}
