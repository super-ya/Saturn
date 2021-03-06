/**
 * Copyright 2016 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * </p>
 **/

package com.vip.saturn.job.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.vip.saturn.job.threads.SaturnThreadFactory;

public class ThreadTest {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ScheduledThreadPoolExecutor timeoutExecutor = new ScheduledThreadPoolExecutor(
				Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
				new SaturnThreadFactory("-timeout-watchdog"));
		timeoutExecutor.setRemoveOnCancelPolicy(true);

		ExecutorService executorService = Executors.newSingleThreadExecutor(new SaturnThreadFactory("test"));

		ThreadA a = new ThreadA();
		Future<String> future = executorService.submit(a);
		timeoutExecutor.schedule(new TimeoutHandleTask(a), 2, TimeUnit.SECONDS);
		String res = future.get();
		System.out.println("res:" + res);

		a = new ThreadA();
		future = executorService.submit(a);
		res = future.get();
		System.out.println("res:" + res);

	}

}

class TimeoutHandleTask implements Runnable {
	private ThreadA threadA;

	public TimeoutHandleTask(ThreadA threadA) {
		this.threadA = threadA;
	}

	@Override
	public void run() {
		try {
			threadA.getCurrentThread().stop();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}

class ThreadA implements Callable<String> {
	private Thread currentThread;

	public Thread getCurrentThread() {
		return currentThread;
	}

	@Override
	public String call() throws Exception {
		currentThread = Thread.currentThread();
		try {
			System.out.println("i am threadA ;" + currentThread.toString());
			for (int i = 0; i < 1000; i++) {
				Thread.sleep(10);
			}
		} catch (Throwable e) {
			System.out.println("threadA is stopping");
		}
		System.out.println("threadA is stopped");
		return "a";
	}

}