package jrm.fullserver.security;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import jrm.misc.Log;

/**
 * Class to reload SSL certificate periodically
 * <p>
 * This class is responsible for periodically reloading the SSL certificate used by the server.<br>
 * It extends TimerTask and uses a Timer to schedule the reload operation to run at midnight every day.<br>
 * The reload method of the SslContextFactory is called to refresh the SSL context with the new certificate, and any exceptions
 * during the reload process are logged.
 */
public class SSLReload extends TimerTask {
    /**
     * The Timer object used to schedule the SSL reload task.
     * <p>
     * The Timer is initialized as a daemon thread (true) to ensure that it does not prevent the JVM from shutting down if the main
     * thread finishes execution. It is used to schedule the SSL reload task to run at specific intervals, in this case, at midnight
     * every day.
     * 
     * @see Timer
     */
    private Timer timer = new Timer(true);
    /**
     * The SslContextFactory used to reload the SSL certificate.
     * <p>
     * The SslContextFactory is responsible for managing the SSL context and providing the functionality to reload the SSL certificate
     * when needed. It is passed to the SSLReload class through the constructor and stored as a private field for use in the run method
     * when reloading the certificate.
     * 
     * @see SslContextFactory
     */
    private SslContextFactory sslcontext;

    /**
     * Constructs a new SSLReload instance with the specified SslContextFactory.
     * <p>
     * The constructor initializes the sslcontext field with the provided SslContextFactory, which will be used to reload the SSL
     * certificate when the run method is executed. This constructor is private to enforce the use of the getInstance static method for
     * creating instances of SSLReload, ensuring that the SslContextFactory is properly initialized when creating an instance of the
     * SSLReload class.
     * 
     * @param sslcontext The SslContextFactory used to reload the SSL certificate.
     */
    private SSLReload(SslContextFactory sslcontext) {
        this.sslcontext = sslcontext;
    }

    /**
     * Gets an instance of the SSLReload class with the specified SslContextFactory.
     * <p>
     * This static method serves as a factory method for creating instances of the SSLReload class. It takes an SslContextFactory as a
     * parameter and returns a new instance of SSLReload initialized with the provided SslContextFactory. This method allows for proper
     * initialization of the SSLReload class with the necessary SslContextFactory, ensuring that the SSL certificate can be reloaded
     * correctly when the run method is executed. By using a static factory method, it also provides flexibility in how instances of
     * SSLReload are created and allows for potential future enhancements, such as adding additional parameters or configuration options
     * when creating instances of SSLReload.
     * 
     * @param sslcontext The SslContextFactory used to reload the SSL certificate.
     * 
     * @return an instance of SSLReload initialized with the provided SslContextFactory.
     */
    public static SSLReload getInstance(SslContextFactory sslcontext) {
        return new SSLReload(sslcontext);
    }

    /**
     * The run method is called when the scheduled task is executed.
     * <p>
     * This method is responsible for reloading the SSL certificate using the SslContextFactory's reload method. It logs a message if
     * the reload is successful, and logs an error if any exceptions occur during the reload process. After attempting to reload the SSL
     * certificate, it cancels the current task and schedules the next execution of the task to run at midnight the next day using the
     * schedule method. This ensures that the SSL certificate is reloaded periodically every day at midnight.
     * <p>
     * The method uses a try-catch block to handle any exceptions that may occur during the reload process, ensuring that any errors are
     * properly logged without crashing the application. The use of the cancel and schedule methods allows for continuous scheduling of
     * the SSL reload task at the desired intervals.
     * <p>
     * Note: The actual implementation of the reload method in the SslContextFactory may vary depending on the specific requirements of
     * the application and how the SSL certificate is managed. It is important to ensure that the reload method properly refreshes the
     * SSL context with the new certificate to maintain secure communication for the server.
     * 
     * @see SslContextFactory#reload(java.util.function.Consumer)
     */
    @Override
    public void run() {
        try {
            /*
             * This tells the SSLContextFactory to reload its certificate
             */
            sslcontext.reload(_ -> Log.info("SSL certificate reloaded"));
        } catch (Exception e) {
            Log.err("Error while reloading SSL certificate", e);
        }
        cancel();
        schedule();
    }

    /**
     * Helper method to calculate the Date object representing midnight of the next day. This is used to schedule the SSL reload task to
     * run at midnight every day. It uses the LocalDate and ZoneId classes to determine the correct time for the next midnight based on
     * the system's default time zone.
     * <p>
     * The method first gets the current date using LocalDate.now(), then adds one day to it using plusDays(1) to get the date for
     * tomorrow. It then converts this LocalDate to a Date object representing midnight of the next day by using the atStartOfDay method
     * to get the start of the day and the system's default time zone to convert it to an Instant, which is then converted to a Date
     * object. This Date object is returned and used to schedule the SSL reload task to run at the correct time each day.
     * <p>
     * Note: The use of LocalDate and ZoneId ensures that the calculation of the next midnight is accurate and takes into account any
     * time zone differences, making it suitable for use in applications that may be deployed in different regions or time zones.
     * 
     * @return a Date object representing midnight of the next day.
     */
    private Date getTomorrowMidNight() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Helper method to schedule the SSL reload task to run at midnight the next day. It uses the Timer's schedule method to schedule
     * the task to execute at the time calculated by the getTomorrowMidNight method.
     * <p>
     * This method is called after the SSL reload task is executed to ensure that the next execution of the task is scheduled correctly
     * for the following day. By calling this method after each execution of the run method, it ensures that the SSL reload task
     * continues to run at the desired intervals without requiring manual intervention to reschedule the task each time. This allows for
     * a continuous and automated process of reloading the SSL certificate at midnight every day, ensuring that the server always has
     * the most up-to-date SSL certificate for secure communication.
     * 
     * @see Timer#schedule(java.util.TimerTask, java.util.Date)
     */
    private void schedule() {
        timer.schedule(this, getTomorrowMidNight());
    }

    /**
     * Starts the SSL reload task by scheduling it to run at midnight the next day. This method is called to initiate the periodic SSL
     * certificate reload process when the server starts.
     * <p>
     * By calling the schedule method, it ensures that the SSL reload task is set up to run at the correct intervals, allowing for
     * automatic and continuous reloading of the SSL certificate without requiring manual intervention. This is important for
     * maintaining secure communication for the server, as it ensures that the SSL certificate is always up-to-date and valid.
     * 
     * @see #schedule()
     */
    public void start() {
        schedule();
    }

}
