import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json
import models._
import repositories.UserDetailsRepository
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.Properties
import play.api.i18n.{I18nSupport, MessagesApi, MessagesProvider}

@Singleton
class UserBookingController @Inject()(
                                       cc: ControllerComponents,
                                       userDetailsRepository: UserDetailsRepository,
                                       messagesApi: MessagesApi
                                     )(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport{

  val kafkaProducer = new KafkaProducer[String, String](createProducerConfig())
  val topic = "UserDetails"

  val userBookingForm: Form[UserBookingFormData] = Form {
    mapping(
      "username" -> nonEmptyText,
      "aircraftId" -> nonEmptyText,
      "aadharcardNumber" -> nonEmptyText.verifying("Aadharcard number must be 12 digits", _.matches("\\d{12}")),
      "phoneNumber" -> nonEmptyText,
      "email" -> email
    )(UserBookingFormData.apply)(UserBookingFormData.unapply)
  }

  def createProducerConfig(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  def bookFlight: Action[AnyContent] = Action.async { implicit request =>
    userBookingForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest("Invalid form submission"))
      },
      formData => {
        val jsonData =
          s"""
             |{
             |  "username": "${formData.username}",
             |  "aircraftId": "${formData.aircraftId}",
             |  "aadharcardNumber": "${formData.aadharcardNumber}",
             |  "phoneNumber": "${formData.phoneNumber}",
             |  "email": "${formData.email}"
             |}
           """.stripMargin

        val record = new ProducerRecord[String, String](topic, jsonData)
        kafkaProducer.send(record)

        userDetailsRepository.insertUserDetails(formData).map { _ =>
          Ok("User booking details submitted successfully!")
        }.recover {
          case _ => InternalServerError("Failed to submit user booking details")
        }
      }
    )
  }

  def showBookingForm: Action[AnyContent] = Action { implicit request =>
    val messagesProvider = messagesApi.preferred(request)
    Ok(views.html.userBookingForm(userBookingForm)(request, messagesProvider))
  }
}
