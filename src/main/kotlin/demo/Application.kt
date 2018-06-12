package demo

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.get
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.findAll
import org.springframework.fu.application
import org.springframework.fu.module.data.mongodb.mongodb
import org.springframework.fu.module.jackson.jackson
import org.springframework.fu.module.logging.*
import org.springframework.fu.module.mustache.mustache
import org.springframework.fu.module.webflux.netty.netty
import org.springframework.fu.module.webflux.webflux
import org.springframework.fu.ref
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import javax.annotation.PostConstruct

val app = application {

	logging {
		level(LogLevel.INFO)
		level("org.springframework", LogLevel.DEBUG)
		level<DefaultListableBeanFactory>(LogLevel.WARN)
		logback {
			debug(true)
			consoleAppender()
		}
	}
	mongodb(env["MONGO_URI"] ?: "mongodb://localhost/test")
	webflux {
		server(netty()) {
			mustache()
			codecs {
				jackson()
			}
			routes {
				val reservationRepository = ref<ReservationRepository>()
				GET("/api/reservations") {
					ok().body(reservationRepository.findAll())
				}
				GET("/hello/{name}") {
					ok().body(Flux.just("Hello, ${it.pathVariable("name")}!"))
				}
				GET("/reservations.html") {
					ServerResponse.ok().render("reservations", mapOf("reservations" to reservationRepository.findAll()))
				}
			}
		}
	}
	bean<ReservationApplicationRunner>()
	bean<ReservationRepository>()
}

class ReservationApplicationRunner(val reservationRepository: ReservationRepository) {

	@PostConstruct
	fun init() {

		this.reservationRepository
				.delete()
				.thenMany(Flux.just("A", "B", "C", "D", "E").map { Reservation(name = it) }.flatMap { this.reservationRepository.save(it) })
				.thenMany(this.reservationRepository.findAll())
				.subscribe { println(it) }
	}
}

class ReservationRepository(val reactiveMongoTemplate: ReactiveMongoTemplate) {

	fun delete() = this.reactiveMongoTemplate.dropCollection<Reservation>()

	fun save(r: Reservation) = this.reactiveMongoTemplate.save(r)

	fun findAll() = this.reactiveMongoTemplate.findAll<Reservation>()
}

data class Reservation(@Id val id: String? = null, val name: String? = null)

fun main(args: Array<String>) = app.run(await = true, context = AnnotationConfigApplicationContext())