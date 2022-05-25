package my.ktbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class AppStarter {
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<AppStarter>(*args)
		}
	}
}
