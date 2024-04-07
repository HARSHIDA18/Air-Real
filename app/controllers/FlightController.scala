package controllers

import javax.inject._
import play.api.mvc._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.concurrent.{ExecutionContext, Future}
import play.api.data._
import play.api.data.Forms._
import models.{FlightDAO, FlightFormData}
import play.api.i18n.I18nSupport

@Singleton
class FlightController @Inject()(cc: ControllerComponents, flightDAO: FlightDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  val kafkaProducer = new KafkaProducer[String, String](createProducerConfig())
  val topic = "FlightAdmin"

  val flightFormData: Form[FlightFormData] = Form(
    mapping(
      "aircraftId" -> nonEmptyText,
      "airlinesName" -> nonEmptyText,
      "price" -> number,
      "destination" -> nonEmptyText,
      "source" -> nonEmptyText,
      "departureTime" -> nonEmptyText,
      "arrivalTime" -> nonEmptyText,
      "seatAvailability" -> number
    )(FlightFormData.apply)(FlightFormData.unapply))

  // Define search form for source and destination
  val searchForm: Form[(String, String)] = Form(
    tuple(
      "source" -> nonEmptyText,
      "destination" -> nonEmptyText
    )
  )

  def createProducerConfig(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.flightForm(flightFormData))
  }

  def submitFlightData: Action[AnyContent] = Action.async { implicit request =>
    flightFormData.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.flightForm(formWithErrors)))
      },
      formData => {
        val jsonData =
          s"""
             |{
             |  "aircraft_id": "${formData.aircraftId}",
             |  "airlines_name": "${formData.airlinesName}",
             |  "price": ${formData.price},
             |  "destination": "${formData.destination}",
             |  "source": "${formData.source}",
             |  "departure_time": "${formData.departureTime}",
             |  "arrival_time": "${formData.arrivalTime}",
             |  "seat_availability": ${formData.seatAvailability}
             |}
           """.stripMargin

        val record = new ProducerRecord[String, String](topic, jsonData)
        kafkaProducer.send(record)

        flightDAO.insertFlight(formData).map { _ =>
          Ok("Flight data submitted successfully!")
        }.recover {
          case _ => InternalServerError("Failed to submit flight data")
        }
      }
    )
  }

  def showAvailableFlights: Action[AnyContent] = Action.async { implicit request =>
    val source = request.queryString.get("source").flatMap(_.headOption).getOrElse("")
    val destination = request.queryString.get("destination").flatMap(_.headOption).getOrElse("")
    flightDAO.getFlightsBySourceAndDestination(source, destination).map { flights =>
      Ok(views.html.availableFlights(flights))
    }.recover {
      case _ => InternalServerError("Failed to fetch available flights")
    }
  }

  def searchFlightsForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.searchFlightsForm(searchForm))
  }

  def searchFlights: Action[AnyContent] = Action.async { implicit request =>
    searchForm.bindFromRequest.fold(
      formWithErrors => {
        // If there are errors in the form, render the form again with errors
        Future.successful(BadRequest(views.html.searchFlightsForm(formWithErrors)))
      },
      formData => {
        val (source, destination) = formData
        flightDAO.getFlightsBySourceAndDestination(source, destination).map { flights =>
          Ok(views.html.availableFlights(flights))
        }.recover {
          case _ => InternalServerError("Failed to fetch available flights")
        }
      }
    )
  }
}
