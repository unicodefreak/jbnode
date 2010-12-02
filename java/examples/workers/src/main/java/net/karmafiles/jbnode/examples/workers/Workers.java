package net.karmafiles.jbnode.examples.workers;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 16:40:10
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class Workers {

    public static final String jbNodeVersion = "1.0";

    private static final Logger logger = Logger.getLogger(Workers.class.getName());

    private CompletionService<String> completionService = new ExecutorCompletionService(
            new ThreadPoolExecutor(
                    10, 10, 1, TimeUnit.HOURS, new LinkedBlockingQueue()
            ),
            new LinkedBlockingQueue()
    );

    public byte[] submit(byte[] task) {
        final String taskString = new String(task);

        completionService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                return taskString;
            }
        });

        return null;
    }

    public byte[] poll(byte[] param) throws ExecutionException, InterruptedException {
        Future<String> future = completionService.poll();

        if(future != null) {
            return future.get().getBytes();
        } else {
            return null;
        }
    }
}
