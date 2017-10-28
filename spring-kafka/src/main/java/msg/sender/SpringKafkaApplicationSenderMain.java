package msg.sender;

import java.applet.AppletContext;
import java.sql.Time;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@EnableAutoConfiguration
@SpringBootApplication
public class SpringKafkaApplicationSenderMain implements CommandLineRunner {

	 @Autowired
	 private KafkaSender kafkaSender;
	 
	 @Autowired
	 private ConfigurableApplicationContext applicationContext;

	    public static void main(String[] args)  {
			ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(SpringKafkaApplicationSenderMain.class)
			.web(WebApplicationType.NONE)
			.run(args);
	    }
	    
		static String fixMsg = 	"8=FIX.4.29=17535=D49=SENDER56=TARGET34=24850=frg52=20100702-11:12:4211=BS0100035492400021=3100=J55=ILA SJ48=YY7722=5167=CS207=J54=160=20100702-11:12:4238=50040=115=ZAR59=010=230";
		private void sendMsg() {
	    	int size = 200;
			ExecutorService executor = Executors.newFixedThreadPool(4);
	    	for(int i=0; i<size;i++) {
	    		executor.execute(()->kafkaSender.sendMessage("darsan", fixMsg));
	    	}
	    	executor.shutdown();
	    	try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	applicationContext.close();

		}

		@Override
		public void run(String... args) throws Exception {

			sendMsg();
		}
}
