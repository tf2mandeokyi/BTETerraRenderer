package com.mndk.bteterrarenderer.util.concurrent;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class ManualThreadExecutor implements Executor {

	private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

	@Override
	public void execute(@Nonnull Runnable command) {
		tasks.offer(command);
	}

	public void process(int n) {
		for (int i = 0; i < n; ++i) {
			Runnable task = tasks.poll();
			if (task != null) task.run();
		}
	}
}
